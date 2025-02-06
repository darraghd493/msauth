package me.darragh.msauth.oauth2;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.RequiredArgsConstructor;
import me.darragh.msauth.minecraft.MinecraftProfile;
import okhttp3.*;

import java.io.IOException;

/**
 * Handles communication with Microsoft's OAuth2 services.
 * <p>
 * This class is responsible for handling the OAuth2 flow with Microsoft's services.
 * It is used to authenticate with Xbox Live and Minecraft services.
 * This class is not thread-safe.
 * <p>
 * Useful resources:
 *      - <a href="https://minecraft.wiki/w/Microsoft_authentication">...</a>
 *      - <a href="https://gist.github.com/johngagefaulkner/7af56c87e29c85641474d2eb43eee441">...</a>
 *
 * @author darraghd493
 * @since 1.0.0
 */
@RequiredArgsConstructor
@SuppressWarnings("SpellCheckingInspection")
public class OAuthMicrosoftService {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build();

    private static final Gson GSON = new Gson();

    private static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MINECRAFT_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String PROFILE_XBOX_URL = "https://profile.xboxlive.com/users/me/profile/settings?settings=GameDisplayName,AppDisplayName,AppDisplayPicRaw,GameDisplayPicRaw,"
            + "PublicGamerpic,ShowUserAsAvatar,Gamerscore,Gamertag,ModernGamertag,ModernGamertagSuffix,UniqueModernGamertag,AccountTier,TenureLevel,XboxOneRep,"
            + "PreferredColor,Location,Bio,Watermarks,RealName,RealNameOverride,IsQuarantined";
    private static final String MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private final OAuthOptions options;

    /**
     * Fetches the OAuth tokens from the OAuth2 code.
     *
     * @param oAuthCode The OAuth2 code.
     * @param redirectUri The redirect URI.
     * @return The OAuth tokens.
     */
    public OAuthTokens fetchOAuthTokens(String oAuthCode, String redirectUri) {
        FormBody formBody = new FormBody.Builder()
                .add("client_id", this.options.clientId())
                .add("code", oAuthCode)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", redirectUri)
                .add("scope", "XboxLive.signin XboxLive.offline_access")
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request)
                .execute()) {
            assert response.body() != null;
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to fetch access token: " + response.body().string());
            }
            return GSON.fromJson(response.body().string(), OAuthTokens.class);
        } catch (IOException e) {
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
        FormBody formBody = new FormBody.Builder()
                .add("client_id", this.options.clientId())
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .add("redirect_uri", redirectUri)
                .add("scope", "XboxLive.signin XboxLive.offline_access")
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request)
                .execute()) {
            assert response.body() != null;
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to use refresh token: " + response.body().string());
            }
            return GSON.fromJson(response.body().string(), OAuthTokens.class);
        } catch (IOException e) {
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

        RequestBody requestBody = RequestBody.create(GSON.toJson(req), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(XBL_AUTH_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request)
                .execute()) {
            assert response.body() != null;
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to authenticate Xbox Live: " + response.body().string());
            }
            XboxLiveAuthentication authResponse = GSON.fromJson(response.body().string(), XboxLiveAuthentication.class);
            return authResponse.token();
        } catch (IOException e) {
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

        RequestBody requestBody = RequestBody.create(GSON.toJson(req), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(XSTS_AUTH_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request)
                .execute()) {
            assert response.body() != null;
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to authenticate XSTS: " + response.body().string());
            }
            XboxSecureTokenServiceAuthentication xstsResponse = GSON.fromJson(response.body().string(), XboxSecureTokenServiceAuthentication.class);
            return "XBL3.0 x=" + xstsResponse.displayClaims().xuis()[0].uhs() + ";" + xstsResponse.token();
        } catch (IOException e) {
            throw new RuntimeException("Failed to authenticate XSTS.", e);
        }
    }

    /**
     * Checks out the Xbox profile.
     *
     * @param xblAuthorisation The Xbox Live authorisation.
     */
    public void checkoutXboxProfile(String xblAuthorisation) {
        Request request = new Request.Builder()
                .url(PROFILE_XBOX_URL)
                .addHeader("Authorization", xblAuthorisation)
                .addHeader("Accept", "application/json")
                .addHeader("x-xbl-contract-version", "3")
                .get()
                .build();

        //noinspection EmptyTryBlock
        try (Response response = HTTP_CLIENT.newCall(request)
                .execute()) {
        } catch (IOException e) {
            throw new RuntimeException("Failed to checkout Xbox profile.", e);
        }
    }

    /**
     * Authenticates with Minecraft.
     *
     * @param xblAuthentication The Xbox Live authentication.
     * @return The Minecraft authentication.
     */
    public String authenticateMinecraft(String xblAuthentication) {
        MinecraftAuthenticationRequest req = new MinecraftAuthenticationRequest(xblAuthentication);

        RequestBody requestBody = RequestBody.create(GSON.toJson(req), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(MINECRAFT_AUTH_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request)
                .execute()) {
            assert response.body() != null;
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to authenticate Minecraft: " + response.body().string());
            }
            MinecraftAuthentication authResponse = GSON.fromJson(response.body().string(), MinecraftAuthentication.class);
            return "%s %s".formatted(authResponse.tokenType(), authResponse.accessToken());
        } catch (IOException e) {
            throw new RuntimeException("Failed to authenticate Minecraft.", e);
        }
    }

    /**
     * Fetches the Minecraft profile.
     *
     * @param minecraftAuthentication The Minecraft authentication.
     * @return The Minecraft profile.
     */
    public MinecraftProfile fetchMinecraftProfile(String minecraftAuthentication) {
        Request request = new Request.Builder()
                .url(MINECRAFT_PROFILE_URL)
                .addHeader("Authorization", minecraftAuthentication)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        try (Response response = HTTP_CLIENT.newCall(request)
                .execute()) {
            assert response.body() != null;
            return GSON.fromJson(response.body().string(), MinecraftProfile.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to checkout Minecraft profile.", e);
        }
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

    private record MinecraftAuthentication(@SerializedName("username") String uuid,
                                           @SerializedName("access_token") String accessToken,
                                           // ignored role
                                           // ignored metadata
                                           @SerializedName("expires_in") int expiresIn,
                                           @SerializedName("token_type") String tokenType) {
    }
}

