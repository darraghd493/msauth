package me.darragh.msauth.util;

import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Builder class for constructing URL-encoded form data strings.
 *
 * @author darraghd493
 * @since 1.1.0
 */
public class FormBuilder {
    private final Map<String, String> fields = new LinkedHashMap<>();

    /**
     * Adds a key-value pair to the form data.
     *
     * @param key The field name.
     * @param value The field value.
     * @return The current FormBuilder instance for chaining.
     */
    public FormBuilder add(@NotNull String key, @NotNull String value) {
        fields.put(key, value);
        return this;
    }

    /**
     * Builds the URL-encoded form data string.
     *
     * @return The constructed form data string.
     */
    public @NotNull String build() {
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return joiner.toString();
    }
}