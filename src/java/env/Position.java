package env;

import jason.environment.grid.Location;

public class Position {
	
    private static final String DOWN = "down";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final String UP = "up";
	
	private int x;
	private int y;
	private String heading;
	
	public Position(int x, int y, String heading) {
		super();
		this.x = x;
		this.y = y;
		this.heading = heading;
	}

	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}

	@Override
	public String toString() {
		return "Position [x=" + x + ", y=" + y + ", heading=" + heading + "]";
	}
	
	// rFront[0], rBack[1], rLeft[2], rRight[3]
	public void relativeMove(int rDirection) {
		String absHeading = Position.getAbsoluteHeading(heading, rDirection);
		switch(absHeading) {
		case DOWN:
			y += 1;
			break;
		case UP:
			y -= 1;
			break;
		case LEFT:
			x -= 1;
			break;
		case RIGHT:
			x += 1;
			break;
		}
	}
	
	 // rFront[0], rBack[1], rLeft[2], rRight[3]
    public static String getAbsoluteHeading(String heading, int rHeading) {
    	switch(heading) {
    	case UP:
    		if (rHeading==0) return UP;
    		if (rHeading==1) return DOWN;
    		if (rHeading==2) return LEFT;
    		if (rHeading==3) return RIGHT;
    		break;
    	case DOWN:
    		if (rHeading==0) return DOWN;
    		if (rHeading==1) return UP;
    		if (rHeading==2) return RIGHT;
    		if (rHeading==3) return LEFT;
    		break;
    	case LEFT:
    		if (rHeading==0) return LEFT;
    		if (rHeading==1) return RIGHT;
    		if (rHeading==2) return DOWN;
    		if (rHeading==3) return UP;
    		break;
    	case RIGHT:
    		if (rHeading==0) return RIGHT;
    		if (rHeading==1) return LEFT;
    		if (rHeading==2) return UP;
    		if (rHeading==3) return DOWN;
    		break;
    	}
    	return "GG";
    } 
    
    // rFront[0], rBack[1], rLeft[2], rRight[3]
    public static int getRelativeHeading(String heading, String targetHeading) {
    	switch(heading) {
    	case UP:
    		if (targetHeading.equals(UP)) return 0;
    		if (targetHeading.equals(DOWN)) return 1;
    		if (targetHeading.equals(LEFT)) return 2;
    		if (targetHeading.equals(RIGHT)) return 3;
    		break;
    	case DOWN:
    		if (targetHeading.equals(UP)) return 1;
    		if (targetHeading.equals(DOWN)) return 0;
    		if (targetHeading.equals(LEFT)) return 3;
    		if (targetHeading.equals(RIGHT)) return 2;
    		break;
    	case LEFT:
    		if (targetHeading.equals(UP)) return 3;
    		if (targetHeading.equals(DOWN)) return 2;
    		if (targetHeading.equals(LEFT)) return 0;
    		if (targetHeading.equals(RIGHT)) return 1;
    		break;
    	case RIGHT:
    		if (targetHeading.equals(UP)) return 2;
    		if (targetHeading.equals(DOWN)) return 3;
    		if (targetHeading.equals(LEFT)) return 1;
    		if (targetHeading.equals(RIGHT)) return 0;
    		break;
    	}
    	return 99;
    } 
    
    
    public Location getLoc() {
    	return new Location(x,y);
    }
}
