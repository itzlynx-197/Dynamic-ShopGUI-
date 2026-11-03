

package com.siliconstudios.dynamicshopguiplus.util.scheduler.schedulers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.ScheduledTask;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.ServerScheduler;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.tasks.BukkitTask;
import org.bukkit.plugin.Plugin;

public class BukkitScheduler
implements ServerScheduler {
    private final org.bukkit.scheduler.BukkitScheduler scheduler;
    private final ScheduledExecutorService thread = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setNameFormat("DSGP_UTIL_THREAD #%d").build());

    public BukkitScheduler(DynamicShopGUIPlus plugin) {
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public ScheduledTask runTask(DynamicShopGUIPlus plugin, Runnable run) {
        return new BukkitTask(this.scheduler.runTask((Plugin)plugin, run));
    }

    @Override
    public ScheduledTask runTaskLater(DynamicShopGUIPlus plugin, Runnable run, long delay) {
        return new BukkitTask(this.scheduler.runTaskLater((Plugin)plugin, run, delay));
    }

    @Override
    public ScheduledTask runTaskTimer(DynamicShopGUIPlus plugin, Runnable run, long delay, long period) {
        return new BukkitTask(this.scheduler.runTaskTimer((Plugin)plugin, run, delay, period));
    }

    @Override
    public ScheduledTask runTaskAsync(DynamicShopGUIPlus plugin, Runnable run) {
        return new BukkitTask(this.scheduler.runTaskAsynchronously((Plugin)plugin, run));
    }

    @Override
    public ScheduledTask runTaskLaterAsync(DynamicShopGUIPlus plugin, Runnable run, long delay) {
        return new BukkitTask(this.scheduler.runTaskLaterAsynchronously((Plugin)plugin, run, delay));
    }

    @Override
    public ScheduledTask runTaskAsyncTimer(DynamicShopGUIPlus plugin, Runnable run, long delay, long period) {
        return new BukkitTask(this.scheduler.runTaskTimerAsynchronously((Plugin)plugin, run, delay, period));
    }

    @Override
    public void runKillableTask(Runnable run) {
        this.thread.execute(run);
    }

    @Override
    public void runKillableTaskLater(Runnable run, long delay) {
        this.thread.schedule(run, delay * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelTasks(DynamicShopGUIPlus plugin) {
        this.scheduler.cancelTasks((Plugin)plugin);
        this.thread.shutdownNow();
    }
}

