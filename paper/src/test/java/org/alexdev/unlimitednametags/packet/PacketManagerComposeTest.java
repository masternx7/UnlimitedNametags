package org.alexdev.unlimitednametags.packet;

import com.github.retrooper.packetevents.protocol.player.User;
import org.alexdev.unlimitednametags.UnlimitedNameTags;
import org.alexdev.unlimitednametags.nametags.NameTagManager;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PacketManagerComposeTest {

    @Test
    void mountsVisibleRowsEvenWhenOwnerSpawnCacheIsStale() throws Exception {
        final UUID viewerId = UUID.randomUUID();
        final User user = mock(User.class);
        when(user.getUUID()).thenReturn(viewerId);

        final Player owner = mock(Player.class);
        when(owner.getEntityId()).thenReturn(42);
        when(owner.getUniqueId()).thenReturn(UUID.randomUUID());

        final PaperTextPacketNameTag row = mock(PaperTextPacketNameTag.class);
        when(row.canViewerSee(viewerId)).thenReturn(true);
        when(row.displayEntityId()).thenReturn(101);

        final NameTagManager nameTags = mock(NameTagManager.class);
        when(nameTags.getPacketDisplays(owner)).thenReturn(List.<PaperNametagRow>of(row));

        final UnlimitedNameTags plugin = mock(UnlimitedNameTags.class);
        when(plugin.getNametagManager()).thenReturn(nameTags);

        final Map<User, PassengerState> connections = new ConcurrentHashMap<>();
        final java.util.Set<Integer> rowIds = ConcurrentHashMap.newKeySet();

        final PacketManager manager = allocateManager();
        set(manager, "plugin", plugin);
        set(manager, "connections", connections);
        set(manager, "levels", new ConcurrentHashMap<User, String>());
        set(manager, "rowIds", rowIds);
        rowIds.add(101);

        final PassengerState state = new PassengerState();
        state.spawn(101); // the row is already on the client
        connections.put(user, state);

        // HMC's force-riding packet replaces the passenger list. The owner spawn cache is stale,
        // but the row is still visible, so it must be kept alongside HMC's backpack passenger.
        assertFalse(state.knows(42), "test requires a stale owner-spawn cache");
        assertArrayEquals(new int[]{7, 101}, manager.passengers(user, owner, new int[]{7}));
    }

    private static PacketManager allocateManager() throws Exception {
        final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        return (PacketManager) ((Unsafe) unsafeField.get(null)).allocateInstance(PacketManager.class);
    }

    private static void set(PacketManager manager, String name, Object value) throws Exception {
        final Field field = PacketManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(manager, value);
    }
}
