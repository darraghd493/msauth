package me.darragh.msauth.cookie;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Handles communication with Microsoft's official authentication services.
 *
 * @apiNote This class is not thread-safe.
 *
 * @author darraghd493
 * @since 1.1.0
 */
public class CookieMicrosoftClient {
    // Note:
    // I haven't found much information online about this endpoint. ugh.
    // Although from my research it's just two redirects, and you get a URL with the access token
    // which is all that we need
    // Source: HTTP logger
    private static final String XBL_AUTH_URL = "https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin&aid=1142970254&as=1";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:146.0) Gecko/20100101 Firefox/146.0";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /**
     * Authenticates with Xbox Secure Token Service (XSTS).
     * This is because the cookies (are intended to) already have cookies signed in for the Xbox Live service.
     *
     * @param cookies The cookies to use in the request (in the correct format).
     * @return The XSTS token.
     */
    public @NotNull String authenticateXSTS(@NotNull String cookies) {
        String redirectUrl1 = encodeSpaces(this.retrieveRedirect(XBL_AUTH_URL, "")),
                redirectUrl2 = encodeSpaces(this.retrieveRedirect(redirectUrl1, cookies)),
                redirectUrl3 = this.retrieveRedirect(redirectUrl2, cookies);

        // Attempt to extract the access token from the final redirect URL
        String[] urlSegments = redirectUrl3.split("accessToken=");
        if (urlSegments.length < 2) {
            throw new RuntimeException("Failed to extract access token from redirect URL: " + redirectUrl3);
        } else {
            String encodedResponseSegment = urlSegments[1];
            String encodedResponse = encodedResponseSegment.split("&")[0];
            String decodedResponse = new String(Base64.getDecoder().decode(encodedResponse), StandardCharsets.UTF_8)
                    .split("\"rp://api.minecraftservices.com/\",")[1];
            System.out.println(decodedResponse);

            // Annoyingly, the response appears to be some weird broken JSON format
            // and quite frankly - I don't want to deal with it properly
            // so we'll just use regex to extract the values we need :p

            // I'd expect there to be a better way to do this, but oh well
            // We are relying on hopes and dreams that the token is the first "Token" field in the response

            String uhs = decodedResponse.split("\\{\"DisplayClaims\":\\{\"xui\":\\[\\{\"uhs\":\"")[1].split("\"")[0];
            String token = decodedResponse.split("\"Token\":\"")[1].split("\"")[0];

            return "XBL3.0 x=" + uhs + ";" + token;
        }
    }

    /**
     * Follows a redirect and retrieves the Location header.
     *
     * @param url The URL to follow.
     * @param cookies The cookies to include in the request, or `null` if none.
     * @return The URL in the Location header of the redirect response.
     */
    private @NotNull String retrieveRedirect(@NotNull String url, @Nullable String cookies) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "en-GB,en;q=0.5")
                .header("User-Agent", USER_AGENT);

        if (cookies != null) {
            requestBuilder.header("Cookie", cookies);
        }

        HttpRequest request = requestBuilder.GET().build();

        try {
            HttpResponse<?> response = this.httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() == 302) {
                if (response.headers().firstValue("Location").isEmpty()) {
                    throw new RuntimeException("Redirect response missing Location header");
                }
                return response.headers()
                        .firstValue("Location")
                        .get();
            } else {
                throw new RuntimeException("Unexpected response status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to follow redirect to access token URL", e);
        }
    }

    /**
     * Encodes plain spaces in a redirect URL to %20 to avoid malformed requests.
     *
     * @param url The URL to encode.
     * @return The encoded URL.
     */
    private static @NotNull String encodeSpaces(@NotNull String url) {
        return url.replace(" ", "%20");
    }
}
