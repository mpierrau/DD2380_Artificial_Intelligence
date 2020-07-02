
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
    Random newRand = new Random();

    public HMM(int states, int emissions) {
    //N is number of hidden states
    //M is number of different observations
    //obs is observations so far

    //This constructor creates A,B and PI randomly with values between 0.1 and 0.9 and normalizes.

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
            tmpPI.mat[0][i] = tmpRand;
            tmpPISum += tmpRand;
        }
        tmpPI.scaleRow(0, 1.0/tmpPISum);

        this.A = tmpA;
        this.B = tmpB;
        this.PI = tmpPI;
    }

    public HMM(Matrix A, Matrix B, Matrix PI) {
    //This constructor creates an HMM model from the given matrices A, B and PI.

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

    //Calculate probability of next observation given observed sequence
    public Matrix nextEmissionDistribution(int[] obs){
        Matrix tmpAlpha = alphaTest(obs,true).getColumn(obs.length-2).transpose();
        Matrix res = Matrix.multiply(tmpAlpha,A);
        Matrix prob = Matrix.multiply(res,B);
        return prob;
    }

    //Alphapass 
    public Matrix alphaTest(int[] obs, boolean doScale) {
    //Implemented according to Stamp tutorial
        this.c = new double[obs.length];

        Matrix alphaMat = new Matrix(A.nrows(),obs.length);
        c[0] = 0;
        for(int i=0; i<A.nrows();i++) {
            alphaMat.mat[i][0] = PI.mat[0][i]*B.mat[i][obs[0]];
            c[0] += alphaMat.mat[i][0];
        }

        c[0]= 1.0/c[0];

        if(doScale) {
            for(int i=0; i<A.nrows();i++) {
                alphaMat.mat[i][0] *= c[0];
            }
        }

        for(int t=1; t<obs.length; t++) {
            c[t] = 0;
            for(int i=0; i<A.nrows();i++) {
                alphaMat.mat[i][t] = 0;
                for(int j=0; j<A.nrows();j++) {
                    alphaMat.mat[i][t] += alphaMat.mat[j][t-1]*A.mat[j][i];
                }
                alphaMat.mat[i][t] = alphaMat.mat[i][t]*B.mat[i][obs[t]];
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

    //Betapass
    private Matrix betaTest(int[] obs){
    //Implemented according to Stamp tutorial
        int states = this.A.nrows();
        Matrix betaMat = new Matrix(states, obs.length);

        for(int i = 0; i < states; i++){
            betaMat.mat[i][betaMat.ncols()-1] = c[betaMat.ncols()-1];
        }

        for(int t = betaMat.ncols()-2; t >= 0; t--){
            for(int i = 0; i < states; i++){
                for(int j = 0; j < states; j++){
                    betaMat.mat[i][t] += A.mat[i][j]*B.mat[j][obs[t+1]]*betaMat.mat[j][t+1];
                }
                betaMat.mat[i][t] = betaMat.mat[i][t]*c[t];

            }
        }
        return betaMat;
    }

    private Matrix getDelta(Matrix delta, Matrix A, Matrix B, int currentObs) {
    //Implemented according to Stamp tutorial
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
    //Implemented according to Stamp tutorial
        diGammaList = new Matrix[obs.length-1];
        gammaList = new Matrix[obs.length];

        for(int t=0; t<obs.length-1; t++) {
            Matrix diGamma = new Matrix(A.nrows(), A.ncols());
            Matrix tmpGamma = new Matrix(A.nrows(), 1);

            for(int i=0; i<A.nrows(); i++) {
                tmpGamma.mat[i][0] = 0;
                for(int j=0; j<A.ncols(); j++) {
                    diGamma.mat[i][j] = alpha.mat[i][t]*A.mat[i][j]*B.mat[j][obs[t+1]]*beta.mat[j][t+1];
                    tmpGamma.mat[i][0] += diGamma.mat[i][j];
                }
            }
            diGammaList[t] = diGamma;
            gammaList[t] = tmpGamma;
        }

        Matrix tmpMat = new Matrix(A.nrows(), 1);

        for(int i=0; i < A.nrows()-1; i++) {
            tmpMat.mat[i][0] = alpha.mat[i][obs.length-1];
        }

        gammaList[obs.length-1] = tmpMat;

    }

    public Matrix viterbi(int[] obs){
    //Implemented according to Stamp tutorial
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
            finalMat.mat[0][i]=deltaMat.getColumn(i).add(A.getColumn(nextState).lnMat()).max(0);
            finalMat.mat[1][i]=deltaMat.getColumn(i).add(A.getColumn(nextState).lnMat()).argmaxCol(0);
        }

        //Returns most likely state sequence
        return finalMat.getRow(1);
    }

    public void reEstimate(int[] obs) {

        int T = obs.length;
        Matrix alpha = alphaTest(obs,true);
        Matrix beta = betaTest(obs);

        getDiGamma(alpha, beta, A, B,obs);

        //Estimate PI
        for(int i=0; i < A.nrows(); i++) {
            PI.mat[0][i] = gammaList[0].mat[i][0];
        }

        //Reestimate A
        Matrix newANum = new Matrix(A.nrows(), A.ncols());
        Matrix newADen = new Matrix(A.nrows(), A.ncols());

        for(int t = 0; t < T-1; t++) {
            for(int i = 0; i < A.nrows(); i++) {
                for(int j = 0; j < A.ncols(); j++) {
                    newADen.mat[i][j] += gammaList[t].mat[i][0];
                    newANum.mat[i][j] += diGammaList[t].mat[i][j];
                }
            }
        }

        this.A = Matrix.elWiseDivide(newANum, newADen);

        //Reestimate B
        Matrix newBNum = new Matrix(B.nrows(), B.ncols());
        Matrix newBDen = new Matrix(B.nrows(), B.ncols());

        for(int t = 0; t < T; t++) {
            for(int i = 0; i < B.nrows(); i++) {
                for(int j = 0; j < B.ncols(); j++) {
                    newBDen.mat[i][j] += gammaList[t].mat[i][0];
                    if(obs[t] == j) {
                        newBNum.mat[i][j] += gammaList[t].mat[i][0];
                    }
                }
            }
        }

        this.B = Matrix.elWiseDivide(newBNum, newBDen);
    }

    public void baumWelch(int[] obs){
        this.alphaTest(obs,true);
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
        Matrix obsProb = nextEmissionDistribution(obs);
        double maxProb =  obsProb.transpose().max(0);

        return maxProb;
    }

    public int maxProbIdx(int[] obs) {
        Matrix obsProb = nextEmissionDistribution(obs);
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
