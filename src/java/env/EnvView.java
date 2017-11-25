package env;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;

import jason.environment.grid.GridWorldView;

// the view of the environment
	class EnvView extends GridWorldView {
		
		private static final long serialVersionUID = 1L;
		private static final int OBSTACLE = 16;
	    private static final int WALL = 128;
	    private static final int POTENTIAL_VICTIM = 32;
	    private static final int VICTIM = 64;

	    private static final Color SCOUT_COLOR = Color.GREEN;
	    private static final Color FONT_COLOR = Color.BLACK;
	    private static final Color POTENTIAL_VICTIM_COLOR = Color.PINK;
	    private static final Color VICTIM_COLOR = Color.RED;
	    private static final Color POSSIBILE_SCOUNT_COLOR = Color.YELLOW;
	    
	    //private EnvController controller;
	    private EnvModel envModel;
	    
		public EnvView(EnvModel model, EnvController controller) {
            super(model, "Children's Playground", 600);
            envModel = model;
            //this.controller = controller;
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }
		
        public void draw(Graphics g, int x, int y, int object) {
			switch (object) {
            	case OBSTACLE: 
            		drawObstacle(g, x, y); 
            		break;
            	case WALL:
            		drawObstacle(g, x, y);
            		break;
            	case POTENTIAL_VICTIM: 
            		drawPotentialVictim(g, x, y); 
            		break;
            	case VICTIM:
            		drawVictim(g, x, y);
            		break;
        	}
        }
        /*
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        	super.drawAgent(g, x, y, SCOUT_COLOR, -1);
        	g.setColor(FONT_COLOR);
        	String directions = "";
        	if (envModel.getPos().getTerm(2).toString().equals("right")) directions += "R"; 
        	if (envModel.getPos().getTerm(2).toString().equals("left")) directions += "L"; 
        	if (envModel.getPos().getTerm(2).toString().equals("up")) directions += "U"; 
        	if (envModel.getPos().getTerm(2).toString().equals("down")) directions += "D"; 
            super.drawString(g, x, y, defaultFont, directions);
        }
*/
        public void drawAgent(Graphics g, int x, int y, Color c, int id){
			g.setColor(POSSIBILE_SCOUNT_COLOR);
			// System.out.println("1");
			for (Position pos : envModel.possiblePosition) {
				int possible_x = pos.getX();
				int possible_y = pos.getY();
				System.out.println(pos.getHeading());
				if (pos.getHeading() == "up") {
					int xPoint[] = { possible_x * cellSizeW, (possible_x + 1) * cellSizeW,
							(int) ((0.5 + possible_x) * cellSizeW) };
					int yPoint[] = { (possible_y + 1) * cellSizeH, (possible_y + 1) * cellSizeH, possible_y * cellSizeH };
					Polygon triangle = new Polygon(xPoint, yPoint, 3);
					g.fillPolygon(triangle);
				} else if (pos.getHeading() == "down") {
					int xPoint[] = { possible_x * cellSizeW, (possible_x + 1) * cellSizeW,
							(int) ((0.5 + possible_x) * cellSizeW) };
					int yPoint[] = { possible_y * cellSizeH, possible_y * cellSizeH, (possible_y + 1) * cellSizeH };
					Polygon triangle = new Polygon(xPoint, yPoint, 3);
					g.fillPolygon(triangle);
				} else if (pos.getHeading() == "left") {
					int xPoint[] = { (possible_x + 1) * cellSizeW, (possible_x + 1) * cellSizeW, possible_x * cellSizeW };
					int yPoint[] = { possible_y * cellSizeH, (possible_y + 1) * cellSizeH,
							(int) ((0.5 + possible_y) * cellSizeH) };
					Polygon triangle = new Polygon(xPoint, yPoint, 3);
					g.fillPolygon(triangle);
				} else if (pos.getHeading() == "right") {
					int xPoint[] = { possible_x * cellSizeW, possible_x * cellSizeW, (possible_x + 1) * cellSizeW };
					int yPoint[] = { possible_y * cellSizeH, (possible_y + 1) * cellSizeH,
							(int) ((0.5 + possible_y) * cellSizeH) };
					Polygon triangle = new Polygon(xPoint, yPoint, 3);
					g.fillPolygon(triangle);
				} else
					return;
	
			}
			int xPoint[] = { x * cellSizeW, (x + 1) * cellSizeW, (int) ((0.5 + x) * cellSizeW) };
			int yPoint[] = { y * cellSizeH, y * cellSizeH, (y + 1) * cellSizeH };
			Polygon triangle = new Polygon(xPoint, yPoint, 3);
			g.fillPolygon(triangle);
			g.setColor(FONT_COLOR);
        }
        
        public void drawPotentialVictim(Graphics g, int x, int y) {
    		g.setColor(POTENTIAL_VICTIM_COLOR);
    		g.fillRect(x * cellSizeW+1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
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