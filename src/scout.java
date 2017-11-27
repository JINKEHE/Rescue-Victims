import java.io.*;
import java.net.Socket;

public class connectionTest {
	
	private static final String IP = "192.168.0.1";
	private static final int port = 2056;
	
	public static void main(String[] args) throws IOException{	
		Socket sock = new Socket(IP, port);
       // sending to client (pwrite object)
		OutputStream ostream = sock.getOutputStream(); 
		PrintWriter pwrite = new PrintWriter(ostream, true);

       // receiving from server ( receiveRead  object)
		InputStream istream = sock.getInputStream();
		BufferedReader receiveRead = new BufferedReader(new InputStreamReader(istream));

		System.out.println("Connected, waiting for message.");

		String receiveMessage, sendMessage;               
		while(true){
			if((receiveMessage = receiveRead.readLine()) != null){
				switch(receiveMessage){
					/* Do what msg asks */
				case "move":
				case "rotate": //etc
				}
			} 
		}
	}
}
