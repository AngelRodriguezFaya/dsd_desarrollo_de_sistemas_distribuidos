// Cliente_Ejemplo.java
// Código del cliente que invoca al objeto remoto implementado en Ejemplo.java

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Cliente_Ejemplo {
    private static final int RMI_PORT = 2099;

    public static void main(String args[]) {
        try {
            String nombre_objeto_remoto = "Ejemplo_I";
            System.out.println("Buscando el objeto remoto");
            Registry registry = LocateRegistry.getRegistry(args[0], RMI_PORT);
            // Invocamos el objeto remoto obtenido del ligador 
            Ejemplo_I instancia_local = (Ejemplo_I) registry.lookup(nombre_objeto_remoto);
            System.out.println("Invocando el objeto remoto");
            // Llamamos al método remoto, pasándole 
            // el id del proceso (recibido como argumento)
            instancia_local.escribir_mensaje(Integer.parseInt(args[1]));
        } catch (Exception e) {
            System.err.println("Ejemplo_I exception:");
            e.printStackTrace();
        }
    }
}