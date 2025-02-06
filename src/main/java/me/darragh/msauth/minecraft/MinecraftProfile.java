package me.darragh.msauth.minecraft;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Represents a Minecraft profile.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record MinecraftProfile(@SerializedName("id") String id, // raw UUID
                               @SerializedName("name") String username,
                               @SerializedName("skins") MinecraftSkin[] skins,
                               @SerializedName("capes") MinecraftCape[] capes) {
    public UUID getUUID() {
        return UUID.fromString(this.id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
}
