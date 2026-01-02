package me.darragh.msauth.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class for parsing query strings.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@UtilityClass
public class QueryUtil {
    public static @Nullable String getQuery(@NotNull String query, @NotNull String key) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith(key + "=")) {
                return param.split("=")[1];
            }
        }
        return null;
    }
}
