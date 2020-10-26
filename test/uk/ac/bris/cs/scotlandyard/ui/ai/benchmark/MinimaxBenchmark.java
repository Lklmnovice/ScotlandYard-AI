package uk.ac.bris.cs.scotlandyard.ui.ai.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.MrXAi;
import uk.ac.bris.cs.scotlandyard.ui.ai.Utils;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 0)
@Measurement(iterations = 2)
public class MinimaxBenchmark {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MinimaxBenchmark.class.getSimpleName())
                .result("benchmarkingResult")
                .resultFormat(ResultFormatType.CSV)
                .build();
        Runner r = new Runner(opt);
        r.run();
    }


    @Benchmark
    @Timeout(time = 15, timeUnit = TimeUnit.SECONDS)
    public Move benchmark(BenchmarkState state) {
        return new Utils(state.getState(state.index), state.depth).benchmarking().next();
    }
//    @Benchmark
//    @Timeout(time = 15, timeUnit = TimeUnit.SECONDS)
//    public Move benchmarkWithoutKillerMoves(BenchmarkState state) {
//        return new Utils(state.getState(state.index), 8).benchmarking().disableKillerMoves().next();
//    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private MrXAi myAi;

        @Param({"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"})
        private int index;
        private BenchmarkData data;

        @Param({"8", "9", "10"})
        private int depth;


        @Setup(Level.Trial)
        public void setup() {
            myAi = new MrXAi();
            data = new BenchmarkData();
        }

        public Board.GameState getState(int index) {
            return data.getStates().get(index);
        }
    }
}

//        BenchmarkData data = new BenchmarkData();
//        long startTime = System.currentTimeMillis();
//        int nState = 0;
//        int depth = 8;
//        Move move = new Utils(data.getStates().get(nState), depth).getBestMove();
//        long endTime = System.currentTimeMillis();
//        System.out.println("cost " + (endTime - startTime)/1000);
//
//        Move move1 = MrXAi.MiniMax(data.getStates().get(nState), depth);
//        if (move.equals(move1)) {
//            System.out.println("same");
//        }
//
//        System.out.println("km: " + move.tickets() + " destination: " + ((Move.SingleMove) move).destination);
//        System.out.println("no: " + move1.tickets() + " destination: " + ((Move.SingleMove) move1).destination);
