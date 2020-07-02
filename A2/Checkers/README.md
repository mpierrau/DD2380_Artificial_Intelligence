
## Describe the possible states, initial state, transition function.
#### Checkers
* Possible states: Any 8x8 checkers board where only the black tiles are occupied, at most 12 checkers of each color
* Initial state: Any 8x8 checkers board with 12 checkers of each color are positioned on the black tiles on opposite sides
* Transfer functions: Any legal move 
#### Tic-tac-toe
* Possible states: Any NxN(xN) board with boxes either empty or occupied with X or O
* Initial state: Clear board
* Transfer functions: Any placed marker 

## Describe the terminal states of both checkers and tic-tac-toe

##### Checkers
Terminal state when either side is out of checkers or can't make a move or draw counter reaches 0
##### Tic-tac-toe
Terminal state is reached when once side has four in a row/column/diagonal

## Why is ν(A,s) = #{white checkers}−#{red checkers} a valid heuristic function for checkers (knowing that A plays white and B plays red)?
Satisfies the conditions for a zero-sum game. Also evaluates how good the move is for player A in each state. 
## When does ν best approximate the utility function, and why?
Since the utility function represents a perfect player, the heuristic function best approximates when it is optimal and has a high accuracy.
## Can you provide an example of a state s where ν(A,s) > 0 and B wins in the following turn? (Hint: recall the rules for jumping in checkers)
Assume player A has two checkers left and player B has one left, in this case ν(A,s) > 0. 
Player B is able to win if it is able to jump over both A's checkers in one turn
## Will η suffer from the same problem (referred to in the last question) as the evaluation function ν?
No, since we are able to look further down the tree to find see states were we loose and are able to avoid those states.

## Java skeleton for checkers dd2380

### Compile
```javac *.java```

### Run
### The players use standard input and output to communicate
### The Moves made are shown as unicode-art on std err if the parameter verbose is given

### Play against self in same terminal
```mkfifo pipe```
```java Main init verbose < pipe | java Main > pipe```

### Play against self in two different terminals
### Terminal 1:
```mkfifo pipe1 pipe2```
```java Main init verbose < pipe1 > pipe2```

### Terminal 2:
```java Main verbose > pipe1 < pipe2```

### To play two different agents against each other, you can use the classpath argument
```java -classpath <path> Main init verbose < pipe | java -classpath <path> Main > pipe```

