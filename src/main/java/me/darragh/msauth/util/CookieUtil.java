package me.darragh.msauth.util;

import lombok.experimental.UtilityClass;

/**
 * Handles the parsing of a cookie file to try and extract information usable for authentication via. {@link me.darragh.msauth.cookie.CookieAuthenticator}.
 *
 * @author darraghd493
 * @since 1.1.0
 */
@UtilityClass
public class CookieUtil {
    /**
     * Reformats a cookie file's contents into a simple cookie string usable in HTTP requests.
     *
     * @param cookies The contents of the cookie file.
     * @return The reformatted cookie string.
     */
    public static String reformatCookies(String cookies) {
        StringBuilder builder = new StringBuilder();

        cookies.lines().forEach(line -> {
            String[] segments = line.split("\t");
            if (segments.length < 7) return; // skip malformed lines
            builder.append(segments[5].trim())
                    .append("=")
                    .append(segments[6].trim())
                    .append("; ");
        });

        return builder.toString();
    }
}
