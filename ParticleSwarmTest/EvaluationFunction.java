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
		return 0;
	}

}
