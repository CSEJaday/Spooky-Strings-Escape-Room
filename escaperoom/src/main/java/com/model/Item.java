package com.model;

/**
 * Immutable data model representing a game item.
 * Each item defines its type, description, usability, and behavior when used.
 */
public class Item {

    private final ItemName name;
    private final String description;
    private final boolean usable;     // can the player "use" this item?
    private final boolean consumable; // does using it remove it from inventory?
    private final String useText;     // default message when used (can be overridden)

    /**
     * Creates a fully defined item.
     *
     * @param name item type; must not be null
     * @param description short text describing the item (defaults to empty string if null)
     * @param usable true if the item can be used
     * @param consumable true if using the item removes it from inventory
     * @param useText text displayed when the item is used (defaults to empty string if null)
     */
    public Item(ItemName name, String description, boolean usable, boolean consumable, String useText) {
        this.name = name;
        this.description = description == null ? "" : description;
        this.usable = usable;
        this.consumable = consumable;
        this.useText = useText == null ? "" : useText;
    }

    /**
     * Creates a simple non-usable item with only a name and description.
     *
     * @param name item type
     * @param description short description; may be null
     */
    public Item(ItemName name, String description) {
        this(name, description, false, false, null);
    }

    /**
     * Get the item enum name.
     *
     * @return ItemName value.
     */
    public ItemName getName() {
        return name;
    }


    /**
     * Get the item's description.
     *
     * @return description (never null).
     */
    public String getDescription() {
        return description;
    }

    /**
     * Determine whether this item is usable.
     *
     * @return true when usable.
     */
    public boolean isUsable() {
        return usable;
    }

    /**
     * Determine whether this item is consumable.
     *
     * @return true when consumable.
     */
    public boolean isConsumable() {
        return consumable;
    }

    /**
     * Get the default use message for this item.
     *
     * @return useText string (may be empty).
     */
    public String getUseText() {
        return useText;
    }

    /**
     * Debug-friendly representation of the Item.
     *
     * @return short string describing the item.
     */
    @Override
    public String toString() {
        return "Item{name=" + name + ", usable=" + usable + ", consumable=" + consumable + ", desc='" + description + "'}";
    }
}

