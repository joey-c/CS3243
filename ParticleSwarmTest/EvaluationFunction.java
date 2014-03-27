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

			StateTester stateBeforeMove = StateTester.fromGame(currentGame);
			StateTester stateAfterMove = StateTester.fromGame(currentGame);
			stateAfterMove.makeMove(orientation, column);

			double weightedScore = calculateWeightedScore(weights,
					stateBeforeMove, stateAfterMove);

			// If this move scores better than the previous ones,
			// set it as our choice.
			if (weightedScore > currentHighScore) {
				currentHighScore = weightedScore;
				moveChoice = i;
			}
		}

		return moveChoice;
	}

	private double calculateWeightedScore(double[] weights,
			StateTester stateBeforeMove, StateTester stateAfterMove) {
		// 1. Landing Height
		// 3. Row Transitions
		// 4. Column Transitions
		// 5. Holes
		// 6. Cumulative Wells

		double weightedScore = 0;

		// 2. Rows Cleared
		double rowsClearedScore = calculateRowsClearedScore(weights,
				stateAfterMove);
		weightedScore += rowsClearedScore;

		// 7. Hole Depth
		double holeDepthScore = calculateHoleDepthScore(weights, stateAfterMove);
		weightedScore += holeDepthScore;

		// 8. Row Holes
		double rowsHolesScore = calculateRowsHolesScore(weights, stateAfterMove);
		weightedScore += rowsHolesScore;
		return weightedScore;
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

	private double calculateRowsHolesScore(double[] weights,
			StateTester clonedState) {
		double rowsHolesWeight = weights[Weight.ROW_HOLES.Value];

		int[][] field = clonedState.getField();
		int rowsWithHoles = countRowsHoles(field);

		double score = (double) rowsWithHoles * rowsHolesWeight;
		return score;
	}

	// A cell is a hole, or part of, if it is empty and
	// its row is lower than its column's height
	private int countRowsHoles(int[][] field){
		int rowsWithHoles = 0;
		int rows = field.length;
		int columns = field[0].length;
		int[] columnHeights = int[rows];

		//get height of columns
		//0: empty column
		//1: index 0 is the highest filled cell in the column
		int row;
		for (int column = 0; column < columns; i++){
			row = rows - 1; //fit indices
			while (row >= 0 && field[row][column] == 0){
				row--;
			}
			columnHeights[column] = row + 1;
		}

		//count rows containing holes
		for (int[] row : field){
			for (int cell : row){
				if (row[cell] == 0 && columnHeights[cell] > row){
					rowsWithHoles++;
					break;
				}
			}
		}

		return rowsWithHoles;
	}
}