package ParticleSwarmTest;

import net.sourceforge.jswarm_pso.Swarm;

public class SwarmTester {

	public static void main(String[] args) {
		Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES, new WeightParticle(), new EvaluationFunction());
		swarm.setMaxPosition(20); // The maximum any one weight could be.
		swarm.setMinPosition(-20); // The minimum any one weight could be.
		
		// We may need to set velocity increments as well? The defaults
		// are 0.9, which seem rather high.
		
		int maxSwarmIterations = 10000;
		for (int i = 0; i < maxSwarmIterations; i++) {
			swarm.evolve();
		}
		
		System.out.println(swarm.toStringStats());
	}

}
