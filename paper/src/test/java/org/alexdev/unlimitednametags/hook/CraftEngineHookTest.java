package org.alexdev.unlimitednametags.hook;

import net.kyori.adventure.key.Key;
import org.alexdev.unlimitednametags.config.Advanced;
import org.alexdev.unlimitednametags.hook.creative.JsonModelHeightResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftEngineHookTest {

    @Test
    void findsGeneratedPackBeforeOtherCandidates(@TempDir Path plugins) throws IOException {
        final Path generated = plugins.resolve("CraftEngine/generated/resource_pack.zip");
        Files.createDirectories(generated.getParent());
        Files.write(generated, new byte[]{1});
        final Path hosted = plugins.resolve("CraftEngine/cache/hosted/default/resource_pack.zip");
        Files.createDirectories(hosted.getParent());
        Files.write(hosted, new byte[]{2});

        assertEquals(generated.toFile(), CraftEngineHook.findPackFile(plugins.toFile()));
    }

    @Test
    void fallsBackToTheGeneratedPathWhenPackIsCreatedAfterStartup(@TempDir Path plugins) {
        assertEquals(plugins.resolve("CraftEngine/generated/resource_pack.zip").toFile(),
                CraftEngineHook.findPackFile(plugins.toFile()));
    }

    @Test
    void resolvesModernItemModelHeightFromCraftEnginePack(@TempDir Path temp) throws IOException {
        final Path zip = temp.resolve("resource_pack.zip");
        writePack(zip,
                "assets/test/items/wizard_hat.json",
                "{\"model\":{\"type\":\"model\",\"model\":\"test:wizard_hat\"}}",
                "assets/test/models/wizard_hat.json",
                "{\"elements\":[{\"to\":[0,32,0]}],"
                        + "\"display\":{\"head\":{\"scale\":[1,1,1],\"translation\":[0,4,0]}}}");

        final JsonModelHeightResolver resolver = new JsonModelHeightResolver(zip.toFile());
        final double height = resolver.heightForKey(Key.key("test", "wizard_hat")).orElseThrow();

        // 32 model pixels * scale 1 * CreativeHook's 1.1 multiplier + 4 translation pixels.
        assertEquals(39.2, height, 1.0e-6);
    }

    @Test
    void defaultMultiplierConvertsModelPixelsToBlocks() {
        // Resource-pack model units are 1/16 block, so a 32-pixel hat must raise the tag by two blocks.
        assertEquals(1f / 16f, new Advanced().getHelmetHeightYOffsetMultiplier());
        assertTrue(new Advanced().getHelmetHeightYOffsetMultiplier() > 0.0179f);
    }

    private static void writePack(Path zip, String... entries) throws IOException {
        try (OutputStream out = Files.newOutputStream(zip);
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            for (int i = 0; i < entries.length; i += 2) {
                zipOut.putNextEntry(new ZipEntry(entries[i]));
                zipOut.write(entries[i + 1].getBytes(StandardCharsets.UTF_8));
                zipOut.closeEntry();
            }
        }
    }
}
