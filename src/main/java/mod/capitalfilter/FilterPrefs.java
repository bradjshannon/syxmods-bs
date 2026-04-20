package mod.capitalfilter;

import snake2d.LOG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Saves and loads filter settings to a properties file so they persist across
 * game sessions. The file is written to:
 * %APPDATA%\songsofsyx\mods\Capital Filter\filter-prefs.properties
 */
public final class FilterPrefs {

    private static Path getPrefsPath() {
        String base = System.getenv("APPDATA");
        if (base == null || base.isEmpty())
            base = System.getProperty("user.home");
        return Paths.get(base, "songsofsyx", "mods", "Capital Filter", "filter-prefs.properties");
    }

    public static void save(FilterState state) {
        try {
            Properties p = new Properties();
            for (int i = 0; i < state.resourceRules.length; i++) {
                p.setProperty("res." + i + ".enabled", String.valueOf(state.resourceRules[i].enabled));
                p.setProperty("res." + i + ".minPercent", String.valueOf(state.resourceRules[i].minPercent));
            }
            p.setProperty("adj.river", String.valueOf(state.requireRiver));
            p.setProperty("adj.ocean", String.valueOf(state.requireOcean));
            p.setProperty("adj.mountain", String.valueOf(state.requireMountain));
            for (int i = 0; i < state.allowedClimates.length; i++) {
                p.setProperty("climate." + i, String.valueOf(state.allowedClimates[i]));
            }
            Path path = getPrefsPath();
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                p.store(out, "Capital Filter preferences");
            }
        } catch (IOException e) {
            LOG.err("[CapitalFilter] FilterPrefs.save failed: " + e);
        }
    }

    public static void load(FilterState state) {
        Path path = getPrefsPath();
        if (!Files.exists(path))
            return;
        try {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(path)) {
                p.load(in);
            }
            for (int i = 0; i < state.resourceRules.length; i++) {
                String en = p.getProperty("res." + i + ".enabled");
                String pct = p.getProperty("res." + i + ".minPercent");
                if (en != null)
                    state.resourceRules[i].enabled = Boolean.parseBoolean(en);
                if (pct != null)
                    state.resourceRules[i].minPercent = Integer.parseInt(pct);
            }
            String river = p.getProperty("adj.river");
            String ocean = p.getProperty("adj.ocean");
            String mountain = p.getProperty("adj.mountain");
            if (river != null)
                state.requireRiver = Boolean.parseBoolean(river);
            if (ocean != null)
                state.requireOcean = Boolean.parseBoolean(ocean);
            if (mountain != null)
                state.requireMountain = Boolean.parseBoolean(mountain);
            for (int i = 0; i < state.allowedClimates.length; i++) {
                String cl = p.getProperty("climate." + i);
                if (cl != null)
                    state.allowedClimates[i] = Boolean.parseBoolean(cl);
            }
        } catch (Exception e) {
            LOG.err("[CapitalFilter] FilterPrefs.load failed: " + e);
        }
    }
}
