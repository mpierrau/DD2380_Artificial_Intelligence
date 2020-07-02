import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GeneticRover extends Rover{

	public PlanGA currentGAPlan;
	int distanceHome;
	boolean doRetreat = false;
	PlanGA retreatPlan;
	double avgMovementCost;
	double estRetreatCost;
	ArrayList<Point> traversedPath = new ArrayList<Point>();
	boolean damonCollected;
//	The hyperparameters below can be tweaked.
//	With these parameters the algorithm runs well
		
	public int planLength = 15;			//(The length (number of steps) of the produced plans.
	public int populationSize = 100;	//( > 5 for code to work, but > ) How large should the population of plans be
	public double selectionProp = 0.25;	//0 < p < 1 What proportion of the simulated population should be chosen for breeding
	public int generations = 40;		//> 1 Number of generations that plans iterate through
	public double eliteProp = 0.1;		//0 < p < 1 Proportion of top selected subpopulation that gets marked as elite
	public double mutateProb = 0.15;	//0 < p < 1 Probability of a child being mutated
	double mutationProp = 0.20;			//0 < p < 1 Proportion of path that gets mutated (how many moves get swapped)
	double marginProp = 1.5;			//How much do we overestimate the cost of traversing nodes; 1.5 = 150%
										//higher value here gives safer plan but worse performance in recovering Damon & objects
	
	

	public GeneticRover(
			int startX, 
			int startY, 
			double startingBattery, 
			Environment estimatedEnv,
			int planLength,
			int populationSize,
			double selectionProp,
			int generations,
			double eliteProp,
			double mutateProb,
			double mutationProp,
			double marginProp) {
		super(startX, startY, startingBattery, estimatedEnv);
		this.planLength = planLength;
		this.populationSize = populationSize;
		this.selectionProp = selectionProp;
		this.generations = generations;
		this.eliteProp = eliteProp;
		this.mutateProb = mutateProb;
		this.mutationProp = mutationProp;
		this.marginProp = marginProp;
	}
	
//	Function for abstract class Rover
//	Used for testing
	public HashMap<String, String> getHyperparameters(){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("planLength", "" + planLength);
		params.put("populationSize", "" + populationSize);
		params.put("selectionProp", "" + selectionProp);
		params.put("generations", "" + generations);
		params.put("eliteProp", "" + eliteProp);
		params.put("mutateProb", "" + mutateProb);
		params.put("mutationProp", "" + mutationProp);
		params.put("marginProp", "" + marginProp);
		return params;
	}
	
//	Calculating the average cost of traversing a node based on the traversed path so far
	public void avgTravelCost() {
		double avgCost;
		double steps = traversedPath.size()-1;

		if(traversedPath.size() == 1) {
			avgCost = 0;
		} else {
			avgCost = ((this.avgMovementCost)*(steps-1) +
					estimatedEnv.getCost(traversedPath.get(traversedPath.size()-2), currentLocation))/steps;
		}
		avgMovementCost = avgCost;
	}

//	Function used by abstract class Rover
//	Determines whether the Rover should update its current plan
	
	public boolean shouldUpdate() {
		try {
			traversedPath.add(currentLocation);
			avgTravelCost();
			
			distanceHome = Math.abs(startingLocation.x - currentLocation.x) + Math.abs(startingLocation.y - currentLocation.y);
			estRetreatCost = distanceHome*avgMovementCost;

			
			if (currentPlan.size() == 0) {
				return true;
			}

			ArrayList<Point> neighbors = estimatedEnv.getNeighbors(currentLocation);

//			If we are retreating and the current plan ends at home and we (probably) won't run out of battery
//			then don't update plan
			if(doRetreat && (retreatPlan.plannedCoords.get(retreatPlan.plannedCoords.size()-1).equals(startingLocation)) && (estRetreatCost < remainingBattery)) {
				return false;
			}

			doRetreat = shouldRetreat();

//			If we get here then probably our retreatPlan is no good
//			Update retreatPlan
			if (doRetreat) {
				return true;
			}

//			If we haven't found Damon and he's next to us we want to update plan (and get him)
			if(!damonCollected) {
				for(Point coord : neighbors) {
					if(estimatedEnv.pdfDamon[coord.x][coord.y] == 1.0) {
						return true;
					}
				}
			}

//			If we are next to an object we want to update the plan (to pick it up)
			for(int i=0; i<estimatedEnv.pdfObjects.length; i++) {
				for(Point coord : neighbors) {
					if(estimatedEnv.pdfObjects[i][coord.x][coord.y] == 1.0) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Debug.Log("Line 109 : " + e);
		}
		
//		Otherwise no need to update plan
		return false;
	}

	public ArrayList<Point> updatePlan() {

		PlanGA newPlan;
		ArrayList<Point> neighbors = estimatedEnv.getNeighbors(currentLocation);
		ArrayList<Point> getObject = new ArrayList<Point>();
		ArrayList<Point> getDamon = new ArrayList<Point>();

		if (Debug.ON) Debug.Log("Entering updatePlan()");
//		If we are next to an object we want to retreive it
		for(int i=0; i<estimatedEnv.pdfObjects.length; i++) {
			for(Point coord : neighbors) {
				if(estimatedEnv.pdfObjects[i][coord.x][coord.y] == 1.0) {
					currentPlan = getObject;
					getObject.add(coord);
					break;
				}
			}
		}

		if (Debug.ON) Debug.Log("Plan empty and going home?");
		if(distanceHome == 0 && doRetreat) {
			return new ArrayList<Point>();
		} else if(!damonCollected) {
//			If Matt Damon is next to us we want to retreive him
			for(Point coord : neighbors) {
				if(estimatedEnv.pdfDamon[coord.x][coord.y] == 1.0) {
					currentPlan = getDamon;
					getDamon.add(coord);
					damonCollected = true;
					break;
				}
			}
		}

//		First go for Damon (even if retreating), then go for objects (unless retreating),
//		then if we are retreating and distance to home > 1 we want to recalculate the retreatplan
		if(getDamon.size() > 0) {
			if (Debug.ON) Debug.Log("Damon is here");
			return getDamon;
		} else if (getObject.size() > 0 && !doRetreat) {
			if (Debug.ON) Debug.Log("Object is here and we have enough batteries");
			return getObject;
		} else if(doRetreat && (distanceHome > 1)) {
//			Create new plan that is has minimum length to home
//			This plan will adhere to a different fitness function
//			than when not retreating
			newPlan = geneticAlgorithm(
					distanceHome,
					populationSize,
					selectionProp,
					generations,
					eliteProp,
					mutateProb,
					mutationProp,
					true);
			try {
				if(retreatPlan != null){
					retreatPlan = (newPlan.heuristicValue > retreatPlan.heuristicValue) ? newPlan : retreatPlan;
				} else {
//					Creating initial retreatPlan
					retreatPlan = newPlan;
				}
			} catch (Exception e) {
				Debug.Log(""+e, "new plan :" + newPlan, "retreat plan : " + retreatPlan);
			}
			return retreatPlan.plannedCoords;
		} else {
//			If none of previous special statements are fulfilled then just return a new plan
			newPlan = geneticAlgorithm(
					planLength,
					populationSize,
					selectionProp,
					generations,
					eliteProp,
					mutateProb,
					mutationProp,
					doRetreat);		

			return newPlan.plannedCoords;
		}
	}

//	This function checks if the Rover should return home
//	This is either due to low battery or all objectives being fulfilled
	public boolean shouldRetreat() {

		boolean haveAllObj = true;

		for(boolean bool : objectCollected) {
			if(!bool) {
				haveAllObj = bool;
				break;
			}
		}
//		marginProp makes sure we retreat before we are in actual danger
		return ((estRetreatCost*marginProp >= remainingBattery) || (damonCollected && haveAllObj));
	}

	public PlanGA geneticAlgorithm(
			int planLength,
			int populationSize,
			double selectionProp,
			int generations,
			double eliteProp,
			double mutateProb,
			double mutationProp,
			boolean doRetreat){

		ArrayList<PlanGA> randomPopulation = new ArrayList<PlanGA>();
		ArrayList<PlanGA> selectedPopulation = new ArrayList<PlanGA>();
		ArrayList<PlanGA> newPopulation = new ArrayList<PlanGA>();
		ArrayList<PlanGA> mutatedPopulation = new ArrayList<PlanGA>();
		ArrayList<PlanGA> newSelection = new ArrayList<PlanGA>();

//		Creating a random population of paths
		randomPopulation = createRandomPopulation(planLength, populationSize, doRetreat);

//		Evaluating all paths and selecting the best for breeding
		selectedPopulation = evaluateAndSelect(randomPopulation, selectionProp, eliteProp, doRetreat);

//		Breeding new population based on selected population
		try {
			newPopulation = breed(selectedPopulation, eliteProp, populationSize);
		} catch (Exception e) {
			Debug.Log("Error in breed during doRetreat : " + e);
		}

//		Mutate population with some probability
		mutatedPopulation = mutatePopulation(newPopulation, mutateProb, mutationProp);

//		Iterate until gone through generations number of generations
		for(int i=1; i<generations; i++) {
			selectedPopulation = evaluateAndSelect(mutatedPopulation, selectionProp, eliteProp, doRetreat);
			newPopulation = breed(selectedPopulation, eliteProp, populationSize);
			mutatedPopulation = mutatePopulation(newPopulation, mutateProb, mutationProp);
		}

//		Evaluate final population
		newSelection = evaluateAndSelect(mutatedPopulation, selectionProp, eliteProp, doRetreat);

//		Return plan with highest fitness
		currentGAPlan = newSelection.get(newSelection.size()-1);

		return currentGAPlan;
	}

//	Create population of valid random moves
	public ArrayList<PlanGA> createRandomPopulation(int planLength, int populationSize, boolean doRetreat){
		ArrayList<PlanGA> populationList = new ArrayList<PlanGA>();

		for(int i=0; i<populationSize; i++) {
			PlanGA newRandomPlan = null;
			try {
				newRandomPlan = new PlanGA(
						planLength, 
						startingLocation, 
						currentLocation, 
						estimatedEnv,
						remainingBattery,
						damonCollected,
						objectCollected,
						1,
						doRetreat);
			} catch (Exception e) {
				Debug.Log("Error in createRandom Population : " + e);
			}
			populationList.add(newRandomPlan);
		}

		return populationList;
	}

	
//	 Sorts the population based on fitness and selects a subset
	public ArrayList<PlanGA> evaluateAndSelect(
			ArrayList<PlanGA> population, 
			double selectionProp, 
			double eliteProp,
			boolean doRetreat){
		double tmpValue;
		HashMap<PlanGA,Double> valueMap = new HashMap<PlanGA,Double>();
		ArrayList<PlanGA> sortedPlans = new ArrayList<PlanGA>();
		ArrayList<PlanGA> selectedPlans = new ArrayList<PlanGA>();

		for(PlanGA plan : population) {
			plan.evaluatePlan();
			tmpValue = plan.heuristicValue;
			valueMap.put(plan,tmpValue);
		}

		sortedPlans = sortByValue(valueMap);

		int eliteSize = (int) (sortedPlans.size()*eliteProp);

//		The absolut best plans are chosen as "elite" - these are guaranteed to breed
		for(int i=0; i<eliteSize; i++) {
			sortedPlans.get(i).setElite(true);
			selectedPlans.add(sortedPlans.get(i));
		}
		
		for(int i=eliteSize; i<((int) (sortedPlans.size()*selectionProp)); i++) { 
			selectedPlans.add(sortedPlans.get(i));
		}

		return selectedPlans;
	}

// Breed a new population from the selected population	
	public ArrayList<PlanGA> breed(ArrayList<PlanGA> population, double eliteProp, int populationSize){
		ArrayList<PlanGA> bredPopulation = new ArrayList<PlanGA>();
		int indexB;
		int indexA;
		int eliteSize = (int) (eliteProp*population.size());

		for(int i=0; i < (populationSize - eliteSize) ; i++) {
			indexA = new Random().nextInt(population.size());
			indexB = new Random().nextInt(population.size());

			while(indexA == indexB) {
				indexB = new Random().nextInt(population.size());
			}
			PlanGA child = null;
			try {
				PlanGA planA = population.get(indexA);
				PlanGA planACopy = new PlanGA(
						planA.plannedMovement, 
						planA.startLocation,
						planA.currentLocation,
						planA.estEnv,
						planA.remainingBattery,
						planA.damonCollected,
						planA.objectsCollected,
						planA.generationNumber,
						planA.parents,
						planA.isRetreatPlan);
				PlanGA planB = population.get(indexB);
				child = planACopy.breedPlan(planB);
			} catch (Exception e) {
				Debug.Log("Error in retreatPlan -> breedPlan : " + e);
			}
			bredPopulation.add(child);
		}

		for(int i=0; i<eliteSize; i++) {
			population.get(i).setElite(false);
			bredPopulation.add(population.get(i));
		}

		return bredPopulation;
	}

//	Creates a new population where some plans are mutated and some are not
	public ArrayList<PlanGA> mutatePopulation(ArrayList<PlanGA> population, double mutateProb, double mutationProp){
		ArrayList<PlanGA> mutatedPopulation = new ArrayList<PlanGA>();

		//		Mutate each plan in population with some probability mutationProb
		try {
			for(PlanGA plan : population) {
				boolean doMutate = (new Random().nextDouble() < mutateProb);

				if(doMutate) {
					plan.mutatePlan(mutationProp);
					mutatedPopulation.add(plan);
				} else {
					mutatedPopulation.add(plan);
				}
			}

		}
		catch (Exception e) {
			Debug.Log("Error in mutatePopulation : " + e);
		}

		return mutatedPopulation;
	}

//	Function for sorting a HashMap by its values and returning an ArrayList with the sorted keys in an ascending order
	public static ArrayList<PlanGA> sortByValue(HashMap<PlanGA, Double> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<PlanGA, Double>> list = new LinkedList<Map.Entry<PlanGA, Double>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<PlanGA, Double>>() {
			public int compare(Map.Entry<PlanGA, Double> o1, Map.Entry<PlanGA, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// put data from sorted list to arrayList
		ArrayList<PlanGA> temp2 = new ArrayList<PlanGA>();
		for (Map.Entry<PlanGA, Double> aa : list) {
			temp2.add(aa.getKey());
		}

		return temp2;
	}
}

