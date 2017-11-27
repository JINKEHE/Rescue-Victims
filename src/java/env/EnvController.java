package env;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.ArrayList;
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
	
	// (front)(back)(left)(right)
	private static final int RELATIVE_FRONT = 0;
	private static final int RELATIVE_BACK = 1;
	private static final int RELATIVE_LEFT = 2;
	private static final int RELATIVE_RIGHT = 3;
	
	private static final String SCOUT = "scout";
	private static final String DOCTOR = "doctor";
	
	private static final int W_GRID = 7 + 2;
	private static final int H_GRID = 6 + 2;
	
	static final Literal test = Literal.parseLiteral("test");
	
	public static final Literal COLOR_POSSIBLE = Literal.parseLiteral("color(possible)");
	public static final Literal COLOR_WHITE = Literal.parseLiteral("color(white)");
	public static final Literal COLOR_BLUE = Literal.parseLiteral("colo(blue)");
	public static final Literal COLOR_GREEN = Literal.parseLiteral("color(green)");
	public static final Literal COLOR_RED = Literal.parseLiteral("color(red)");
	public static final Literal OCCUPIED_BACK = Literal.parseLiteral("occupied(back)");
	public static final Literal OCCUPIED_RIGHT = Literal.parseLiteral("occupied(right)");
	public static final Literal OCCUPIED_LEFT = Literal.parseLiteral("occupied(left)");
	public static final Literal OCCUPIED_FRONT = Literal.parseLiteral("occupied(front)");
	public static final Literal DETERMINED_LOC = Literal.parseLiteral("determined(location)");
	public static final Literal ADD_ALL = Literal.parseLiteral("add(all)");
	public static final Literal REMOVE_IMPOSSIBLE = Literal.parseLiteral("remove(impossible)");
    public static final Literal DETECT_ENV = Literal.parseLiteral("detect(env)");
	public static final String  EXECUTE = "execute";
	public static final Literal TASK_FINISHED = Literal.parseLiteral("finished(task)");
	public static final Literal STOP = Literal.parseLiteral("stop(everything)");
	public static final Literal PLAN = Literal.parseLiteral("plan(path)");
	public static final Literal INIT_ENV = Literal.parseLiteral("initialize(env)");
	public static final Literal UPDATE_MODEL = Literal.parseLiteral("update(model)");
	
	public static final String MOVE = "move";
	
	public static final String RED = "red";
	public static final String BLUE = "blue";
	public static final String WHITE = "white";
	public static final String GREEN = "green";
	public static final String POSSIBLE = "possible";
	
    private Logger logger = Logger.getLogger("optmistor."+ EnvController.class.getName());

    public static final int GARB  = 16;
    
    private static final int SCOUT_ID = 0;

    //private static final int VICTIM = 64;
    
    private static final int DELAY = 300;
    
    private static final String DOWN = "down";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final String UP = "up";
     
    private EnvModel model;
    private EnvView view;
    private Simulation simulation;
    
    public static final Literal goScout = Literal.parseLiteral("go(next)");

    
    private ArrayList<String> thePath;
    
    

    public void init(String[] args) {
    	
    	// add initial beliefs here in the demo
    	Location[] obstacles = new Location[]{new Location(2,1),new Location(2,3), new Location(3,3), new Location(3,4)};
    	Location[] possibleVictims = new Location[]{new Location(2,2),new Location(5,6),new Location(4,4),new Location(2,5), new Location(1,4)}; 
    	Set<Location> obstaclesSet = new HashSet<Location>(Arrays.asList(obstacles)); 
		Set<Location> possibleVictimsSet = new HashSet<Location>(Arrays.asList(possibleVictims)); 
    	// create model and view
		model = new EnvModel(W_GRID, H_GRID, obstaclesSet, possibleVictimsSet);
        view = new EnvView(model, this);
        model.setView(view);
        simulation = new Simulation(model);
        addInitialBeliefs(obstacles, possibleVictims);
    }
    
    public void addInitialBeliefs(Location[] obstacles, Location[] possibleVictims) {
    	// add walls
    	for (int w = 0; w <= model.getWidth() - 1; w++) {
			//addPercept(DOCTOR,Literal.parseLiteral("wall("+w+","+0+")"));
			//addPercept(DOCTOR,Literal.parseLiteral("wall("+w+","+(model.getHeight()-1)+")"));
		}
		for (int h = 0; h <= model.getHeight() - 1; h++) {
			//addPercept(DOCTOR,Literal.parseLiteral("wall("+0+","+h+")"));
			//addPercept(DOCTOR,Literal.parseLiteral("wall("+(model.getWidth()-1)+","+h+")"));
		}
		// add obstacles
		for (Location loc : obstacles) {
			addPercept(DOCTOR,Literal.parseLiteral("obstacle("+loc.x+","+loc.y+")"));
		}
		for (Location loc : possibleVictims) {
			addPercept(DOCTOR,Literal.parseLiteral("potentialVictim("+loc.x+","+loc.y+")"));
			addPercept(SCOUT,Literal.parseLiteral("potentialVictim("+loc.x+","+loc.y+")"));
		}
    }
    
    public void initializeEnv() {
    	//
    }
    
    
    /*       the list of internal actions      
     * 				
     * 	 name (in Jason)        Literal (in Java)       corresponding method       functionality
     * 
     *   add(all)               ADD_ALL                 addAllPositions()
     * 
     * 	 execute(X)                                     execute(String action)    execute an movement action               
     * 
     *   detect(env)            DETECT_ENV              getEnvInfo()              
     * 
     *   remove(impossible)     REMOVE_IMPOSSIBLE       removeImpossiblePositions() remove impossible positions; generate the belief bestAction(X)
     *   
     *   
     *   
     * */
    
    
    
    
    
    
    // I hate this method. It's ugly
    // really useful methods
    // action.getFunctor()
    // action.getTerm(0).toString()
    // for example, an action move(down)
    // getFunctor -> move
    // getTerm(0) -> down
    public boolean executeAction(String agName, Structure action) {
    	try {
    		Thread.sleep(DELAY);
    		if (action.equals(ADD_ALL)) {
    			addAllPositionsToPool();
    		} else if (action.equals(REMOVE_IMPOSSIBLE)) {
    			removeImpossiblePositions();
    		} else if (action.getFunctor().equals(EXECUTE)) {
    			execute(action.getTerm(0).toString());
    		} else if (action.equals(DETECT_ENV)) {
    			getEnvInfo();
    		} else if (action.equals(STOP)) {
    			stop();
    		} else if (action.getFunctor().equals("move")) {
    			move(action.getTerm(0).toString());
    		} else if (action.getFunctor().equals("updateModel")) {
    			updateModel(action.getTerm(0).toString(), action.getTerm(1).toString(), action.getTerm(2).toString());
    		} else if (action.equals(PLAN)) {
    			doPlan();
    		} else if (action.equals(INIT_ENV)){
    			initializeEnv();
    		} else {
    			return false;
    		}
        	if (thePath != null && thePath.size() != 0) {
        		addPercept(SCOUT,Literal.parseLiteral("bestMove("+thePath.get(0)+")"));
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    
    public void doPlan() {
    	thePath = convertToExecutablePlan(model.findOrderOfVictimsToVisit(model.getLoc()));
    }
    
    public ArrayList<String> convertToExecutablePlan(Location[] orderToVisit) {
    	ArrayList<String> thePlan = new ArrayList<String>();
    	ArrayList<Location> gridsToPass = new ArrayList<Location>();
    	Location loc = model.getLoc();
    	gridsToPass.add(loc);
    	gridsToPass.addAll(model.aStarPathFinding(model.getLoc(), orderToVisit[0]));
    	gridsToPass.addAll(model.aStarPathFinding(orderToVisit[0], orderToVisit[1]));
    	gridsToPass.addAll(model.aStarPathFinding(orderToVisit[1], orderToVisit[2]));
    	gridsToPass.addAll(model.aStarPathFinding(orderToVisit[2], orderToVisit[3]));
    	gridsToPass.addAll(model.aStarPathFinding(orderToVisit[3], orderToVisit[4]));
    	for (int i=0; i<=gridsToPass.size()-2; i++) {
    		Location next = gridsToPass.get(i+1);
    		loc = gridsToPass.get(i);
    		//logger.info(next.toString());
    		if (next.x == loc.x + 1) {
    			thePlan.add(RIGHT);
    			//logger.info(RIGHT);
    			continue;
    		}
			if (next.x == loc.x - 1) {
				thePlan.add(LEFT);
				//logger.info(LEFT);
				continue;
			}
			if (next.y == loc.y + 1) {
				thePlan.add(DOWN);
				//logger.info(DOWN);
				continue;
			}
			if (next.y == loc.y - 1) {
				thePlan.add(UP);
				//logger.info(UP);
				continue;
			}
    	}
    	return thePlan;
    }
    
    
    public void execute(String actionStr) {
    	int action = Integer.valueOf(actionStr);
    	logger.info("execute" + action);
    	orderRobotToMove(action); // this line should be deleted
    	for (Position poss : model.possiblePosition) poss.relativeMove(action);
    	this.updatePercepts();
    	//this.clearPercepts(SCOUT);
    	//this.removePerceptsByUnif(Literal.parseLiteral("occupised(_)"));
    	//this.removePerceptsByUnif(Literal.parseLiteral("color(_)"));
    }
    
    public void orderRobotToMove(int action) {
    	simulation.realPos.relativeMove(action); 
    }
    
    public void stop() {
        super.stop();
    }
   
    
    void addAllPositionsToPool(){
		for(int x=0;x<W_GRID;x++) {
			for(int y=0;y<H_GRID;y++) {
				if(!isOccupied(x,y)) {
					model.possiblePosition.add(new Position(x,y,UP));
					addPercept(SCOUT, Literal.parseLiteral("pos("+x+","+y+","+UP+")"));
					model.possiblePosition.add(new Position(x,y,DOWN));
					addPercept(SCOUT, Literal.parseLiteral("pos("+x+","+y+","+DOWN+")"));
					model.possiblePosition.add(new Position(x,y,LEFT));
					addPercept(SCOUT, Literal.parseLiteral("pos("+x+","+y+","+LEFT+")"));
					model.possiblePosition.add(new Position(x,y,RIGHT));
					addPercept(SCOUT, Literal.parseLiteral("pos("+x+","+y+","+RIGHT+")"));
				}
			}
		}
	}
    
	void addAllPositionsToScout(){
		for (Position pos : model.possiblePosition){
			addPercept(SCOUT,pos.toLiteral());
		}
	}
	
	void printAllPosition() {
		for(int x=0;x<W_GRID;x++) {
			for(int y=0;y<H_GRID;y++) {
				if(!isOccupied(x,y)) {
					Position up = new Position(x,y,UP);
					Position down = new Position(x,y,DOWN);
					Position left = new Position(x,y,LEFT);
					Position right = new Position(x,y,RIGHT);
					if (model.possiblePosition.contains(up)) {
						logger.info(up.toString());
					}
					if (model.possiblePosition.contains(down)) {
						logger.info(down.toString());
					}
					if (model.possiblePosition.contains(left)) {
						logger.info(left.toString());
					}
					if (model.possiblePosition.contains(right)) {
						logger.info(right.toString());
					}
				}
			}
		}
	}
    
    void updatePercepts() {
    	this.clearPercepts(SCOUT);
    	if (thePath != null && thePath.size() != 0) {
    		addPercept(SCOUT,Literal.parseLiteral("bestMove("+thePath.get(0)+")"));
    	}
    	addAllPositionsToScout();
    }
    

    

    
    void move(String direction) {
    	Location scoutLoc = model.getAgPos(SCOUT_ID);
        switch (direction) {
		case DOWN:
			scoutLoc.y += 1;
			model.heading = DOWN;
			break;
		case RIGHT:
			scoutLoc.x += 1;
			model.heading = RIGHT;
			break;
		case LEFT:
			scoutLoc.x -= 1;
			model.heading = LEFT;
			break;
		case UP:
			scoutLoc.y -= 1;
			model.heading = UP;
			break;
		}
        model.setAgPos(0, scoutLoc);
        model.heading = direction;
        thePath.remove(0);
        this.clearPercepts(SCOUT);
    	if (thePath != null && thePath.size() != 0) {
    		addPercept(SCOUT,Literal.parseLiteral("bestMove("+thePath.get(0)+")"));
    	}
    	this.addPercept(SCOUT, model.getPosition().toLiteral());
    }
    
    // we are going to modify this part to connect to the robot
    void getEnvInfo() {
    	view.repaint();
    	// before localization finished
    	if (!this.containsPercept(DETERMINED_LOC)) {
        	getColorFromRobot();
        	logger.info("got color from robot");
        	getOccpuiedInfoFromRobot();
        	logger.info("got occupany info from robot");
    	} else {
    		getColorFromRobot();
    	}
    	view.repaint();
    }
    
	// !!! this method should be modified !!! £$%^&*())))(*&^%$£$%^&*(*&^%$%^&*
    void getOccpuiedInfoFromRobot() {
    	// this data should be from the robot
	   	if (isRelativeOccupied(simulation.realPos, RELATIVE_FRONT)) addPercept(SCOUT,OCCUPIED_FRONT);
		if (isRelativeOccupied(simulation.realPos, RELATIVE_BACK)) addPercept(SCOUT,OCCUPIED_BACK);
	   	if (isRelativeOccupied(simulation.realPos, RELATIVE_LEFT)) addPercept(SCOUT,OCCUPIED_LEFT);
	   	if (isRelativeOccupied(simulation.realPos, RELATIVE_RIGHT)) addPercept(SCOUT,OCCUPIED_RIGHT);
    }
    
    String getColorInBeliefBase() {
    	if (containsPercept(COLOR_WHITE)) return WHITE;
    	if (containsPercept(COLOR_BLUE)) return BLUE;
    	if (containsPercept(COLOR_GREEN)) return GREEN;
    	if (containsPercept(COLOR_RED)) return RED;
    	if (containsPercept(COLOR_POSSIBLE)) return POSSIBLE;
    	return WHITE;
    }
    
    @SuppressWarnings("unchecked")
	void removeImpossiblePositions(){
    	view.repaint();
    	String color = getColorInBeliefBase();
    	// remove impossible positiosn according to the occupancy information and the color of the grid
		HashSet<Position> clonePool = (HashSet<Position>) model.possiblePosition.clone();
		logger.info("real="+simulation.realPos.toString());
		for (Position pos: model.possiblePosition) {
	   		if (containsPercept(SCOUT,OCCUPIED_FRONT) != isRelativeOccupied(pos, RELATIVE_FRONT)) {
	   			if (pos.equals(simulation.realPos)) {
		   			logger.info("contain"+containsPercept(OCCUPIED_FRONT));
		   			logger.info("real"+isRelativeOccupied(pos, RELATIVE_FRONT));
	   				logger.info("meila front");
	   			}

	   			clonePool.remove(pos);
	   			continue;
	   		} 
	   		if (containsPercept(SCOUT,OCCUPIED_BACK) != isRelativeOccupied(pos, RELATIVE_BACK)) {
	   			if (pos.equals(simulation.realPos)){
	   				logger.info("contain"+containsPercept(OCCUPIED_BACK));
		   			logger.info("real"+isRelativeOccupied(pos, RELATIVE_BACK));
	   				logger.info("meila back");
	   			}
	   			clonePool.remove(pos);
	   			this.removePercept(SCOUT, pos.toLiteral());
	   			continue;
	   		}  
	   		if (containsPercept(SCOUT,OCCUPIED_LEFT) != isRelativeOccupied(pos, RELATIVE_LEFT)) {
	   			if (pos.equals(simulation.realPos)){
	   				logger.info("meila left");
	   				logger.info("contain"+containsPercept(OCCUPIED_LEFT));
		   			logger.info("real"+isRelativeOccupied(pos, RELATIVE_LEFT));
	   			}
	   			clonePool.remove(pos);
	   			this.removePercept(SCOUT, pos.toLiteral());
	   			continue;
	   		}  
	   		if (containsPercept(SCOUT,OCCUPIED_RIGHT) != isRelativeOccupied(pos, RELATIVE_RIGHT)) {
	   			if (pos.equals(simulation.realPos)) {
	   				logger.info("meila right");
	   				logger.info("contain"+containsPercept(OCCUPIED_RIGHT));
		   			logger.info("real"+isRelativeOccupied(pos, RELATIVE_RIGHT));
	   			}
	   			clonePool.remove(pos);
	   			this.removePercept(SCOUT, pos.toLiteral());
	   			continue;
	   		}
	   		if (!getColorAt(pos.getLoc()).equals(POSSIBLE) && !getColorAt(pos.getLoc()).equals(color)) {
	   			if (pos.equals(simulation.realPos)){
	   				logger.info("meila color");
	   			}
	   			clonePool.remove(pos);
	   			this.removePercept(SCOUT, pos.toLiteral());
				continue;
			} 
	   	
	   	}
	   	model.possiblePosition = clonePool;
	   	//printAllPosition();
    	// there is only one location possible, then the location is determined
    	if (model.possiblePosition.size()==1) {
    		Position pos = model.possiblePosition.toArray(new Position[1])[0];
    		model.setAgPos(SCOUT_ID, pos.getLoc());
    		model.heading = pos.getHeading();
    		addPercept(DETERMINED_LOC);
    	} else {
    		// find ssssssss
    		int bestAction = chooseAction();
    		addPercept(SCOUT,Literal.parseLiteral("bestAction("+bestAction+")"));
    		/*
    		if (model.possiblePosition.size()!=2){
        		int bestAction = chooseAction();
        		addPercept(SCOUT,Literal.parseLiteral("bestAction("+bestAction+")"));
    		} else {
    			// what would u do if there are only two possible positions
    			// generic
    			
    		}
    		*/
    	}
    	try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
		}
    	logger.info("real="+simulation.realPos.toString());
    	//printAllPosition();
		view.repaint();
    }
    
    public void updateModel(String color, String xInput, String yInput){
    	int x = Integer.valueOf(xInput);
    	int y = Integer.valueOf(yInput);
    	int object = colorToObject(color);
    	model.set(object, x, y);
    	view.repaint();
    }
    
    public int colorToObject(String color) {
    	if (color.equals(BLUE)) {
    		return EnvModel.BLUE_VICTIM;
    	} else if (color.equals(RED)) {
    		return EnvModel.RED_VICTIM;
    	} else if (color.equals(GREEN)) {
    		return EnvModel.GREEN_VICTIM;
    	} else if (color.equals(POSSIBLE)) {
    		return EnvModel.POTENTIAL_VICTIM;
    	} else if (color.equals(WHITE)){
    		return EnvModel.CLEAN;
    	} else {
    		return EnvModel.CLEAN;
    	}
    }
    
    /*
    // need optimization
    public void updateModel(){
    	for(int w=0; w<=model.getWidth()-1; w++){
    		for (int h=0; h<=model.getHeight()-1; h++){
    			if (containsPercept(DOCTOR,Literal.parseLiteral("red("+w+","+h+")"))){
    				model.set(EnvModel.RED_VICTIM, w, h);
    			} else if (containsPercept(DOCTOR,Literal.parseLiteral("green("+w+","+h+")"))) {
    				model.set(EnvModel.GREEN_VICTIM, w, h);
    			} else if (containsPercept(DOCTOR,Literal.parseLiteral("blue("+w+","+h+")"))) {
    				model.set(EnvModel.BLUE_VICTIM, w, h);
    			} else if (containsPercept(DOCTOR,Literal.parseLiteral("wall("+w+","+h+")"))){
    				model.set(EnvModel.WALL, w, h);
    			} else if (containsPercept(DOCTOR,Literal.parseLiteral("obstacle("+w+","+h+")"))) {
    				model.set(EnvModel.OBSTACLE, w, h);
    			} else if (containsPercept(DOCTOR,Literal.parseLiteral("potentialVictim("+w+","+h+")"))) {
    				model.set(EnvModel.POTENTIAL_VICTIM, w, h);
    			} else {
    				model.set(EnvModel.CLEAN, w, h);
    			}
    		}
    	} 
    	view.repaint();
    }*/
    
    public void getColorFromRobot() {
    	String color = simulation.getGridColor();
    	addPercept(SCOUT,Literal.parseLiteral("color("+color+")"));
    }
    
    
    // get the color of a grid 
    // if there's possible victim and we don't know its serverity, the value is possible
    public String getColorAt(int x, int y) {
    	if (model.hasObject(EnvModel.RED_VICTIM, x, y)){
    		return RED;
    	} else if (model.hasObject(EnvModel.BLUE_VICTIM, x, y)) {
    		return BLUE;
    	} else if (model.hasObject(EnvModel.GREEN_VICTIM, x, y)) {
    		return GREEN;
    	} else if (model.hasObject(EnvModel.POTENTIAL_VICTIM, x, y)){
    		return POSSIBLE;
    	} else {
    		return WHITE;
    	}
    }
    
    public String getColorAt(Location loc) {
    	int x = loc.x;
    	int y = loc.y;
    	if (model.hasObject(EnvModel.RED_VICTIM, x, y)){
    		return RED;
    	} else if (model.hasObject(EnvModel.BLUE_VICTIM, x, y)) {
    		return BLUE;
    	} else if (model.hasObject(EnvModel.GREEN_VICTIM, x, y)) {
    		return GREEN;
    	} else if (model.hasObject(EnvModel.POTENTIAL_VICTIM, x, y)){
    		return POSSIBLE;
    	} else {
    		return WHITE;
    	}
    }
    
    
    public boolean isOccupied(int x, int y) {
		return (model.hasObject(EnvModel.WALL, x, y) || model.hasObject(EnvModel.OBSTACLE, x, y));
	}
 
    
    
    // for a position, there are 16=2^4 possibilities of occupancy around it.
    // maybe we can use binary number to represent each case
    // (front)(back)(left)(right)
    String getRelativeOccupiedInfo(Position pos){
    	String str = "";
    	if (isRelativeOccupied(pos,RELATIVE_FRONT)) str += RELATIVE_FRONT;
    	if (isRelativeOccupied(pos,RELATIVE_BACK)) str += RELATIVE_BACK;
    	if (isRelativeOccupied(pos,RELATIVE_LEFT)) str += RELATIVE_LEFT;
    	if (isRelativeOccupied(pos,RELATIVE_RIGHT)) str += RELATIVE_RIGHT;
    	return str;
    }
    
    // the double to return is equal to (the number of different scenario after this action/the total number of scenarios after this action)
    double howThisActionCanDistinguish(HashSet<Position> pool, int action) {
    	// if action is not valid, return -1
	    ArrayList<String> results = new ArrayList<String>();
	    for (Position pos : pool) {
		    // clone the position (if we use the original one, we need to move it back, which I don't want to do)
		    Position copy = pos.clone();
		    // simulate the movement
		    copy.relativeMove(action);
		    results.add(getRelativeOccupiedInfo(copy));
	    }
	    double totalNum = results.size();
	    double diffNum = new HashSet<String>(results).size();
	    return diffNum/totalNum;
    }
    
    // if at least one possible position can be removed, than diffNum > 2
    
    // whether this action can remove at least one possible position
    boolean canThisActionDistinguish(HashSet<Position> pool, int action) {
    	// if action is not valid, return -1
	    ArrayList<String> results = new ArrayList<String>();
	    for (Position pos : pool) {
		    // clone the position (if we use the original one, we need to move it back, which I don't want to do)
		    Position copy = pos.clone();
		    // simulate the movement
		    copy.relativeMove(action);
		    results.add(getRelativeOccupiedInfo(copy));
	    }
	    double diffNum = new HashSet<String>(results).size();
	    return diffNum>1;
    }
    
    
    int chooseAction() {
    	double[] resultsOfActions = new double[4];
    	// for invalid actions, the results are -1
    	resultsOfActions[RELATIVE_FRONT] = containsPercept(SCOUT,OCCUPIED_FRONT) ? -1 : howThisActionCanDistinguish(model.possiblePosition, RELATIVE_FRONT);
    	resultsOfActions[RELATIVE_BACK] = containsPercept(SCOUT,OCCUPIED_BACK) ? -1 : howThisActionCanDistinguish(model.possiblePosition, RELATIVE_BACK);
    	resultsOfActions[RELATIVE_LEFT] = containsPercept(SCOUT,OCCUPIED_LEFT) ? -1 : howThisActionCanDistinguish(model.possiblePosition, RELATIVE_LEFT);
    	resultsOfActions[RELATIVE_RIGHT] = containsPercept(SCOUT,OCCUPIED_RIGHT) ? -1 : howThisActionCanDistinguish(model.possiblePosition, RELATIVE_RIGHT);
    	// simply pick the one with the maximum value
    	int action = 0;
    	// requrie strategy to break tie because it there's a tie, the robot may 
    	// end up in an infinite loop
    	// therefore, we need some randomity for ties
    	for (int i=0; i<=3; i++){
    		if (resultsOfActions[i] > resultsOfActions[action]) {
    			action = i;
    		} else if (resultsOfActions[i] == resultsOfActions[action]) {
    			if (new Random().nextFloat() > 0.5) {
    				action = i;
    			}
    		}
    	}
    	return action;
    }
    

    
    // remove impossible locations
    /*
    void cleanDirty() {
    	for (Position pos : envModel.possiblePosition) {
    		// rFront[0], rBack[1], rLeft[2], rRight[3]
    		
    	}
    }
    */
    

	public boolean isRelativeOccupied(Position pos, int rHeading) {
		String abs = Position.getAbsoluteHeading(pos.getHeading(), rHeading);
		int x = pos.getX();
		int y = pos.getY();
		if (abs.equals(UP)) return isOccupied(x, y-1);
		if (abs.equals(DOWN)) return isOccupied(x, y+1);
		if (abs.equals(LEFT)) return isOccupied(x-1, y);
		if (abs.equals(RIGHT)) return isOccupied(x+1, y);
		return true;
	}
    
    
    
    // belief: color()
    // colors are red = critical, blue, green
    
    
    // this class was totally designed for testing before applying to real robots
    class Simulation {
    	// to do: random generation -> automatic testin
    	public Location[] realVictims; 
    	public Position realPos;
    	private Simulation(EnvModel envModel) {
    		List<Location> list = Arrays.asList(model.victimsToVisit.toArray(new Location[model.victimsToVisit.size()]));
    		Collections.shuffle(list);
    		realVictims = new Location[]{list.get(0),list.get(1),list.get(2)};
    		/*
    		logger.info("First real victim = "+list.get(0));
    		logger.info("Second real victim = "+list.get(1));
    		logger.info("Third real victim = "+list.get(2));
    		*/
    		realPos = new Position(5, 2, "down");
    		// generate random heading
    		List<String> headingList = Arrays.asList(new String[]{LEFT,RIGHT,UP,DOWN});
    		Collections.shuffle(headingList);
    		String heading = headingList.get(1);
    		// generate random position
    		int x=1, y=1;
    		while (true) {
    			Random rn = new Random();
    			x=rn.nextInt(W_GRID-2)+1;
    			y=rn.nextInt(H_GRID-2)+1;
    			if (model.isOccpuied(x, y)){
    				continue;
    			} else {
    				break;
    			}
    		}
    		realPos = new Position(x,y,heading);
    		// index -> severity
    	}
    	
    	// simulation -> these methods should be implemented in robots using sensor
    	
    	// jinke's trash
    	public boolean isUpOccupied() {
    		return isOccupied(realPos.getX(), realPos.getY()-1);
    	}
    	
    	public boolean isDownOccupied() {
    		return isOccupied(realPos.getX(), realPos.getY()+1);
    	}
		
    	public boolean isRightOccupied() {
    		return isOccupied(realPos.getX()+1, realPos.getY());	
		}
		
    	public boolean isLeftOccupied() {
			return isOccupied(realPos.getX()-1, realPos.getY());
		}
    	

    	
    	
    	// rFront[0], rBack[1], rLeft[2], rRight[3]
    	
    	public String getGridColor() {
    		if (containsPercept(DETERMINED_LOC)) realPos = model.getPosition();
    		if (realPos.getLoc().equals(realVictims[0])) {
    			return RED;
    		} else if (realPos.getLoc().equals(realVictims[1])) {
    			return BLUE;
    		} else if (realPos.getLoc().equals(realVictims[2])){
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


