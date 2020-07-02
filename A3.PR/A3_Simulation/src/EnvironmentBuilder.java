import java.awt.Point;
import java.util.Random;

// This class contains all of the code for creating and changing
// environments. We can implement new kinds of terrain and noise here.
public class EnvironmentBuilder {
	private static Random rand = new Random();
	
	
	public static void setSeed(long seed) {
		rand.setSeed(seed);
	}
	
	// Creates a deep copy of the provided environment and adds some additive i.i.d. noise to the terrain
	public static Environment copyWithNoisyTerrain(Environment env, double noise) {
		Environment copy = new Environment(env.size, env.numObjects, env.getMinTravelCost());
		for(int i = 0; i < env.size; i++) {
			for (int j = 0; j < env.size; j++) {
				copy.terrain[i][j] = env.terrain[i][j] + (rand.nextDouble() * (noise) * 2) - noise;
				copy.pdfDamon[i][j] = env.pdfDamon[i][j];
				for (int obj = 0; obj < env.numObjects; obj++) {
					copy.pdfObjects[obj][i][j] = env.pdfObjects[obj][i][j];
				}
			}
		}
		return copy;
	}
	
	
	// Fills a double[][] with i.i.d. noise ~ U[min, max]
	public static void addNoise(double[][] vals, double min, double max) {
		for(int i = 0; i < vals.length; i++) {
			for(int j = 0; j < vals[0].length; j++) {
				vals[i][j] += min + (rand.nextDouble() * (max - min));
			}
		}
	}
	
	
	// Sets all values in a double[][] to zero
	public static void clear(double[][] vals) {
		for (int i = 0; i < vals.length; i++) {
			for (int j = 0; j < vals[0].length; j++) {
				vals[i][j] = 0.0;
			}
		}
	}
	
	
	// Renormalizes a PDF given a known current total
	public static void renormalize(double[][] vals, double total) {
		for (int i = 0; i < vals.length; i++) {
			for (int j = 0; j < vals[0].length; j++) {
				vals[i][j] /= total;
			}
		}
	}
	
	
	// Fills a double[][] with a noisy PDF
	// This is equivalent to setNoise(pdf, min, max) followed by normalizing the pdf
	public static void setNoisyPdf(double[][] pdf, double min, double max) {
		double total = 0.0;
		
		// Assign unnormalized quantities
		for(int i = 0; i < pdf.length; i++) {
			for(int j = 0; j < pdf[0].length; j++) {
				pdf[i][j] = min + (rand.nextDouble() * (max - min));
				total += pdf[i][j];
			}
		}
		
		//Renormalize using the total density
		for(int i = 0; i < pdf.length; i++) {
			for(int j = 0; j < pdf[0].length; j++) {
				pdf[i][j] = pdf[i][j] / total;
			}
		}
	}
	
	
	// Fills a double[][] with a 2D gaussian PDF centered at
	// a random point with a variance between minV and maxV
	public static void setRandomGaussianPdf(double[][] pdf, double minV, double maxV) {
		// Randomly sample parameters for the PDF
		double var = minV + rand.nextDouble() * (maxV - minV);
		int centerX = rand.nextInt(pdf.length), centerY = rand.nextInt(pdf[0].length);
		
		setGaussianPdf(pdf, centerX, centerY, var);
	}
	
	
	// Fills a double[][] with a 2D gaussian PDF centered at
	// a given point (cX,cY) and with a given variance (var).
	public static void setGaussianPdf(double[][] pdf, int cX, int cY, double var) {
		double total = 0.0;
		
		// Assign unnormalized quantities based on the distance from the gaussian's center
		for(int x = 0; x < pdf.length; x++) {
			for(int y = 0; y < pdf[0].length; y++) {
				pdf[x][y] = Math.exp(-0.5 * ((cX - x) * (cX - x) + (cY - y) * (cY - y)) / var);
				total += pdf[x][y];
			}
		}
		
		// Renormalize using the total density
		for(int x = 0; x < pdf.length; x++) {
			for(int y = 0; y < pdf[0].length; y++) {
				pdf[x][y] = pdf[x][y] / total;
			}
		}
	}
	
	
	// Adds a hill to an existing double[][]
	public static void addHill(double[][] arr, int cX, int cY, double spread, double height) {
		// Add values scaled by gaussian distribution about the center point
		for(int x = 0; x < arr.length; x++) {
			for(int y = 0; y < arr[0].length; y++) {
				arr[x][y] += height * Math.exp(-0.5 * ((cX - x) * (cX - x) + (cY - y) * (cY - y)) / spread);
			}
		}
	}
	
	
	// Samples a point from a 2D PDF using the CDF along a linear ordering
	public static Point sampleFromPdf(double[][] pdf) {
		// Sample a threshold in [0.0, 1.0)
		double threshold = rand.nextDouble(), sum = 0;
		
		// Search for the point where the CDF crosses the sampled threshold
		for (int x = 0; x < pdf.length; x++) {
			for (int y = 0; y < pdf[0].length; y++) {
				sum += pdf[x][y];
				if(sum > threshold)
					return new Point(x, y);
			}
		}
		
		// If no point found, there may be a problem with the PDF provided
		System.err.println("Attempted to sample from a PDF that may not be valid.");
		return null;
	}
	
}
