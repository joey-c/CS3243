package ParticleSwarmTest;

import java.util.concurrent.Callable;

public class CallableGame extends Game implements Callable <Double>{

	double[] position;
	int id;
	public CallableGame(double[] weights, int assignedID){
		super();
		this.position = weights;
		this.id = assignedID;
	}
	
	@Override
	public Double call() {
		int lastCleared = 0;

		while (!this.hasLost()) {
			int cleared = this.getRowsCleared();
			if (cleared % 100000 == 0 && cleared != lastCleared){
				lastCleared = cleared;
				System.out.println("ID " + this.id + " Rows cleared: " + cleared);
			}			
			int[][] moves = this.legalMoves();

			int chosenMove = GameEvaluationFunction.pickMove(this, moves, this.position);
			this.makeMove(chosenMove);
		}	

		double rowsCleared = this.getRowsCleared();
		return rowsCleared;
	}
}