package Maquina;

import java.net.*;
import java.io.*;


class fileHandler implements Runnable{
    private String[] parts = null;
    private String filename = null;
    private Socket socket = null;
    private DataOutputStream out     = null;
    private static String wait = "Please use command (ls, get, put, delete, quit):";
    private File currentDir = new File("./Maquina");

    public fileHandler(Socket clientSocket){
        this.socket = clientSocket;
    }

    public void run(){
        try {
            InputStream input  = socket.getInputStream();
            DataInputStream in = new DataInputStream(new BufferedInputStream(input));
            OutputStream output = socket.getOutputStream();
            OutputStream outfile = null;
            String line = in.readUTF();
            System.out.println(line);
            int bytesRead;
            int current = 0;

            InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
            String address = isa.getAddress().getHostAddress();

            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("connection established");
            line = in.readUTF();
            System.out.println(line);

            line = in.readUTF();
            parts = line.split(" ");
            while ( !parts[0].equals("quit")){
                switch (parts[0]){
                    case "put":
                        outfile = new FileOutputStream("./Maquina/" + in.readUTF());
                        long size = in.readLong();
                        byte[] buffer = new byte[1024];
                        while (size > 0 && (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1){
                            outfile.write(buffer, 0, bytesRead);
                            size -= bytesRead;
                        }

                        outfile.close();
                        out.writeUTF("archivo subido");
                        out.writeUTF(wait);
                        break;
                    case "get":
                        File myFile = new File("./Maquina/"+parts[1]);
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
                        out.writeUTF(wait);
                        break;
                    case "delete":
                        File delfile = new File("./Maquina/"+parts[1]);
                        if(delfile.delete()){
                            out.writeUTF("Archivo eliminado");
                        }else out.writeUTF("Archivo no existe");
                        out.writeUTF(wait);
                        break;
                    default:
                        out.writeUTF(wait);

                }
                line = in.readUTF();
                parts = line.split(" ");
            }
            output.close();
            input.close();
            System.out.println("Request processed");
            Maquina.threadlisto(); //Quito uno del contador de threads del servidor
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }

    public static void ls(File dir, DataOutputStream out) {
        try {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    out.writeUTF("     carpeta:    " + file.getName());
                    //ls(file, out);
                } else {
                    out.writeUTF("     archivo:    " + file.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

