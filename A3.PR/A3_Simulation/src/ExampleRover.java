import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Predicate;

public class ExampleRover extends Rover{
	
	private Random rand;
	private ArrayList<Point> visited;
	private final Predicate<Point> hasBeenVisited = p -> (visited.contains(p));

	public ExampleRover(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		super(startX, startY, startingBattery, estimatedEnv);
		
		// Here we can initialize objects specific to our algorithm
		rand = new Random();
		visited = new ArrayList<Point>();
	}
	
	
	public HashMap<String, String> getHyperparameters(){
		return new HashMap<String, String>();
	}
	
	
	// Returns true if we should update our plan
	public boolean shouldUpdate() {
		// This example rover will always update its plan
		return true;
	}

	
	// Updates our plan (by changing this.currentPlan)
	public ArrayList<Point> updatePlan() {
		if (Debug.ON) Debug.LogEnter("ExampleRover::updatePlan");
		// Add our current location to the places we have visited
		visited.add(this.currentLocation);
		
		// Gets  the valid neighbors of this point (excluding points outside of the terrain)
		ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
		
		// Remove neighbors we have already visited
		options.removeIf(hasBeenVisited);
		
		// If we have no options, just return an empty plan
		if (options.isEmpty()) {
			if (Debug.ON) Debug.LogExit("ExampleRover::updatePlan");
			return options;
		}
		
		// Selects a random neighbor as our next move
		ArrayList<Point> ret = new ArrayList<Point>();
		ret.add(options.get(rand.nextInt(options.size())));
		
		if (Debug.ON) Debug.LogExit("ExampleRover::updatePlan");
		return ret;
	}

}
