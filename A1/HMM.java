import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Random;

public class HMM {

    private Matrix A;
    private Matrix B;
    private Matrix PI;
    public double[] c;
    public Matrix[] gammaList;
    public Matrix[] diGammaList;
    double initMin = 0.1;
    double initMax = 0.9;
    //    long seed = 13371337;
    Random newRand = new Random();

    public HMM(int states, int emissions) {
//    	newRand.setSeed(seed);

//    	N is number of hidden states
//    	M is number of different observations
//    	obs is observations so far

        Matrix tmpA = new Matrix(states,states);
        Matrix tmpB = new Matrix(states,emissions);
        Matrix tmpPI = new Matrix(1,states);
        
        double tmpSum; 
        double tmpRand;
        
        for(int i=0; i < states; i++) {
            tmpSum = 0;
            for(int j=0; j < states; j++) {
            	tmpRand = getRandDouble(initMin,initMax);
                tmpA.mat[i][j] = tmpRand;
                tmpSum += tmpRand;
            }
            tmpA.scaleRow(i, 1.0/tmpSum);
        }

        for(int i=0; i < states; i++) {
            tmpSum = 0;
            for(int j=0; j < emissions; j++) {
            	tmpRand = getRandDouble(initMin,initMax);
                tmpB.mat[i][j] = tmpRand;
                tmpSum += tmpRand;
            }
            tmpB.scaleRow(i, 1.0/tmpSum);
        }

        double tmpPISum = 0;
        for(int i=0; i < states; i++) {
        	tmpRand = getRandDouble(initMin,initMax);
//            tmpRand = (1/doubleN) + (2*Math.random()-1)/(2*doubleN);
            tmpPI.mat[0][i] = tmpRand;
            tmpPISum += tmpRand;
        }
        tmpPI.scaleRow(0, 1.0/tmpPISum);

        this.A = tmpA;
        this.B = tmpB;
        this.PI = tmpPI;
    }

    public HMM(Matrix A, Matrix B, Matrix PI) {
        int states = A.mat[0].length;
        int emissions = B.mat[0].length;
        this.A = new Matrix(states,states);
        this.B = new Matrix(states,emissions);
        this.PI = new Matrix(1,states);

        for (int i = 0; i < states; ++i) {
            for (int j = 0; j < states; ++j) {
                this.A.mat[i][j] = A.mat[i][j];
            }
        }

        for (int i = 0; i < states; ++i) {
            for (int j = 0; j < emissions; ++j) {
                this.B.mat[i][j] = B.mat[i][j];
            }
        }

        for (int i = 0; i < states; ++i) {
            this.PI.mat[0][i] = PI.mat[0][i];
        }
    }



    public Matrix getA() {
        return A;
    }

    public Matrix getB() {
        return B;
    }

    public Matrix getPI() {
        return PI;
    }

    public double getLogProb() {
        double logProb = 0;

        for(int i=0; i<c.length; i++) {
            logProb+=Math.log(c[i]);
        }
        return -logProb;
    }


    public Matrix nextEmissionDistribution(int[] obs, boolean doScale){
        //Matrix tmpAlpha = alphaTest(obs,doScale).getColumn(obs.length-2);
        //Matrix res = Matrix.multiply(A,tmpAlpha).transpose();
        //Matrix prob = Matrix.multiply(res,B);

        //System.out.println("res : " + res);

        Matrix tmpAlpha2 = alphaTest(obs,doScale).getColumn(obs.length-2).transpose();  
        Matrix res2 = Matrix.multiply(tmpAlpha2,A);
        Matrix prob2 = Matrix.multiply(res2, B);

        //System.out.println("res2 : " + res2);

        return prob2;
    }

    public Matrix alphaTest(int[] obs, boolean doScale) {
        this.c = new double[obs.length-1];

        Matrix alphaMat = new Matrix(A.nrows(),obs.length-1);
        c[0] = 0;

        for(int i=0; i<A.nrows();i++) {
            alphaMat.mat[i][0] = PI.mat[0][i]*B.mat[i][obs[1]];
            c[0] += alphaMat.mat[i][0];
        }

        c[0]= 1.0/c[0];

        if(doScale) {
            for(int i=0; i<A.nrows();i++) {
                alphaMat.mat[i][0] *= c[0];
            }
        }

        for(int t=1; t<obs.length-1; t++) {
            c[t] = 0;
            for(int i=0; i<A.nrows();i++) {
                alphaMat.mat[i][t] = 0;
                for(int j=0; j<A.nrows();j++) {
                    alphaMat.mat[i][t] += alphaMat.mat[j][t-1]*A.mat[j][i];
                }
                alphaMat.mat[i][t] = alphaMat.mat[i][t]*B.mat[i][obs[t+1]];
                c[t] = c[t] + alphaMat.mat[i][t];
            }


            c[t] = 1.0/c[t];

            if(doScale) {
                for(int i=0; i<A.nrows();i++) {
                    alphaMat.mat[i][t] *= c[t];
                }
            }
        }
        return alphaMat;
    }

    private Matrix betaTest(int[] obs){
        int states = this.A.nrows();
//        int emissions = this.B.ncols();
        Matrix betaMat = new Matrix(states, obs.length-1);

        for(int i = 0; i < states; i++){
            betaMat.mat[i][betaMat.ncols()-1] = c[betaMat.ncols()-1];
        }

        for(int t = betaMat.ncols()-2; t >= 0; t--){
            for(int i = 0; i < states; i++){
                for(int j = 0; j < states; j++){
                    betaMat.mat[i][t] += A.mat[i][j]*B.mat[j][obs[t+2]]*betaMat.mat[j][t+1];
                }
                betaMat.mat[i][t] = betaMat.mat[i][t]*c[t];

            }
        }
        //System.out.println("betaTest done");
        return betaMat;
    }

    private Matrix getDelta(Matrix delta, Matrix A, Matrix B, int currentObs) {

        Matrix tmpMatr;
        Matrix resMatr = new Matrix(A.nrows(),2);

        Matrix lnA = A.lnMat();
        Matrix lnB = B.lnMat();

        for(int i=0; i<A.nrows(); i++) {
            tmpMatr = lnA.getColumn(i).add(delta).scalAdd(lnB.mat[i][currentObs]);
            resMatr.mat[i][0] = tmpMatr.max(0);
            resMatr.mat[i][1] = tmpMatr.argmaxCol(0);
        }

        return resMatr;
    }

    private void getDiGamma(Matrix alpha, Matrix beta, Matrix A, Matrix B, int[] obs) {
//    	currentIdx here is given as time t. We don't have to take into consideration that the array obs begins with a dimension, since we use currentIdx+1 in the formulas below
        //System.out.println("in getDiGamma");
        diGammaList = new Matrix[obs.length-2];
        gammaList = new Matrix[obs.length-1];

        for(int t=0; t<obs.length-2; t++) {
            Matrix diGamma = new Matrix(A.nrows(), A.ncols());
            Matrix tmpGamma = new Matrix(A.nrows(), 1);

            for(int i=0; i<A.nrows(); i++) {
                tmpGamma.mat[i][0] = 0;
                for(int j=0; j<A.ncols(); j++) {
                    diGamma.mat[i][j] = alpha.mat[i][t]*A.mat[i][j]*B.mat[j][obs[t+2]]*beta.mat[j][t+1];
                    tmpGamma.mat[i][0] += diGamma.mat[i][j];
                }
            }
            diGammaList[t] = diGamma;
            gammaList[t] = tmpGamma;
        }
        Matrix tmpMat = new Matrix(A.nrows(), 1);

        for(int i=0; i < A.nrows(); i++) {
            tmpMat.mat[i][0] = alpha.mat[i][obs.length-2];
        }

        gammaList[obs.length-2] = tmpMat;

    }

    public Matrix viterbi(int[] obs){
        Matrix deltaNull;
        Matrix newDelta;
        Matrix deltaMat = new Matrix(A.nrows(), 0);
        Matrix deltaIdxMat = new Matrix(A.nrows(), 0);
        Matrix thisDelta;

        Matrix lnPI = PI.lnMat();
        Matrix lnB = B.lnMat();

        deltaNull = Matrix.transpose(lnPI).add(lnB.getColumn(obs[1]));

//        Add this first delta column to delta-matrix and zero/undefined first values to psi/deltaIdx-matrix
        deltaMat.addColumn(deltaNull);
        deltaIdxMat.addColumn(new Matrix(A.nrows(),1));
        thisDelta=deltaNull;

//        For loop to calculate all deltas
//        Not giving correct result... not sure what is wrong
        for(int i=2; i<obs.length; i++) {
            //Find new delta with getDelta-function
            newDelta = getDelta(thisDelta, A, B, obs[i]);
            //Extract first column of result (this is the new delta) and add to delta-matrix
            thisDelta = newDelta.getColumn(0);
//            System.out.println(thisDelta);
            deltaMat.addColumn(thisDelta);
            //Also add the corresponding indices to deltaIdx-matrix
            deltaIdxMat.addColumn(newDelta.getColumn(1));
        }

        //deltaIdx at T is argmax of delta_T(i) over all i. thus
        //New matrix to return results of final path (first row) and its prob (2nd row)
        Matrix finalMat = new Matrix(2,obs.length-1);

        //Add last observations separately as they are calculated differently
        finalMat.mat[0][obs.length-2] = deltaMat.max(obs.length-2);
        finalMat.mat[1][obs.length-2] = deltaMat.argmaxCol(obs.length-2);

        for(int i=obs.length-3; i>=0; i--) {
            int nextState = (int) Math.round(finalMat.mat[1][i+1]);
            //WITH LN
            finalMat.mat[0][i]=deltaMat.getColumn(i).add(A.getColumn(nextState).lnMat()).max(0);
            finalMat.mat[1][i]=deltaMat.getColumn(i).add(A.getColumn(nextState).lnMat()).argmaxCol(0);
        }

        //Returns most likely state sequence
        return finalMat.getRow(1);
    }

    public void reEstimate(int[] obs) {
//    	initialise
        //System.out.println("in reEstimate");
        int T = obs.length-1;
//    	Matrix alpha = alphapass(obs);
        Matrix alpha = alphaTest(obs,true);
        Matrix beta = betaTest(obs);
        getDiGamma(alpha, beta, A, B,obs);

        //Estimate PI
        for(int i=0; i < A.nrows(); i++) {
            PI.mat[0][i] = gammaList[0].mat[i][0];
        }
        //System.out.println("1 in reEstimate");

        //Reestimate A
        //Matrix newANum = new Matrix(A.nrows(), A.ncols());
        //Matrix newADen = new Matrix(A.nrows(), A.ncols());
        double denomA;
        double numerA;
        for(int i = 0; i < A.nrows(); i++){
            denomA = 0;
            for(int t = 0; t < T-1; t++){
                denomA += gammaList[t].mat[i][0];
            }
            for(int j = 0; j < A.nrows(); j++){
                numerA=0;
                for(int t = 0; t < T-1; t++){
                    numerA += diGammaList[t].mat[i][j];
                }
                A.mat[i][j] = numerA/denomA;
            }
        }

        //for(int t = 0; t < T-1; t++) {
         //   for(int i = 0; i < A.nrows(); i++) {
          //      for(int j = 0; j < A.ncols(); j++) {
           //         newADen.mat[i][j] += gammaList[t].mat[i][0];
            //        newANum.mat[i][j] += diGammaList[t].mat[i][j];
             //   }
           // }
        //}
       // this.A = Matrix.elWiseDivide(newANum, newADen);
       // System.out.println("2 in reEstimate");

        //Reestimate B
        //Matrix newBNum = new Matrix(B.nrows(), B.ncols());
        //Matrix newBDen = new Matrix(B.nrows(), B.ncols());
        
        double denom, numer;
        for(int i=0; i < B.nrows(); i++){
            denom = 0;
            for(int t=0; t < T; t++){
                denom += gammaList[t].mat[i][0];
            }
            
            for(int j = 0; j < B.ncols(); j++){
                numer = 0;
                for(int t = 0; t < T; t++){
                    if(obs[t+1] == j){
                        numer += gammaList[t].mat[i][0];
                    }
                }
                this.B.mat[i][j] = numer/denom;
            }
        }

       // for(int t = 0; t < T; t++) {
        //    for(int i = 0; i < B.nrows(); i++) {
        //        for(int j = 0; j < B.ncols(); j++) {
        //            newBDen.mat[i][j] += gammaList[t].mat[i][0];
        //            if(obs[t] == j) {
        //                newBNum.mat[i][j] += gammaList[t].mat[i][0];
        ////            }
        //        }
        //    }
        //}

        //this.B = Matrix.elWiseDivide(newBNum, newBDen);
       // System.out.println("reEstimate Done");
    }

    public void baumWelch(int[] obs){
        this.alphaTest(obs,true);
        // Below is the BW-algorithm
        double logProb = Integer.MIN_VALUE + 1;
        int maxIters = 25;
        double oldLogProb = Integer.MIN_VALUE;
        int iter = 1;
        double logLim = 0.0001;
        boolean ratioCond = true;

        while ((iter < maxIters) && (logProb > oldLogProb) && ratioCond) {

            this.reEstimate(obs);
            oldLogProb = logProb;
            logProb = this.getLogProb();

            ratioCond = (Math.abs((logProb - oldLogProb) / oldLogProb) > logLim);
            iter++;
        }
    }

    public double maxProbObs(int[] obs) {
        Matrix obsProb = nextEmissionDistribution(obs, true);
        double maxProb =  obsProb.transpose().max(0);

        return maxProb;
    }

    public int maxProbIdx(int[] obs) {
        Matrix obsProb = nextEmissionDistribution(obs, true);
        int maxProbIndex = obsProb.argmaxRow(0);

        return maxProbIndex;
    }


    public  double getRandDouble(double min, double max){
        double x = (double)(newRand.nextDouble()*((max-min)+1))+min;
        return x;
    }

    public static Matrix normalize(Matrix m) {
        Matrix m2 = new Matrix(m.nrows(),m.ncols());
        for (int row = 0; row < m.nrows(); row++) {
            double sum = 0;
            for (int col=0; col < m.ncols(); col++)
                sum += m.mat[row][col];
            if (sum != 0)
                for (int col = 0; col < m.ncols(); col++)
                    m2.mat[row][col] = m.mat[row][col] / sum;
        }
        return m2;
    }
}
