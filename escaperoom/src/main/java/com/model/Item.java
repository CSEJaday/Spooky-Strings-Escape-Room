package com.model;

public class Item {
    private String itemName;
    private String iconFilePath; //icon??

    public Item(String name, String iconFilePath) 
    {
        this.itemName = name;
        this.iconFilePath = iconFilePath;
    }

    public String getName() 
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
