package org.alexdev.unlimitednametags.config;

import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.alexdev.unlimitednametags.api.UnlimitedNameTagsInstancePaper;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public enum Formatter {

    MINIMESSAGE(
            (plugin, player, text) -> plugin.formatTextForNametag(player, HexColorCodes.toMiniMessage(text)),
            "MiniMessage"
    ),
    LEGACY(
            (plugin, player, text) -> getSTUPID().deserialize(HexColorCodes.toLegacy(text)),
            "Legacy Text"
    ),
    UNIVERSAL(
            (plugin, player, text) -> {
                final String legacy = getHEX().serialize(getSTUPID().deserialize(HexColorCodes.toLegacy(text)));
                final Component component = getHEX().deserialize(legacy);
                final String string = MiniMessage.miniMessage().serialize(component).replace("\\<", "<").replace("\\", "");
                return MINIMESSAGE.formatter.apply(plugin, player, string);
            },
            "Universal"
    );

    @Getter
    private final String name;

    private final TriFunction<UnlimitedNameTagsInstancePaper, CommandSender, String, Component> formatter;

    @Getter(value = AccessLevel.PRIVATE)
    private static final LegacyComponentSerializer STUPID = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .useUnusualXRepeatedCharacterHexFormat()
            .hexColors()
            .build();
    @Getter(value = AccessLevel.PRIVATE)
    private static final LegacyComponentSerializer HEX = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    Formatter(@NotNull TriFunction<UnlimitedNameTagsInstancePaper, CommandSender, String, Component> formatter, @NotNull String name) {
        this.formatter = formatter;
        this.name = name;
    }

    @NotNull
    public static Formatter from(@NotNull TextFormatter textFormatter) {
        return valueOf(textFormatter.name());
    }

    public Component format(@NotNull UnlimitedNameTagsInstancePaper plugin, @NotNull CommandSender audience, @NotNull String text) {
        return formatter.apply(plugin, audience, text);
    }
}
