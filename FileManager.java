

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class FileManager {

	private File file;
	private int packetLength;
	private int index;
	private int packetCount;
	byte[] fileContent = null;
	private int headerLength;

	public FileManager(int packetLength) {
	    headerLength = 2;
		index = 0;
		packetCount = 0;
		this.packetLength = packetLength;
	}


	public int getIndex() {
		return index;
	}

	public int getPacketLength() {
		return packetLength;
	}

	public int getPacketCount() {
		return packetCount;
	}

	public void setPacketCount(int packetCount) {
		this.packetCount = packetCount;
	}

    public byte[] importFile(String path) {
    	file = new File(path);
    	try {
			fileContent = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return fileContent;
    }

    public void exportFile(String path) {
    	try (FileOutputStream outputStream = new FileOutputStream(path)) {
    		   outputStream.write(fileContent);

    		} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

    public boolean isCorrupted(byte[] packet){
        byte[] header = extractPacketHeader(packet);
        if(header[0] == (byte) -128) //if the first bit is equal to 1111 1111, then that packet is corrupted.
            return true;
        return false;
    }

    public byte[] extractPacketHeader(byte[] packet){

        byte[] header = Arrays.copyOfRange(packet, 0, headerLength-1); //Copies header for header size of 16
        return header;
    }

    public byte[] extractPacket(byte[] packet){

        byte[] header = Arrays.copyOfRange(packet, headerLength, packet.length);//Copies packet after header
        return header;
    }


    public void addPacket(byte[] packet) {
        if(isCorrupted(packet) || packet[1] != (byte) packetCount){
            return; //does not add packet if the packet is corrupted or if it's not the next packet.
        }
    	int pStart = index;
    	int pEnd = index+packet.length-1;

    	System.out.println("R["+(packetCount+1)+"]-["+pStart+"]-["+pEnd+"]");
    	//System.out.println(index);

    	if(fileContent == null) {
    		fileContent = new byte[1024];
    	}else {
    	byte[] temp = new byte[index+packet.length];
    	System.arraycopy(fileContent, 0, temp, 0, index);
    	fileContent = temp;
    	//System.out.println(fileContent.length);
    	}

    	byte[] temp = new byte[fileContent.length + packet.length];

    	System.arraycopy(fileContent, 0, temp, 0, fileContent.length);

    	System.arraycopy(packet, 0, temp, 0, packet.length);

    	fileContent = temp;
    	//for(int i = 0; i < packet.length; i++) {
    	//	fileContent[i+index] = packet[i];
    	//}
    	index = index + packet.length;
    	packetCount++;
    }

    public byte[] nextPacket() {
    	int pStart = index;

    	if((index+packetLength) > fileContent.length){
    		packetLength = fileContent.length - index;
    	}else if((index == fileContent.length)) {
    		byte[] end = null;
    		return end;
    	}

    	int pEnd = index+packetLength-1;

    	System.out.println("S["+(packetCount+1)+"]-["+pStart+"]-["+pEnd+"]");

    	byte[] packet = new byte[packetLength+headerLength];

    	System.arraycopy(fileContent, index, packet, headerLength-1, packetLength);
    	if(Math.random() < 0.1)
    	    packet[0] = (byte) -128; //corrupts about 1 in 10 packets, on average.
    	packet[1] = (byte) packetCount;
    	//for(int i = 0; i < packetLength; i++) {
    	//	packet[i] = fileContent[index];
    	//	index++;
    	//}
    	packetCount++;
    	return packet;
    }
}