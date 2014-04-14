import java.util.concurrent.Callable;

public class CallableState extends State implements Callable<Integer> {
	int id;
	CallableState (int assignedId){
		id = assignedId;
	}

	@Override
	public Integer call() {
		PlayerSkeleton p = new PlayerSkeleton();
		int lastCleared = 0;
		while (!this.hasLost()) {
			this.makeMove(p.pickMove(this, this.legalMoves()));

			int cleared = this.getRowsCleared();
			if (cleared % 10000 == 0 && cleared != lastCleared){
				lastCleared = cleared;
				System.out.println("ID " + this.id + " Rows cleared: " + cleared);
			}
		}
		System.out.println("ID " + this.id + " completed with" + this.getRowsCleared()
				+ " rows cleared.");
		return this.getRowsCleared();
	}
}