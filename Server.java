import java.io.*;
import java.net.*;
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

                    socket.receive(request);

                    System.out.println("Request Received.");
                    byte[] packet = null;
                    boolean init = true;
                    while(packet != null || init) {
                        init = false;
                        packet = fileM.nextPacket();

                        DatagramPacket response = new DatagramPacket(packet, fileM.getPacketLength(),
                                                                         request.getAddress(), request.getPort());

                        socket.send(response);
                        if(packet.length < 1024){

                        byte[] end = null;
                        socket.send( new DatagramPacket(end, 0, request.getAddress(), request.getPort()));
                        }
                    }
                    System.out.println("sending:null");



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