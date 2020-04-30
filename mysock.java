import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

class MyReliableUDPSocket extends DatagramSocket{


	private static int MAX_PACKET_DATA_LENGTH = 500;// in bytes
	final static int MAX_PACKET_SIZE = Integer.BYTES+Long.BYTES+MAX_PACKET_DATA_LENGTH;
	// 1st 4 bytes seq no, next 8 bytes packet id, 512 bytes
	private static int TRANSMISSION_ID_PACKET_LENGTH = Integer.BYTES + Long.BYTES + Integer.BYTES + Integer.BYTES; 
	// 20 bytes
	// Integer for sequence number, Long for transmission ID, Integer Total number of packets, Integer Total data length of byte array

	private static int ACK_PCKT_DATA_LENGTH = Integer.BYTES + Long.BYTES;
	// Seq no. in negative (to identify that these are acknowledgements)

	public MyReliableUDPSocket(int port, InetAddress hostIP)throws SocketException{
		super(port, hostIP);
	}

	public static MyReliableUDPSocket create(int port, InetAddress hostIP)throws SocketException, UnknownHostException{
		return new MyReliableUDPSocket(port, hostIP);
	}

	public int send(byte[] arr, InetAddress destAddr, int destPort){
		byte[] idArr = new byte[TRANSMISSION_ID_PACKET_LENGTH];// initial packet containing single transmissions info

		byte[] temp;
		int count = 0;

		// seq number for transmission ID packet = 1
		temp = inttoBytes(1);
		for(int i=0;i<4;i++)
			idArr[count++]=temp[i];

		// next 8 bytes packet id

		final TransmissionHelperSender ths = new TransmissionHelperSender();
		ths.packetID = System.currentTimeMillis();
		
		ths.packetCount = arr.length / MAX_PACKET_DATA_LENGTH;
		if(arr.length % MAX_PACKET_DATA_LENGTH != 0)
			ths.packetCount++;

		ths.dataLength = arr.length;

		temp = longtoBytes(ths.packetID);//packet id
		for(int i=0;i<8;i++)
			idArr[count++]=temp[i];

		
		// next 4 bytes packet count
		temp = inttoBytes(ths.packetCount);
		for(int i=0;i<4;i++)
			idArr[count++]=temp[i];


		// next 4 bytes total data array length
		temp = inttoBytes(arr.length);
		for(int i=0;i<4;i++)
			idArr[count++]=temp[i];


		DatagramPacket idPacket = new DatagramPacket(idArr, idArr.length, destAddr, destPort);
		ths.map.put(1, idPacket);

		ths.startReceiverThread(this);

        try{
        	System.out.println("Sending Seq 0 packet");
        	System.out.println("Packet ID : Timestamp - "+ ths.packetID);
        	send(idPacket);

        	byte[] pc_id = longtoBytes(ths.packetID);
        	for(int i=0;i<ths.packetCount;i++){
        		DatagramPacket dp = createDataPacket(i+2, pc_id, arr, i*MAX_PACKET_DATA_LENGTH,destAddr, destPort);
        		ths.map.put(i+2, dp);
        		System.out.println("Sending Seq "+(i+2)+" packet");
        		send(dp);
        	}
        }
        catch(Exception e){
        	System.out.println("Failed to send data");
        	return -1;
        }
        return 0;
	}

	public byte[] receive(){
		TransmissionHelperRecevier thr = new TransmissionHelperRecevier();
		byte[] buf;
		while(true){
			try{
				buf = new byte[MAX_PACKET_SIZE];
				DatagramPacket recv = new DatagramPacket(buf, buf.length);
        		receive(recv);
				System.out.println(buf.toString());
        		int seqNo = getIntFromByteArray(buf, 0);
        		long packetID = getLongFromByteArray(buf ,4);
        		System.out.println("\n**Received a packet** \nAddress:"+recv.getAddress()+" Port"+recv.getPort()+" Seq no "+seqNo +" Packet ID"+packetID);
        		send(createAckPacket(seqNo,packetID,recv.getAddress(),recv.getPort()));
        	}
        	catch(Exception e){
        		System.out.println("Error while receiving data");
        		return null;
        	}
    	}
		// return buf;
	}

	public static byte[] longtoBytes(long data) {
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

	public static byte[] inttoBytes(int data) {
 		return new byte[]{
 			(byte) ((data >> 24) & 0xff),
 			(byte) ((data >> 16) & 0xff),
 			(byte) ((data >> 8) & 0xff),
 			(byte) ((data >> 0) & 0xff),
 		};
	}

	public static int getIntFromByteArray(byte[] data, int start){
		ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
 		byteBuffer.put(data, start, Integer.BYTES);
 		byteBuffer.flip();
 		return byteBuffer.getInt();
	}

	public static long getLongFromByteArray(byte[] data, int start){
 		ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
 		byteBuffer.put(data, start, Long.BYTES);
 		byteBuffer.flip();
 		return byteBuffer.getLong();
	}

	private DatagramPacket createDataPacket(int seqNo, byte[] id, byte[] data, int start, InetAddress destAddr, int destPort){
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

	private DatagramPacket createAckPacket(int seqNo, long  pcktid, InetAddress destAddr, int destPort){
		byte[] pckt = new byte[ACK_PCKT_DATA_LENGTH];
		int count = 0;

		// adding sequence number
		byte[] temp = inttoBytes(seqNo);
		for(int i=0;i<4;i++)
			pckt[count++]=temp[i];

		// next 8 bytes packet id
		temp = longtoBytes(pcktid);
		for(int i=0;i<8;i++)
			pckt[count++]=temp[i];

		return new DatagramPacket(pckt, pckt.length, destAddr, destPort);
	}
}

class TransmissionHelperSender{
	long packetID;
	int packetCount;
	int dataLength;
	int receivedCount;
	HashMap<Integer, DatagramPacket> map;
	HashMap<Integer, Boolean> ackStatus;

	private Thread receiver;
	boolean threadExit;

	public TransmissionHelperSender(){
		map = new HashMap<>();
		ackStatus = new HashMap<>();
	}

	void startReceiverThread(DatagramSocket sock){	
        receiver = new Thread(){
        	@Override
        	public void run(){
        		while(!threadExit){
        			try{
	        			byte[] buf = new byte[MyReliableUDPSocket.MAX_PACKET_SIZE];
	        			DatagramPacket recv = new DatagramPacket(buf, buf.length);
	        			sock.receive(recv);
	        			int seqNo = MyReliableUDPSocket.getIntFromByteArray(buf, 0);
	        			long pID = MyReliableUDPSocket.getLongFromByteArray(buf ,4);
	        			if(seqNo<0){
	        				System.out.println("Received acknowledgement "+ pID + " Seq No "+ seqNo);
	        				if(packetID == pID){
	        					ackStatus.put(seqNo*-1, true);
	        				}
	        			}
	        		}
	        		catch(Exception e){
	
        			}
        		}
        	}
        };
        threadExit = false;
        receiver.start();
	}
	void stopReceiverThread(){
		threadExit = true;
	}
}

class TransmissionHelperRecevier{
	long packetID;
	int packetCount;//doesn't include the seq 0 packet
	int dataLength;

	byte[] data;

	HashMap<Integer, DatagramPacket> map;
	HashMap<Integer, Boolean> writtenStatus; // true if yet to be written otherwise not present
	public TransmissionHelperRecevier(){
		map = new HashMap<>();
	}
}