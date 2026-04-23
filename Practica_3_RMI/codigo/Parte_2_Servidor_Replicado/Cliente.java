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

    static void mostrarSeparador(Scanner scanner) {
        System.out.println("\n ----- Presione Enter para continuar -----");
        scanner.nextLine(); // Espera a que el usuario pulse Enter
        for (int i = 0; i < 30; i++) {
            System.out.println(); // Añade muchos saltos de línea para limpiar la pantalla
        }
    }

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
            
            while (opcion != 9) { // El menú se repetirá hasta que el usuario elija salir (opción 9)
                System.out.println("\n\n\n------- MENÚ DE CLIENTE (RMI) --------------------------------");
                System.out.println("Nombre de usuario: " 
                                    + (miNombre != "" ? miNombre : "Ninguno"));
                System.out.println("Réplica actual asignada: " + servidor.getNombre());
                System.out.println("-----------------------------");
                System.out.println("1. Registrarme como donante / Iniciar sesión (si ya estoy registrado)");
                System.out.println("2. Darme de baja como donante");
                System.out.println("3. Realizar donación");
                System.out.println("4. Consultar total de donaciones (de forma global)");
                System.out.println("5. Listar todos los donantes (de forma global)");
                System.out.println("6. Consultar el mayor donante registrado en el sistema");
                System.out.println("7. Consultar el total de mis donaciones");
                System.out.println("8. Consultar mi historial de donaciones");
                System.out.println("9. Salir");
                System.out.println("----------------------------------------------------------------------");
                System.out.print("Elija una opción: ");
                
                try {
                    opcion = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Opción inválida. Introduzca un número (del 1 al 9).");
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
                    case 2: // Dar de baja a un donante
                        if (miNombre.isEmpty()) {
                            System.out.println("[ERROR] Primero debe registrarse (Opción 1).");
                        } else {
                            servidor.darDeBaja(miNombre);
                            System.out.println("[ÉXITO] Dado de baja correctamente.");
                            miNombre = "";
                        }
                        break;

                    case 3: // Realizar donación
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
                        
                    case 4: // Consultar total de donaciones (global)
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
                        
                    case 5: // Listar todos los donantes (global)
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
                    
                    case 6: // Consultar el mayor donante registrado en el sistema
                        System.out.println("[RESULTADO] El mayor donante registrado en el sistema es: " 
                                            + servidor.mayorDonanteGlobal() + " (cantidad donada: "
                                            + servidor.getTotalDonacionesDonante(servidor.mayorDonanteGlobal()) + "€)");
                        break;
                    
                    case 7: // Consultar el total de mis donaciones
                        if (miNombre.isEmpty()) {
                            System.out.println("[ERROR] Primero debe registrarse (Opción 1).");
                        } else {
                            int totalMisDonaciones = servidor.getTotalDonacionesDonante(miNombre);
                            System.out.println("[RESULTADO] El total de sus donaciones es: " + totalMisDonaciones + "€");
                        }
                        break;
                    
                    case 8: // Consultar mi historial de donaciones
                        if (miNombre.isEmpty()) {
                            System.out.println("[ERROR] Primero debe registrarse (Opción 1).");
                        } else {
                            List<Integer> historial = servidor.obtenerHistorialDonaciones(miNombre);
                            if (historial != null && !historial.isEmpty()) {
                                System.out.println("[RESULTADO] Su historial de donaciones:");
                                for (int i = 0; i < historial.size(); i++) {
                                    System.out.println(" Donación " + (i+1) + ": " + historial.get(i) + "€");
                                }
                            } else {
                                System.out.println("[RESULTADO] No ha realizado ninguna donación aún.");
                            }
                        }
                        break;
                        
                    case 9: // Salir
                        System.out.println("Desconectando y cerrando el cliente...");
                        break;
                        
                    default:
                        System.out.println("La opción introducida no es válida. Por favor, seleccione del 1 al 9.");
                }

                if(opcion != 9)
                    mostrarSeparador(scanner);
            }
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("Excepción en el cliente:");
            e.printStackTrace();
        }
    }
}