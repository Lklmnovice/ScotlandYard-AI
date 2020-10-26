package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.AbstractValueGraph;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.junit.jupiter.api.Test;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import static org.junit.jupiter.api.Assertions.*;

class PathFinderTest {
    @Test
    void testPathFinder1() {
        PathFinder<Integer> finder = new PathFinder<>(generateGraph(), 0);
        var rows = finder.getTable();

        assertEquals(rows[0].priority, 0);
        assertEquals(rows[1].priority, 4);
        assertEquals(rows[2].priority, 22);
        assertEquals(rows[3].priority, 9);
        assertEquals(rows[4].priority, 5);
        assertEquals(rows[5].priority, 13);
        assertEquals(rows[6].priority, 8);
        assertEquals(rows[7].priority, 14);
    }

    @Test
    void testPathFinder2() {
        PathFinder<Integer> finder = new PathFinder<>(generateGraph(), 5);
        var rows = finder.getTable();

        assertEquals(rows[0].priority, 13);
        assertEquals(rows[1].priority, 9);
        assertEquals(rows[2].priority, 9);
        assertEquals(rows[3].priority, 10);
        assertEquals(rows[4].priority, 18);
        assertEquals(rows[5].priority, 0);
        assertEquals(rows[6].priority, 21);
        assertEquals(rows[7].priority, 1);
    }


    @Test
    void testPathFinder3() {
        PathFinder<Integer> finder = new PathFinder<>(generateGraphComplex(), 9);
        var rows = finder.getTable();

        assertEquals(rows[0].priority, 9);
        assertEquals(rows[1].priority, 8);
        assertEquals(rows[2].priority, 7);
        assertEquals(rows[3].priority, 10);
        assertEquals(rows[4].priority, 9);
        assertEquals(rows[5].priority, 1);
        assertEquals(rows[6].priority, 5);
        assertEquals(rows[7].priority, 12);
        assertEquals(rows[8].priority, 5);
        assertEquals(rows[9].priority, 0);
        assertEquals(rows[10].priority, 4);
        assertEquals(rows[11].priority, 19);
        assertEquals(rows[12].priority, 2);
        assertEquals(rows[13].priority, 9);
        assertEquals(rows[14].priority, 16);
        assertEquals(rows[15].priority, 15);
        assertEquals(rows[16].priority, 8);
        assertEquals(rows[17].priority, 8);
    }

    ImmutableValueGraph<Integer, Integer> generateGraph() {
        var builder = ValueGraphBuilder
                .undirected()
                .<java.lang.Integer, java.lang.Integer>immutable();

        builder.putEdgeValue(0,1,4);
        builder.putEdgeValue(0,4,5);
        builder.putEdgeValue(1,3,5);
        builder.putEdgeValue(1,5,9);
        builder.putEdgeValue(5,2,9);
        builder.putEdgeValue(5,7,1);
        builder.putEdgeValue(3,7,9);
        builder.putEdgeValue(4,6,3);

        return builder.build();
    }

    ImmutableValueGraph<Integer, Integer> generateGraphComplex() {
        var builder = ValueGraphBuilder
                .undirected()
                .<java.lang.Integer, java.lang.Integer>immutable();

        builder.putEdgeValue(0,2,2);
        builder.putEdgeValue(0,14,7);

        builder.putEdgeValue(0,4,5);
        builder.putEdgeValue(1,5,7);
        builder.putEdgeValue(2,5,8);
        builder.putEdgeValue(2,6,2);

        builder.putEdgeValue(4,5,8);
        builder.putEdgeValue(5,6,9);

        builder.putEdgeValue(4,7,3);
        builder.putEdgeValue(5,8,4);
        builder.putEdgeValue(5,9,1);
        builder.putEdgeValue(6,9,9);
        builder.putEdgeValue(6,10,1);

        builder.putEdgeValue(9,10,4);

        builder.putEdgeValue(7,11,7);
        builder.putEdgeValue(8,12,5);
        builder.putEdgeValue(9,12,2);
        builder.putEdgeValue(9,13,9);
        builder.putEdgeValue(10,13,9);

        builder.putEdgeValue(11,14,6);
        builder.putEdgeValue(11,15,8);
        builder.putEdgeValue(12,16,6);
        builder.putEdgeValue(13,16,6);
        builder.putEdgeValue(13,17,7);

        builder.putEdgeValue(15,16,7);
        builder.putEdgeValue(16,17,6);
        builder.putEdgeValue(15,17,8);

        builder.putEdgeValue(3,10,7);
        builder.putEdgeValue(3,17,2);
        builder.putEdgeValue(10,17,4);

        return builder.build();
    }
}