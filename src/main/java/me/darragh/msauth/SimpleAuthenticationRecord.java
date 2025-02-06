package me.darragh.msauth;

import java.util.UUID;

/**
 * A simple {@link AuthenticationRecord} implementation.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record SimpleAuthenticationRecord(String username, UUID uuid, String accessToken, String refreshToken) implements AuthenticationRecord {
}
