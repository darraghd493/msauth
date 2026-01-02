package me.darragh.msauth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A simple {@link AuthenticationRecord} implementation.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record SimpleAuthenticationRecord(@NotNull String username,
                                         @NotNull UUID uuid,
                                         @NotNull String accessToken,
                                         @Nullable String refreshToken) implements AuthenticationRecord {
}
