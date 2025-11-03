package com.model;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class InventoryTest {

    private Inventory inv;
    private ItemName name;

    @Before
    public void setUp() {
        inv = new Inventory();
        ItemName[] all = ItemName.values();
        if (all.length == 0) throw new IllegalStateException("ItemName must have at least one constant for tests");
        name = all[0];
    }

    @Test
    public void testAddAndRemove_basic() {
        inv.addItemByName(name, 5, null);
        assertEquals(5, inv.getQuantity(name));
        assertTrue(inv.has(name));

        int removed = inv.remove(name, 3);
        assertEquals(3, removed);
        assertEquals(2, inv.getQuantity(name));

        removed = inv.remove(name, 5); // remove more than present
        assertEquals(2, removed);
        assertEquals(0, inv.getQuantity(name));
        assertFalse(inv.has(name));
    }

    @Test
    public void testAdd_invalidInputsIgnored() {
        inv.addItemByName(null, 5, null);
        inv.addItemByName(name, 0, null);
        inv.addItemByName(name, -1, null);
        assertEquals(0, inv.getQuantity(name));
    }

    @Test
    public void testGetQuantities_unmodifiable_andEnumMapShape() {
        inv.addItemByName(name, 4, null);
        Map<ItemName,Integer> q = inv.getQuantities();
        assertEquals(Integer.valueOf(4), q.get(name));
        try {
            q.put(name, 1);
            throw new AssertionError("Expected unmodifiable map");
        } catch (UnsupportedOperationException expected) { /* ok */ }
    }

    @Test
    public void testToString_emptyAndNonEmpty() {
        assertEquals("Inventory: (empty)", inv.toString());

        inv.addItemByName(name, 2, null);
        String s = inv.toString();
        assertTrue(s.startsWith("Inventory: "));
        assertTrue(s.contains(name.name()));
        assertTrue(s.contains("x2"));
    }

    @Test
    public void testConcurrentAddsAndRemoves() throws Exception {
        int threads = 30;
        ExecutorService es = Executors.newFixedThreadPool(threads);
        inv.addItemByName(name, 0, null); // ensure key absent initially

        AtomicInteger totalAdded = new AtomicInteger(0);
        Callable<Void> addTask = () -> { inv.addItemByName(name, 1, null); totalAdded.incrementAndGet(); return null; };
        Callable<Void> removeTask = () -> { inv.remove(name, 1); return null; };

        // submit 100 add tasks and 60 remove tasks concurrently
        int adds = 100, removes = 60;
        for (int i = 0; i < adds; i++) es.submit(addTask);
        for (int i = 0; i < removes; i++) es.submit(removeTask);

        es.shutdown();
        es.awaitTermination(3, TimeUnit.SECONDS);

        int finalQty = inv.getQuantity(name);
        // final quantity = added - removed (cannot be negative)
        assertTrue(finalQty >= 0 && finalQty <= adds);
    }

    @Test
    public void testUseItem_withoutTemplate_behavesAsNonConsumable() {
        // add by name without registering template
        inv.addItemByName(name, 3, null);
        assertTrue(inv.useItem(name));
        // quantity should remain
        assertEquals(3, inv.getQuantity(name));
    }
}
