package com.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.StringJoiner;

/**
 * Represents a simple inventory system that tracks quantities of {@link ItemName} items
 * and stores one {@link Item} template per item type to preserve metadata (e.g. usability, description).
 *
 * Provides basic operations for adding, removing, checking, and using items.
 */
public class Inventory {

    /** Map of item type to quantity owned. */
    private final Map<ItemName, Integer> quantities = new ConcurrentHashMap<>();

    /** Map of item type to its metadata template. */
    private final Map<ItemName, Item> templates = new ConcurrentHashMap<>();
    
    /** Creates an empty inventory. */
    public Inventory() {
    }

    /**
     * Adds one copy of the given item to the inventory.
     * If the item type is new, its metadata is stored as a template.
     *
     * @param item the {@link Item} to add
     */
    public void addItem(Item item) {
        addItem(item, 1);
    }

    /**
     * Adds a specific quantity of the given item.
     * Stores the provided item as the template if this type was not seen before.
     *
     * @param item the {@link Item} to add
     * @param qty number of copies to add
     */
    public void addItem(Item item, int qty) {
        if (item == null || qty <= 0) return;
        ItemName nm = item.getName();
        templates.putIfAbsent(nm, item);
        quantities.merge(nm, qty, Integer::sum);
    }

    /**
     * Adds an item by its {@link ItemName}, with an optional default template if new.
     *
     * @param name the item type
     * @param qty number of copies to add
     * @param templateIfNew template to register if item type is new
     */
    public void addItemByName(ItemName name, int qty, Item templateIfNew) {
        if (name == null || qty <= 0) return;
        if (templateIfNew != null) templates.putIfAbsent(name, templateIfNew);
        quantities.merge(name, qty, Integer::sum);
    }

    /**
     * Removes up to the specified number of copies of an item.
     *
     * @param name the item type to remove
     * @param qty number of copies to remove
     * @return the actual number of copies removed
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
     * Returns the number of copies currently held for the given item type.
     *
     * @param name the item type
     * @return quantity owned (0 if none)
     */
    public int getQuantity(ItemName name) {
        return quantities.getOrDefault(name, 0);
    }

    /**
     * Checks whether at least one copy of the specified item type exists.
     *
     * @param name the item type
     * @return true if at least one copy exists
     */
    public boolean has(ItemName name) {
        return getQuantity(name) > 0;
    }

    /**
     * Uses an item according to its template:
     * - returns false if item not present or not usable,
     * - decrements quantity if consumable,
     * - leaves inventory unchanged if non-consumable.
     *
     * @param name the item type to use
     * @return true if item was successfully used; false otherwise
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
     * Returns the metadata template for a given item type.
     *
     * @param name the item type
     * @return the stored {@link Item} template, or null if none
     */
    public Item getTemplate(ItemName name) {
        return templates.get(name);
    }

    /**
     * Returns an unmodifiable view of current item quantities.
     *
     * @return an unmodifiable map of {@link ItemName} to quantity
     */
    public Map<ItemName, Integer> getQuantities() {
        return Collections.unmodifiableMap(new EnumMap<>(quantities));
    }

    /**
     * Returns a readable string representation of the inventory contents.
     */
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


