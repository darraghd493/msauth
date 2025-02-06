package me.darragh.msauth.oauth2.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the state of an OAuth response.
 *
 * @author darraghd493
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum OAuthResponseState {
    SUCCESS("You have successfully authenticated.", true),
    FAILURE("Authentication failed.", false),
    INVALID("Invalid response.", false);

    private final String defaultMessage;
    private final boolean success;
}
