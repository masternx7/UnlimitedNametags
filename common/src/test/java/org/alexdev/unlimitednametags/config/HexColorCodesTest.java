package org.alexdev.unlimitednametags.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HexColorCodesTest {

    @Test
    void convertsLegacyHexToMiniMessage() {
        assertEquals("<#ff0000>Hello", HexColorCodes.toMiniMessage("&#ff0000Hello"));
        assertEquals("<#00FF00>Hello", HexColorCodes.toMiniMessage("&#00FF00Hello"));
        assertEquals("A <#123456>B <#abcdef>C", HexColorCodes.toMiniMessage("A &#123456B &#abcdefC"));
    }

    @Test
    void convertsRepeatedLegacyHexToMiniMessage() {
        assertEquals("<#ff0000>Hello", HexColorCodes.toMiniMessage("&x&f&f&0&0&0&0Hello"));
        assertEquals("<#00FF00>Hello", HexColorCodes.toMiniMessage("&x&0&0&F&F&0&0Hello"));
        assertEquals("<#00FF00>Hello", HexColorCodes.toMiniMessage("&X&0&0&F&F&0&0Hello"));
    }

    @Test
    void convertsSectionSignHexToMiniMessage() {
        assertEquals("<#ff0000>Hello", HexColorCodes.toMiniMessage("§#ff0000Hello"));
        assertEquals("<#ff0000>Hello", HexColorCodes.toMiniMessage("§x§f§f§0§0§0§0Hello"));
    }

    @Test
    void leavesMiniMessageHexUntouched() {
        assertEquals("<#ff0000>Hello", HexColorCodes.toMiniMessage("<#ff0000>Hello"));
    }

    @Test
    void leavesInvalidHexUntouched() {
        assertEquals("&#ff00Hello", HexColorCodes.toMiniMessage("&#ff00Hello"));
        assertEquals("&x&f&f&0&0&0Hello", HexColorCodes.toMiniMessage("&x&f&f&0&0&0Hello"));
        assertEquals("&#ggggggHello", HexColorCodes.toMiniMessage("&#ggggggHello"));
    }

    @Test
    void convertsMiniMessageHexToLegacy() {
        assertEquals("&#ff0000Hello", HexColorCodes.toLegacy("<#ff0000>Hello"));
        assertEquals("&#00FF00Hello", HexColorCodes.toLegacy("<#00FF00>Hello"));
        assertEquals("A &#123456B &#abcdefC", HexColorCodes.toLegacy("A <#123456>B <#abcdef>C"));
    }

    @Test
    void convertsRepeatedLegacyHexToLegacy() {
        assertEquals("&#ff0000Hello", HexColorCodes.toLegacy("&x&f&f&0&0&0&0Hello"));
        assertEquals("&#00FF00Hello", HexColorCodes.toLegacy("&x&0&0&F&F&0&0Hello"));
        assertEquals("&#00FF00Hello", HexColorCodes.toLegacy("&X&0&0&F&F&0&0Hello"));
    }

    @Test
    void convertsSectionSignMiniMessageHexToLegacy() {
        assertEquals("&#ff0000Hello", HexColorCodes.toLegacy("§#ff0000Hello"));
        assertEquals("&#ff0000Hello", HexColorCodes.toLegacy("§x§f§f§0§0§0§0Hello"));
    }

    @Test
    void leavesLegacyHexUntouched() {
        assertEquals("&#ff0000Hello", HexColorCodes.toLegacy("&#ff0000Hello"));
    }

    @Test
    void leavesInvalidMiniMessageHexUntouched() {
        assertEquals("<#ff00>Hello", HexColorCodes.toLegacy("<#ff00>Hello"));
        assertEquals("<#gggggg>Hello", HexColorCodes.toLegacy("<#gggggg>Hello"));
    }
}
