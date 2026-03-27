/**
 * servidor.java
 * Servidor de la calculadora remota implementada con Apache Thrift
 * 
 * Autor: Ángel Rodríguez Faya
 * Fecha: 25-03-2026
 */

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

// Importamos las clases que Thrift ha generado 
import calculadora.*; 

import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

public class servidor {

    // Creamos la clase ESTÁTICA handler que implementa la interfaz de la calculadora
    public static class CalculadoraHandler implements Calculadora.Iface {

        private double realizarOperacion(double op1, String operador, double op2) throws org.apache.thrift.TException {
            double resultado = 0.0;

            switch (operador) {
                case "+":
                    System.out.println("Realizando suma: " + op1 + " + " + op2);
                    resultado = op1 + op2;
                    break;
                
                case "-":
                    System.out.println("Realizando resta: " + op1 + " - " + op2);
                    resultado = op1 - op2;
                    break;

                case "x":
                    System.out.println("Realizando multiplicación: " + op1 + " * " + op2);
                    resultado = op1 * op2;
                    break;

                case "/":
                    System.out.println("Realizando división: " + op1 + " / " + op2);
                    if (op2 == 0) {
                        System.out.println("\t ¡ERROR! No se puede dividir entre 0.");
                        throw new CalculadoraValueError("No se puede dividir por cero");
                    }
                    resultado = op1 / op2;
                    break;

                case "^":
                    System.out.println("Realizando potencia: " + op1 + " ^ " + op2);
                    resultado = Math.pow(op1, op2);
                    break;

                case "sqrt":
                    System.out.println("Realizando raíz cuadrada: sqrt(" + op1 + ")");
                    if (op1 < 0) {
                        System.out.println("\t ¡ERROR! No se puede calcular la raíz cuadrada de un número negativo.");
                        throw new CalculadoraValueError("No se puede calcular la raíz cuadrada de un número negativo");
                    }
                    resultado = Math.sqrt(op1);
                    break;

                case "log10":
                    System.out.println("Realizando logaritmo base 10: log10(" + op1 + ")");
                    if (op1 <= 0) {
                        System.err.println("\t ¡ERROR! No se puede calcular el logaritmo de un número no positivo.");
                        throw new CalculadoraValueError("No se puede calcular el logaritmo de un número no positivo");
                    }
                    resultado = Math.log10(op1);
                    break;

                case "ln":
                    System.out.println("Realizando logaritmo natural: ln(" + op1 + ")");
                    if (op1 <= 0) {
                        System.err.println("\t ¡ERROR! No se puede calcular el logaritmo de un número no positivo.");
                        throw new CalculadoraValueError("No se puede calcular el logaritmo de un número no positivo");
                    }
                    resultado = Math.log(op1);
                    break;
                
                case "sin":
                    System.out.println("Realizando seno de " + op1 + " grados");
                    resultado = Math.sin(Math.toRadians(op1));
                    break;
                
                case "cos":
                    System.out.println("Realizando coseno de " + op1 + " grados");
                    resultado = Math.cos(Math.toRadians(op1));
                    break;
                
                case "tan":
                    System.out.println("Realizando tangente de " + op1 + " grados");
                    resultado = Math.tan(Math.toRadians(op1));
                    break;
                
                case "aCua":
                    System.out.println("Realizando área de un cuadrado con lado " + op1 + " metros");
                    resultado = op1 * op1;
                    break;

                case "aTri":
                    System.out.println("Realizando área de un triángulo con base " + op1
                                        + " y altura " + op2 + " metros");
                    resultado = (op1 * op2) / 2;
                    break;
                
                case "aCir":
                    System.out.println("Realizando área de un círculo con radio " + op1 + " metros");
                    resultado = Math.PI * op1 * op1;
                    break;
                
                default:
                    System.err.println("\t ¡ERROR! Operador no reconocido: " + operador);
                    throw new CalculadoraValueError("Operador no reconocido: " + operador);
        
            }
            return resultado;
        }

        // Método auxiliar para imprimir la matriz por consola
        private void mostrarMatriz(List<List<Integer>> matriz) {
            
            // Recorremos cada fila de la matriz
            for (List<Integer> fila : matriz) {
                
                // Empezamos a imprimir la fila 
                // Usamos print() en lugar de println() para no saltar de línea todavía
                System.out.print("\t[");
                
                for (int i = 0; i < fila.size(); i++) {
                    // El "%4d" le dice a Java: "Reserva 4 espacios para este número entero (d)"
                    System.out.print(String.format("%4d", fila.get(i)));
                    
                    // Añadimos un espacio entre los números
                    if (i < fila.size() - 1) {
                        System.out.print(" ");
                    }
                }
                
                // Cerramos el corchete y saltamos de línea con println()
                System.out.println(" ]");
            }
        }
                    
        @Override
        public void ping() throws org.apache.thrift.TException {
            System.out.println("Recibido ping()");
        }

        @Override
        public double suma(double num1, double num2) throws org.apache.thrift.TException {
            return this.realizarOperacion(num1, "+", num2);
        }

        @Override
        public double resta(double num1, double num2) throws org.apache.thrift.TException{
            return this.realizarOperacion(num1, "-", num2);
        }

        @Override
        public double multiplicacion(double num1, double num2) throws org.apache.thrift.TException{
            return this.realizarOperacion(num1, "x", num2);
        }

        @Override
        public double division(double num1, double num2) throws CalculadoraValueError, org.apache.thrift.TException{
            return this.realizarOperacion(num1, "/", num2);
        }

        @Override
        public double potencia(double base, double exponente) throws org.apache.thrift.TException {
            return this.realizarOperacion(base, "^", exponente);
        }

        @Override
        public double raizCuadrada(double numero) throws CalculadoraValueError, org.apache.thrift.TException {
            return this.realizarOperacion(numero, "sqrt", 0);
        }

        @Override
        public double logBase10(double numero) throws CalculadoraValueError, org.apache.thrift.TException {
            return this.realizarOperacion(numero, "log10", 0);
        }

        @Override
        public double logNatural(double numero) throws CalculadoraValueError, org.apache.thrift.TException {
            return this.realizarOperacion(numero, "ln", 0);
        }

        @Override
        public double seno(double angulo) throws org.apache.thrift.TException {
            return this.realizarOperacion(angulo, "sin", 0);
        }

        @Override
        public double coseno(double angulo) throws org.apache.thrift.TException {
            return this.realizarOperacion(angulo, "cos", 0);
        }

        @Override
        public double tangente(double angulo) throws org.apache.thrift.TException {
            return this.realizarOperacion(angulo, "tan", 0);
        }

        @Override
        public double areaCuadrado(double lado) throws org.apache.thrift.TException {
            return this.realizarOperacion(lado, "aCua", 0);
        }

        @Override
        public double areaTriangulo(double base, double altura) throws org.apache.thrift.TException {
            return this.realizarOperacion(base, "aTri", altura);
        }

        @Override
        public double areaCirculo(double radio) throws org.apache.thrift.TException {
            return this.realizarOperacion(radio, "aCir", 0);
        }

        @Override
        public List<List<Integer>> sumaMatrices(List<List<Integer>> m1, List<List<Integer>> m2) 
            throws CalculadoraValueError, org.apache.thrift.TException
        {
            if (m1.size() != m2.size() || m1.get(0).size() != m2.get(0).size()) {
                throw new CalculadoraValueError("Las matrices deben tener las mismas dimensiones para ser sumadas");
            }

            // Inicializamos la matriz resultado
            List<List<Integer>> resultado = new ArrayList<>();

            // Recorremos las filas
            for (int i = 0; i < m1.size(); i++) {
                // Creamos una nueva lista para la fila actual
                List<Integer> filaResultado = new ArrayList<>();
                
                // Recorremos las columnas
                for (int j = 0; j < m1.get(0).size(); j++) {
                    // Sumamos los elementos de ambas matrices
                    int suma = m1.get(i).get(j) + m2.get(i).get(j);
                    
                    // Añadimos el valor a la fila
                    filaResultado.add(suma);
                }
                
                // Una vez terminada la fila, la añadimos a la matriz resultado
                resultado.add(filaResultado);
            }

            // Antes de devolver el resultado, imprimimos las matrices y el resultado en la consola
            System.out.println("Me han pedido que sume dos matrices");
            
            System.out.println(" --- MATRIZ 1 ---");
            this.mostrarMatriz(m1);
            
            System.out.println(" --- MATRIZ 2 ---");
            this.mostrarMatriz(m2);
            
            System.out.println(" --- RESULTADO (MATRIZ SUMA) ---");
            this.mostrarMatriz(resultado);

            return resultado;
        }

        @Override
        public List<List<Integer>> multiplicaMatrices(List<List<Integer>> m1, List<List<Integer>> m2) 
            throws CalculadoraValueError, org.apache.thrift.TException
        {
            
            if (m1.get(0).size() != m2.size()) {
                throw new CalculadoraValueError("El número de columnas de la primera matriz debe ser igual al número de filas de la segunda matriz.");
            }

            // Inicializamos la matriz resultado
            List<List<Integer>> resultado = new ArrayList<>();

            for (int i = 0; i < m1.size(); i++) {
                // Creamos la fila actual del resultado
                List<Integer> filaResultado = new ArrayList<>();
                
                for (int j = 0; j < m2.get(0).size(); j++) {
                    int suma = 0; 
                    
                    for (int k = 0; k < m2.size(); k++) {
                        suma += m1.get(i).get(k) * m2.get(k).get(j);
                    }
                    
                    // Cuando terminamos de multiplicar la fila por la columna, guardamos el resultado
                    filaResultado.add(suma);
                }
                
                // Añadimos la fila completa a la matriz final
                resultado.add(filaResultado);
            }

            // Antes de devolver el resultado, imprimimos las matrices y el resultado en la consola
            System.out.println("Me han pedido que multiplique dos matrices");
            
            System.out.println(" --- MATRIZ 1 ---");
            this.mostrarMatriz(m1);
            
            System.out.println(" --- MATRIZ 2 ---");
            this.mostrarMatriz(m2);
            
            System.out.println(" --- RESULTADO (MATRIZ MULTIPLICACIÓN) ---");
            this.mostrarMatriz(resultado);

            return resultado;
        }

        @Override
        public List<Double> procesaLoteOperaciones(List<Operacion> operaciones) throws CalculadoraValueError, org.apache.thrift.TException{

            List<Double> resultados = new ArrayList<>();

            for (Operacion operacion : operaciones) {
                double resultado = 0.0;
                String ope = operacion.operador;


                if (ope.equals("sqrt") || ope.equals("log10") || ope.equals("ln") || ope.equals("sin") 
                    || ope.equals("cos") || ope.equals("tan") || ope.equals("aCua") || ope.equals("aCir")) {
                    System.out.println("Procesando operación: " + ope + " (" + operacion.num1 + ")");
                    resultado = this.realizarOperacion(operacion.num1, ope, 0);
                } else {
                    System.out.println("Procesando operación: " + operacion.num1 + " " + ope + " " + operacion.num2);
                    resultado = this.realizarOperacion(operacion.num1, ope, operacion.num2);
                }  
            
                resultados.add(resultado);
            }

            return resultados;
        }
    }

    // Método principal y arranque del servidor
    public static void main(String[] args) {
        try {
            CalculadoraHandler handler = new CalculadoraHandler();
            Calculadora.Processor<CalculadoraHandler> processor = new Calculadora.Processor<>(handler);

            TServerTransport serverTransport = new TServerSocket(9090);
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

            System.out.println("Iniciando el servidor ...");
            server.serve();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
