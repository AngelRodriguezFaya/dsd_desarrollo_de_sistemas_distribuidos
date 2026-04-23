/**
 * @file Servidor.java
 * @brief Código del servidor que implementa la lógica de negocio y publica el objeto remoto en
 * el RMI Registry. Este servidor es replicado, lo que significa que pueden lanzarse varias
 * instancias de este servidor, cada una con un nombre único, y se conocen entre ellas para 
 * replicarse mutuamente. 
 */
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class Servidor {

    private static final int RMI_PORT = 2099;

    /**
     * @brief Genera un nombre único para cada servidor que se lance, basado en el 
     * número de servidores que ya están registrados en el RMI Registry. Así, los 
     * servidores se llamarán Donacion_1, Donacion_2, etc. 
     * @param registry El RMI Registry donde se van a registrar los servidores. 
     * @return String. El nombre único generado para el nuevo servidor.
     */
	private static String generateName(Registry registry) throws RemoteException {
		int n = 0;
        // Con registry.list() obtenemos los nombres de los objetos 
        // registrados en el RMI Registry y miramos cuántos de ellos 
        // son de tipo "Donacion_" para asignar un nombre incremental 
        // a cada nuevo objeto que se lance. Así es como se llamarán 
        // nuestros servidores: Donacion_1, Donacion_2, ...
		for(String name : registry.list()){
				if(name.contains("Donacion_")){
					n++;
				}
		}
		return "Donacion_" + (n+1);
	}
    
    /**
     * @brief Método principal del servidor. Crea el RMI Registry si no existe, 
     * genera un nombre único para el servidor, crea una instancia del objeto 
     * remoto Donacion, la exporta y la registra en el RMI Registry. Luego, 
     * busca otros servidores registrados para conocerse entre ellos y replicarse. 
     * @param args Argumentos de línea de comandos (no se utilizan en este caso).
     */
    public static void main(String[] args) {

        // Creamos las fábricas de sockets SSL para el cliente y el servidor
        SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
        SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();

        // Creamos el registry en este mismo proceso si no existe.
        try {
            LocateRegistry.createRegistry(RMI_PORT, csf, ssf);
            System.out.println("RMI Registry creado en el puerto " + RMI_PORT);
        } catch (ExportException e) {
            System.out.println("Un RMI Registry ya estaba activo en el puerto " 
                            + RMI_PORT);
        } catch (RemoteException e) {
            System.out.println("No se pudo iniciar el RMI Registry en el puerto " 
                            + RMI_PORT + ": " + e.getMessage());
            return;
        }

        try {
            // Obtenemos el registro (si el registry va con SSL, usa csf aquí también)
            Registry registry = LocateRegistry.getRegistry("localhost", RMI_PORT, csf);

            // Determinar dinámicamente el nombre incremental
            String ob_name = generateName(registry);

            // Creamos el objeto
            Donacion gestor = new Donacion(ob_name);

            // Generamos un stub y lo publicamos
            iDonacion stub0 = (iDonacion) UnicastRemoteObject.exportObject(gestor, 0, csf, ssf);
            registry.rebind(ob_name, stub0);

			// registry.lookup() devuelve un java.rmi.Remote genérico, hay que castearlo explícitamente a la interfaz deseada
			iDonacion obLocal = (iDonacion) registry.lookup(ob_name);
			for(String name : registry.list()){
				if(name.contains("Donacion_") && !name.equals(ob_name)){
					iDonacion obRemoto = (iDonacion) registry.lookup(name);
					// setPeer sirve para que cada servidor conozca a los demás servidores 
                    // y puedan replicarse entre ellos.
					obLocal.setPeer(obRemoto);
					obRemoto.setPeer(obLocal);	
				}
			}

            System.out.println("Servidor preparado. Lanzado el objeto: " + ob_name);

        } catch (RemoteException e) {
            System.out.println("No se pudieron registrar los objetos remotos: " + e.getMessage());
        } catch (java.rmi.NotBoundException e) {
            System.out.println("No se encontró el objeto remoto en el registro: " + e.getMessage());
        }
    }
}