package kirballs.usualallies.network;

import kirballs.usualallies.entity.kirb.KirbCarryEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EmptyClickPacket {
    private final boolean rightClick;

    public EmptyClickPacket(boolean rightClick) {
        this.rightClick = rightClick;
    }

    public static void encode(EmptyClickPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.rightClick);
    }

    public static EmptyClickPacket decode(FriendlyByteBuf buf) {
        return new EmptyClickPacket(buf.readBoolean());
    }

    public static void handle(EmptyClickPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            KirbCarryEvents.handleEmptyClickServer(player, packet.rightClick);
        });
        context.setPacketHandled(true);
    }
}
