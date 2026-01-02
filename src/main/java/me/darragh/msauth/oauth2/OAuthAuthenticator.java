package me.darragh.msauth.oauth2;

import com.sun.net.httpserver.HttpExchange;
import me.darragh.msauth.AuthenticationCallback;
import me.darragh.msauth.AuthenticationRecord;
import me.darragh.msauth.Authenticator;
import me.darragh.msauth.SimpleAuthenticationRecord;
import me.darragh.msauth.client.XboxClient;
import me.darragh.msauth.minecraft.MinecraftProfile;
import me.darragh.msauth.client.MinecraftClient;
import me.darragh.msauth.oauth2.server.OAuthResponseState;
import me.darragh.msauth.oauth2.server.OAuthServerHandler;
import me.darragh.msauth.util.QueryUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final OAuthServerHandler serverHandler;
    private final OAuthOptions options;

    private AuthenticationCallback<AuthenticationRecord> callback;

    public OAuthAuthenticator(OAuthOptions options) {
        this.serverHandler = new OAuthServerHandler(options, this::handleResponse);
        this.options = options;
    }

    @Override
    public void performAuthentication(AuthenticationRecord record, @NotNull AuthenticationCallback<AuthenticationRecord> callback) {
        if (record.refreshToken() == null) {
            throw new IllegalArgumentException("Record does not contain a refresh token");
        }

        if (this.callback != null) {
            throw new IllegalStateException("Already performing authentication");
        }

        this.callback = callback;

        OAuthMicrosoftClient microsoftClient = new OAuthMicrosoftClient(this.options);
        XboxClient xboxClient = new XboxClient();
        MinecraftClient minecraftClient = new MinecraftClient();
        OAuthMicrosoftClient.OAuthTokens oAuthTokens = microsoftClient.useRefreshToken(record.refreshToken(), this.generateRedirectUrl());
        this.authenticateTokens(microsoftClient, xboxClient, minecraftClient, oAuthTokens);
    }

    @Override
    public void performAuthentication(@NotNull AuthenticationCallback<AuthenticationRecord> callback) {
        if (this.callback != null) {
            throw new IllegalStateException("Already performing authentication");
        }

        try {
            this.serverHandler.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server", e);
        }

        this.callback = callback;
    }

    @Override
    public void stopAuthentication() {
        EXECUTOR.execute(() -> { // Shut the server down after 3 seconds - this may be called immediately after receiving a response by some clients. ~_~
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.serverHandler.stop();
        });

        if (this.callback != null) {
            this.callback = null;
        }
    }

    @Override
    public boolean isAuthenticating() {
        return this.callback != null || this.serverHandler.isRunning();
    }

    /**
     * Generates the URL to authenticate with.
     *
     * @return The URL to authenticate with.
     */
    public @NotNull String generateUrl() {
        return String.format(AUTH_URL_TEMPLATE, this.options.clientId(), this.generateRedirectUrl());
    }

    /**
     * Generates the redirect URL.
     *
     * @return The redirect URL.
     */
    public @NotNull String generateRedirectUrl() {
        return "http://localhost:%s".formatted(this.options.port());
    }

    /**
     * Handles the response from the OAuth server.
     *
     * @param exchange The exchange to handle.
     * @return The response state.
     */
    private @NotNull OAuthResponseState handleResponse(@NotNull HttpExchange exchange) {
        try {
            if (exchange.getRequestURI().getQuery() == null) {
                throw new RuntimeException("No query in request");
            }

            String code = QueryUtil.getQuery(exchange.getRequestURI().getQuery(), "code");
            if (code == null) {
                throw new RuntimeException("No code in query");
            }

            OAuthMicrosoftClient microsoftClient = new OAuthMicrosoftClient(this.options);
            XboxClient xboxClient = new XboxClient();
            MinecraftClient minecraftClient = new MinecraftClient();
            OAuthMicrosoftClient.OAuthTokens oAuthTokens = microsoftClient.fetchOAuthTokens(code, this.generateRedirectUrl());
            this.authenticateTokens(microsoftClient, xboxClient, minecraftClient, oAuthTokens);
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle response", e);
        }

        return OAuthResponseState.SUCCESS;
    }

    /**
     * Authenticates the OAuth tokens and supplies the callback with the authentication record.
     *
     * @param microsoftClient The Microsoft communication service.
     * @param oAuthTokens The OAuth tokens.
     */
    private void authenticateTokens(@NotNull OAuthMicrosoftClient microsoftClient, @NotNull XboxClient xboxClient, @NotNull MinecraftClient minecraftClient, @NotNull OAuthMicrosoftClient.OAuthTokens oAuthTokens) {
        String xblToken = microsoftClient.authenticateXboxLive(oAuthTokens.accessToken());
        String xblAuthenticationToken = microsoftClient.authenticateXSTS(xblToken);
        xboxClient.checkoutXboxProfile(xblAuthenticationToken);
        MinecraftClient.MinecraftAuthentication minecraftAuthentication = minecraftClient.authenticateMinecraft(xblAuthenticationToken);
        String minecraftToken = minecraftClient.getMinecraftAuthToken(minecraftAuthentication);
        MinecraftProfile minecraftProfile = minecraftClient.fetchMinecraftProfile(minecraftToken);

        // Supply callback with authentication record
        this.callback.onAuthentication(new SimpleAuthenticationRecord(
                minecraftProfile.username(),
                minecraftProfile.getUUID(),
                minecraftAuthentication.accessToken(),
                oAuthTokens.refreshToken()
        ), minecraftProfile);

        // Remove callback
        this.callback = null;

        // Forcefully stop the server after 3 seconds
        EXECUTOR.execute(() -> {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (this.callback != null || !this.serverHandler.isRunning()) { // Another authentication is in progress
                return;
            }
            this.serverHandler.stop();
        });
    }
}
