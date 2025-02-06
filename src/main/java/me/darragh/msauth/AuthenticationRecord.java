package me.darragh.msauth;

import java.util.UUID;

/**
 * The standard authentication record used to store authentication data.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public interface AuthenticationRecord {
    /**
     * Username of the user.
     *
     * @return The username of the user.
     */
    String username();

    /**
     * The UUID of the user.
     *
     * @return The UUID of the user.
     */
    UUID uuid();

    /**
     * The access token.
     *
     * @return The access token.
     */
    String accessToken();

    /**
     * The refresh token.
     *
     * @return The refresh token.
     */
    String refreshToken();
}
