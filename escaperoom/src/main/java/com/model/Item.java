package com.model;

public class Item {
    private ItemName itemName;
    private String iconFilePath; //icon??

    public Item(ItemName name, String iconFilePath) 
    {
        this.itemName = name;
        this.iconFilePath = iconFilePath;
    }

    public ItemName getName() 
    {
        return itemName;
    }

    public String getIconFilePath() 
    {
        return iconFilePath;
    }

    public void useItem()
    {
        System.out.println("Using item:" + itemName);
    }
    @Override
    public String toString() 
    {
        return "Item: " + itemName + " (Path: " + iconFilePath + ")";
    }
}
