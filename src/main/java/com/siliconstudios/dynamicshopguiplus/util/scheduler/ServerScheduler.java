

package com.siliconstudios.dynamicshopguiplus.util.scheduler;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.util.scheduler.ScheduledTask;

public interface ServerScheduler {
    public ScheduledTask runTask(DynamicShopGUIPlus var1, Runnable var2);

    public ScheduledTask runTaskLater(DynamicShopGUIPlus var1, Runnable var2, long var3);

    public ScheduledTask runTaskTimer(DynamicShopGUIPlus var1, Runnable var2, long var3, long var5);

    public ScheduledTask runTaskAsync(DynamicShopGUIPlus var1, Runnable var2);

    public ScheduledTask runTaskLaterAsync(DynamicShopGUIPlus var1, Runnable var2, long var3);

    public ScheduledTask runTaskAsyncTimer(DynamicShopGUIPlus var1, Runnable var2, long var3, long var5);

    public void runKillableTask(Runnable var1);

    public void runKillableTaskLater(Runnable var1, long var2);

    public void cancelTasks(DynamicShopGUIPlus var1);
}

