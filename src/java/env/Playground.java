package env;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
public class Playground extends Environment {
	
	// (front)(back)(left)(right)
	private static final int RELATIVE_FRONT = 0;
	private static final int RELATIVE_BACK = 1;
	private static final int RELATIVE_LEFT = 2;
	private static final int RELATIVE_RIGHT = 3;
	
	private static final String SCOUT = "scout";
	private static final String DOCTOR = "doctor";
	
	private int W_GRID = 5 + 2;
	private int H_GRID = 6 + 2;
	
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
	
    private Logger logger = Logger.getLogger("optmistor."+ Playground.class.getName());
    
    private static final int SCOUT_ID = 0;
    
    private static final int DELAY = 500;
    
    private static final String DOWN = "down";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final String UP = "up";
     
    private EnvModel model;
    private EnvView view;
    private Simulation simulation;
    
    private boolean simu = true;
    
    public static final Literal goScout = Literal.parseLiteral("go(next)");

    
    private ArrayList<String> thePath;
    private ServerSocket sersock;
    Socket sock;                      
    BufferedReader keyRead ;
	                      
    OutputStream ostream;
    PrintWriter pwrite;

                          
    InputStream istream;
    BufferedReader receiveRead;
    
    public void init(String[] args) {
    	logger.info("Environment initialized.");
    }
    
    
    
    public void buildModel() {
    	
    }
    
	public String sendCommand(String command) {
		
		// send command to the robot via socket
		// until the robot gives back the result
		// while(){}
		// return result
		this.pwrite.println(command);
		String reply="GG";
		try {
			reply = this.receiveRead.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Reply: " + reply);
		return reply;
	}
    
    
    
    
    
    // I hate this method. It's ugly
    // really useful methods
    // action.getFunctor()
    // action.getTerm(0).toString()
    // for example, an action move(down)
    // getFunctor -> move
    // getTerm(0) -> down
    public void buildSock() {
    		System.out.println("Server ready to send command");
    		try {
				boolean connected = false;
				while (!connected) {
					try {
						sock = new Socket("10.0.1.1",18888);
						connected = true;
						logger.info("connected");
					} catch(Exception e) {
						
					}
				}
				
				keyRead = new BufferedReader(new InputStreamReader(System.in));
				ostream = sock.getOutputStream(); 
				pwrite = new PrintWriter(ostream, true);
				istream = sock.getInputStream();
				receiveRead = new BufferedReader(new InputStreamReader(istream));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    }
    
    public void closeSock() {
    		try {
    			receiveRead.close();
    			istream.close();
    			pwrite.close();
    			ostream.close();
    			keyRead.close();
    			sock.close();
				sersock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    }
    public boolean executeAction(String agName, Structure action) {

    	try {
    		if (action.equals(ADD_ALL)) {
    			addAllPositionsToPool();
    		} else if (action.equals(REMOVE_IMPOSSIBLE)) {
    			Thread.sleep(DELAY);
    			removeImpossiblePositions();
    		} else if (action.getFunctor().equals(EXECUTE)) {
        		Thread.sleep(DELAY);
    			moveBeforeLocalization(action.getTerm(0).toString());
    		} else if (action.equals(Literal.parseLiteral("get(color)"))) {
    			if (simu==true) {
    				simulation.getColorFromSimulation();
    			} else {
    				this.getColorFromRobot();
    			}
    			view.repaint();
    		} else if (action.equals(STOP)) {
    			stop();
    		} else if (action.getFunctor().equals("move")) {
        		Thread.sleep(DELAY);
    			moveAfterLocalization(action.getTerm(0).toString());
    		} else if (action.getFunctor().equals("updateModel")) {
    			updateModel(action.getTerm(0).toString(), action.getTerm(1).toString(), action.getTerm(2).toString());
    		} else if (action.getFunctor().equals("plan")) {
    			model.victimsToVisit = this.strToLocSet(action.getTerm(0).toString());
    			logger.info("OK");
    			doPlan();
    		} else if (action.getFunctor().equals("addObject")) {
        		model.set(Integer.valueOf(action.getTerm(0).toString()), Integer.valueOf(action.getTerm(1).toString()),Integer.valueOf(action.getTerm(2).toString()));
        	} else if (action.getFunctor().equals("askOccupied()")) {
        	
        	} else if (action.equals(Literal.parseLiteral("test(com)"))) {
            	this.buildSock();
        		this.sendCommand("jeff's success");
        	} else if (action.equals(Literal.parseLiteral("run(simulation)"))){
        		if (simu == false) {
            		buildSock(); // this line should be deleted
            	} else {
            		simulation = new Simulation(model);
            	}
        		//simulation = new Simulation(model);

        	} else if (action.equals(Literal.parseLiteral("get(nextMove)"))){
    			this.offerBfestMove();
    		} else if (action.equals(Literal.parseLiteral("get(occupied)"))){
    			if (simu==true) {
    				simulation.getOccupiedInfoFromSimulation();
    			} else {
    				this.getOccpuiedInfoFromRobot();
    			}
    			view.repaint();
    		} else if (action.getFunctor().equals("buildModel")) {
    			W_GRID = Integer.valueOf(action.getTerm(0).toString());
    			H_GRID = Integer.valueOf(action.getTerm(1).toString());
    			String victimListStr = action.getTerm(2).toString();
    			String obstacleListStr = action.getTerm(3).toString();
    			HashSet<Location> victimLocs = strToLocSet(victimListStr);
    			HashSet<Location> obstacleLocs = strToLocSet(obstacleListStr);
    			model = new EnvModel(W_GRID, H_GRID, obstacleLocs, victimLocs);
    	        view = new EnvView(model, this);
    	        model.setView(view);
    		} else {
    			return false;
    		}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // convert a Sttring in the foramt of "[A(X1,Y1),B(X2,Y2),C(X3,Y3),...]" to a Location Set
    public HashSet<Location> strToLocSet(String str) {
    	String[] temp = str.split(",");
    	HashSet<Location> list = new HashSet<Location>();
    	int xTemp = 0;
    	int yTemp = 0;
    	for (int i=0; i<=temp.length-1; i++) {
    		String s = temp[i];
    		ArrayList<Character> digits = new ArrayList<Character>();
    		for (char c : s.toCharArray()) {
    			if (Character.isDigit(c) && c!='[' && c!=']') {
    				digits.add(c);
    			}
    		}
    		StringBuilder builder = new StringBuilder(digits.size());
    		for (Character c: digits) {
    			builder.append(c);
    		}
    		s = builder.toString();
    		//System.out.println(s);
    		if (i%2==0) xTemp = Integer.valueOf(s);
    		if (i%2!=0) {
    			yTemp = Integer.valueOf(s);
    			list.add(new Location(xTemp, yTemp));
    		}
    	}
    	//System.out.println(list.toString());
    	//ArrayList<int> indexOfComma = new ArrayList<int>();
    	return list;
    }
    
    public void addOccupiedPercepts(String xInput, String yInput, String heading, String rHeadingInput) {
    	int x = Integer.valueOf(xInput);
    	int y = Integer.valueOf(yInput);
    	int rHeading = Integer.valueOf(rHeadingInput);
    	Position pos = new Position(x,y,heading);
    	if (this.isRelativeOccupied(pos, rHeading)) {
    		this.addPercept(Literal.parseLiteral("occupied("+xInput+","+yInput+","+heading+","+rHeadingInput+")"));
    	}
    }
    
    public void doPlan() {
    	logger.info("plan should not be empty");
    	thePath = convertToExecutablePlan(model.findOrderOfVictimsToVisit(model.getLoc()));
    	logger.info("in do plan"+thePath.size());
    }
    
    public ArrayList<String> convertToExecutablePlan(Location[] orderToVisit) {
    	ArrayList<String> thePlan = new ArrayList<String>();
    	ArrayList<Location> gridsToPass = new ArrayList<Location>();
    	Location loc = model.getLoc();
    	gridsToPass.add(loc);
    	for (int i=0; i<=orderToVisit.length-1; i++){
    		gridsToPass.addAll(model.aStarPathFinding(loc, orderToVisit[i]));
    		loc = orderToVisit[i];
    	}
    	/*
    	gridsToPass.addAll(model.aStarPathFinding(model.getLoc(), orderToVisit[0]));
    	gridsToPass.addAll(model.aStarPathFinding(orderToVisit[0], orderToVisit[1]));
    	gridsToPass.addAll(model.aStarPathFinding(orderToVisit[1], orderToVisit[2]));
    	gridsToPass.addAll(model.aStarPathFinding(orderToVisit[2], orderToVisit[3]));
    	gridsToPass.addAll(model.aStarPathFinding(orderToVisit[3], orderToVisit[4]));
    	*/
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
    
    
    public void moveBeforeLocalization(String actionStr) {
    	int action = Integer.valueOf(actionStr);
    	logger.info("execute" + action);
    	if (simu == false) {
    		moveRobot(action); // this line should be deleted
    	} else {
    		moveSimulation(action);
    	}
    	for (Position poss : model.possiblePosition) poss.relativeMove(action);
    	this.updatePercepts();

    	//this.clearPercepts(SCOUT);
    	//this.removePerceptsByUnif(Literal.parseLiteral("occupised(_)"));
    	//this.removePerceptsByUnif(Literal.parseLiteral("color(_)"));
    }
    
    public void moveAfterLocalization(String direction) {
    	int action = this.absoluteToRelative(direction, model.heading);
    	if (simu == false) {
    		moveRobot(action); // this line should be deleted
    	} else {
    		moveSimulation(action);
    	}
    	System.out.println("want to move" + direction);
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
        if (simu == true) {
        	simulation.realPos = model.getPosition();
        }
        this.clearAllPercepts();
        view.repaint();
    	this.addPercept(model.getPosition().toLiteral());
    }
    
   
    public void moveSimulation(int action) {
    	simulation.realPos.relativeMove(action); 
    }
    
    public void moveRobot(int action) {
    	//simulation.realPos.relativeMove(action); 
    	this.sendCommand("move("+action+")");
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
			addPercept(pos.toLiteral());
		}
	}
	
	void printAllPosition(HashSet<Position> pool) {
		for(int x=0;x<W_GRID;x++) {
			for(int y=0;y<H_GRID;y++) {
				if(!isOccupied(x,y)) {
					Position up = new Position(x,y,UP);
					Position down = new Position(x,y,DOWN);
					Position left = new Position(x,y,LEFT);
					Position right = new Position(x,y,RIGHT);
					if (pool.contains(up)) {
						logger.info(up.toString());
					}
					if (pool.contains(down)) {
						logger.info(down.toString());
					}
					if (pool.contains(left)) {
						logger.info(left.toString());
					}
					if (pool.contains(right)) {
						logger.info(right.toString());
					}
				}
			}
		}
	}
    
    void updatePercepts() {
    	this.clearPercepts(SCOUT);
    	addAllPositionsToScout();
    }
    
    void offerBfestMove() {
    	this.removePerceptsByUnif(Literal.parseLiteral("bestMove(X,Y)"));
    	if (thePath != null && thePath.size() != 0) {
    		addPercept(DOCTOR,Literal.parseLiteral("bestMove("+thePath.get(0)+")"));
            thePath.remove(0);
    	}
    }
    

    

    
    // we are going to modify this part to connect to the robot
    void getEnvInfo() {
    	view.repaint();
    	// before localization finished
    	if (!this.containsPercept(DETERMINED_LOC)) {
    		if (simu == false) {
        	getColorFromRobot();
        	logger.info("got color from robot");
        	getOccpuiedInfoFromRobot();
        	logger.info("got occupany info from robot");
    		} else {
    			simulation.getColorFromSimulation();
    			simulation.getOccupiedInfoFromSimulation();
    		}
    	} else {
    		if (simu==false){
    		getColorFromRobot();
    		} else {
    			simulation.getColorFromSimulation(); 	 
    		}
    	}
    	view.repaint();
    }
    

    
	// !!! this method should be modified !!! �$%^&*())))(*&^%$�$%^&*(*&^%$%^&*
    void getOccpuiedInfoFromRobot() {
    	// this data should be from the robot
    	// a binary string [front,back,left,right]
//	   	if (isRelativeOccupied(simulation.realPos, RELATIVE_FRONT)) addPercept(SCOUT,OCCUPIED_FRONT);
//		if (isRelativeOccupied(simulation.realPos, RELATIVE_BACK)) addPercept(SCOUT,OCCUPIED_BACK);
//	   	if (isRelativeOccupied(simulation.realPos, RELATIVE_LEFT)) addPercept(SCOUT,OCCUPIED_LEFT);
//	   	if (isRelativeOccupied(simulation.realPos, RELATIVE_RIGHT)) addPercept(SCOUT,OCCUPIED_RIGHT);
    	String binaryResult = this.sendCommand("get(occupied)");
    	boolean frontOccupied = binaryResult.charAt(0) == '0' ? false : true;
    	boolean backOccupied = binaryResult.charAt(1) == '0' ? false : true;
    	boolean leftOccupied = binaryResult.charAt(2) == '0' ? false : true;
    	boolean rightOccupied = binaryResult.charAt(3) == '0' ? false : true;
    	if (frontOccupied) addPercept(SCOUT,OCCUPIED_FRONT);
 		if (backOccupied) addPercept(SCOUT,OCCUPIED_BACK);
	   	if (leftOccupied) addPercept(SCOUT,OCCUPIED_LEFT);
	   	if (rightOccupied) addPercept(SCOUT,OCCUPIED_RIGHT);
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
		//logger.info("new real="+simulation.realPos.toString());
		//logger.info("before removing");
		//if (model.possiblePosition.size() <= 20) this.printAllPosition(model.possiblePosition);
		for (Position pos: model.possiblePosition) {
	   		if (containsPercept(SCOUT,OCCUPIED_FRONT) != isRelativeOccupied(pos, RELATIVE_FRONT)) {
	   			clonePool.remove(pos);
	   			continue;
	   		} 
	   		if (containsPercept(SCOUT,OCCUPIED_BACK) != isRelativeOccupied(pos, RELATIVE_BACK)) {
//	   			if (pos.equals(simulation.realPos)){
//	   				logger.info("contain"+containsPercept(OCCUPIED_BACK));
//		   			logger.info("real"+isRelativeOccupied(pos, RELATIVE_BACK));
//	   				logger.info("meila back\n\n\n\n\n");
//	   			}
	   			clonePool.remove(pos);
	   			this.removePercept(SCOUT, pos.toLiteral());
	   			continue;
	   		}  
	   		if (containsPercept(SCOUT,OCCUPIED_LEFT) != isRelativeOccupied(pos, RELATIVE_LEFT)) {
//	   			if (pos.equals(simulation.realPos)){
//	   				logger.info("meila left\n\n\n\n");
//	   				logger.info("contain"+containsPercept(OCCUPIED_LEFT));
//		   			logger.info("real"+isRelativeOccupied(pos, RELATIVE_LEFT));
//	   			}
	   			clonePool.remove(pos);
	   			this.removePercept(SCOUT, pos.toLiteral());
	   			continue;
	   		}  
	   		if (containsPercept(SCOUT,OCCUPIED_RIGHT) != isRelativeOccupied(pos, RELATIVE_RIGHT)) {
//	   			if (pos.equals(simulation.realPos)) {
//	   				logger.info("meila right\n\n\n\n\n\n");
//	   				logger.info("contain"+containsPercept(OCCUPIED_RIGHT));
//		   			logger.info("real"+isRelativeOccupied(pos, RELATIVE_RIGHT));
//	   			}
	   			clonePool.remove(pos);
	   			this.removePercept(SCOUT, pos.toLiteral());
	   			continue;
	   		}
	   		System.out.println("the real color: " + color);
	   		if (!getColorAt(pos.getLoc()).equals(POSSIBLE) && !getColorAt(pos.getLoc()).equals(color)) {
//	   			logger.info("meila color\n\n\n\n\n\n");
//	   			if (pos.equals(simulation.realPos)){
//	   				logger.info("meila color\n\n\n\n\n\n");
//	   			}
	   			clonePool.remove(pos);
	   			this.removePercept(SCOUT, pos.toLiteral());
				continue;
			} 
	   	}
	   	model.possiblePosition = clonePool;
   		//if (model.possiblePosition.size()<=20) this.printAllPosition(model.possiblePosition);
	   	//printAllPosition();
    	// there is only one location possible, then the location is determined
    	if (model.possiblePosition.size()==1) {
    		Position pos = model.possiblePosition.toArray(new Position[1])[0];
    		model.heading = pos.getHeading();
    		model.setAgPos(SCOUT_ID, pos.getLoc());
    		model.localizationFinished = true;
    		this.removePerceptsByUnif(Literal.parseLiteral("pos(_,_,_)"));
    		this.addPercept(pos.toLiteral());
    		addPercept(DETERMINED_LOC);
    	} else {
    		// find ssssssss
    		int bestAction;
    		logger.info("how many?"+model.possiblePosition.size());
    		logger.info("just before search");
    		this.printAllPosition(model.possiblePosition);
    		if (model.possiblePosition.size()>=4){
    			bestAction = chooseAction();
    		} else {
    			bestAction = chooseActionViaTreeSearch(model.possiblePosition);
    		}
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
    
//    public void getColorFromSimulation() {
//    	String color = simulation.getGridColor();
//    	System.out.println("the color I got is " + color);
//    	addPercept(SCOUT,Literal.parseLiteral("color("+color+")"));
//    }
    
    public void getColorFromRobot() {
    	String color = this.sendCommand("get(color)");
    	System.out.println("the color I got is " + color);
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
    boolean canThisActionDistinguish(HashSet<Position> thePool, int action) {
    	@SuppressWarnings("unchecked")
		HashSet<Position> pool = (HashSet<Position>) thePool.clone();
    	logger.info("print of can");
    	logger.info("the size"+pool.size());
    	this.printAllPosition(pool);
    	logger.info("print of can");
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
	    logger.info("diff num"+diffNum);
	    return diffNum>1;
    }
    
    // TODO we need two methods: absolute to relative, relative to absolute
    // and the world will become better
    public int absoluteToRelative(String absDir, String heading) {
    	switch(heading) {
    	case UP:{
    		if (absDir.equals(UP)) return RELATIVE_FRONT;
    		if (absDir.equals(DOWN)) return RELATIVE_BACK;
    		if (absDir.equals(LEFT)) return RELATIVE_LEFT;
    		if (absDir.equals(RIGHT)) return RELATIVE_RIGHT;
    	}
    	case DOWN:{
    		if (absDir.equals(UP)) return RELATIVE_BACK;
    		if (absDir.equals(DOWN)) return RELATIVE_FRONT;
    		if (absDir.equals(LEFT)) return RELATIVE_RIGHT;
    		if (absDir.equals(RIGHT)) return RELATIVE_LEFT;
    	}
    	case LEFT: {
    		if (absDir.equals(UP)) return RELATIVE_RIGHT;
    		if (absDir.equals(DOWN)) return RELATIVE_LEFT;
    		if (absDir.equals(LEFT)) return RELATIVE_FRONT;
    		if (absDir.equals(RIGHT)) return RELATIVE_BACK;
    	}
        case RIGHT:
        	if (absDir.equals(UP)) return RELATIVE_LEFT;
    		if (absDir.equals(DOWN)) return RELATIVE_RIGHT;
    		if (absDir.equals(LEFT)) return RELATIVE_BACK;
    		if (absDir.equals(RIGHT)) return RELATIVE_FRONT;
    	}
    	return -1;
    }
    
    // TODO Convert Relative direction to Absolute Direction
    public String relativeToAbsolute(int relDir, String heading) {
    	switch(heading) {
    	case UP:{
    		if (relDir == RELATIVE_FRONT) return UP;
    		if (relDir == RELATIVE_BACK) return DOWN;
    		if (relDir == RELATIVE_LEFT) return LEFT;
    		if (relDir == RELATIVE_RIGHT) return RIGHT;
    	}
    	case DOWN:{
    		if (relDir == RELATIVE_BACK) return UP;
    		if (relDir == RELATIVE_FRONT) return DOWN;
    		if (relDir == RELATIVE_RIGHT) return LEFT;
    		if (relDir == RELATIVE_LEFT) return RIGHT;
    	}
    	case LEFT: {
    		if (relDir == RELATIVE_RIGHT) return UP;
    		if (relDir == RELATIVE_LEFT) return DOWN;
    		if (relDir == RELATIVE_FRONT) return LEFT;
    		if (relDir == RELATIVE_BACK) return RIGHT;
    	}
        case RIGHT:
        	if (relDir == RELATIVE_LEFT) return UP;
    		if (relDir == RELATIVE_RIGHT) return DOWN;
    		if (relDir == RELATIVE_BACK) return LEFT;
    		if (relDir == RELATIVE_FRONT) return RIGHT;
    	}
    	return "Wrong inputs!";
    }
    
    // do a breadth first tree search - return the first action to make
    public int chooseActionViaTreeSearch(HashSet<Position> thePool) {
    	@SuppressWarnings("unchecked")
		HashSet<Position> workingPool = (HashSet<Position>) thePool.clone();
    	logger.info("just before again");
    	this.printAllPosition(workingPool);
    	logger.info("Tree Search Started");
    	logger.info("just after again");
    	this.printAllPosition(workingPool);
    	Node root = new Node(workingPool,-1);
    	ArrayList<Node> listOfNodes = new ArrayList<Node>();
    	listOfNodes.add(root);
		while(true) {
			ArrayList<Node> newList = new ArrayList<Node>();
			for (Node node : listOfNodes) {
				logger.info("just before again");
				logger.info("the working"+workingPool.size());
		    	this.printAllPosition(workingPool);
		    	logger.info("the node"+node.positionPool.size());
		    	this.printAllPosition(node.positionPool);
				if (!isRelativeOccupied(node.positionPool.iterator().next(), RELATIVE_FRONT)) {
					Node newNode = node.getNewNode(RELATIVE_FRONT);
					if (canThisActionDistinguish(node.positionPool, RELATIVE_FRONT)) {
						logger.info("action chosen"+RELATIVE_FRONT);
						return newNode.getActionTakenInRootNode();
					}
					newList.add(newNode);
				}
				if (!isRelativeOccupied(node.positionPool.iterator().next(), RELATIVE_BACK)){
					Node newNode = node.getNewNode(RELATIVE_BACK);
					if (canThisActionDistinguish(node.positionPool, RELATIVE_BACK)) {
						logger.info("action chosen"+RELATIVE_BACK);
						return newNode.getActionTakenInRootNode();
					}
					newList.add(newNode);
				}
				if(!isRelativeOccupied(node.positionPool.iterator().next(), RELATIVE_LEFT)) {
					Node newNode = node.getNewNode(RELATIVE_LEFT);
					if (canThisActionDistinguish(node.positionPool, RELATIVE_LEFT) ) {
						logger.info("action chosen"+RELATIVE_LEFT);
						return newNode.getActionTakenInRootNode();
					}
					newList.add(newNode);
				}
				if(!isRelativeOccupied(node.positionPool.iterator().next(), RELATIVE_RIGHT)) {
					Node newNode = node.getNewNode(RELATIVE_RIGHT);
					if (canThisActionDistinguish(node.positionPool, RELATIVE_RIGHT)) {
						logger.info("action chosen"+RELATIVE_RIGHT);
						return newNode.getActionTakenInRootNode();
					}
					newList.add(newNode);
				}
			}
			listOfNodes = newList;
			if (newList.size() >= 10000) {
				logger.info("size"+newList.size());
				return 0;
			}
		}
    }
    
    
    
    public HashSet<Position> moveTheWholePool(HashSet<Position> pool, int action) {
    	HashSet<Position> resultPool = new HashSet<Position>();
    	for (Position pos: pool) {
    		Position copy = pos.clone();
    		copy.relativeMove(action);
    		resultPool.add(copy);
    	}
    	return resultPool;
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
    
    
    
    // the simulation class: provide a simulation environment (for testing without robot)
    class Simulation {
    	public Location[] realVictims; 
    	public Position realPos;
    	private Simulation(EnvModel envModel) {
    		realVictims = generateRandomLocsOfRealVictims();
    		realPos = generateRandomStartingPosition();
    	}
    	
    	// generate locations of real victims randomly
    	public Location[] generateRandomLocsOfRealVictims() {
    		List<Location> list = Arrays.asList(model.victimsToVisit.toArray(new Location[model.victimsToVisit.size()]));
    		Collections.shuffle(list);
    		logger.info("Red: "+list.get(0).toString());
    		logger.info("Blue: "+list.get(0).toString());
    		logger.info("Green: "+list.get(0).toString());
    		return new Location[]{list.get(0),list.get(1),list.get(2)};
    	}
    	
    	// generate a starting position randomly
    	public Position generateRandomStartingPosition() {
    		// generate heading
    		List<String> headingList = Arrays.asList(new String[]{LEFT,RIGHT,UP,DOWN});
    		Collections.shuffle(headingList);
    		String heading = headingList.get(2);
    		// generate location
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
    		return new Position(x,y,heading);
    	}
    	
    	public void getColorFromSimulation() {
    		String color;
    		if (containsPercept(DETERMINED_LOC)) realPos = model.getPosition();
    		if (realPos.getLoc().equals(realVictims[0])) {
    			color = RED;
    		} else if (realPos.getLoc().equals(realVictims[1])) {
    			color = BLUE;
    		} else if (realPos.getLoc().equals(realVictims[2])){
    			color = GREEN;
    		} else {
    			color = WHITE;
    		}
    		System.out.println("Simulation: The color is " + color);
    		addPercept(SCOUT,Literal.parseLiteral("color("+color+")"));
    	}
        
        public void getOccupiedInfoFromSimulation() {
    	   	if (isRelativeOccupied(simulation.realPos, RELATIVE_FRONT)) addPercept(SCOUT,OCCUPIED_FRONT);
    		if (isRelativeOccupied(simulation.realPos, RELATIVE_BACK)) addPercept(SCOUT,OCCUPIED_BACK);
    	   	if (isRelativeOccupied(simulation.realPos, RELATIVE_LEFT)) addPercept(SCOUT,OCCUPIED_LEFT);
    	   	if (isRelativeOccupied(simulation.realPos, RELATIVE_RIGHT)) addPercept(SCOUT,OCCUPIED_RIGHT);
        }
    }
    
    // Node class: for breadth first search
    class Node {
    	
    	public HashSet<Position> positionPool;
    	
    	// father node: one action is taken in father node, leading to this node
    	Node father;
    	
    	// action taken in father node
    	int action;
    	
		public Node(HashSet<Position> thePool, int theAction) {
    		positionPool = (HashSet<Position>) thePool.clone();
    		action = theAction;
    	}

		void setFatherNode(Node theFatherNode) {
    		this.father = theFatherNode;
    	}
		
    	Node getFatherNode() {
    		return father;
    	}
    	
    	int getActionTakenInFatherNode() {
    		return action;
    	}
    	
    	// via this tree search, we only want to know one step to move
    	int getActionTakenInRootNode() {
    		int action = getActionTakenInFatherNode();
    		Node father = getFatherNode();
    		while (father.getFatherNode() != null) {
    			action = father.getActionTakenInFatherNode();
    			father = father.getFatherNode();
    		}
    		return action;
    	}
    	
    	// get a new node by taking an action in the current node
    	public Node getNewNode(int action) {
			HashSet<Position> newPool = moveTheWholePool((HashSet<Position>) this.positionPool.clone(), action);
    		Node newNode = new Node(newPool,action);
    		newNode.setFatherNode(this);
    		return newNode;
    	}
    }
}

