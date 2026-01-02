package me.darragh.msauth.cookie;

import lombok.RequiredArgsConstructor;
import me.darragh.msauth.AuthenticationCallback;
import me.darragh.msauth.AuthenticationRecord;
import me.darragh.msauth.Authenticator;
import me.darragh.msauth.SimpleAuthenticationRecord;
import me.darragh.msauth.client.MinecraftClient;
import me.darragh.msauth.client.XboxClient;
import me.darragh.msauth.minecraft.MinecraftProfile;
import me.darragh.msauth.util.CookieUtil;
import org.jetbrains.annotations.NotNull;

/**
 * An authenticator for cookies, emulating web-based authentication using Minecraft's official login flow.
 * <p>
 * Please note that this will not provide a refresh token, only an access token.
 *
 * @author darraghd493
 * @since 1.1.0
 */
@RequiredArgsConstructor
public class CookieAuthenticator implements Authenticator<AuthenticationRecord> {
    private final @NotNull String cookies;
    private boolean authenticating = false;

    @Override
    public void performAuthentication(AuthenticationRecord record, AuthenticationCallback<AuthenticationRecord> callback) {
        throw new UnsupportedOperationException("CookieAuthenticator does not support authentication using an existing record. Please use the original cookie file");
    }

    @Override
    public void performAuthentication(AuthenticationCallback<AuthenticationRecord> callback) {
        String httpUsableCookies = CookieUtil.reformatCookies(this.cookies); // usable in HTTP requests
        this.authenticating = true;

        CookieMicrosoftClient microsoftClient = new CookieMicrosoftClient();
        XboxClient xboxClient = new XboxClient();
        MinecraftClient minecraftClient = new MinecraftClient();

        String xblAuthenticationToken = microsoftClient.authenticateXSTS(httpUsableCookies);
        xboxClient.checkoutXboxProfile(xblAuthenticationToken);
        MinecraftClient.MinecraftAuthentication minecraftAuthentication = minecraftClient.authenticateMinecraft(xblAuthenticationToken);
        String minecraftToken = minecraftClient.getMinecraftAuthToken(minecraftAuthentication);
        MinecraftProfile minecraftProfile = minecraftClient.fetchMinecraftProfile(minecraftToken);

        // Supply callback with authentication record
        callback.onAuthentication(new SimpleAuthenticationRecord(
                minecraftProfile.username(),
                minecraftProfile.getUUID(),
                minecraftAuthentication.accessToken(),
                ""
        ), minecraftProfile);

        this.authenticating = false;
    }

    @Override
    public void stopAuthentication() {
        // no-op
        // this performs the login/non-async
    }

    @Override
    public boolean isAuthenticating() {
        return this.authenticating;
    }
}
