package cc.funkemunky.api.tinyprotocol.api;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.packets.ChannelInjector;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TinyProtocolHandler {
    @Getter
    private static ChannelInjector instance;

    public static boolean enabled = true;

    public TinyProtocolHandler() {
        // 1.8+ and 1.7 NMS have different class paths for their libraries used. This is why we have to separate the two.
        // These feed the packets asynchronously, before Minecraft processes it, into our own methods to process and be used as an API.

        instance = new ChannelInjector();

        Bukkit.getPluginManager().registerEvents(instance, Atlas.getInstance());
    }

    // Purely for making the code cleaner
    public static void sendPacket(Player player, Object packet) {
        instance.getChannel().sendPacket(player, packet);
    }

    public static int getProtocolVersion(Player player) {
        return -1;
    }

    private boolean didPosition = false;

    public Object onPacketOutAsync(Player sender, Object packet) {
        String name = packet.getClass().getName();
        int index = name.lastIndexOf(".");
        String packetName = name.substring(index + 1);

        PacketSendEvent event = new PacketSendEvent(sender, packet, packetName);

        //EventManager.callEvent(new cc.funkemunky.api.event.custom.PacketSendEvent(sender, packet, packetName));

        Atlas.getInstance().getEventManager().callEvent(event);

        return !event.isCancelled() ? event.getPacket() : null;
    }

    public Object onPacketInAsync(Player sender, Object packet) {
        String name = packet.getClass().getName();
        int index = name.lastIndexOf(".");
        String packetName = name.substring(index + 1).replace("PacketPlayInUseItem", "PacketPlayInBlockPlace")
                .replace(Packet.Client.LEGACY_LOOK, Packet.Client.LOOK)
                .replace(Packet.Client.LEGACY_POSITION, Packet.Client.POSITION)
                .replace(Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.POSITION_LOOK);

        PacketReceiveEvent event = new PacketReceiveEvent(sender, packet, packetName);

        //EventManager.callEvent(new cc.funkemunky.api.event.custom.PacketReceiveEvent(sender, packet, packetName));

        Atlas.getInstance().getEventManager().callEvent(event);

        return !event.isCancelled() ? event.getPacket() : null;
    }
}
