package ParticleSwarmTest;

import net.ayulin.simpleswarmer.EvaluationFunction;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

enum Weight {
	LANDING_HEIGHT(0), ROWS_CLEARED(1), ROW_TRANSITIONS(2), COLUMN_TRANSITIONS(
			3), HOLES(4), CUMULATIVE_WELLS(5), HOLE_DEPTH(6), ROW_HOLES(7);

	public final int Value;

	private Weight(int value) {
		Value = value;
	}
}

public class GameEvaluationFunction implements EvaluationFunction {
	int gamesToAverageOver = 10;
	ExecutorService threadPool = Executors.newFixedThreadPool (gamesToAverageOver);

	@Override
	public double evaluatePosition(double[] position) {
		// The position here is an array of all the weights.
		// We need to play the game from start to finish using
		// these weights, and return the number of rows cleared
		// (or some other metric based on that).

		ArrayList<Future<Double>> trackers = new ArrayList<Future<Double>>();

		for (int i = 0; i < gamesToAverageOver; i++){
			Callable<Double> game = new CallableGame(position, i);
			Future<Double> tracker = threadPool.submit(game);
			trackers.add(tracker);
		}

		int total = 0;
		int successfulGames = 0;
				
		while (!trackers.isEmpty()){
			Future<Double> currentTracker = trackers.get(0);
			if (currentTracker.isDone()){
				try {
					total += currentTracker.get();
					successfulGames++;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				trackers.remove(currentTracker);
			}
		}

		if (successfulGames > 0){
			return total/successfulGames;
		} else {
			return 0;
		}
	}

	protected static int pickMove(Game currentGame, int[][] moves, double[] weights) {
		int moveChoice = 0;
		double currentHighScore = Double.NEGATIVE_INFINITY;

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

	private static double calculateWeightedScore(double[] weights,
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

	private static double calculateCumulativeWellScore(double[] weights,
			StateTester clonedState) {
		double cumulativeWellsWeight = weights[Weight.CUMULATIVE_WELLS.Value];

		int[][] field = clonedState.getField();
		int cumulativeWellsCount = countCumulativeWells(field);

		double score = (double) cumulativeWellsCount * cumulativeWellsWeight;

		return score;
	}

	private static int countCumulativeWells(int[][] field) {

		int cumulativeWellsCount = 0;
		final int amountOfColumns = field[0].length;

		for (int col = 0; col < amountOfColumns; col++) {
			cumulativeWellsCount += countCumulativeWells(col, field);
		}

		return cumulativeWellsCount;

	}

	private static int countCumulativeWells(int col, int[][] field) {

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

	private static double calculateBuriedHolesScore(double[] weights,
			StateTester clonedState) {
		double buriedHolesWeight = weights[Weight.HOLES.Value];

		int[][] field = clonedState.getField();
		int totalBuriedHoles = countBuriedHoles(field);

		double score = (double) totalBuriedHoles * buriedHolesWeight;

		return score;
	}

	private static int countBuriedHoles(int[][] field) {

		int buriedHolesCount = 0;
		final int amountOfColumns = field[0].length;

		for (int col = 0; col < amountOfColumns; col++) {
			buriedHolesCount += countBuriedHolesInColumn(col, field);
		}

		return buriedHolesCount;
	}

	private static int countBuriedHolesInColumn(int col, int[][] field) {
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

	private static double calculateColumnTransitionScore(double[] weights,
			StateTester clonedState) {
		double columnTransitionWeight = weights[Weight.COLUMN_TRANSITIONS.Value];

		int[][] field = clonedState.getField();
		int totalColumnTransitions = countColumnTransitions(field);

		double score = (double) totalColumnTransitions * columnTransitionWeight;

		return score;
	}

	private static int countColumnTransitions(int[][] field) {
		int columnTransitionCount = 0;
		final int amountOfColumns = field[0].length;

		for (int col = 0; col < amountOfColumns; col++) {
			columnTransitionCount += getTransitionCountForColumn(col, field);
		}

		return columnTransitionCount;
	}

	private static int getTransitionCountForColumn(int col, int[][] field) {
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

	private static double calculateRowTransitionScore(double[] weights,
			StateTester clonedState) {
		double rowTransitionWeight = weights[Weight.ROW_TRANSITIONS.Value];

		int[][] field = clonedState.getField();
		int totalRowTransitions = countRowTransitions(field);

		double score = (double) totalRowTransitions * rowTransitionWeight;

		return score;
	}

	private static int countRowTransitions(int[][] field) {

		int rowTransitionCount = 0;
		final int amountOfRows = field.length;

		for (int row = 0; row < amountOfRows; row++) {
			rowTransitionCount += getTransitionCountForRow(row, field);
		}

		return rowTransitionCount;
	}

	private static int getTransitionCountForRow(int row, int[][] field) {

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

	private static double calculateHoleDepthScore(double[] weights,
			StateTester clonedState) {
		double holeDepthWeight = weights[Weight.HOLE_DEPTH.Value];

		int[][] field = clonedState.getField();
		int totalHoleDepth = countHoleDepth(field);

		double score = (double) totalHoleDepth * holeDepthWeight;
		return score;
	}

	private static int countHoleDepth(int[][] field) {
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

	private static double calculateRowsClearedScore(double[] weights,
			StateTester clonedState) {
		double rowsClearedWeight = weights[Weight.ROWS_CLEARED.Value];
		double rowsClearedScore = clonedState.rowsClearedAfterMove
				* rowsClearedWeight;
		return rowsClearedScore;
	}

	private static double calculateRowsHolesScore(double[] weights,
			StateTester clonedState) {
		double rowsHolesWeight = weights[Weight.ROW_HOLES.Value];

		int[][] field = clonedState.getField();
		int rowsWithHoles = countRowsHoles(field);

		double score = (double) rowsWithHoles * rowsHolesWeight;
		return score;
	}

	// A cell is a hole, or part of, if it is empty and
	// its row is lower than its column's height
	private static int countRowsHoles(int[][] field) {
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
	private static int[] getColumnHeights(int[][] field) {
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

	private static double calculateLandingHeightScore(double[] weights,
			StateTester stateBeforeMove, StateTester stateAfterMove) {
		double landingHeightWeight = weights[Weight.LANDING_HEIGHT.Value];
		double landingHeight = findLandingHeight(stateBeforeMove, stateAfterMove);

		return (double) landingHeight * landingHeightWeight;
	}

	// Calculates average landing height of a piece across its width,
	// before the filled rows are cleared.
    private static double findLandingHeight(StateTester stateBeforeMove,
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
}