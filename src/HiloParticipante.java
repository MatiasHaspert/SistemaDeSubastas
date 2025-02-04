import java.io.*;
import java.net.Socket;

public class HiloParticipante implements Runnable{
    private Socket socket;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;
    private DataInputStream dataIn;
    private GestorSubasta gestorSubasta;
    public HiloParticipante(Socket socket, ObjectOutputStream objectOut, ObjectInputStream objectIn,
                            DataInputStream dataIn, GestorSubasta gs){
        this.socket = socket;
        this.objectOut = objectOut;
        this.objectIn = objectIn;
        this.dataIn = dataIn;
        this.gestorSubasta = gs;
        System.out.println("Hilo Participante creado correctamente");
    }

    @Override
    public void run() {
        if(gestorSubasta.isSubastaActiva()){
            gestorSubasta.enviarMensajeIndividual(String.format("Hay una subasta en curso. Puedes participar \n" +
                                    "Subastador: %s\n" +
                                    "Producto: \n%s\n" +
                                    "Tiempo restante: %d segundos\n" +
                                    "%s",
                                    gestorSubasta.getSubasta().getSubastador().getNombre(),
                                    gestorSubasta.getSubasta().getArticulo(),
                                    gestorSubasta.getTiempoRestante(),
                                    gestorSubasta.getSubasta().getOfertaMayor() != null
                                    ? "Mayor oferta actual: $" + gestorSubasta.getSubasta().getOfertaMayor().getMonto()
                                    : "Aun no hay ofertas para el articulo")
                            ,objectOut);
        }
        int opcion;
        boolean salir = false;
        while(!salir){
            try {
                opcion = dataIn.readInt();
                switch (opcion) {
                    case 1:
                        if (!gestorSubasta.isSubastaActiva()) {
                            gestorSubasta.enviarMensajeIndividual("Espera a que haya una subasta activa para realizar una oferta", objectOut);
                        } else {
                            Oferta ofertaCliente = (Oferta) objectIn.readObject();
                            if ((gestorSubasta.getSubasta().getOfertaMayor() == null && ofertaCliente.getMonto() >= gestorSubasta.getSubasta().getArticulo().getPrecioBase()) ||
                                    (gestorSubasta.getSubasta().getOfertaMayor() != null && ofertaCliente.getMonto() > gestorSubasta.getSubasta().getOfertaMayor().getMonto())) {
                                fijarOfertaMayor(ofertaCliente);
                            } else if(gestorSubasta.getSubasta().getOfertaMayor() == null && gestorSubasta.getSubasta().getArticulo().getPrecioBase() > ofertaCliente.getMonto()) {
                                gestorSubasta.enviarMensajeIndividual("Oferta rechazada. La oferta realizada no supera el precio base", objectOut);
                            }else{
                                gestorSubasta.enviarMensajeIndividual("Oferta rechazada. La oferta realizada no supera el monto de la oferta mayor", objectOut);
                            }
                        }
                        break;
                    case 2:
                        salir = true;
                        manejarDesconexionParticipante();
                        System.out.println("El participante se ha desconectado correctamente");
                        break;
                    default:
                        gestorSubasta.enviarMensajeIndividual("Debes ingresar una opción valida", objectOut);
                }
            }catch (IOException e){
                System.err.println("Error en el socket: " + e.getMessage());
                manejarDesconexionParticipante();
                break;
            }catch (ClassNotFoundException e){
                System.err.println("Error al leer el objeto oferta: " + e.getMessage());
            }
        }
    }

    private void manejarDesconexionParticipante() {
        gestorSubasta.eliminarCliente(objectOut);
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar el socket: " + e.getMessage());
        }
    }

    private synchronized void fijarOfertaMayor(Oferta ofertaCliente){
        gestorSubasta.getSubasta().setOfertaMayor(ofertaCliente);
        gestorSubasta.reiniciarTemporizador();
        System.out.println("Actualizacion realizada, nueva oferta mayor: " + gestorSubasta.getSubasta().getOfertaMayor().getMonto() + ".\nOfertante: "+ gestorSubasta.getSubasta().getOfertaMayor().getParticipante().getNombre());
        gestorSubasta.enviarMensajeIndividual("Oferta recibida correctamente. Actualmente tu oferta es la mayor", objectOut);
        gestorSubasta.enviarActualizacionGlobal(MensajeGlobal.NUEVA_OFERTA);
    }
}


