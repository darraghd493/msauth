package me.darragh.msauth.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * A utility class for opening URLs in a browser.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@UtilityClass
public class BrowserUtil {
    /**
     * Open a URL in the default browser.
     *
     * @param url The URL to open.
     */
    public static void open(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open browser", e);
        }
    }

    /**
     * Open a URL in a specified browser.
     *
     * @implNote This method is not well-supported and has not been tested on all platforms. It is highly suggested to use the default method.
     *
     * @param url The URL to open.
     * @param browser The browser to open the URL in.
     * @param incognito Whether to open the URL in incognito mode.
     */
    public static void open(String url, Browser browser, boolean incognito) {
        try {
            browser.open(url, incognito);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open browser", e);
        }
    }

    @RequiredArgsConstructor
    public enum Browser {
        CHROME(BrowserOpeners.CHROME, BrowserOpeners.CHROME_INCOGNITO),
        EDGE(BrowserOpeners.EDGE, BrowserOpeners.EDGE_INCOGNITO),
        FIREFOX(BrowserOpeners.FIREFOX, BrowserOpeners.FIREFOX_INCOGNITO);

        private final BrowserOpener opener, incognitoOpener;

        void open(String url, boolean incognito) throws IOException {
            if (incognito) {
                incognitoOpener.open(url);
            } else {
                opener.open(url);
            }
        }
    }
}
