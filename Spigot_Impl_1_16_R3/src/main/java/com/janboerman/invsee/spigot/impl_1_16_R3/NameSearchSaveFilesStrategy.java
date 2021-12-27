package com.janboerman.invsee.spigot.impl_1_16_R3;

import com.janboerman.invsee.spigot.api.resolve.NameResolveStrategy;
import com.janboerman.invsee.spigot.internal.CompletedEmpty;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.WorldNBTStorage;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NameSearchSaveFilesStrategy implements NameResolveStrategy {

    private static final int TAG_END = 0;
    private static final int TAG_BYTE = 1;
    private static final int TAG_SHORT = 2;
    private static final int TAG_INT = 3;
    private static final int TAG_LONG = 4;
    private static final int TAG_FLOAT = 5;
    private static final int TAG_DOUBLE = 6;
    private static final int TAG_BYTE_ARRAY = 7;
    private static final int TAG_STRING = 8;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;
    private static final int TAG_INT_ARRAY = 11;
    private static final int TAG_LONG_ARRAY = 12;
    private static final int TAG_UNKNOWN = 99;

    private final Plugin plugin;

    public NameSearchSaveFilesStrategy(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Optional<String>> resolveUserName(UUID uniqueId) {
        CraftServer craftServer = (CraftServer) plugin.getServer();
        WorldNBTStorage worldNBTStorage = craftServer.getHandle().playerFileData;

        File playerDirectory = worldNBTStorage.getPlayerDir();
        if (!playerDirectory.exists() || !playerDirectory.isDirectory())
            return CompletedEmpty.the();

        return CompletableFuture.supplyAsync(() -> {
            NBTTagCompound compound = worldNBTStorage.getPlayerData(uniqueId.toString());
            if (compound.hasKeyOfType("bukkit", TAG_COMPOUND)) {
                NBTTagCompound bukkit = compound.getCompound("bukkit");
                if (bukkit.hasKeyOfType("lastKnownName", TAG_STRING)) {
                    String lastKnownName = bukkit.getString("lastKnownName");
                    return Optional.of(lastKnownName);
                }
            }
            return Optional.empty();
        }, runnable -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

}
