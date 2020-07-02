import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Renderer extends JFrame{
	
	public Simulation sim;
	
	// Highest point is used to scale the terrain values to be
	// between 0 and 1 for rendering purposes.
	private static final int pixelsPerSquare = 14;
	private static final Color roverColor = new Color(170, 170, 170);
	private static final Color startingColor = new Color(255, 255, 255);
	private static final Color damonColor = new Color(0, 170, 0);
	private static final Color objectColor = new Color(0, 0, 170);
	private static final Color planColor = new Color(230, 230, 0);
	
	
	public Renderer(Simulation sim) {
		
		// This code is temporary for testing:
		this.sim = sim;
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		add(new RenderMapComponent(sim.env.terrain, 1.0, true, true, true, true, true));
		add(new RenderMapComponent(sim.rover.estimatedEnv.terrain, 1.0, true, true, true, true, true));
		add(new RenderMapComponent(sim.rover.estimatedEnv.pdfDamon, 0.0, true, true, true, true, true));
		pack();
		this.
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	
	// Renders the simulation all the way through. This method schedules
	// the stepping of the simulation and repainting of the renderer such that
	// each frame is displayed msPerFrame milliseconds after the last (if the
	// simulation can update that quickly). The runnable terminates its own scheduling
	// once the simulation is done.
	public void renderSimulation(long msPerFrame) {
		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	    executorService.scheduleAtFixedRate(new Runnable() {
			public void run() {
				if(!sim.isDone) {
					sim.stepSimulation();
					if (Debug.ON) Debug.Log("Repainting\n");
					repaint();
				} else {
					executorService.shutdown();
				}
			}
	    }, 1000, msPerFrame, TimeUnit.MILLISECONDS);
	}
	
	
	class RenderMapComponent extends JComponent {
		
		double[][] map;
		private double max;
		private boolean staticMax = true;
		private boolean drawRover, drawStart, drawDamon, drawObjects, drawPlan;
		
		public RenderMapComponent(double[][] map, double max, boolean rover, boolean start, boolean damon, boolean objs, boolean plan) {
			setPreferredSize(new Dimension(map.length*pixelsPerSquare, map[0].length*pixelsPerSquare));
			this.max = max;
			if (max <= 0)
				staticMax = false;
			this.map = map;
			this.drawRover = rover;
			this.drawStart  = start;
			this.drawDamon = damon;
			this.drawObjects = objs;
			this.drawPlan = plan;
		}
		
		// Updates the scaling if necessary
		public void checkUpdate() {
			// Do nothing if our max value is static
			if (staticMax) {
				return;
			}
			
			double newMax = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < map.length; i++) {
				for(int j = 0; j < map[0].length; j++) {
					if (map[i][j] > newMax)
						newMax = map[i][j];
				}
			}
			max = newMax;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			checkUpdate();

			// Use anti-aliasing for smooth lines
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	        // Draw Map
			if (max > 0.0) {
				for (int x = 0; x < map.length; x++) {
			        for (int y = 0; y < map[0].length; y++) {
			        	float val = (float) (map[x][y] / max);
						g2.setColor(new Color(val, val*0.6f, val*0.4f));
				        g2.fillRect(x*pixelsPerSquare, y*pixelsPerSquare, pixelsPerSquare, pixelsPerSquare);
			        }
				}
			}
			
			// Draw Matt Damon
			if (drawDamon) {
				g2.setColor(damonColor);
				g2.fillOval(sim.damonLocation.x*pixelsPerSquare,
						sim.damonLocation.y*pixelsPerSquare, pixelsPerSquare, pixelsPerSquare);
			}
			
			// Draw Objects
			if (drawObjects) {
				g2.setColor(objectColor);
				for (int i = 0; i < sim.objectLocations.length; i++)
					g2.fillOval(sim.objectLocations[i].x*pixelsPerSquare,
							sim.objectLocations[i].y*pixelsPerSquare, pixelsPerSquare, pixelsPerSquare);
			}
			
			// Draw Rover
			if (drawRover) {
				g2.setColor(roverColor);
				g2.fillOval(sim.rover.currentLocation.x*pixelsPerSquare,
						sim.rover.currentLocation.y*pixelsPerSquare, pixelsPerSquare, pixelsPerSquare);
			}
			
			// Draw starting location (if not obscured by rover)
			if (drawStart && !sim.rover.currentLocation.equals(sim.rover.startingLocation)) {
				g2.setColor(startingColor);
				g2.fillOval(sim.rover.startingLocation.x*pixelsPerSquare,
						sim.rover.startingLocation.y*pixelsPerSquare, pixelsPerSquare, pixelsPerSquare);
			}
			
			// Draw the future plan (with smaller circles)
			if (drawPlan) {
				for (Point p : sim.rover.currentPlan) {
					g2.setColor(planColor);
					g2.fillOval(p.x*pixelsPerSquare + 2, p.y * pixelsPerSquare + 2, pixelsPerSquare - 4, pixelsPerSquare - 4);
				}
			}
	    }
	}
}