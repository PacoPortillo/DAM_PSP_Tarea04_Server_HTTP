package PaquetePrincipal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Servidor HTTP que atiende peticiones de tipo 'GET' recibidas por el puerto
 * 8066, de manera concurrente y enviando contenido HTML. Las páginas se deben
 * cargar en carpetas (una por sitio) en la ruta ./www/
 *
 * NOTA: para probar este código, comprueba primero de que no tienes ningún otro
 * servicio por el puerto 8066 (por ejemplo, con el comando 'netstat' si estás
 * utilizando Windows)
 *
 * @author IMCG y José Francisco Sánchez Portillo
 */
public class ServidorHTTP extends Thread {

    //private Log log;
    private Socket socket;

    private int timeout_cliente;

    private final static String CRLF = "\r\n";//returning carriage return (CR) and a line feed (LF)

    /**
     * Constructor Para cada conexión del cliente al servidor: esto es una
     * petición.
     *
     * @param socket Conexión
     * @param timeoutCliente Tiempo máximo de espera.
     * @throws InterruptedException Excepciones de ocupación del Thread.
     */
    public ServidorHTTP(Socket socket, int timeoutCliente) throws InterruptedException {

        this.socket = socket;
        this.timeout_cliente = timeoutCliente;

    }

    /**
     * Por cada instancia de ServidorHTTP este método procesa una petición,
     * devuelve el resultado y se desconecta.
     */
    @Override
    public void run() {
        try {
            socket.setSoTimeout(timeout_cliente);
            System.out.println("******************************************************* Atendiendo al cliente "
                    + Thread.currentThread().getName());
            procesaPeticion(socket);
            //cierra la conexión entrante
            socket.close();
            System.out.println("************************************************************ Cliente atendido "
                    + Thread.currentThread().getName() + "\n\n");
        } catch (SocketTimeoutException e) {
            System.out.println("\n\nError: Tiempo excedido " + e.getMessage() + "\n\n");
        } catch (IOException ex) {
            System.out.println("\n\nError: Al prcesar la respuesta " + ex.getMessage() + "\n\n");
        } catch (Exception ed) {
            System.out.println("\n\nError: " + ed.getMessage() + "\n\n");
        }
    }

    /**
     * Procesa la petición recibida desde el cliente y envia el resultado.
     *
     * @param socket Recibe el socket de conexión
     * @throws SocketTimeoutException Tiempo excedido
     * @throws IOException De entrada y salida
     * @throws Exception Erroes genéricos
     */
    private void procesaPeticion(Socket socket) throws SocketTimeoutException, IOException, Exception {
        // Recibimos la referencia del socket y los datos que contienen:
        System.out.println(socket);
        InputStream is = socket.getInputStream();
        // Configuramos la entrada de datos y leemos el contenido:
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // Creamos la salida de datos:
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        //**********************************************************************
        // Recuperamos la primera línea de la petición del Cliente:
        String peticion = br.readLine();// GET /path/file.html HTTP/1.1
        System.out.println("Solicita: " + peticion);
        // Recuperamos el nombre del archivo:
        StringTokenizer tokens = new StringTokenizer(peticion); // Separamos la primera línea en trozos:
        tokens.nextToken(); // omitimos el GET (primer trozo)
        String rutaArchivo = tokens.nextToken(); // Nos quedamos con la ruta al archivo (segundo trozo):
        System.out.println("Archivo: " + rutaArchivo); // /path/file.html

        // Componemos la ruta relativa a la carpeta de webs:
        rutaArchivo = "./www" + rutaArchivo; // ./www/path/file.html
        System.out.println("Ruta" + rutaArchivo);

        // Recuperamos y mostramos por consola las demás líneas de la petición del cliente:
        System.out.println();
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }
        System.out.println();
        //**********************************************************************

        // Compruebo que la ruta al archivo existe:
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(rutaArchivo);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }

        //****************************
        // Construcción de la salida de datos:
        // ***************************
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;

        if (fileExists) { // Si el archivo existe:
            statusLine = "HTTP/1.0 200 OK" + CRLF; //common success message
            contentTypeLine = "Content-type: " + contentType(rutaArchivo) + CRLF; //content info
        } else { // Si NO existe:
            statusLine = "HTTP/1.0 404 Not Found" + CRLF;//common error message
            contentTypeLine = "Content-type: " + "text/html\nDate:" + new Date() + CRLF;//content info
            entityBody = "<html>"
                    + "<head><title>noEncontrado</title></head>"
                    + "<body>"
                    + "<h1>¡ERROR! Página no encontrada</h1>"
                    + "<p>La página que solicitaste no existe en nuestro servidor</p>"
                    + "</body>"
                    + "</html>";
        }

        // Exista o no el archivo se envían los siguientes datos:
        // Envío la statusLine:
        dos.writeBytes(statusLine);
        // Envío el ContentType:
        dos.writeBytes(contentTypeLine);
        // Se envía una línea en blanco para indicar el final de la cabecera:
        dos.writeBytes(CRLF);

        // Y se envía el contenido en función de si el archivo existe o no:
        if (fileExists) {
            sendBytes(fis, dos);
            //dos.writeBytes(statusLine);// Podemos escribir en la página el statusLine. HTTP/1.0 200 OK 
            System.out.println(statusLine); // Sacamos por línea de comandos el statusLine. HTTP/1.0 200 OK 
            //dos.writeBytes(contentTypeLine);// Podemos escribir en la página el contentType. Content-type: text/html;charset=UTF-8 Date:Sat Feb 17 12:44:18 CET 2018 
            System.out.println(contentTypeLine); // Sacamos por línea de comandos el contentType. Content-type: text/html;charset=UTF-8 Date:Sat Feb 17 12:44:18 CET 2018
            fis.close();
        } else {
            dos.writeBytes(entityBody);//Send the an html error message info body.
            System.out.println(statusLine); // Sacamos por línea de comandos el statusLine. HTTP/1.0 404 Not Found 
            System.out.println(contentTypeLine); // Sacamos por línea de comandos el contentType. Content-type: text/html;charset=UTF-8 Date:Sun Feb 18 09:11:51 CET 2018
        }

        // Cerramos la entrada, el buffer y la salida:
        dos.close();
        br.close();
        is.close();
    } // Fin de procesaPeticion()

    /**
     * Devuelve el tipo de contenido del archivo para que pueda ser leído por el
     * navegador.
     *
     * @param rutaArchivo Ruta al archivo
     * @return Retorna el ContentType del archivo para formar la cabecera.
     */
    private String contentType(String rutaArchivo) {
        // Si es un HTML se envía la fecha de solicitud del servidor:
        if (rutaArchivo.endsWith(".htm") || rutaArchivo.endsWith(".html")) {
            return "text/html;charset=UTF-8\nDate:" + new Date();
        }
        if (rutaArchivo.endsWith(".css")) {
            return "text/css";
        }
        if (rutaArchivo.endsWith(".jpg") || rutaArchivo.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (rutaArchivo.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }

    /**
     * Configuro un flujo de salida para poder enviar el archivo al cliente.
     *
     * @param fis Introducimos el archivo
     * @param os Introducimos la salida
     * @throws Exception Errores
     */
    private void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        // Construct a 1K buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;
        // Copy requested file into the socket's output stream.
        while ((bytes = fis.read(buffer)) != -1) { // read() returns minus one, indicating that the end of the file
            os.write(buffer, 0, bytes);
        }
    }
// Fuente utilizada para ver contenido html:
    // http://programwebserver.blogspot.com.es/2011/10/webserverjava.html

} // Fin de la clase ServidorHTTP
