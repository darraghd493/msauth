package me.darragh.msauth.util;

import java.io.IOException;

@FunctionalInterface
public interface BrowserOpener {
    void open(String url) throws IOException;
}
