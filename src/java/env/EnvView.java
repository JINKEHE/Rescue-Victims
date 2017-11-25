package env;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import jason.asSyntax.Literal;
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
	    
	    private EnvController controller;
	    private EnvModel envModel;
	    
		public EnvView(EnvModel model, EnvController controller) {
            super(model, "Children's Playground", 600);
            envModel = model;
            this.controller = controller;
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