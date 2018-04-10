
package PaquetePrincipal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import vistas.Vista10Control;

/**
 * Esta clase inicia un servidor HTTP y configura un Pool de conexiones en función
 * de la demanda y las posibilidades del servidor. Se cran instancias de {@link ServidorHTTP}
 * 
 * @author José Francisco Sánchez Portillo
 */
public class ConectorServer {
    
    private ServerSocket serverSocket;
    // Se configura el puerto del servidor:
    private static final int PUERTO =8066;
    // Se configura el tiempo máximo de espera de respuesta:
    private static final int TIMEOUT_CLIENTE = 30 * 1000; // 30 seg
    

    /**
     * Método constructor: Inicia el servidor y crea un Pool de conexiones en función
     * de la demanda y las posibilidades del servidor.
     */
    public ConectorServer() {
        
        //Log log = new Log();
 
        try{
            // Inicio el servidor: solo se ejecuta una vez
            inicioServidor();
            // Atiendo al cliente mediante un pool de thread:
            ExecutorService pool = Executors.newCachedThreadPool();

            while(true){
                // Se conecta un cliente
                Socket socket = serverSocket.accept(); 
                // Atiendo al cliente mediante un thread:
                //new ServidorHTTP(id, socket).start();
                // ó
                // Atiendo al cliente mediante un pool de thread:
                pool.execute(new ServidorHTTP(socket, TIMEOUT_CLIENTE));
            }

        } catch (Exception e) {
            //Vista10Control.log("Error: No es posible la conexión con Clientes: " + e.getMessage() + "\n");
            System.out.println("Error: No es posible la conexión con Clientes: " + e.getMessage());
        }

    }
    
    /**
     * Este método inicia el servidor y si todo va bien informa del servicio.
     */
    public void inicioServidor() {
        try {
            serverSocket = new ServerSocket(PUERTO);
            imprimeDisponible();
            //Vista10Control.log("Servidor> Servidor en funcionamiento...\n");
        } catch (IOException ex) {
            //Vista10Control.log("Servidor> No es posible iniciar el servicio: " + ex.getMessage() + "\n");
            System.out.println("Error: El servidor HTTP no está iniciado: " + ex.getMessage());
        }
    }
    

    /**
     * Mensaje de bienvenida al servidor. Imprime por consola.
     * Muestra un mensaje en la Salida que confirma el arranque, y da algunas
     * indicaciones posteriores
     */
  private static void imprimeDisponible() {

    System.out.println("El Servidor WEB se está ejecutando y permanece a la escucha por el puerto 8066."
            + "\n\nEscribe en la barra de direcciones de tu navegador preferido para cargar una página:"
            + "\n\n\thttp://localhost:8066/agencia/index.html"
            + "\n\nLa página que se muestra es un trabajo realizado el año pasado "
            + "\npara el módulo de DAM LMSGI en la tarea 2,"
            + "\nque he aprovechado para utizarlo de prueba para ver como funciona este servidor."
            + "\n\n\nOtras páginas disponibles:\n"
            + "\n\thttp://localhost:8066/prueba/index.html => Esta es la página simple creada por defecto.\n"
            + "\n\thttp://localhost:8066 => Esto simula una página no existente."
            + "\n\n");
  }
    
}
