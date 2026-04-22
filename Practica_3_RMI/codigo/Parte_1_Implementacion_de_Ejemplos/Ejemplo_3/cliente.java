// Fichero: cliente.java
// Es el cliente que invoca al objeto remoto contador

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class cliente {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java cliente <host>");
            System.exit(1);
        }

        try {
            // Crea el stub para el cliente especificando el nombre del servidor 
            // y el puerto del registro RMI
            Registry mireg = LocateRegistry.getRegistry(args[0], 1099);

            icontador micontador = (icontador) mireg.lookup("mmicontador");
            
            // Pone el contador al valor inicial 0
            System.out.println("Poniendo contador a 0...");
            micontador.sumar(0);

            // Obtiene hora de comienzo
            long horacomienzo = System.currentTimeMillis();

            // Incrementa 1000 veces
            System.out.println("Incrementando 1000 veces...");
            for (int i = 0; i < 1000; i++) {
                micontador.incrementar();
            }
            // Obtiene hora final, realiza e imprime calculos
            long horafin = System.currentTimeMillis();
            System.out.println("Media de las RMI realizadas = " 
                            + ((horafin - horacomienzo) / 1000f) 
                            + " msegs");
            System.out.println("Valor final del contador = " + micontador.sumar());

        } catch (Exception e) {
            System.err.println("Excepción del sistema: " + e);
        }
        System.exit(0);
    }
}
