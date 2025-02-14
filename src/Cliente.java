import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Cliente {
    private static Usuario usuario;
    private static final String DIRECCION_SERVIDOR = "localhost";
    private static final int PUERTO = 5555;
    private static Socket socket;
    private static boolean subastaActiva;

    public Cliente(){}

    public static void main (String[] args){
        Cliente.definirUsuario();
        Cliente.manejarConexionCliente();
    }

    public static void definirUsuario(){
        Scanner scanner = new Scanner(System.in);
        String nombreUsuario;
        String emailUsuario;
        String rolUsuario;

        System.out.println("Ingrese su nombre: ");
        do{
            nombreUsuario = scanner.nextLine();
            if(nombreUsuario.length() < 3){
                System.out.println("Debes ingresar un nombre de al menos 3 caracteres");
            }
        }while(nombreUsuario.length() < 3);

        System.out.println("Ingrese su email: ");
        do{
            emailUsuario = scanner.nextLine();
            if(emailUsuario.length() < 8 || !emailUsuario.contains("@")){
                System.out.println("Debes ingresar un email valido");
            }
        }while(emailUsuario.length() < 8 || !emailUsuario.contains("@"));


        System.out.println("Desea ser Subastador o Participante? (Ingrese S o P): ");
        boolean rolValido = false;
        do{
            rolUsuario = scanner.nextLine();
            if(rolUsuario.equals("S") || rolUsuario.equals("P")){
                rolValido = true;
            }else{
                System.out.println("Debes ingresar un rol valido. Intente nuevamente");
            }
        }while(!rolValido);

        if (rolUsuario.equals("S")){
            Cliente.setUsuario(new Subastador(nombreUsuario,emailUsuario));
        }else{
            Cliente.setUsuario(new Participante(nombreUsuario,emailUsuario));
        }
    }

    public static void manejarConexionCliente(){

        try {
            Cliente.socket = new Socket(Cliente.DIRECCION_SERVIDOR,Cliente.PUERTO);
            ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
            objectOut.writeObject(Cliente.usuario);
            Scanner scanner = new Scanner(System.in);
            int opcion;

            boolean salir = false;
            System.out.println("===============================================\n" +

                            "      Bienvenido al sistema de subasta OMv2\n" +
                                "===============================================");

          


            Cliente.hiloDeEscucha(objectIn);

            if(Cliente.usuario.getRol() == Rol.SUBASTADOR){
                Subastador subastador = (Subastador)Cliente.usuario;
                System.out.println("Ingrese 1 para iniciar una subasta");
                System.out.println("Ingrese 2 para salir del sistema");
                while(!salir){
                    opcion = scanner.nextInt();
                    dataOut.writeInt(opcion);

                    switch (opcion){
                        case 1:
                            if(!subastaActiva){
                                objectOut.writeObject(new Subasta(subastador.generarArticuloASubastar(), subastador.fijarTiempoSubasta(), (Subastador)Cliente.usuario));
                                subastaActiva = true;
                            }
                            break;
                        case 2:
                            salir = true;
                            desconexion();
                            break;
                    }
                }
            }else{
                Participante participante = (Participante) Cliente.usuario;
                System.out.println("Ingrese 1 para realizar una oferta");
                System.out.println("Ingrese 2 para salir del sistema");
                while(!salir){
                    opcion = scanner.nextInt();
                    dataOut.writeInt(opcion);
                    switch (opcion){
                        case 1:
                            if(subastaActiva){
                                objectOut.writeObject(participante.realizarOferta());
                            }
                            break;
                        case 2:
                            salir = true;
                            desconexion();
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error en la conexion con el servidor: " + e.getMessage());
        }
    }

    public static void hiloDeEscucha(ObjectInputStream objectIn){
        new Thread(()->{
            try{
                while(true){
                    Object mensaje = objectIn.readObject();

                    System.out.println("====================================================================");
                    System.out.println("                 [Notificacion del servidor]\n" + mensaje);
                    System.out.println("====================================================================");
                    System.out.print("[Opcion (1 | 2)]\n");
                    String msg = (String) mensaje;
                    if(msg.contains("Ya hay una subasta activa") || msg.contains("Se ha iniciado una subasta")
                            || msg.contains("Hay una subasta en curso") || msg.contains("Ya hay un subastador")){
                        subastaActiva = true;
                    }
                    else if(msg.contains("La subasta ha finalizado") || msg.contains("Subastador desconectado! La subasta queda cancelada.")){
                        subastaActiva = false;
                    }
                }
            }catch (EOFException |SocketException e) {
                System.exit(0);
            }catch(IOException e){
                System.out.println("Error en el socket " + e.getMessage());
            }catch (ClassNotFoundException e) {
                System.out.println("Error al leer el mensaje del servidor " + e.getMessage());

            }
        }).start();
    }

    public static void desconexion(){
        try {
            if (socket != null) socket.close();
            System.out.println("Te has desconectado correctamente");
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }finally{
            System.exit(0);
        }
    }
    public static void setUsuario(Usuario usuario){Cliente.usuario = usuario;}
}