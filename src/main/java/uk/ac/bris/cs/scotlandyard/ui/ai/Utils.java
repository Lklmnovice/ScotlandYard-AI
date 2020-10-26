package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class that gives the best move based on MiniMax algorithm
 * MiniMax + AlphaBeta pruning + Iterative Deepening + Killer Heuristic
 * Evaluation(state) = min(distance(MrX, detective)) + sum(distance(MrX, detective)) / 100 + #MrXSecreteTickets / 10 + possibleLocationOfMrX / 100
 * Credit to @see https://dke.maastrichtuniversity.nl/m.winands/documents/TCAIG_ScotlandYard.pdf
 * and @see https://incoherency.co.uk/blog/stories/scotland-yard.html
 */
@SuppressWarnings("UnstableApiUsage")
public class Utils implements Iterator<Move> {
    private ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph;
    private List<Integer> revealRounds;
    private Move[][] killerMoves;

    public final int maxDepth;
    private int currDepth = 1;
    private Board rootState;

    private final int maxKillerMoveSlot = 2;
    private final int dangerThreshold = 10;
    private boolean enableKillerMoves = true;

    /**
     * Create a new Utility class with given board and max depth
     * Call {@link #hasNext} to check if we can improve the result
     * And only after that, call {@link #next} to get the best move
     * @param b board
     * @param maxDepth max depth we try to push
     */
    public Utils(Board b, int maxDepth) {
        this.rootState = b;
        this.maxDepth = maxDepth;
        this.graph = b.getSetup().graph;
        this.killerMoves = new Move[maxDepth + 1][];
        for (int i = 0; i < maxDepth + 1; i++) {
            killerMoves[i] = new Move[maxKillerMoveSlot];
        }
        var rounds = b.getSetup().rounds;
        this.revealRounds = IntStream
                .range(0, rounds.size())
                .filter(rounds::get)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Return the best move
     * Since the best move per ply is always added to the begging of the array,
     * the best move for root node can be found at killerMoves[0][0]
     * @return best move
     */
    public Move getBestMove() {
        return killerMoves[0][0];
    }

    /**
     * MiniMax enhanced with alpha-beta pruning and killer ordering
     *
     * @param state current state
     * @param alpha max so far
     * @param beta min so far
     * @param depth current depth
     * @return max value for Maximizer and min value for Minimizer
     */
    private double alphaBeta(Board state, double alpha, double beta, int depth) {
        if (depth == currDepth)
            return evaluate(state);
        if (!state.getWinner().isEmpty())
            return isGameEnded(state);

        if (isMaximizer(state)) {
            return executeMaximizer((Board.GameState) state, alpha, beta, depth);
        } else
            return executeMinimizer((Board.GameState) state, alpha, beta, depth);
    }

    /**
     * Check if its a Maximizer
     * @param board current board
     * @return t/f
     */
    private boolean isMaximizer(Board board) {
        return board.getAvailableMoves().asList().get(0).commencedBy().isMrX();
    }

    /**
     * Execute Maximizer's part
     * @param state current state
     * @param alpha max so far
     * @param beta min so far
     * @param depth current depth
     * @return max value
     */
    private double executeMaximizer(Board.GameState state, double alpha, double beta, int depth) {
        List<Move> moves = sortMoves(trimMoves(state, true), depth);

        Move bestMove = null;
        double val = Double.NEGATIVE_INFINITY;
        for (Move move : moves) {
            double temp = alphaBeta(state.advance(move), alpha, beta, depth + 1);
            if (temp >= val) {
                val = temp;
                bestMove = move;
            }
            alpha = Math.max(val, alpha);
            if (beta <= alpha)
                break;
        }
        updateKillerMoves(bestMove, depth);
        return alpha;
    }

    /**
     * Execute Minimizer's part
     * @param state current state
     * @param alpha max so far
     * @param beta min so far
     * @param depth current depth
     * @return min value
     */
    private double executeMinimizer(Board.GameState state, double alpha, double beta, int depth) {
        List<Move> moves = sortMoves(trimMoves(state, false), depth);

        Move bestMove = null;
        double val = Double.POSITIVE_INFINITY;
        for (Move move : moves) {
            double temp = alphaBeta(state.advance(move), alpha, beta, depth + 1);
            if (temp <= val) {
                val = temp;
                bestMove = move;
            }
            beta = Math.min(val, beta);

            if (beta <= alpha)
                break;
        }
        updateKillerMoves(bestMove, depth);
        return beta;
    }

    /**
     * Add killer moves to the moves set
     * @param moves moves
     * @param depth current depth
     * @return list of moves cause it's "ordered"
     */
    private List<Move> sortMoves(LinkedList<Move> moves, int depth) {
        if (!this.enableKillerMoves)
            return moves;
        //check if there're any killer moves
        boolean allNull = true;
        for (int i = 0; i < killerMoves[depth].length; i++) {
            if (killerMoves[depth][i] != null) {
                allNull = false;
                break;
            }
        }
        if (allNull)
            return new ArrayList<>(moves);
        //else get killer moves
        var res = new ArrayList<>(Arrays.asList(killerMoves[depth]));
        res.removeIf(Objects::isNull);
        res.retainAll(moves);   //remove killer moves that aren't present in possible moves
        moves.removeAll(res);   //remove duplicates
        res.addAll(moves);
        return res;
    }

    /**
     * Trim available moves
     * For detectives, return only set of moves commenced by one detective
     * For MrX, filter moves
     * @param state current state
     * @param isMaximizer is maximizer
     * @return moves
     */
    private LinkedList<Move> trimMoves(Board.GameState state, boolean isMaximizer) {
        LinkedList<Move> newList = new LinkedList<>();
        var list = state.getAvailableMoves().asList();
        Piece p = list.get(0).commencedBy();
        if (isMaximizer) {
            filterMoves(state, newList, list);
        } else {
            for (Move move : list) {
                if (move.commencedBy() != p)
                    break;
                newList.addLast(move);
            }
        }
        return newList;
    }

    /**
     * When to use black ticket and double ticket
     * For black ticket:
     * - Use it when the |L| is low, so basically right after reveal round
     * - do not use in following situations
     * + first two rounds
     * + during a round where he has to surface
     * + when all possible locations has only taxi edges
     * For double ticket:
     * - use it to escape
     * - when next single move is pretty risky
     * @param state current state
     * @param newList new list
     * @param list all available moves
     */
    private void filterMoves(Board.GameState state, LinkedList<Move> newList, List<Move> list) {
        boolean useBlack = checkIfUseBlackTicket(state);
        boolean useDouble = checkIfUseDoubleMove(state);
        for (Move move : list) {
            if (move instanceof Move.DoubleMove && useDouble) {
                Move.DoubleMove dm = (Move.DoubleMove) move;
                if (useBlack
                        && ((dm.ticket1 == ScotlandYard.Ticket.SECRET && dm.ticket2 != ScotlandYard.Ticket.SECRET)
                        || (dm.ticket1 != ScotlandYard.Ticket.SECRET && dm.ticket2 == ScotlandYard.Ticket.SECRET))) {
                    newList.addFirst(dm);
                } else if (!useBlack
                            && (dm.ticket1 != ScotlandYard.Ticket.SECRET && dm.ticket2 != ScotlandYard.Ticket.SECRET))
                    newList.addFirst(dm);
            } else if (move instanceof Move.SingleMove) {
                Move.SingleMove sm = ((Move.SingleMove) move);

                if (useBlack && (sm.ticket == ScotlandYard.Ticket.SECRET))
                    newList.addFirst(sm);
                else if (!useBlack && (sm.ticket != ScotlandYard.Ticket.SECRET))
                    newList.addLast(sm);
            }
        }
    }

    /**
     * Use it when the |L| is low, so basically right after reveal round
     * - do not use in following situations
     *    + first two rounds
     *    + during a round where he has to surface
     *    + when all possible locations has only taxi edges
     *
     * @param state current state
     * @return t/f
     */
    private boolean checkIfUseBlackTicket(Board.GameState state) {
        Move move = state.getAvailableMoves().asList().get(0);
        int mrXLocation = move.source();
        var pairs = graph.incidentEdges(mrXLocation);
        boolean onlyTaxiEdges = pairs.stream().noneMatch(
                        pair ->
                                graph.edgeValue(pair).get().stream().anyMatch(t -> t != ScotlandYard.Transport.TAXI));
        return calculatePossibleLocations(state) < dangerThreshold
                && !revealRounds.contains(state.getMrXTravelLog().size())
                && !onlyTaxiEdges;
    }

    /**
     * For double ticket:
     * - use it to escape
     * - when next single move is pretty risky
     * @param state current state
     * @return t/f
     */
    private boolean checkIfUseDoubleMove(Board.GameState state) {
        Move move = state.getAvailableMoves().asList().get(0);
        int mrXLocation = move.source();
        PathFinder<ImmutableSet<ScotlandYard.Transport>>
                finder = new PathFinder<>(state.getSetup().graph, mrXLocation);

        for (int pos : getDetectiveLocations(state)) {
            if (finder.calculateDistance(pos) < 3)
                return true;
        }
        return false;
    }

    /**
     * put the new best move at first location and the shift the rest to right
     * @param bestMove new best move
     * @param depth current depth
     * @see <a href="https://stackoverflow.com/a/17706147">Zong's anwser</a>
     */
    private void updateKillerMoves(Move bestMove, int depth) {
        if (killerMoves[depth][0] != null && killerMoves[depth][0].equals(bestMove))
            return;
        //TODO check duplicates
        System.arraycopy(killerMoves[depth], 0, killerMoves[depth], 1, maxKillerMoveSlot - 1);

        killerMoves[depth][0] = bestMove;
    }

    /**
     * Check if game is ended
     * @param board current board
     * @return t/f
     */
    private double isGameEnded(Board board) {
        var winners = board.getWinner();
        if (winners.contains(Piece.MrX.MRX))
            return 0;
        else
            return Double.POSITIVE_INFINITY;
    }

    /**
     * Evaluation(state) =
     *        min(distance(MrX, detective))
     *      + sum(distance(MrX, detective)) / 100
     *      + #MrXSecreteTickets / 10
     *      + possibleLocationOfMrX / 100
     * Credit to @see https://dke.maastrichtuniversity.nl/m.winands/documents/TCAIG_ScotlandYard.pdf [Section V-C]
     * and @see https://incoherency.co.uk/blog/stories/scotland-yard.html
     * @param state current state
     * @return value
     */
    private int evaluate(Board state) {
        var availableMoves = state.getAvailableMoves();
        if (!availableMoves.isEmpty()) {
            var move = availableMoves.iterator().next();
            PathFinder<ImmutableSet<ScotlandYard.Transport>> finder = new PathFinder<>(state.getSetup().graph, move.source());
            int sum = 0;
            int min = Integer.MAX_VALUE;
            for (Piece player : Piece.Detective.values()) {
                int val = finder.calculateDistance(
                        state.getDetectiveLocation( (Piece.Detective) player ).get() //detective's location
                );
                if (min >= val)
                    min = val;
                sum += val;
            }

            return min
                    + countMrXSecretTickets(state) /10
                    + sum / 100
                    + calculatePossibleLocations(state) / 100;
        }
        return 0;
    }

    /**
     * Count MrX's Secret tickets
     * @param state current state
     * @return count
     */
    private int countMrXSecretTickets(Board state) {
        return state.getPlayerTickets(Piece.MrX.MRX).get().getCount(ScotlandYard.Ticket.SECRET);
    }

    /**
     * Calculate possible locations when MrX can be
     * return 200 if not position revealed yet
     * @param state current state
     * @return count
     */
    private int calculatePossibleLocations(Board state) {
        Set<Integer> possibleLocations = new HashSet<>();
        var log = state.getMrXTravelLog();
        //get the closest reveal round
        int revealRound = -1;
        for (Integer round : revealRounds)
            if (round < log.size())
                revealRound = round;    //closest reveal round
        //if no position has been revealed yet
        if (revealRound == -1)
            return 200; //cuz there are 200 position in total
        //else add the revealed location
        possibleLocations.add(
                log.get(revealRound).location().get());

        //for each following round
        for (int i = revealRound + 1; i < log.size(); i++) {
            //get the transport used by MrX and update
            ScotlandYard.Transport usedTransport = getCorrespondingTransport(log.get(i).ticket());
            possibleLocations = updatePossibleLocations(possibleLocations, usedTransport);
        }
        possibleLocations.removeAll(getDetectiveLocations(state));
        return possibleLocations.size();
    }

    /**
     * Return set of detectives' locations [Assumed all detectives are playing]
     * @param state
     * @return set of detectives' locations
     */
    private Set<Integer> getDetectiveLocations(Board state) {
        return Arrays
                .stream(Piece.Detective.values())
                .map(d -> state.getDetectiveLocation(d).get())
                .collect(Collectors.toSet());
    }

    /**
     * expand the set of locations with the given transport
     * For each old location in the set, we add neighbours that can be reached with the given transport
     * @param oldLocations set of locations
     * @param usedTransport transport used by MrX, null if MrX's used a secret ticket
     * @return new locations
     */
    private Set<Integer> updatePossibleLocations(Set<Integer> oldLocations, @Nullable ScotlandYard.Transport usedTransport) {
        var temp = new HashSet<Integer>();
        //add every possible new location to temp
        oldLocations.forEach(from -> {
            var nodes = graph.adjacentNodes(from);
            if (usedTransport == null) //if its a secret ticket
                temp.addAll(nodes);
            else {
                for (Integer toNode : nodes) {
                    if (graph.edgeValue(from, toNode)
                            .get()
                            .stream()
                            .anyMatch(transport -> transport == usedTransport)) {
                        temp.add(toNode);
                    }
                }
            }
        });
        //update possible locations
        return temp;
    }

    /**
     * Map ticket to the corresponding transport
     * @param ticket t
     * @return transport
     */
    private ScotlandYard.Transport getCorrespondingTransport(ScotlandYard.Ticket ticket) {
        switch (ticket) {
            case TAXI:
                return ScotlandYard.Transport.TAXI;
            case BUS:
                return ScotlandYard.Transport.BUS;
            case UNDERGROUND:
                return ScotlandYard.Transport.UNDERGROUND;
            default:
                return null;
        }
    }

    /**
     * Check if result can be improved
     * @return t/f
     */
    @Override
    public boolean hasNext() {
        return currDepth != maxDepth + 1;
    }

    /**
     * get the new best move
     * @return move
     */
    @Override
    public Move next() {
        alphaBeta(rootState, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0);
        currDepth++;
        return getBestMove();
    }

    /**
     *
     * @return
     */
    public Utils benchmarking() {
        this.currDepth = maxDepth;
        return this;
    }

    /**
     * Disable killer moves
     * @return
     */
    public Utils disableKillerMoves() {
        this.enableKillerMoves  = false;
        return this;
    }

}
