package org.alexdev.unlimitednametags.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.alexdev.unlimitednametags.api.UnlimitedNameTagsInstancePaper;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormatterTest {

    private static final TextColor RED = TextColor.color(0xff0000);
    private static final String[] HEX_NAMES = {
            "&#ff0000",
            "<#ff0000>",
            "&x&f&f&0&0&0&0"
    };

    @Test
    void legacySupportsAllHexFormats() {
        assertFormats(Formatter.LEGACY);
    }

    @Test
    void miniMessageSupportsAllHexFormats() {
        assertFormats(Formatter.MINIMESSAGE);
    }

    @Test
    void universalSupportsAllHexFormats() {
        assertFormats(Formatter.UNIVERSAL);
    }

    private static void assertFormats(Formatter formatter) {
        final UnlimitedNameTagsInstancePaper plugin = mockPlugin();
        final CommandSender audience = mock(CommandSender.class);
        for (String hex : HEX_NAMES) {
            final Component component = formatter.format(plugin, audience, hex + "Hello");
            assertEquals(Component.text("Hello", RED), component, () -> hex + " was not parsed correctly");
        }
    }

    private static UnlimitedNameTagsInstancePaper mockPlugin() {
        final UnlimitedNameTagsInstancePaper plugin = mock(UnlimitedNameTagsInstancePaper.class);
        when(plugin.formatTextForNametag(any(CommandSender.class), anyString()))
                .thenAnswer(invocation -> MiniMessage.miniMessage().deserialize(invocation.getArgument(1)));
        return plugin;
    }
}
