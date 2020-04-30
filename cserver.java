import java.net.*;
import java.io.*;

class CServer{
	public static void main(String[] args)throws IOException {
		if(args.length<3){
			System.out.println("Please specify the HostIP, Port number and Output file name...");
			return;
		}
		InetAddress hostIP = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);

		MyReliableUDPSocket sock = MyReliableUDPSocket.create(port, hostIP);

		byte[] data = sock.receive();
		File f = new File(args[2]);
		OutputStream os = new FileOutputStream(f);
		os.write(data);
		os.close();
	}
}