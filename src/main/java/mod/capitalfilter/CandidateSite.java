package mod.capitalfilter;

import init.type.CLIMATE;

public final class CandidateSite {

    /** Top-left tile coords of the 3×3 footprint. */
    public final int tx1, ty1;

    /**
     * Per-minable resource score in [0, 1].
     * Indexed by {@code Minable.index}; length = RESOURCES.minables().size() at
     * scan time.
     */
    public final double[] resourceScores;

    public final boolean adjacentRiver;
    public final boolean adjacentOcean;
    public final boolean adjacentMountain;

    /** Climate of the centre tile of the footprint. */
    public final CLIMATE climate;

    public CandidateSite(int tx1, int ty1,
            double[] resourceScores,
            boolean adjacentRiver,
            boolean adjacentOcean,
            boolean adjacentMountain,
            CLIMATE climate) {
        this.tx1 = tx1;
        this.ty1 = ty1;
        this.resourceScores = resourceScores;
        this.adjacentRiver = adjacentRiver;
        this.adjacentOcean = adjacentOcean;
        this.adjacentMountain = adjacentMountain;
        this.climate = climate;
    }
}
