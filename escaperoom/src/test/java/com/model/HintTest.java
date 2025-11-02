package com.model;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class HintTest {

    private Hint hint;

    @Before
    public void setUp() {
        hint = new Hint(1);
    }

    @Test
    public void testConstructor_emptyList() {
        assertEquals(1, hint.getIndex());
        assertTrue(hint.getHints().isEmpty());
        assertEquals(0, hint.getCount());
    }

    @Test
    public void testConstructor_withListCopiesValues() {
        List<String> list = Arrays.asList("first", "second");
        Hint h2 = new Hint(2, list);

        // copied contents, not reference
        assertEquals(2, h2.getIndex());
        assertEquals(2, h2.getCount());
        assertEquals("first", h2.getHints().get(0));
        assertEquals("second", h2.getHints().get(1));
        assertFalse(h2.getHints() == list); // ensure it made a defensive copy
    }

    @Test
    public void testConstructor_withNullList_createsEmpty() {
        Hint h = new Hint(3, null);
        assertTrue(h.getHints().isEmpty());
        assertEquals(0, h.getCount());
    }

    @Test
    public void testAddHint_validAndIgnoredCases() {
        hint.addHint(" one ");
        hint.addHint("two");
        hint.addHint(""); // ignored blank
        hint.addHint("   "); // ignored spaces
        hint.addHint(null); // ignored null

        assertEquals(2, hint.getCount());
        assertEquals(Arrays.asList("one", "two"), hint.getHints());
    }

    @Test
    public void testGetNextHint_basicSequence() {
        hint.addHint("first");
        hint.addHint("second");

        // no hints used
        assertEquals("first", hint.getNextHint(0));
        // one hint used
        assertEquals("second", hint.getNextHint(1));
        // too many used -> null
        assertEquals(null, hint.getNextHint(2));
    }

    @Test
    public void testGetNextHint_handlesNegative() {
        hint.addHint("first");
        assertEquals("first", hint.getNextHint(-5));
    }

    @Test
    public void testGetNextHint_onEmptyList_returnsNull() {
        assertEquals(null, hint.getNextHint(0));
        assertEquals(null, hint.getNextHint(-1));
    }

    @Test
    public void testHintsList_modifiableFromGetter() {
        hint.getHints().add("direct");
        assertEquals(1, hint.getCount());
        assertEquals("direct", hint.getNextHint(0));
    }

    @Test
    public void testMultipleHints_orderPreserved() {
        List<String> list = Arrays.asList("A", "B", "C");
        Hint h = new Hint(9, list);

        assertEquals("A", h.getNextHint(0));
        assertEquals("B", h.getNextHint(1));
        assertEquals("C", h.getNextHint(2));
        assertEquals(null, h.getNextHint(3));
    }
}

