package com.model;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class HintTest {

    private Hint h;

    @Before
    public void setUp() {
        h = new Hint(7);
    }

    @Test
    public void testInitialState() {
        assertEquals(7, h.getIndex());
        assertEquals(0, h.getCount());
        assertTrue(h.getHints().isEmpty());
    }

    @Test
    public void testConstructWithList_andDefensiveCopy() {
        List<String> src = Arrays.asList("a", "b");
        Hint h2 = new Hint(3, src);
        assertEquals(2, h2.getCount());
        assertEquals("a", h2.getNextHint(0));
        // modify original source does not change Hint internals
        src.set(0, "Z");
        assertEquals("a", h2.getNextHint(0));
    }

    @Test
    public void testAddHint_trimmingAndIgnoreBlank() {
        h.addHint("  hi  ");
        h.addHint("");
        h.addHint("   ");
        h.addHint(null);
        h.addHint("there");
        assertEquals(2, h.getCount());
        assertEquals("hi", h.getNextHint(0));
        assertEquals("there", h.getNextHint(1));
    }

    @Test
    public void testGetNextHint_negativeAndOOB() {
        h.addHint("one");
        assertEquals("one", h.getNextHint(-10));
        assertNull(h.getNextHint(1));
    }

    @Test
    public void testGetHints_mutability_reflectsChanges() {
        List<String> list = h.getHints();
        list.add("x");
        assertEquals(1, h.getCount());
        assertEquals("x", h.getNextHint(0));
    }

    @Test
    public void testUnicodeExamples() {
        Hint hu = new Hint(99, Arrays.asList("mañana", "straße"));
        assertEquals("mañana", hu.getNextHint(0));
        assertEquals("straße", hu.getNextHint(1));
    }
}
