import java.util.HashMap;
import java.util.Map.Entry;

public class Results {
	
	public String algorithmName, terrainType;
	public int boardSize, numObjects,objectsCollected, stepsTaken;
	public boolean collectedDamon, returnedHome;
	public long totalPlanningTime, randomSeed;
	public double estimationNoise, startingBattery, batteryUsed;
	public HashMap<String, String> hyperparameters;
	
	public static final String header = "\"algorithmName\",\"terrainType\",\"randomSeed\",\"boardSize\",\"numObjects\",\"collectedDamon\",\"returnedHome\",\"objectsCollected\",\"stepsTaken\",\"runtime\",\"estimationNoise\",\"startingBattery\",\"usedBattery\"";
	
	public Results(String algorithmName, String terrainType, long randomSeed, int boardSize, int numObjects,
			boolean collectedDamon, boolean returnedHome, int objectsCollected, int stepsTaken, long totalPlanningTime,
			double estimationNoise, double startingBattery, double batteryUsed, HashMap<String, String> hyperparameters) {
		this.algorithmName = algorithmName;
		this.terrainType = terrainType;
		this.randomSeed = randomSeed;
		this.boardSize = boardSize;
		this.numObjects = numObjects;
		this.collectedDamon = collectedDamon;
		this.returnedHome = returnedHome;
		this.objectsCollected = objectsCollected;
		this.stepsTaken = stepsTaken;
		this.totalPlanningTime = totalPlanningTime;
		this.estimationNoise = estimationNoise;
		this.startingBattery = startingBattery;
		this.batteryUsed = batteryUsed;
		this.hyperparameters = hyperparameters;
	}
	
	
	public String toString() {
		String ret = "\"" + algorithmName + "\",\"" + terrainType + "\"," + randomSeed + "," + boardSize + "," + 
				numObjects + "," + collectedDamon + "," + returnedHome + "," + objectsCollected + "," + 
				stepsTaken + "," + totalPlanningTime + "," + estimationNoise + "," + startingBattery + "," + 
				batteryUsed;
		
		for (Entry<String, String> entry: hyperparameters.entrySet())
			ret += ",\"" + entry.getKey() + "\"," + entry.getValue();
		
		return ret;
	}
	
}
