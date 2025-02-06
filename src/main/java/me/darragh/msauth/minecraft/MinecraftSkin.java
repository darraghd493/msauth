package me.darragh.msauth.minecraft;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Minecraft skin.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record MinecraftSkin(@SerializedName("id") String id,
                            @SerializedName("state") String state,
                            @SerializedName("url") String url,
                            @SerializedName("textureKey") String textureKey,
                            @SerializedName("variant") String variant) {
}
