package env;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class test {
	
		private static final String IP = "localhost";
		private static final int port = 8888;
		
		private static Socket sock;
		private static OutputStream oStream;
		private static PrintWriter pWrite;
		private static InputStream iStream;
		private static BufferedReader recieveRead;
		
		public static void main(String[] args){
			/* Setup connection and streams */
			try {
				sock = new Socket(IP, port);
				oStream = sock.getOutputStream();
				pWrite = new PrintWriter(oStream, true);
				iStream = sock.getInputStream();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			recieveRead = new BufferedReader(new InputStreamReader(iStream));
			System.out.println("Connected, waiting for commands.");

			String inputMsg, outputMsg;
			
			while(true){
				try {
					System.out.println(recieveRead.readLine());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			
		}	
}

