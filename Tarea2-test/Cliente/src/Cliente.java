package Cliente;

import java.net.*;
import java.io.*;

public class Cliente{

    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out     = null;
    private String[] parts = null;

    public Cliente(String  address, int port){

        //connect
        try{
            socket = new Socket(address, port);
            System.out.println("Conectado");

            //input = new DataInputStream(System.in);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            OutputStream os =socket.getOutputStream();
            out = new DataOutputStream(os);
            out.writeUTF("requesting connection");
            InputStream input  = socket.getInputStream();
            DataInputStream in = new DataInputStream(new BufferedInputStream(input));
            OutputStream outfile = null;
            int bytesRead;
            int current = 0;

            String line = in.readUTF();
            System.out.println(line);
            out.writeUTF("10-4");
            line = in.readUTF();
            System.out.println(line);

            try{
                line = reader.readLine();
                parts = line.split(" ");
                while ( !parts[0].equals("quit")){
                    switch (parts[0]){
                        case "ls":
                            out.writeUTF(line);
                            line = in.readUTF();
                            while(!line.equals("DONE")) {
                                System.out.println(line);
                                line = in.readUTF();
                            }
                            break;
                        case "put":
                            out.writeUTF(line);
                            File myFile = new File(parts[1]);
                            byte[] mybytearray = new byte[(int) myFile.length()];
                            FileInputStream fis = new FileInputStream(myFile);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            DataInputStream dis = new DataInputStream(bis);
                            dis.readFully(mybytearray, 0, mybytearray.length);
                            out.writeUTF(myFile.getName());
                            out.writeLong(mybytearray.length);
                            out.write(mybytearray, 0, mybytearray.length);
                            out.flush();
                            dis.close();
                            line = in.readUTF();
                            System.out.println(line);

                            //os.write(mybytearray, 0, mybytearray.length);
                            //os.flush();
                            break;
                        case "get":
                            out.writeUTF(line);
                            outfile = new FileOutputStream(in.readUTF());
                            long size = in.readLong();
                            byte[] buffer = new byte[1024];
                            while (size > 0 && (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1){
                                outfile.write(buffer, 0, bytesRead);
                                size -= bytesRead;
                            }

                            outfile.close();
                            System.out.println("archivo descargado");

                            break;
                        case "delete":
                            out.writeUTF(line);
                            line = in.readUTF();
                            System.out.println(line);
                            break;
                        default:
                            out.writeUTF(line);
                    }
                    line = in.readUTF();
                    System.out.println(line);
                    line = reader.readLine();
                    parts = line.split(" ");
                }


            }
            catch(IOException i){
                System.out.println(i);
            }
            out.writeUTF(line);
        }
        catch(UnknownHostException u){
            System.out.println(u);
        }
        catch(IOException i){
            System.out.println(i);
        }




        // close the connection
        try{
            //input.close();
            out.close();
            socket.close();
        }
        catch(IOException i)        {
            System.out.println(i);
        }

    }

    public static void main(String args[]){
        //Cliente cliente = new Cliente("127.0.0.1", 1238);
        Cliente cliente = new Cliente("10.6.40.177", 1238);
    }
}
