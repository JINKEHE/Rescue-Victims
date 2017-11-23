
import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.logging.*;

// the centralised environment
public class Env extends Environment {

    private Logger logger = Logger.getLogger("optmistor."+ Env.class.getName());
    public static final Literal haha = Literal.parseLiteral("haha(jinwei)");
    public static final Literal goScout = Literal.parseLiteral("go(next)");
    public static final int H_GRID = 6; 
    public static final int W_GRID = 7;
    public static final int GARB  = 16;
    
    private static final int SCOUT_ID = 0;
    private static final int DOCTOR_ID = 1;
    private static final Color SCOUT_COLOR = Color.GREEN;
    private static final Color FONT_COLOR = Color.BLACK;
    private static final Color POTENTIAL_VICTIM_COLOR = Color.PINK;
    private static final Color VICTIM_COLOR = Color.RED;
    
    private static final int OBSTACLE = 16;
    private static final int POTENTIAL_VICTIM = 32;
    private static final int VICTIM = 64;
    
    private Model model;
    private View  view;
    
    public void init(String[] args) {
        model = new Model();
        view  = new View(model);
        model.setView(view);
    }
    
    public boolean executeAction(String agName, Structure action) {
        try {
        	if (action.equals(goScout)) {
            	model.scoutGo();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        return true;
    }

    public void stop() {
        super.stop();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    class Model extends GridWorldModel {
        private Model() {
            super(W_GRID, H_GRID, 1);
            setAgPos(SCOUT_ID, 0, 0);
            
            
            
            
            
            addInitialBeliefs();
        }
        
        /* add initial beliefs */
        void addInitialBeliefs() {
        	add(OBSTACLE, 1, 1);
        	add(OBSTACLE, 2, 2);
        	add(OBSTACLE, 3, 3);
        	add(OBSTACLE, 4, 4);
        	add(VICTIM, 3, 5);
        	add(POTENTIAL_VICTIM, 5, 5);
        }
        
        
        
        
        
        /*      Internal Actions       */
        
        void scoutGo() {
            Location scoutLoc = getAgPos(0);
            if (scoutLoc.x != getWidth()-1) {
            	scoutLoc.x += 1;
            } else if (scoutLoc.y != getHeight()-1) {
            	scoutLoc.y += 1;
            	scoutLoc.x = 0;
            }
            setAgPos(0, scoutLoc);
        }
        
        /*      Internal Actions       */
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    // the view of the environment
	class View extends GridWorldView {
		
		private static final long serialVersionUID = 1L;

		public View(Model model) {
            super(model, "Children's Playground", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }
		
		@Override
        public void draw(Graphics g, int x, int y, int object) {
			switch (object) {
            	case Env.OBSTACLE: 
            		drawObstacle(g, x, y); 
            		break;
            	case Env.POTENTIAL_VICTIM: 
            		drawPotentialVictim(g, x, y); 
            		break;
            	case Env.VICTIM:
            		drawVictim(g, x, y);
            		break;
        	}
        }

        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        	super.drawAgent(g, x, y, SCOUT_COLOR, -1);
            g.setColor(FONT_COLOR);
            super.drawString(g, x, y, defaultFont, "Scout");
        }
        
        public void drawPotentialVictim(Graphics g, int x, int y) {
    		g.setColor(POTENTIAL_VICTIM_COLOR);
    		g.fillRect(x * cellSizeW + 1, y * cellSizeH+1, cellSizeW-1, cellSizeH-1);
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
}




