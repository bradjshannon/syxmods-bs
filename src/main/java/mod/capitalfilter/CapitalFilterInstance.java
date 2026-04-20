package mod.capitalfilter;

import java.io.IOException;

import init.resources.RESOURCES;
import init.type.CLIMATES;
import script.SCRIPT;
import snake2d.LOG;
import snake2d.Renderer;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import view.main.VIEW;
import view.world.generator.WorldViewGenerator;
import world.WORLD;

public final class CapitalFilterInstance implements SCRIPT.SCRIPT_INSTANCE {

    private CandidateCache cache;
    private FilterState filters;
    private FilterPanel panel;
    private MarkerRenderer markers;

    /** True once initComponents() has run for this session. */
    private boolean initialized = false;
    /** True if initComponents() threw — prevents NPEs in subsequent calls. */
    private boolean initFailed = false;

    public CapitalFilterInstance() {
    }

    // -----------------------------------------------------------------------
    // SCRIPT_INSTANCE
    // -----------------------------------------------------------------------

    @Override
    public void update(double ds) {
        if (!(VIEW.current() instanceof WorldViewGenerator)) return;
        if (!WORLD.GEN().hasGeneratedTerrain)               return; // too early (species select etc.)
        if (WORLD.GEN().playerX >= 0)                       return; // capital already placed
        if (initFailed)                                     return;

        if (!initialized) {
            initComponents();
            initialized = true;
        }
        try {
            cache.rebuildIfNeeded();
        } catch (Exception e) {
            LOG.err("[CapitalFilter] rebuildIfNeeded failed: " + e);
        }
    }

    @Override
    public void render(Renderer r, float ds) {
        if (!(VIEW.current() instanceof WorldViewGenerator)) return;
        if (!initialized || initFailed)                     return;
        if (!WORLD.GEN().hasGeneratedTerrain)               return;
        if (WORLD.GEN().playerX >= 0)                       return;

        try {
            // Re-add overlay markers every frame (EThings clears each frame).
            // FilterPanel renders itself via its Interrupter.render() in current.uiManager.
            markers.render(cache.getSites(), filters);
        } catch (Exception e) {
            LOG.err("[CapitalFilter] render failed: " + e);
        }
    }

    @Override
    public void save(FilePutter file) {
        // Filter state is intentionally not persisted across saves (MVP scope).
        // If the saved game re-enters the map screen the panel will be blank, which is
        // acceptable.
    }

    @Override
    public void load(FileGetter file) throws IOException {
        // Filter state starts fresh each session.
        // Hide and discard the old panel interrupter before re-initializing so we don't
        // accumulate stale Interrupter instances in uiManager after a game reload.
        if (panel != null) {
            panel.hide();
        }
        initialized = false;
    }

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    private void initComponents() {
        try {
            int resourceCount = RESOURCES.minables().all().size();
            int climateCount  = CLIMATES.ALL().size();
            LOG.ln("[CapitalFilter] init — resources=" + resourceCount + " climates=" + climateCount);

            cache   = new CandidateCache();
            cache.init();
            cache.rebuildIfNeeded();

            filters = new FilterState(resourceCount, climateCount);

            // Register the panel as a pinned Interrupter in the current view's uiManager.
            // This makes hover/click properly consumed (prevents capital placement on panel
            // clicks) and ensures the panel renders in the right layer order.
            panel = new FilterPanel(cache, filters);
            ((WorldViewGenerator) VIEW.current()).uiManager.add(panel);

            markers = new MarkerRenderer();
            LOG.ln("[CapitalFilter] init complete — sites=" + cache.getSites().size());
        } catch (Exception e) {
            LOG.err("[CapitalFilter] initComponents failed: " + e);
            initFailed = true;
        }
    }
}
