

package com.siliconstudios.dynamicshopguiplus.providers.requirements.items;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.providers.RequirementManager;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemRequirementLoadException;
import org.bukkit.entity.Player;

public class TimeRequirement
implements ItemRequirement {
    private final TimeRange range;
    private final RequirementManager.RequirementLore lore;

    public TimeRequirement(String timestamp, List<String> lore) throws ItemRequirementLoadException {
        String[] stamps = timestamp.split("-");
        try {
            this.range = new TimeRange(stamps[0], stamps[1]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new ItemRequirementLoadException("Invalid time range for " + timestamp + ", should be formatted as 00:00-00:00");
        }
        this.lore = RequirementManager.getLore(lore, Lang.TIME_REQUIREMENT_LORE.get().toString(), this.range.getMin(), this.range.getMax());
    }

    @Override
    public boolean isMet(Player p) {
        return this.range.isInRange(p.getWorld().getTime());
    }

    @Override
    public String[] getLore(Player p, boolean fast, boolean pr) {
        return this.lore.get(p, fast, pr, "%progress%", this.range.format12(p.getWorld().getTime()));
    }

    @Override
    public void sendNotMetMessage(Player p) {
        SendMessage.chatToPlayer(p, Lang.TIME_REQUIREMENT_NOT_MET.get(p).replace("%minTime%", this.range.getMin()).replace("%maxTime%", this.range.getMax()).replace("%progress%", this.range.format12(p.getWorld().getTime())));
    }

    private class TimeRange {
        private final int ticksAtMidnight = 18000;
        private final int ticksPerDay = 24000;
        private final int ticksPerHour = 1000;
        private final double ticksPerMinute = 16.666666666666668;
        private final double ticksPerSecond = 0.2777777777777778;
        private final SimpleDateFormat SDFTwentyFour = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        private final SimpleDateFormat SDFTwelve = new SimpleDateFormat("h:mm aa", Locale.ENGLISH);
        private final long min;
        private final long max;

        public TimeRange(String min, String max) throws ItemRequirementLoadException {
            this.min = this.parse24(min);
            this.max = this.parse24(max);
        }

        public boolean isInRange(long time) {
            if (this.min > this.max) {
                return time >= this.min || time <= this.max;
            }
            return time >= this.min && time <= this.max;
        }

        public String getMin() {
            return this.format12(this.min);
        }

        public String getMax() {
            return this.format12(this.max);
        }

        private long parse24(String stamp) throws ItemRequirementLoadException {
            if (stamp.length() != 5 || !stamp.contains(":")) {
                throw new ItemRequirementLoadException("Invalid timestamp for " + stamp + ", should be formatted as 00:00");
            }
            if (!(stamp = stamp.replace(":", "")).matches("^[0-9]{2}[^0-9]?[0-9]{2}$")) {
                throw new ItemRequirementLoadException("Invalud timestamp for " + stamp + ", can only contain numbers");
            }
            int hours = Integer.parseInt((stamp = stamp.toLowerCase(Locale.ENGLISH).replaceAll("[^0-9]", "")).substring(0, 2));
            if (hours > 24 || hours < 0) {
                throw new ItemRequirementLoadException("Invalid amount of hours for timestamp " + stamp + " should be in range of 0-24");
            }
            int minutes = Integer.parseInt(stamp.substring(2, 4));
            if (minutes > 60 || minutes < 0) {
                throw new ItemRequirementLoadException("Invalid amount of minutes for timestamp " + stamp + " should be in range of 0-60");
            }
            return this.hoursMinutesToTicks(hours, minutes);
        }

        private long hoursMinutesToTicks(int hours, int minutes) {
            Objects.requireNonNull(this);
            long ret = 18000L;
            ret += (long)(hours * this.ticksPerHour);
            ret = (long)((double)ret + (double)minutes / 60.0 * (double)this.ticksPerHour);
            Objects.requireNonNull(this);
            return ret %= 24000L;
        }

        
        public String format24(long ticks) {
            SimpleDateFormat simpleDateFormat = this.SDFTwentyFour;
            synchronized (simpleDateFormat) {
                return this.formatDateFormat(ticks, this.SDFTwentyFour);
            }
        }

        
        public String format12(long ticks) {
            SimpleDateFormat simpleDateFormat = this.SDFTwelve;
            synchronized (simpleDateFormat) {
                return this.formatDateFormat(ticks, this.SDFTwelve);
            }
        }

        private String formatDateFormat(long ticks, SimpleDateFormat format) {
            Date date = this.ticksToDate(ticks);
            return format.format(date);
        }

        private Date ticksToDate(long ticks) {
            Objects.requireNonNull(this);
            Objects.requireNonNull(this);
            ticks = ticks - 18000L + 24000L;
            Objects.requireNonNull(this);
            long days = ticks / 24000L;
            Objects.requireNonNull(this);
            Objects.requireNonNull(this);
            long hours = (ticks -= days * 24000L) / 1000L;
            Objects.requireNonNull(this);
            long minutes = (long)Math.floor((double)(ticks -= hours * 1000L) / this.ticksPerMinute);
            double dticks = (double)ticks - (double)minutes * this.ticksPerMinute;
            long seconds = (long)Math.floor(dticks / this.ticksPerSecond);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
            cal.setLenient(true);
            cal.set(0, 0, 1, 0, 0, 0);
            cal.add(6, (int)days);
            cal.add(11, (int)hours);
            cal.add(12, (int)minutes);
            cal.add(13, (int)seconds + 1);
            return cal.getTime();
        }
    }
}

