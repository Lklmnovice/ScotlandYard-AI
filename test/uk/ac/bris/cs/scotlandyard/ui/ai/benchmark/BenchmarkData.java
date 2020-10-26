package uk.ac.bris.cs.scotlandyard.ui.ai.benchmark;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public final class BenchmarkData implements Serializable {
    private ImmutableList<Board.GameState> states;

    public BenchmarkData() {
        var builder = ImmutableList.<Board.GameState>builder();
        try {
            var graph = ScotlandYard.standardGraph();
            var setup = new GameSetup(graph, STANDARD24ROUNDS);
            Map<Integer, List<Integer>> map = new TreeMap<>();
            map.put(106, Arrays.asList(123, 94, 26, 50, 155));
            map.put(166, Arrays.asList(103, 53, 26, 91, 123));
            map.put(45, Arrays.asList(123, 138, 112, 53, 117));
            map.put(170, Arrays.asList(154, 100, 102, 39,105));

            map.put(149, Arrays.asList(94, 79, 93, 197, 173));
            map.put(155, Arrays.asList(13, 53, 79, 188, 183));
            map.put(154, Arrays.asList(112, 88, 8, 56, 186));
            map.put(159, Arrays.asList(118, 79, 34, 114, 3));
            map.put(8, Arrays.asList(185, 192, 29, 54, 131));
            map.put(153, Arrays.asList(126, 175, 25, 103, 79));
            map.put(165, Arrays.asList(138, 64, 9, 149, 77));
            map.put(59, Arrays.asList(2, 52, 176, 188, 118));
            map.put(88, Arrays.asList(60, 14, 82, 165, 35));
            map.put(29, Arrays.asList(131, 137, 19, 103, 89));


            map.forEach((mrX, detectives) -> builder.add(generateGameState(
                    setup, detectives, mrX
            )));
            states = builder.build();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Board.GameState generateGameState(GameSetup setup, List<Integer> detectiveLocations, int mrxLocation) {
        var itr = detectiveLocations.iterator();
        var detectives = DETECTIVES.stream()
                .map(d ->
                        new Player(d, defaultDetectiveTickets(), itr.next()))
                .collect(Collectors.toList());
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), mrxLocation);
        return MyGameStateFactory.a(setup, mrX, ImmutableList.copyOf(detectives));
    }
/*
    public void save() {
        try {
            FileOutputStream outFile = new FileOutputStream(savePath);
            var out = new ObjectOutputStream(outFile);
            out.writeObject(this);
            out.close();
            outFile.close();

            System.out.println("Serialization completed -- saved at " + savePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BenchmarkData retrieveData() {
        try {
            var inFile = new FileInputStream(savePath);
            var in = new ObjectInputStream(inFile);
            BenchmarkData data = (BenchmarkData) in.readObject();
            in.close();
            inFile.close();
            return data;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public ImmutableList<Board.GameState> getStates() {
        return states;
    }

/*    public static void main(String[] args) {
        var data = new BenchmarkData();
        data.save();
    }*/
}
