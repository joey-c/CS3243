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
		this.field = s.getField();
		this.top = s.getTop();

		turn = s.getTurnNumber() + 1;
	}

	public static StateTester fromState(State s) {
		return new StateTester(s);
	}

	public boolean makeMove(int orient, int slot) {
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
		return 0;
	}

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
			s.draw();
			s.drawNext(0, 0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed " + s.getRowsCleared()
				+ " rows.");
	}

}
