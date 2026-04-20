package mod.capitalfilter;

import java.util.ArrayList;
import java.util.List;

import init.resources.Minable;
import init.resources.RESOURCES;
import init.type.CLIMATE;
import init.type.CLIMATES;
import init.type.TERRAINS;
import world.WORLD;
import world.map.regions.centre.WCentre;
import world.map.regions.centre.WorldCentrePlacablity;
import world.map.terrain.WorldTerrainInfo;
import snake2d.LOG;

public final class CandidateCache {

    private List<CandidateSite> sites = new ArrayList<>();
    private int lastSeed = Integer.MIN_VALUE;
    private int resourceCount;

    /** Call once after RESOURCES are loaded (i.e. from initComponents). */
    public void init() {
        resourceCount = RESOURCES.minables().all().size();
        LOG.ln("[CapitalFilter] CandidateCache.init — resourceCount=" + resourceCount);
    }

    /**
     * Rebuilds the candidate list if the world seed has changed since last build.
     */
    public void rebuildIfNeeded() {
        // Guard: world not yet generated
        if (WORLD.THEIGHT() <= 0)
            return;

        int seed = WORLD.GEN().seed;
        if (seed == lastSeed)
            return;
        lastSeed = seed;
        rebuild();
    }

    /** Returns the current list of valid capital candidate sites. */
    public List<CandidateSite> getSites() {
        return sites;
    }

    // -----------------------------------------------------------------------

    private void rebuild() {
        sites.clear();

        WorldTerrainInfo info = new WorldTerrainInfo();
        int W = WORLD.TWIDTH();
        int H = WORLD.THEIGHT();
        int dim = WCentre.TILE_DIM; // 3
        LOG.ln("[CapitalFilter] rebuild start — world=" + W + "x" + H);

        for (int ty1 = 0; ty1 + dim <= H; ty1++) {
            for (int tx1 = 0; tx1 + dim <= W; tx1++) {
                // Skip tiles where the game already says placement is blocked
                if (WorldCentrePlacablity.terrain(tx1, ty1) != null)
                    continue;

                info.initCity(tx1, ty1);
                sites.add(buildSite(tx1, ty1, info));
            }
        }
        LOG.ln("[CapitalFilter] rebuild done — candidates=" + sites.size());
    }

    private CandidateSite buildSite(int tx1, int ty1, WorldTerrainInfo info) {
        // --- Resource scores --------------------------------------------------
        // Score = 4 * Σ(terrainFraction * minable.terrainPref) clamped to [0,1]
        double[] scores = new double[resourceCount];
        int rIdx = 0;
        snake2d.util.sets.LIST<Minable> minables = RESOURCES.minables().all();
        snake2d.util.sets.LIST<init.type.TERRAIN> terrains = TERRAINS.ALL();
        for (int mi = 0; mi < minables.size(); mi++) {
            Minable m = minables.get(mi);
            double score = 0.0;
            for (int ti = 0; ti < terrains.size(); ti++) {
                score += info.get(terrains.get(ti)).getD() * m.terrain(terrains.get(ti));
            }
            score = Math.min(score * 4.0, 1.0);
            scores[rIdx++] = score;
        }

        // --- Adjacency checks -------------------------------------------------
        int cx = tx1 + 1;
        int cy = ty1 + 1;

        // River: centre tile or any orthogonal neighbor
        boolean river = WORLD.WATER().RIVER.is(cx, cy) ||
                WORLD.WATER().RIVER.is(cx - 1, cy) ||
                WORLD.WATER().RIVER.is(cx + 1, cy) ||
                WORLD.WATER().RIVER.is(cx, cy - 1) ||
                WORLD.WATER().RIVER.is(cx, cy + 1);

        // Ocean: any tile in the 3x3 footprint or immediate ring
        boolean ocean = false;
        outer: for (int dy = -1; dy <= WCentre.TILE_DIM; dy++) {
            for (int dx = -1; dx <= WCentre.TILE_DIM; dx++) {
                int tx = tx1 + dx;
                int ty = ty1 + dy;
                if (WORLD.WATER().OCEAN.is.is(tx, ty)) {
                    ocean = true;
                    break outer;
                }
            }
        }

        // Mountain: scan surrounding ring for a mountain tile
        boolean mountain = false;
        outer2: for (int dy = -1; dy <= WCentre.TILE_DIM; dy++) {
            for (int dx = -1; dx <= WCentre.TILE_DIM; dx++) {
                if (dx >= 0 && dx < WCentre.TILE_DIM && dy >= 0 && dy < WCentre.TILE_DIM)
                    continue; // skip interior
                int tx = tx1 + dx;
                int ty = ty1 + dy;
                if (WORLD.MOUNTAIN().coversTile(tx, ty)) {
                    mountain = true;
                    break outer2;
                }
            }
        }

        // --- Climate ----------------------------------------------------------
        CLIMATE climate = WORLD.CLIMATE().getter.get(cx, cy);
        if (climate == null) {
            LOG.err("[CapitalFilter] null climate at (" + cx + "," + cy + ") — using fallback");
            climate = CLIMATES.TEMP(); // fallback
        }

        return new CandidateSite(tx1, ty1, scores, river, ocean, mountain, climate);
    }
}
