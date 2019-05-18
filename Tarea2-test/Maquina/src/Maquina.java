package Maquina;

import java.io.*;
import java.net.*;

public class Maquina {
    //private int port = 1234; // Puerto para la conexión del Servidor
    protected ServerSocket ss; //Socket del servidor
    protected Socket cs; //Socket del cliente
    private static ThreadPool pool;
    private static Boolean serverup = Boolean.TRUE;
    public static void threadlisto() {
        pool.quitar_thread();
    }
    public void startMaquina(int port)//Método para iniciar el servidor
    {
        try
        {
            pool = new ThreadPool(5);
            ss = new ServerSocket(port);//Se crea el socket para el servidor en puerto port
            cs = new Socket(); //Socket para el cliente

            while(serverup) {
                System.out.println("Maquina esperando a un cliente..."); //Esperando conexión
                cs = ss.accept(); //Accept comienza el socket y espera una conexión desde un cliente
                if (pool.anadir_thread()){
                    System.out.println("Hay threads disponibles, cliente conectado, asignando thread");
                    new Thread (new fileHandler(cs)).start();
                }
                else{
                    OutputStream output = cs.getOutputStream();
                    DataOutputStream respuesta = new DataOutputStream(output);
                    respuesta.writeUTF("Server dice: Servidor colapsado, intentar mas tarde");
                    InetSocketAddress isa = (InetSocketAddress) cs.getRemoteSocketAddress();
                    String address = isa.getAddress().getHostAddress();
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
        Maquina maq = new Maquina(); //Se crea el servidor
        //System.out.println("Ingrese numero de puerto\n");
        //BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        //int port = Integer.parseInt(reader.readLine());
        System.out.println("Iniciando maquina\n");
        maq.startMaquina(8080); //Se inicia el servidor
    }
    /*
    public static synchronized void logger(String print) {
        log.println(print);
        log.flush();
    }
*/
}
