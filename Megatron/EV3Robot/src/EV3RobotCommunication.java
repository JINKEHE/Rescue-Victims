import java.io.*;
import java.net.*;

public class EV3RobotCommunication implements Runnable{
	private static final String IP = "192.168.0.1";
	private static final int port = 2056;
	
	private Socket sock;
	private OutputStream oStream;
	private PrintWriter pWrite;
	private InputStream iStream;
	private BufferedReader recieveRead;
	private EV3RobotPilot rPilot;
	
	public EV3RobotCommunication(EV3RobotPilot tPilot) throws IOException{
		rPilot = tPilot;
		/* Setup connection and streams */
		sock = new Socket(IP, port);
		oStream = sock.getOutputStream();
		pWrite = new PrintWriter(oStream, true);
		iStream = sock.getInputStream();
		recieveRead = new BufferedReader(new InputStreamReader(iStream));
		System.out.println("Connected, waiting for commands.");
	}
	
	public void run() {
		String inputMsg, outputMsg;
		
		while(true){
			/* Wait for a message and execute the command recieved */
			try{
				if((inputMsg = recieveRead.readLine()) != null){
					switch(inputMsg){
					case "forward": rPilot.getPilot().travel(10);
					case "exit": System.exit(1);;
					}
				}
			} catch(Exception e){}
		}
	}
}
