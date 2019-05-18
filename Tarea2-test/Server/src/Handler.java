package Server;

import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

class Handler implements Runnable{
    private String[] parts = null;
    private String filename = null;
    private Socket socket = null;
    private Socket msocket = null;
    private Date date;
    private SimpleDateFormat ft;
    private DataOutputStream out     = null;
    private DataOutputStream mos     = null;
    private DataInputStream mis = null;
    private String wait = "Please use command (ls, get, put, delete, quit):";
    private File currentDir = new File("./servidor");
    private List<Boolean> alive = new ArrayList<Boolean>();
    private List frommaq = new ArrayList();
    private List tomaq = new ArrayList();

    public Handler(Socket clientSocket){
        this.socket = clientSocket;
    }

    public void run(){
        try {
            InputStream input  = socket.getInputStream();
            DataInputStream in = new DataInputStream(new BufferedInputStream(input));
            OutputStream output = socket.getOutputStream();
            OutputStream outfile = null;
            String line = in.readUTF();
            String maqline;
            System.out.println(line);
            int bytesRead;
            int current = 0;

            //conexion a maquinas
            for (int i=0;i<Servidor.nmaq;i++){
                msocket = new Socket(Servidor.ipMaquinas.get(i),(int)Servidor.portMaquinas.get(i));
                frommaq.add(new DataInputStream(new BufferedInputStream(msocket.getInputStream())));
                tomaq.add(new DataOutputStream(msocket.getOutputStream()));
                alive.add(true);
                mos = (DataOutputStream)tomaq.get(i);
                mos.writeUTF("requesting connection");
                mis = ((DataInputStream) frommaq.get(i));
                maqline = mis.readUTF();
                System.out.println(maqline);
                ((DataOutputStream) tomaq.get(i)).writeUTF("10-4");
            }


            InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
            String address = isa.getAddress().getHostAddress();
            date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            Servidor.logger(format.format(date)+"\t connection\t"+ address +" conexion entrante");

            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("connection established");
            out.writeUTF(wait);
            line = in.readUTF();
            parts = line.split(" ");
            while ( !parts[0].equals("quit")){
                switch (parts[0]){
                    case "ls":
                        Servidor.logger(format.format(date)+"\t command\t"+ address +" ls");
                        ls(currentDir, out);
                        out.writeUTF("DONE");
                        Servidor.logger(format.format(date)+"\t response\t"+ "servidor envia respuesta a " + address);
                        out.writeUTF(wait);
                        break;
                    case "put":
                        Servidor.logger(format.format(date)+"\t command\t"+ address +" "+line);
                        outfile = new FileOutputStream("./servidor/" + in.readUTF());
                        long size = in.readLong();
                        byte[] buffer = new byte[1024];
                        while (size > 0 && (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1){
                            outfile.write(buffer, 0, bytesRead);
                            size -= bytesRead;
                        }

                        outfile.close();
                        out.writeUTF("archivo subido");
                        Servidor.logger(format.format(date)+"\t response\t"+ "servidor envia respuesta a " + address);
                        out.writeUTF(wait);
                        break;
                    case "get":
                        Servidor.logger(format.format(date)+"\t command\t"+ address +" "+line);
                        File myFile = new File("./servidor/"+parts[1]);
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
                        Servidor.logger(format.format(date)+"\t response\t"+ "servidor envia respuesta a " + address);
                        out.writeUTF(wait);
                        break;
                    case "delete":
                        Servidor.logger(format.format(date)+"\t command\t"+ address +" "+line);
                        File delfile = new File("./servidor/"+parts[1]);
                        if(delfile.delete()){
                            out.writeUTF("Archivo eliminado");
                        }else out.writeUTF("Archivo no existe");
                        Servidor.logger(format.format(date)+"\t response\t"+ "servidor envia respuesta a " + address);
                        out.writeUTF(wait);
                        break;
                    default:
                        out.writeUTF(wait);

                }
                line = in.readUTF();
                parts = line.split(" ");
            }
            Servidor.logger(format.format(date)+"\t connection\t"+ address +" conexion terminada");
            output.close();
            input.close();
            System.out.println("Request processed");
            Servidor.threadlisto(); //Quito uno del contador de threads del servidor
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

