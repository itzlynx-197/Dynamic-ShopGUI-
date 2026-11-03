

package com.siliconstudios.dynamicshopguiplus.providers.requirements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.ItemRequirement;
import com.siliconstudios.dynamicshopguiplus.providers.requirements.RequirementType;
import org.jetbrains.annotations.NotNull;

public class Requirements
implements Collection<ItemRequirement> {
    private final Collection<ItemRequirement> requirements = new ArrayList<ItemRequirement>();

    @Override
    public int size() {
        return this.requirements.size();
    }

    @Override
    public boolean isEmpty() {
        return this.requirements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.requirements.contains(o);
    }

    @Override
    @NotNull
    public Iterator<ItemRequirement> iterator() {
        return this.requirements.iterator();
    }

    @Override
    @NotNull
    public Object[] toArray() {
        return this.requirements.toArray();
    }

    @Override
    @NotNull
    public <T> T[] toArray(@NotNull T[] a) {
        return null;
    }

    @Override
    public boolean add(ItemRequirement requirement) {
        return this.requirements.add(requirement);
    }

    @Override
    public boolean remove(Object o) {
        return this.requirements.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.requirements.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends ItemRequirement> c) {
        return this.requirements.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return this.requirements.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return this.requirements.retainAll(c);
    }

    @Override
    public void clear() {
        this.requirements.clear();
    }

    public ItemRequirement getByType(RequirementType type) {
        for (ItemRequirement requirement : this.requirements) {
            if (requirement.getClass() != type.getClazz()) continue;
            return requirement;
        }
        return null;
    }

    public boolean hasType(RequirementType type) {
        for (ItemRequirement requirement : this.requirements) {
            if (requirement.getClass() != type.getClazz()) continue;
            return true;
        }
        return false;
    }

    public boolean removeType(RequirementType type) {
        for (ItemRequirement requirement : this.requirements) {
            if (requirement.getClass() != type.getClazz()) continue;
            return this.requirements.remove(requirement);
        }
        return false;
    }
}

