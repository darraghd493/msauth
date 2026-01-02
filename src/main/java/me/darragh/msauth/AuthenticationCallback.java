package me.darragh.msauth;

import me.darragh.msauth.minecraft.MinecraftProfile;
import org.jetbrains.annotations.NotNull;

/**
 * A callback for when authentication is complete.
 *
 * @author darraghd493
 * @since 1.0.0
 *
 * @param <T> The type of authentication record used to store authentication data.
 */
@FunctionalInterface
public interface AuthenticationCallback<T extends AuthenticationRecord> {
    /**
     * Called when authentication is complete.
     *
     * @param record The authentication record.
     */
    void onAuthentication(@NotNull T record, @NotNull MinecraftProfile profile);
}
