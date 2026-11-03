

package com.siliconstudios.dynamicshopguiplus.util;

import java.util.List;

public interface ItemLayout {
    public boolean containsKey(int var1);

    public void add(String var1);

    public void set(int var1, String var2);

    public List<String> build(Integer var1, int var2);
}

