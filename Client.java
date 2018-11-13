import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;



public class Client {



    private final static int PORT = 2001;

    private final static String HOSTNAME = "127.0.0.1";





    public static void main(String[] args) throws IOException {
        int ackno = 0;

        try (DatagramSocket socket = new DatagramSocket(0)){

            FileManager fileM = new FileManager(1024);

            System.out.println("enter download path");

            Scanner input = new Scanner(System.in);

            String path = input.next();

            socket.setSoTimeout(10000);

            InetAddress host = InetAddress.getByName(HOSTNAME);

            DatagramPacket request = new DatagramPacket(new byte[1], 1, host, PORT);

            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);



            socket.send(request);

            FileOutputStream fos = new FileOutputStream(path);

            socket.receive(response);
            boolean end = true;
            while(response.getLength() != 0 && end) {
                try {
                    int byteOS = (int) ((fileM.getSeqno(response.getData())-1)*1012);
                    if(fileM.isCorrupted((response.getData()))){
                        System.out.println("[RECV]: "+(int)fileM.getSeqno(response.getData()) + " " +byteOS + ":" +
                            (byteOS+response.getData().length-12)+" " + System.currentTimeMillis() + " [CRPT]");
                    }else if((int)fileM.getSeqno(response.getData()) > ackno){ //compares ackno of packet to seqno of last ack.
                        fileM.addPacket(response.getData()); //adds packet only if the next packet is sent
                        System.out.println("[RECV]: "+(int)fileM.getSeqno(response.getData()) + " " + byteOS + ":" +
                            (byteOS+response.getData().length-12)+" " + System.currentTimeMillis() + " [RECV]");
                    }else{
                        System.out.println("[RECV]: "+(int)fileM.getSeqno(response.getData()) + " " +byteOS + ":" +
                            (byteOS+response.getData().length-12)+" " +System.currentTimeMillis() + " [!Seq]");
                    }
                    int checksum;
                    //Set up acknowledgement
                    byte[] ackArray = new byte[8];
                    ByteBuffer ackBB = ByteBuffer.allocate(8);

                    if(Math.random() < .1)
                        checksum = 1;
                    else
                        checksum = 0;


                    if(!fileM.isCorrupted((response.getData()))){
                    ackno = (int)fileM.getSeqno(response.getData()); //sets ackno of ack to seqno of packet.
                    }else
                        checksum = 1;
                    ackBB.putInt(checksum);
                    ackBB.putInt(ackno);
                    ackBB.rewind();
                    ackBB.get(ackArray);
                    DatagramPacket acknoPacket = new DatagramPacket(ackArray, ackArray.length, host, PORT);

                    if(Math.random() < .1){
                        System.out.println("[SENDing ACK]: " + ackno + " " + byteOS + ":" +
                            (byteOS+response.getData().length-12)+" " +System.currentTimeMillis() + " [DRPT]");
                    }else{
                        socket.send(acknoPacket);
                        System.out.print("[SENDing ACK]: " + ackno + " " +byteOS + ":" +
                            (byteOS+response.getData().length-12)+" " +System.currentTimeMillis());
                        if(checksum == 0){
                            System.out.println(" [SENT]");
                        }else
                            System.out.println(" [ERR]");
                    }




                    if(response.getLength() < 1024)
                        break;

                    socket.receive(response);
                } catch (SocketTimeoutException e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();


                }

            }

            fos.write(fileM.fileContent);

            fos.close();

            System.out.println("File created.");

            //Testing System.out.println(response.getData().length);

        } catch (IOException ex) {

            ex.printStackTrace();

        }

    }



}