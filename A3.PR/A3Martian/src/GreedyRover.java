import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

public class GreedyRover extends Rover {
	private ArrayList<Point> visited;
	private double[][] Damon, homePdf;
	private int currentObj;
	private double[][][] Object;
	private final Predicate<Point> hasBeenVisited = p -> (visited.contains(p));
	private boolean goHome;
	private double threshold;
	private Point lastLocation;


	public GreedyRover(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		super(startX, startY, startingBattery, estimatedEnv);
		visited = new ArrayList<Point>();
		Damon = copy2dArray(estimatedEnv.pdfDamon);
		homePdf=new double[Damon.length][Damon.length];
		EnvironmentBuilder.setGaussianPdf(homePdf, this.currentLocation.x, this.currentLocation.y, 3.0);
		goHome = false;
		Object = copy3dArray(estimatedEnv.pdfObjects);
		currentObj = 0;
		threshold = (estimatedEnv.size / 2.0);
		lastLocation=this.currentLocation;
	}
	
	
	// Currently does not return any parameters.
	// What about the threshold above? What is that?
	public HashMap<String, String> getHyperparameters(){
		return new HashMap<String, String>();
	}


	// Creates a deep copy of a double[][]
	public double[][] copy2dArray(double arrp[][]) {
		// Allocate new double[][]
		double arr2[][] = new double[arrp.length][arrp[1].length];

		// Fill new double[][][] with provided values
		for (int i = 0; i < arrp.length; i++)
			for (int j = 0; j < arrp[1].length; j++)
				arr2[i][j] = arrp[i][j];
		return arr2;
	}


	// Creates a deep copy of a double[][][]
	public double[][][] copy3dArray(double arrp[][][]) {
		// Allocate new double[][][]
		double arr2[][][] = new double[arrp.length][arrp[0].length][arrp[0][0].length];

		// Fill new double[][][] with the provided values
		for (int i = 0; i < arrp.length; i++)
			for (int j = 0; j < arrp[0].length; j++)
				for (int k = 0; k < arrp[0][0].length; k++)
					arr2[i][j][k] = arrp[i][j][k];

		return arr2;
	}


	// This algorithm will always update.
	public boolean shouldUpdate() {
		return true;
	}


	// Prints the values of an array
	public void printArray(double arrp[][]) {
		for (int j = 0; j < arrp.length; j++) {
			// Construct one line of output from a row of the matrix
			String line = "";
			for (int i = 0; i < arrp[0].length; i++)
				line += arrp[i][j] + " ";

			// Log that line
			Debug.Log(line);
		}
	}


	public ArrayList<Point> goingHome()
	{
		visited.clear();
		ArrayList<Point> u=new ArrayList<Point>();
		ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
		options.removeIf(hasBeenVisited);
		Point toMove = this.currentLocation;
		double moveScore = Double.MIN_VALUE;
		for (int i = 0; i < options.size(); i++) {
			if(!options.get(i).equals(lastLocation))
			{
				Point p = options.get(i);
				int x = p.x;
				int y = p.y;
			
				double testScore = 2*homePdf[x][y]/estimatedEnv.getCost(this.currentLocation, options.get(i));
				if (testScore > moveScore) {
					moveScore = testScore;
					toMove = p;
				}
			}
			
		}
		u.add(toMove);
		return u;
		
	}


	public ArrayList<Point> updatePlan() {

		if (currentObj < estimatedEnv.numObjects && objectCollected[currentObj] == true) {

			currentObj++;
			visited.clear();

		}

		if (this.remainingBattery < threshold) {
			goHome = true;

		}
		if (currentObj == (estimatedEnv.numObjects)) {
			goHome = true;
		}

		visited.add(this.currentLocation);
		ArrayList<Point> u = new ArrayList<>();

		if (damonCollected == true & this.currentLocation.equals(startingLocation)) {
			ArrayList<Point> empty = new ArrayList<Point>();
			return empty;
		}

		if (goHome) {
			u=goingHome();
		} else if (!damonCollected) {
			ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
			options.removeIf(hasBeenVisited);
			Point toMove = this.currentLocation;
			double moveScore = Double.MIN_VALUE;
			for (int i = 0; i < options.size(); i++) {
				Point p = options.get(i);
				int x = p.x;
				int y = p.y;
				if (estimatedEnv.pdfDamon[x][y] == 1.0) {
					toMove = p;
					break;
				} else {
					double testScore = Damon[x][y]/estimatedEnv.getCost(this.currentLocation, options.get(i));
					if (testScore > moveScore) {
						moveScore = testScore;
						toMove = p;
					}
				}
			}
			if(estimatedEnv.getCost(this.currentLocation, toMove)>999999) {
				u=goingHome();
			} else {
				u.add(toMove);
			}
		} else if (objectCollected[currentObj] == false && currentObj < estimatedEnv.numObjects) {
			ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
			options.removeIf(hasBeenVisited);
			Point toMove = this.currentLocation;
			double moveScore = Double.MIN_VALUE;
			for (int i = 0; i < options.size(); i++) {
				Point p = options.get(i);
				int x = p.x;
				int y = p.y;
				if (estimatedEnv.pdfObjects[currentObj][x][y] == 1.0) {
					toMove = p;
					break;
				} else {
					double testScore = Object[currentObj][x][y]/estimatedEnv.getCost(this.currentLocation, options.get(i));
					if (testScore > moveScore) {
						moveScore = testScore;
						toMove = p;
					}
				}
			}

			if(estimatedEnv.getCost(this.currentLocation, toMove)>999999) {
				u=goingHome();
			} else {
				u.add(toMove);
			}
		} else {
			u=goingHome();
		}

		lastLocation=this.currentLocation;
		return u;

	}

}
