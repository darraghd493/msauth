package me.darragh.msauth.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A functional interface for opening a URL in a browser.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@FunctionalInterface
public interface BrowserOpener {
    /**
     * Open a URL in a browser.
     *
     * @param url The URL to open.
     * @throws IOException If an error occurs while opening the URL.
     */
    void open(@NotNull String url) throws IOException;
}
