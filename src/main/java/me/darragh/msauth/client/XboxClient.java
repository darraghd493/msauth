package me.darragh.msauth.client;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Handles common communication with Xbox's services.
 * <p>
 * Useful resources:
 *      - <a href="https://minecraft.wiki/w/Microsoft_authentication">...</a>
 *      - <a href="https://gist.github.com/johngagefaulkner/7af56c87e29c85641474d2eb43eee441">...</a>
 *
 * @apiNote This class is not thread-safe.
 *
 * @author darraghd493
 * @since 1.1.0
 */
public class XboxClient {
    private static final String PROFILE_XBOX_URL = "https://profile.xboxlive.com/users/me/profile/settings?settings=GameDisplayName,AppDisplayName,AppDisplayPicRaw,GameDisplayPicRaw,"
            + "PublicGamerpic,ShowUserAsAvatar,Gamerscore,Gamertag,ModernGamertag,ModernGamertagSuffix,UniqueModernGamertag,AccountTier,TenureLevel,XboxOneRep,"
            + "PreferredColor,Location,Bio,Watermarks,RealName,RealNameOverride,IsQuarantined";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /**
     * Checks out the Xbox profile.
     *
     * @apiNote This is used to validate the Xbox Live authorisation token. It will throw a {@link RuntimeException} if the token is invalid.
     * @param xblAuthorisation The Xbox Live authorisation token.
     */
    public void checkoutXboxProfile(@NotNull String xblAuthorisation) {
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
            throw new RuntimeException("Failed to checkout Xbox profile", e);
        }
    }
}
