package me.darragh.msauth.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@UtilityClass
public class BrowserUtil {
    public static void open(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open browser.", e);
        }
    }

    public static void open(String url, Browser browser, boolean incognito) {
        try {
            browser.open(url, incognito);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open browser.", e);
        }
    }

    @RequiredArgsConstructor
    public enum Browser {
        CHROME(
                url -> Runtime.getRuntime().exec("cmd.exe /c start chrome.exe \"%s\"".formatted(url)),
                url -> Runtime.getRuntime().exec("cmd.exe /c start chrome.exe --incognito \"%s\"".formatted(url))
        ),
        EDGE(
                url -> Runtime.getRuntime().exec("cmd.exe /c start msedge.exe \"%s\"".formatted(url)),
                url -> Runtime.getRuntime().exec("cmd.exe /c start msedge.exe -inprivate \"%s\"".formatted(url))
        );

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
