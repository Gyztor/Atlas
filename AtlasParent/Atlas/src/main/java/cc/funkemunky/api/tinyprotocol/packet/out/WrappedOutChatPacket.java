package cc.funkemunky.api.tinyprotocol.packet.out;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedChatComponent;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedChatMessage;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedChatMessageType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class WrappedOutChatPacket extends NMSObject {
    private final static String packet = Server.CHAT;
    public static UUID b = new UUID(0L, 0L);

    public WrappedOutChatPacket(Object packetObj, Player player) {
        super(packetObj, player);
    }

    //Constructor (ichatbase, chattype);
    public WrappedOutChatPacket(String message, WrappedChatMessageType type) {
        if(ProtocolVersion.getGameVersion().isOrBelow(ProtocolVersion.v1_15_2))
        setPacket(packet, stcToComponent.invoke(null, message), type.toNMS());
        else setPacket(packet, new WrappedChatMessage(message).getObject(), type.toNMS(), b);
    }

    //Saving your fingers from the most common use for using this wrapper.
    public WrappedOutChatPacket(String message) {
        this(message, WrappedChatMessageType.CHAT);
    }

    private static WrappedClass chatBaseComp = Reflections.getNMSClass("IChatBaseComponent");
    private static WrappedClass outChatClass = Reflections.getNMSClass(packet);
    private static WrappedClass chatSerialClass = Reflections.getNMSClass("IChatBaseComponent$ChatSerializer");
    private static WrappedMethod stcToComponent = chatSerialClass.getMethod("a", String.class);
    private static WrappedMethod getTextMethod = chatBaseComp.getMethod("getText");
    private static WrappedField chatTypeField, chatCompField;

    private String message;
    private WrappedChatMessageType chatType;

    @Override
    public void process(Player player, ProtocolVersion version) {
        //Getting the message
        message = getTextMethod.invoke(getObject());

        //Getting the chat type.
        chatType = WrappedChatMessageType.fromNMS(chatTypeField.get(getObject()));
    }

    @Override
    public void updateObject() {

    }

    static {
        if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_12)) {
            chatTypeField = outChatClass.getFieldByType(Reflections.getNMSClass("ChatMessageType").getParent(), 0);
        } else if(ProtocolVersion.getGameVersion().isAbove(ProtocolVersion.V1_7_10)) {
            chatTypeField = outChatClass.getFieldByType(byte.class, 0);
        } else {
            chatTypeField = outChatClass.getFieldByType(int.class, 0);
        }
        chatCompField = outChatClass.getFieldByType(chatBaseComp.getParent(), 0);
    }
}
