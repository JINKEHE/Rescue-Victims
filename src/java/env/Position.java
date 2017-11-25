package env;

public class Position {
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
	
	public void moveOneStep(String direction) {
		switch(direction) {
		case "down":
			y += 1;
			break;
		case "up":
			y -= 1;
			break;
		case "left":
			x -= 1;
			break;
		case "right":
			x += 1;
			break;
		}
	}
}
