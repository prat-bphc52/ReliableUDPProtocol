import java.util.*;
import java.net.*;
import java.io.*;

class MyReliableUDPSocket extends DatagramSocket{
	private static int MAX_PACKET_DATA_LENGTH = 500;// in bytes
	private static int MAX_PACKET_SIZE = Integer.BYTES+Long.BYTES+MAX_PACKET_DATA_LENGTH;
	// 1st 4 bytes seq no, next 8 bytes packet id, 512 bytes
	private static int TRANSMISSION_ID_PACKET_LENGTH = Integer.BYTES + Long.BYTES + Integer.BYTES + Integer.BYTES; 
	// 20 bytes
	// Integer for sequence number, Long for transmission ID, Integer Total number of packets, Integer Total data length in bytes

	public MyReliableUDPSocket(int port, InetAddress hostIP)throws SocketException{
		super(port, hostIP);
	}

	public static MyReliableUDPSocket create(int port, InetAddress hostIP)throws SocketException, UnknownHostException{
		return new MyReliableUDPSocket(port, hostIP);
	}

	public int send(byte[] arr, InetAddress destAddr, int destPort){
		byte[] idArr = new byte[TRANSMISSION_ID_PACKET_LENGTH];// initial packet containing single transmissions info

		byte[] temp;
		int count = 4;//starts from 4 because first bytes are 0 by default

		// first 4 bytes 0000 seq numbeer
		// next 8 bytes packet id
		long timestamp = System.currentTimeMillis();
		temp = longtoBytes(timestamp);//packet id

		for(int i=0;i<8;i++)
			idArr[count++]=temp[i];


		// next 4 bytes packet count
		int totPacketCount = arr.length / MAX_PACKET_DATA_LENGTH;
		if(arr.length % MAX_PACKET_DATA_LENGTH != 0)
			totPacketCount++;
		temp = inttoBytes(totPacketCount);
		
		for(int i=0;i<4;i++)
			idArr[count++]=temp[i];


		// next 4 bytes total data array length
		temp = inttoBytes(arr.length);
		for(int i=0;i<4;i++)
			idArr[count++]=temp[i];


		DatagramPacket idPacket = new DatagramPacket(idArr, idArr.length, destAddr, destPort);
        try{
        	System.out.print("\nSending Seq 0 packet");
        	System.out.print("\nPacket ID : Timestamp - "+ timestamp);
        	send(idPacket);

        }
        catch(Exception e){
        	System.out.print("\nFailed to send data");
        	return -1;
        }
        return 0;
	}

	public byte[] receive(){
		byte[] buf = new byte[MAX_PACKET_SIZE];
		DatagramPacket recv = new DatagramPacket(buf, buf.length);
		try{
        	receive(recv);
        }
        catch(Exception e){
        	System.out.println("Error while receiving data");
        	return null;
        }
		System.out.println("Received Data");
		for(int i=0;i<12;i++)
			System.out.print(buf[i]+" ");
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

	private static byte[] inttoBytes(int data) {
 		return new byte[]{
 			(byte) ((data >> 24) & 0xff),
 			(byte) ((data >> 16) & 0xff),
 			(byte) ((data >> 8) & 0xff),
 			(byte) ((data >> 0) & 0xff),
 		};
	}

	private DatagramPacket createPacket(int seqNo, byte[] id, byte[] data, int start, InetAddress destAddr, int destPort){
		int packet_size = Integer.BYTES+Long.BYTES;
		int end = start + MAX_PACKET_DATA_LENGTH;
		if(end>data.length)
			end = data.length;
		packet_size+=(end-start);

		byte[] pckt = new byte[packet_size];
		int count = 0;
		// adding sequence number
		byte[] temp = inttoBytes(seqNo);
		for(int i=0;i<4;i++)
			pckt[count++]=temp[i];

		// next 8 bytes packet id
		for(int i=0;i<8;i++)
			pckt[count++]=id[i];

		// adding data
		for(int i=start;i<end;i++)
			pckt[count++]=data[i];

		return new DatagramPacket(pckt, pckt.length, destAddr, destPort);
	}
}