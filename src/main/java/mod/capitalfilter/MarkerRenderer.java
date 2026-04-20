package mod.capitalfilter;

import init.constant.C;
import snake2d.util.color.COLOR;
import world.WORLD;
import world.map.regions.centre.WCentre;
import world.overlay.EThings;

import java.util.List;

public final class MarkerRenderer {

    public void render(List<CandidateSite> sites, FilterState filters) {
        if (!filters.hasAnyRuleEnabled())
            return;

        EThings things = WORLD.OVERLAY().things;
        int w = WCentre.TILE_DIM * C.TILE_SIZE;
        int h = WCentre.TILE_DIM * C.TILE_SIZE;

        for (CandidateSite site : sites) {
            if (FilterEvaluator.passes(site, filters)) {
                things.hover(
                        site.tx1 * C.TILE_SIZE,
                        site.ty1 * C.TILE_SIZE,
                        w, h,
                        COLOR.GREEN200, // (0,255,0) — bright enough to read over world tiles
                        true);
            }
        }
    }
}
