

import java.io.*;
import java.nio.file.*;


public class FileManager {

    private File file;
    private int packetLength;
    private int index;
    private int header;
    private int checksumCor;
    private int packetCount;
    byte[] fileContent = null;
    byte[] packetContent = null;
    private int fileSize;
    public FileManager(int packetLength) {
        index = 0;
        checksumCor = 65535;
        header = 12;
        packetCount = 1;
        this.packetLength = packetLength;
    }

    public byte[] getPacket(){
        if (packetContent != null)
        Corrupt(packetContent);
        return packetContent;
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
        if(getChecksum(packet) != checksumCor)
            return false;
        return true;
    }

    public boolean isNext(byte[] packet){
        if(getSeqno(packet) ==  packetCount)
            return true;
        return false;
    }

    public void Corrupt(byte[] packet){
        packet[0] = (byte) 0;
        packet[1] = (byte) 0;
        if(Math.random() < 0.1){
            packet[0] = (byte)((checksumCor>>8)&255); //"corrupts" about 1 in 10 packets, on average.
            packet[1]= (byte)(checksumCor&255);
            }
    }

    public int getChecksum(byte[] packet){
        int checksum = (((packet[0]<<8)&65280)|(packet[1]&255));
        return checksum;
    }

    public int getPLength(byte[] packet){
        int length = (((packet[2]<<8)&65280)|(packet[3]&255));
        return length;
    }

    public long getSeqno(byte[] packet){
        long seqno = (((packet[8]<<24)&4278190080l)|((packet[9]<<16)&16711680l)|((packet[10]<<8)&65280)
                            |(packet[11]&255));
        return seqno;
    }

    public long getAckno(byte[] packet){
        long ackno = (((packet[4]<<24)&4278190080l)|((packet[5]<<16)&16711680l)|((packet[6]<<8)&65280)
                            |(packet[7]&255));
        return ackno;
    }

    public String getHeader(byte[] packet){
        String header = (" "+getChecksum(packet)+" "+getPLength(packet)+" "+getSeqno(packet)+" "+getAckno(packet)+" ");
        return header;
    }

    public void addPacket(byte[] packet) {
        int pStart = index;
        int pEnd = index+packet.length-(1+header);
        //System.out.println("["+pStart+"]-["+pEnd+"]");
        //System.out.println(getHeader(packet));

        if(fileContent == null) {
            fileContent = new byte[1024];
        }else {
        byte[] temp = new byte[index+packet.length];
        System.arraycopy(fileContent, 0, temp, 0, index);
        fileContent = temp;
        //System.out.println(fileContent.length);zqwas2
        }

        for(int i = header; i < packet.length; i++) {
            fileContent[i+index-header] = packet[i];


        }
        index = index + packet.length-header;
        packetCount++;
    }

    public byte[] nextPacket() {
        int pStart = index;
        if((index == fileContent.length)) {
            byte[] end = null;
            return end;
        }

        if((index+packetLength) > fileContent.length){
            packetLength = fileContent.length - index+header;
        }

        int pEnd = index+packetLength-(1+header);
        //System.out.println(" ["+pStart+"]-["+pEnd+"]");
        byte[] packet = new byte[packetLength];
        for(int i = header; i < packetLength; i++) {
            packet[i] = fileContent[index];
            index++;
            //System.out.println(index+"::"+fileContent.length);
        }

        Corrupt(packet);

        packet[2] = (byte)((packetLength>>8)&255);
        packet[3] = (byte)(packetLength&255);

        //adding seqno to header
        packet[8] = (byte)((packetCount>>24)&255);
        packet[9] = (byte)((packetCount>>16)&255);
        packet[10] = (byte)((packetCount>>8)&255);
        packet[11] = (byte)(packetCount&255);

        packetCount++;
        //adding ackno to header
        packet[4] = (byte)((packetCount>>24)&255);
        packet[5] = (byte)((packetCount>>16)&255);
        packet[6] = (byte)((packetCount>>8)&255);
        packet[7] = (byte)(packetCount&255);
        packetContent = packet;
        return packet;
    }
}