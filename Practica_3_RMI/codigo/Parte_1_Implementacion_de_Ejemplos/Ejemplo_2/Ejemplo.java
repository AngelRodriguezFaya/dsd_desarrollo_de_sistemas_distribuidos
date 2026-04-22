// Ejemplo.java
// Implementa la Interfaz remota

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class Ejemplo implements Ejemplo_I {
    private static final int RMI_PORT = 2099;

    public Ejemplo() {
        super();
    }

    public synchronized void escribir_mensaje(String mensaje) {
        System.out.println("\nEntra Hebra " + mensaje);
        //Buscamos los procesos 0, 10, 20,...
        if (mensaje.endsWith("0")) {
            try {
                System.out.println("Empezamos a dormir");
                Thread.sleep(5000);
                System.out.println("Terminamos de dormir");
            } catch (Exception e) {
                System.err.println("Ejemplo exception:");
                e.printStackTrace();
            }
        }
        System.out.println("Sale Hebra " + mensaje);
    }

    public static void main(String[] args) {
        try {
            // Definimos el nombre del objeto remoto que se registrará en el ligador
            String nombre_objeto_remoto = "Ejemplo_I"; 

            // Creamos una instancia del objeto remoto
            Ejemplo_I prueba = new Ejemplo();

            // Creamos los socket factories para SSL
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();

            // Creamos el stub del objeto remoto para el cliente, exportandolo al 
            // puerto 0 (puerto anonimo) y usando los socket factories creados.
            Ejemplo_I stub =
                (Ejemplo_I) UnicastRemoteObject.exportObject(prueba, 0, csf, ssf);

            // El Registry vive como proceso externo (rmiregistry).
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            // Registramos el objeto remoto en el ligador
            registry.rebind(nombre_objeto_remoto, stub);
            
            // A partir de aquí, el objeto remoto ya puede ser invocado por los clientes.
            
            System.out.println("Ejemplo bound");
        }
        catch (Exception e) {
            System.err.println("Ejemplo exception:");
            e.printStackTrace();
        }
    }
}