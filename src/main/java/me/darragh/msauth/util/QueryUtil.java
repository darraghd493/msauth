package me.darragh.msauth.util;

import lombok.experimental.UtilityClass;

/**
 * A utility class for parsing query strings.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@UtilityClass
public class QueryUtil {
    public static String getQuery(String query, String key) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith(key + "=")) {
                return param.split("=")[1];
            }
        }
        return null;
    }
}
