package env;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import jason.asSyntax.Literal;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;



// a grid map
public class EnvModel extends GridWorldModel {
	public boolean localizationFinished = false;
	
	private static final int SCOUT_ID = 0;
	
	public static final int OBSTACLE = 16;
    public static final int WALL = 128;
    public static final int POTENTIAL_VICTIM = 32;
    public static final int RED_VICTIM = 256;
    public static final int BLUE_VICTIM = 512;
    public static final int GREEN_VICTIM = 1024;
	
	// the set of possible Victims;
	public Set<Location> victimsToVisit;
	// costs from one victim to another victim
	private HashMap<Location, HashMap<Location, Integer>> costToEachOther;
	private HashMap<Location, HashMap<Location, LinkedList<Location>>> pathToEachOther;
	// the heading of the robot
	public String heading;
    public HashSet<Position> possiblePosition = new HashSet<Position>();
	
    
 // constructor with width, height, set of obstacles, set of possible victims, whether there are wall
 	public EnvModel(int W_GRID, int H_GRID, Set<Location> setOfObstacles, Set<Location> setOfPossibleVictims) {
 		super(W_GRID, H_GRID, 1);
 		victimsToVisit = setOfPossibleVictims;
 		costToEachOther = new HashMap<Location, HashMap<Location, Integer>>();
 		pathToEachOther = new HashMap<Location, HashMap<Location, LinkedList<Location>>>();
 		computeCosts();
 		this.setAgPos(SCOUT_ID, 0,0);
 	}
	
	// this method should only be called when the location is determined
	public Location getLoc(){
		return getAgPos(SCOUT_ID);
	}
	
	public void setPosition(Position pos) {
		setAgPos(SCOUT_ID, pos.getLoc());
	}
	
	public void addWalls() {
		for (int w = 0; w <= width - 1; w++) {
			add(WALL, new Location(w, 0));
			add(WALL, new Location(w, height - 1));
		}
		for (int h = 0; h <= height - 1; h++) {
			add(WALL, new Location(0, h));
			add(WALL, new Location(width - 1, h));
		}
	}
	
	public Literal getPosLiteral() {
		Location currentLoc = getAgPos(0);
		Literal currentPos = Literal.parseLiteral("pos("+currentLoc.x+","+currentLoc.y+","+heading+")");
		return currentPos;
	}
	
	public Position getPosition() {
		Location currentLoc = getAgPos(0);
		return new Position(currentLoc.x, currentLoc.y, this.heading);
	}

	public boolean isOccpuied(int x, int y) {
		return hasObject(WALL, x, y) || hasObject(OBSTACLE, x, y);
	}
	
	public boolean isThatGridOccupied(int x, int y, String absoluteHeading) {
		if (absoluteHeading.equals("up")) return isOccpuied(x, y-1);
		if (absoluteHeading.equals("down")) return isOccpuied(x, y+1);
		if (absoluteHeading.equals("left")) return isOccpuied(x-1, y);
		if (absoluteHeading.equals("right")) return isOccpuied(x+1, y);
		return true;
	}
	
	// compute the cost it will take to get from one victim to another
	public void computeCosts() {
		for (Location vicOne : victimsToVisit) {
			pathToEachOther.put(vicOne, new HashMap<Location, LinkedList<Location>>());
			costToEachOther.put(vicOne, new HashMap<Location, Integer>());
			for (Location vicTwo : victimsToVisit) {
				// can be easily further optimized, but I am lazy, hopefully
				// someone will do this
				if (vicOne != vicTwo) {
					LinkedList<Location> path = aStarPathFinding(vicOne, vicTwo);
					pathToEachOther.get(vicOne).put(vicTwo, path);
					costToEachOther.get(vicOne).put(vicTwo, path.size());
				}
			}
		}
	}
	
	// A* path finding algorithm with Location as the basic element
	public LinkedList<Location> aStarPathFinding(Location start, Location goal) {
		// initialize the data structures to be used in the search
		LinkedList<Location> openList = new LinkedList<Location>();
		Set<Location> closedList = new HashSet<Location>();
		HashMap<Location, Integer> values = new HashMap<Location, Integer>();
		HashMap<Location, Location> parents = new HashMap<Location, Location>();
		openList.add(start);
		values.put(start, 0);
		parents.put(start, null);
		boolean found = false;
		// start the search
		while (!(found || openList.isEmpty())) {
			// find the optimal grid with smallest f(n) = g(n) + h(n)
			Location besGrid = openList.get(0);
			for (Location grid : openList) {
				if (values.get(grid) + grid.distanceManhattan(goal) < values.get(besGrid)
						+ besGrid.distanceManhattan(goal)) {
					besGrid = grid;
				}
			}
			// explore this grid
			openList.remove(besGrid);
			closedList.add(besGrid);
			Location nextGrid;
			for (int[] g : new int[][] { { -1, 0 }, { 0, -1 }, { 0, 1 }, { 1, 0 } }) {
				int x = besGrid.x + g[0];
				int y = besGrid.y + g[1];
				nextGrid = new Location(x, y);
				if (y <= height - 1 && x <= width - 1 && y >= 0 && x >= 0 && !closedList.contains(nextGrid)
						&& !values.containsKey(nextGrid) && !hasObject(OBSTACLE,nextGrid) && !hasObject(WALL,nextGrid)) {
					parents.put(nextGrid, besGrid);
					if (goal.equals(nextGrid)) {
						found = true;
						break;
					} else {
						openList.addFirst(nextGrid);
						values.put(nextGrid, 1 + values.get(besGrid));
					}
				}
			}
			values.remove(besGrid);
		}
		// generate the path according to the results
		Location grid = goal;
		LinkedList<Location> path = new LinkedList<Location>();
		while (!grid.equals(start)) {
			path.addFirst(grid);
			grid = parents.get(grid);
		}
		return path;
	}

	// draw the map (for debugging purpose)
	public void visualize() {
		String str = "";
		for (int h = 0; h <= height - 1; h++) {
			for (int w = 0; w <= width - 1; w++) {
				if (hasObject(OBSTACLE,new Location(w, h))) {
					str += "1";
				} else if (victimsToVisit.contains(new Location(w, h))) {
					str += "?";
				} else {
					str += "0";
				}
				str += " ";
			}
			str += "\n";
		}
		System.out.println(str);
	}

	// print the path generated by A* search algorithm (for debugging purpose)
	// a static method
	public void printPath(LinkedList<Location> path) {
		for (int i = 0; i <= path.size() - 1; i++) {
			System.out.print("(" + path.get(i).toString() + ")");
			if (i != path.size() - 1)
				System.out.print("->");
		}
		System.out.println();
	}

	// that's hard to explain.
	// hopefully, someone will add more comments here (or not?)
	public Location[] findOrderOfVictimsToVisit(Location currentLoc) { 
	// if there's no unvisited victims, return immediately 
		ArrayList<Location[]> permutations = getPermutations(0, victimsToVisit.toArray(new Location[victimsToVisit.size()]));
		HashMap<Location, Integer> costToFirstVic = new HashMap<Location, Integer>();
		for (Location victim : victimsToVisit) {
			LinkedList<Location> path = aStarPathFinding(currentLoc, victim);
			costToFirstVic.put(victim, path.size());
		}
		// find the optimal order of victims to visit 
		Location[] bestOrder = permutations.get(0);
		int miniTotalCost = computeTotalCost(bestOrder) + costToFirstVic.get(bestOrder[0]);
		for (Location[] order : permutations) {
			if (computeTotalCost(order)+costToFirstVic.get(order[0])<miniTotalCost){
				miniTotalCost = computeTotalCost(order)+costToFirstVic.get(order[0]);
				bestOrder = order;
			}
		}
		// return the order of victims to visit
		return bestOrder;
		// don't worry about the plans, they are already generated and stored
	}

	// compute the cose for each possible order of victims to visit 
	public int computeTotalCost(Location[] orderOfVictims) {
		int totalCost = 0;
		for (int i=1; i<=orderOfVictims.length-1; i++) {
			totalCost += costToEachOther.get(orderOfVictims[i-1]).get(orderOfVictims[i]);
		}
		return totalCost;
	}
	
	// a recursive method to get all the permutations of a set of numbers
	// I love this generic method
	public static <T> ArrayList<T[]> getPermutations(int start, T[] input) {
		ArrayList<T[]> output = new ArrayList<T[]>();
		if (start == input.length) {
			output.add(input.clone());
		}
		for (int i = start; i < input.length; i++) {
			// swap the current number with later numbers
			T temp = input[i];
			input[i] = input[start];
			input[start] = temp;
			// then recursively call the permute method
			output.addAll(getPermutations(start+1, input));
			// swap back
			temp = input[i];
			input[i] = input[start];
			input[start] = temp;
		}
		return output;
	}

	// why do I have this method?
	public static String integerArrayToStr(Integer[] arr) {
		String str = "[";
		for (int i=0; i<=arr.length-1; i++) {
			str += arr[i];
			if (i != arr.length-1) str += ", ";
		}
		str += "]";
		return str;
	}
	
	// why do I need this method?
	public static String locArrayToStr(Location[] arr) {
		String str = "[";
		for (int i=0; i<=arr.length-1; i++) {
			str += "("+arr[i].toString()+")";
			if (i != arr.length-1) str += ", ";
		}
		str += "]";
		return str;
	}
	
	public void test() {
		try {
			EnvModel.class.getMethod("theMethod").invoke(this);
		} catch (Exception e) {
		} 
	}
	
	public void theMethod() {
		System.out.println("Success");
	}
	
	public static void main(String[] args) {
		Location[] obstacles = new Location[]{new Location(2,1),new Location(2,3)}; 
		Location[] possibleVictims = new Location[]{new Location(2,2),new Location(5,6)}; 
		Set<Location> obstaclesSet = new HashSet<Location>(Arrays.asList(obstacles)); 
		Set<Location> possibleVictimsSet = new HashSet<Location>(Arrays.asList(possibleVictims)); 
		EnvModel envModel = new EnvModel(9, 8, obstaclesSet, possibleVictimsSet);
		envModel.visualize(); 
		System.out.println(locArrayToStr(envModel.findOrderOfVictimsToVisit(new Location(4,6))));
		Position a = new Position(1,1,"up");
		Position b = new Position(1,1,"up");
		HashSet<Position> s = new HashSet<Position>();
		s.add(a);
		System.out.println(a.equals(b));
		System.out.println(s.contains(b));
		envModel.test();
	}
}