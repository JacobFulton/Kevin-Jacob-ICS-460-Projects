

import java.io.*;
import java.nio.file.*;


public class FileManager {

    private File file;
    private int packetLength;
    private int index;
    private int packetCount;
    byte[] fileContent = null;
    private int fileSize;
    public FileManager(int packetLength) {
        index = 0;
        packetCount = 1;
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
            fileSize = fileContent.length;
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
        if(packet[0] == (byte) 0)
            return false;
        return true;
    }

    public boolean isNext(byte[] packet){
        if(packet[1] == (byte) packetCount)
            return true;
        return false;
    }

    public void addPacket(byte[] packet) {
        int pStart = index;
        int pEnd = index+packet.length-3;
        System.out.println("R["+packetCount+"]-["+pStart+"]-["+pEnd+"]");


        if(fileContent == null) {
            fileContent = new byte[1024];
        }else {
        byte[] temp = new byte[index+packet.length];
        System.arraycopy(fileContent, 0, temp, 0, index);
        fileContent = temp;
        //System.out.println(fileContent.length);
        }

        for(int i = 2; i < packet.length; i++) {
            fileContent[i+index-2] = packet[i];


        }
        index = index + packet.length-2;
        packetCount++;
    }

    public byte[] nextPacket() {
        int pStart = index;
        if((index == fileContent.length)) {
            byte[] end = null;
            return end;
        }

        if((index+packetLength) > fileContent.length){
            packetLength = fileContent.length - index+2;
        }

        int pEnd = index+packetLength-3;
        System.out.println("S["+packetCount+"]-["+pStart+"]-["+pEnd+"]");
        byte[] packet = new byte[packetLength];
        for(int i = 2; i < packetLength; i++) {
            packet[i] = fileContent[index];
            index++;
            //System.out.println(index+"::"+fileContent.length);
        }

        packet[0] = (byte) packetCount;
        packet[1] = (byte) 0;
        if(Math.random() < 0.1)
            packet[0] = (byte) -128; //"corrupts" about 1 in 10 packets, on average.


        packetCount++;
        return packet;
    }
}