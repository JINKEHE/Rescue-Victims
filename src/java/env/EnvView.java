package env;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;


import jason.environment.grid.GridWorldView;

/* Class draws the enviroment taking into account our current beliefs */
class EnvView extends GridWorldView {
		
	public static final long serialVersionUID = 1L;

	private static final Color FONT_COLOR = Color.BLACK;
	private static final Color POTENTIAL_VICTIM_COLOR = Color.PINK;
	private static final Color SCOUT_COLOR = Color.BLACK;
	private static final Color POSSIBILE_SCOUT_COLOR = Color.YELLOW;
	    
	private static final String UP = "up";
	private static final String DOWN = "down";
	private static final String LEFT = "left";
	private static final String RIGHT = "right";
	private Playground controller;
	private EnvModel envModel;
	    
	public EnvView(EnvModel model, Playground controller) {
            	super(model, "Children's Playground", 600);
            	envModel = model;
            	defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            	setVisible(true);
            	repaint();
            	this.controller = controller;
       	}
		
        /* Draw out our current view of the enviroment */
	public void draw(Graphics g, int x, int y, int object) {
        	switch (object) {
            	case EnvModel.OBSTACLE: 
            		drawObstacle(g, x, y); 
            		break;
            	case EnvModel.WALL:
            		drawObstacle(g, x, y);
            		break;
            	case EnvModel.RED_VICTIM:
            		drawVictim(g, x, y, "RED");
            		break;
            	case EnvModel.BLUE_VICTIM:
            		drawVictim(g, x, y, "BLUE");
            		break;
            	case EnvModel.GREEN_VICTIM:
            		drawVictim(g, x, y, "GREEN");
            		break;
            	case EnvModel.POTENTIAL_VICTIM: 
            		drawPotentialVictim(g, x, y); 
            		break;
        	}
        	if (!controller.containsPercept(Playground.DETERMINED_LOC)) drawClones(g);
        }
       
	/* Draw the agents in the enviroment */
        public void drawAgent(Graphics g, int x, int y, Color c, int id){
        	if (envModel.localizationFinished && !envModel.hasObject(EnvModel.RED_VICTIM,x,y) & !envModel.hasObject(EnvModel.BLUE_VICTIM,x,y) & !envModel.hasObject(EnvModel.GREEN_VICTIM,x,y)) {
        		drawSingleAgent(g, envModel.getPosition(), SCOUT_COLOR);
        	} else if (!envModel.localizationFinished){
        		drawClones(g);
        	} else {
        		System.out.println("haoba");
        	}
        }
        
        public void drawSingleAgent(Graphics g, Position pos, Color color) {
        	int[] xPoint = null;
			int[] yPoint = null;
			int x = pos.getX(), y = pos.getY();
			String heading = pos.getHeading();
			if (heading.equals(UP)) {
				xPoint = new int[]{x*cellSizeW, (x+1)*cellSizeW, (int)((0.5+x)*cellSizeW)};
				yPoint = new int[]{(y+1)*cellSizeH, (y+1)*cellSizeH, y*cellSizeH};
			} else if (heading.equals(DOWN)) {
				xPoint = new int[]{x*cellSizeW, (x+1)*cellSizeW, (int)((0.5+x)*cellSizeW)};
				yPoint = new int[]{y*cellSizeH, y*cellSizeH, (y+1)*cellSizeH};
			} else if (heading.equals(LEFT)) {
				xPoint = new int[]{(x+1)*cellSizeW, (x+1)*cellSizeW, x*cellSizeW};
				yPoint = new int[]{y*cellSizeH, (y+1)*cellSizeH, (int)((0.5+y)*cellSizeH)};
			} else if (heading.equals(RIGHT)) {
				xPoint = new int[]{x*cellSizeW, x*cellSizeW, (x+1)*cellSizeW};
				yPoint = new int[]{y*cellSizeH, (y+1)*cellSizeH, (int)((0.5+y)*cellSizeH)};	
			}
        	g.setColor(color);
			g.fillPolygon(new Polygon(xPoint, yPoint, 3));
        }
        
        public void drawClones(Graphics g) {
        	g.setColor(POSSIBILE_SCOUT_COLOR);
        	if (envModel.possiblePosition.size() > 1) {
			for (Position pos : envModel.possiblePosition) {
				drawSingleAgent(g, pos, POSSIBILE_SCOUT_COLOR);
			}
        	}
        }
        
	/* Draw a potential victim in the enviroment */ 
        public void drawPotentialVictim(Graphics g, int x, int y) {
    		g.setColor(POTENTIAL_VICTIM_COLOR);
    		g.fillRect(x * cellSizeW+1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
    		g.setColor(FONT_COLOR);
    		super.drawString(g, x, y, new Font("Arial", Font.BOLD, 12), "Potential Victim");
        }
        
	/* If victim location is correct, draw them and their severity */
        public void drawVictim(Graphics g, int x, int y, String color) {
        	if (color.equals("RED")) g.setColor(Color.RED);
        	if (color.equals("BLUE")) g.setColor(Color.BLUE);
        	if (color.equals("GREEN")) g.setColor(Color.GREEN);
    		g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
        }
    }
