package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ItemTest {

    @Test
    public void testFullConstructor_andGetters() {
        ItemName name = ItemName.values()[0];
        Item it = new Item(name, "A useful thing", true, true, "You used it.");

        assertEquals(name, it.getName());
        assertEquals("A useful thing", it.getDescription());
        assertTrue(it.isUsable());
        assertTrue(it.isConsumable());
        assertEquals("You used it.", it.getUseText());

        String s = it.toString();
        // toString contains key pieces of information
        assertTrue(s.contains(name.name()));
        assertTrue(s.contains("usable=true"));
        assertTrue(s.contains("consumable=true"));
        assertTrue(s.contains("A useful thing"));
    }

    @Test
    public void testSimpleConstructor_defaults() {
        ItemName name = ItemName.values()[0];
        Item it = new Item(name, null); // description null -> should default to empty string

        assertEquals(name, it.getName());
        assertEquals("", it.getDescription());
        assertFalse(it.isUsable());
        assertFalse(it.isConsumable());
        assertEquals("", it.getUseText()); // default from full ctor when null passed
    }

    @Test
    public void testUseTextAndDescriptionNullHandled() {
        ItemName name = ItemName.values()[0];
        Item it = new Item(name, null, true, false, null);

        assertEquals("", it.getDescription());
        assertEquals("", it.getUseText());
        assertTrue(it.isUsable());
        assertFalse(it.isConsumable());
    }
}


