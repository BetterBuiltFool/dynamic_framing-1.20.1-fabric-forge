package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.data.FramedStructureStorage;
import dev.architectury.networking.NetworkManager;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.function.Supplier;

public class ChunkNodeRequestPacket {
    private final LongSet chunkPos = new LongOpenHashSet();
    private final ResourceKey<Level> dimensionKey;
    
    private static final ResourceKey<Level> OVERWORLD = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation("minecraft", "overworld")
    );
    
    public ChunkNodeRequestPacket( ResourceKey<Level> dimensionKey, long... chunkPos) {
        this.chunkPos.addAll(LongArrayList.wrap(chunkPos));
        this.dimensionKey = dimensionKey;
    }
    
    public ChunkNodeRequestPacket(ResourceKey<Level> dimensionKey, ChunkPos... chunkPos) {
        this(
                dimensionKey,
                Arrays.stream(chunkPos)
                      .mapToLong(ChunkPos::toLong)
                      .toArray()
        );
    }
    
    public ChunkNodeRequestPacket(long... chunkPos) {
        this(OVERWORLD, chunkPos);
    }
    
    public ChunkNodeRequestPacket(ChunkPos... chunkPos) {
        this(OVERWORLD, chunkPos);
    }
    
    public static ChunkNodeRequestPacket decode(FriendlyByteBuf buffer) {
        return new ChunkNodeRequestPacket(
                ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation()),
                buffer.readLongArray()
        );
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.dimensionKey.location());
        buffer.writeLongArray(this.chunkPos.toLongArray());
    }
    
    public void handle(Supplier<NetworkManager.PacketContext> contextSupplier) {
        var context = contextSupplier.get();
        context.queue(() -> {
            FramedStructureStorage.sendChunkDataToPlayer((ServerPlayer) context.getPlayer(), this.dimensionKey, this.chunkPos.toLongArray());
        });
    }
}
