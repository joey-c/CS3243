package ParticleSwarmTest;

import net.sourceforge.jswarm_pso.Swarm;

public class SwarmTester {

	public static void main(String[] args) {
		Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES, new WeightParticle(), new EvaluationFunction());
		swarm.setMaxPosition(10); // The maximum any one weight could be.
		swarm.setMinPosition(-10); // The minimum any one weight could be.
		
		int maxSwarmIterations = 10000;
		for (int i = 0; i < maxSwarmIterations; i++) {
			swarm.evolve();
		}
		
		System.out.println(swarm.toStringStats());
	}

}
