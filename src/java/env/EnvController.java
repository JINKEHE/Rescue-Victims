package env;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Logger;


import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;


// the centralised environment
public class EnvController extends Environment {

	private static final int W_GRID = 7 + 2;
	private static final int H_GRID = 6 + 2;
	
	public static final Literal OCCUPIED_DOWN = Literal.parseLiteral("occupied(down)");
	public static final Literal OCCUPIED_RIGHT = Literal.parseLiteral("occupied(right)");
	public static final Literal OCCUPIED_LEFT = Literal.parseLiteral("occupied(left)");
	public static final Literal OCCUPIED_UP = Literal.parseLiteral("occupied(up)");
	public static final String MOVE = "move";
	
	public static final String RED = "red";
	public static final String BLUE = "blue";
	public static final String WHITE = "white";
	public static final String GREEN = "green";
	
    private Logger logger = Logger.getLogger("optmistor."+ EnvController.class.getName());

    public static final int GARB  = 16;
    
    private static final int SCOUT_ID = 0;

    private static final int OBSTACLE = 16;
    private static final int WALL = 128;
    private static final int POTENTIAL_VICTIM = 32;
    //private static final int VICTIM = 64;
    
    private static final int DELAY = 500;
    
    private static final String DOWN = "down";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final String UP = "up";
     
    private EnvModel envModel;
    private EnvView envView;
    private Simulation simulation;
    

    
    public static final Literal goScout = Literal.parseLiteral("go(next)");
    public static final Literal getEnvInfo = Literal.parseLiteral("get(info)");
    public static final Literal allPos = Literal.parseLiteral("allPos(Scout)");

    public void init(String[] args) {
    	// add initial beliefs here in the demo
    	Location[] obstacles = new Location[]{new Location(2,1),new Location(2,3), new Location(3,3), new Location(3,4)};
    	Location[] possibleVictims = new Location[]{new Location(2,2),new Location(5,6),new Location(4,4),new Location(2,5), new Location(1,4)}; 
    	Set<Location> obstaclesSet = new HashSet<Location>(Arrays.asList(obstacles)); 
		Set<Location> possibleVictimsSet = new HashSet<Location>(Arrays.asList(possibleVictims)); 
    	// create model and view
		envModel = new EnvModel(W_GRID, H_GRID, obstaclesSet, possibleVictimsSet, true);
        envView = new EnvView(envModel, this);
        envModel.setView(envView);
        simulation = new Simulation(envModel);
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
        		scoutGoNext();
            } else if (action.equals(getEnvInfo)) {
            	getEnvInfo();
            } else if (action.equals(allPos)) {
            	logger.info("exec");
            	addAllPosition();
            } else if (action.getFunctor().equals(MOVE)){
            	move(action.getTerm(0).toString());
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
   
    void clearDirty() {
//        if (!percepts.isEmpty()) {
//            uptodateAgs.clear();
//            percepts.clear();
//        }
    		//this.getPercepts("Scout");
    		//this.removePercept(per);
    }
    
	void addAllPosition(){
		for(int x=0;x<W_GRID;x++) {
			for(int y=0;y<H_GRID;y++) {
				if(!envModel.hasObject(OBSTACLE,x,y)&&!envModel.hasObject(WALL,x,y)) {
					logger.info("ff");
					envModel.possiblePosition.add(new Position(x,y,UP));
					envModel.possiblePosition.add(new Position(x,y,DOWN));
					envModel.possiblePosition.add(new Position(x,y,LEFT));
					envModel.possiblePosition.add(new Position(x,y,RIGHT));
				}
			}
		}
	}
    
    void updatePercepts() {
    	clearPercepts();
    	Literal scoutPos = envModel.getPos();
    	addPercept(scoutPos);
    	
    }
    
    
    void scoutGoNext() {
        Location scoutLoc = envModel.getAgPos(0);
        if (scoutLoc.x != envModel.getWidth()-2) {
        	scoutLoc.x += 1;
        } else if (scoutLoc.y != envModel.getHeight()-2) {
        	scoutLoc.y += 1;
        	scoutLoc.x = 1;
        }
        envModel.setAgPos(SCOUT_ID, scoutLoc);
    }
    
    void move(String direction) {
    	Location scoutLoc = envModel.getAgPos(0);
        switch (direction) {
		case DOWN:
			scoutLoc.y += 1;
			envModel.heading = "down";
			break;
		case RIGHT:
			scoutLoc.x += 1;
			envModel.heading = "right";
			break;
		case LEFT:
			scoutLoc.x -= 1;
			envModel.heading = "left";
			break;
		case UP:
			scoutLoc.y -= 1;
			envModel.heading = "up";
			break;
		}
        envModel.setAgPos(0, scoutLoc);
        updatePercepts();
    }
    
    // we are going to modify this part to connect to the robot
    void getEnvInfo() {
    	if (simulation.isDownOccupied()) {
    		//logger.info("Down occpuied");
    		addPercept(OCCUPIED_DOWN);
    	}
    	if (simulation.isLeftOccupied()){
    		//logger.info("left occpuied");
    		addPercept(OCCUPIED_LEFT);
    	}
    	if (simulation.isRightOccupied()){
    		//logger.info("right occpuied");
    		addPercept(OCCUPIED_RIGHT);
    	}
    	if (simulation.isUpOccupied()){
    		//logger.info("Up occpuied");
    		addPercept(OCCUPIED_UP);
    	}
    	String color = simulation.getGridColor();
    	logger.info("the color is " + color);
    	addPercept("color("+color+")");
    }
    
    // belief: color()
    // colors are red = critical, blue, green
    
    
    // this class was totally designed for testing before applying to real robots
    class Simulation {
    	// to do: random generation -> automatic testing
    	public final Location[] obstacles = new Location[]{new Location(2, 2),new Location(4, 6),new Location(5, 3), new Location(4, 4)};
    	public final Location[] potentialVictims = new Location[]{new Location(4, 3),new Location(7, 3),new Location(1, 3),new Location(3, 6),new Location(5, 5)};
    	public Location[] realVictims; 
    	private Simulation(EnvModel envModel) {
    		// draw obstacles
    		for (Location ob: obstacles){
    			envModel.add(OBSTACLE, ob);
    		}
    		// draw possible victims
    		for (Location pv: potentialVictims){
    			envModel.add(POTENTIAL_VICTIM, pv);
    		}
    		// draw walls
    		for (int w = 0; w <= W_GRID - 1; w ++) {
    			for (int h = 0; h <= H_GRID - 1; h ++) {
    				if ((w==0||w==W_GRID-1||h == 0||h==H_GRID-1) && !envModel.hasObject(WALL,w,h)) {
    					envModel.add(WALL, w, h);
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
    		Location scoutLoc = envModel.getAgPos(SCOUT_ID);
    		return isOccupied(scoutLoc.x, scoutLoc.y-1);
    	}
    	
    	public boolean isDownOccupied() {
    		Location scoutLoc = envModel.getAgPos(SCOUT_ID);
    		return isOccupied(scoutLoc.x, scoutLoc.y+1);
    	}
		
    	public boolean isRightOccupied() {
    		Location scoutLoc = envModel.getAgPos(SCOUT_ID);
    		return isOccupied(scoutLoc.x+1, scoutLoc.y);	
		}
		
    	public boolean isLeftOccupied() {
    		Location scoutLoc = envModel.getAgPos(SCOUT_ID);
			return isOccupied(scoutLoc.x-1, scoutLoc.y);
		}
    	
    	public boolean isOccupied(int x, int y) {
    		return (envModel.hasObject(WALL, x, y) || envModel.hasObject(OBSTACLE, x, y));
    	}
    	
    	public String getGridColor() {
    		Location scoutLoc = envModel.getAgPos(SCOUT_ID);
    		if (scoutLoc.equals(realVictims[0])) {
    			return RED;
    		} else if (scoutLoc.equals(realVictims[1])) {
    			return BLUE;
    		} else if (scoutLoc.equals(realVictims[2])){
    			return GREEN;
    		} else {
    			return WHITE;
    		}
    	}
    }
}



/* rubbish 

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
    
    // add initial beliefs 
    void addInitialBeliefs() {
    	
    }
    
    
    
    
    
    //     Internal Actions   
    
    
 	// if possible, go down, if impossible, go right, if impossible, go left, if impossible, go up
    
    // now everything is in simulation, but eventually, these methods should control robots in real world
    
    
    // 0 - down, 1 - right, 2 - left, 3 - up
    void move(String direction) {
    	Location scoutLoc = getAgPos(0);
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
    
    
    //      Internal Actions 
}

*/

/*
	else if (action.toString().equals("turn(90)")) {
            	logger.info("previous: " + envModel.heading);
            	switch(envModel.heading) {
            	case "down":
            		envModel.heading = "right";
            		break;
            	case "up":
            		envModel.heading = "left";
            		break;
            	case "left":
            		envModel.heading = "down";
            		break;
            	case "right":
            		envModel.heading = "up";
            		break;
            	}
            	logger.info("turn to" + envModel.heading);
            	updatePercepts();
            }

 */


