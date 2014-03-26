package ParticleSwarmTest;

import net.sourceforge.jswarm_pso.FitnessFunction;

enum Weight {
	LANDING_HEIGHT(0), ROWS_CLEARED(1), ROW_TRANSITIONS(2), COLUMN_TRANSITIONS(
			3), HOLES(4), CUMULATIVE_WELLS(5), HOLE_DEPTH(6), ROW_HOLES(7);

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

		int rowsCleared = currentGame.getRowsCleared();
		if (rowsCleared > 0) {
			System.out.println("Rows cleared: " + rowsCleared);
		}

		return rowsCleared;
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

			// 1. Landing Height
			// 3. Row Transitions
			// 4. Column Transitions
			// 5. Holes
			// 6. Cumulative Wells
			// 8. Row Holes

			double weightedScore = 0; 

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
		int totalHoleDepth = countHoleDepth(field);

		double score = (double) totalHoleDepth * holeDepthWeight;
		return score;
	}

	// TODO: look into better algorithms for this.
	private int countHoleDepth(int[][] field) {
		int totalHoleDepth = 0;
		for (int row = 0; row < field.length; row++) {
			int[] currRow = field[row];

			for (int col = 0; col < currRow.length; col++) {
				boolean isCellHole = field[row][col] == 0;
				if (isCellHole) {
					for (int i = row + 1; i < field.length; i++) {
						boolean isCurrCellFilled = field[i][col] != 0;
						if (!isCurrCellFilled) {
							break;
						}
						totalHoleDepth++;
					}
				}
			}
		}
		return totalHoleDepth;
	}

	private double calculateRowsClearedScore(double[] weights,
			StateTester clonedState) {
		double rowsClearedWeight = weights[Weight.ROWS_CLEARED.Value];
		double rowsClearedScore = clonedState.rowsClearedAfterMove
				* rowsClearedWeight;
		return rowsClearedScore;
	}

}
