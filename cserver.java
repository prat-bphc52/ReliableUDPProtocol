import java.net.*;
import java.io.*;

class CServer{
	public static void main(String[] args)throws IOException {
		if(args.length<2){
			System.out.println("Please specify the HostIP and Port number...");
			return;
		}
		InetAddress hostIP = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);

		MyReliableUDPSocket sock = MyReliableUDPSocket.create(port, hostIP);

		sock.receive();
		// File f = new File("sample.txt");
		// FileReader fr = new FileReader(f);
	}
}