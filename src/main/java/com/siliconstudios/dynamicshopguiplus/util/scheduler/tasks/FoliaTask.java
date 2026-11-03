

package com.siliconstudios.dynamicshopguiplus.util.scheduler.tasks;

import com.siliconstudios.dynamicshopguiplus.util.scheduler.ScheduledTask;

public class FoliaTask
implements ScheduledTask {
    private final io.papermc.paper.threadedregions.scheduler.ScheduledTask task;

    public FoliaTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        this.task.cancel();
    }
}

