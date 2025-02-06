package me.darragh.msauth.oauth2;

import me.darragh.msauth.oauth2.server.OAuthPageHandler;
import me.darragh.msauth.oauth2.server.SimpleOAuthPageHandler;

/**
 * Represents the options for OAuth2 authentication.
 *
 * @author darraghd493
 * @since 1.0.0
 *
 * @param clientId The client ID for the OAuth2 application.
 * @param port The port to run the OAuth2 pageHandler on.
 * @param pageHandler The pageHandler to handle the OAuth2 authentication.
 */
public record OAuthOptions(String clientId, int port, OAuthPageHandler pageHandler) {
    public static final OAuthOptions DEFAULT = new OAuthOptions(
            "54fd49e4-2103-4044-9603-2b028c814ec3", // In-Game Account Switcher Client ID
            59125,
            new SimpleOAuthPageHandler()
    );
}
