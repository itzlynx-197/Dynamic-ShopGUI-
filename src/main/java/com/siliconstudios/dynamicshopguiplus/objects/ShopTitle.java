

package com.siliconstudios.dynamicshopguiplus.objects;

import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.providers.UserManager;
import org.bukkit.entity.Player;

public class ShopTitle {
    private final Title title;
    private final Title bedrockTitle;

    public ShopTitle(Translatable title, Translatable bedrockTitle) {
        this.title = new Title(title);
        this.bedrockTitle = bedrockTitle == null ? this.title : new Title(bedrockTitle);
    }

    public ShopTitle(String section) {
        this.title = new Title(DynamicShopGUIPlus.getInstance().getSectionTitle(section));
        this.bedrockTitle = DynamicShopGUIPlus.getInstance().getBedrockSectionTitle(section) == null ? this.title : new Title(DynamicShopGUIPlus.getInstance().getBedrockSectionTitle(section));
    }

    public Translatable getTitle() {
        return this.title.get();
    }

    public Translatable getBedrockTitle() {
        return this.bedrockTitle.get();
    }

    public String getRaw() {
        return this.title.getRaw();
    }

    public Translatable getTitle(Player p, int page, int pages) {
        return (UserManager.getUser(p).isBedrock() ? this.bedrockTitle : this.title).getTitle(p, page, pages);
    }

    public boolean isStatic() {
        return this.title.isStatic() && this.bedrockTitle.isStatic();
    }

    private static class Title {
        private final Translatable title;
        private final boolean dynamicPages;

        private Title(Translatable title) {
            this.title = title;
            this.dynamicPages = title.toString().contains("%page%") || title.toString().contains("%pages%");
        }

        private Translatable get() {
            return this.title;
        }

        private String getRaw() {
            return this.title.toString();
        }

        private Translatable getTitle(Player p, int page, int pages) {
            Translatable title = this.title;
            if (this.dynamicPages) {
                title = this.title.replace("%page%", page + "").replace("%pages%", pages + "");
            }
            return title;
        }

        private boolean isStatic() {
            return !this.dynamicPages;
        }
    }
}

