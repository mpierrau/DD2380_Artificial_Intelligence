import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Rover {

	public Environment estimatedEnv;
	public double remainingBattery;
	public long planningTime;
	public boolean damonCollected;
	public boolean[] objectCollected;
	public Point currentLocation, startingLocation;
	public ArrayList<Point> currentPlan;
	
	
	// All rover classes must provide these pieces
	public Rover(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		this.startingLocation = new Point(startX, startY);
		this.currentLocation = this.startingLocation;
		this.remainingBattery = startingBattery;
		this.estimatedEnv = estimatedEnv;
		this.currentPlan = new ArrayList<Point>();
		this.objectCollected = new boolean[estimatedEnv.numObjects];
		for (int i = 0; i < estimatedEnv.numObjects; i++)
			this.objectCollected[i] = false;
		this.damonCollected = false;
	}
	
	// Here the rover decides if it needs to update its plan
	public abstract boolean shouldUpdate();
	
	// Here the rover updates its current plan.
	public abstract ArrayList<Point> updatePlan();
	
	// Here the rover returns a map containing all of its hyperparameter
	// names and values (for results)
	public abstract HashMap<String, String> getHyperparameters();
	
	
	public Point getNextMove() {
		if (Debug.ON) Debug.LogEnter("Rover::getNextMove");
		
		long startTime = System.nanoTime();
		if(shouldUpdate())
			this.currentPlan = updatePlan();
		long duration = System.nanoTime() - startTime;
		planningTime += duration;
		
		if (Debug.ON) Debug.Log("Time to plan: " + duration + ", Running total: " + planningTime);
			
		if (currentPlan.size() == 0) {
			if (Debug.ON) {
				Debug.Log("Returning NULL");
				Debug.LogExit("Rover::getNextMove");
			}
			return null;
		}
		
		if (Debug.ON) Debug.LogExit("Rover::getNextMove");
		return currentPlan.remove(0);
	}
}
