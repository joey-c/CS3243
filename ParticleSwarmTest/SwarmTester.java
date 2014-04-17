package ParticleSwarmTest;

import java.util.Date;

import net.ayulin.simpleswarmer.OptimisationProblem;
import net.ayulin.simpleswarmer.OptimisationStrategy;
import net.ayulin.simpleswarmer.Swarm;

public class SwarmTester {

	public static void main(String[] args) {
		OptimisationProblem problem = new OptimisationProblem();
		problem.setDimensions(8);
		problem.setMaxMinPosition(20);
		problem.setMaxMinVelocity(10);
		problem.setNumberOfParticles(200);
		problem.setParticleResetProbability(0.2);
		problem.setStrategy(OptimisationStrategy.MAXIMISE);
		problem.setFunction(new GameEvaluationFunction());

		Swarm swarm = Swarm.forOptimisationProblem(problem);

		/*
		// Our own best-known weights.
		swarm.seedParticlePosition(0, new double[] { -8.246690443403839, 5.5653965284795035, -6.0028118815494835, -13.893704583221037,
				-7.686314111961439, -10.50286050418964, -3.042176134157476, -18.186194325237683 });

		// El-Tetris weights.
		swarm.seedParticlePosition(1, new double[] { -4.500158825082766, 3.4181268101392694, -3.2178882868487753, -9.348695305445199,
				-7.899265427351652, -3.3855972247263626, 0, 0 });

		// Pierre's weights.
		swarm.seedParticlePosition(2, new double[] { -1.0, 1.0, -1.0, -1.0, -4.0, -1.0, 0, 0 });
		*/

		double bestFitness = 0;
		int rounds = 0;
		while (bestFitness < 1e8) {
			rounds++;
			System.out.println(String.format("Round %d started at %s", rounds, new Date()));

			swarm.optimise();
			if (swarm.getBestScore() > bestFitness) {
				bestFitness = swarm.getBestScore();
				System.out.println(swarm.toStringStats());
			}
		}
	}
}
