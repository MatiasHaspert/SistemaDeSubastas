import java.io.*;
import java.net.Socket;

public class HiloSubastador implements Runnable{
    private Socket socket;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;
    private DataInputStream dataIn;
    private GestorSubasta gestorSubasta;

    public HiloSubastador(Socket socket, ObjectOutputStream objectOut, ObjectInputStream objectIn,
                          DataInputStream dataIn, GestorSubasta gs){
        this.socket = socket;
        this.objectOut = objectOut;
        this.objectIn = objectIn;
        this.dataIn = dataIn;
        this.gestorSubasta = gs;
        System.out.println("Hilo Subastador creado correctamente");
    }

    @Override
    public void run() {
        int opcion;
        boolean salir = false;
        while(!salir){
            try{
                opcion = dataIn.readInt();
                switch (opcion){
                    case 1:
                        if(gestorSubasta.isSubastaActiva()){
                            gestorSubasta.enviarMensajeIndividual("Ya hay una subasta en curso, debes esperar a que finalice", objectOut);
                        }else{
                            Subasta subasta =(Subasta)objectIn.readObject();
                            gestorSubasta.setSubasta(subasta);
                            gestorSubasta.setSubastaActiva(true);
                            gestorSubasta.iniciarTemporizador();
                            System.out.println("Subasta iniciada correctamente");
                            gestorSubasta.enviarMensajeIndividual("Subasta iniciada correctamente", objectOut);
                            gestorSubasta.enviarActualizacionGlobal(MensajeGlobal.INICIO_SUBASTA);
                        }
                        break;
                    case 2:
                        salir = true;
                        if(gestorSubasta.isSubastaActiva()){
                            gestorSubasta.finalizarSubasta(MensajeGlobal.SUBASTADOR_DESCONECTADO);
                            manejarDesconexionSubastador();
                        }else{
                            gestorSubasta.setSubastadorConectado(false);
                            manejarDesconexionSubastador();
                        }
                        System.out.println("El subastador se ha desconectado correctamente.");
                        break;
                    default:
                        gestorSubasta.enviarMensajeIndividual("Debes ingresar una opcion valida", objectOut);
                }
            }catch (IOException e){
                System.err.println("Error en el socket: " + e.getMessage());
                manejarDesconexionSubastador();
                break;
            }catch (ClassNotFoundException e){
                System.err.println("Error al leer el objeto subasta: " + e.getMessage());
            }
        }
    }

    private void manejarDesconexionSubastador() {
        gestorSubasta.eliminarCliente(objectOut);
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar el socket: " + e.getMessage());
        }
    }
}


