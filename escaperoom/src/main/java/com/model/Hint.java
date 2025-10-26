package com.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents hints for a single puzzle (global puzzle index).
 * Stores an ordered list of hint strings.
 */
public class Hint {
    private final int index;          // global puzzle index (1-based)
    private final List<String> hints; // ordered hints

    public Hint(int index) {
        this.index = index;
        this.hints = new ArrayList<>();
    }

    public Hint(int index, List<String> hints) {
        this.index = index;
        this.hints = new ArrayList<>();
        if (hints != null) this.hints.addAll(hints);
    }

    public int getIndex() {
        return index;
    }

    public List<String> getHints() {
        return hints;
    }

    public int getCount() {
        return hints.size();
    }

    /**
     * Return the next hint given how many have already been used.
     * If none available returns null.
     *
     * @param alreadyUsed number of hints the player has already used for this puzzle
     * @return next hint String or null if none remain
     */
    public String getNextHint(int alreadyUsed) {
        if (alreadyUsed < 0) alreadyUsed = 0;
        if (alreadyUsed >= hints.size()) return null;
        return hints.get(alreadyUsed);
    }

    /**
     * Add a hint to the list (appends).
     */
    public void addHint(String hint) {
        if (hint != null && !hint.isBlank()) hints.add(hint.trim());
    }
}

