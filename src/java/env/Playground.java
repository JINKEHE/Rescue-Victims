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

// the environment
public class Playground extends Environment {

    // absolute directions
    private static final String DOWN = "down";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final String UP = "up";

    // relative directions
    private static final int RELATIVE_FRONT = 0;
    private static final int RELATIVE_BACK = 1;
    private static final int RELATIVE_LEFT = 2;
    private static final int RELATIVE_RIGHT = 3;

    // color constants
    private static final String RED = "red";
    private static final String BLUE = "blue";
    private static final String WHITE = "white";
    private static final String GREEN = "green";
    private static final String POSSIBLE = "possible";

    // agent names and id
    private static final String SCOUT = "scout";
    private static final String DOCTOR = "doctor";
    private static final int SCOUT_ID = 0;

    private int W_GRID = 6 + 2; // will be overwritten later
    private int H_GRID = 6 + 2; // will be overwritten later

    // constant literals
    public static final Literal COLOR_POSSIBLE = Literal.parseLiteral("color(possible)");
    public static final Literal COLOR_WHITE = Literal.parseLiteral("color(white)");
    public static final Literal COLOR_BLUE = Literal.parseLiteral("color(blue)");
    public static final Literal COLOR_GREEN = Literal.parseLiteral("color(green)");
    public static final Literal COLOR_RED = Literal.parseLiteral("color(red)");
    public static final Literal OCCUPIED_BACK = Literal.parseLiteral("occupied(back)");
    public static final Literal OCCUPIED_RIGHT = Literal.parseLiteral("occupied(right)");
    public static final Literal OCCUPIED_LEFT = Literal.parseLiteral("occupied(left)");
    public static final Literal OCCUPIED_FRONT = Literal.parseLiteral("occupied(front)");
    public static final Literal DETERMINED_LOC = Literal.parseLiteral("determined(location)");
    public static final Literal ADD_ALL = Literal.parseLiteral("add(allPossiblePositions)");
    public static final Literal REMOVE_IMPOSSIBLE = Literal.parseLiteral("remove(impossible)");
    public static final Literal DETECT_ENV = Literal.parseLiteral("detect(env)");

    public static final Literal TASK_FINISHED = Literal.parseLiteral("finished(task)");
    public static final Literal STOP = Literal.parseLiteral("stop(everything)");
    public static final Literal INIT_ENV = Literal.parseLiteral("initialize(env)");
    public static final Literal goScout = Literal.parseLiteral("go(next)");
    public static final Literal START_SOCKET = Literal.parseLiteral("start(socket)");
    public static final Literal START_SIMULATION = Literal.parseLiteral("start(simulation)");
    public static final Literal GET_NEXT_MOVE_TO_VISIT = Literal.parseLiteral("get(nextMove)");
    public static final Literal GET_NEXT_MOVE_TO_LOCALIZE = Literal.parseLiteral("get(nextMoveToLocalize)");
    public static final Literal GET_OCCUPIED_INFO = Literal.parseLiteral("get(occupiedInfo)");
    public static final Literal GET_COLOR = Literal.parseLiteral("get(color)");

    // names of functors
    private static final String MOVE = "move";
    private static final String PLAN = "plan";
    private static final String ADD_OBJECT = "addObject";
    private static final String UPDATE_MODEL = "updateModel";
    private static final String BUILD_MODEL = "buildModel";
    public static final String MOVE_BEFORE = "moveBefore";
    public static final String MOVE_AFTER = "moveAfter";

    // a logger
    private Logger logger = Logger.getLogger("optmistor." + Playground.class.getName());

    // model and view and simulation
    private EnvModel model;
    private EnvView view;
    private Simulation simulation;

    // socket related variables
    private Socket sock;
    private ServerSocket server;
    private OutputStream oStream;
    private PrintWriter output;
    private InputStream iStream;
    private BufferedReader input;
    private static final String ROBOT_IP = "10.0.1.1";
    private static final int ROBOT_PORT = 18888;

    // the path to find possible victims, which consists of relative directions
    // to move
    private ArrayList<String> thePathToFindVictims;
    public ArrayList<Location> gridsToPass;
    
    // the time of delay
    private static final int DELAY = 500;

    // number of steps taken by the robot
    private int stepsTaken = 0;

    // whether or not use simulation
    private boolean useSimulation = true;

    public void init(String[] args) {
        logger.info("Environment initialized.");
    }

    public boolean executeAction(String agName, Structure action) {
        System.out.println("Jason called action: " + action.toString());
        try {
            if (action.equals(ADD_ALL)) {
                addAllPositionsToPool();
            } else if (action.equals(REMOVE_IMPOSSIBLE)) {
                removeImpossiblePositions();
            } else if (action.getFunctor().equals(MOVE_BEFORE)) {
                moveBeforeLocalization(action.getTerm(0).toString());
            } else if (action.equals(GET_COLOR)) {
                if (useSimulation == true) {
                    simulation.getColorFromSimulation();
                } else {
                    getColorFromRobot();
                }
            } else if (action.equals(STOP)) {
                if (output != null)
                    sendCommand("end");
                gridsToPass.clear();
                view.update();
                stop();
            } else if (action.getFunctor().equals(MOVE_AFTER)) {
                moveAfterLocalization(action.getTerm(0).toString());
            } else if (action.getFunctor().equals(UPDATE_MODEL)) {
                updateModel(action.getTerm(0).toString(), action.getTerm(1).toString(), action.getTerm(2).toString());
            } else if (action.getFunctor().equals(PLAN)) {
                makePlan(action.getTerm(0).toString());
            } else if (action.getFunctor().equals(ADD_OBJECT)) {
                model.set(Integer.valueOf(action.getTerm(0).toString()), Integer.valueOf(action.getTerm(1).toString()),
                Integer.valueOf(action.getTerm(2).toString()));
            } else if (action.equals(GET_NEXT_MOVE_TO_VISIT)) {
                provideNextMove();
            } else if (action.equals(GET_OCCUPIED_INFO)) {
                if (useSimulation == true) {
                    simulation.getOccupiedInfoFromSimulation();
                } else {
                    getOccpuiedInfoFromRobot();
                }
            } else if (action.getFunctor().equals(BUILD_MODEL)) {
                buildModel(action);
            } else if (action.equals(START_SIMULATION)) {
                simulation = new Simulation(model);
                useSimulation = true;
            } else if (action.equals(START_SOCKET)) {
                buildSocket();
                useSimulation = false;
            } else if (action.equals(GET_NEXT_MOVE_TO_LOCALIZE)) {
                getNextActionToTakeToLocalize();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // build the model with the information from doctor agent
    private void buildModel(Literal action) {
        W_GRID = Integer.valueOf(action.getTerm(0).toString());
        H_GRID = Integer.valueOf(action.getTerm(1).toString());
        String victimListStr = action.getTerm(2).toString();
        String obstacleListStr = action.getTerm(3).toString();
        HashSet<Location> victimLocs = stringToLocationSet(victimListStr);
        HashSet<Location> obstacleLocs = stringToLocationSet(obstacleListStr);
        model = new EnvModel(W_GRID, H_GRID, obstacleLocs, victimLocs);
        view = new EnvView(model, this);
        model.setView(view);
    }

    // send a command to the robot and wait for a reply
    private String sendCommand(String command) {
        String reply = "GG";
        output.println(command);
        System.out.println("Sent command: " + command);
        try {
            reply = this.input.readLine();
        } catch (IOException e) {
        }
        return reply;
    }

    // build a socket connection with the robot
    private void buildSocket() {
        try {
            // try to connect to the server
            boolean connected = false;
            // if failed, try again until a connection is established
            while (!connected) {
                try {
                    sock = new Socket(ROBOT_IP, ROBOT_PORT);
                    connected = true;
                    logger.info("connected");
                } catch (Exception e) {
                }
            }
            // set up streams
            oStream = sock.getOutputStream();
            output = new PrintWriter(oStream, true);
            iStream = sock.getInputStream();
            input = new BufferedReader(new InputStreamReader(iStream));
        } catch (IOException e) {
        }
        logger.info("Server ready to send command");
    }

    // close the socket
    private void closeSocket() {
        try {
            input.close();
            iStream.close();
            output.close();
            oStream.close();
            sock.close();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // convert a Sttring in the foramt of "[A(X1,Y1),B(X2,Y2),C(X3,Y3),...]" to
    // a Location Set
    private HashSet<Location> stringToLocationSet(String str) {
        String[] temp = str.split(",");
        HashSet<Location> list = new HashSet<Location>();
        int xTemp = 0;
        int yTemp = 0;
        for (int i = 0; i <= temp.length - 1; i++) {
            String s = temp[i];
            ArrayList<Character> digits = new ArrayList<Character>();
            for (char c : s.toCharArray()) {
                if (Character.isDigit(c) && c != '[' && c != ']') {
                    digits.add(c);
                }
            }
            StringBuilder builder = new StringBuilder(digits.size());
            for (Character c : digits) {
                builder.append(c);
            }
            s = builder.toString();
            if (i % 2 == 0)
                xTemp = Integer.valueOf(s);
            if (i % 2 != 0) {
                yTemp = Integer.valueOf(s);
                list.add(new Location(xTemp, yTemp));
            }
        }
        return list;
    }

    // make the plan to visit victims
    private void makePlan(String victimsInString) {
        model.victimsToVisit = stringToLocationSet(victimsInString);
        thePathToFindVictims = convertLocationsToExecutablePlan(model.findOrderOfVictimsToVisit(model.getLoc()));
    }

    // convert a plan consisting of locations the robot need to visit to a plan
    // consisting of directions to move
    private ArrayList<String> convertLocationsToExecutablePlan(Location[] orderToVisit) {
        gridsToPass = new ArrayList<Location>();
        ArrayList<String> thePlan = new ArrayList<String>();
        Location loc = model.getLoc();
        gridsToPass.add(loc);
        for (int i = 0; i <= orderToVisit.length - 1; i++) {
            gridsToPass.addAll(model.aStarPathFinding(loc, orderToVisit[i]));
            loc = orderToVisit[i];
        }
        for (int i = 0; i <= gridsToPass.size() - 2; i++) {
            Location next = gridsToPass.get(i + 1);
            loc = gridsToPass.get(i);
            if (next.x == loc.x + 1) {
                thePlan.add(RIGHT);
                continue;
            }
            if (next.x == loc.x - 1) {
                thePlan.add(LEFT);
                continue;
            }
            if (next.y == loc.y + 1) {
                thePlan.add(DOWN);
                continue;
            }
            if (next.y == loc.y - 1) {
                thePlan.add(UP);
                continue;
            }
        }
        return thePlan;
    }

    // move before the localization is finished
    private void moveBeforeLocalization(String actionStr) {
        int action = Integer.valueOf(actionStr);
        logger.info("execute" + action);
        if (useSimulation == false) {
            moveRobot(action, "unknown");
        } else {
            moveSimulation(action);
        }
        for (Position poss : model.possiblePosition)
            poss.relativeMove(action);
        this.updatePercepts();
    }

    // move after the localization is finished
    private void moveAfterLocalization(String direction) {
        int action = this.absoluteToRelative(direction, model.heading);
        if (useSimulation == false) {
            moveRobot(action, model.heading);
        } else {
            moveSimulation(action);
        }
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
        if (useSimulation) {
            simulation.realPos = model.getPosition();
        }
        this.clearAllPercepts();
        view.update();
        this.addPercept(model.getPosition().toLiteral());
    }

    // move in simulation
    private void moveSimulation(int action) {
        simulation.realPos.relativeMove(action);
        stepsTaken++;
    }

    // move when using real robot
    private void moveRobot(int action, String heading) {
        this.sendCommand("move(" + action + "," + heading + ")");
        stepsTaken++;
    }

    // add all possible positions to the pool and belief base as perceptions
    private void addAllPositionsToPool() {
        view.setIgnoreRepaint(true);
        for (int x = 0; x < W_GRID; x++) {
            for (int y = 0; y < H_GRID; y++) {
                if (!isOccupied(x, y)) {
                    model.possiblePosition.add(new Position(x, y, UP));
                    addPercept(SCOUT, Literal.parseLiteral("pos(" + x + "," + y + "," + UP + ")"));
                    model.possiblePosition.add(new Position(x, y, DOWN));
                    addPercept(SCOUT, Literal.parseLiteral("pos(" + x + "," + y + "," + DOWN + ")"));
                    model.possiblePosition.add(new Position(x, y, LEFT));
                    addPercept(SCOUT, Literal.parseLiteral("pos(" + x + "," + y + "," + LEFT + ")"));
                    model.possiblePosition.add(new Position(x, y, RIGHT));
                    addPercept(SCOUT, Literal.parseLiteral("pos(" + x + "," + y + "," + RIGHT + ")"));
                }
            }
        }
        view.setIgnoreRepaint(false);
        view.update();
    }

    // add all possible positions to the scout' belief base as perceptions
    private void addAllPositionsToScout() {
        for (Position pos : model.possiblePosition) {
            addPercept(pos.toLiteral());
        }
    }

    // print all the possible positions (for testing)
    private void printAllPosition(HashSet<Position> pool) {
        for (int x = 0; x < W_GRID; x++) {
            for (int y = 0; y < H_GRID; y++) {
                if (!isOccupied(x, y)) {
                    Position up = new Position(x, y, UP);
                    Position down = new Position(x, y, DOWN);
                    Position left = new Position(x, y, LEFT);
                    Position right = new Position(x, y, RIGHT);
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

    // update the perceptions
    private void updatePercepts() {
        clearPercepts(SCOUT);
        addAllPositionsToScout();
    }

    // add the direction of move to belief base as a perception
    private void provideNextMove() {
        removePerceptsByUnif(Literal.parseLiteral("bestMove(X,Y)"));
        if (thePathToFindVictims != null && thePathToFindVictims.size() != 0) {
            addPercept(DOCTOR, Literal.parseLiteral("bestMove(" + thePathToFindVictims.get(0) + ")"));
            thePathToFindVictims.remove(0);
            gridsToPass.remove(0);
        }
    }

    // get occupied information from robot
    private void getOccpuiedInfoFromRobot() {
        String binaryResult = this.sendCommand("get(occupied)");
        boolean frontOccupied = binaryResult.charAt(0) == '0' ? false : true;
        boolean backOccupied = binaryResult.charAt(1) == '0' ? false : true;
        boolean leftOccupied = binaryResult.charAt(2) == '0' ? false : true;
        boolean rightOccupied = binaryResult.charAt(3) == '0' ? false : true;
        if (frontOccupied)
            addPercept(SCOUT, OCCUPIED_FRONT);
        if (backOccupied)
            addPercept(SCOUT, OCCUPIED_BACK);
        if (leftOccupied)
            addPercept(SCOUT, OCCUPIED_LEFT);
        if (rightOccupied)
            addPercept(SCOUT, OCCUPIED_RIGHT);
    }

    // get color percept
    private String getColorPercept() {
        if (containsPercept(SCOUT, COLOR_WHITE)) {
            return WHITE;
        } else if (containsPercept(SCOUT, COLOR_BLUE)) {
            return BLUE;
        } else if (containsPercept(SCOUT, COLOR_GREEN)) {
            return GREEN;
        } else if (containsPercept(SCOUT, COLOR_RED)) {
            return RED;
        } else if (containsPercept(SCOUT, COLOR_POSSIBLE)) {
            return POSSIBLE;
        }
        return WHITE;
    }

    // remove the impossible positions
    private void removeImpossiblePositions() {
        String color = getColorPercept();
        HashSet<Position> clonePool = (HashSet<Position>) model.possiblePosition.clone();
        for (Position pos : model.possiblePosition) {
            if (containsPercept(SCOUT, OCCUPIED_FRONT) != isRelativeOccupied(pos, RELATIVE_FRONT)) {
                clonePool.remove(pos);
                this.removePercept(SCOUT, pos.toLiteral());
                continue;
            }
            if (containsPercept(SCOUT, OCCUPIED_BACK) != isRelativeOccupied(pos, RELATIVE_BACK)) {
                clonePool.remove(pos);
                this.removePercept(SCOUT, pos.toLiteral());
                continue;
            }
            if (containsPercept(SCOUT, OCCUPIED_LEFT) != isRelativeOccupied(pos, RELATIVE_LEFT)) {
                clonePool.remove(pos);
                this.removePercept(SCOUT, pos.toLiteral());
                continue;
            }
            if (containsPercept(SCOUT, OCCUPIED_RIGHT) != isRelativeOccupied(pos, RELATIVE_RIGHT)) {
                clonePool.remove(pos);
                this.removePercept(SCOUT, pos.toLiteral());
                continue;
            }
            // the real color got is not white but this position has no possible victim
            if (!color.equals(WHITE) && getColorAt(pos.getLoc()).equals(WHITE)) {
                clonePool.remove(pos);
                this.removePercept(SCOUT, pos.toLiteral());
                continue;
            }
        }
        model.possiblePosition = clonePool;
        // there is only one location possible, then the location is determined
        if (model.possiblePosition.size() == 1) {
            Position pos = model.possiblePosition.toArray(new Position[1])[0];
            model.heading = pos.getHeading();
            model.setAgPos(SCOUT_ID, pos.getLoc());
            model.localizationFinished = true;
            removePerceptsByUnif(Literal.parseLiteral("pos(_,_,_)"));
            addPercept(pos.toLiteral());
        }
        view.update();
    }

    // strategies to choose action to localize
    private void getNextActionToTakeToLocalize() {
        int bestAction;
        if (model.possiblePosition.size() >= 3) {
            bestAction = chooseActionByOneStepSearch();
        } else {
            bestAction = chooseActionViaTreeSearch(model.possiblePosition);
        }
        addPercept(DOCTOR, Literal.parseLiteral("nextMoveToLocalize(" + bestAction + ")"));
    }

    // do a one step search - return the action that can reduce the most number
    // of possible positions
    int chooseActionByOneStepSearch() {
        double[] resultsOfActions = new double[4];
        // for invalid actions, the results are -1
        resultsOfActions[RELATIVE_FRONT] = containsPercept(SCOUT, OCCUPIED_FRONT) ? -1
                : howThisActionCanDistinguish(model.possiblePosition, RELATIVE_FRONT);
        resultsOfActions[RELATIVE_BACK] = containsPercept(SCOUT, OCCUPIED_BACK) ? -1
                : howThisActionCanDistinguish(model.possiblePosition, RELATIVE_BACK);
        resultsOfActions[RELATIVE_LEFT] = containsPercept(SCOUT, OCCUPIED_LEFT) ? -1
                : howThisActionCanDistinguish(model.possiblePosition, RELATIVE_LEFT);
        resultsOfActions[RELATIVE_RIGHT] = containsPercept(SCOUT, OCCUPIED_RIGHT) ? -1
                : howThisActionCanDistinguish(model.possiblePosition, RELATIVE_RIGHT);
        // simply pick the one with the maximum value
        int action = 0;
        for (int i = 0; i <= 3; i++) {
            if (resultsOfActions[i] > resultsOfActions[action]) {
                action = i;
            } else if (resultsOfActions[i] == resultsOfActions[action]) {
                // add some randomness
                if (new Random().nextFloat() > 0.5) {
                    action = i;
                }
            }
        }
        return action;
    }

    // do a breadth first tree search - return the first action that can lead to
    // a reduction in the number of possible positions
    // require more computational resources
    public int chooseActionViaTreeSearch(HashSet<Position> thePool) {
        HashSet<Position> workingPool = (HashSet<Position>) thePool.clone();
        Node root = new Node(workingPool, -1);
        ArrayList<Node> listOfNodes = new ArrayList<Node>();
        listOfNodes.add(root);
        while (true) {
            ArrayList<Node> newList = new ArrayList<Node>();
            for (Node node : listOfNodes) {
                if (!isRelativeOccupied(node.positionPool.iterator().next(), RELATIVE_FRONT)) {
                    Node newNode = node.getNewNode(RELATIVE_FRONT);
                    if (canThisActionDistinguish(node.positionPool, RELATIVE_FRONT)) {
                        logger.info("action chosen" + RELATIVE_FRONT);
                        return newNode.getActionTakenInRootNode();
                    }
                    newList.add(newNode);
                }
                if (!isRelativeOccupied(node.positionPool.iterator().next(), RELATIVE_BACK)) {
                    Node newNode = node.getNewNode(RELATIVE_BACK);
                    if (canThisActionDistinguish(node.positionPool, RELATIVE_BACK)) {
                        logger.info("action chosen" + RELATIVE_BACK);
                        return newNode.getActionTakenInRootNode();
                    }
                    newList.add(newNode);
                }
                if (!isRelativeOccupied(node.positionPool.iterator().next(), RELATIVE_LEFT)) {
                    Node newNode = node.getNewNode(RELATIVE_LEFT);
                    if (canThisActionDistinguish(node.positionPool, RELATIVE_LEFT)) {
                        logger.info("action chosen" + RELATIVE_LEFT);
                        return newNode.getActionTakenInRootNode();
                    }
                    newList.add(newNode);
                }
                if (!isRelativeOccupied(node.positionPool.iterator().next(), RELATIVE_RIGHT)) {
                    Node newNode = node.getNewNode(RELATIVE_RIGHT);
                    if (canThisActionDistinguish(node.positionPool, RELATIVE_RIGHT)) {
                        logger.info("action chosen" + RELATIVE_RIGHT);
                        return newNode.getActionTakenInRootNode();
                    }
                    newList.add(newNode);
                }
            }
            listOfNodes = newList;
            if (newList.size() >= 10000) {
                logger.info("Mirror detected.");
                return 0;
            }
        }
    }

    // update the model with the object value
    private void updateModel(String color, String xInput, String yInput) {
        int x = Integer.valueOf(xInput);
        int y = Integer.valueOf(yInput);
        int object = colorToObject(color);
        model.set(object, x, y);
        view.update();
    }

    private int colorToObject(String color) {
        if (color.equals(BLUE)) {
            return EnvModel.BLUE_VICTIM;
        } else if (color.equals(RED)) {
            return EnvModel.RED_VICTIM;
        } else if (color.equals(GREEN)) {
            return EnvModel.GREEN_VICTIM;
        } else if (color.equals(POSSIBLE)) {
            return EnvModel.POTENTIAL_VICTIM;
        } else if (color.equals(WHITE)) {
            return EnvModel.CLEAN;
        } else {
            return EnvModel.CLEAN;
        }
    }

    // get the color from the robot
    public void getColorFromRobot() {
        String color = sendCommand("get(color)");
        logger.info("The color I got is " + color);
        addPercept(SCOUT, Literal.parseLiteral("color(" + color + ")"));
    }

    // get color at a location of the map
    public String getColorAt(Location loc) {
        int x = loc.x;
        int y = loc.y;
        if (model.hasObject(EnvModel.RED_VICTIM, x, y)) {
            return RED;
        } else if (model.hasObject(EnvModel.BLUE_VICTIM, x, y)) {
            return BLUE;
        } else if (model.hasObject(EnvModel.GREEN_VICTIM, x, y)) {
            return GREEN;
        } else if (model.hasObject(EnvModel.POTENTIAL_VICTIM, x, y)) {
            return POSSIBLE;
        } else {
            return WHITE;
        }
    }

    public boolean isOccupied(int x, int y) {
        return (model.hasObject(EnvModel.WALL, x, y) || model.hasObject(EnvModel.OBSTACLE, x, y));
    }

    // for a position, there are 2^4=16 possibilities of occupancy around it.
    // notice that in robot side, a binary number is used to represent this
    String getRelativeOccupiedInfo(Position pos) {
        String str = "";
        if (isRelativeOccupied(pos, RELATIVE_FRONT))
            str += RELATIVE_FRONT;
        if (isRelativeOccupied(pos, RELATIVE_BACK))
            str += RELATIVE_BACK;
        if (isRelativeOccupied(pos, RELATIVE_LEFT))
            str += RELATIVE_LEFT;
        if (isRelativeOccupied(pos, RELATIVE_RIGHT))
            str += RELATIVE_RIGHT;
        return str;
    }

    // the double to return is equal to
    // the number of different scenario after this action/the total number of
    // scenarios after this action
    private double howThisActionCanDistinguish(HashSet<Position> pool, int action) {
        // if action is not valid, return -1
        ArrayList<String> results = new ArrayList<String>();
        for (Position pos : pool) {
            // clone the position
            // if we use the original one, we need to move it back, which I
            // don't want to do
            Position copy = pos.clone();
            // simulate the movement
            copy.relativeMove(action);
            results.add(getRelativeOccupiedInfo(copy));
        }
        double totalNum = results.size();
        double diffNum = new HashSet<String>(results).size();
        return diffNum / totalNum;
    }

    // whether this action can remove at least one possible position
    private boolean canThisActionDistinguish(HashSet<Position> thePool, int action) {
        HashSet<Position> pool = (HashSet<Position>) thePool.clone();
        // if action is not valid, return -1
        ArrayList<String> results = new ArrayList<String>();
        for (Position pos : pool) {
            // clone the position (if we use the original one, we need to move
            // it back, which I don't want to do)
            Position copy = pos.clone();
            // simulate the movement
            copy.relativeMove(action);
            results.add(getRelativeOccupiedInfo(copy));
        }
        double diffNum = new HashSet<String>(results).size();
        return diffNum > 1;
    }

    // convert absolute direction to relative direction
    private int absoluteToRelative(String absDir, String heading) {
        switch (heading) {
        case UP: {
            if (absDir.equals(UP))
                return RELATIVE_FRONT;
            if (absDir.equals(DOWN))
                return RELATIVE_BACK;
            if (absDir.equals(LEFT))
                return RELATIVE_LEFT;
            if (absDir.equals(RIGHT))
                return RELATIVE_RIGHT;
        }
        case DOWN: {
            if (absDir.equals(UP))
                return RELATIVE_BACK;
            if (absDir.equals(DOWN))
                return RELATIVE_FRONT;
            if (absDir.equals(LEFT))
                return RELATIVE_RIGHT;
            if (absDir.equals(RIGHT))
                return RELATIVE_LEFT;
        }
        case LEFT: {
            if (absDir.equals(UP))
                return RELATIVE_RIGHT;
            if (absDir.equals(DOWN))
                return RELATIVE_LEFT;
            if (absDir.equals(LEFT))
                return RELATIVE_FRONT;
            if (absDir.equals(RIGHT))
                return RELATIVE_BACK;
        }
        case RIGHT:
            if (absDir.equals(UP))
                return RELATIVE_LEFT;
            if (absDir.equals(DOWN))
                return RELATIVE_RIGHT;
            if (absDir.equals(LEFT))
                return RELATIVE_BACK;
            if (absDir.equals(RIGHT))
                return RELATIVE_FRONT;
        }
        return -1;
    }

    // Convert Relative direction to Absolute Direction
    private String relativeToAbsolute(int relDir, String heading) {
        switch (heading) {
        case UP: {
            if (relDir == RELATIVE_FRONT)
                return UP;
            if (relDir == RELATIVE_BACK)
                return DOWN;
            if (relDir == RELATIVE_LEFT)
                return LEFT;
            if (relDir == RELATIVE_RIGHT)
                return RIGHT;
        }
        case DOWN: {
            if (relDir == RELATIVE_BACK)
                return UP;
            if (relDir == RELATIVE_FRONT)
                return DOWN;
            if (relDir == RELATIVE_RIGHT)
                return LEFT;
            if (relDir == RELATIVE_LEFT)
                return RIGHT;
        }
        case LEFT: {
            if (relDir == RELATIVE_RIGHT)
                return UP;
            if (relDir == RELATIVE_LEFT)
                return DOWN;
            if (relDir == RELATIVE_FRONT)
                return LEFT;
            if (relDir == RELATIVE_BACK)
                return RIGHT;
        }
        case RIGHT:
            if (relDir == RELATIVE_LEFT)
                return UP;
            if (relDir == RELATIVE_RIGHT)
                return DOWN;
            if (relDir == RELATIVE_BACK)
                return LEFT;
            if (relDir == RELATIVE_FRONT)
                return RIGHT;
        }
        return "Wrong inputs!";
    }

    // move a whole pool of possible positions
    public HashSet<Position> moveTheWholePool(HashSet<Position> pool, int action) {
        HashSet<Position> resultPool = new HashSet<Position>();
        for (Position pos : pool) {
            Position copy = pos.clone();
            copy.relativeMove(action);
            resultPool.add(copy);
        }
        return resultPool;
    }

    public boolean isRelativeOccupied(Position pos, int rHeading) {
        String abs = Position.getAbsoluteHeading(pos.getHeading(), rHeading);
        int x = pos.getX();
        int y = pos.getY();
        if (abs.equals(UP))
            return isOccupied(x, y - 1);
        if (abs.equals(DOWN))
            return isOccupied(x, y + 1);
        if (abs.equals(LEFT))
            return isOccupied(x - 1, y);
        if (abs.equals(RIGHT))
            return isOccupied(x + 1, y);
        return true;
    }

    // the simulation class: provide a simulation environment (for testing
    // without robot)
    class Simulation {
        public Location[] realVictims;
        public Position realPos;
        public long delay = 800;
        
        // simulate time delay
        public void delay() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // pass
            }
        }
        
        public Simulation(EnvModel envModel) {
            realVictims = generateRandomLocsOfRealVictims();
            realPos = generateRandomStartingPosition();
            //realPos = new Position(6,6,"up");
        }

        // generate locations of real victims randomly
        public Location[] generateRandomLocsOfRealVictims() {
            List<Location> list = Arrays.asList(model.victimsToVisit.toArray(new Location[model.victimsToVisit.size()]));
            Collections.shuffle(list);
            logger.info("Red: " + list.get(0).toString());
            logger.info("Blue: " + list.get(0).toString());
            logger.info("Green: " + list.get(0).toString());
            return new Location[] { list.get(0), list.get(1), list.get(2) };
        }

        // generate a starting position randomly
        public Position generateRandomStartingPosition() {
            // generate heading
            List<String> headingList = Arrays.asList(new String[] { LEFT, RIGHT, UP, DOWN });
            Collections.shuffle(headingList);
            String heading = headingList.get(2);
            // generate location
            int x = 1, y = 1;
            while (true) {
                Random rn = new Random();
                x = rn.nextInt(W_GRID - 2) + 1;
                y = rn.nextInt(H_GRID - 2) + 1;
                if (model.isOccpuied(x, y)) {
                    continue;
                } else {
                    break;
                }
            }
            return new Position(x, y, heading);
        }

        public void getColorFromSimulation() {
            String color;
            if (containsPercept(DETERMINED_LOC))
                realPos = model.getPosition();
            if (realPos.getLoc().equals(realVictims[0])) {
                color = RED;
            } else if (realPos.getLoc().equals(realVictims[1])) {
                color = BLUE;
            } else if (realPos.getLoc().equals(realVictims[2])) {
                color = GREEN;
            } else {
                color = WHITE;
            }
            delay();
            System.out.println("Simulation: The color is " + color);
            addPercept(SCOUT, Literal.parseLiteral("color(" + color + ")"));
        }

        public void getOccupiedInfoFromSimulation() {
            if (isRelativeOccupied(simulation.realPos, RELATIVE_FRONT))
                addPercept(SCOUT, OCCUPIED_FRONT);
            if (isRelativeOccupied(simulation.realPos, RELATIVE_BACK))
                addPercept(SCOUT, OCCUPIED_BACK);
            if (isRelativeOccupied(simulation.realPos, RELATIVE_LEFT))
                addPercept(SCOUT, OCCUPIED_LEFT);
            if (isRelativeOccupied(simulation.realPos, RELATIVE_RIGHT))
                addPercept(SCOUT, OCCUPIED_RIGHT);
            delay();
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
            Node newNode = new Node(newPool, action);
            newNode.setFatherNode(this);
            return newNode;
        }
    }
}
