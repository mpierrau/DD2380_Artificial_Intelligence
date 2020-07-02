import java.lang.Math;
import java.util.Random;
//import java.math;

public class HMM_Ani {

  
  final int states; 
  final int emissions; 
  
  double[][] A; 
  double[][] B;  
  double[][] pi;  
  Random newRand = new Random();
//  long seed = 13371337;
 


  public  double getRandDouble(double min, double max){
	double x = (double)(newRand.nextDouble()*((max-min)+1))+min;
	return x;}
  public HMM_Ani(int states, int emissions) {
//	newRand.setSeed(seed);


    this.states = states;
    this.emissions = emissions;
    this.A = new double[states][states];
    this.B = new double[states][emissions];
    this.pi = new double[1][states];

    
    for (int i = 0; i < states; ++i) {
      for (int j = 0; j < states; ++j) {
        this.A[i][j] = getRandDouble(.1,.9);
      }
    }

    this.A= normalize(A);
   

    for (int i = 0; i < states; ++i) {
      for (int j = 0; j < emissions; ++j) {
        this.B[i][j] = getRandDouble(.1,.9);
      }
    }

    this.B = normalize(B);

    for (int i = 0; i < states; ++i) {
      this.pi[0][i] = getRandDouble(.1,.9);
    }

    this.pi = normalize(pi);
  }

  public HMM_Ani(double[][] A, double[][] B, double[][] pi){
    this.states = A[0].length;
    this.emissions = B[0].length;
    this.A = new double[states][states];
    this.B = new double[states][emissions];
    this.pi = new double[1][states];

    for (int i = 0; i < states; ++i) {
      for (int j = 0; j < states; ++j) {
        this.A[i][j] = A[i][j];
      }
    }
    
    for (int i = 0; i < states; ++i) {
      for (int j = 0; j < emissions; ++j) {
        this.B[i][j] = B[i][j];
      }
    }

    for (int i = 0; i < states; ++i) {
      this.pi[0][i] = pi[0][i];
    }
  }
  

  public double fwdAlgo(int[] O){
    double probability = 0.0;
    double[][] alpha = new double[O.length][states];

    for (int i = 0; i < states; ++i) {
      alpha[0][i]=pi[0][i]*B[i][O[0]];
    }
    
    for (int t = 1; t < O.length; ++t) {
      for (int i = 0; i < states; ++i) {
        for (int j = 0; j < states; ++j) {
          alpha[t][i] += alpha[(t-1)][j]*A[j][i]*B[i][O[t]];
        }
      }
    }
   
    for (int i = 0; i < states; ++i) {
      probability += alpha[(O.length-1)][i];
    }
    return probability;
  }
  public double[] fwdAlgo_alphaAtT(int[] O){
    double[][] alpha = new double[O.length][states];

    for (int i = 0; i < states; ++i) {
      alpha[0][i]=pi[0][i]*B[i][O[0]];
    }
    
    for (int t = 1; t < O.length; ++t) {
      for (int i = 0; i < states; ++i) {
        for (int j = 0; j < states; ++j) {
          alpha[t][i] += alpha[(t-1)][j]*A[j][i]*B[i][O[t]];
        }
      }
    }
   
    return alpha[O.length-1];
  }
  public void baumWelch(int[] arrS) {
    int rowA = A.length;
    int colA = A[0].length;
    int rowB = B.length;
    int colB = B[0].length;
    int rowI = pi.length;
    int colI = pi[0].length;
    int rowS = arrS.length;

    int maxiter = 25;
    int iter = 0;
    double oldLogProb = Double.NEGATIVE_INFINITY;
    double logprob = 0;
    boolean flag = true;
//    System.err.println("ANIS B IN BAUM WELCH");
//    for(int i=0; i<rowB;i++) {
//    	System.err.println(" ");
//    	for(int j=0; j<colB;j++) {
//            System.err.print(B[i][j] + " ");
//    	}    	
//    }
//	System.err.println();
    while (iter < maxiter && flag) {

        /* Alpha estimation */
        double scale[] = new double[rowS];
        double alpha[][] = new double[rowS][rowA];
        scale[0] = 0;
        for (int i = 0; i < rowA; i++) {
            alpha[0][i] = pi[0][i] * B[i][arrS[0]];
            scale[0] = scale[0] + alpha[0][i];
        }
        scale[0] = 1.0 / scale[0];
        for (int i = 0; i < rowA; i++) {
            alpha[0][i] = alpha[0][i] * scale[0];
        }
        for (int t = 1; t < rowS; t++) {
            scale[t] = 0;
            for (int i = 0; i < rowA; i++) {
                for (int j = 0; j < rowA; j++) {
                    alpha[t][i] = alpha[t][i] + alpha[t - 1][j] * A[j][i];
                }
                alpha[t][i] = alpha[t][i] * B[i][arrS[t]];
                scale[t] = alpha[t][i] + scale[t];
            }
            scale[t] = 1.0 / scale[t];
            for (int i = 0; i < rowA; i++) {
                alpha[t][i] = alpha[t][i] * scale[t];
            }
        }

        /* Beta estimation */
        double beta[][] = new double[rowS][rowA];
        for (int i = 0; i < rowA; i++) {
            beta[rowS - 1][i] = scale[rowS - 1];
        }
        for (int t = rowS - 2; t >= 0; t--) {
            for (int i = 0; i < rowA; i++) {
                beta[t][i] = 0;
                for (int j = 0; j < rowA; j++) {
                    beta[t][i] = beta[t][i] + beta[t + 1][j] * A[i][j] * B[j][arrS[t + 1]];
                }
                beta[t][i] = beta[t][i] * scale[t];
            }

        }
        /* gamma and digamma estimation */
        double gamma[][] = new double[rowS][rowA];
        double digamma[][][] = new double[rowS][rowA][rowA];
        for (int t = 0; t < rowS - 1; t++) {
            for (int i = 0; i < rowA; i++) {
                gamma[t][i] = 0;
                for (int j = 0; j < rowA; j++) {
                    digamma[t][i][j] = alpha[t][i] * A[i][j] * B[j][arrS[t + 1]] * beta[t + 1][j];
                    gamma[t][i] = gamma[t][i] + digamma[t][i][j];
                }
            }
        }
        for (int i = 0; i < rowA; i++) {
            gamma[rowS - 1][i] = alpha[rowS - 1][i];
        }

        /* pi estimation */
        double[][] newI = new double[rowI][colI];
        for (int i = 0; i < rowA; i++) {
            newI[rowI - 1][i] = gamma[0][i];
        }

        /* A estimation */
        double newA[][] = new double[rowA][colA];
        double num, den;
        for (int i = 0; i < rowA; i++) {
            den = 0.0;
            for (int t = 0; t < rowS - 1; t++) {
                den = den + gamma[t][i];
            }
            for (int j = 0; j < rowA; j++) {
                num = 0.0;
                for (int t = 0; t < rowS - 1; t++) {
                    num = num + digamma[t][i][j];
                }
                newA[i][j] = num / den;
            }
        }

        /* B estimation */
        double[][] newB = new double[rowB][colB];
        for (int i = 0; i < rowA; i++) {
            den = 0.0;
            for (int t = 0; t < rowS; t++) {
                den = den + gamma[t][i];
            }
            for (int j = 0; j < colB; j++) {
                num = 0;
                for (int t = 0; t < rowS; t++) {
                    if (arrS[t] == j) {
                        num = num + gamma[t][i];
                    }
                }
                newB[i][j] = num / den;
            }
        }

        /* log prob estimation */
        logprob = 0;
        for (int t = 0; t < rowS; t++) {
            logprob = logprob + Math.log(scale[t]);
        }
        logprob = -logprob;

        iter++;
        if (logprob > oldLogProb) {
            oldLogProb = logprob;
        } else {
            flag = false;
        }
        A = newA;
        B = newB;
        pi = newI;
        
//        for(int i=0; i<rowB;i++) {
//        	System.err.println(" ");
//        	for(int j=0; j<colB;j++) {
//                System.err.print(B[i][j] + " ");
//        	}
//        }
//        System.err.println();
    }
//    System.err.println("Ani ITER = " + iter);
   // return Math.exp(logprob);
}

public double[] pOfNextOb(double arr[]){
double[] pOfOb = new double[emissions];
for(int i = 0; i < states; i++){
  for(int j = 0; j < states; j++){
    for(int k = 0; k < emissions; k++){
      pOfOb[k] += arr[j]*A[j][i]*B[i][k];
    }
  }
}
return pOfOb;
}


  
 
  

  


  public static double[][] normalize(double m[][]) {
      double[][] m2 = new double[m.length][m[0].length];

      for (int row = 0; row < m.length; row++) {
        double sum = 0;
      for (double temp : m[row])
        sum += temp;
        if (sum != 0)
          for (int col = 0; col < m[row].length; col++) {
            m2[row][col] = m[row][col] / sum;
          }
      }

      return m2;

}

  public static double[] normalize(double[] a) {
    double sum =0.0;
    double[] a2 = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      sum += a[i];       
    }
    //System.err.println("SUM "+ sum);
    for (int i = 0; i < a.length; i++) {
      a2[i] = a[i] * (1.0 / sum);       
    }

    return a2;
  }

  
  
}
  