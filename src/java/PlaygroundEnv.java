import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.logging.*;


public class PlaygroundEnv extends Environment {

    private Logger logger = Logger.getLogger("optmistor."+ PlaygroundEnv.class.getName());
    
    public static final Literal haha = Literal.parseLiteral("haha(jinwei)");
    public static final Literal goScout = Literal.parseLiteral("go(next)");
    public static final int H_GRID = 6; 
    public static final int W_GRID = 7;
    public static final int GARB  = 16;
    
    private static final int SCOUT_ID = 0;
    private static final int DOCTOR_ID = 1;
    private static final Color SCOUT_COLOR = Color.GREEN;
    private static final Color DOCTOR_COLOR = Color.YELLOW;
    private static final Color FONT_COLOR = Color.BLACK;
    
    private static final Location DOCTOR_LOC = new Location(1,1);
    
    private PlaygroundModel model;
    private PlaygroundView  view;
    
    public void init(String[] args) {
        model = new PlaygroundModel();
        view  = new PlaygroundView(model);
        model.setView(view);
    }
    
    /*      internal actions          */
    
    void haha() {
    	logger.info("jinwei is being fucked");
    }
    
    /*     internal actions           */
    
    public boolean executeAction(String agName, Structure action) {
        try {
            if (action.equals(haha)) {
                haha();
            } else if (action.equals(goScout)) {
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
    
    class PlaygroundModel extends GridWorldModel {
        private PlaygroundModel() {
            super(W_GRID, H_GRID, 2);
            setAgPos(SCOUT_ID, 1, 1);
            setAgPos(DOCTOR_ID, DOCTOR_LOC);
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
            setAgPos(1, DOCTOR_LOC);
        }
        
        /*      Internal Actions       */
    }
    
	class PlaygroundView extends GridWorldView {
		
		// I have no idea what this thing is
		private static final long serialVersionUID = 1L;

		public PlaygroundView(PlaygroundModel model) {
            super(model, "Children's Playground", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        public void draw(Graphics g, int x, int y, int object) {
        	
        }

        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        	String label = "";
        	logger.info(String.valueOf(id));
        	if (id == SCOUT_ID) {
        		super.drawAgent(g, x, y, SCOUT_COLOR, -1);
        		label = "Scout";
        	} else if (id == DOCTOR_ID) {
        		super.drawAgent(g, x, y, DOCTOR_COLOR, -1);
        		label= "Doctor";
        	}
            g.setColor(FONT_COLOR);
            super.drawString(g, x, y, defaultFont, label);
        }
    }
}




