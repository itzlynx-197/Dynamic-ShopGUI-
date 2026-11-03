

package com.siliconstudios.dynamicshopguiplus.util.scheduler.tasks;

import com.siliconstudios.dynamicshopguiplus.util.scheduler.ScheduledTask;

public class BukkitTask
implements ScheduledTask {
    private final org.bukkit.scheduler.BukkitTask task;

    public BukkitTask(org.bukkit.scheduler.BukkitTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        this.task.cancel();
    }
}

