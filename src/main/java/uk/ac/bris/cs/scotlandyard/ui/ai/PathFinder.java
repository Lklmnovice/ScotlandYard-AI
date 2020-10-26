package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableValueGraph;

import java.util.HashSet;
import java.util.PriorityQueue;

public class PathFinder<V> {

    private int source;
    private PriorityQueue<Row> queue;
    private Row[] references;
    private ImmutableValueGraph<Integer, V> graph;

    public PathFinder(
            ImmutableValueGraph<Integer, V> graph,
            int source) {
        this.graph = graph;
        this.source = source;
        computeDijkstra();
    }

    /**
     * Compute Dijkstra algorithm to find the shortest path
     */
    private void computeDijkstra() {
        prepareTable();
        var visited = new HashSet<Row>();
        while (!queue.isEmpty()) {
            var smallest = queue.poll();
            visited.add(smallest);

            for (EndpointPair<Integer> incidentEdge : graph.incidentEdges(smallest.node)) {
                V edgeValue = graph.edgeValueOrDefault(incidentEdge, null);
                int newPriority = smallest.priority + 1;
                if (edgeValue instanceof Integer) {
                    newPriority = smallest.priority + (Integer) edgeValue;
                }

                var adjacentNode = incidentEdge.nodeU();    //u -- the adjacentNode while v -- the source node
                var newRow = new Row(adjacentNode, newPriority, smallest.node);
                if (!visited.contains(newRow) && (references[adjacentNode].priority > newPriority)) {
                    queue.remove(references[adjacentNode]);
                    queue.add(newRow);
                    references[adjacentNode] = newRow;
                } // end if
            }
        }
    }

    private void prepareTable() {
        this.queue = new PriorityQueue<Row>(200,
                (e1, e2) -> e1.priority - e2.priority);
        this.references = new Row[200]; //there're in total 199 nodes

//        initialization
        for (Integer node : graph.nodes()) {
            Row row;
            if (node == source) {
                row = new Row(source, 0, -1);
            } else {
                row = new Row(node, Integer.MAX_VALUE, -1);
            }
            queue.add(row);
            references[node] = row;
        }

    }

    public Row[] getTable(){
        return this.references;
    }

    public int calculateDistance(int destination) {
        return this.references[destination].priority;
    }

}
