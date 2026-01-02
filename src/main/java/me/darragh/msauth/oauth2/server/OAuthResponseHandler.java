package me.darragh.msauth.oauth2.server;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the response from the OAuth pageHandler.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@FunctionalInterface
public interface OAuthResponseHandler {
    /**
     * Handles the response from the OAuth pageHandler.
     *
     * @param exchange The HTTP exchange.
     *
     * @return Whether the response was handled successfully.
     */
    @NotNull OAuthResponseState handleResponse(@NotNull HttpExchange exchange);
}
