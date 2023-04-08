package com.janboerman.invsee.folia;

import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.internal.Scheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FoliaScheduler implements Scheduler {

    private InvseePlusPlus plugin;

    public FoliaScheduler(InvseePlusPlus plugin) {
        this.plugin = plugin;
    }

    public void executeSyncPlayer(UUID playerId, Runnable task, Runnable retired) {
        Server server = plugin.getServer();
        Player player = server.getPlayer(playerId);
        if (player != null) {
            EntityScheduler scheduler = player.getScheduler();
            scheduler.run(plugin, scheduledTask -> task.run(), retired);
        } else {
            executeSyncGlobal(task);
        }
    }

    public void executeSyncPlayer(HumanEntity player, Runnable task, Runnable retired) {
        player.getScheduler().run(plugin, scheduledTask -> task.run(), retired);
    }

    @Override
    public void executeSyncGlobal(Runnable task) {
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, task);
    }

    @Override
    public void executeAsync(Runnable task) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    @Override
    public void executeLaterGlobal(Runnable task, long delayTicks) {
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
    }
}
