package mod.capitalfilter;

import snake2d.LOG;

public final class FilterEvaluator {

    private FilterEvaluator() {
    }

    /**
     * Returns true if {@code site} passes all active rules in {@code filters}.
     * Always returns false when no rules are enabled (nothing to show).
     */
    public static boolean passes(CandidateSite site, FilterState filters) {
        if (!filters.hasAnyRuleEnabled())
            return false;

        // Resource rules
        for (int i = 0; i < filters.resourceRules.length; i++) {
            ResourceRule rule = filters.resourceRules[i];
            if (rule.enabled && site.resourceScores[i] < rule.minPercent / 100.0) {
                return false;
            }
        }

        // Adjacency rules
        if (filters.requireRiver && !site.adjacentRiver)
            return false;
        if (filters.requireOcean && !site.adjacentOcean)
            return false;
        if (filters.requireMountain && !site.adjacentMountain)
            return false;

        // Climate rules — active only when at least one climate is allowed
        boolean climateFilterActive = false;
        for (boolean b : filters.allowedClimates) {
            if (b) {
                climateFilterActive = true;
                break;
            }
        }
        if (climateFilterActive) {
            int ci = site.climate.index();
            if (ci < 0 || ci >= filters.allowedClimates.length) {
                LOG.err("[CapitalFilter] climate index out of bounds: " + ci + " (len=" + filters.allowedClimates.length
                        + ")");
                return false;
            }
            if (!filters.allowedClimates[ci])
                return false;
        }

        return true;
    }
}
