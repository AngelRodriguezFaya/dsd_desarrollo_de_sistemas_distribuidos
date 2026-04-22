// Fichero: servidor.java
// Es el servidor que publica el objeto remoto contador

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class servidor {
    public static void main(String[] args) {
        try {
            int puerto = 1099;
            contador micontador = new contador();

            // Configuramos las factorías SSL
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();

            // Creamos el stub del objeto remoto para el cliente, exportándolo 
            // al puerto 0 (puerto anónimo) y usando las factorías SSL
            icontador stub = (icontador) UnicastRemoteObject.exportObject(micontador, 0, csf, ssf);

            // Creamos el registro en el puerto 1099
            Registry registry = LocateRegistry.createRegistry(puerto);
            registry.rebind("mmicontador", stub);

            System.out.println("Servidor contador preparado con SSL en puerto " + puerto);
        } catch (Exception e) {
            System.err.println("Excepción en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}