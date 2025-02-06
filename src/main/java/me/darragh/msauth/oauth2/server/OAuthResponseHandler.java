package me.darragh.msauth.oauth2.server;

import com.sun.net.httpserver.HttpExchange;

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
    OAuthResponseState handleResponse(HttpExchange exchange);
}
