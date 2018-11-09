import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;



public class Server {

    private final static int PORT = 2001;

    public static void main(String[] args) throws IOException {

        //Take a file.

        FileManager fileM = new FileManager(1024);

        System.out.println("enter path of file to send");

        Scanner input = new Scanner(System.in);

        String filePath = input.next();

/** if (args.length > 0) {
            fileName = args[0];
        }
        else fileName = "didn'twork.jpeg";
**/


        //Put it into a byte[]

        byte[] bytesArray = fileM.importFile(filePath);
/**
        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray);
        fis.close();
**/
        try (DatagramSocket socket = new DatagramSocket(PORT)) {

            System.out.println("Waiting for Request...");

            while (true) {

                try {

                    DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
                    DatagramPacket acknowledgement = new DatagramPacket(new byte[1024],1024);

                    socket.receive(request);

                    System.out.println("Request Received.");
                    byte[] packet = null;
                    boolean init = true;
                    int ackno = 0;
                    packet = fileM.nextPacket();
                    while(packet != null || init) {
                        init = false;

                        DatagramPacket response = new DatagramPacket(packet, fileM.getPacketLength(), request.getAddress(), request.getPort());
                        byte[] ackPacket = new byte[8];
                        socket.send(response);
                        if(ackno == fileM.getSeqno(packet))
                            System.out.println("[ReSENDing]:"+fileM.getSeqno(packet) );
                        System.out.println("[SENDing]:"+fileM.getSeqno(packet) );
                        socket.receive(acknowledgement);
                        System.out.print("[AckRcvd]:");

                        //Break down acknowledgement
                        byte [] acknodata = acknowledgement.getData();
                        ByteBuffer bb = ByteBuffer.wrap(acknodata);
                        int corrupted = bb.getInt();
                        ackno = bb.getInt();
                        System.out.print(ackno);
                        if(corrupted == 1)
                        System.out.println(" [ErrAck]");
                        else{
                            System.out.println(" [MoveWnd]");
                            packet = fileM.nextPacket();
                        }

                    }
                    byte[] end = null;
                    socket.send( new DatagramPacket(end, 0, request.getAddress(), request.getPort()));


                //System.out.println("File Sent");
                //Null Pointer signals end of file.
                } catch (NullPointerException ex) {
                    System.out.println("File Sent");
                    break;

                } catch (IOException ex) {

                    ex.printStackTrace();

                }

            }

        } catch (IOException ex){

            ex.printStackTrace();

        }



    }



}