

package com.siliconstudios.dynamicshopguiplus.util.scheduler.schedulers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.ScheduledTask;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.ServerScheduler;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.tasks.FoliaTask;
import org.bukkit.plugin.Plugin;

public class FoliaScheduler
implements ServerScheduler {
    private final GlobalRegionScheduler globalRegionScheduler;
    private final AsyncScheduler asyncScheduler;
    private final ScheduledExecutorService thread = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setNameFormat("DSGP_UTIL_THREAD #%d").build());

    public FoliaScheduler(DynamicShopGUIPlus plugin) {
        this.globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
    }

    @Override
    public ScheduledTask runTask(DynamicShopGUIPlus plugin, Runnable run) {
        return new FoliaTask(this.globalRegionScheduler.run((Plugin)plugin, t -> run.run()));
    }

    @Override
    public ScheduledTask runTaskLater(DynamicShopGUIPlus plugin, Runnable run, long delay) {
        if (delay <= 0L) {
            return this.runTask(plugin, run);
        }
        return new FoliaTask(this.globalRegionScheduler.runDelayed((Plugin)plugin, t -> run.run(), delay));
    }

    @Override
    public ScheduledTask runTaskTimer(DynamicShopGUIPlus plugin, Runnable run, long delay, long period) {
        if (delay <= 0L) {
            delay = 1L;
        }
        return new FoliaTask(this.globalRegionScheduler.runAtFixedRate((Plugin)plugin, t -> run.run(), delay, period));
    }

    @Override
    public ScheduledTask runTaskAsync(DynamicShopGUIPlus plugin, Runnable run) {
        return new FoliaTask(this.asyncScheduler.runNow((Plugin)plugin, t -> run.run()));
    }

    @Override
    public ScheduledTask runTaskLaterAsync(DynamicShopGUIPlus plugin, Runnable run, long delay) {
        if (delay <= 0L) {
            return this.runTaskAsync(plugin, run);
        }
        return new FoliaTask(this.asyncScheduler.runDelayed((Plugin)plugin, t -> run.run(), delay * 50L, TimeUnit.MILLISECONDS));
    }

    @Override
    public ScheduledTask runTaskAsyncTimer(DynamicShopGUIPlus plugin, Runnable run, long delay, long period) {
        if (delay <= 0L) {
            delay = 1L;
        }
        return new FoliaTask(this.asyncScheduler.runAtFixedRate((Plugin)plugin, t -> run.run(), delay * 50L, period * 50L, TimeUnit.MILLISECONDS));
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
        this.asyncScheduler.cancelTasks((Plugin)plugin);
        this.globalRegionScheduler.cancelTasks((Plugin)plugin);
        this.thread.shutdownNow();
    }
}

