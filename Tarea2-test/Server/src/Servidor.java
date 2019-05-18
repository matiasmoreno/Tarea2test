package Server;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Vector;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Servidor {
    private int port = 1238; // Puerto para la conexión del Servidor
    private int portm = 1234;
    protected ServerSocket ss; //Socket del servidor
    protected Socket cs; //Socket del cliente
    private static ThreadPool pool;
    private static Boolean serverup = Boolean.TRUE;
    private static PrintWriter log;
    public static void threadlisto() {
        pool.quitar_thread();
    }
    protected static List<String> ipMaquinas = new Vector<String>();
    protected static List portMaquinas = new Vector();
    protected static List<String> nameIndex = new Vector<String>();
    protected static List dataIndex = new Vector();
    protected static int nmaq;

    public void startServer(){
        try
        {
            pool = new ThreadPool(5);
            ss = new ServerSocket(port);//Se crea el socket para el servidor en puerto 11580
            cs = new Socket(); //Socket para el cliente
            nmaq= 2;
            /*for(int i=0;i<nmaq;i++){
                ipMaquinas.add("127.0.0.1");

                portMaquinas.add(portm+i);
            }*/
            ipMaquinas.add("10.6.40.177");
            portMaquinas.add(8080);
            ipMaquinas.add("10.6.40.178");
            portMaquinas.add(8080);
            try {
                FileWriter fw = new FileWriter("./log/log.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                log = new PrintWriter(bw);
            }catch (IOException e) {
                e.printStackTrace();
            }

            while(serverup) {
                System.out.println("Servidor sperando a un cliente..."); //Esperando conexión
                cs = ss.accept(); //Accept comienza el socket y espera una conexión desde un cliente
                if (pool.anadir_thread()){
                    System.out.println("Hay threads disponibles, cliente conectado, asignando thread");
                    new Thread (new Handler(cs)).start();
                }
                else{
                    OutputStream output = cs.getOutputStream();
                    DataOutputStream respuesta = new DataOutputStream(output);
                    respuesta.writeUTF("Server dice: Servidor colapsado, intentar mas tarde");
                    InetSocketAddress isa = (InetSocketAddress) cs.getRemoteSocketAddress();
                    String address = isa.getAddress().getHostAddress();
                    Date date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                    logger(format.format(date)+"\t error\t"+ "conexion rechazada por "+ address);
                    output.close();
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] args) throws IOException
    {
        Servidor serv = new Servidor(); //Se crea el servidor
        System.out.println("Iniciando servidor\n");
        serv.startServer(); //Se inicia el servidor
    }
    public static synchronized void logger(String print) {
        log.println(print);
        log.flush();
    }
}