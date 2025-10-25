package com.model;

/**
 * Lightweight Item model used by the backend.
 *
 * Immutable-ish metadata about an item (name, description, whether it's usable and/or consumable).
 * Concrete game behavior (what happens when you 'use' it) should be implemented by game code,
 * e.g. by matching on ItemName in EscapeRoomGameUI or by providing callbacks.
 */
public class Item {

    private final ItemName name;
    private final String description;
    private final boolean usable;     // can the player "use" this item?
    private final boolean consumable; // does using it remove it from inventory?
    private final String useText;     // default message when used (can be overridden)

    public Item(ItemName name, String description, boolean usable, boolean consumable, String useText) {
        this.name = name;
        this.description = description == null ? "" : description;
        this.usable = usable;
        this.consumable = consumable;
        this.useText = useText == null ? "" : useText;
    }

    // convenience constructor for non-usable items
    public Item(ItemName name, String description) {
        this(name, description, false, false, null);
    }

    public ItemName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUsable() {
        return usable;
    }

    public boolean isConsumable() {
        return consumable;
    }

    public String getUseText() {
        return useText;
    }

    @Override
    public String toString() {
        return "Item{name=" + name + ", usable=" + usable + ", consumable=" + consumable + ", desc='" + description + "'}";
    }
}

