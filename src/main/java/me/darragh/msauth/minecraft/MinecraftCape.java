package me.darragh.msauth.minecraft;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Minecraft cape.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record MinecraftCape(@SerializedName("id") String id,
                            @SerializedName("state") String state,
                            @SerializedName("url") String url,
                            @SerializedName("alias") String alias) {
}
