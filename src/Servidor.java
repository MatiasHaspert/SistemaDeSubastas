import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor{
    private static final int PUERTO = 5555;
    private static final GestorSubasta gestorSubasta = new GestorSubasta();
    private static ServerSocket server;

    public static void main(String[] args){
        Servidor.iniciarServidor();
    }

    private static void iniciarServidor(){
        try{
            server = new ServerSocket(Servidor.PUERTO);
            System.out.println("Servidor corriendo en el puerto " + Servidor.PUERTO + ".");
            while(true){
                try {
                    Socket socket = server.accept();
                    Servidor.establecerConexionInicial(socket);
                } catch (IOException e) {
                    System.err.println("Error al aceptar la conexión de un cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }


    private static void establecerConexionInicial(Socket socket){
        try{
            ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            Usuario usuario = (Usuario) objectIn.readObject();
            if(usuario.getRol() == Rol.SUBASTADOR){
                gestionarSubastador(socket,usuario, objectOut, objectIn, dataIn);
            }else{
                gestionarParticipante(socket,usuario,objectOut,objectIn,dataIn);
            }
        }catch(IOException e){
            System.err.println("Error al establecer la conexion inicial con el cliente: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Error al leer el objeto Usuario: " + e.getMessage());
        }
    }

    private static void gestionarSubastador(Socket socket, Usuario usuario, ObjectOutputStream objOut,
                                            ObjectInputStream objIn, DataInputStream dataIn) throws IOException{
        if(gestorSubasta.isSubastadorConectado()){
            System.out.println("Fallo al ingresar subastador. Ya hay un subastador conectado.");
            gestorSubasta.enviarMensajeIndividual("Ya hay un subastador. No puedes conectarte como subastador en este momento. Intentalo mas tarde",objOut);
            socket.close();
        }else{
            System.out.println("Subastador conectado.\nNombre: " + usuario.getNombre() + "\nEmail: " + usuario.getEmail());
            gestorSubasta.enviarMensajeIndividual("Te has conectado correctamente al servidor como subastador",objOut);
            gestorSubasta.setSubastadorConectado(true);
            gestorSubasta.agregarCliente(objOut);
            gestorSubasta.manejarConexionSubastador(new HiloSubastador(socket,objOut,objIn,dataIn,gestorSubasta));
        }
    }

    private static void gestionarParticipante(Socket socket, Usuario usuario, ObjectOutputStream objOut,
                                              ObjectInputStream objIn, DataInputStream dataIn) throws IOException{
        System.out.println("Participante conectado.\nNombre: " + usuario.getNombre() + "\nEmail: " + usuario.getEmail());
        gestorSubasta.enviarMensajeIndividual("Te has conectado correctamente al servidor como participante",objOut);
        gestorSubasta.agregarCliente(objOut);
        gestorSubasta.manejarConexionParticipante(new HiloParticipante(socket,objOut,objIn,dataIn,gestorSubasta));


    }

    public GestorSubasta getGestorSubasta() {
        return gestorSubasta;
    }
}