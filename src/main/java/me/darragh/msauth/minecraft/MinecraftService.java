package me.darragh.msauth.minecraft;

import com.google.gson.Gson;
import me.darragh.msauth.gson.GsonProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Handles communication with Minecraft's services.
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
public class MinecraftService {
    private static final Gson GSON = GsonProvider.get();

    private static final String MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /**
     * Fetches the Minecraft profile.
     *
     * @param minecraftAuthentication The Minecraft authentication.
     * @return The Minecraft profile.
     */
    public MinecraftProfile fetchMinecraftProfile(String minecraftAuthentication) {
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
            throw new RuntimeException("Failed to checkout Minecraft profile.", e);
        }
    }
}
