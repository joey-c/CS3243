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
		int moveChoice = 0;
		double currentHighScore = 0;

		for (int i = 0; i < moves.length; i++) {
			int[] currentMove = moves[i];
			int orientation = currentMove[0];
			int column = currentMove[1];
			
			StateTester clonedState = StateTester.fromGame(currentGame);
			clonedState.makeMove(orientation, column);
			
			// We want to evaluate this move based on the 8 characteristics:
			// 1. Landing Height
			// 3. Row Transitions
			// 4. Column Transitions
			// 5. Holes
			// 6. Cumulative Wells
			// 7. Hole Depth
			// 8. Row Holes
			
			double weightedScore = 0; // This should contain the sum of all the characteristics.
			
			// 2. Rows Cleared
			double rowsClearedScore = calculateRowsClearedScore(weights,
					clonedState);
			weightedScore += rowsClearedScore;
			
			// 7. Hole Depth
			double holeDepthScore = calculateHoleDepthScore(weights,
					clonedState);
			weightedScore += holeDepthScore;
			
			// If this move scores better than the previous ones,
			// set it as our choice.
			if (weightedScore > currentHighScore) {
				currentHighScore = weightedScore;
				moveChoice = i;
			}
		}

		return moveChoice;
	}

	private double calculateHoleDepthScore(double[] weights,
			StateTester clonedState) {
		double holeDepthWeight = weights[Weight.HOLE_DEPTH.Value];
		
		int[][] field = clonedState.getField();
		
		int totalHoleDepth = 0;

		double score = (double)totalHoleDepth * holeDepthWeight;
		return score;
	}

	private double calculateRowsClearedScore(double[] weights,
			StateTester clonedState) {
		double rowsClearedWeight = weights[Weight.ROWS_CLEARED.Value];
		double rowsClearedScore = clonedState.rowsClearedAfterMove * rowsClearedWeight;
		return rowsClearedScore;
	}

}
