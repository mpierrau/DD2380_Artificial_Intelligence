import java.util.ArrayList;

class Player {
	
	private int numStatesGuess = 6;
	private int numEmissions = Constants.COUNT_MOVE; 
	double probLim = 0.6; // limit for probability of shooting
	int waitingTurns = 60; // observe this many turns before beginning to predict
	
	HMM[] birdModelList;	
	ArrayList<ArrayList<HMM>> hmmModels = new ArrayList<ArrayList<HMM> >(Constants.COUNT_SPECIES); //list for keeping model estimates of different species
	ArrayList<Double> probList = new ArrayList<Double>();
	ArrayList<Integer> indexList = new ArrayList<Integer>();
	int[][] obsMatrix;
	int[] lGuess;

	//To keep track of statistics
	int shotsFired;
	int birdsHit;
	int shotsFiredPerRound;
	int birdsHitPerRound;
	int nGuesses;
	int nCorrectGuesses;
	int nGuessesPerRound;
	int nCorrectGuessesPerRound;
	
	int nBirds;
	
    public Player() {
    	for(int i=0; i<Constants.COUNT_SPECIES; i++){
             hmmModels.add(i,new ArrayList<HMM>());  // initliazed seperate HMM Model for each species
         }
    }

    /**
     * Shoot!
     *
     * This is the function where you start your work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     *
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */

    public Action shoot(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         * 
         */
    	
    	shotsFiredPerRound = 0;
    	birdsHitPerRound = 0;
    	nGuessesPerRound = 0;
    	nCorrectGuessesPerRound = 0;
		
		//If there are opponents, we wait a shorter time before predicting and shooting
    	if(pState.getNumPlayers() > 1) {
    		waitingTurns = 40;
    	}
    	
    	nBirds = pState.getNumBirds();
    	
    	birdModelList = new HMM[nBirds];
    	
    	probList = new ArrayList<Double>(nBirds);
    	indexList = new ArrayList<Integer>(nBirds);
    	obsMatrix = new int[nBirds][pState.getBird(0).getSeqLength()];

    	Matrix obsProb;
    	int maxProbIndex;
    	double maxProb;
    	HMM birdHMM;
    	
		if(pState.getBird(0).getSeqLength() < waitingTurns) return cDontShoot;
		
		//
    	for(int bird = 0; bird < nBirds; bird++) {

    		Bird thisBird = pState.getBird(bird);
			
			//Enter if statement unless Black Stork or dead bird
    		if(thisBird.isAlive() && getSpecies(thisBird,pState.getRound()) != Constants.SPECIES_BLACK_STORK) {
    			int seqLen = thisBird.getSeqLength();            	
        		
        		for(int i=0; i<seqLen; i++) {
        			obsMatrix[bird][i] = pState.getBird(bird).getObservation(i);
        		}
        		
        		birdHMM = new HMM(numStatesGuess,numEmissions);
        		birdHMM.baumWelch(obsMatrix[bird]); //Estimate model based on observations so far
				
				//Calculate most probable next move by bird
        		obsProb = birdHMM.nextEmissionDistribution(obsMatrix[bird]);
        		maxProbIndex = obsProb.argmaxRow(0);
        		maxProb =  obsProb.transpose().max(0);

				//Add model to list
        		birdModelList[bird] = birdHMM;
        		probList.add(maxProb);
        		indexList.add(maxProbIndex);
    		} else {
    			probList.add(0.0);
    			indexList.add(-1);
    		}
    	}
    	
    	double totMaxProb = Double.NEGATIVE_INFINITY;
		int totMaxIdx = 0;
		
		//Find bird whose next move we are most certain of 
		for(int z=0; z<probList.size(); z++) {
			if(probList.get(z) > totMaxProb) {
				totMaxProb = probList.get(z);
				totMaxIdx = z;
			}
		}
		
		//If certain enough, shoot
    	if(totMaxProb >= probLim) {
    		shotsFiredPerRound++;
    		return new Action(totMaxIdx,indexList.get(totMaxIdx));
    	}
    	return cDontShoot;
    }

    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to guess the species of
         * each bird. This skeleton makes no guesses, better safe than sorry!
         */    	

		 //For each bird in round, guess species by getSpecies function.
    	lGuess = new int[nBirds];
    	for(int bird=0; bird<pState.getNumBirds();bird++) {
    		lGuess[bird] = getSpecies(pState.getBird(bird),pState.getRound());
    	}	
        return lGuess;
    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified
     * through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird the bird you hit
     * @param pDue time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {
        birdsHitPerRound++;
    }

    /**
     * If you made any guesses, you will find out the true species of those
     * birds through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {

		//When round is over, save HMM models for each specie.
    	for(int bird=0; bird < pSpecies.length; bird++) {
    		int spec = pSpecies[bird];
    		Bird currentBird = pState.getBird(bird);
    		HMM currentHMM = new HMM(numStatesGuess, numEmissions);
    		
    		int[] currentObs = new int[currentBird.getSeqLength()];
    		for(int t=0; t < currentBird.getSeqLength(); t++) {
    			if(currentBird.wasDead(t)) {
    				break;
    			} else {
    				currentObs[t] = currentBird.getObservation(t);
    			}
    		}
    		
    		currentHMM.baumWelch(currentObs);
    		this.hmmModels.get(spec).add(currentHMM);
    	}
    	
    	
    	System.err.println("Guesses: ");
    	for(int i=0; i<lGuess.length; i++) {
    		if(lGuess[i] == pSpecies[i]) nCorrectGuessesPerRound++;
    		nGuessesPerRound++;
    	}

    	
    	nGuesses += nGuessesPerRound;
    	nCorrectGuesses += nCorrectGuessesPerRound;
    	birdsHit += birdsHitPerRound;
    	shotsFired += shotsFiredPerRound;
    	
    	System.err.println("Shooting accuracy this round: " + ((double) birdsHitPerRound/(double) shotsFiredPerRound));
    	System.err.println("Shooting accuracy: " + ((double) birdsHit/(double) shotsFired));
    	System.err.println("Accuracy this round: " + ((double) nCorrectGuessesPerRound/(double) nGuessesPerRound));
    	System.err.println("Guess acc: " + ((double) nCorrectGuesses/(double) nGuesses));
    }

    public static final Action cDontShoot = new Action(-1, -1);
    
    
    public int getSpecies(Bird bird, int round) {
		
		//First round we guess randomly on species
    	if(round == 0) {
    		return (int) (Math.random()*Constants.COUNT_SPECIES);
    	} else {
			double maxSpeciesProb = Double.NEGATIVE_INFINITY;
			int[] maxSpeciesIdx = {0,0};
			int[] currentObs = new int[bird.getSeqLength()];
    		for(int t=0; t < bird.getSeqLength(); t++) {
    			if(bird.wasDead(t)) {
    				break;
    			} else {
    				currentObs[t] = bird.getObservation(t);
    			}
    		}
			
			//For each species, we have many models. We find the one which gives the highest probability
			//of the observed sequence and choose it as our current model for that species.
			for(int species = 0; species < hmmModels.size(); species++) {
				int bestModelIdx = 0;
				double bestModelProb = Double.NEGATIVE_INFINITY;
				double tmpModelProb;
				for(int model = 0; model < hmmModels.get(species).size(); model++) {					
					tmpModelProb = Math.log(hmmModels.get(species).get(model).alphaTest(currentObs, false).getColumn(currentObs.length-1).sum());
					if(tmpModelProb > bestModelProb) {
						bestModelProb = tmpModelProb;
						bestModelIdx = model;
					}
				}
				
				if(bestModelProb > maxSpeciesProb) {
					maxSpeciesProb = bestModelProb;
					maxSpeciesIdx[0] = species;
					maxSpeciesIdx[1] = bestModelIdx; 
				}
    		}
			return maxSpeciesIdx[0];
    	}
    }
    
    public static double[] scale(double[] arr) {

        double temp =0.0;
        double[] arr2 = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
          temp += arr[i];
        }
        for (int i = 0; i < arr.length; i++) {
            arr2[i] = arr[i]/ temp;
        }

        return arr2;
    }
}