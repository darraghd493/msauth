package me.darragh.msauth.util;

import lombok.experimental.UtilityClass;

/**
 * A class containing {@link BrowserOpener} instances for various browsers and operating systems.
 * <p>
 * It is not recommended to use browser openers due to the lack of support and testing on all platforms, alongside the requirement of the user to have the browser installed on their system.
 * It is recommended to use {@link BrowserUtil#open(String)} class instead.
 *
 * @see BrowserOpener
 *
 * @implNote It is not recommended to use this class directly. Instead, use the {@link BrowserUtil} class.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@UtilityClass
public class BrowserOpeners {
    private static final BrowserOpener CHROME_WINDOWS = url -> Runtime.getRuntime().exec("cmd.exe /c start chrome.exe \"%s\"".formatted(url));
    private static final BrowserOpener CHROME_WINDOWS_INCOGNITO = url -> Runtime.getRuntime().exec("cmd.exe /c start chrome.exe --incognito \"%s\"".formatted(url));
    private static final BrowserOpener CHROME_MAC = url -> Runtime.getRuntime().exec("sh -c open -a \"Google Chrome\" \"%s\"".formatted(url));
    private static final BrowserOpener CHROME_MAC_INCOGNITO = url -> Runtime.getRuntime().exec("sh -c open -a \"Google Chrome\" --args --incognito \"%s\"".formatted(url));
    private static final BrowserOpener CHROME_LINUX = url -> Runtime.getRuntime().exec("sh -c google-chrome \"%s\"".formatted(url));
    private static final BrowserOpener CHROME_LINUX_INCOGNITO = url -> Runtime.getRuntime().exec("sh -c google-chrome --incognito \"%s\"".formatted(url));

    private static final BrowserOpener FIREFOX_WINDOWS = url -> Runtime.getRuntime().exec("cmd.exe /c start firefox.exe \"%s\"".formatted(url));
    private static final BrowserOpener FIREFOX_WINDOWS_INCOGNITO = url -> Runtime.getRuntime().exec("cmd.exe /c start firefox.exe -private-window \"%s\"".formatted(url));
    private static final BrowserOpener FIREFOX_MAC = url -> Runtime.getRuntime().exec("sh -c open -a \"Firefox\" \"%s\"".formatted(url));
    private static final BrowserOpener FIREFOX_MAC_INCOGNITO = url -> Runtime.getRuntime().exec("sh -c open -a \"Firefox\" --args -private-window \"%s\"".formatted(url));
    private static final BrowserOpener FIREFOX_LINUX = url -> Runtime.getRuntime().exec("sh -c firefox \"%s\"".formatted(url));
    private static final BrowserOpener FIREFOX_LINUX_INCOGNITO = url -> Runtime.getRuntime().exec("sh -c firefox --private-window \"%s\"".formatted(url));

    public static final BrowserOpener CHROME = generateDynamicOpener(CHROME_WINDOWS, CHROME_MAC, CHROME_LINUX);
    public static final BrowserOpener CHROME_INCOGNITO = generateDynamicOpener(CHROME_WINDOWS_INCOGNITO, CHROME_MAC_INCOGNITO, CHROME_LINUX_INCOGNITO);

    public static final BrowserOpener EDGE = url -> Runtime.getRuntime().exec("cmd.exe /c start msedge.exe \"%s\"".formatted(url));
    public static final BrowserOpener EDGE_INCOGNITO = url -> Runtime.getRuntime().exec("cmd.exe /c start msedge.exe -inprivate \"%s\"".formatted(url));

    public static final BrowserOpener FIREFOX = generateDynamicOpener(FIREFOX_WINDOWS, FIREFOX_MAC, FIREFOX_LINUX);
    public static final BrowserOpener FIREFOX_INCOGNITO = generateDynamicOpener(FIREFOX_WINDOWS_INCOGNITO, FIREFOX_MAC_INCOGNITO, FIREFOX_LINUX_INCOGNITO);

    private static BrowserOpener generateDynamicOpener(BrowserOpener windows, BrowserOpener mac, BrowserOpener linux) {
        return url -> {
            OSUtil.OperatingSystem os = OSUtil.OS;
            switch (os) {
                case WINDOWS -> windows.open(url);
                case MAC -> mac.open(url);
                case LINUX -> linux.open(url);
                default -> throw new RuntimeException("Unsupported operating system: " + os);
            }
        };
    }
}
