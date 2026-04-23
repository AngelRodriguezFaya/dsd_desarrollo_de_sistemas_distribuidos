/**
 * @file Cliente.java
 * @brief Cliente RMI para el sistema de donaciones replicado.
 */

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import javax.rmi.ssl.SslRMIClientSocketFactory;

public class Cliente {

    public static void main(String[] args) {
        
        // Host y puerto donde se encuentra el RMI Registry
        String host = "localhost";
        int puerto = 2099;
        
        // Nombre del servidor al que nos conectaremos inicialmente (punto de entrada)
        // Asumimos que al menos existe una réplica llamada "Donacion_1"
        String nombreServidor = "Donacion_1"; 
        
        try {
            // Como el servidor usa SSL, el cliente también debe usar la fábrica SSL
            SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
            
            // Obtenemos el RMI Registry
            Registry registry = LocateRegistry.getRegistry(host, puerto, csf);
            
            // Buscamos el objeto remoto (servidor) inicial
            iDonacion servidor = (iDonacion) registry.lookup(nombreServidor);
            System.out.println("Se ha establecido la conexión con el servidor inicial: " + servidor.getNombre());
            
            Scanner scanner = new Scanner(System.in);
            int opcion = 0; // Opción seleccionada del menú
            String miNombre = ""; // Guardará el nombre del donante activo en esta sesión
            
            while (opcion != 5) {
                System.out.println("\n\n\n--- MENÚ DE CLIENTE (RMI) ---");
                System.out.println("Réplica actual asignada: " + servidor.getNombre());
                System.out.println("Usuario activo: " + (miNombre != "" ? miNombre : "Ninguno"));
                System.out.println("-----------------------------");
                System.out.println("1. Registrar donante");
                System.out.println("2. Realizar donación");
                System.out.println("3. Consultar total de donaciones (de forma global)");
                System.out.println("4. Listar todos los donantes (de forma global)");
                System.out.println("5. Salir");
                System.out.print("Elija una opción: ");
                
                try {
                    opcion = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Opción inválida. Introduzca un número (del 1 al 5).");
                    continue;
                }
                
                switch (opcion) {
                    case 1: // Registrar donante
                        System.out.print("Introduzca el nombre del donante a registrar: ");
                        miNombre = scanner.nextLine();
                        // Al llamar a registrar, el servidor puede devolvernos la referencia 
                        // de otra réplica (balanceo de carga). Es pro eso que debemos actualizar
                        // nuestra referencia.
                        servidor = servidor.registrar(miNombre);
                        System.out.println("[ÉXITO] Registrado correctamente. Asignado a la réplica: " 
                                            + servidor.getNombre());
                        break;
                        
                    case 2: // Realizar donación
                        if (miNombre.isEmpty()) {
                            System.out.println("[ERROR] Primero debe registrarse (Opción 1).");
                        } else {
                            System.out.print("Introduzca la cantidad a donar (en €): ");
                            try {
                                int cantidad = Integer.parseInt(scanner.nextLine());
                                if(cantidad <= 0) {
                                    System.out.println("[ERROR] La cantidad debe ser mayor que 0.");
                                } else {
                                    // Si ha introducido una cantidad válida, realizamos la donación
                                    servidor.donar(miNombre, cantidad);
                                    System.out.println("[ÉXITO] Donación de " + cantidad 
                                                        + "€ realizada correctamente.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("[ERROR] La cantidad introducida no es válida.");
                            }
                        }
                        break;
                        
                    case 3: // Consultar total de donaciones (global)
                        if (miNombre.isEmpty()) {
                            System.out.println("[ERROR] Primero debe registrarse (Opción 1).");
                        } else {
                            int total = servidor.totalDonaciones(miNombre);
                            if (total != -1) { 
                                System.out.println("[RESULTADO] El total donado en todo el sistema es: " 
                                                    + total + "€");
                            } else { // Si el resultado es -1, significa que no se cumplen las precondiciones para consultar
                                System.out.println("[DENEGADO] No puede consultar. Compruebe que ha donado más de 0€.");
                            }
                        }
                        break;
                        
                    case 4:
                        if (miNombre.isEmpty()) {
                            System.out.println("[ERROR] Primero debe registrarse (Opción 1).");
                        } else {
                            List<String> listado = servidor.listarDonantes(miNombre);
                            if (listado != null && !listado.isEmpty()) { 
                                System.out.println("[RESULTADO] Lista global de donantes registrados:");
                                for (String d : listado) {
                                    System.out.println(" - " + d);
                                }
                            } else {
                                System.out.println("[DENEGADO] No puede listar donantes. Compruebe que ha donado más de 0€.");
                            }
                        }
                        break;
                        
                    case 5:
                        System.out.println("Desconectando y cerrando el cliente...");
                        break;
                        
                    default:
                        System.out.println("La opción introducida no es válida. Por favor, seleccione del 1 al 5.");
                }
            }
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("Excepción en el cliente:");
            e.printStackTrace();
        }
    }
}