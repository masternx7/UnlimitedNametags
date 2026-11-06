package org.alexdev.unlimitednametags.config;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes the hex color notations accepted by the plugin.
 *
 * <p>Supported input forms:</p>
 * <ul>
 *     <li>{@code &#RRGGBB} (legacy)</li>
 *     <li>{@code <#RRGGBB>} (MiniMessage)</li>
 *     <li>{@code &x&R&R&G&G&B&B} (vanilla repeated legacy)</li>
 *     <li>The same forms using {@code §} instead of {@code &}</li>
 * </ul>
 */
public final class HexColorCodes {

    private static final Pattern LEGACY_HEX = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern MINI_MESSAGE_HEX = Pattern.compile("<#([0-9a-fA-F]{6})>");
    private static final Pattern REPEATED_HEX = Pattern.compile("&[xX]((?:&[0-9a-fA-F]){6})");

    private HexColorCodes() {
    }

    /**
     * Converts legacy hex colors to MiniMessage tags.
     *
     * @param text text that may contain legacy hex colors
     * @return text with {@code &#RRGGBB} and {@code &x&R&R&G&G&B&B} converted to {@code <#RRGGBB>}
     */
    @NotNull
    public static String toMiniMessage(@NotNull String text) {
        final String normalized = sectionToAmpersand(text);
        return LEGACY_HEX.matcher(convertRepeated(normalized, "<#", ">")).replaceAll("<#$1>");
    }

    /**
     * Converts MiniMessage hex colors to the legacy format.
     *
     * @param text text that may contain MiniMessage hex colors
     * @return text with {@code <#RRGGBB>} and {@code &x&R&R&G&G&B&B} converted to {@code &#RRGGBB}
     */
    @NotNull
    public static String toLegacy(@NotNull String text) {
        final String normalized = sectionToAmpersand(text);
        return MINI_MESSAGE_HEX.matcher(convertRepeated(normalized, "&#", "")).replaceAll("&#$1");
    }

    @NotNull
    private static String sectionToAmpersand(@NotNull String text) {
        return text.indexOf('§') == -1 ? text : text.replace('§', '&');
    }

    @NotNull
    private static String convertRepeated(@NotNull String text, @NotNull String prefix, @NotNull String suffix) {
        final Matcher matcher = REPEATED_HEX.matcher(text);
        if (!matcher.find()) {
            return text;
        }

        final StringBuilder builder = new StringBuilder(text.length());
        int lastAppendPosition = 0;
        do {
            builder.append(text, lastAppendPosition, matcher.start())
                    .append(prefix)
                    .append(matcher.group(1).replace("&", ""))
                    .append(suffix);
            lastAppendPosition = matcher.end();
        } while (matcher.find());
        builder.append(text, lastAppendPosition, text.length());
        return builder.toString();
    }
}
