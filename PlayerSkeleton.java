
public class PlayerSkeleton {
	
	int ROWS;
	int COLS;

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		double best = -10000, pos = 0.0;
		int toR = 0;
		for(int j=0;j<legalMoves.length;j++){
			pos = getMoveScorePierre(s,legalMoves[j][0], legalMoves[j][1]);
			//System.out.println(pos);
			if (pos>best){
				best = pos;
				toR = j;
			}
		}
		return toR;
	}
	
	public double getMoveScorePierre(State s, int orient, int slot){
		int[] top = s.getTop();
		int[][] pWidth = s.getpWidth();
		int[][] pHeight = s.getpHeight();
		int[][][] pBottom = s.getpBottom();
		int[][][] pTop = s.getpTop();
		int nextPiece = s.getNextPiece();
		ROWS = s.ROWS;
		COLS = s.COLS;
		int[][] field = s.getField();
		int turn = s.getTurnNumber();
		
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}
		
		double pierre = 0;
		
		double factor1,factor2,factor3,factor4,factor5,factor6;
		
		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			return 0;
		}

		int[][] fieldCopy = new int[ROWS][COLS];
		
		for(int i=0;i<ROWS;i++){
			for(int j=0;j<COLS;j++){
				fieldCopy[i][j] = field[i][j];
			}
		}
		
		int ymin = 999;
		int ymax = 0;
		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				fieldCopy[h][i+slot] = turn+1;
				if (h<ymin){
					ymin = h;
				}
				if (h>ymax){
					ymax = h;
				}
			}
		}
		
		factor1 = (0.5*(ymin+ymax));

		int rowsCleared = 0;
		int erodedPiece = 0;
		
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(fieldCopy[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {
					if (fieldCopy[r][c]==(turn+1)){
						erodedPiece++;
					}
					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						fieldCopy[i][c] = fieldCopy[i+1][c];
					}

				}
			}
		}
		
		factor2 = (double)rowsCleared * (double)erodedPiece;
		
		factor3 = 0.0;
		factor4 = 0.0;
		factor5 = 0.0;
		factor6 = 0.0;
		
		
		for(int r=0;r<ROWS;r++){
			factor3 += getTransitionCountForRow(r,fieldCopy);
		}
		
		for(int c=0;c<COLS;c++){
			factor4 += getTransitionCountForCol(c,fieldCopy);
			factor5 += getBuriedHoles(c,fieldCopy);
			factor6 += getAllWells(c,fieldCopy);
		}
		
		pierre = 0.0;
		pierre += (-1.0) * factor1;
		pierre += (1.0) * factor2;
		pierre += (-1.0) * factor3;
		pierre += (-1.0) * factor4;
		pierre += (-4.0) * factor5;
		pierre += (-1.0) * factor6;
		
		return pierre;
	
	}
	
	public int getAllWells(int x, int[][] someField){
		int wellValue  = 0;
		int y          = 0;
		int cellLeft   = 0;
		int cellRight  = 0;
		int blanksDown = 0;
	
		for ( y = ROWS-1; y >= 1; y-- )
		{
			if ((x-1) >= 1)
			{
				cellLeft  = someField[y][x-1];
			}
			else
			{
				cellLeft = 1;
			}
	
			if ((x + 1) <= COLS-1)
			{
				cellRight = someField[y][x+1];
			}
			else
			{
				cellRight = 1;
			}
	
			if ((cellLeft!=0) && (cellRight!=0))
			{
				blanksDown = 0;
				for(int i=y;i>=0;i--){
					if(someField[i][x]>0){
						break;
					} else {
						blanksDown++;
					}
				}
				wellValue += blanksDown;
			}
		}
	
		return( wellValue );
	}
	
	public int getBuriedHoles(int x, int[][] someField){
		int totalHoles = 0;
		int cell   = 0;
		int enable = 0;
		int y      = 0;

		for ( y = ROWS-1; y >= 1; y-- )
		{
			cell = someField[y][x];

			if (0 != cell)
			{
				enable = 1;
			}
			else
			{
				if (0 != enable)
				{
					totalHoles++;
				}
			}
		}

		return( totalHoles );
	}
		
	public int getTransitionCountForCol(int x,int[][] someField){
		int transitionCount = 0;

		int y     = 0;
		int flagA = 0;
		int flagB = 0;

		// Check cell and neighbor to right...
		for ( y = 1; y < ROWS-1; y++ )
		{
			if(someField[y][x]==0){
				flagA = 0;
			} else {
				flagA = 1;
			}
			if(someField[y+1][x]==0){
				flagB = 0;
			} else {
				flagB = 1;
			}

			// If a transition from occupied to unoccupied, or
			// from unoccupied to occupied, then it's a transition.
			if 
				( 
				((0 != flagA) && (0 == flagB)) ||
				((0 == flagA) && (0 != flagB)) 
				)
			{
				transitionCount++;
			}
		}

		// Check transition between left-exterior and column 1.
		// (NOTE: Exterior is implicitly "occupied".)
		flagA = someField[0][x];
		if (0 == flagA) 
		{
			transitionCount++;
		}

		// Check transition between column 'mWidth' and right-exterior.
		// (NOTE: Exterior is implicitly "occupied".)
		flagA = someField[ROWS-1][x];
		if (0 == flagA) 
		{
			transitionCount++;
		}

		return( transitionCount );
	}
	
	public int getTransitionCountForRow(int y,int[][] someField){
		int transitionCount = 0;

		int x     = 0;
		int flagA = 0;
		int flagB = 0;

		// Check cell and neighbor to right...
		for ( x = 1; x < COLS-1; x++ )
		{
			if(someField[y][x]==0){
				flagA = 0;
			} else {
				flagA = 1;
			}
			if(someField[y][x+1]==0){
				flagB = 0;
			} else {
				flagB = 1;
			}

			// If a transition from occupied to unoccupied, or
			// from unoccupied to occupied, then it's a transition.
			if 
				( 
				((0 != flagA) && (0 == flagB)) ||
				((0 == flagA) && (0 != flagB)) 
				)
			{
				transitionCount++;
			}
		}

		// Check transition between left-exterior and column 1.
		// (NOTE: Exterior is implicitly "occupied".)
		flagA = someField[y][0];
		if (0 == flagA) 
		{
			transitionCount++;
		}

		// Check transition between column 'mWidth' and right-exterior.
		// (NOTE: Exterior is implicitly "occupied".)
		flagA = someField[y][COLS-1];
		if (0 == flagA) 
		{
			transitionCount++;
		}

		return( transitionCount );
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
}
