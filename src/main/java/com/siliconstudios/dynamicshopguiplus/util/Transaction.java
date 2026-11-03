

package com.siliconstudios.dynamicshopguiplus.util;

import com.siliconstudios.dynamicshopguiplus.files.Lang;

public class Transaction {

    public static enum Result {
        SUCCESS,
        SUCCESS_COMMANDS_EXECUTED,
        ITEM_ERROR,
        DISPLAY_ITEM,
        NEGATIVE_ITEM_PRICE,
        NO_PERMISSIONS,
        REQUIREMENTS_FAILED,
        INSUFFICIENT_FUNDS,
        NO_INVENTORY_SPACE,
        TRANSACTION_CANCELLED,
        NO_ITEMS_FOUND,
        NOT_ENOUGH_ITEMS,
        TO_MANY_ITEMS,
        NO_ITEM_STOCK_LEFT,
        REACHED_SELL_LIMIT,
        CANT_STORE_PAYMENT;

    }

    public static enum Type {
        SELL_GUI_SCREEN(Lang.SELLGUI_SCREEN.get().getLegacy()),
        SELL_ALL_COMMAND(Lang.SELLALL_COMMAND.get().getLegacy()),
        SELL_ALL_SCREEN(Lang.SELLALL_SCREEN.get().getLegacy()),
        SELL_SCREEN(Lang.SELL_SCREEN.get().getLegacy()),
        BUY_SCREEN(Lang.BUY_SCREEN.get().getLegacy()),
        BUY_STACKS_SCREEN(Lang.BUYSTACKS_SCREEN.get().getLegacy()),
        QUICK_SELL(Lang.QUICK_SELL.get().getLegacy()),
        QUICK_BUY(Lang.QUICK_BUY.get().getLegacy()),
        SHOPSTAND_BUY_SCREEN(Lang.SHOPSTAND_BUY_SCREEN.get().getLegacy()),
        SHOPSTAND_SELL_SCREEN(Lang.SHOPSTAND_SELL_SCREEN.get().getLegacy()),
        AUTO_SELL_CHEST(Lang.AUTO_SELL_CHEST.get().getLegacy()),
        API_BUY("API buy (%type%)"),
        API_SELL("API sell (%type%)");

        private String name;
        private String customType;

        private Type(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public Type withCustomType(String type) {
            this.customType = type.toUpperCase().replace(" ", "_");
            this.name = this.name.replace("%type%", this.customType);
            return this;
        }

        public String enumName() {
            return this.name() + (this.customType != null ? "_" + this.customType : "");
        }

        public static Type fromValue(String s) {
            if (s.startsWith("API")) {
                String[] parts = s.split("_", 3);
                Type t = Type.valueOf(parts[0] + "_" + parts[1]);
                t.customType = parts[2];
                t.name = t.name.replace("%type%", t.customType);
                return t;
            }
            return Type.valueOf(s);
        }

        public Mode getTransactionMode() {
            return Mode.getFromType(this);
        }

        public String getMode() {
            return Mode.getFromType(this).getName();
        }
    }

    public static enum Mode {
        BUY(Lang.BOUGHT.get().getLegacy()),
        SELL(Lang.SOLD.get().getLegacy());

        private String name;

        private Mode(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static Mode getFromType(Type type) {
            return type == Type.BUY_SCREEN || type == Type.BUY_STACKS_SCREEN || type == Type.QUICK_BUY || type == Type.SHOPSTAND_BUY_SCREEN || type == Type.API_BUY ? BUY : SELL;
        }
    }
}

