import java.util.*;

public class Player {
	
	private int myIndex;
	private int currentDepth;
	private int MAX_DEPTH = 9;
	private boolean timeIsUp;
	long ourDeadline = (long) 8e8;
	
	TreeMap<String,Double> orderingMap;
	Map<String,Double> sortedOrderingMap;
	
	HashMap<String,Double> lookUpMap = new HashMap<String, Double>();
    /**
     * Performs a move
     *
     * @param pState
     *            the current state of the board
     * @param pDue
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState pState, final Deadline pDue) {
    	myIndex = pState.getNextPlayer();
    	timeIsUp = false;
    	currentDepth = 1;
        Vector<GameState> lNextStates = new Vector<GameState>();
        pState.findPossibleMoves(lNextStates);

        if (lNextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }

        if (lNextStates.size() == 1) {
        	return lNextStates.get(0);
        }

        double tmpValue;
        
        TreeMap<String,Double> valueMap = new TreeMap<String,Double>();
        
        orderingMap = new TreeMap<String,Double>();
        
        for(GameState state: lNextStates) {
        	orderingMap.put(state.toMessage(), eval(state));
        }
        
        sortedOrderingMap = sortByValues(orderingMap);
        
        while(!timeIsUp && (currentDepth < MAX_DEPTH)) {
	        for(String stateString: sortedOrderingMap.keySet()) {
	        	GameState tmpState = new GameState(stateString);
	        	tmpValue = alphabeta(tmpState, (-1)*Double.MAX_VALUE, Double.MAX_VALUE, tmpState.getNextPlayer(), currentDepth-1, pDue, ourDeadline/lNextStates.size());
	        	valueMap.put(tmpState.toMessage(), tmpValue);
	        }
	        currentDepth++;
        }
        
		Map<String, Double> sortedValueMap = sortByValues(valueMap);
		Map.Entry<String,Double> entry = sortedValueMap.entrySet().iterator().next();
		String tmpMaxKey = entry.getKey();
		GameState returningState = new GameState(tmpMaxKey);
		
		orderingMap.clear();
		sortedOrderingMap.clear();
		valueMap.clear();
		
        return returningState; 
    }
    
    public double alphabeta(final GameState gameState, double alpha, double beta, int nextPlayer, int depth, final Deadline pDue, long thisDepthDL){
		double v = 0;
		
		Deadline depthDue = new Deadline(Deadline.getCpuTime()+thisDepthDL);
		double bufferPercent = 0.1;
		timeIsUp = (pDue.timeUntil() < ourDeadline); 
		
		if(gameState.isEOG()){
			if((gameState.isRedWin() && (myIndex == Constants.CELL_RED)) ||
					(gameState.isWhiteWin() && (myIndex == Constants.CELL_WHITE))){
				v = Double.MAX_VALUE;
			} else if((gameState.isRedWin() && (myIndex != Constants.CELL_RED)) ||
					(gameState.isWhiteWin() && (myIndex != Constants.CELL_WHITE))) {
				v = (-1)*Double.MAX_VALUE;
			} else {
				v = 0;
			}
		}else if(depth == 0){
			v = eval(gameState);
		}else if(nextPlayer == myIndex){
			v = (-1)*Double.MAX_VALUE;
			
			Vector<GameState> nextStates = new Vector<GameState>();
			gameState.findPossibleMoves(nextStates);
		
			TreeMap<String,Double> tmpOrdMap = new TreeMap<String,Double>();
			
			 for(GameState state: nextStates) {
		        	tmpOrdMap.put(state.toMessage(), eval(state));
		        }
		        
			 Map<String, Double> tmpSortOrdMap = sortByValues(tmpOrdMap);
		     
			 tmpOrdMap.clear();
			
			for(String stateString : tmpSortOrdMap.keySet()) {
				GameState tmpState = new GameState(stateString);
				if(depthDue.timeUntil() > thisDepthDL*bufferPercent) {
					v = Double.max(v,alphabeta(tmpState,alpha,beta,tmpState.getNextPlayer(),depth-1,pDue,thisDepthDL/nextStates.size()));
					alpha = Double.max(alpha,v);
					if(beta <= alpha){
						break;
					}
				} else {
					v = eval(tmpState);
					break;
				}
			}
			tmpSortOrdMap.clear();
		}
		else{
			v = Double.MAX_VALUE;
			Vector<GameState> nextStates = new Vector<GameState>();
			gameState.findPossibleMoves(nextStates);
			TreeMap<String,Double> tmpOrdMap = new TreeMap<String,Double>();
			
			
			 for(GameState state: nextStates) {
		        	tmpOrdMap.put(state.toMessage(), eval(state));
		        }
		        
			 Map<String, Double> tmpSortOrdMap = sortByValues(tmpOrdMap);
			 
			 tmpOrdMap.clear();
			
			for(String stateString : tmpSortOrdMap.keySet()) {
				GameState tmpState = new GameState(stateString);
				if(depthDue.timeUntil() > thisDepthDL*bufferPercent) {
					v = Double.min(v,alphabeta(tmpState,alpha,beta,tmpState.getNextPlayer(),depth-1,pDue,thisDepthDL/nextStates.size()));
					beta = Double.min(beta,v);
					if(beta <= alpha){
						break;
					}
				} else {
					v=eval(tmpState);
					break;
				}
			}
			tmpSortOrdMap.clear();
		}
		return v;
	}
    
//    public double eval(GameState state) {
////    	NUMBER OF PIECES
////    	NUMBER OF PIECES ON OPPOSITE SIDE
////    	NUMBER OF CAPTURES
////    	NUMBER OF KINGS?
//		
////		Get points for:
////		Taking enemy checkers
////		Advancing the board
////		Keeping along the edges
//
//    	if(lookUpMap.containsKey(state.toMessage())) {
//    		return lookUpMap.get(state.toMessage());
//    	} else {
//    		int kingFactor = 2;
//    		double reward = Double.MAX_VALUE;
//    		
//    			int[] pieces = new int[8];
//    			int[] noKings = new int[3];
//    			for(int i=0; i < GameState.NUMBER_OF_SQUARES; i++) {
//    				int tmpIndex = state.get(i);
//    				if(tmpIndex==(Constants.CELL_KING|Constants.CELL_WHITE)) {
//    					pieces[Constants.CELL_WHITE]+=(kingFactor);
//    					noKings[Constants.CELL_WHITE]++;
//    				} else if (tmpIndex==(Constants.CELL_KING|Constants.CELL_RED)) {
//    					pieces[Constants.CELL_RED]+=(kingFactor);
//    					noKings[Constants.CELL_RED]++;
//    				} else {
//    					pieces[tmpIndex]++;
//    				}
//    			}
//
//    			double myFinalVal = pieces[myIndex];
//    			double oppFinalVal = pieces[3-myIndex];
//    			double val;
//
//    			if(oppFinalVal == 0) {
//    				val = reward;
//    			} else if (state.isRedWin() && myIndex == Constants.CELL_RED) {
//    				val = reward;
//    			} else if (state.isWhiteWin() && myIndex == Constants.CELL_WHITE) {
//    				val = reward;
//    			} else {
//    				val = myFinalVal-oppFinalVal;
//    			}
//    			
//    			if(state.getNextPlayer() == myIndex) {
//    				lookUpMap.put(state.toMessage(),val);
//    				lookUpMap.put(state.reversed().toMessage(), -1*val);
//    				return val;}
//    			else {
//    				lookUpMap.put(state.toMessage(),-1*val);
//    				lookUpMap.put(state.reversed().toMessage(), val);
//    				return -1*val;}
//    	}
//    }
    
    public double eval(GameState state) {
//    	NUMBER OF PIECES
//    	NUMBER OF PIECES ON OPPOSITE SIDE
//    	NUMBER OF CAPTURES
//    	NUMBER OF KINGS?
		
//		Get points for:
//		Taking enemy checkers
//		Advancing the board
//		Keeping along the edges
    		
    	if(lookUpMap.containsKey(state.toMessage())) {
    		return lookUpMap.get(state.toMessage()); 
    	} else {
    	
			int kEdge = 100;
			int hEdge = 20;
			int row1 = 40;
			int row2 = 60;
			int row3 = 80;
			int edge1 = 50;
			int edge2 = 70;
			int edge3 = 90;
			int kingFactor = 1000;
			double reward = Double.MAX_VALUE;
			int captureFactor = 2000;
			int nCaptured = 0;
			int currPlayer = state.getNextPlayer();
			
			if(!state.isEOG()) {
				nCaptured = Integer.parseInt(state.toMessage().substring(33,34));
			}		
	//		System.err.println("nCaptured = " + nCaptured);
	//		ArrayList<Integer> scoreArray = new ArrayList<Integer>{0, 1, 2, 3, 4, 11, 12, 19, 20, 27, 28, 29, 30, 31};
			
			int[][] scoreMatrix = new int[3][32];
			
			int[] scoreArrayUs = new int[] {
						kEdge, kEdge, kEdge, kEdge, 
					edge3, row3, row3, row3, 
						row3, row3, row3, edge3,
					edge2, row2, row2, row2,
						row2, row2, row2, edge2,
					edge1, row1, row1, row1,
						row1, row1, row1, edge1,
					hEdge, hEdge, hEdge, hEdge};
			
			int[] scoreArrayThem = new int[] {
					hEdge, hEdge, hEdge, hEdge, 
				edge1, row1, row1, row1, 
					row1, row1, row1, edge1,
				edge2, row2, row2, row2,
					row2, row2, row2, edge2,
				edge3, row3, row3, row3,
					row3, row3, row3, edge3,
				kEdge, kEdge, kEdge, kEdge};
			
			scoreMatrix[myIndex] = scoreArrayUs;
			scoreMatrix[3-myIndex] = scoreArrayThem;
			
				int[] pieces = new int[8];
				int[] noKings = new int[3];
				for(int i=0; i < GameState.NUMBER_OF_SQUARES; i++) {
					int tmpIndex = state.get(i);
					if(tmpIndex==(Constants.CELL_KING|Constants.CELL_WHITE)) {
						pieces[Constants.CELL_WHITE]+=(kingFactor)+scoreMatrix[currPlayer][i];
						noKings[Constants.CELL_WHITE]++;
					} else if (tmpIndex==(Constants.CELL_KING|Constants.CELL_RED)) {
						pieces[Constants.CELL_RED]+=(kingFactor)+scoreMatrix[currPlayer][i];
						noKings[Constants.CELL_RED]++;
					} else {
						pieces[state.get(i)]+=scoreMatrix[currPlayer][i];
					}
				}
				
				pieces[3-state.getNextPlayer()] += Math.pow(captureFactor, nCaptured);
				
				double myFinalVal = pieces[myIndex];
				double oppFinalVal = pieces[3-myIndex];
				double val;
	
				if(oppFinalVal == 0) {
					val = reward;
				} else if (state.isRedWin() && myIndex == Constants.CELL_RED) {
					val = reward;
				} else if (state.isWhiteWin() && myIndex == Constants.CELL_WHITE) {
					val = reward;
				} else {
					val = myFinalVal-oppFinalVal;
				}
				
				if(state.getNextPlayer() == myIndex) {
    				lookUpMap.put(state.toMessage(),val);
    				lookUpMap.put(state.reversed().toMessage(), -1*val);
    				return val;}
    			else {
    				lookUpMap.put(state.toMessage(),-1*val);
    				lookUpMap.put(state.reversed().toMessage(), val);
    				return -1*val;}
				
//				lookUpMap.put(state.toMessage(), val);
				
//				return val;
    	}		
	}
    
	public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K,V> map){

		Comparator<K> valueComparator = new Comparator<K>() {
			@Override
			public int compare(K k1, K k2) {
				int comp = map.get(k1).compareTo(map.get(k2));
				if (comp == 0) {
					return 1;
				} else {
					return 	(-1)*comp;
				}
			};
		};

		TreeMap<K,V> sorted = new TreeMap<K,V>(valueComparator);

		sorted.putAll(map);

		return sorted;
	}
	
	public static <K, V extends Comparable<V>> Map<K, V> sortAscending(final Map<K,V> map){

		Comparator<K> valueComparator = new Comparator<K>() {
			@Override
			public int compare(K k1, K k2) {
				int comp = map.get(k1).compareTo(map.get(k2));
				if (comp == 0) {
					return 1;
				} else {
					return 	comp;
				}
			};
		};

		TreeMap<K,V> sorted = new TreeMap<K,V>(valueComparator);

		sorted.putAll(map);

		return sorted;
	}
}
