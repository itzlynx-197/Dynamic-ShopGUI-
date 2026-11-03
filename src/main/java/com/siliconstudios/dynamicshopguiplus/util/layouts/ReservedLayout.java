

package com.siliconstudios.dynamicshopguiplus.util.layouts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.siliconstudios.dynamicshopguiplus.DynamicShopGUIPlus;
import com.siliconstudios.dynamicshopguiplus.methodes.SendMessage;
import com.siliconstudios.dynamicshopguiplus.util.ItemLayout;

public class ReservedLayout
implements ItemLayout {
    private final String section;
    private final Map<Integer, Reserved> reserved = new HashMap<Integer, Reserved>();
    private final ArrayList<Reserved> outOfBounds = new ArrayList();

    public ReservedLayout(String section) {
        this.section = section;
    }

    public void reserve(int slot) {
        this.reserved.put(slot, new Reserved());
    }

    @Override
    public boolean containsKey(int slot) {
        Reserved res = this.reserved.get(slot);
        return res != null && res.index != null;
    }

    @Override
    public void add(String index) {
        for (Reserved res : this.reserved.values()) {
            if (res.getIndex() != null) continue;
            res.setIndex(index);
            return;
        }
        this.outOfBounds.add(new Reserved(index));
    }

    @Override
    public void set(int slot, String index) {
        Reserved reserved = this.reserved.get(slot);
        if (reserved != null && reserved.index != null) {
            this.shift(slot, index);
        } else {
            this.reserved.put(slot, new Reserved(index));
        }
    }

    private void shift(int slot, String index) {
        LinkedList<String> toAdd = new LinkedList<String>();
        ArrayList<Integer> keys = new ArrayList<Integer>(this.reserved.keySet());
        Collections.sort(keys);
        int last = keys.get(keys.size() - 1);
        for (int i = slot; i < last; ++i) {
            Reserved res = this.reserved.get(i);
            if (res == null) continue;
            if (toAdd.isEmpty() && res.index == null) {
                res.setIndex(index);
                return;
            }
            toAdd.add(res.index);
            res.setIndex(null);
        }
        for (String add : toAdd) {
            this.add(add);
        }
    }

    @Override
    public List<String> build(Integer size, int defSize) {
        int largest = this.reserved.entrySet().stream().filter(e -> ((Reserved)e.getValue()).index != null).map(Map.Entry::getKey).collect(Collectors.toList()).stream().max(Integer::compareTo).orElse(defSize);
        int minSize = Math.max(9, Math.min((int)Math.ceil((double)largest / 9.0) * 9, defSize));
        int fillSize = size == null ? minSize : size - (DynamicShopGUIPlus.getInstance().navBar.isEnabled(this.section) ? 9 : 0);
        ArrayList<String> order = new ArrayList<String>();
        for (int i = 0; i < fillSize; ++i) {
            Reserved res = this.reserved.remove(i);
            if (res != null && res.index != null) {
                order.add(res.getIndex());
                continue;
            }
            order.add(null);
        }
        while (order.size() < fillSize) {
            order.add(null);
        }
        this.outOfBounds.addAll(this.reserved.values());
        if (!this.outOfBounds.isEmpty()) {
            for (Reserved res : this.outOfBounds) {
                if (res.index == null) continue;
                SendMessage.warnMessage("Cannot load item '%itemPath%' because it is out of range for page size %size%".replace("%itemPath%", this.section + "." + res.index).replace("%size%", String.valueOf(fillSize)));
            }
        }
        return order;
    }

    public static final class Reserved {
        private String index;

        public Reserved() {
        }

        public Reserved(String index) {
            this.index = index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getIndex() {
            return this.index;
        }
    }
}

