import java.io.IOException;

public class EV3Robot {
	
	public static void main(String args[]) throws IOException{
		EV3RobotPilot rPilot = new EV3RobotPilot();
		EV3RobotCommunication rComm = new EV3RobotCommunication(rPilot);
		
		rComm.run();
	}

}
