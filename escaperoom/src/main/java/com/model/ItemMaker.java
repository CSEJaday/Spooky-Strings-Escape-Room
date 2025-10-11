package com.model;

public class ItemMaker {
    public Item createItem(ItemName name) {
        return new Item(name);
    }
}
