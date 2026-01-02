package me.darragh.msauth.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A provider class for a singleton instance of Gson.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public final class GsonProvider {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    public static Gson get() {
        return GSON;
    }
}
