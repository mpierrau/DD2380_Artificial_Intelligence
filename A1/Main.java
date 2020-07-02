import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		// hmm0();
		// hmm1();
		// hmm2();
		//hmm3();
		// hmmc();
		//hmm_uniform();
		//hmm_diag();
		//hmm_close();
		hmm_different();
	}

	public static void hmm_different(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Mangemange\\OneDrive\\Dokument\\KTH\\2019HT\\DD2380 Artificial Intelligence\\Labs\\DD2380-master\\A1\\Test data\\hmm_c_N10000.in"));
			String[] Obs = reader.readLine().split(" ");
			int nonOverfit = 10000;
			System.out.println("Result for samplesize: " + nonOverfit);
			int[] training = new int[Obs.length-1]; //10 000 training data
			for (int i = 0; i < nonOverfit; i++) {
				training[i] = Integer.parseInt(Obs[i+1]);
			}
			reader.close();
			reader = new BufferedReader(new FileReader("C:\\Users\\Mangemange\\OneDrive\\Dokument\\KTH\\2019HT\\DD2380 Artificial Intelligence\\Labs\\DD2380-master\\A1\\Test data\\hmm_c_N1000.in"));
			Obs = reader.readLine().split(" ");
			
			int[] test = new int[Obs.length - 1]; //define new array for training data			

			for (int i = 0; i < Obs.length-1; i++) {
				test[i] = Integer.parseInt(Obs[i+1]);
			}

			reader.close();
			Matrix trueA = new Matrix("3 3 0.7 0.05 0.25 0.1 0.8 0.1 0.2 0.3 0.5");
			Matrix trueB = new Matrix("3 4 0.7 0.2 0.05 0.05 0.1 0.4 0.3 0.2 0.0 0.1 0.2 0.7");
			Matrix truePI = new Matrix("1 3 1.0 0.0 0.0");

			Matrix A = new Matrix("3 3 0.54 0.26 0.20 0.19 0.53 0.28 0.22 0.18 0.6");
			Matrix B = new Matrix("3 4 0.5 0.2 0.11 0.19 0.22 0.28 0.23 0.27 0.19 0.21 0.15 0.45");
			Matrix PI = new Matrix("1 3 0.3 0.2 0.5");

			//HMM hmm = new HMM(3,4);
			
			HMM hmm = new HMM(A,B,PI);

			hmm.alphaTest(training,true);
			// Below is the BW-algorithm
			double logProb = Integer.MIN_VALUE + 1;
			int maxIters = 50;
			double oldLogProb = Integer.MIN_VALUE;
			int iter = 1;
			double logLim = 0.0001;
			double ratio = 0;
			boolean ratioCond = true;
			while ((iter < maxIters) && (logProb > oldLogProb) && ratioCond) {

				hmm.reEstimate(training);

				oldLogProb = logProb;
				logProb = hmm.getLogProb();
				ratio = Math.abs((logProb - oldLogProb) / oldLogProb);
				ratioCond = (ratio > logLim);
				iter++;

				if(!ratioCond){
					System.out.println("ratioCond");
				} else if (logProb < oldLogProb){
					System.out.println("logProb > oldLogProb");
					System.out.println("oldLogProv = " + oldLogProb);
					System.out.println("logProb = " + logProb);
				} else if (iter >= maxIters){
					System.out.println("iter > maxIters");
				}
			}
			System.out.println("oldLogProb : " + oldLogProb);
			System.out.println("logProb : " + logProb);
			System.out.println("Iterations: " + iter);
			System.out.println("LogLikelihood ratio: " + ratio);
			System.out.println(hmm.getA());
			System.out.println(hmm.getB());
			System.out.println(hmm.getPI());
			System.out.println("Matrix variance for A: " + hmm.getA().compareVar(trueA));
			System.out.println("Matrix max difference for A: " + hmm.getA().compareMax(trueA));
			System.out.println("Matrix variance for B: " + hmm.getB().compareVar(trueB));
			System.out.println("Matrix max difference for B: " + hmm.getB().compareMax(trueB));
			System.out.println("Matrix variance for PI: " + hmm.getPI().compareVar(truePI));
			System.out.println("Matrix max difference for PI: " + hmm.getPI().compareMax(truePI));


			int[] testarray = new int[1];
			testarray[0] = 0;
			int prediction;
			int nCorrect = 0;
			ArrayList<Integer> predArray = new ArrayList<>();
			for(int i=0; i<Obs.length-2; i++){
				testarray = extendArray(testarray,test[i+1]);
				prediction = hmm.nextEmissionDistribution(testarray, true).argmaxRow(0);
				predArray.add(prediction);
				if(prediction == test[i+1]){
					nCorrect++;
				}
			}
		
			//System.out.println(hmm.viterbi(testarray));
			System.out.println(nCorrect);
			//System.out.println(predArray);
			//System.out.println(test);
			System.out.println((double)(nCorrect/(double)(Obs.length - 2)));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void hmm_uniform(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("Test data/hmm_c_N1000.in"));
			String[] Obs = reader.readLine().split(" ");
			int[] obs = new int[Obs.length];

			for (int i = 0; i < obs.length; i++) {
				obs[i] = Integer.parseInt(Obs[i]);
			}

			reader.close();


			//Question 9
			Matrix A = new Matrix("3 3 0.7 0.05 0.25 0.1 0.8 0.1 0.2 0.3 0.5");
			Matrix B = new Matrix("3 4 0.7 0.2 0.1 0.0 0.1 0.4 0.3 0.2 0.0 0.1 0.2 0.7");
			Matrix PI = new Matrix("1 3 1.0 0.0 0.0");


			HMM hmm = new HMM(3,4);
			hmm.alphaTest(obs,true);
			// Below is the BW-algorithm
			double logProb = Integer.MIN_VALUE + 1;
			int maxIters = 2500;
			double oldLogProb = Integer.MIN_VALUE;
			int iter = 1;
			double logLim = 0.00001;
			double ratio = 0;
			boolean ratioCond = true;

			while ((iter < maxIters) && (logProb > oldLogProb) && ratioCond) {

				hmm.reEstimate(obs);

				oldLogProb = logProb;
				logProb = hmm.getLogProb();

				ratio = Math.abs((logProb - oldLogProb) / oldLogProb);
				ratioCond = (ratio > logLim);
				iter++;
			}
			//System.out.println("Iterations: " + iter);
			//System.out.println("LogLikelihood ratio: " + ratio);
			//System.out.println(hmm.getA());
			//System.out.println(hmm.getB());
			//System.out.println(hmm.getPI());
			//System.out.println("A similarity: " + Matrix.compareMatrices(hmm.getA(),A));
			//System.out.println("B similarity: " + Matrix.compareMatrices(hmm.getB(),B));
			//System.out.println("PI similarity: " + Matrix.compareMatrices(hmm.getPI(),PI));

		} catch (Exception e) {
			System.out.println(e);
		}
	}
	public static void hmm_diag(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("Test data/hmm_c_N10000.in"));
			String[] Obs = reader.readLine().split(" ");
			int[] obs = new int[Obs.length];

			for (int i = 0; i < obs.length; i++) {
				obs[i] = Integer.parseInt(Obs[i]);
			}

			reader.close();

			Matrix A = new Matrix("3 3 0.7 0.05 0.25 0.1 0.8 0.1 0.2 0.3 0.5");
			Matrix B = new Matrix("3 4 0.7 0.2 0.1 0.0 0.1 0.4 0.3 0.2 0.0 0.1 0.2 0.7");
			Matrix PI = new Matrix("1 3 1.0 0.0 0.0");

			Matrix Atest = new Matrix("3 3 1.0 0.0 0.0 0.0 0.1 0.0 0.0 0.0 1.0");
			Matrix Btest = new Matrix("3 4 0.5 0.2 0.11 0.19 0.22 0.28 0.23 0.27 0.19 0.21 0.15 0.45");
			Matrix PItest = new Matrix("1 3 0.0 1.0 0.0");

			HMM hmm = new HMM(Atest, Btest, PItest);
			hmm.alphaTest(obs, true);
			// Below is the BW-algorithm
			double logProb = Integer.MIN_VALUE + 1;
			int maxIters = 2500;
			double oldLogProb = Integer.MIN_VALUE;
			int iter = 1;
			double logLim = 0.00001;
			double ratio = 0;
			boolean ratioCond = true;

			while ((iter < maxIters) && (logProb > oldLogProb) && ratioCond) {

				hmm.reEstimate(obs);

				oldLogProb = logProb;
				logProb = hmm.getLogProb();

				ratio = Math.abs((logProb - oldLogProb) / oldLogProb);
				ratioCond = (ratio > logLim);
				iter++;
			}
			System.out.println("Iterations: " + iter);
			System.out.println("LogLikelihood ratio: " + ratio);
			System.out.println(hmm.getA());
			System.out.println(hmm.getB());
			System.out.println(hmm.getPI());
			//System.out.println("A similarity: " + Matrix.compareMatrices(hmm.getA(),A));
			//System.out.println("B similarity: " + Matrix.compareMatrices(hmm.getB(),B));
			//System.out.println("PI similarity: " + Matrix.compareMatrices(hmm.getPI(),PI));

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void hmm_close(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("Test data/hmm_c_N10000.in"));
			String[] Obs = reader.readLine().split(" ");
			int[] obs = new int[Obs.length];

			for (int i = 0; i < obs.length; i++) {
				obs[i] = Integer.parseInt(Obs[i]);
			}

			reader.close();


			//Question 9
			Matrix A = new Matrix("3 3 0.7 0.05 0.25 0.1 0.8 0.1 0.2 0.3 0.5");
			Matrix B = new Matrix("3 4 0.7 0.2 0.1 0.0 0.1 0.4 0.3 0.2 0.0 0.1 0.2 0.7");
			Matrix PI = new Matrix("1 3 1.0 0.0 0.0");

			Matrix Atest = new Matrix("3 3 0.65 0.08 0.27 0.08 0.85 0.07 0.15 0.37 0.48");
			Matrix Btest = new Matrix("3 4 0.72 0.19 0.08 0.01 0.11 0.39 0.29 0.21 0.02 0.08 0.21 0.69");
			Matrix PItest = new Matrix("1 3 0.9 0.05 0.05");

			HMM hmm = new HMM(Atest, Btest, PItest);
			hmm.alphaTest(obs,true);
			// Below is the BW-algorithm
			double logProb = Integer.MIN_VALUE + 1;
			int maxIters = 2500;
			double oldLogProb = Integer.MIN_VALUE;
			int iter = 1;
			double logLim = 0.00001;
			double ratio = 0;
			boolean ratioCond = true;

			while ((iter < maxIters) && (logProb > oldLogProb) && ratioCond) {

				hmm.reEstimate(obs);

				oldLogProb = logProb;
				logProb = hmm.getLogProb();

				ratio = Math.abs((logProb - oldLogProb) / oldLogProb);
				ratioCond = (ratio > logLim);
				iter++;
			}
			System.out.println(ratioCond);
			System.out.println("Iterations: " + iter);
			System.out.println("LogLikelihood ratio: " + ratio);
			System.out.println(hmm.getA());
			System.out.println(hmm.getB());
			System.out.println(hmm.getPI());
			//System.out.println("A similarity: " + Matrix.compareMatrices(hmm.getA(),A));
			//System.out.println("B similarity: " + Matrix.compareMatrices(hmm.getB(),B));
			//System.out.println("PI similarity: " + Matrix.compareMatrices(hmm.getPI(),PI));

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static int[] extendArray(int[] array, int n){
		//System.out.println("in extendArray");
		int[] newArray = new int[array.length + 1];
		//System.out.println("array length" + array.length);
		if(array.length != 0){
			for(int i = 0; i < array.length; i++){
				newArray[i] = array[i];
			}
		}
		//System.out.println("new array length : " + newArray.length);
		newArray[array.length] = n;
		
		return newArray;
	}
}
