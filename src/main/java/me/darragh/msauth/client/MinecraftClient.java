package me.darragh.msauth.client;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import me.darragh.msauth.gson.GsonProvider;
import me.darragh.msauth.minecraft.MinecraftProfile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Handles common communication with Minecraft's services.
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
public class MinecraftClient {
    private static final Gson GSON = GsonProvider.get();

    private static final String MINECRAFT_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /**
     * Authenticates with Minecraft.
     *
     * @param xblAuthentication The Xbox Live authentication token.
     * @return The Minecraft authentication token.
     */
    public MinecraftAuthentication authenticateMinecraft(@NotNull String xblAuthentication) {
        MinecraftAuthenticationRequest req = new MinecraftAuthenticationRequest(xblAuthentication, true);

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
            throw new RuntimeException("Failed to authenticate Minecraft", e);
        }
    }

    /**
     * Formats the Minecraft authentication token for use in the Authorization header.
     *
     * @param authResponse The Minecraft authentication response.
     * @return The formatted Minecraft authentication token.
     */
    public @NotNull String getMinecraftAuthToken(@NotNull MinecraftAuthentication authResponse) {
        return "%s %s".formatted(authResponse.tokenType(), authResponse.accessToken());
    }

    /**
     * Fetches the Minecraft profile.
     *
     * @param minecraftAuthentication The Minecraft authentication.
     * @return The Minecraft profile.
     */
    public @NotNull MinecraftProfile fetchMinecraftProfile(@NotNull String minecraftAuthentication) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MINECRAFT_PROFILE_URL))
                .header("Authorization", minecraftAuthentication)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to checkout Minecraft profile: " + response.body());
            }
            return GSON.fromJson(response.body(), MinecraftProfile.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to checkout Minecraft profile", e);
        }
    }

    //region Records
    private record MinecraftAuthenticationRequest(@SerializedName("identityToken") String identityToken,
                                                  @SerializedName("ensureLegacyEnabled") Boolean ensureLegacyEnabled) {

    }

    public record MinecraftAuthentication(@SerializedName("username") String uuid,
                                          @SerializedName("access_token") String accessToken,
                                          // ignored role
                                          // ignored metadata
                                          @SerializedName("expires_in") int expiresIn,
                                          @SerializedName("token_type") String tokenType) {
    }
    //endregion
}
