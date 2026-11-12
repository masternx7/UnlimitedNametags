package org.alexdev.unlimitednametags.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import org.alexdev.unlimitednametags.UnlimitedNameTags;
import org.alexdev.unlimitednametags.nametags.NameTagManager;
import org.alexdev.unlimitednametags.packet.PacketManager;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class PacketEventsListenerPassengersTest {
    @Test
    void recoveryMountReachesComposerWhenOwnerCacheIsStale() {
        final UnlimitedNameTags plugin = mock(UnlimitedNameTags.class);
        final PacketManager packets = mock(PacketManager.class);
        final PlayerListener players = mock(PlayerListener.class);
        final Player owner = mock(Player.class);
        final User viewer = mock(User.class);
        final PacketSendEvent event = mock(PacketSendEvent.class);
        when(plugin.getPacketManager()).thenReturn(packets);
        when(plugin.getPlayerListener()).thenReturn(players);
        when(plugin.getNametagManager()).thenReturn(mock(NameTagManager.class));
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.SET_PASSENGERS);
        when(event.getUser()).thenReturn(viewer);
        when(players.getPlayerFromEntityId(42)).thenReturn(Optional.of(owner));
        when(packets.isRow(101)).thenReturn(true);
        when(packets.knowsOwner(viewer, owner)).thenReturn(false);
        final int[] original = {7, 101}; // cosmetic passenger plus a recovery nametag
        final int[] composed = {7, 102}; // composer drops stale rows and keeps the current one
        when(packets.passengers(viewer, owner, original)).thenReturn(composed);

        try (var construction = mockConstruction(WrapperPlayServerSetPassengers.class, (packet, context) -> {
            when(packet.getEntityId()).thenReturn(42);
            when(packet.getPassengers()).thenReturn(original);
        })) {
            new PacketEventsListener(plugin).onPacketSend(event);

            verify(event, never()).setCancelled(true);
            verify(packets).passengers(viewer, owner, original);
            verify(construction.constructed().getFirst()).setPassengers(composed);
            verify(event).markForReEncode(true);
        }
    }
}
