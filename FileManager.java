

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
        this.packetLength = packetLength-headerLength;
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

        if(packet[0] == (byte) -128) //if the first bit is equal to 1111 1111, then that packet is corrupted.
            return true;
        return false;
    }

    public byte[] extractPacketHeader(byte[] packet){

        byte[] header = Arrays.copyOfRange(packet, 0, headerLength); //Copies header
        return header;
    }

    public byte[] extractPacket(byte[] packet){

        byte[] pac = Arrays.copyOfRange(packet, headerLength, packet.length);//Copies packet after header
        return pac;
    }


    public boolean addPacket(byte[] packet) {

        if(isCorrupted(packet) || packet[1] != (byte) packetCount){
           // return false; //does not add packet if the packet is corrupted or if it's not the next packet.
        }

        byte[] newPacket = extractPacket(packet);
        int pStart = index;
        int pEnd = index+newPacket.length-1;
      //  for(int i = 0; i < packet.length; i++ )
      //      System.out.println(packet[i]);
        System.out.println(newPacket.length);
        System.out.println("R[" + (packetCount+1) + "]-[" + pStart + "]-[" + pEnd + "]");
        //System.out.println(index);

        if(fileContent == null) {
            fileContent = newPacket;
        }else {



        byte[] temp = new byte[fileContent.length + newPacket.length];

        System.arraycopy(fileContent, 0, temp, 0, fileContent.length);

        System.arraycopy(newPacket, 0, temp, index, newPacket.length);

        fileContent = temp;
        }
        index = index + newPacket.length;
        System.out.println(index);
        packetCount++;
        return true;
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

        System.arraycopy(fileContent, index, packet, headerLength, packetLength);

        packet[0] = (byte) 0;
        if(Math.random() < 0.1)
            packet[0] = (byte) -128; //"corrupts" about 1 in 10 packets, on average.
        packet[1] = (byte) packetCount;

        index = index + packetLength;

        packetCount++;
        return packet;
    }
}