package mod.capitalfilter;

import init.constant.C;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.type.CLIMATE;
import init.type.CLIMATES;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import util.gui.misc.GBox;
import snake2d.LOG;
import view.interrupter.Interrupter;
import view.main.VIEW;
import view.subview.GameWindow;
import view.world.generator.WorldViewGenerator;
import world.WORLD;

import java.util.ArrayList;
import java.util.List;

/**
 * The filter panel rendered to the right of the vanilla "Place Capital" panel.
 *
 * Implemented as an {@link Interrupter} so that:
 * - It properly consumes hover/click events (prevents capital placement on
 * panel clicks)
 * - It renders in the correct layer (after inters.manager, so settings menus
 * appear on top)
 * - It can be pinned into current.uiManager to survive
 * WorldViewGenerator.reset() calls
 */
public final class FilterPanel extends Interrupter {

    // Layout constants
    private static final int ROW_H = 20;
    private static final int BOX_SIZE = 14;
    private static final int LABEL_X_OFF = BOX_SIZE + 4;
    private static final int MARGIN = 8;
    private static final int PANEL_W = 240;
    private static final int PLUS_W = 16;
    private static final int NUM_W = 36; // width for the "100%" number field

    // Panel screen position — recomputed each frame
    private int panelX, panelY, panelH;

    // Hit rects: [x1, y1, x2, y2]
    private final List<int[]> resourceBoxRects = new ArrayList<>();
    private final List<int[]> resourcePlusRects = new ArrayList<>();
    private final List<int[]> resourceMinusRects = new ArrayList<>();
    private final int[] riverBoxRect = new int[4];
    private final int[] oceanBoxRect = new int[4];
    private final int[] mountainBoxRect = new int[4];
    private final List<int[]> climateBoxRects = new ArrayList<>();
    private final int[] jumpButtonRect = new int[4];
    private int jumpIndex = -1;

    // Reusable text buffer (size 64 to handle long labels, thresholds, and status
    // line)
    private final snake2d.util.sprite.text.Text labelText;

    private final CandidateCache cache;
    private final FilterState filters;

    /**
     * @param cache   shared candidate cache (mutable; this panel reads it each
     *                render)
     * @param filters shared filter state (mutable; clicks toggle it in-place)
     */
    public FilterPanel(CandidateCache cache, FilterState filters) {
        // persistent=true -> survives when new interrupters are added on top
        // pinned=true -> survives uiManager.clear() (WorldViewGenerator.reset())
        // desturber=false -> adding this panel does NOT hide other interrupters
        super(true, true, false);
        this.cache = cache;
        this.filters = filters;
        labelText = new snake2d.util.sprite.text.Text(UI.FONT().S, 64);
    }

    // -----------------------------------------------------------------------
    // Interrupter overrides
    // -----------------------------------------------------------------------

    /**
     * Returns true (consumes hover) when the mouse is within the panel bounds.
     * This prevents ToolManager from treating the area as a world-click target,
     * so clicking the panel does not accidentally place the capital.
     */
    @Override
    protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
        if (!WORLD.GEN().hasGeneratedTerrain)
            return false;
        if (WORLD.GEN().playerX >= 0)
            return false;
        computePosition();
        return mCoo.x() >= panelX && mCoo.x() < panelX + PANEL_W
                && mCoo.y() >= panelY && mCoo.y() < panelY + panelH;
    }

    /**
     * Called only when this panel is the hovered interrupter (mouse is inside
     * panel).
     */
    @Override
    protected void mouseClick(MButt button) {
        if (button == MButt.LEFT) {
            onMouseClick(VIEW.mouse().x(), VIEW.mouse().y());
        }
    }

    @Override
    protected void hoverTimer(GBox text) {
        // No tooltip for MVP
    }

    /**
     * Renders the panel. Returns true so subsequent interrupters (e.g. settings
     * menu)
     * can still render on top of us. Called only after inters.manager.render()
     * succeeds,
     * so global overlays correctly occlude the panel.
     */
    @Override
    protected boolean render(Renderer r, float ds) {
        if (!WORLD.GEN().hasGeneratedTerrain)
            return true;
        if (WORLD.GEN().playerX >= 0)
            return true;
        renderPanel(r);
        return true;
    }

    @Override
    protected boolean update(float ds) {
        return true;
    }

    /**
     * Called by CapitalFilterInstance.load() to deactivate this panel before
     * re-init.
     */
    public void dispose() {
        hide();
    }

    // -----------------------------------------------------------------------
    // Rendering helpers
    // -----------------------------------------------------------------------

    // Minimap occupies the top-right corner: ~36px button bar + 262px map body.
    // Position the filter panel just below it with a small gap.
    private static final int MINIMAP_BOTTOM = 36 + 262 + 4;

    private void computePosition() {
        panelX = C.WIDTH() - PANEL_W - 4;
        panelY = MINIMAP_BOTTOM;
        int rows = 1 // title
                + filters.resourceRules.length // resource rows
                + 1 // "Adjacency" header
                + 3 // river / ocean / mountain
                + 1 // "Climate" header
                + filters.allowedClimates.length // climate rows
                + 1 // status line
                + 1 // "next result" button
                + 1; // bottom padding row
        panelH = rows * ROW_H + MARGIN * 2;
    }

    private void renderPanel(SPRITE_RENDERER r) {
        computePosition();

        UI.PANEL().thin.render(r, panelX, panelX + PANEL_W, panelY, panelY + panelH, 0, 0);

        int curY = panelY + MARGIN;

        // Title
        renderLabel(r, "Capital Candidates", panelX + MARGIN, curY, COLOR.WHITE150);
        curY += ROW_H;

        // Resource rows
        resourceBoxRects.clear();
        resourcePlusRects.clear();
        resourceMinusRects.clear();

        for (int i = 0; i < filters.resourceRules.length; i++) {
            ResourceRule rule = filters.resourceRules[i];
            Minable m = RESOURCES.minables().all().get(i);

            int bx1 = panelX + MARGIN;
            int bx2 = bx1 + BOX_SIZE;
            int by1 = curY + (ROW_H - BOX_SIZE) / 2 - 2;
            int by2 = by1 + BOX_SIZE;

            renderCheckbox(r, bx1, by1, rule.enabled);
            resourceBoxRects.add(new int[] { bx1, curY, panelX + PANEL_W - MARGIN, curY + ROW_H });

            renderLabel(r, m.resource.name, bx1 + LABEL_X_OFF, curY,
                    rule.enabled ? COLOR.WHITE150 : COLOR.WHITE65);

            if (rule.enabled) {
                int tx = panelX + PANEL_W - MARGIN - PLUS_W * 2 - NUM_W;

                renderLabel(r, "-", tx, curY, COLOR.WHITE150);
                resourceMinusRects.add(new int[] { tx, curY, tx + PLUS_W, curY + ROW_H });

                labelText.clear();
                labelText.add(rule.minPercent);
                labelText.add("%");
                labelText.render(r, tx + PLUS_W, tx + PLUS_W + NUM_W, curY, curY + ROW_H);

                renderLabel(r, "+", tx + PLUS_W + NUM_W, curY, COLOR.WHITE150);
                resourcePlusRects.add(new int[] { tx + PLUS_W + NUM_W, curY,
                        tx + PLUS_W + NUM_W + PLUS_W, curY + ROW_H });
            } else {
                resourceMinusRects.add(null);
                resourcePlusRects.add(null);
            }
            curY += ROW_H;
        }

        // Adjacency section
        renderLabel(r, "Adjacency", panelX + MARGIN, curY, COLOR.WHITE150);
        curY += ROW_H;

        curY = renderAdjRow(r, curY, "River", filters.requireRiver, riverBoxRect);
        curY = renderAdjRow(r, curY, "Ocean", filters.requireOcean, oceanBoxRect);
        curY = renderAdjRow(r, curY, "Mountain", filters.requireMountain, mountainBoxRect);

        // Climate section
        renderLabel(r, "Climate", panelX + MARGIN, curY, COLOR.WHITE150);
        curY += ROW_H;

        climateBoxRects.clear();
        for (int i = 0; i < filters.allowedClimates.length; i++) {
            CLIMATE climate = CLIMATES.ALL().get(i);
            int bx1 = panelX + MARGIN;
            int by1 = curY + (ROW_H - BOX_SIZE) / 2;
            int bx2 = bx1 + BOX_SIZE;
            int by2 = by1 + BOX_SIZE;

            renderCheckbox(r, bx1, by1, filters.allowedClimates[i]);
            climateBoxRects.add(new int[] { bx1, curY, panelX + PANEL_W - MARGIN, curY + ROW_H });

            renderLabel(r, climate.name, bx1 + LABEL_X_OFF, curY,
                    filters.allowedClimates[i] ? COLOR.WHITE150 : COLOR.WHITE65);
            curY += ROW_H;
        }

        // Status line
        int passing = 0;
        int total = cache.getSites().size();
        if (filters.hasAnyRuleEnabled()) {
            for (CandidateSite site : cache.getSites()) {
                if (FilterEvaluator.passes(site, filters))
                    passing++;
            }
        }
        labelText.clear();
        labelText.add(passing);
        labelText.add(" / ");
        labelText.add(total);
        labelText.add(" pass");
        labelText.render(r, panelX + MARGIN, panelX + PANEL_W - MARGIN, curY, curY + ROW_H);
        curY += ROW_H;
        renderJumpButton(r, curY);
    }

    private int renderAdjRow(SPRITE_RENDERER r, int curY, String label,
            boolean checked, int[] rectOut) {
        int bx1 = panelX + MARGIN;
        int by1 = curY + (ROW_H - BOX_SIZE) / 2 - 2;
        renderCheckbox(r, bx1, by1, checked);
        rectOut[0] = bx1;
        rectOut[1] = curY;
        rectOut[2] = panelX + PANEL_W - MARGIN;
        rectOut[3] = curY + ROW_H;
        renderLabel(r, label, bx1 + LABEL_X_OFF, curY,
                checked ? COLOR.WHITE150 : COLOR.WHITE65);
        return curY + ROW_H;
    }

    private void renderCheckbox(SPRITE_RENDERER r, int x1, int y1, boolean checked) {
        int x2 = x1 + BOX_SIZE;
        int y2 = y1 + BOX_SIZE;
        // Solid white-grey border so the box is always visible against the panel
        COLOR.WHITE150.render(r, x1, x2, y1, y2);
        // Dark interior (unchecked background)
        COLOR.WHITE20.render(r, x1 + 2, x2 - 2, y1 + 2, y2 - 2);
        if (checked) {
            // Green fill inside when active
            COLOR.GREEN200.render(r, x1 + 3, x2 - 3, y1 + 3, y2 - 3);
        }
    }

    private void renderLabel(SPRITE_RENDERER r, CharSequence text,
            int x1, int y1, COLOR color) {
        color.bind();
        labelText.clear();
        labelText.add(text);
        labelText.render(r, x1, x1 + PANEL_W - MARGIN * 2, y1, y1 + ROW_H);
        COLOR.unbind();
    }

    // -----------------------------------------------------------------------
    // Input
    // -----------------------------------------------------------------------

    private void onMouseClick(int mx, int my) {
        // Jump button (before everything else)
        if (hits(mx, my, jumpButtonRect)) {
            advanceToNextResult();
            return;
        }
        // Resource threshold +/− (before checkbox rects because checkbox rects are
        // expanded to full row)
        for (int i = 0; i < resourcePlusRects.size(); i++) {
            int[] plus = resourcePlusRects.get(i);
            int[] minus = resourceMinusRects.get(i);
            if (plus != null && hits(mx, my, plus)) {
                filters.resourceRules[i].minPercent = Math.min(100, filters.resourceRules[i].minPercent + 20);
                FilterPrefs.save(filters);
                return;
            }
            if (minus != null && hits(mx, my, minus)) {
                filters.resourceRules[i].minPercent = Math.max(0, filters.resourceRules[i].minPercent - 20);
                FilterPrefs.save(filters);
                return;
            }
        }
        // Resource checkboxes
        for (int i = 0; i < resourceBoxRects.size(); i++) {
            int[] rect = resourceBoxRects.get(i);
            if (rect != null && hits(mx, my, rect)) {
                filters.resourceRules[i].enabled = !filters.resourceRules[i].enabled;
                FilterPrefs.save(filters);
                return;
            }
        }
        // Adjacency checkboxes
        if (hits(mx, my, riverBoxRect)) {
            filters.requireRiver = !filters.requireRiver;
            FilterPrefs.save(filters);
            return;
        }
        if (hits(mx, my, oceanBoxRect)) {
            filters.requireOcean = !filters.requireOcean;
            FilterPrefs.save(filters);
            return;
        }
        if (hits(mx, my, mountainBoxRect)) {
            filters.requireMountain = !filters.requireMountain;
            FilterPrefs.save(filters);
            return;
        }
        // Climate checkboxes
        for (int i = 0; i < climateBoxRects.size(); i++) {
            int[] rect = climateBoxRects.get(i);
            if (rect != null && hits(mx, my, rect)) {
                filters.allowedClimates[i] = !filters.allowedClimates[i];
                FilterPrefs.save(filters);
                return;
            }
        }
    }

    private static boolean hits(int mx, int my, int[] rect) {
        return mx >= rect[0] && mx <= rect[2] && my >= rect[1] && my <= rect[3];
    }

    private void renderJumpButton(SPRITE_RENDERER r, int curY) {
        int bx1 = panelX + MARGIN;
        int bx2 = panelX + PANEL_W - MARGIN;
        jumpButtonRect[0] = bx1;
        jumpButtonRect[1] = curY;
        jumpButtonRect[2] = bx2;
        jumpButtonRect[3] = curY + ROW_H;
        // Solid border + dark interior, same pattern as checkboxes
        COLOR.WHITE150.render(r, bx1, bx2, curY, curY + ROW_H);
        COLOR.WHITE20.render(r, bx1 + 1, bx2 - 1, curY + 1, curY + ROW_H - 1);
        renderLabel(r, "Next result", bx1 + MARGIN, curY, COLOR.WHITE150);
    }

    private void advanceToNextResult() {
        List<CandidateSite> passing = new ArrayList<>();
        for (CandidateSite s : cache.getSites()) {
            if (FilterEvaluator.passes(s, filters))
                passing.add(s);
        }
        if (passing.isEmpty())
            return;
        jumpIndex = (jumpIndex + 1) % passing.size();
        centerOnSite(passing.get(jumpIndex));
    }

    private void centerOnSite(CandidateSite site) {
        if (!(VIEW.current() instanceof WorldViewGenerator))
            return;
        try {
            WorldViewGenerator gen = (WorldViewGenerator) VIEW.current();
            java.lang.reflect.Field f = WorldViewGenerator.class.getDeclaredField("window");
            f.setAccessible(true);
            GameWindow window = (GameWindow) f.get(gen);
            window.centerAtTile(site.tx1 + 1, site.ty1 + 1);
        } catch (Exception e) {
            LOG.err("[CapitalFilter] centerOnSite failed: " + e);
        }
    }
}
