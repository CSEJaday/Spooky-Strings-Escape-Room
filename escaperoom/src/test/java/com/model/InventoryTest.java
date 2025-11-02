package com.model;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class InventoryTest {

    private Inventory inv;
    private ItemName name; // first enum constant chosen dynamically

    @Before
    public void setUp() {
        inv = new Inventory();
        ItemName[] vals = ItemName.values();
        // pick first enum constant for tests; assume enum has at least one value
        name = vals[0];
    }

    @Test
    public void testConstructor_initialEmpty() {
        assertEquals(0, inv.getQuantity(name));
        assertFalse(inv.has(name));
        assertEquals("Inventory: (empty)", inv.toString());
    }

    @Test
    public void testAddItemByName_andQuantities() {
        inv.addItemByName(name, 2, null);
        assertEquals(2, inv.getQuantity(name));
        assertTrue(inv.has(name));

        // adding more increases quantity
        inv.addItemByName(name, 3, null);
        assertEquals(5, inv.getQuantity(name));
    }

    @Test
    public void testAddItemByName_invalidArgsIgnored() {
        // null name ignored
        inv.addItemByName(null, 5, null);
        // negative qty ignored
        inv.addItemByName(name, -3, null);
        // zero qty ignored
        inv.addItemByName(name, 0, null);

        assertEquals(0, inv.getQuantity(name));
    }

    @Test
    public void testRemove_partialAndExhaust() {
        inv.addItemByName(name, 4, null);
        // remove fewer than present
        int removed = inv.remove(name, 2);
        assertEquals(2, removed);
        assertEquals(2, inv.getQuantity(name));
        assertTrue(inv.has(name));

        // remove more than present -> only remaining removed
        removed = inv.remove(name, 5);
        assertEquals(2, removed);
        assertEquals(0, inv.getQuantity(name));
        assertFalse(inv.has(name));

        // removing when none -> returns 0
        removed = inv.remove(name, 1);
        assertEquals(0, removed);
    }

    @Test
    public void testGetQuantities_unmodifiableViewAndContents() {
        inv.addItemByName(name, 3, null);
        Map<ItemName, Integer> q = inv.getQuantities();
        // view contains the entry
        assertTrue(q.containsKey(name));
        assertEquals(Integer.valueOf(3), q.get(name));

        // attempting to modify should throw UnsupportedOperationException
        try {
            q.put(name, 10);
            throw new AssertionError("Expected UnsupportedOperationException when modifying quantities view");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testUseItem_withoutTemplate_nonConsumableDefault() {
        // add item by name but no template => templates.get(name) is null
        inv.addItemByName(name, 2, null);
        assertEquals(2, inv.getQuantity(name));

        // useItem should return true (no template -> treat as non-consumable primitive)
        boolean used = inv.useItem(name);
        assertTrue(used);

        // inventory should be unchanged (non-consumable)
        assertEquals(2, inv.getQuantity(name));
    }

    @Test
    public void testUseItem_nullNameReturnsFalse() {
        assertFalse(inv.useItem(null));
    }

    @Test
    public void testGetTemplate_defaultNull() {
        // no template added, so should be null
        assertEquals(null, inv.getTemplate(name));
    }

    @Test
    public void testToString_nonEmptyContainsNameAndCount() {
        inv.addItemByName(name, 7, null);
        String s = inv.toString();
        // should contain enum name and count "x7"
        assertTrue(s.contains(name.name()));
        assertTrue(s.contains("x7"));
        assertTrue(s.startsWith("Inventory: "));
    }
}
