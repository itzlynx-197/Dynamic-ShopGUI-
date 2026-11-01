package com.silicon_studios.dynamic_shopgui_plus.features.commanditems;

import java.util.ArrayList;
import java.util.List;

public class CommandItem {
    public static class DelayedCommand {
        public final String command;
        public final long delayTicks;
        public final long repeatTicks;
        public final int repetitions;

        public DelayedCommand(String command, long delayTicks, long repeatTicks, int repetitions) {
            this.command = command;
            this.delayTicks = delayTicks;
            this.repeatTicks = repeatTicks;
            this.repetitions = repetitions;
        }
    }

    private final List<String> immediate = new ArrayList<>();
    private final List<DelayedCommand> delayed = new ArrayList<>();
    private final boolean runAsConsole;

    public CommandItem(boolean runAsConsole) {
        this.runAsConsole = runAsConsole;
    }

    public void addImmediate(String cmd) { immediate.add(cmd); }
    public void addDelayed(DelayedCommand dc) { delayed.add(dc); }
    public List<String> getImmediate() { return immediate; }
    public List<DelayedCommand> getDelayed() { return delayed; }
    public boolean isRunAsConsole() { return runAsConsole; }
    public boolean hasCommands() { return !immediate.isEmpty() || !delayed.isEmpty(); }
}