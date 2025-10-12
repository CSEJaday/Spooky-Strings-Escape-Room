package com.model;
import java.util.HashMap;
import java.util.Queue;

public class HintList {
    private HashMap<Integer, Queue<Hint>> hintsMap;
    private static HintList instance;

    private HintList()
    {
        hintsMap = new HashMap<>();
    }

    public static HintList getInstance()
    {
        if (instance == null)
        {
            instance = new HintList();
        }
        return instance;
    }

    public void addHint(Hint hint)
    {
        int level = hint.getLevel(); // ensure hint has getLevel method
        hintsMap.putIfAbsent(level, new LinkedList<>());
        hintsMap.get(level).offer(hint);
    }

    public Hint getNextHint(int level)
    {
        Queue<Hint> queue = hintsMap.get(level);
        if (queue != null && !queue.isEmpty())
        {
            return queue.poll();
        }
        return null;
    }

}
