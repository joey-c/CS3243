
enum Weight {
	LANDING_HEIGHT(0), ROWS_CLEARED(1), ROW_TRANSITIONS(2), COLUMN_TRANSITIONS(
			3), HOLES(4), CUMULATIVE_WELLS(5), HOLE_DEPTH(6), ROW_HOLES(7);

	public final int Value;

	private Weight(int value) {
		Value = value;
	}
}

class StateTester {
	int nextPiece;
	int[][] field;
	int[] top;
	int turn;

	static int ROWS = State.ROWS;
	static int COLS = State.COLS;

	public int rowsClearedAfterMove = 0;

	// width of the pieces [piece ID][orientation]
	protected static int[][] pWidth = { { 2 }, { 1, 4 }, { 2, 3, 2, 3 },
			{ 2, 3, 2, 3 }, { 2, 3, 2, 3 }, { 3, 2 }, { 3, 2 } };
	// height of the pieces [piece ID][orientation]
	private static int[][] pHeight = { { 2 }, { 4, 1 }, { 3, 2, 3, 2 },
			{ 3, 2, 3, 2 }, { 3, 2, 3, 2 }, { 2, 3 }, { 2, 3 } };
	private static int[][][] pBottom = { { { 0, 0 } },
			{ { 0 }, { 0, 0, 0, 0 } },
			{ { 0, 0 }, { 0, 1, 1 }, { 2, 0 }, { 0, 0, 0 } },
			{ { 0, 0 }, { 0, 0, 0 }, { 0, 2 }, { 1, 1, 0 } },
			{ { 0, 1 }, { 1, 0, 1 }, { 1, 0 }, { 0, 0, 0 } },
			{ { 0, 0, 1 }, { 1, 0 } }, { { 1, 0, 0 }, { 0, 1 } } };
	private static int[][][] pTop = { { { 2, 2 } }, { { 4 }, { 1, 1, 1, 1 } },
			{ { 3, 1 }, { 2, 2, 2 }, { 3, 3 }, { 1, 1, 2 } },
			{ { 1, 3 }, { 2, 1, 1 }, { 3, 3 }, { 2, 2, 2 } },
			{ { 3, 2 }, { 2, 2, 2 }, { 2, 3 }, { 1, 2, 1 } },
			{ { 1, 2, 2 }, { 3, 2 } }, { { 2, 2, 1 }, { 2, 3 } } };

	private StateTester(State s) {
		this.nextPiece = s.getNextPiece();

		int[][] existingField = s.getField();
		this.field = new int[existingField.length][existingField[0].length];
		for (int row = 0; row < existingField.length; row++) {
			System.arraycopy(existingField[row], 0, this.field[row], 0, existingField[row].length);
		}

		int[] existingTop = s.getTop();
		this.top = new int[existingTop.length];
		System.arraycopy(existingTop, 0, this.top, 0, existingTop.length);

		turn = s.getTurnNumber() + 1;
	}

	public static StateTester fromState(State s) {
		return new StateTester(s);
	}

	public boolean makeMove(int orient, int slot) {
		rowsClearedAfterMove = 0;
		// height if the first column makes contact
		int height = top[slot] - pBottom[nextPiece][orient][0];
		// for each column beyond the first in the piece
		for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
			height = Math.max(height, top[slot + c]
					- pBottom[nextPiece][orient][c]);
		}

		// check if game ended
		if (height + pHeight[nextPiece][orient] >= ROWS) {
			return false;
		}

		// for each column in the piece - fill in the appropriate blocks
		for (int i = 0; i < pWidth[nextPiece][orient]; i++) {
			// from bottom to top of brick
			for (int h = height + pBottom[nextPiece][orient][i]; h < height
					+ pTop[nextPiece][orient][i]; h++) {
				field[h][i + slot] = turn;
			}
		}

		// adjust top
		for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot + c] = height + pTop[nextPiece][orient][c];
		}

		// check for full rows - starting at the top
		for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
			// check all columns in the row
			boolean full = true;
			for (int c = 0; c < COLS; c++) {
				if (field[r][c] == 0) {
					full = false;
					break;
				}
			}
			// if the row was full - remove it and slide above stuff down
			if (full) {
				rowsClearedAfterMove++;
				// for each column
				for (int c = 0; c < COLS; c++) {
					// slide down all bricks
					for (int i = r; i < top[c]; i++) {
						field[i][c] = field[i + 1][c];
					}

					// lower the top
					top[c]--;
					while (top[c] >= 1 && field[top[c] - 1][c] == 0)
						top[c]--;
				}
			}
		}

		return true;
	}

	public int[][] getField() {
		return field;
	}
}

public class PlayerSkeleton {

	// implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		// legalMoves: array of two-element arrays
		// First element: orientation
		// Second element: column
		
		final double[] weights = {-0.9688760448682666, 1.6745776494980364, -0.6416325275407198, 9.507886401694847, -20.0, -0.9250600624530636, 0.16776394160879354, -4.727027063488643};

		int moveChoice = 0;
		double currentHighScore = Double.NEGATIVE_INFINITY;	

		for (int i = 0; i < legalMoves.length; i++) {
			int[] currentMove = legalMoves[i];
			int orientation = currentMove[0];
			int column = currentMove[1];

			StateTester stateBeforeMove = StateTester.fromState(s);
			StateTester stateAfterMove = StateTester.fromState(s);
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
		double weightedScore = 0;
		
		// 1. Landing Height
        double landingHeightScore = calculateLandingHeightScore(weights,
                stateBeforeMove, stateAfterMove);
        weightedScore += landingHeightScore;

		// 2. Rows Cleared
		double rowsClearedScore = calculateRowsClearedScore(weights,
				stateAfterMove);
		weightedScore += rowsClearedScore;

		// 3. Row Transitions
		double rowTransitionScore = calculateRowTransitionScore(weights,
				stateAfterMove);
		weightedScore += rowTransitionScore;

		// 4. Column Transitions
		double columnTransitionScore = calculateColumnTransitionScore(weights,
				stateAfterMove);
		weightedScore += columnTransitionScore;

		// 5. Holes
		double buriedHolesScore = calculateBuriedHolesScore(weights,
				stateAfterMove);
		weightedScore += buriedHolesScore;

		// 6. Cumulative Wells
		double cumulativeWellScore = calculateCumulativeWellScore(weights,
				stateAfterMove);
		weightedScore += cumulativeWellScore;

		// 7. Hole Depth
		double holeDepthScore = calculateHoleDepthScore(weights, stateAfterMove);
		weightedScore += holeDepthScore;

		// 8. Row Holes
		double rowsHolesScore = calculateRowsHolesScore(weights, stateAfterMove);
		weightedScore += rowsHolesScore;

		return weightedScore;
	}
	private double calculateCumulativeWellScore(double[] weights,
			StateTester clonedState) {
		double cumulativeWellsWeight = weights[Weight.CUMULATIVE_WELLS.Value];

		int[][] field = clonedState.getField();
		int cumulativeWellsCount = countCumulativeWells(field);

		double score = (double) cumulativeWellsCount * cumulativeWellsWeight;

		return score;
	}

	private int countCumulativeWells(int[][] field) {

		int cumulativeWellsCount = 0;
		final int amountOfColumns = field[0].length;

		for (int col = 0; col < amountOfColumns; col++) {
			cumulativeWellsCount += countCumulativeWells(col, field);
		}

		return cumulativeWellsCount;

	}

	private int countCumulativeWells(int col, int[][] field) {

		int wellCount = 0;
		final int amountOfRows = field.length;
		final int amountOfColumns = field[0].length;
		boolean leftFilled;
		boolean rightFilled;

		for (int row = amountOfRows - 1; row >= 0; row--) {

			if (col == 0) {
				leftFilled = true;
			} else if (field[row][col - 1] == 0) {
				leftFilled = false;
			} else {
				leftFilled = true;
			}

			if (col == amountOfColumns - 1) {
				rightFilled = true;
			} else if (field[row][col + 1] == 0) {
				rightFilled = false;
			} else {
				rightFilled = true;
			}

			if (leftFilled && rightFilled) {
				for (int i = row; i >= 0; i--) {
					if (field[i][col] == 0) {
						wellCount++;
					} else {
						break;
					}
				}
			}

		}
		return wellCount;
	}

	private double calculateBuriedHolesScore(double[] weights,
			StateTester clonedState) {
		double buriedHolesWeight = weights[Weight.HOLES.Value];

		int[][] field = clonedState.getField();
		int totalBuriedHoles = countBuriedHoles(field);

		double score = (double) totalBuriedHoles * buriedHolesWeight;

		return score;
	}

	private int countBuriedHoles(int[][] field) {

		int buriedHolesCount = 0;
		final int amountOfColumns = field[0].length;

		for (int col = 0; col < amountOfColumns; col++) {
			buriedHolesCount += countBuriedHolesInColumn(col, field);
		}

		return buriedHolesCount;
	}

	private int countBuriedHolesInColumn(int col, int[][] field) {
		int holeCount = 0;
		final int amountOfRows = field.length;

		boolean ceilingSeen = false;

		for (int row = amountOfRows - 1; row >= 0; row--) {
			if (ceilingSeen) {
				if (field[row][col] == 0) {
					holeCount++;
				}
			} else if (field[row][col] != 0) {
				ceilingSeen = true;
			}
		}

		return holeCount;
	}

	private double calculateColumnTransitionScore(double[] weights,
			StateTester clonedState) {
		double columnTransitionWeight = weights[Weight.COLUMN_TRANSITIONS.Value];

		int[][] field = clonedState.getField();
		int totalColumnTransitions = countColumnTransitions(field);

		double score = (double) totalColumnTransitions * columnTransitionWeight;

		return score;
	}

	private int countColumnTransitions(int[][] field) {
		int columnTransitionCount = 0;
		final int amountOfColumns = field[0].length;

		for (int col = 0; col < amountOfColumns; col++) {
			columnTransitionCount += getTransitionCountForColumn(col, field);
		}

		return columnTransitionCount;
	}

	private int getTransitionCountForColumn(int col, int[][] field) {
		int transitionCount = 0;
		final int amountOfRows = field.length;

		// Bottom implicitly filled
		if (field[0][col] == 0) {
			transitionCount++;
		}

		for (int row = 0; row < amountOfRows - 1; row++) {
			if ((field[row][col] == 0 && field[row + 1][col] != 0)
					|| (field[row][col] != 0 && field[row + 1][col] == 0)) {
				transitionCount++;
			}
		}

		// Top implicitly empty
		if (field[amountOfRows - 1][col] != 0) {
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

	private int countRowTransitions(int[][] field) {

		int rowTransitionCount = 0;
		final int amountOfRows = field.length;

		for (int row = 0; row < amountOfRows; row++) {
			rowTransitionCount += getTransitionCountForRow(row, field);
		}

		return rowTransitionCount;
	}

	private int getTransitionCountForRow(int row, int[][] field) {

		int transitionCount = 0;
		final int amountOfColumns = field[0].length;

		// Left edge implicitly filled
		if (field[row][0] == 0) {
			transitionCount++;
		}

		for (int col = 0; col < amountOfColumns - 1; col++) {
			if ((field[row][col] == 0 && field[row][col + 1] != 0)
					|| (field[row][col] != 0 && field[row][col + 1] == 0)) {
				transitionCount++;
			}
		}

		// Right edge implicitly filled
		if (field[row][amountOfColumns - 1] == 0) {
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
	private int countRowsHoles(int[][] field) {
		int rowsWithHoles = 0;
		int rows = field.length;
		int[] columnHeights = getColumnHeights(field);

		// count rows containing holes
		for (int i = 0; i < rows; i++) {
			for (int col = 0; col < field[i].length; col++) {
				if (field[i][col] == 0 && columnHeights[col] > i) {
					rowsWithHoles++;
					break;
				}
			}
		}
		return rowsWithHoles;
	}

	// Took this from countRowsHoles(int[][]) and made it into a function
	// as I was going to use it.
	private int[] getColumnHeights(int[][] field) {
		int rows = field.length;
		int columns = field[0].length;
		int[] columnHeights = new int[rows];

		// get height of columns
		// 0: empty column
		// 1: index 0 is the highest filled cell in the column
		int row;
		for (int column = 0; column < columns; column++) {
			row = rows - 1; // fit indices
			while (row >= 0 && field[row][column] == 0) {
				row--;
			}
			columnHeights[column] = row + 1;
		}
		return columnHeights;
	}

	private double calculateLandingHeightScore(double[] weights,
			StateTester stateBeforeMove, StateTester stateAfterMove) {
		double landingHeightWeight = weights[Weight.LANDING_HEIGHT.Value];
		double landingHeight = findLandingHeight(stateBeforeMove, stateAfterMove);

		return (double) landingHeight * landingHeightWeight;
	}

    // Calculates average landing height of a piece across its width,
    // before the filled rows are cleared.
	private double findLandingHeight(StateTester stateBeforeMove,
			StateTester stateAfterMove) {
		int numRowsCleared = stateAfterMove.rowsClearedAfterMove;
		int[] columnHeightsBefore = getColumnHeights(stateBeforeMove.getField());
		int[] columnHeightsAfter = getColumnHeights(stateAfterMove.getField());
		int numColumns = stateBeforeMove.getField()[0].length;
		int landingHeightsSum = 0;
        int pieceWidth = 0;

		for (int i = 0; i < numColumns; i++) {
			if (columnHeightsBefore[i] != columnHeightsAfter[i]
					+ numRowsCleared) {
				landingHeightsSum += columnHeightsBefore[i];
                pieceWidth++;
			}
		}

		return (double) landingHeightsSum / pieceWidth;
	}
	

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
			s.draw();
			s.drawNext(0, 0);
		}
		System.out.println("You have completed " + s.getRowsCleared()
				+ " rows.");
	}

}
