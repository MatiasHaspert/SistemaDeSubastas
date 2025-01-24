import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;

public class Servidor{
    private int PUERTO;
    private ServerSocket server;
    private GestorSubasta gestorSubasta;

    public Servidor(int puerto){
        this.PUERTO = puerto;
        this.gestorSubasta = new GestorSubasta();
        try {
            this.server = new ServerSocket(this.PUERTO);
            System.out.println("Servidor corriendo en el puerto " + this.PUERTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        Servidor servidorSubasta = new Servidor(5555);
        servidorSubasta.iniciarServidor(servidorSubasta.getServerSocket(), servidorSubasta.getGestorSubasta());
    }

    private void iniciarServidor(ServerSocket ss, GestorSubasta gestorSubasta){
        try{
            while(true){
                Socket socket = ss.accept();
                ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                Usuario usuario = (Usuario)objectIn.readObject();
                if(usuario.getRol() == Rol.SUBASTADOR){
                    if(gestorSubasta.isSubastadorConectado()){
                        System.out.println("Ya hay un subastador. No se permite otro subastador.");
                        gestorSubasta.enviarMensajeIndividual("Ya hay un subastador. No puedes conectarte como subastador en este momento. Intentalo mas tarde", objectOut);
                        socket.close();
                    }else{
                        System.out.println("Subastador conectado: " + usuario.getNombre());
                        gestorSubasta.enviarMensajeIndividual("Te has conectado correctamente al servidor como subastador", objectOut);
                        gestorSubasta.setSubastadorConectado(true);
                        gestorSubasta.manejarConexionSubastador(new HiloSubastador(socket,objectOut,objectIn,dataOut,dataIn,gestorSubasta));
                    }
                }else{
                    System.out.println("Participante conectado: " + usuario.getNombre());
                    gestorSubasta.enviarMensajeIndividual("Te has conectado correctamente al servidor como participante", objectOut);
                    gestorSubasta.manejarConexionParticipante(new HiloParticipante(socket,objectOut,objectIn,dataOut,dataIn, gestorSubasta));
                }
                gestorSubasta.agregarCliente(objectOut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServerSocket getServerSocket() {
        return this.server;
    }

    public GestorSubasta getGestorSubasta() {
        return gestorSubasta;
    }
}