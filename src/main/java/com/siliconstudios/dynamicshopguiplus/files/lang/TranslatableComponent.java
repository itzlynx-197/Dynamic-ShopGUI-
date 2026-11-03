

package com.siliconstudios.dynamicshopguiplus.files.lang;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.util.AdventureUtil;
import com.siliconstudios.dynamicshopguiplus.util.ChatUtil;
import com.siliconstudios.dynamicshopguiplus.util.meta.PaperMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class TranslatableComponent
implements Translatable {
    private final String preTranslated;

    public TranslatableComponent(String s) {
        this.preTranslated = ChatUtil.getAdventureUtils().formatLegacyToMini(s);
    }

    public TranslatableComponent(String preTranslated, boolean original) {
        this.preTranslated = preTranslated;
    }

    public TranslatableComponent(Component component) {
        this.preTranslated = AdventureUtil.serialize(component);
    }

    @Override
    public Translatable replace(String ph, String value) {
        if (this.preTranslated.indexOf(ph) == -1) {
            return this;
        }
        return new TranslatableComponent(this.preTranslated.replace(ph, ChatUtil.getAdventureUtils().formatLegacyToMini(value)), false);
    }

    @Override
    public Translatable replace(String ph, Translatable value) {
        if (this.preTranslated.indexOf(ph) == -1) {
            return this;
        }
        return new TranslatableComponent(this.preTranslated.replace(ph, ChatUtil.getAdventureUtils().formatLegacyToMini(value.toString())), false);
    }

    @Override
    public String getLegacy() {
        return AdventureUtil.toLegacy((Component)this.get());
    }

    @Override
    public String getBedrockLegacy() {
        return AdventureUtil.toBedrockLegacy((Component)this.get());
    }

    @Override
    public String toString() {
        return this.preTranslated;
    }

    @Override
    public Object get() {
        return ChatUtil.getAdventureUtils().formatMini(this.preTranslated);
    }

    @Override
    public List<Translatable> getLines() {
        return Arrays.asList(this.toString().split("\n")).stream().map(TranslatableComponent::new).collect(Collectors.toList());
    }

    @Override
    public Translatable.Builder builder() {
        return new ComponentBuilder(this);
    }

    public static class ComponentBuilder
    implements Translatable.Builder {
        private Component component;

        public ComponentBuilder(TranslatableComponent base) {
            this.component = (Component)base.get();
        }

        @Override
        public Translatable.Builder append(Translatable s) {
            this.component = this.component.append((Component)s.get());
            return this;
        }

        @Override
        public Translatable.Builder append(String s) {
            this.component = this.component.append((Component)Component.text((String)s));
            return this;
        }

        @Override
        public Translatable.Builder color(char c) {
            this.component = this.component.append((Component)Component.text((String)"", (TextColor)PaperMeta.COLOR_MAP.get(Character.valueOf(c))));
            return this;
        }

        @Override
        public Translatable build() {
            return new TranslatableComponent(this.component);
        }
    }
}

