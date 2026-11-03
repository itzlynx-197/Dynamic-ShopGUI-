

package com.siliconstudios.dynamicshopguiplus.files.lang;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;

public class TranslatableRaw
implements Translatable {
    private final String s;

    public TranslatableRaw(String s) {
        this.s = ChatUtil.formatColors(s);
    }

    @Override
    public Translatable replace(String ph, String value) {
        return new TranslatableRaw(this.s.replace(ph, value));
    }

    @Override
    public Translatable replace(String ph, Translatable value) {
        return new TranslatableRaw(this.s.replace(ph, (String)value.get()));
    }

    @Override
    public String getLegacy() {
        return this.s;
    }

    @Override
    public String getBedrockLegacy() {
        return this.s;
    }

    @Override
    public String toString() {
        return this.s;
    }

    @Override
    public Object get() {
        return this.s;
    }

    @Override
    public List<Translatable> getLines() {
        return Arrays.stream(this.s.split("\n")).map(TranslatableRaw::new).collect(Collectors.toList());
    }

    @Override
    public Translatable.Builder builder() {
        return new StringBuilder(this.s);
    }

    public class StringBuilder
    implements Translatable.Builder {
        private final java.lang.StringBuilder builder = new java.lang.StringBuilder();

        public StringBuilder(String base) {
            this.builder.append(base);
        }

        @Override
        public Translatable.Builder append(Translatable s) {
            this.builder.append(s.get());
            return this;
        }

        @Override
        public Translatable.Builder append(String s) {
            this.builder.append(s);
            return this;
        }

        @Override
        public Translatable.Builder color(char c) {
            this.builder.append("\u00a7").append(c);
            return this;
        }

        @Override
        public Translatable build() {
            return new TranslatableRaw(this.builder.toString());
        }
    }
}

