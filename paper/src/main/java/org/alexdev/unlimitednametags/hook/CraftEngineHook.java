package org.alexdev.unlimitednametags.hook;

import org.alexdev.unlimitednametags.UnlimitedNameTags;
import org.alexdev.unlimitednametags.hook.creative.JsonModelHeightResolver;
import org.alexdev.unlimitednametags.hook.hat.HatHookPaper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves hat heights from CraftEngine's generated resource pack.
 *
 * <p>CraftEngine items (including the virtual items returned by HMCCosmetics for its HELMET slot) use
 * the modern {@code item_model} component, so they are invisible to the Nexo/ItemsAdder/Oraxen hooks
 * unless those plugins generated the item. Reading CraftEngine's own generated pack fixes the missing
 * helmet offset and keeps tall CraftEngine hats from covering the nametag.</p>
 */
public class CraftEngineHook extends Hook implements HatHookPaper {

    private static final String[] PACK_CANDIDATES = {
            "generated/resource_pack.zip",
            "cache/hosted/default/resource_pack.zip",
            "build/resource_pack.zip"
    };

    private final File packFile;
    private final JsonModelHeightResolver heightResolver;
    private final Map<String, Double> heightCache = new ConcurrentHashMap<>();
    private volatile long packLastModified;

    public CraftEngineHook(@NotNull UnlimitedNameTags plugin) {
        this(plugin, findPackFile(plugin.getDataFolder().getParentFile()));
    }

    CraftEngineHook(@NotNull UnlimitedNameTags plugin, @NotNull File packFile) {
        super(plugin);
        this.packFile = packFile;
        this.heightResolver = new JsonModelHeightResolver(packFile);
        this.packLastModified = packFile.lastModified();
    }

    /**
     * @param pluginsDirectory the server's plugins directory
     * @return the first existing CraftEngine pack, or the default generated-pack path so a pack
     * generated after startup is picked up by the resolver without reopening the hook
     */
    static @NotNull File findPackFile(@Nullable File pluginsDirectory) {
        final File base = pluginsDirectory == null
                ? new File("plugins/CraftEngine")
                : new File(pluginsDirectory, "CraftEngine");
        for (final String candidate : PACK_CANDIDATES) {
            final File file = new File(base, candidate);
            if (file.isFile()) {
                return file;
            }
        }
        return new File(base, PACK_CANDIDATES[0]);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public double getHigh(@NotNull UUID playerId) {
        final Player player = plugin.getPlayerListener().getPlayer(playerId);
        if (player == null) {
            return 0;
        }
        return getHigh(player, player.getInventory().getHelmet());
    }

    @Override
    public double getHigh(@NotNull Player player, @Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0;
        }

        final long modified = packFile.lastModified();
        if (modified != packLastModified) {
            packLastModified = modified;
            heightCache.clear();
        }

        final String cacheKey = cacheKey(item);
        if (cacheKey == null) {
            return 0;
        }
        return heightCache.computeIfAbsent(cacheKey, ignored -> {
            final OptionalDouble height = heightResolver.heightForItem(item);
            if (height.isEmpty()) {
                return 0d;
            }
            final double value = height.getAsDouble();
            return value > 0 ? value : 0d;
        });
    }

    private @Nullable String cacheKey(@NotNull ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        final StringBuilder key = new StringBuilder(item.getType().getKey().toString());
        boolean hasModel = false;
        if (meta.hasCustomModelData()) {
            key.append("|cmd:").append(meta.getCustomModelData());
            hasModel = true;
        }
        try {
            if (meta.hasItemModel() && meta.getItemModel() != null) {
                key.append("|item:").append(meta.getItemModel());
                hasModel = true;
            }
        } catch (Throwable ignored) {
            // Item-model components are not available on older server versions.
        }
        try {
            if (meta.hasEquippable() && meta.getEquippable().getModel() != null) {
                key.append("|equip:").append(meta.getEquippable().getModel());
                hasModel = true;
            }
        } catch (Throwable ignored) {
            // Equippable components are not available before 1.21.3.
        }
        return hasModel ? key.toString() : null;
    }
}
