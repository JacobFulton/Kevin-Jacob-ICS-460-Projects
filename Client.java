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

                    if(fileM.isCorrupted((response.getData()))){
                        System.out.println("[NOTadded(corr)]: ");
                    }else if((int)fileM.getSeqno(response.getData()) > ackno){ //compares ackno of packet to ackno of last ack.
                        fileM.addPacket(response.getData()); //adds packet only if ackno of packet is greater
                        System.out.println("[ADDed]: "+(int)fileM.getSeqno(response.getData()));
                    }else{
                        System.out.println("[NOTadded(doup)]: "+(int)fileM.getSeqno(response.getData()));
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
                    ackno = (int)fileM.getSeqno(response.getData()); //sets ackno of ack to ackno of packet.
                    }else
                        checksum = 1;
                    ackBB.putInt(checksum);
                    ackBB.putInt(ackno);
                    ackBB.rewind();
                    ackBB.get(ackArray);
                    DatagramPacket acknoPacket = new DatagramPacket(ackArray, ackArray.length, host, PORT);

                    if(Math.random() < .1){
                        System.out.println("[SENDing ACK]: " + ackno +" [DRPT]");
                    }else{
                        socket.send(acknoPacket);
                        System.out.print("[SENDing ACK]: " + ackno);
                        if(checksum == 0){
                            System.out.println(" [SENT]");
                        }else
                            System.out.println(" [ERR]");
                    }



                    socket.setSoTimeout(2000);


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