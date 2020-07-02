## DD2380 Artificial Intelligence A3 Project

This repo contains the code for Group 12's A3 project (updated Saturday, October 12).

### Implementing New Algorithms

The architecture of the project is designed to be modular so that we encounter as few merge conflicts as possible. Thus, implementing a new algorithm does not require changing any of the base simulation code. Instead, you should create a new class which extends Rover and implements the two abstract methods it declares:

 - *shouldUpdate()* is called at the beginning of every step of the simulation and is meant to return whether the updatePlan() method should be called during this step.
 - *updatePlan()* is called if shouldUpdate() returns true. It uses the information known to the rover (PDFs, terrain, currentLocation, startingLocation, currentBattery) to produce an ArrayList<Point> that represents the path to travel. If you only want to plan one step ahead, your ArrayList<Point> should just contain a single point with the world coordinates of your next move. If you want to plan your entire path out, you should return an ArrayList<Point> where each node along your path is specified in order from your next step until your last. Even if you return a long plan, shouldUpdate() will still be called during every step of execution in case you decide to change your plan.

### Architecture

So far, the simulation code is broken up into the following files:

 1. *Debug.java*: This class implements some useful debugging tools. I already make use of them for the simulator code and I highly recommend that you use them too. They make debugging 100x easier and will save time in the long run.
 2. *Environment.java*: This class is basically a Tensor. It contains the 2D double arrays which represent the terrain as well as the PDFs for Matt Damon and the objects.
 3. *EnvironmentBuilder.java*: This class contains a bunch of static methods used to manipulate the terrain and PDFs of an environment. Basically a lot of helper functions.
 4. *Renderer.java*: This class extends JFrame and is the class which renders simulations to the screen. I think this will be useful for debugging and will produce nice visuals for our presentation, but the majority of our running will be done without this, so the running of the simulation has been abstracted away from its visualization.
 5. *Rover.java*: This abstract class defines the two methods that any planning algorithm must implement: shouldUpdate() and updatePlan(). In order to implement a new algorithm, you should extend this class and implement those methods. See "ExampleRover.java" for an example of how to extend the Rover class.
 6. *Simulation.java*: This class contains the nuts and bolts of initializing and running the simulation. It provides the "stepSimulation" method for debugging and visualization purposes, but also a more complete "runSimulation" method which runs the simulation until it completes and returns the results.

This is all first draft code, so I've tried to include comments describing the function of nearly every block. Please ask questions if you are confused and I will happily walk you through it. Also, please be on the lookout for bugs. I've had three cups of coffee this afternoon, so there is a moderate likelihood that I totally overlooked something in my coding frenzy.

:)

-Jackson
