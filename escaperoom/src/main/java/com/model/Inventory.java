package com.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.StringJoiner;

/**
 * Simple inventory that tracks quantities of ItemName and stores a Item metadata per ItemName.
 */
public class Inventory {

    // map item name -> count
    private final Map<ItemName, Integer> quantities = new ConcurrentHashMap<>();

    private final Map<ItemName, Item> templates = new ConcurrentHashMap<>();

    public Inventory() {
    }

    /**
     * Add one copy of the item (registers template info if first time).
     */
    public void addItem(Item item) {
        addItem(item, 1);
    }

    /**
     * Add qty copies of the item. If a template for this ItemName does not yet exist, we keep the provided item as the template.
     */
    public void addItem(Item item, int qty) {
        if (item == null || qty <= 0) return;
        ItemName nm = item.getName();
        templates.putIfAbsent(nm, item);
        quantities.merge(nm, qty, Integer::sum);
    }

    /**
     * Convenience: increment by qty for an ItemName when you only have the name and a default template to use.
     */
    public void addItemByName(ItemName name, int qty, Item templateIfNew) {
        if (name == null || qty <= 0) return;
        if (templateIfNew != null) templates.putIfAbsent(name, templateIfNew);
        quantities.merge(name, qty, Integer::sum);
    }

    /**
     * Remove up to 'qty' copies of an item; returns the number actually removed.
     */
    public int remove(ItemName name, int qty) {
        if (name == null || qty <= 0) return 0;
        Integer current = quantities.getOrDefault(name, 0);
        if (current <= 0) return 0;
        int removed = Math.min(current, qty);
        int remaining = current - removed;
        if (remaining > 0) quantities.put(name, remaining);
        else quantities.remove(name);
        return removed;
    }

    /**
     * Return quantity for an item.
     */
    public int getQuantity(ItemName name) {
        return quantities.getOrDefault(name, 0);
    }

    /**
     * Test presence of an item.
     */
    public boolean has(ItemName name) {
        return getQuantity(name) > 0;
    }

    /**
     * Use an item (game logic should decide what "using" does). This method will:
     *  - check template metadata to see if the item is usable,
     *  - if usable and consumable, decrement the count,
     *  - return true when the item was present and usable.
     */
    public boolean useItem(ItemName name) {
        if (name == null) return false;
        Item template = templates.get(name);
        if (template == null) {
            // no metadata; conservatively allow "use" if there is at least one copy (non-consumable default)
            if (!has(name)) return false;
            // treat as non-consumable primitive
            return true;
        }

        if (!template.isUsable()) return false;
        if (template.isConsumable()) {
            int removed = remove(name, 1);
            return removed > 0;
        } else {
            // non-consumable usable tool
            return has(name);
        }
    }

    /**
     * Return the template Item metadata for a name, or null if unknown.
     */
    public Item getTemplate(ItemName name) {
        return templates.get(name);
    }

    /**
     * Get an unmodifiable view of current quantities.
     */
    public Map<ItemName, Integer> getQuantities() {
        return Collections.unmodifiableMap(new EnumMap<>(quantities));
    }

    @Override
    public String toString() {
        if (quantities.isEmpty()) return "Inventory: (empty)";
        StringJoiner sj = new StringJoiner(", ");
        for (Map.Entry<ItemName, Integer> e : quantities.entrySet()) {
            sj.add(e.getKey().name() + " x" + e.getValue());
        }
        return "Inventory: " + sj.toString();
    }
}


