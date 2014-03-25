package ParticleSwarmTest;

import net.sourceforge.jswarm_pso.FitnessFunction;

enum Weight {
	LANDING_HEIGHT(0),
	ROWS_CLEARED(1),
	ROW_TRANSITIONS(2),
	COLUMN_TRANSITIONS(3),
	HOLES(4),
	CUMULATIVE_WELLS(5),
	HOLE_DEPTH(6),
	ROW_HOLES(7);
	
	public final int Value;
	
	private Weight(int value) {
		Value = value;
	}
}

public class EvaluationFunction extends FitnessFunction {

	@Override
	public double evaluate(double[] position) {
		// The position here is an array of all the weights.
		// We need to play the game from start to finish using
		// these weights, and return the number of rows cleared
		// (or some other metric based on that).
		Game currentGame = new Game();
		while (!currentGame.hasLost()) {
			int[][] moves = currentGame.legalMoves();
			
			int chosenMove = pickMove(currentGame, moves, position);
			currentGame.makeMove(chosenMove);
		}
		
		return currentGame.getRowsCleared();
	}

	private int pickMove(Game currentGame, int[][] moves, double[] weights) {
		for (int i = 0; i < moves.length; i++) {
			int[] currentMove = moves[i];
			int orientation = currentMove[0];
			int column = currentMove[1];
			
			StateTester clonedState = StateTester.fromGame(currentGame);
			clonedState.makeMove(orientation, column);
			
			int[][] fieldAfterMove = clonedState.getField();
			
			// We want to evaluate this move based on the 8 characteristics:
			// 1. Landing Height
			// 2. Rows Cleared
			// 3. Row Transitions
			// 4. Column Transitions
			// 5. Holes
			// 6. Cumulative Wells
			// 7. Hole Depth
			// 8. Row Holes
			double landingHeightWeight = weights[Weight.LANDING_HEIGHT.Value]; // Example of how to get weights.
		}
		return 0;
	}

}
