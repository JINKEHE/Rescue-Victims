import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class doctor {
	  public static void main(String[] args) throws Exception {
		  
		  // setup server
	      ServerSocket sersock = new ServerSocket(3000);
	      System.out.println("Server ready to send command");
	      Socket sock = sersock.accept();                          
	      BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
		                      
	      OutputStream ostream = sock.getOutputStream(); 
	      PrintWriter pwrite = new PrintWriter(ostream, true);
	 
	                            
	      InputStream istream = sock.getInputStream();
	      BufferedReader receiveRead = new BufferedReader(new InputStreamReader(istream));
	 
	      String receiveMessage, sendMessage;               
	      while(true){
	        if((receiveMessage = receiveRead.readLine()) != null){
	           System.out.println(receiveMessage);         
	        } 
	        // send a command to the scout
	        sendMessage = keyRead.readLine(); 
	        pwrite.println(sendMessage);             
	        pwrite.flush();
	      }               
	    }
}