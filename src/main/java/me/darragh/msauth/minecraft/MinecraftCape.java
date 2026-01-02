package me.darragh.msauth.minecraft;

import com.google.gson.annotations.SerializedName;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Minecraft cape.
 *
 * @author darraghd493
 * @since 1.0.0
 */
public record MinecraftCape(@SerializedName("id") @NotNull String id,
                            @SerializedName("state") @NotNull @MagicConstant(stringValues = {"ACTIVE", "INACTIVE"}) String state,
                            @SerializedName("url") @NotNull String url,
                            @SerializedName("alias") @NotNull String alias) {
}
