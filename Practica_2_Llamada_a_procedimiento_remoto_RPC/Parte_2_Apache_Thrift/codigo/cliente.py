# cliente.py

# Importamos la clase cliente generada por Thrift a partir del fichero .thrift
from calculadora import Calculadora
from calculadora.ttypes import CalculadoraValueError

# Importamos las librerías de Thrift
from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol


def mostar_menu():
    print("\n==== CALCULADORA DISTRIBUIDA usando Apache Thrift ============================")
    print("Opciones disponibles: \n")
    print("  0. Salir de la aplicación. ")
    print("  1. Suma                                     '+'")
    print("  2. Resta                                    '-'")
    print("  3. Multiplicación                           'x'")
    print("  4. División                                 '/'")
    print("  5. Potencia                                 '^'")
    print("  6. Raíz cuadrada                            'sqrt'")
    print("  7. Logaritmo base 10                        'log10'")
    print("  8. Logaritmo natural                        'ln'")
    print("  9. Seno                                     'sin'")
    print("  10. Coseno                                  'cos'")
    print("  11. Tangente                                'tan'")
    print("  12. Área de un cuadrado                     'aCua'")
    print("  13. Área de un triángulo                    'aTri'")
    print("  14. Área de un círculo                      'aCir'")
    print("  15. Suma de matrices (de enteros)           'sMat'")
    print("  16. Multiplicación de matrices (de enteros) 'mMat'")
    print("  17. Calcular un lote de operaciones. (sólo operaciones básicas, no matrices) ")
    print("==============================================================================\n")

    opcion = -1

    while opcion < 0 or opcion > 17:
        try:
            opcion = int(input(" Seleccione una opción (0-17): "))
            if opcion < 0 or opcion > 17:
                print("Opción no válida. Por favor, seleccione una opción entre 0 y 17.")
        except ValueError:
            print("Entrada no válida. Por favor, ingrese un número entre 0 y 17.")
    
    return opcion



def procesar_opcion(opcion):

    operador = ""

    match opcion:
        case 1:
            operador = "+"
        case 2:
            operador = "-"
        case 3:
            operador = "x"
        case 4:
            operador = "/"
        case 5:
            operador = "^"
        case 6:
            operador = "sqrt"
        case 7:
            operador = "log10"
        case 8:
            operador = "ln"
        case 9:
            operador = "sin"
        case 10:
            operador = "cos"
        case 11:
            operador = "tan"
        case 12:
            operador = "aCua"
        case 13:
            operador = "aTri"
        case 14:
            operador = "aCir"
        case 15:
            operador = "sMat"
        case 16:
            operador = "mMat"
        case 17:
            operador = "lote"

        case _: # El '_' equivale al 'default' de C
            print("Opción no válida. Por favor, seleccione una opción entre 0 y 17.")

    return operador



def leer_matriz():
    # Pedimos al usuario que introduzca el número de filas y columnas de la matriz
    filas = int(input(" Introduzca el número de filas de la matriz: "))
    columnas = int(input(" Introduzca el número de columnas de la matriz: "))

    matriz = []

    print(f"\n Introduzca los elementos fila por fila, separados por espacios, (total {columnas} elementos por fila):")
    for i in range(filas):
        # Leemos la fila, la dividimos por espacios y convertimos cada elemento a entero
        valores_fila = input(f" Fila {i+1}: ").split()
        fila = [int(valor) for valor in valores_fila]

        # Validación para asegurar que introduce el número correcto de columnas
        while len(fila) != columnas:
            print(f" Error: Debes introducir exactamente {columnas} elementos.")
            valores_fila = input(f" Fila {i+1}: ").split()
            fila = [int(valor) for valor in valores_fila]
        
        matriz.append(fila)

    return matriz



def leer_lote_operaciones():
    print("\n Para esta opción, debe introducir un lote de operaciones a realizar.")
    print(" Primero introducirá el número de operaciones que desea realizar, luego cada operación una por una.")
    print(" Asegúrese de introducir cada operación en el formato correcto según el tipo de operación que desea realizar.")
    print(" SÓLO se permiten operaciones básicas (no matrices) para esta opción.")
    print("-------------------------------------------------------------\n")

    operaciones = []

    try:
        num_operaciones = int(input(" Ingrese el número de operaciones a realizar: "))

        for i in range(num_operaciones):
            print(f"\n --- Introduzca la operación nro {i+1} de {num_operaciones} en total: ---")
            print(" Operadores disponibles: +, -, x, /, ^, sqrt, log10, ln, sin, cos, tan, aCua, aTri, aCir")
            operador = input(" Introduzca el operador: ")
            
            operaciones.append(pedir_operacion_basica(operador))

            print(f" --- Operación nro {i+1} leída --- ")

    except ValueError:
        print("Entrada no válida. Vuelva a intentarlo.")
        return None
    
    print ("\n -------- Lote de operaciones leído correctamente ---------- \n")

    return operaciones



def pedir_operacion_basica(operador):

    op1 = 0
    op2 = 0

    try:
        print("--------------------------------------")

        if operador in ["sqrt", "log10", "ln"]:
            op1 = float(input(" Introduzca el operando: "))
        
        elif operador in ["sin", "cos", "tan"]:
            op1 = float(input(" Introduzca el ángulo en grados: "))

        elif operador in ["aCir"]:
            op1 = float(input(" Introduzca el radio del círculo: "))
            
        elif operador in ["aCua"]:
            op1 = float(input(" Introduzca el lado del cuadrado: "))
        
        elif operador in ["aTri"]:
            op1 = float(input(" Introduzca la base del triángulo: "))
            op2 = float(input(" Introduzca la altura del triángulo: "))
        
        else: # Resto de operaciones binarias
            op1 = float(input(" Introduzca el 1er operando: "))
            op2 = float(input(" Introduzca el 2o operando: "))

        print("--------------------------------------")
    
    except ValueError:
        print("Entrada no válida. Vuelva a intentarlo.")
        return None

    return Calculadora.Operacion(num1=op1, operador=operador, num2=op2)



def pedir_operacion_matrices(operador):

    if operador in ["sMat", "mMat"]:
        print("\n Para esta operación, debe introducir dos matrices de enteros.")
        print(" Primero introducirá la primera matriz, luego la segunda.")
        print(" Asegúrese de que las dimensiones de las matrices sean compatibles con la operación seleccionada.")
        print("-------------------------------------------------------------\n")
        
        m1 = leer_matriz()
        print("\n La primera matriz introducida es: ")
        mostrar_matriz(m1) 

        print("\n Introduzca la segunda matriz: ")
        m2 = leer_matriz()
        print("\n La segunda matriz introducida es: ")
        mostrar_matriz(m2)

        print("-------------------------------------------------------------\n")

    else:
        print(" Operador no válido para esta función. Vuelva a intentarlo.")
        return None
    
    return Calculadora.OperacionMatriz(m1=m1, operador=operador, m2=m2)



def pedir_operacion(operador):

    if operador in ["sMat", "mMat"]:
        return pedir_operacion_matrices(operador)
    else:
        return pedir_operacion_basica(operador)



def realizar_operacion_basica(operacion, client):

    resultado = None

    try:
        match operacion.operador:
            case "+":
                resultado = client.suma(operacion.num1, operacion.num2)
            case "-":
                resultado = client.resta(operacion.num1, operacion.num2)
            case "x":
                resultado = client.multiplicacion(operacion.num1, operacion.num2)
            case "/":
                resultado = client.division(operacion.num1, operacion.num2)
            case "^":
                resultado = client.potencia(operacion.num1, operacion.num2)
            case "sqrt":
                resultado = client.raizCuadrada(operacion.num1)
            case "log10":
                resultado = client.logBase10(operacion.num1)
            case "ln":
                resultado = client.logNatural(operacion.num1)
            case "sin":
                resultado = client.seno(operacion.num1)
            case "cos":
                resultado = client.coseno(operacion.num1)
            case "tan":
                resultado = client.tangente(operacion.num1)
            case "aCua":
                resultado = client.areaCuadrado(operacion.num1)
            case "aTri":
                resultado = client.areaTriangulo(operacion.num1, operacion.num2)
            case "aCir":
                resultado = client.areaCirculo(operacion.num1)
            case _:
                print("Operador no válido. Por favor, seleccione un operador válido.")

    except CalculadoraValueError as e:
        # Esta es la excepción personalizada que definimos en el fichero .thrift 
        # Sirve para manejar errores específicos como la división por cero
        print(f"Error del servidor, no es posible realizar la operación. \n{e}")
        # resultado = None

    return resultado



def realizar_operacion_matrices(operacion, client):
    
    resultado = None

    try:
        match operacion.operador:
            case "sMat":
                resultado = client.sumaMatrices(operacion.m1, operacion.m2)
            case "mMat":
                resultado = client.multiplicaMatrices(operacion.m1, operacion.m2)
            case _:
                print("Operador no válido. Por favor, seleccione un operador válido para matrices.")

    except CalculadoraValueError as e:
        print(f"Error del servidor, no es posible realizar la operación con matrices. \n{e}")

    return resultado



def realizar_operacion(operacion, client):

    if isinstance(operacion, Calculadora.Operacion): # Operación básica
        return realizar_operacion_basica(operacion, client)
    
    elif isinstance(operacion, Calculadora.OperacionMatriz): # Operación con matrices
        return realizar_operacion_matrices(operacion, client)
    
    else:
        print("Tipo de operación no reconocido.")
        return None



def mostrar_matriz(matriz):
    for fila in matriz:
        # Con esto hacemos que Python reserve 4 espacios para este número, alineado a la derecha"
        fila_formateada = [f"{elemento:4}" for elemento in fila]
        
        # Unimos los elementos de la fila
        print("\t[" + " ".join(fila_formateada) + " ]")



def mostrar_resultado_matrices(operacion, resultado):
    if operacion.operador == "sMat":
        operador = "+"
    elif operacion.operador == "mMat":
        operador = "x"
    
    print("\n--- MATRIZ 1 ---")
    mostrar_matriz(operacion.m1)
    
    print(f"\n       {operador}       \n")
    
    print("--- MATRIZ 2 ---")
    mostrar_matriz(operacion.m2)
    
    print("\n = RESULTADO =")
    mostrar_matriz(resultado)
    print("\n----------------\n")



def mostrar_resultado_lote(operaciones, resultados):

    print("\n=== RESULTADOS DEL LOTE DE OPERACIONES ===\n")

    for i in range(len(operaciones)):
        operacion = operaciones[i]
        resultado = resultados[i]

        print(f" Operación {i+1}: ", end="")
        mostrar_resultado(operacion, resultado)

    print("\n==========================================\n")



def mostrar_resultado(operacion, resultado):

    if operacion.operador in ["sqrt", "log10", "ln", "sin", "cos", "tan"]:
        print(f"\n >>> Resultado de {operacion.operador}({operacion.num1}) = {resultado}")

    elif operacion.operador in ["aCua"]:
        print(f"\n >>> Área de un cuadrado de lados {operacion.num1} y {operacion.num2} = {resultado}")
    
    elif operacion.operador in ["aTri"]:
        print(f"\n >>> Área de un triángulo de base {operacion.num1} y altura {operacion.num2} = {resultado}")

    elif operacion.operador in ["aCir"]:
        print(f"\n >>> Área de un círculo de radio {operacion.num1} = {resultado}")
    
    elif operacion.operador in ["sMat", "mMat"]:
        print(f"\n >>> Resultado de la operación con matrices: ")
        mostrar_resultado_matrices(operacion, resultado)
    
    else: # Resto de operaciones binarias
        print(f"\n >>> Resultado de {operacion.num1} {operacion.operador} {operacion.num2} = {resultado}")


def main():

    # Creamos el transporte, el protocolo y el cliente para conectarnos al servidor
    transport = TSocket.TSocket("localhost", 9090)
    transport = TTransport.TBufferedTransport(transport)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)

    # Creamos el cliente 
    client = Calculadora.Client(protocol)

    try:
        # Abrimos el transporte para establecer la conexión con el servidor
        transport.open()

        print("hacemos ping al server")
        client.ping()

        while True:
            opcion = mostar_menu()

            if opcion == 0:
                print("\nSaliendo de la calculadora. ¡Hasta luego!\n")
                break

            # Obtenemos el operador de la operación seleccionada por el usuario
            operador = procesar_opcion(opcion)

            
            if operador == "lote": # Si el usuario seleccionó la opción de procesar un lote de operaciones
                operaciones = leer_lote_operaciones()
                
                if operaciones is not None:
                    resultados = client.procesaLoteOperaciones(operaciones)
                    mostrar_resultado_lote(operaciones, resultados)
                
                else:
                    print("No se pudo leer el lote de operaciones. Vuelva a intentarlo.")

                print("\n Pulse 'Enter' para continuar...")
                input()
                print("\n\n\n\n\n\n\n\n")
                
            else: # El usuario seleccionó una operación básica o con matrices            
                operacion = pedir_operacion(operador)
                
                if operacion is None:
                    print("No se pudo crear la operación. Vuelva a intentarlo.")
                    continue
                else:
                    resultado = realizar_operacion(operacion, client)
                    if resultado is not None:
                        # Mostramos el resultado de la operación al usuario
                        mostrar_resultado(operacion, resultado)

                print("\n Pulse 'Enter' para continuar...")
                input()
                print("\n\n\n\n\n\n\n\n")
            
            
    except Exception as e:
        print("Error al conectar con el servidor: " + str(e))

    finally:
        # IMPORTANTE: Cerramos el transporte al finalizar para liberar recursos
        transport.close()

if __name__ == "__main__":
    main()