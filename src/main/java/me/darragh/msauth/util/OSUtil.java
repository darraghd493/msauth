package me.darragh.msauth.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class for identifying the operating system.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@UtilityClass
class OSUtil {
    public static final OperatingSystem OS = getOS();

    /**
     * Identifies the operating system.
     *
     * @return The operating system.
     */
    private static @NotNull OperatingSystem getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (os.contains("mac")) {
            return OperatingSystem.MAC;
        } else if (os.contains("nix") || os.contains("nux")) {
            return OperatingSystem.LINUX;
        } else {
            return OperatingSystem.UNKNOWN;
        }
    }

    public enum OperatingSystem {
        WINDOWS,
        MAC,
        LINUX,
        UNKNOWN
    }
}
