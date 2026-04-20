package mod.capitalfilter;

public final class FilterState {

    /** Per-minable resource rules; length = RESOURCES.minables().size(). */
    public final ResourceRule[] resourceRules;

    public boolean requireRiver = false;
    public boolean requireOcean = false;
    public boolean requireMountain = false;

    /**
     * Per-climate allow flags; length = CLIMATES.ALL().size().
     * If all false → climate filter is inactive (any climate passes).
     */
    public boolean[] allowedClimates;

    /**
     * Constructed once game resources are loaded (from
     * CapitalFilterInstance.initComponents).
     * 
     * @param resourceCount RESOURCES.minables().size()
     * @param climateCount  CLIMATES.ALL().size()
     */
    public FilterState(int resourceCount, int climateCount) {
        resourceRules = new ResourceRule[resourceCount];
        for (int i = 0; i < resourceCount; i++) {
            resourceRules[i] = new ResourceRule();
        }
        allowedClimates = new boolean[climateCount];
    }

    /**
     * @return true if at least one rule is enabled (resource, adjacency, or
     *         climate).
     */
    public boolean hasAnyRuleEnabled() {
        for (ResourceRule rule : resourceRules) {
            if (rule.enabled)
                return true;
        }
        if (requireRiver || requireOcean || requireMountain)
            return true;
        for (boolean b : allowedClimates) {
            if (b)
                return true;
        }
        return false;
    }
}
