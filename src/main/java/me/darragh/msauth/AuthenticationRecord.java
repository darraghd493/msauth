package me.darragh.msauth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull String username();

    /**
     * The UUID of the user.
     *
     * @return The UUID of the user.
     */
    @NotNull UUID uuid();

    /**
     * The access token.
     *
     * @return The access token.
     */
    @NotNull String accessToken();

    /**
     * The refresh token.
     *
     * @return The refresh token.
     */
    @Nullable String refreshToken();
}
