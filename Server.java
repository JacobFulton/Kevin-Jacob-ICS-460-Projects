import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;



public class Server {

    private final static int PORT = 2001;
    private static int expected = 1;

    public static void main(String[] args) throws IOException {
        boolean reSend = false;
        boolean firstPacketSent = false;
        byte[] packet = null;
        DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
        DatagramPacket acknowledgement = new DatagramPacket(new byte[1024],1024);
        int ackno = 0;

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
                    if (firstPacketSent == false) {

                        socket.receive(request);

                        System.out.println("Request Received.");
                        firstPacketSent = true;

                        packet = fileM.nextPacket();
                    }
                    try{

                        while(packet != null) {
                            int byteOS = (int) ((fileM.getSeqno(packet)-1)*1012);
                            DatagramPacket response = new DatagramPacket(packet, fileM.getPacketLength(), request.getAddress(), request.getPort());
                            if(Math.random() < .10){
                                if (reSend == true) {
                                    System.out.println("[ReSEND.]:"+ (fileM.getSeqno(packet)) + " "+byteOS + ":" +
                                        (byteOS+packet.length-12)+" " + System.currentTimeMillis() + " [DRPT]" );
                                    throw new SocketTimeoutException();
                                }
                                else {
                                    System.out.println("[SENDing]:"+ (fileM.getSeqno(packet)) + " " +byteOS + ":" +
                                        (byteOS+packet.length-12)+" " + System.currentTimeMillis() + " [DRPT]" );
                                    reSend = true;
                                    throw new SocketTimeoutException();
                                }
                                //need to cause a timeout here
                            }else{
                                socket.send(response);
                            }

                            if(ackno == fileM.getSeqno(packet))
                                System.out.println("[ReSEND.]:"+ (fileM.getSeqno(packet)) + " " +byteOS + ":" +
                                    (byteOS+packet.length-12)+" " + System.currentTimeMillis() + " [SENT]" );
                            else {
                                if (reSend == true)
                                    System.out.println("[ReSEND.]:"+ (fileM.getSeqno(packet)) +  " " + byteOS + ":" +
                                        (byteOS+packet.length-12)+" " +System.currentTimeMillis() + " [SENT]");
                                else
                                    System.out.println("[SENDing]:"+ (fileM.getSeqno(packet)) +  " " +byteOS + ":" +
                                        (byteOS+packet.length-12)+" " + System.currentTimeMillis() + " [SENT]");
                            }
                            socket.setSoTimeout(2000);
                            socket.receive(acknowledgement);
                            System.out.print("[AckRcvd]:");
                            reSend = false;

                            //Break down acknowledgement
                            byte [] acknodata = acknowledgement.getData();
                            ByteBuffer bb = ByteBuffer.wrap(acknodata);
                            int corrupted = bb.getInt();
                            ackno = bb.getInt();
                            System.out.print(ackno);

                            if (ackno == expected) {

                                if(corrupted == 1){
                                    fileM.Corrupt(packet);
                                    System.out.println(" [ErrAck]");
                                }
                                else{
                                    System.out.println(" [MoveWnd]");
                                    packet = fileM.nextPacket();
                                    expected++;
                            }
                            }

                            else {
                                 System.out.println(" [DuplAck]");
                                 reSend = true;
                                 fileM.Corrupt(packet);
                            }

                            if(response.getLength() < 1024)
                                break;

                        }
                    }catch (SocketTimeoutException ex) {
                            System.out.println("Timeout " + expected);
                            reSend = true;
                            //Ensure packet doesn't stay broken
                            fileM.Corrupt(packet);
                            continue;
                    }

                    byte[] end = null;
                    socket.send( new DatagramPacket(end, 0, request.getAddress(), request.getPort()));


                //System.out.println("File Sent");
                //Null Pointer signals end of file.
                }catch (SocketTimeoutException ex) {
                    ex.printStackTrace();

                }catch (NullPointerException ex) {
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