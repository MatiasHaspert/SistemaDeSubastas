import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GestorSubasta {

    private Subasta subasta;
    private boolean subastaActiva;
    private boolean subastadorConectado;
    private int tiempoRestante;
    private Timer temporizador;
    private ArrayList<ObjectOutputStream> clientesConectados;
    private String carpetaSubastas = "RegistroSubastas";

    public GestorSubasta(){
        this.subasta = new Subasta();
        this.subastaActiva = false;
        this.subastadorConectado = false;
        this.tiempoRestante = 0;
        this.temporizador = new Timer();
        this.clientesConectados = new ArrayList<>();
    }

    public void enviarMensajeIndividual(String mensaje, ObjectOutputStream objOut){
        try {
            objOut.writeObject(mensaje);
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje individual al cliente: " + e.getMessage());
        }
    }

    public void enviarActualizacionGlobal(MensajeGlobal eventoSubasta) {
        String mensaje  = null;
        switch (eventoSubasta) {
            case INICIO_SUBASTA:
                mensaje = String.format("Se ha iniciado una subasta. \n" +
                        "Subastador: %s\n" +
                        "Producto a subastar: \n%s\n" +
                        "Duracion de la subasta: %d", subasta.getSubastador().getNombre(), subasta.getArticulo(), subasta.getTiempo());
                break;
            case FIN_SUBASTA:
                if (subasta.getOfertaMayor() == null) {
                    mensaje = "La subasta ha finalizado sin ofertas para el siguiente articulo: \n" + subasta.getArticulo();
                } else {
                    mensaje = String.format("La subasta ha finalizado.\n" +
                            "Ganador: %s\n" +
                            "Monto final: $%.2f", subasta.getOfertaMayor().getParticipante().getNombre(), subasta.getOfertaMayor().getMonto());
                    almacenarSubasta();
                }
                break;
            case NUEVA_OFERTA:
                mensaje = String.format("Se ha registrado una nueva oferta mayor \n" +
                        "Ofertante: %s\n" +
                        "Monto: $%.2f", subasta.getOfertaMayor().getParticipante().getNombre(), subasta.getOfertaMayor().getMonto());
                break;
            case AVISO_TIEMPO:
                mensaje = "Quedan 10 segundos para que finalice la subasta!";
                break;
            case SUBASTADOR_DESCONECTADO:
                mensaje = "Subastador desconectado! La subasta queda cancelada.";
                break;
        }

        if (!clientesConectados.isEmpty()) {
            for (ObjectOutputStream objOut : clientesConectados) {
                try {
                    objOut.writeObject(mensaje);
                    objOut.flush();
                } catch (IOException e) {
                    System.err.println("Error al enviar mensaje global a los clientes: " + e.getMessage());
                }
            }
        }
    }

    public void almacenarSubasta(){
        LocalDateTime fecha = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaFormateada = fecha.format(formato);

        // Crear carpeta si no existe
        File carpeta = new File(carpetaSubastas);
        if (!carpeta.exists()) {
            if (carpeta.mkdir()) {
                System.out.println("Carpeta creada: " + carpetaSubastas);
            } else {
                System.out.println("Error al crear la carpeta.");
                return;
            }
        }

        String nombreArchivo = fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".txt";
        File archivo = new File(carpeta, nombreArchivo);

        try (FileWriter escritor = new FileWriter(archivo)) {
            String texto = "Subasta del día: " + fechaFormateada +
                    "\nSubastador: \n" + subasta.getSubastador().toString() +
                    "Artículo: \n" + subasta.getArticulo().toString() +
                    "\nGanador de la subasta: \nParticipante:\n" + subasta.getOfertaMayor().getParticipante().toString() +
                    "\nOferta mayor: " + subasta.getOfertaMayor().getMonto() + "\n";
            escritor.write(texto);
            System.out.println("Subasta almacenada en: " + archivo.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ocurrió un error al escribir en el archivo: " + e.getMessage());
        }
    }

    public void finalizarSubasta(MensajeGlobal eventoFinSubasta){
        finalizarTemporizador();
        subastaActiva = false;
        enviarActualizacionGlobal(MensajeGlobal.FIN_SUBASTA);
        if(eventoFinSubasta == MensajeGlobal.FIN_SUBASTA){
            System.out.println("Subasta finalizada por tiempo cumplido");
        }else{
            System.out.println("Subasta finalizada por desconexión del subastador");
            subastadorConectado = false;
        }

    }

    public void iniciarTemporizador(){
        temporizador = new Timer();
        tiempoRestante = subasta.getTiempo();
        temporizador.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(tiempoRestante == 10){
                    System.out.println("Quedan 10 segundos para que finalice la subasta actual");
                    enviarActualizacionGlobal(MensajeGlobal.AVISO_TIEMPO);
                }
                if(tiempoRestante == 0){
                    finalizarSubasta(MensajeGlobal.FIN_SUBASTA);
                }else{
                    tiempoRestante--;
                }
            }
        },0,1000);
    }

    public void finalizarTemporizador(){
        temporizador.cancel();
    }

    public void reiniciarTemporizador(){
        finalizarTemporizador();
        setTiempoRestante(getSubasta().getTiempo());
        iniciarTemporizador();
    }

    public void manejarConexionSubastador(HiloSubastador hiloSubastador){
        new Thread(hiloSubastador).start();
    }

    public void manejarConexionParticipante(HiloParticipante hiloParticipante) {
        new Thread(hiloParticipante).start();
    }

    public void agregarCliente(ObjectOutputStream obj){
        clientesConectados.add(obj);
    }

    public void eliminarCliente(ObjectOutputStream obj){
        clientesConectados.remove(obj);
    }

    public boolean isSubastadorConectado(){
        return this.subastadorConectado;
    }

    public void setSubastadorConectado(boolean subastadorConectado){
        this.subastadorConectado = subastadorConectado;
    }

    public boolean isSubastaActiva() {
        return subastaActiva;
    }

    public void setSubastaActiva(boolean subastaActiva) {
        this.subastaActiva = subastaActiva;
    }

    public Subasta getSubasta() {
        return this.subasta;
    }

    public void setSubasta(Subasta subasta) {
        this.subasta = subasta;
    }

    public int getTiempoRestante(){
        return this.tiempoRestante;
    }

    public void setTiempoRestante(int tiempoRestante){
        this.tiempoRestante = tiempoRestante;
    }

}
