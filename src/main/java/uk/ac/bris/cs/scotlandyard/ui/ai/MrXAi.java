package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MrXAi implements Ai {
	@Nonnull @Override public String name() { return "Pikachuuuuuuuuuuu!"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			@Nonnull AtomicBoolean terminate) {

		AtomicReference<Move> bestMove = new AtomicReference<>();
		var realTerminate = setTimer();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		var task = executor.submit(() -> {
			Utils u = new Utils(board, 20);
			while (u.hasNext()) {
				bestMove.set(u.next());
			}
		});

//		while (!terminate.get() && !task.isDone()); -- UI bugs
		while (!realTerminate.get() && !task.isDone());

		executor.shutdown();
		return bestMove.get();
	}

	private AtomicBoolean setTimer() {
		var realTerminate = new AtomicBoolean(false);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				realTerminate.set(true);
			}
		}, 14500);

		return realTerminate;
	}

}
