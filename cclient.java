import java.net.*;
import java.io.*;

class CClient{
	public static void main(String[] args) throws IOException{
		if(args.length<2){
			System.out.println("Please specify the HostIP and Port number...");
			return;
		}
		InetAddress hostIP = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		MyReliableUDPSocket sock = MyReliableUDPSocket.create(port, hostIP);
		
		InetAddress senderIP = InetAddress.getByName("127.0.0.1");

		byte[] temp = new byte[10000];
		for(int i=0;i<10000;i++){
			temp[i]=(byte)i;
		}
		sock.send(temp, senderIP, 6000);
	}
}