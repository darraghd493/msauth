package me.darragh.msauth.oauth2;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.RequiredArgsConstructor;
import me.darragh.msauth.gson.GsonProvider;
import me.darragh.msauth.util.FormBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Handles communication with Microsoft's OAuth2 services.
 * <p>
 * Useful resources:
 *      - <a href="https://minecraft.wiki/w/Microsoft_authentication">...</a>
 *      - <a href="https://gist.github.com/johngagefaulkner/7af56c87e29c85641474d2eb43eee441">...</a>
 *
 * @apiNote This class is not thread-safe.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@RequiredArgsConstructor
@SuppressWarnings("SpellCheckingInspection")
public class OAuthMicrosoftService {
    private static final Gson GSON = GsonProvider.get();

    private static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MINECRAFT_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String PROFILE_XBOX_URL = "https://profile.xboxlive.com/users/me/profile/settings?settings=GameDisplayName,AppDisplayName,AppDisplayPicRaw,GameDisplayPicRaw,"
            + "PublicGamerpic,ShowUserAsAvatar,Gamerscore,Gamertag,ModernGamertag,ModernGamertagSuffix,UniqueModernGamertag,AccountTier,TenureLevel,XboxOneRep,"
            + "PreferredColor,Location,Bio,Watermarks,RealName,RealNameOverride,IsQuarantined";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final OAuthOptions options;

    /**
     * Fetches the OAuth tokens from the OAuth2 code.
     *
     * @param oAuthCode The OAuth2 code.
     * @param redirectUri The redirect URI.
     * @return The OAuth tokens.
     */
    public OAuthTokens fetchOAuthTokens(String oAuthCode, String redirectUri) {
        String formBody = new FormBuilder()
                .add("client_id", this.options.clientId())
                .add("code", oAuthCode)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", redirectUri)
                .add("scope", "XboxLive.signin XboxLive.offline_access")
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch access token: " + response.body());
            }

            return GSON.fromJson(response.body(), OAuthTokens.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch access token.", e);
        }
    }

    /**
     * Uses the refresh token to fetch new OAuth tokens.
     *
     * @param refreshToken The refresh token.
     * @param redirectUri The redirect URI.
     * @return The new OAuth tokens.
     */
    public OAuthTokens useRefreshToken(String refreshToken, String redirectUri) {
        String formBody = new FormBuilder()
                .add("client_id", this.options.clientId())
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .add("redirect_uri", redirectUri)
                .add("scope", "XboxLive.signin XboxLive.offline_access")
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to use refresh token: " + response.body());
            }
            return GSON.fromJson(response.body(), OAuthTokens.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to use refresh token.", e);
        }
    }

    /**
     * Authenticates with Xbox Live.
     *
     * @param authToken The authentication token.
     * @return The Xbox Live token.
     */
    public String authenticateXboxLive(String authToken) {
        XboxLiveProperties properties = new XboxLiveProperties("RPS", "user.auth.xboxlive.com", "d=" + authToken);
        XboxLiveAuthenticationRequest req = new XboxLiveAuthenticationRequest(properties, "http://auth.xboxlive.com", "JWT");

        String jsonBody = GSON.toJson(req);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(XBL_AUTH_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to authenticate Xbox Live: " + response.body());
            }
            XboxLiveAuthentication authResponse = GSON.fromJson(response.body(), XboxLiveAuthentication.class);
            return authResponse.token();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to authenticate Xbox Live.", e);
        }
    }

    /**
     * Authenticates with Xbox Secure Token Service (XSTS).
     *
     * @param xblToken The Xbox Live token.
     * @return The XSTS token.
     */
    public String authenticateXSTS(String xblToken) {
        XboxSecureTokenServiceProperties properties = new XboxSecureTokenServiceProperties(new String[] { xblToken }, "RETAIL");
        XboxSecureTokenServiceRequest req = new XboxSecureTokenServiceRequest(properties, "rp://api.minecraftservices.com/", "JWT");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(XSTS_AUTH_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(req)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to authenticate XSTS: " + response.body());
            }
            XboxSecureTokenServiceAuthentication xstsResponse = GSON.fromJson(response.body(), XboxSecureTokenServiceAuthentication.class);
            return "XBL3.0 x=" + xstsResponse.displayClaims().xuis()[0].uhs() + ";" + xstsResponse.token();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to authenticate XSTS.", e);
        }
    }

    /**
     * Checks out the Xbox profile.
     *
     * @param xblAuthorisation The Xbox Live authorisation token.
     */
    public void checkoutXboxProfile(String xblAuthorisation) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROFILE_XBOX_URL))
                .header("Authorization", xblAuthorisation)
                .header("Accept", "application/json")
                .header("x-xbl-contract-version", "3")
                .GET()
                .build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to checkout Xbox profile.", e);
        }
    }

    /**
     * Authenticates with Minecraft.
     *
     * @param xblAuthentication The Xbox Live authentication token.
     * @return The Minecraft authentication token.
     */
    public MinecraftAuthentication authenticateMinecraft(String xblAuthentication) {
        MinecraftAuthenticationRequest req = new MinecraftAuthenticationRequest(xblAuthentication);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MINECRAFT_AUTH_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(req)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to authenticate Minecraft: " + response.body());
            }
            return GSON.fromJson(response.body(), MinecraftAuthentication.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to authenticate Minecraft.", e);
        }
    }

    /**
     * Formats the Minecraft authentication token for use in the Authorization header.
     *
     * @param authResponse The Minecraft authentication response.
     * @return The formatted Minecraft authentication token.
     */
    public String getMinecraftAuthToken(MinecraftAuthentication authResponse) {
        return "%s %s".formatted(authResponse.tokenType(), authResponse.accessToken());
    }

    // OAuth2
    public record OAuthTokens(@SerializedName("token_type") String tokenType,
                              @SerializedName("expires_in") int expiresIn,
                              @SerializedName("scope") String scope,
                              @SerializedName("access_token") String accessToken,
                              @SerializedName("refresh_token") String refreshToken,
                              @SerializedName("user_id") String userId) {
    }

    // Xbox Live
    private record XboxLiveProperties(@SerializedName("AuthMethod") String authMethod,
                                      @SerializedName("SiteName") String siteName,
                                      @SerializedName("RpsTicket") String rpsTicket) {
    }

    private record XboxLiveXui(@SerializedName("uhs") String uhs) {

    }
    private record XboxLiveDisplayClaims(@SerializedName("xui") XboxLiveXui[] xuis) {

    }

    private record XboxLiveAuthenticationRequest(@SerializedName("Properties") XboxLiveProperties properties,
                                                 @SerializedName("RelyingParty") String relyingParty,
                                                 @SerializedName("TokenType") String tokenType) {
    }

    private record XboxLiveAuthentication(@SerializedName("IssueInstant") String issueInstant,
                                          @SerializedName("NotAfter") String notAfter,
                                          @SerializedName("Token") String token,
                                          @SerializedName("DisplayClaims") XboxLiveDisplayClaims displayClaims) {
    }

    // Xbox Secure Token Service (XSTS) - integrated with Xbox Live
    private record XboxSecureTokenServiceProperties(@SerializedName("UserTokens") String[] userTokens,
                                                    @SerializedName("SandboxId") String sandboxId) {
    }

    private record XboxSecureTokenServiceRequest(@SerializedName("Properties") XboxSecureTokenServiceProperties properties,
                                                 @SerializedName("RelyingParty") String relyingParty,
                                                 @SerializedName("TokenType") String tokenType) {
    }

    private record XboxSecureTokenServiceAuthentication(@SerializedName("IssueInstant") String issueInstant,
                                                        @SerializedName("NotAfter") String notAfter,
                                                        @SerializedName("Token") String token,
                                                        @SerializedName("DisplayClaims") XboxLiveDisplayClaims displayClaims) {
    }

    // Minecraft
    private record MinecraftAuthenticationRequest(@SerializedName("identityToken") String identityToken) {

    }

    public record MinecraftAuthentication(@SerializedName("username") String uuid,
                                           @SerializedName("access_token") String accessToken,
                                           // ignored role
                                           // ignored metadata
                                           @SerializedName("expires_in") int expiresIn,
                                           @SerializedName("token_type") String tokenType) {
    }
}

