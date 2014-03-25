package ParticleSwarmTest;

import net.sourceforge.jswarm_pso.FitnessFunction;

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
