package mod.capitalfilter;

import init.constant.C;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.type.CLIMATE;
import init.type.CLIMATES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the filter panel to the right of the vanilla "Place Capital" panel.
 * Uses raw SPRITE_RENDERER calls against game UI sprites.
 *
 * Panel origin:
 * x = C.DIM().x()/2 + 130 + 8 (130 = half vanilla panel width, 8 = gap)
 * y = 64
 */
public final class FilterPanel {

    // Layout constants
    private static final int ROW_H = 20;
    private static final int BOX_SIZE = 14;
    private static final int LABEL_X_OFF = BOX_SIZE + 4;
    private static final int MARGIN = 8;
    private static final int PANEL_W = 240;
    private static final int PLUS_W = 16;

    // Panel screen position (computed each frame from C.DIM() in case of resize)
    private int panelX, panelY, panelH;

    // Each row stores a bounding rect for click-testing: [x1, y1, x2, y2]
    private final List<int[]> resourceBoxRects = new ArrayList<>();
    private final List<int[]> resourcePlusRects = new ArrayList<>();
    private final List<int[]> resourceMinusRects = new ArrayList<>();
    private final int[] riverBoxRect = new int[4];
    private final int[] oceanBoxRect = new int[4];
    private final int[] mountainBoxRect = new int[4];
    private final List<int[]> climateBoxRects = new ArrayList<>();

    // Reusable single-char text for threshold numbers
    private final snake2d.util.sprite.text.Text thresholdText;

    public FilterPanel() {
        thresholdText = new snake2d.util.sprite.text.Text(UI.FONT().S, 8);
    }

    // -----------------------------------------------------------------------
    // Render
    // -----------------------------------------------------------------------

    /**
     * Called every frame when the capital-placement screen is active.
     */
    public void render(SPRITE_RENDERER r, CandidateCache cache, FilterState filters) {
        // Recompute position each frame (safe if screen resizes)
        panelX = C.WIDTH() / 2 + 130 + 8;
        panelY = 64;

        int resourceRows = filters.resourceRules.length;
        int adjacencyRows = 3;
        int climateRows = filters.allowedClimates.length;
        int statusRows = 1;
        int rows = 1 + resourceRows + 1 + adjacencyRows + 1 + climateRows + 1 + statusRows;
        panelH = rows * ROW_H + MARGIN * 2;

        // --- Panel background -----------------------------------------------
        UI.PANEL().thin.render(r, panelX, panelX + PANEL_W, panelY, panelY + panelH, 0, 0);

        int curY = panelY + MARGIN;

        // --- Title -----------------------------------------------------------
        renderLabel(r, "Capital Candidates", panelX + MARGIN, curY, COLOR.WHITE150);
        curY += ROW_H;

        // --- Resource rows ---------------------------------------------------
        resourceBoxRects.clear();
        resourcePlusRects.clear();
        resourceMinusRects.clear();

        for (int i = 0; i < filters.resourceRules.length; i++) {
            ResourceRule rule = filters.resourceRules[i];
            Minable m = RESOURCES.minables().all().get(i);

            int bx1 = panelX + MARGIN;
            int bx2 = bx1 + BOX_SIZE;
            int by1 = curY + (ROW_H - BOX_SIZE) / 2;
            int by2 = by1 + BOX_SIZE;

            // Checkbox
            renderCheckbox(r, bx1, by1, rule.enabled);
            resourceBoxRects.add(new int[] { bx1, by1, bx2, by2 });

            // Label (resource name)
            renderLabel(r, m.resource.name, bx1 + LABEL_X_OFF, curY,
                    rule.enabled ? COLOR.WHITE150 : COLOR.WHITE65);

            if (rule.enabled) {
                // Threshold: [−] 25 [+]
                int tx = panelX + PANEL_W - MARGIN - PLUS_W * 2 - 24;

                renderLabel(r, "-", tx, curY, COLOR.WHITE150);
                resourceMinusRects.add(new int[] { tx, curY, tx + PLUS_W, curY + ROW_H });

                thresholdText.clear();
                thresholdText.add(rule.minPercent);
                thresholdText.add("%");
                thresholdText.render(r, tx + PLUS_W, tx + PLUS_W + 24, curY, curY + ROW_H);

                renderLabel(r, "+", tx + PLUS_W + 24, curY, COLOR.WHITE150);
                resourcePlusRects.add(new int[] { tx + PLUS_W + 24, curY, tx + PLUS_W + 24 + PLUS_W, curY + ROW_H });
            } else {
                resourceMinusRects.add(null);
                resourcePlusRects.add(null);
            }
            curY += ROW_H;
        }

        // --- Adjacency rows --------------------------------------------------
        renderLabel(r, "Adjacency", panelX + MARGIN, curY, COLOR.WHITE150);
        curY += ROW_H;

        curY = renderAdjRow(r, curY, "River", filters.requireRiver, riverBoxRect);
        curY = renderAdjRow(r, curY, "Ocean", filters.requireOcean, oceanBoxRect);
        curY = renderAdjRow(r, curY, "Mountain", filters.requireMountain, mountainBoxRect);

        // --- Climate rows ----------------------------------------------------
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
            climateBoxRects.add(new int[] { bx1, by1, bx2, by2 });

            renderLabel(r, climate.name, bx1 + LABEL_X_OFF, curY,
                    filters.allowedClimates[i] ? COLOR.WHITE150 : COLOR.WHITE65);
            curY += ROW_H;
        }

        // --- Status line -----------------------------------------------------
        int passing = 0;
        int total = cache.getSites().size();
        if (filters.hasAnyRuleEnabled()) {
            for (CandidateSite site : cache.getSites()) {
                if (FilterEvaluator.passes(site, filters))
                    passing++;
            }
        }
        thresholdText.clear();
        thresholdText.add(passing);
        thresholdText.add(" / ");
        thresholdText.add(total);
        thresholdText.add(" pass");
        thresholdText.render(r, panelX + MARGIN, panelX + PANEL_W - MARGIN, curY, curY + ROW_H);
    }

    // -----------------------------------------------------------------------
    // Input
    // -----------------------------------------------------------------------

    public void onMouseClick(int mx, int my, FilterState filters) {
        // Resource checkboxes
        for (int i = 0; i < resourceBoxRects.size(); i++) {
            int[] rect = resourceBoxRects.get(i);
            if (rect != null && hits(mx, my, rect)) {
                filters.resourceRules[i].enabled = !filters.resourceRules[i].enabled;
                return;
            }
        }
        // Resource threshold +/−
        for (int i = 0; i < resourcePlusRects.size(); i++) {
            int[] plus = resourcePlusRects.get(i);
            int[] minus = resourceMinusRects.get(i);
            if (plus != null && hits(mx, my, plus)) {
                filters.resourceRules[i].minPercent = Math.min(100, filters.resourceRules[i].minPercent + 5);
                return;
            }
            if (minus != null && hits(mx, my, minus)) {
                filters.resourceRules[i].minPercent = Math.max(0, filters.resourceRules[i].minPercent - 5);
                return;
            }
        }
        // Adjacency checkboxes
        if (hits(mx, my, riverBoxRect)) {
            filters.requireRiver = !filters.requireRiver;
            return;
        }
        if (hits(mx, my, oceanBoxRect)) {
            filters.requireOcean = !filters.requireOcean;
            return;
        }
        if (hits(mx, my, mountainBoxRect)) {
            filters.requireMountain = !filters.requireMountain;
            return;
        }
        // Climate checkboxes
        for (int i = 0; i < climateBoxRects.size(); i++) {
            int[] rect = climateBoxRects.get(i);
            if (rect != null && hits(mx, my, rect)) {
                filters.allowedClimates[i] = !filters.allowedClimates[i];
                return;
            }
        }
    }

    public void onMouseHover(int mx, int my) {
        // No hover effects needed for MVP
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private int renderAdjRow(SPRITE_RENDERER r, int curY, String label, boolean checked, int[] rectOut) {
        int bx1 = panelX + MARGIN;
        int by1 = curY + (ROW_H - BOX_SIZE) / 2;
        renderCheckbox(r, bx1, by1, checked);
        rectOut[0] = bx1;
        rectOut[1] = by1;
        rectOut[2] = bx1 + BOX_SIZE;
        rectOut[3] = by1 + BOX_SIZE;
        renderLabel(r, label, bx1 + LABEL_X_OFF, curY, checked ? COLOR.WHITE150 : COLOR.WHITE65);
        return curY + ROW_H;
    }

    private void renderCheckbox(SPRITE_RENDERER r, int x1, int y1, boolean checked) {
        int x2 = x1 + BOX_SIZE;
        int y2 = y1 + BOX_SIZE;
        // Outer border using thin UI panel (1-cell 24px tile — we use it as a small
        // box)
        UI.PANEL().thin.render(r, x1, x2, y1, y2, 0, 0);
        if (checked) {
            // Fill interior with green tint by rendering a colored sprite
            COLOR.GREEN200.bind();
            UI.PANEL().thin.render(r, x1 + 2, x2 - 2, y1 + 2, y2 - 2, 0, 0);
            COLOR.unbind();
        }
    }

    private void renderLabel(SPRITE_RENDERER r, CharSequence text, int x1, int y1, COLOR color) {
        color.bind();
        thresholdText.clear();
        thresholdText.add(text);
        thresholdText.render(r, x1, x1 + PANEL_W - MARGIN * 2, y1, y1 + ROW_H);
        COLOR.unbind();
    }

    private static boolean hits(int mx, int my, int[] rect) {
        return mx >= rect[0] && mx <= rect[2] && my >= rect[1] && my <= rect[3];
    }
}
