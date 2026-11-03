

package com.siliconstudios.dynamicshopguiplus.util.layouts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.ItemLayout;

public class PageLayout
implements ItemLayout {
    private final String section;
    private final HashMap<Integer, String> staticItems = new HashMap();
    private final List<String> softItems = new LinkedList<String>();

    public PageLayout(String section) {
        this.section = section;
    }

    @Override
    public boolean containsKey(int slot) {
        return this.staticItems.containsKey(slot);
    }

    @Override
    public void add(String index) {
        this.softItems.add(index);
    }

    @Override
    public void set(int slot, String index) {
        this.staticItems.put(slot, index);
    }

    @Override
    public List<String> build(Integer size, int defSize) {
        int minSize = Math.max(9, Math.min((int)Math.ceil((double)(this.softItems.size() + this.staticItems.size()) / 9.0) * 9, defSize));
        int fillSize = size == null ? minSize : size - (DynamicShopGUIPlus.getInstance().navBar.isEnabled(this.section) ? 9 : 0);
        ArrayList<String> order = new ArrayList<String>();
        for (int i = 0; i < Math.min(this.softItems.size(), fillSize); ++i) {
            order.add(this.softItems.get(i));
        }
        this.softItems.removeAll(order);
        ArrayList<Integer> keys = new ArrayList<Integer>(this.staticItems.keySet());
        Collections.sort(keys);
        for (Integer slot : keys) {
            if (slot > fillSize) continue;
            while (order.size() < slot) {
                order.add(null);
            }
            order.add(slot, this.staticItems.remove(slot));
        }
        this.softItems.addAll(this.staticItems.values());
        while (order.size() < fillSize) {
            order.add(null);
        }
        if (!this.softItems.isEmpty()) {
            for (String res : this.softItems) {
                if (res == null) continue;
                SendMessage.warnMessage("Cannot load item '%itemPath%' because it is out of range for page size %size%".replace("%itemPath%", this.section + "." + res).replace("%size%", String.valueOf(fillSize)));
            }
        }
        return order;
    }
}

