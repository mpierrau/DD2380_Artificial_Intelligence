import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {

	public static void main(String[] args) {
//		Simulation sim = new Simulation(1011, "Genetic", 32, 3, 3.0, 8.0, 100.0, 0.05, 0.2, 0.8, 0.2);
//		Renderer r = new Renderer(sim);
//		r.renderSimulation(400);
		evaluateAlgorithms();
	}
	
	public static void evaluateAlgorithms() {
		Debug.Log("Evaluating Algorithms.");
		// Define the axes of the gridsearch
		int[] worldSizes = {64, 48, 32};
		int[] numObjects = {5, 3};
		double[] estimationNoises = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6};
		double[] batteries = {2.0, 1.0, 0.5, 0.25, 0.125};
		int numSeeds = 1;
		
		int totalSims = worldSizes.length * numObjects.length * estimationNoises.length * batteries.length * numSeeds;
		int currentSim = 1;
		
		Random seedSelector = new Random();
		FileWriter output = null;
		try {
			output = new FileWriter("Results.csv", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			output.write(Results.header+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long duration;
		// Perform the grid search
		for (int w = 0; w < worldSizes.length; w++) {
			for (int objs = 0; objs < numObjects.length; objs++) {
				for (int est = 0; est < estimationNoises.length; est++) {
					for (int batt = 0; batt < batteries.length; batt++) {
						for (int seedIdx = 0; seedIdx < numSeeds; seedIdx++) {
							duration = System.nanoTime();
							// Scale up battery with square of world size
							double battery = batteries[batt] * worldSizes[w] * worldSizes[w] / 20.0;
							long seed = seedSelector.nextLong();
							
							// Run simulations for all three algorithms
							Simulation simGenetic = new Simulation(seed, "Genetic", worldSizes[w], numObjects[objs], 3.0, 8.0, battery, 0.05, 0.2, 0.8, estimationNoises[est]);
							Results resGenetic = simGenetic.runSimulation();
							Simulation simGreedy = new Simulation(seed, "Greedy", worldSizes[w], numObjects[objs], 3.0, 8.0, battery, 0.05, 0.2, 0.8, estimationNoises[est]);
							Results resGreedy = simGreedy.runSimulation();
							Simulation simAnts = new Simulation(seed, "Ants", worldSizes[w], numObjects[objs], 3.0, 8.0, battery, 0.05, 0.2, 0.8, estimationNoises[est]);
							Results resAnts = simAnts.runSimulation();
							
							// Record results
							try {
								output.write(resGenetic.toString()+"\n");
								output.write(resGreedy.toString()+"\n");
								output.write(resAnts.toString()+"\n");
								output.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}
							duration = System.nanoTime() - duration;
							Debug.Log("Completed simulation " + (currentSim++) + "/" + totalSims + " in " + duration / 1000000000.0 + " sec");
						}
					}
				}
			}
		}

		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
