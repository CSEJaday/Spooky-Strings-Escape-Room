package com.model;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class HintList {
    private HashMap<Integer, Queue<Hint>>;
    private HintList hintList;

    private HintList()
    {
        return;
    }

    public HintList getInstance()
    {
        return null;
    }

    public void addHint(Hint hint)
    {
        hints.computeIfAbsent(hint.level, k -> new LinkedList<>()).add(hint);
    }
    

    public Hint getNextHint(int level)
    {
        return null;
    }
}

