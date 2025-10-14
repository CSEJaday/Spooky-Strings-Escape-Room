package com.model;

import java.util.*;

public class Inventory {
    private static Inventory instance;

    private HashMap<Item, Integer> inventory;

    private Inventory()
    {
        inventory = new HashMap<>();
    }

    public static Inventory getInstance()
    {
        if (instance == null)
        {
            instance = new Inventory();
        }
        return instance;
    }

    public void addItem(Item item)
    {
        if (inventory.containsKey(item))
        {
            int quantity = inventory.get(item);
            if (quantity > 1)
            {
                inventory.put(item, quantity - 1);
            }
            else
            {
                inventory.remove(item);
            }
        }
    }

    // method to remove item once used 
    public void removeItem(Item item) 
    {
        if (inventory.containsKey(item)) 
        {
            int quantity = inventory.get(item);
            if (quantity > 1) 
            {
                inventory.put(item, quantity - 1);
            } 
            else 
            {
                inventory.remove(item);
            }
        }
    }
    public int getItemQuantity(Item item)
    {
        return inventory.getOrDefault(item, 0);
    }

    public void sort()
    {
        List<Map.Entry<Item, Integer>> sortedList = new ArrayList<>(inventory.entrySet());
        //sort by item name assuming Item has getName()
        sortedList.sort(Comparator.comparing(entry -> entry.getKey().getName()));

        inventory = new LinkedHashMap<>();
        for (Map.Entry<Item, Integer> entry : sortedList)
        {
            inventory.put(entry.getKey(), entry.getValue());
        }

    }
    // string representation of inventory
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Inventory: \n");
        for (Map.Entry<Item, Integer> entry : inventory.entrySet())
        {
            sb.append(entry.getKey().getName())
            .append(" x")
            .append(entry.getValue())
            .append("\n");
        }
        return sb.toString();
    }
}

