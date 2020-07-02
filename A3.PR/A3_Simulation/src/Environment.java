import java.awt.Point;
import java.util.ArrayList;
import java.util.function.Predicate;

public class Environment {
	// Basically a fancy tensor
	public int size, numObjects;
	private double minTravelCost;
	public double[][] terrain, pdfDamon;
	public double[][][] pdfObjects;
	
	// Predicate used to filter out points outside of the terrain bounds
	public final Predicate<Point> edgeFilter = p -> (p.x < 0 || p.y < 0 || p.x >= size || p.y >= size);
	
	// Creates an empty environment (just initializes all arrays to 0.0)
	public Environment(int size, int numObjects, double minTravelCost) {
		this.size = size;
		this.numObjects = numObjects;
		this.minTravelCost = minTravelCost;
		this.terrain = new double[size][size];
		this.pdfDamon = new double[size][size];
		this.pdfObjects = new double[numObjects][size][size];
	}
	
	// Returns the cost of travelling from point "from" to point "to"
	public double getCost(Point from, Point to) {
		// Points that are not neighbors are given infinite cost
		if (!areNeighbors(from, to))
			return Double.POSITIVE_INFINITY;
		return Math.abs(terrain[to.x][to.y] - terrain[from.x][from.y]) + minTravelCost;
	}
	
	// Getter for private minTravelCost
	public double getMinTravelCost() {
		return minTravelCost;
	}
	
	public ArrayList<Point> getNeighbors(Point p){
		ArrayList<Point> ret = new ArrayList<Point>();
		ret.add(new Point(p.x + 1, p.y));
		ret.add(new Point(p.x - 1, p.y));
		ret.add(new Point(p.x, p.y + 1));
		ret.add(new Point(p.x, p.y - 1));
		ret.removeIf(edgeFilter);
		return ret;
	}
	
	// Returns true if p1 and p2 are neighbors (if their L1 distance is 1)
	public boolean areNeighbors(Point p1, Point p2) {
		return (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) == 1);
	}
}
