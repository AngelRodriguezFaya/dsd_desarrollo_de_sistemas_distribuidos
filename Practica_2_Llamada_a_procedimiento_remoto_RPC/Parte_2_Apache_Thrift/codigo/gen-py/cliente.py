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
    print("\n==== CALCULADORA DISTRIBUIDA usando Apache Thrift ====\n")
    print("Operaciones disponibles: ")
    print("0. Salir de la aplicación. ")
    print("1. Suma  '+'")
    print("2. Resta  '-'")
    print("3. Multiplicación  '*'")
    print("4. División  '/'")

    opcion = -1

    while opcion < 0 or opcion > 4:
        try:
            opcion = int(input("Seleccione una opción (0-4): "))
            if opcion < 0 or opcion > 4:
                print("Opción no válida. Por favor, seleccione una opción entre 0 y 4.")
        except ValueError:
            print("Entrada no válida. Por favor, ingrese un número entre 0 y 4.")
    
    return opcion



def procesar_menu(opcion):

    operador = ""

    match opcion:
        case 1:
            operador = "+"
        case 2:
            operador = "-"
        case 3:
            operador = "*"
        case 4:
            operador = "/"
        case _: # El '_' equivale al 'default' de C
            print("Opción no válida. Por favor, seleccione una opción entre 0 y 4.")

    return operador




def realizar_operacion(op1, op2, operador, client):

    resultado = 0

    try:
        match operador:
            case "+":
                resultado = client.suma(op1, op2)
            case "-":
                resultado = client.resta(op1, op2)
            case "*":
                resultado = client.multiplicacion(op1, op2)
            case "/":
                resultado = client.division(op1, op2)
            case _:
                print("Operador no válido. Por favor, seleccione un operador válido.")

    except CalculadoraValueError as e:
        # Esta es la excepción personalizada que definimos en el fichero .thrift 
        # Sirve para manejar errores específicos como la división por cero
        print(f"Error del servidor, no es posible realizar la operación: {e}")

    return resultado





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

            # Pedimos al usuario que ingrese los operandos para la operación
            try:
                print("\n------------------------------------")
                op1 = float(input("Ingrese el 1er operando: "))
                op2 = float(input("Ingrese el 2o operando: "))
            except ValueError:
                print("Entrada no válida. Vuelva a intentarlo.")
                continue

            # Realizamos la operación seleccionada por el usuario
            operador = procesar_menu(opcion)
            resultado = realizar_operacion(op1, op2, operador, client)

            # Mostramos el resultado de la operación al usuario
            print(f"\nResultado de {op1} {operador} {op2} = {resultado}")
            print("--------------------------------------")

    except Exception as e:
        print("Error al conectar con el servidor: " + str(e))

    finally:
        # IMPORTANTE: Cerramos el transporte al finalizar para liberar recursos
        transport.close()

if __name__ == "__main__":
    main()