package com.model;

public class Item {
    private String itemName;
    private String filePath;

    public Item(String name, String filePath) {
        this.itemName = name;
        this.filePath = filePath;
    }

    public String getItemName() {
        return itemName;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "Item: " + itemName + " (Path: " + filePath + ")";
    }
}
