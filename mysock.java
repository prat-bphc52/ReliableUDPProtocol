import java.util.*;
import java.net.*;
import java.io.*;

class MyReliableUDPSocket extends DatagramSocket{
	private static int MAX_PACKET_LENGTH = 1000;
	private static int TRANSMISSION_ID_PACKET_LENGTH = Integer.BYTES + Long.BYTES; // Integer for sequence number, Long for transmission ID

	public MyReliableUDPSocket(int port, InetAddress hostIP)throws SocketException{
		super(port, hostIP);
	}

	public static MyReliableUDPSocket create(int port, InetAddress hostIP)throws SocketException, UnknownHostException{
		return new MyReliableUDPSocket(port, hostIP);
	}

	public int send(byte[] arr, InetAddress destAddr, int destPort){
		long timestamp = System.currentTimeMillis();
		byte[] packetID = longtoBytes(timestamp);
		System.out.println("Packet ID " + packetID+"  timestamp "+ timestamp);
		byte[] idArr = new byte[TRANSMISSION_ID_PACKET_LENGTH];// initial packet containing single transmissions info

		// first 32 bits to store sequence number, rest data
		for(int i=0;i<Long.BYTES;i++){
			idArr[i+Integer.BYTES]=packetID[i];
		}
		DatagramPacket idPacket = 
                  new DatagramPacket(idArr, idArr.length, destAddr, destPort);
        try{
        	send(idPacket);
        }
        catch(Exception e){
        	System.out.println("Failed to send data");
        	return -1;
        }
        return 0;
	}

	public byte[] receive(){
		byte[] buf = new byte[MAX_PACKET_LENGTH];
		DatagramPacket recv = new DatagramPacket(buf, buf.length);
		try{
        	receive(recv);
        }
        catch(Exception e){
        	System.out.println("Error while receiving data");
        	return null;
        }
		System.out.println("Received Data");
		for(int i=0;i<10;i++)
			System.out.print(buf[i]);
		return buf;
	}

	private static byte[] longtoBytes(long data) {
 		return new byte[]{
 			(byte) ((data >> 56) & 0xff),
 			(byte) ((data >> 48) & 0xff),
 			(byte) ((data >> 40) & 0xff),
 			(byte) ((data >> 32) & 0xff),
 			(byte) ((data >> 24) & 0xff),
 			(byte) ((data >> 16) & 0xff),
 			(byte) ((data >> 8) & 0xff),
 			(byte) ((data >> 0) & 0xff),
 		};
	}
}