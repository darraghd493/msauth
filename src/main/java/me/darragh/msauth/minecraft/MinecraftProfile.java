package me.darragh.msauth.minecraft;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a Minecraft profile.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record MinecraftProfile(@SerializedName("id") @NotNull String id, // raw UUID
                               @SerializedName("name") @NotNull String username,
                               @SerializedName("skins") @NotNull MinecraftSkin[] skins,
                               @SerializedName("capes") @NotNull MinecraftCape[] capes) {
    public @NotNull UUID getUUID() {
        return UUID.fromString(this.id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
}
