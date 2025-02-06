package me.darragh.msauth.oauth2;

import com.sun.net.httpserver.HttpExchange;
import me.darragh.msauth.AuthenticationCallback;
import me.darragh.msauth.AuthenticationRecord;
import me.darragh.msauth.Authenticator;
import me.darragh.msauth.SimpleAuthenticationRecord;
import me.darragh.msauth.minecraft.MinecraftProfile;
import me.darragh.msauth.oauth2.server.OAuthResponseState;
import me.darragh.msauth.oauth2.server.OAuthServerHandler;
import me.darragh.msauth.util.BrowserUtil;

import java.io.IOException;

/**
 * An authenticator for web-based authentication using OAuth2.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public class OAuthAuthenticator implements Authenticator<AuthenticationRecord> {
    private static final String AUTH_URL_TEMPLATE = "https://login.live.com/oauth20_authorize.srf?client_id=%s" +
            "&response_type=code" +
            "&scope=XboxLive.signin%%20XboxLive.offline_access" +
            "&redirect_uri=%s&prompt=select_account";

    private final OAuthServerHandler serverHandler;
    private final OAuthOptions options;

    private AuthenticationCallback<AuthenticationRecord> callback;

    public OAuthAuthenticator(OAuthOptions options) {
        this.serverHandler = new OAuthServerHandler(options, this::handleResponse);
        this.options = options;
    }

    @Override
    public void performAuthentication(AuthenticationRecord record, AuthenticationCallback<AuthenticationRecord> callback) {
        if (record.refreshToken() == null) {
            throw new IllegalArgumentException("Record does not contain a refresh token.");
        }

        if (this.callback != null) {
            throw new IllegalStateException("Already performing authentication.");
        }

        this.callback = callback;

        OAuthMicrosoftService microsoftService = new OAuthMicrosoftService(this.options);
        OAuthMicrosoftService.OAuthTokens oAuthTokens = microsoftService.useRefreshToken(record.refreshToken(), this.generateRedirectUrl());
        this.authenticateTokens(microsoftService, oAuthTokens);
    }

    @Override
    public void performAuthentication(AuthenticationCallback<AuthenticationRecord> callback) {
        if (this.callback != null) {
            throw new IllegalStateException("Already performing authentication.");
        }

        try {
            this.serverHandler.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server", e);
        }

        this.callback = callback;
    }

    /**
     * Generates the URL to authenticate with.
     *
     * @return The URL to authenticate with.
     */
    public String generateUrl() {
        return String.format(AUTH_URL_TEMPLATE, this.options.clientId(), this.generateRedirectUrl());
    }

    /**
     * Generates the redirect URL.
     *
     * @return The redirect URL.
     */
    public String generateRedirectUrl() {
        return "http://localhost:%s".formatted(this.options.port());
    }

    /**
     * Handles the response from the OAuth server.
     *
     * @param exchange The exchange to handle.
     * @return The response state.
     */
    private OAuthResponseState handleResponse(HttpExchange exchange) {
        // Expected query:
        // code={...}
        // TODO: Handle this better
        try {
            if (exchange.getRequestURI().getQuery() == null) {
                throw new RuntimeException("No query in request.");
            }

            String code = exchange.getRequestURI().getQuery().split("=")[1];
            OAuthMicrosoftService microsoftService = new OAuthMicrosoftService(this.options);
            OAuthMicrosoftService.OAuthTokens oAuthTokens = microsoftService.fetchOAuthTokens(code, this.generateRedirectUrl());
            this.authenticateTokens(microsoftService, oAuthTokens);
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle response.", e);
        }

        return OAuthResponseState.SUCCESS;
    }

    /**
     * Authenticates the OAuth tokens and supplies the callback with the authentication record.
     *
     * @param microsoftService The Microsoft communication service.
     * @param oAuthTokens The OAuth tokens.
     */
    private void authenticateTokens(OAuthMicrosoftService microsoftService, OAuthMicrosoftService.OAuthTokens oAuthTokens) {
        String xblToken = microsoftService.authenticateXboxLive(oAuthTokens.accessToken());
        String xblAuthentication = microsoftService.authenticateXSTS(xblToken);
        microsoftService.checkoutXboxProfile(xblAuthentication);
        String minecraftToken = microsoftService.authenticateMinecraft(xblAuthentication);
        MinecraftProfile minecraftProfile = microsoftService.fetchMinecraftProfile(minecraftToken);

        // Supply callback with authentication record
        this.callback.onAuthentication(new SimpleAuthenticationRecord(
                minecraftProfile.username(),
                minecraftProfile.getUUID(),
                oAuthTokens.accessToken(),
                oAuthTokens.refreshToken()
        ), minecraftProfile);
    }
}
