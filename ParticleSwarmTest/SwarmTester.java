package ParticleSwarmTest;

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

		double bestFitness = 0;
		while (bestFitness < 1e8) {
			swarm.optimise();
			if (swarm.getBestScore() > bestFitness) {
				bestFitness = swarm.getBestScore();
				System.out.println(swarm.toStringStats());
			}
		}
	}

}
