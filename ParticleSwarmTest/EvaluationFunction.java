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
		// 5. Holes
		// 6. Cumulative Wells

		double weightedScore = 0;

		// 2. Rows Cleared
		double rowsClearedScore = calculateRowsClearedScore(weights,
				stateAfterMove);
		weightedScore += rowsClearedScore;
		
		// 3. Row Transitions
		double rowTransitionScore = calculateRowTransitionScore(weights,
				stateAfterMove);
		weightedScore += rowTransitionScore;
		
		//4. Column Transitions
		double columnTransitionScore = calculateColumnTransitionScore(weights,
				stateAfterMove);
		weightedScore += columnTransitionScore;

		// 7. Hole Depth
		double holeDepthScore = calculateHoleDepthScore(weights, stateAfterMove);
		weightedScore += holeDepthScore;

		// 8. Row Holes
		double rowsHolesScore = calculateRowsHolesScore(weights, stateAfterMove);
		weightedScore += rowsHolesScore;
		return weightedScore;
	}
	
	private double calculateColumnTransitionScore(double[] weights,
			StateTester clonedState) {
		double columnTransitionWeight = weights[Weight.COLUMN_TRANSITIONS.Value];
		
		int [][] field = clonedState.getField();
		int totalColumnTransitions = countColumnTransitions(field);
		
		double score = (double) totalColumnTransitions * columnTransitionWeight;
		
		return score;
	}
	
	private int countColumnTransitions(int[][] field){
		
		int columnTransitionCount = 0;
		final int amountOfColumns = field[0].length;
		
		for (int col=0; col < amountOfColumns; col++){
			columnTransitionCount += getTransitionCountForColumn(col, field);
		}
	
		return columnTransitionCount;
	}
	
	private int getTransitionCountForColumn(int col, int[][] field){
		
		int transitionCount = 0;
		final int amountOfRows = field.length;
		
		//Bottom implicitly filled
		if (field[0][col] == 0){
			transitionCount++;
		}
		
		for(int row=0; row < amountOfRows-1; row++){
			if ((field[row][col]==0 && field[row+1][col]!=0)||
					(field[row][col]!=0 && field[row+1][col]==0)){
				transitionCount++;
			}
		}
		
		//Top implicitly empty
		if (field[amountOfRows-1][col] != 0){
			transitionCount++;
		}
		
		return transitionCount;
	}
	
	private double calculateRowTransitionScore(double[] weights,
			StateTester clonedState) {
		double rowTransitionWeight = weights[Weight.ROW_TRANSITIONS.Value];
		
		int[][] field = clonedState.getField();
		int totalRowTransitions = countRowTransitions(field);
		
		double score = (double) totalRowTransitions * rowTransitionWeight;
		
		return score;
	}
	
	private int countRowTransitions(int[][] field){
		
		int rowTransitionCount = 0;
		final int amountOfRows = field.length;
		
		for(int row=0; row < amountOfRows; row++){
			rowTransitionCount += getTransitionCountForRow(row, field);
		}
		
		return rowTransitionCount;
	}
	
	private int getTransitionCountForRow(int row, int[][] field){
		
		int transitionCount = 0;
		final int amountOfColumns = field[0].length;
		
		//Left edge implicitly filled
		if(field[row][0]==0){
			transitionCount++;
		}
		
		for(int col=0; col < amountOfColumns-1; col++){
			if ((field[row][col]==0 && field[row][col+1]!=0)||
					(field[row][col]!=0 && field[row][col+1]==0)){
				transitionCount++;
			}
		}
		
		//Right edge implicitly filled
		if(field[row][amountOfColumns-1]==0){
			transitionCount++;
		}
		
		return transitionCount;
		
	}

	private double calculateHoleDepthScore(double[] weights,
			StateTester clonedState) {
		double holeDepthWeight = weights[Weight.HOLE_DEPTH.Value];

		int[][] field = clonedState.getField();
		int totalHoleDepth = countHoleDepth(field);

		double score = (double) totalHoleDepth * holeDepthWeight;
		return score;
	}

	private int countHoleDepth(int[][] field) {
		int totalHoleDepth = 0;

		final int amountOfColumns = field[0].length;
		final int amountOfRows = field.length;

		for (int col = 0; col < amountOfColumns; col++) {
			boolean hasHoleBefore = field[0][col] == 0;
			int row = 1;
			while (row < amountOfRows) {
				int currentCell = field[row][col];

				boolean isCellFilled = currentCell != 0;
				if (isCellFilled && hasHoleBefore) {
					// This cell is on top of a hole.
					totalHoleDepth++;
				} else if (!isCellFilled) {
					// This cell is a hole.
					hasHoleBefore = true;
				}
				// Otherwise, the cell is filled and is
				// not on top of a hole. Disregard it.
				
				row++;
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
		int[] columnHeights = new int[rows];

		//get height of columns
		//0: empty column
		//1: index 0 is the highest filled cell in the column
		int row;
		for (int column = 0; column < columns; column++){
			row = rows - 1; //fit indices
			while (row >= 0 && field[row][column] == 0){
				row--;
			}
			columnHeights[column] = row + 1;
		}

		//count rows containing holes
		for (int i = 0; i < rows; i++){
			for (int cell : field[i]){
				if (field[i][cell] == 0 && columnHeights[cell] > i){
					rowsWithHoles++;
					break;
				}
			}
		}

		return rowsWithHoles;
	}
}