package com.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for ordered hints belonging to a particular global puzzle index.
 *
 * This class holds an integer {@code index} (the puzzle id) and an ordered
 * {@code List<String>} of hints. Hints should be added in increasing specificity
 * (e.g., easiest -> most direct).
 */
public class Hint {
    private final int index;          // global puzzle index (1-based)
    private final List<String> hints; // ordered hints

    /**
     * Create a Hint container for the given puzzle index.
     *
     * @param index global puzzle index (1-based).
     */
    public Hint(int index) {
        this.index = index;
        this.hints = new ArrayList<>();
    }

    /**
     * Create a Hint container and seed with an initial hint list.
     *
     * @param index global puzzle index.
     * @param hints list of hint strings to copy; may be null.
     */
    public Hint(int index, List<String> hints) {
        this.index = index;
        this.hints = new ArrayList<>();
        if (hints != null) this.hints.addAll(hints);
    }

    /**
     * Get the puzzle index this hint set belongs to.
     *
     * @return puzzle index (1-based).
     */
    public int getIndex() {
        return index;
    }

    /**
     * Access the underlying ordered list of hints.
     *
     * @return modifiable list of hints (may be empty).
     */
    public List<String> getHints() {
        return hints;
    }

    /**
     * Return how many hints are stored for this puzzle.
     *
     * @return count of available hints.
     */
    public int getCount() {
        return hints.size();
    }

    /**
     * Return the next hint based on number of hints already used.
     *
     * If {@code alreadyUsed} is negative it is treated as 0. If there are no
     * hints remaining, this returns {@code null}.
     *
     * @param alreadyUsed number of hints the player has already used for this puzzle.
     * @return next hint string, or {@code null} when no more hints remain.
     */
    public String getNextHint(int alreadyUsed) {
        if (alreadyUsed < 0) alreadyUsed = 0;
        if (alreadyUsed >= hints.size()) return null;
        return hints.get(alreadyUsed);
    }

    /**
     * Append a new hint string to the stored list.
     *
     * @param hint hint text to add; ignored if null or blank.
     */
    public void addHint(String hint) {
        if (hint != null && !hint.isBlank()) hints.add(hint.trim());
    }
}

