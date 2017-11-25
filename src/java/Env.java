
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;


import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;


// the centralised environment
public class Env extends Environment {

	public static final Literal OCCUPIED_DOWN = Literal.parseLiteral("occupied(down)");
	public static final Literal OCCUPIED_RIGHT = Literal.parseLiteral("occupied(right)");
	public static final Literal OCCUPIED_LEFT = Literal.parseLiteral("occupied(left)");
	public static final Literal OCCUPIED_UP = Literal.parseLiteral("occupied(up)");
	public static final String MOVE = "move";
	
    private Logger logger = Logger.getLogger("optmistor."+ Env.class.getName());

    public static final int H_GRID = 6 + 2; 
    public static final int W_GRID = 7 + 2;
    public static final int GARB  = 16;
    
    private static final int SCOUT_ID = 0;
    private static final int DOCTOR_ID = 1;
    private static final Color SCOUT_COLOR = Color.GREEN;
    private static final Color FONT_COLOR = Color.BLACK;
    private static final Color POTENTIAL_VICTIM_COLOR = Color.PINK;
    private static final Color VICTIM_COLOR = Color.RED;
    
    private static final int OBSTACLE = 16;
    private static final int WALL = 128;
    private static final int POTENTIAL_VICTIM = 32;
    private static final int VICTIM = 64;
    
    private static final int DELAY = 500;
    
    private static final String DOWN = "down";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final String UP = "up";
    
    
    private Model model;
    private View  view;
    private Simulation simulation;
    
    /*        Literals        */
    public static final Literal goScout = Literal.parseLiteral("go(next)");
    public static final Literal getPercepts = Literal.parseLiteral("get(percepts)");
    /*        Literals        */
    public void init(String[] args) {
        model = new Model();
        view  = new View(model);
        model.setView(view);
        simulation = new Simulation(model);
        updatePercepts();
    }
    
    // really useful methods
    // action.getFunctor()
    // action.getTerm(0).toString()
    // for example, an action move(down)
    // getFunctor -> move
    // getTerm(0) -> down
    public boolean executeAction(String agName, Structure action) {
        try {
            Thread.sleep(DELAY);
        } catch (Exception e) {}
    	try {
        	if (action.equals(goScout)) {
        		model.scoutGoNext();
            } else if (action.equals(getPercepts)) {
            	model.getPercepts();
            } else if (action.getFunctor().equals(MOVE)){
            	model.move(action.getTerm(0).toString());
            } else if (action.toString().equals("turn(90)")) {
            	logger.info("previous: " + model.heading);
            	switch(model.heading) {
            	case "down":
            		model.heading = "right";
            		break;
            	case "up":
            		model.heading = "left";
            		break;
            	case "left":
            		model.heading = "down";
            		break;
            	case "right":
            		model.heading = "up";
            		break;
            	}
            	logger.info("turn to" + model.heading);
            	updatePercepts();
            } else {
            	return false;
            }
        	
        } catch (Exception e) {
            e.printStackTrace();
        }
        

        return true;
    }

    public void stop() {
        super.stop();
    }
   
    void updatePercepts() {
    	clearPercepts();
    	Location scoutLoc = model.getAgPos(SCOUT_ID);
    	//logger.info("In system, X="+String.valueOf(scoutLoc.x)+"Y="+String.valueOf(scoutLoc.y));
    	Literal scoutPos = Literal.parseLiteral("pos(scout," + scoutLoc.x + "," + scoutLoc.y + ")");
    	addPercept(scoutPos);
    	Literal scoutHeading = Literal.parseLiteral("heading("+model.getHeading()+")");
    	addPercept(scoutHeading);
    }
    
    
    /*    rubbish bin    */
    
    public void delaySome() throws InterruptedException {
    	Thread.sleep(DELAY);
    }
    
    
    
    
    
    
    
    
    
    // this class was totally designed for testing before applying to real robots
    class Simulation {
    	// to do: random generation -> automatic testing
    	public final Location[] obstacles = new Location[]{new Location(2, 2),new Location(4, 6),new Location(5, 3), new Location(4, 4)};
    	public final Location[] potentialVictims = new Location[]{new Location(4, 3),new Location(7, 3),new Location(1, 3),new Location(3, 6),new Location(5, 5)};
    	public Location[] realVictims; 
    	private Simulation(Model model) {
    		// draw obstacles
    		for (Location ob: obstacles){
    			model.add(OBSTACLE, ob);
    		}
    		// draw possible victims
    		for (Location pv: potentialVictims){
    			model.add(POTENTIAL_VICTIM, pv);
    		}
    		// draw walls
    		for (int w = 0; w <= W_GRID - 1; w ++) {
    			for (int h = 0; h <= H_GRID - 1; h ++) {
    				if ((w==0||w==W_GRID-1||h == 0||h==H_GRID-1) && !model.hasObject(WALL,w,h)) {
    					model.add(WALL, w, h);
    				}
    			} 
    		}
    		List<Location> list = Arrays.asList(potentialVictims);
    		Collections.shuffle(list);
    		realVictims = new Location[]{list.get(0),list.get(1),list.get(2)};
    		logger.info("First real victim = "+list.get(0));
    		logger.info("Second real victim = "+list.get(1));
    		logger.info("Third real victim = "+list.get(2));
    		// index -> severity
    	}
    	
    	// simulation -> these methods should be implemented in robots using sensor
    	
    	
    	
    	public boolean isUpOccupied() {
    		Location scoutLoc = model.getAgPos(SCOUT_ID);
    		return isOccupied(scoutLoc.x, scoutLoc.y-1);
    	}
    	
    	public boolean isDownOccupied() {
    		Location scoutLoc = model.getAgPos(SCOUT_ID);
    		return isOccupied(scoutLoc.x, scoutLoc.y+1);
    	}
		
    	public boolean isRightOccupied() {
    		Location scoutLoc = model.getAgPos(SCOUT_ID);
    		return isOccupied(scoutLoc.x+1, scoutLoc.y);	
		}
		
    	public boolean isLeftOccupied() {
    		Location scoutLoc = model.getAgPos(SCOUT_ID);
			return isOccupied(scoutLoc.x-1, scoutLoc.y);
		}
    	
    	public boolean isOccupied(int x, int y) {
    		return (model.hasObject(WALL, x, y) || model.hasObject(OBSTACLE, x, y));
    	}
    	
    }
    
    
    
    
    
    
    
    class Model extends GridWorldModel {
    	private String heading = "down";
    	
	    	String getHeading() {
	    		return heading;
	    	}
    	
        private Model() {
            super(W_GRID, H_GRID, 1);
            setAgPos(SCOUT_ID, 1, 1);
            addInitialBeliefs();
        }
        
        /* add initial beliefs */
        void addInitialBeliefs() {
        	
        }
        
        
        
        
        
        /*      Internal Actions       */
        
        
     	// if possible, go down, if impossible, go right, if impossible, go left, if impossible, go up
        
        // now everything is in simulation, but eventually, these methods should control robots in real world
        
        
        // 0 - down, 1 - right, 2 - left, 3 - up
<<<<<<< HEAD
        void move(int direction) {
        		Location scoutLoc = getAgPos(0);
=======
        void move(String direction) {
        	Location scoutLoc = getAgPos(0);
>>>>>>> origin/jinke
            switch (direction) {
			case DOWN:
				scoutLoc.y += 1;
				heading = "down";
				break;
			case RIGHT:
				scoutLoc.x += 1;
				heading = "right";
				break;
			case LEFT:
				scoutLoc.x -= 1;
				heading = "left";
				break;
			case UP:
				scoutLoc.y -= 1;
				heading = "up";
				break;
			}
            if (new java.util.Random().nextFloat() > 0.5) {
            	strangeTurn();
            }
            setAgPos(0, scoutLoc);
            updatePercepts();
        }
        
        void strangeTurn() {
        	switch(heading) {
        	case "down":
        		heading = "left";
        		break;
        	case "up":
        		heading = "right";
        		break;
        	case "left":
        		heading = "up";
        		break;
        	case "right":
        		heading = "down";
        		break;
        	}
        }
        
        void rotateTo(int newHeading) {
        	
        }
        
        
        
        
        
        
        
        
        
        
        // a stupid strategy -> if can, move down; can't, turn left
        void scoutGoNext() {
            Location scoutLoc = getAgPos(0);
            if (scoutLoc.x != getWidth()-2) {
            	scoutLoc.x += 1;
            } else if (scoutLoc.y != getHeight()-2) {
            	scoutLoc.y += 1;
            	scoutLoc.x = 1;
            }
            setAgPos(0, scoutLoc);
        }
        
        // get informaiton from robots and translate it into beliefs
        void getPercepts() {
        	if (simulation.isDownOccupied()) {
        		logger.info("Down occpuied");
        		addPercept(OCCUPIED_DOWN);
        	}
        	if (simulation.isLeftOccupied()){
        		logger.info("left occpuied");
        		addPercept(OCCUPIED_LEFT);
        	}
        	if (simulation.isRightOccupied()){
        		logger.info("right occpuied");
        		addPercept(OCCUPIED_RIGHT);
        	}
        	if (simulation.isUpOccupied()){
        		logger.info("Up occpuied");
        		addPercept(OCCUPIED_UP);
        	}
        }
        
        // if no belief of the current state is possible, then do localization again -> there may be some mistakes
        
        
        /*      Internal Actions       */
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    // the view of the environment
	class View extends GridWorldView {
		
		private static final long serialVersionUID = 1L;

		public View(Model model) {
            super(model, "Children's Playground", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }
		
        public void draw(Graphics g, int x, int y, int object) {
			switch (object) {
            	case Env.OBSTACLE: 
            		drawObstacle(g, x, y); 
            		break;
            	case Env.WALL:
            		drawObstacle(g, x, y);
            		break;
            	case Env.POTENTIAL_VICTIM: 
            		drawPotentialVictim(g, x, y); 
            		break;
            	case Env.VICTIM:
            		drawVictim(g, x, y);
            		break;
        	}
        }

        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        	super.drawAgent(g, x, y, SCOUT_COLOR, -1);
            g.setColor(FONT_COLOR);
            super.drawString(g, x, y, defaultFont, "Scout");
        }
        
        public void drawPotentialVictim(Graphics g, int x, int y) {
    		g.setColor(POTENTIAL_VICTIM_COLOR);
    		g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
    		g.setColor(FONT_COLOR);
    		super.drawString(g, x, y, new Font("Arial", Font.BOLD, 12), "Potential Victim");
        }
        
        public void drawVictim(Graphics g, int x, int y) {
        	g.setColor(VICTIM_COLOR);
    		g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
    		g.setColor(FONT_COLOR);
    		super.drawString(g, x, y, new Font("Arial", Font.BOLD, 12), "Potential Victim");
        }
    }
}




