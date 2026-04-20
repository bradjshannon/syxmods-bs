package mod.capitalfilter;

public final class ResourceRule {

    /** Whether this rule is active and will be evaluated. */
    public boolean enabled = false;

    /**
     * Minimum required score as an integer percentage (0–100).
     * The site's {@code resourceScore * 100} must be >= this value.
     */
    public int minPercent = 25;
}
