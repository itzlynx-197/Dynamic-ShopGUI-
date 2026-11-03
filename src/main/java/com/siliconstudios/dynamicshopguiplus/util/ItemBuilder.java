

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.files.Lang;
import com.siliconstudios.dynamicshopguiplus.files.Translatable;
import com.siliconstudios.dynamicshopguiplus.methodes.CalculateAmount;
import com.siliconstudios.dynamicshopguiplus.util.ServerInfo;
import com.siliconstudios.dynamicshopguiplus.util.SkullUtil;
import com.siliconstudios.dynamicshopguiplus.util.XEnchantment;
import com.siliconstudios.dynamicshopguiplus.util.XMaterial;
import com.siliconstudios.dynamicshopguiplus.util.exceptions.ItemLoadException;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemBuilder {
    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(ItemStack item) {
        this.item = new ItemStack(item);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material mat) {
        this.item = new ItemStack(mat);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder withSimpleMeta(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            this.setDisplayName(meta.getDisplayName());
        }
        if (meta.hasLore()) {
            this.setLore(meta.getLore());
        }
        this.meta.addItemFlags(meta.getItemFlags().toArray(new ItemFlag[0]));
        this.setAmount(item.getAmount());
        return this;
    }

    public void setDisplayName(Translatable s) {
        DynamicShopGUIPlus.getInstance().getMetaUtils().setDisplayName(this.meta, s);
    }

    public ItemBuilder withDisplayName(Translatable s) {
        DynamicShopGUIPlus.getInstance().getMetaUtils().setDisplayName(this.meta, s);
        return this;
    }

    public String getRawDisplayName() {
        return DynamicShopGUIPlus.getInstance().getMetaUtils().getRawDisplayName(this.meta);
    }

    public void setRawDisplayName(String s) {
        DynamicShopGUIPlus.getInstance().getMetaUtils().setRawDisplayName(this.meta, s);
    }

    public void setDisplayName(String s) {
        this.meta.setDisplayName(s);
    }

    public ItemBuilder withDisplayName(String s) {
        this.meta.setDisplayName(s);
        return this;
    }

    public ItemBuilder withCLore(List<Translatable> lore) {
        DynamicShopGUIPlus.getInstance().getMetaUtils().setLore(this.meta, lore);
        return this;
    }

    public void setCLore(List<Translatable> lore) {
        DynamicShopGUIPlus.getInstance().getMetaUtils().setLore(this.meta, lore);
    }

    public ItemBuilder withLore(Translatable ... lore) {
        DynamicShopGUIPlus.getInstance().getMetaUtils().setLore(this.meta, Arrays.asList(lore));
        return this;
    }

    public List<String> getRawLore() {
        return DynamicShopGUIPlus.getInstance().getMetaUtils().getRawLore(this.meta);
    }

    public void setRawLore(List<String> lore) {
        DynamicShopGUIPlus.getInstance().getMetaUtils().setRawLore(this.meta, lore);
    }

    public ItemBuilder withLore(List<String> lore) {
        this.meta.setLore(lore);
        return this;
    }

    public void setLore(List<String> lore) {
        this.meta.setLore(lore);
    }

    public ItemBuilder addLore(List<String> lore) {
        List<String> list = this.meta.hasLore() ? this.meta.getLore() : new ArrayList();
        list.addAll(lore);
        this.meta.setLore(list);
        return this;
    }

    public ItemBuilder withLore(String ... lore) {
        this.meta.setLore(CalculateAmount.getMultipleItemLore(lore));
        return this;
    }

    public ItemBuilder removeLore() {
        this.meta.setLore(Collections.EMPTY_LIST);
        return this;
    }

    public ItemBuilder withEnchantGlint() {
        if (ServerInfo.supportsComponents()) {
            this.meta.setEnchantmentGlintOverride(Boolean.valueOf(true));
        } else {
            this.meta.addEnchant(XEnchantment.WATER_WORKER.parseEnchantment(), 1, false);
            this.meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
        }
        return this;
    }

    public ItemBuilder removeEnchants() {
        for (Enchantment ench : this.meta.getEnchants().keySet()) {
            this.meta.removeEnchant(ench);
        }
        return this;
    }

    public ItemBuilder replaceText(String old, String value) {
        if (this.meta.hasDisplayName()) {
            this.meta.setDisplayName(this.meta.getDisplayName().replace(old, value));
        }
        if (this.meta.hasLore()) {
            this.meta.setLore(this.meta.getLore().stream().map(s -> s.replace(old, value)).collect(Collectors.toList()));
        }
        return this;
    }

    public ItemBuilder replaceName(String old, String value) {
        if (!this.meta.hasDisplayName()) {
            return this;
        }
        this.meta.setDisplayName(this.meta.getDisplayName().replace(old, value));
        return this;
    }

    public ItemBuilder replaceLore(String old, String value) {
        if (!this.meta.hasLore()) {
            return this;
        }
        this.meta.setLore(this.meta.getLore().stream().map(s -> s.replace(old, value)).collect(Collectors.toList()));
        return this;
    }

    public void setOwner(String owner) throws ItemLoadException {
        if (!this.item.getType().equals((Object)XMaterial.PLAYER_HEAD.parseMaterial())) {
            throw new ItemLoadException(Lang.MATERIAL_NEEDS_TO_BE_SKULL.get());
        }
        SkullUtil.setSkullTexture(this.item, (SkullMeta)this.meta, owner, false);
    }

    public ItemBuilder withSkullOwner(String owner) {
        SkullUtil.setSkullTexture(this.item, (SkullMeta)this.meta, owner, false);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder withAmount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder hideFlags() {
        this.meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemBuilder hideInteractLore() {
        if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_21_R4) && !ServerInfo.supportsPaper()) {
            ItemFlag flag = ItemFlag.valueOf((String)"HIDE_BLOCK_ENTITY_DATA");
            this.meta.addItemFlags(new ItemFlag[]{flag});
        } else {
            this.meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    public ItemBuilder hideEntityData() {
        if (ServerInfo.getVersion().newerOrEqualAs(ServerInfo.Version.v1_21_R4) && !ServerInfo.supportsPaper()) {
            ItemFlag flag = ItemFlag.valueOf((String)"HIDE_BLOCK_ENTITY_DATA");
            this.meta.addItemFlags(new ItemFlag[]{flag});
        } else {
            this.meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
        }
        return this;
    }

    public ItemBuilder setArmorColor(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta)this.meta;
        meta.setColor(color);
        this.meta = meta;
        return this;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }
}

