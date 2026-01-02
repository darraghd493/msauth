package me.darragh.msauth.oauth2.server;

import org.jetbrains.annotations.NotNull;

/**
 * Handles the generation of an OAuth response page.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@FunctionalInterface
public interface OAuthPageHandler {
    /**
     * Generates an HTML page with the given message.
     *
     * @param message The message to display on the page.
     *
     * @return The generated HTML page.
     */
    @NotNull String generatePage(@NotNull String message);

    /**
     * Returns the message to display on the page for the given response state.
     *
     * @param responseState The response state.
     *
     * @return The message to display on the page.
     */
    default @NotNull String getMessage(@NotNull OAuthResponseState responseState) {
        return responseState.getDefaultMessage();
    }
}
