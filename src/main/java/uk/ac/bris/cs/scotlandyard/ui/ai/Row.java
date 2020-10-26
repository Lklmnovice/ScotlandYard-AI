package uk.ac.bris.cs.scotlandyard.ui.ai;

public class Row {
    public final int node;
    public final int priority;
    public final int nearestNode;

    public Row(int node, int priority, int nearestNode) {
        this.node = node;
        this.priority = priority;
        this.nearestNode = nearestNode;
    }

    @Override
    public boolean equals(Object obj) {
        return node == ((Row)obj).node;
    }
}