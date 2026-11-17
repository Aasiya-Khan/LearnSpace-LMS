package database;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Finds the LearnSpace home folder: the folder that holds db.properties and uploads/.
 * Works no matter which folder the app is started from.
 *
 * Order: -Dlearnspace.home=..., then the LEARNSPACE_HOME environment variable,
 * then the project folder the compiled classes live in (e.g. target/classes → project root),
 * then the current folder and its parents, then ~/.learnspace as a last resort.
 */
public final class AppPaths {

    private static final Path HOME = findHome();

    private AppPaths() {}

    public static Path home() { return HOME; }

    public static Path configFile() { return HOME.resolve("db.properties"); }

    /** Turns a stored path such as "uploads/videos/x.mp4" into a real file under the home folder. */
    public static File resolve(String storedPath) {
        Path p = Paths.get(storedPath);
        return (p.isAbsolute() ? p : HOME.resolve(p)).toFile();
    }

    /** Turns a real file under the home folder back into the short path saved in the database. */
    public static String toStored(Path file) {
        Path abs = file.toAbsolutePath().normalize();
        String s = abs.startsWith(HOME) ? HOME.relativize(abs).toString() : abs.toString();
        return s.replace('\\', '/');
    }

    private static Path findHome() {
        String prop = System.getProperty("learnspace.home");
        if (prop != null && !prop.isBlank()) return Paths.get(prop).toAbsolutePath().normalize();
        String env = System.getenv("LEARNSPACE_HOME");
        if (env != null && !env.isBlank()) return Paths.get(env).toAbsolutePath().normalize();

        try {
            Path code = Paths.get(AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path found = searchUp(code);
            if (found != null) return found;
        } catch (Exception ignored) {
            // fall through to the next option
        }
        Path found = searchUp(Paths.get("").toAbsolutePath());
        if (found != null) return found;

        return Paths.get(System.getProperty("user.home"), ".learnspace").toAbsolutePath().normalize();
    }

    private static Path searchUp(Path start) {
        for (Path d = start.toAbsolutePath().normalize(); d != null; d = d.getParent()) {
            if (Files.isRegularFile(d.resolve("db.properties"))) return d;
        }
        return null;
    }
}
