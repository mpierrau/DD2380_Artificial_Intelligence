import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class AntColonyRover extends Rover{
	
	public AntColonyRover(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		super(startX, startY, startingBattery, estimatedEnv);
		this.antsPath = new ArrayList<ArrayList<Point>>();
		this.rand = new Random();
		this.visitedNodes = new int[this.estimatedEnv.size][this.estimatedEnv.size];
		this.pheromonesMap = new HashMap<Integer, Double>();
		this.antPlan = new ArrayList<Point>();
		this.localObjectFoundTrigger = new boolean[super.estimatedEnv.numObjects];
		this.initialBattery = super.remainingBattery;
		for (int i = 0; i < this.localObjectFoundTrigger.length; i++) {
			this.localObjectFoundTrigger[i] = true;
		}
		
		for(int i=0; i< this.estimatedEnv.size; i++) {
			for(int j=0; j<this.estimatedEnv.size; j++) {
				Point p = new Point(i,j);
				ArrayList<Point> neighbour = super.estimatedEnv.getNeighbors(p);
				for (int k = 0; k<neighbour.size(); k++) {
					String s = "" + i + j + neighbour.get(k).x + neighbour.get(k).y;
					int hashcode = Integer.valueOf(s);
					this.pheromonesMap.put(hashcode, 1.0/this.antPheromoneInit);
				}
			}
		}
	}

	// hyperparameters for ACO. subject to change
//	private double costOfPath = 1.0;
	private double alpha = 1;
	private double beta = 3;
	private double evaporationFactor = 0.5;
	private double ants = 50;
	private int iterations = 100;
	private int roverSteps = 20;
	private int[][] visitedNodes;
	private boolean localDamonFoundTrigger = true;
	private boolean[] localObjectFoundTrigger;
	
	// state maintenance variables
//	private double [][][] pheromones;
	private Map<Integer, Double> pheromonesMap;
	public ArrayList<ArrayList<Point>> antsPath;
	private Random rand; 
	private double chance;
	private ArrayList<Point> antPlan;
	private boolean everythingCollected = false;
	private boolean reachedHome = false;
	private double initialBattery;
	private double antPheromoneInit = 256;
//	private int simulationSteps = 0;
//	private int[][] roverVisit;
	
	@Override
	public boolean shouldUpdate() {
		
		Map<Point, Double> nextPossiblePosition = nextPositionProb(super.currentLocation);

		for(Point pt: nextPossiblePosition.keySet()) {
			if(super.estimatedEnv.pdfDamon[pt.x][pt.y] == 1.0) {
				this.antPlan.clear();
				this.antPlan.add(pt);
				break;
			}
			for(int i = 0; i < super.estimatedEnv.numObjects; i++) {
				if (super.estimatedEnv.pdfObjects[i][pt.x][pt.y] == 1) {
					this.antPlan.clear();
					this.antPlan.add(pt);
					break;
				}
			}			
			if(this.everythingCollected) {
				if(super.startingLocation.x == pt.x && super.startingLocation.y == pt.y) {
					this.antPlan.clear();
					this.antPlan.add(pt);
					this.reachedHome = true;
				}
			}
		}
		if (this.antPlan.size() > 0)
			return false;
		return true;
	}
	
	@Override
	public ArrayList<Point> updatePlan() {
		
		try {
			if(super.remainingBattery <= 0.5 * this.initialBattery) {
				if (Debug.ON) Debug.Log("going home");
				resetPheromones();
				this.everythingCollected = true;
			}
			if(this.reachedHome)
				return new ArrayList<Point>();
			
			Map<Point, Double> nextPossiblePosition;
			
			double sum = 0;
			
			if (super.damonCollected && this.localDamonFoundTrigger) {
				resetPheromones();
				this.localDamonFoundTrigger = false;
			}
			
			for (int i = 0; i < this.localObjectFoundTrigger.length; i++) {
				if( this.localObjectFoundTrigger[i] && super.objectCollected[i] && super.damonCollected) {
					resetPheromones();
					this.localObjectFoundTrigger[i] = false;
				}
			}
			
			this.everythingCollected = checkCollectedObjectAndDamon();

			for(int i = 0; i < iterations; i++) {
				
				initialiseAntsPath(super.currentLocation);
				
				for(int a = 0; a < ants; a++) {
					double batteryLeft = super.remainingBattery;
					
					for(int t = 0; t < this.visitedNodes.length; t++)
						Arrays.fill(this.visitedNodes[t], 0);
					this.visitedNodes[super.currentLocation.x][super.currentLocation.y] = 1;

					while(batteryLeft >= super.remainingBattery * 0.5) {
						ArrayList<Point> currentAnt = this.antsPath.get(a);
						Point currentAntLocation = currentAnt.get(currentAnt.size()-1);						
						nextPossiblePosition = nextPositionProb(currentAntLocation);
						boolean present = false;
						Point nextMove = new Point();
						boolean exhaustedAllMoves = false;
						
						while(!present) {
							chance = rand.nextDouble();					
							sum = 0;
							
							for(Point pt : nextPossiblePosition.keySet()) {
								sum += nextPossiblePosition.get(pt);
								if (nextPossiblePosition.size() == 1) {
									nextMove = pt;
									present = true;
									break;
								}
								if (chance <= sum) {
									nextMove = pt;
									break;
								}
							}
							if (this.visitedNodes[nextMove.x][nextMove.y] == 0) {
								present = true;
								this.visitedNodes[nextMove.x][nextMove.y] = 1;
							}
							else {
								nextPossiblePosition.remove(nextMove);
								if (nextPossiblePosition.size() == 0) {
									exhaustedAllMoves = true;
									break;
								}
								double normFactor = nextPossiblePosition.values().stream().mapToDouble(Double::doubleValue).sum();
								if (normFactor == 0.0) {
									if (Debug.ON) Debug.Log("size: " + nextPossiblePosition.size() + " norm factor: " + normFactor);
									if(nextPossiblePosition.size() == 2)
										if (Debug.ON) Debug.Log("value 1: " + nextPossiblePosition);
									exhaustedAllMoves = true;
									break;
								}
								for(Point pt : nextPossiblePosition.keySet()) {
									double temp = nextPossiblePosition.get(pt);
									nextPossiblePosition.replace(pt, temp/normFactor);
								}
							}
								
						}
						if (exhaustedAllMoves == true) {
//							Debug.Log("exhausted all moves");
							break;
						}
						batteryLeft -= super.estimatedEnv.getCost(currentAntLocation, nextMove);
						if (estimatedEnv.areNeighbors(nextMove, currentAntLocation))
							this.antsPath.get(a).add(nextMove);
					}
				}
				updatePheromones();			
			}
			updateCurrentPlan(super.currentLocation);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Debug.ON) Debug.Log("plan length: " + this.antPlan.size());
		return this.antPlan;
	}
	
	// this function takes current point as argument
	// And returns the probability of possible moves
	// Intermediate values and function calls:
	// gets the next valid moves. And use pheromone and prior to compute probability
	public Map<Point, Double> nextPositionProb(Point p) {
		
		double cost = 0;
		double prob = 0.0;
		double normalisingFactorPrior = 0.0;
		double normalisingFactorProb = 0.0;
		
		Map<Point, Double> possibleMoves = new HashMap<Point,Double>();
		
		ArrayList<Point> nextValidMoves = super.estimatedEnv.getNeighbors(p);
		
		// get costs for each move.
		for(int i = 0; i < nextValidMoves.size(); i++) {
			cost = super.estimatedEnv.getCost(p, nextValidMoves.get(i));
			possibleMoves.put(nextValidMoves.get(i), 1.0/cost);
			normalisingFactorPrior += 1.0/cost;
		}
		
		// normalise it with all possible moves
		for(Point pt : possibleMoves.keySet()) {
			double eta = possibleMoves.get(pt)/normalisingFactorPrior;
			String s = "" + p.x + p.y + pt.x + pt.y;
			int hashcode = Integer.valueOf(s);
			prob = Math.pow(this.pheromonesMap.get(hashcode), this.alpha) * Math.pow(eta, this.beta);
			normalisingFactorProb += prob;
			possibleMoves.replace(pt, prob);
		}
		
		for(Point pt : possibleMoves.keySet()) {
			double temp = possibleMoves.get(pt)/normalisingFactorProb;
			possibleMoves.replace(pt, temp);
			
		}	
		return possibleMoves;
	}
	
	// this function updates the pheromone matrix
	public void updatePheromones() {
		// first add expression for evaporation of pheromones
		for(int key : this.pheromonesMap.keySet()) {
			
			double pheromoneAfterEvap = (1 - evaporationFactor) * this.pheromonesMap.get(key);
			pheromoneAfterEvap = pheromoneAfterEvap > 0.0 ? pheromoneAfterEvap : 0.0;
			this.pheromonesMap.replace(key, pheromoneAfterEvap);
		}
		
		// additive expression for the path taken by ants.
		
		for(int i = 0; i < this.ants; i++) {
			ArrayList<Point> currAnt = this.antsPath.get(i);
			Point p1,p2;
			double pdfGain = 0.0;
			
			if(this.pheromonesMap.values().stream().mapToDouble(Double::doubleValue).sum() < Math.pow(Math.E,-30)) {
				if (Debug.ON) Debug.Log("resetting due to underflow");
				this.resetPheromones();
			}
			// heuristic for maximising path that leads to Damon
			try {
				if(!this.everythingCollected) {
					for (int j = 1; j < currAnt.size(); j++) {
						p1 = currAnt.get(j);
						int count = 0;
						if(!super.damonCollected)
							pdfGain += super.estimatedEnv.pdfDamon[p1.x][p1.y]/currAnt.size();
						else {
							for(int k = 0; k < super.estimatedEnv.numObjects; k++) {
								if(super.objectCollected[k] == false) {
									pdfGain += super.estimatedEnv.pdfObjects[k][p1.x][p1.y]/currAnt.size();								
									count += 1;
								}
							}
						}
					}
				}
				else {
					Point init = currAnt.get(0);
//					double initDist = Math.hypot(Math.abs(init.x - super.startingLocation.x), Math.abs(init.y - super.startingLocation.y));
					double dist = Double.MAX_VALUE;
					for (int j = 1; j < currAnt.size(); j++) {
						Point from = currAnt.get(j);
//						Point to = currAnt.get(j+1);
						double tempDist = Math.hypot(Math.abs(from.x - super.startingLocation.x), Math.abs(from.y - super.startingLocation.y));
						if(tempDist < dist) {
							dist = tempDist;
						}
					}

					if(dist == 0.0)
						pdfGain = Integer.MAX_VALUE;
					else
						pdfGain = 1.0/dist;
				}

//				Debug.Log("sum of pheromones: " + this.pheromonesMap.values().stream().mapToDouble(Double::doubleValue).sum());

				for (int j = 0; j < currAnt.size() - 1; j++) {
					p1 = currAnt.get(j);
					p2 = currAnt.get(j+1);
					String s = "" + p1.x + p1.y + p2.x + p2.y;
					int hashCode = Integer.valueOf(s);
					double temp = this.pheromonesMap.get(hashCode) + pdfGain;
					this.pheromonesMap.replace(hashCode, temp);
				}
			} catch (Exception e) {
				Debug.Log("pdf Gain: " + pdfGain + " ant path length: " + currAnt.size());
				e.printStackTrace();
			}
			
		}
	}
	
	public void initialiseAntsPath(Point p) {
		
		this.antsPath.clear();
		for(int i=0; i<this.ants; i++) {
			ArrayList<Point> a = new ArrayList<Point>();
			a.add(p);
			this.antsPath.add(a);
		}
	}
	
	public void updateCurrentPlan(Point p) {
		Map<Point, Double> nextPossiblePosition;
		double max = 0.0;
		boolean foundDamon = false;
		Point tempPoint = p;
		Point prevPoint = p;
		this.antPlan.clear();
		
		for(int i = 0; i < this.roverSteps; i++){
			max = 0.0;
			nextPossiblePosition = nextPositionProb(tempPoint);
			
			for(Point pt: nextPossiblePosition.keySet()) {
				if(super.estimatedEnv.pdfDamon[pt.x][pt.y] == 1.0) {
					tempPoint = pt;
					foundDamon = true;
					break;
				}
				if(pt == prevPoint) {
					continue;
				}
				if(nextPossiblePosition.get(pt) > max) {
					max = nextPossiblePosition.get(pt);
					tempPoint = pt;
				}
			}
			this.antPlan.add(tempPoint);

			if(foundDamon == true)
				break;
		}
	}
	
	public void resetPheromones() {
		if (Debug.ON) Debug.Log("reset pheromones called");
		for(int a : this.pheromonesMap.keySet()) {
			this.pheromonesMap.replace(a, 1.0/this.antPheromoneInit);
		}
	}
	
	public boolean checkCollectedObjectAndDamon() {
		boolean foundObjects = true;
		for(int i = 0; i < super.estimatedEnv.numObjects; i++) {
			foundObjects  = foundObjects && super.objectCollected[i];
		}
		
		return foundObjects && super.damonCollected;
	}
	
	public ArrayList<Point> goingHome()
	{	Point curr = new Point(this.currentLocation.x, this.currentLocation.y);
		ArrayList<Point> u = new ArrayList<>();
		Point home = super.startingLocation;
		if (curr.x - super.startingLocation.x != 0) {

			int a;
			if (curr.x > home.x) {
				a = curr.x - 1;
			} else {
				a = curr.x + 1;
			}
			Point p = new Point(a, curr.y);

			u.add(p);

		} else if (curr.y - home.y != 0) {
			int a;
			if (curr.y > home.y) {
				a = curr.y - 1;
			} else {
				a = curr.y + 1;
			}
			Point p = new Point(curr.x, a);

			u.add(p);
		}
		return u;
	}

	public HashMap<String, String> getHyperparameters() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("alpha", ""+alpha);
		params.put("beta", ""+beta);
		params.put("evaporationFactor", ""+evaporationFactor);
		params.put("ants", ""+ants);
		params.put("iterations", ""+iterations);
		params.put("roverSteps", ""+roverSteps);
		return params;
	}
		
}

