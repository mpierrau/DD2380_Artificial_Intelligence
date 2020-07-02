import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;


// This is a class for the Plan determining how the rover should move. It uses a Genetic Algorithm-approach.
// The 

public class Plan {
	
	public ArrayList<Point> plannedMovement = new ArrayList<Point>();
	public ArrayList<Point> plannedCoords = new ArrayList<Point>();
	public Point currentLocation;
	public Point startLocation;
	public double heuristicValue;
	public Environment estimatedEnvironment;
	public Point damonLocation;
	public ArrayList<Point> objectLocations;
	int maxSteps;
	int haveDamon = 0;
	int objectsFound = 0;
	
	// These are hyperparameters which can be tuned after preference
	double w1=100 , w2=10 , w3=-1 , w4=-1000;
	double loLimBreedProp = 0.4;
	double hiLimBreedProp = 0.6;
	
	public Plan(
			int planLength,
			Point startLocation, 
			Point currentLocation, 
			Environment estimatedEnvironment,
			Point damonLocation,
			ArrayList<Point> objectLocations) {
		
		this.maxSteps = planLength;
		this.currentLocation = currentLocation;
		this.startLocation = startLocation;
		this.estimatedEnvironment = estimatedEnvironment;
		this.damonLocation = damonLocation;
		this.objectLocations = objectLocations;
		randomPlan();
		
//		System.out.println(plannedMovement);
	}
	
	public Plan(
			ArrayList<Point> givenMoves,
			Point startLocation, 
			Point currentLocation, 
			Environment estimatedEnvironment,
			Point damonLocation,
			ArrayList<Point> objectLocations) {
		
		this.maxSteps = givenMoves.size();
		this.currentLocation = currentLocation;
		this.startLocation = startLocation;
		this.estimatedEnvironment = estimatedEnvironment;
		this.damonLocation = damonLocation;
		this.objectLocations = objectLocations;
		this.plannedMovement = givenMoves;
		
		computeNewCoords();
	}
	
	public int size() {
		return plannedMovement.size();
	}
	
	public Point getNextMove() {
		return plannedMovement.get(0);
	}
	
	public Point pullNextMove() {
		Point nextMove = plannedMovement.get(0);
		plannedMovement.remove(0);
		System.out.println("Pulling next move = " + nextMove);
		return nextMove;
	}
	
	public boolean equals(Plan plan) {
		if(plan == this) return true;
		if(!(plan instanceof Plan)) {
			return false;
		}
		for(int i=0; i<this.plannedMovement.size();i++){
			if(!(plan.plannedMovement.get(i)).equals(plannedMovement.get(i))){
				return false;
			}
		}
		
		return true;
	}
			
	
	public Point computeNewCoord(Point currentCoord, Point nextMove) {
		// computeNewCoord calculates the rovers new coordinates after performing nextMove from position currentCoord
		
		return new Point(
				currentCoord.x + nextMove.x,
				currentCoord.y + nextMove.y);
	}
	
	public void randomPlan() {
		// randomPlan creates a random plan of length maxSteps consisting of valid steps
		
		ArrayList<Point> newMovements = new ArrayList<Point>();
		ArrayList<Point> newCoords = new ArrayList<Point>();
		
		newMovements.add(getValidMove(currentLocation, new Point(0,0)));
		newCoords.add(computeNewCoord(currentLocation, newMovements.get(0)));
		for(int i=1; i<maxSteps; i++) {
			
			Point movement = getValidMove(newCoords.get(i-1), newMovements.get(i-1));
			newMovements.add(movement);
			newCoords.add(computeNewCoord(newCoords.get(i-1), movement));
		}
		
		plannedMovement = newMovements;
		plannedCoords = newCoords;
		
		evaluatePlan();
//		System.out.println("Heuristic value: " + heuristicValue);
	}
	
	public Point getValidMove(Point location, Point lastMove) {
		// Returns a random valid move based on the current location
		// A valid move is one that does not result in the rover running off the map
		// or is not the reverse of the previous move
		
		ArrayList<Point> validMoves = new ArrayList<Point>();
		
		int edgeIndex = estimatedEnvironment.size-1;
		
		validMoves.add(new Point(1,0));
		validMoves.add(new Point(-1,0));
		validMoves.add(new Point(0,1));
		validMoves.add(new Point(0,-1));
		
		for(int i=0; i<validMoves.size(); i++) {
			if(validMoves.get(i).equals(reverseMove(lastMove))) {
				validMoves.remove(i);
			}
		}

		//	 	Below code allows rover to run diagonally

//		int[] possibleY;
//		int[] possibleX;
		
		
//		if(location.x == edgeIndex) {
//			possibleX = new int[] {-1,0};
//		} else if(location.x == 0) {
//			possibleX = new int[] {0,1};
//		} else {
//			possibleX = new int[] {-1,0,1};	
//		}
//		
//		if(location.y == edgeIndex) {
//			possibleY = new int[] {-1,0};
//		} else if(location.y == 0) {
//			possibleY = new int[] {0,1};
//		} else {
//			possibleY = new int[] {-1,0,1};
//		}
//		
//		for(int i : possibleX) {
//			for(int j : possibleY) {
//				validMoves.add(new Point(i,j));
//			}
//		}
		
		
		if(location.x == edgeIndex) {
			validMoves.remove(0);
		} else if(location.x == 0) {
			validMoves.remove(1);
		}
		if(location.y == edgeIndex) {
			validMoves.remove(2);
		} else if(location.y == 0) {
			validMoves.remove(3);
		}
		
		int chosenIndex = new Random().nextInt(validMoves.size());
		
		return validMoves.get(chosenIndex);
	}
	
	public void evaluatePlan() {
//		This function calculates our "utility function" of the current plan.
//		The value is based on the acquisition of Matt Damon, the number of objects found,
//		the amount of used power and the distance to home from the current position.
//		w1, ..., w4 are weights for the different objectives. 
//		We want to maximize the heuristic.
		
		double usedPower = estimatedEnvironment.terrain[currentLocation.x][currentLocation.y] - 
				estimatedEnvironment.terrain[plannedCoords.get(0).x][plannedCoords.get(0).y];
		
		for(int i=1; i<plannedCoords.size(); i++) {
			
			Point thisCoord = plannedCoords.get(i);
			Point prevCoord = plannedCoords.get(i-1);
			
			foundDamon(thisCoord);
			foundObject(thisCoord,i);
			
			usedPower += estimatedEnvironment.terrain[prevCoord.x][prevCoord.y] - 
					estimatedEnvironment.terrain[thisCoord.x][thisCoord.y]; 
		}
		
		double distanceHome =  plannedCoords.get(plannedCoords.size()-1).distance(startLocation);
		
		heuristicValue = w1*haveDamon + w2*objectsFound + w3*usedPower + w4*distanceHome;
	}
	
	public void foundDamon(Point coord) {
		if(haveDamon == 0 & damonLocation.equals(coord)) {
			this.haveDamon = 1;
		}
	}
	
	public void foundObject(Point coord, int index) {
		if(objectLocations.contains(coord)) {
			objectsFound++;
			objectLocations.set(index, null);
		}
	}

	public Point reverseMove(Point move) {
		return new Point(move.x*(-1),move.y*(-1));
	}
	
	public Plan breedPlan(Plan planA) {
//		This function creates a new plan from this.plan and planA
//		This can be done in different ways but here we randomly select
//		a part of this.plan to be replaced with the corresponding part of planA.
		
		ArrayList<Point> newPlan = new ArrayList<Point>();
		int cutSize = (int) (loLimBreedProp*this.size() + new Random().nextInt((int) (hiLimBreedProp*this.size())));
		int cutStart = new Random().nextInt(this.size() - cutSize);
		
		for(int i=0; i<cutStart; i++) {
			newPlan.add(this.plannedMovement.get(i));
		}
		for(int i=cutStart; i<cutStart+cutSize; i++) {
			newPlan.add(planA.plannedMovement.get(i));
		}
		for(int i=cutStart+cutSize; i<this.size(); i++) {
			newPlan.add(this.plannedMovement.get(i));
		}
		
		return new Plan(newPlan, startLocation, currentLocation,
				estimatedEnvironment, damonLocation, objectLocations);
	}
	
	public void computeNewCoords() {
		// computeNewCoord calculates the new coordinates after performing nextMove from position currentCoord
		
		plannedCoords.add(new Point(currentLocation.x + plannedMovement.get(0).x,currentLocation.y + plannedMovement.get(0).y));
		
		for(int i=1; i < plannedMovement.size(); i++) {
				plannedCoords.add(new Point(
						plannedCoords.get(i-1).x + plannedMovement.get(i).x,
						plannedCoords.get(i-1).y + plannedMovement.get(i).y));
		}
	}	
}