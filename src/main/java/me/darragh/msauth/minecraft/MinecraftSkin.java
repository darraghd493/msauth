package me.darragh.msauth.minecraft;

import com.google.gson.annotations.SerializedName;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Minecraft skin.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record MinecraftSkin(@SerializedName("id") @NotNull String id,
                            @SerializedName("state") @NotNull @MagicConstant(stringValues = {"ACTIVE", "INACTIVE"}) String state,
                            @SerializedName("url") @NotNull String url,
                            @SerializedName("textureKey") @NotNull String textureKey,
                            @SerializedName("variant") @NotNull @MagicConstant(stringValues = {"CLASSIC", "SLIM"}) String variant) {
}
