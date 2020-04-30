import java.net.*;
import java.io.*;

class CClient{
	public static void main(String[] args) throws IOException{
		if(args.length<5){
			System.out.println("Please specify the HostIP, Port number, Target IP, Target Port number and Input file name...");
			return;
		}
		InetAddress hostIP = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		MyReliableUDPSocket sock = MyReliableUDPSocket.create(port, hostIP);
		
		InetAddress senderIP = InetAddress.getByName(args[2]);


		File f =new File(args[4]);
		FileInputStream fis = new FileInputStream(f);
		byte[] bytesArray = new byte[(int) f.length()];

		System.out.println();
		fis.read(bytesArray); //read file into bytes[]
		fis.close();
		sock.send(bytesArray, senderIP, Integer.parseInt(args[3]));
	}
}