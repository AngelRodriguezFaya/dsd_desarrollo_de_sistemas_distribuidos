# servidor.py

import glob
import sys

import math # Para funciones matemáticas

from calculadora import Calculadora
from calculadora.ttypes import CalculadoraValueError
# Lo de ttypes es por añadir tipos en el fichero .thrift

# Importamos las librerías de Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

import logging
logging.basicConfig(level=logging.DEBUG)
# Muy útil para depurar e imprmimir cuando haya errores en el servidor

# Definimos la clase que implementa el handler de la calculadora
class CalculadoraHandler:
    def __init__(self): # Constructor de la clase
        self.log = {}

    def realizar_operacion(self, op1, operador, op2):

        resultado = None

        match operador:

            case "+":
                print("Me han pedido que sume " + str(op1) + " y " + str(op2))
                resultado = op1 + op2
            
            case "-":
                print("Me han pedido que reste " + str(op1) + " y " + str(op2))
                resultado = op1 - op2  

            case "x":
                print("Me han pedido que multiplique " + str(op1) + " y " + str(op2))
                resultado = op1 * op2
            
            case "/":
                print("Me han pedido que divida " + str(op1) + " y " + str(op2))
                if op2 == 0:
                    raise CalculadoraValueError("No se puede dividir por cero.")
                resultado = op1 / op2

            case "^":
                print("Me han pedido que calcule " + str(op1) + " elevado a " + str(op2))
                resultado = math.pow(op1, op2)  
            
            case "sqrt":
                print("Me han pedido que calcule la raíz cuadrada de " + str(op1))
                if op1 < 0:
                    raise CalculadoraValueError("No se puede calcular la raíz cuadrada de un número negativo.")
                resultado = math.sqrt(op1)

            case "log10":
                print("Me han pedido que calcule el logaritmo base 10 de " + str(op1))
                if op1 <= 0:
                    raise CalculadoraValueError("No se puede calcular el logaritmo de un número no positivo.")
                resultado = math.log10(op1)

            case "ln":
                print("Me han pedido que calcule el logaritmo natural de " + str(op1))
                if op1 <= 0:
                    raise CalculadoraValueError("No se puede calcular el logaritmo de un número no positivo.")
                resultado = math.log(op1)
            
            case "sin":
                print("Me han pedido que calcule el seno de " + str(op1) + " grados")
                resultado = math.sin(math.radians(op1)) # hay que convertir el ángulo a radianes para usar la función
            
            case "cos":
                print("Me han pedido que calcule el coseno de " + str(op1) + " grados")
                resultado = math.cos(math.radians(op1))

            case "tan":
                print("Me han pedido que calcule la tangente de " + str(op1) + " grados")
                resultado = math.tan(math.radians(op1))

            case "aCua":
                print("Me han pedido que calcule el área de un cuadrado con lados " 
                      + str(op1) + " y " + str(op2))
                resultado = op1 * op2
            
            case "aTri":
                print("Me han pedido que calcule el área de un triángulo con base " 
                      + str(op1) + " y altura " + str(op2))
                resultado = (op1 * op2) / 2
            
            case "aCir":
                print("Me han pedido que calcule el área de un círculo con radio " + str(op1))
                resultado = math.pi * op1 * op1

            case _:
                raise CalculadoraValueError("Operador no válido.")

        return resultado



    # Aquí implementamos las funciones definidas en el fichero .thrift
    def ping(self):
        print("\nMe han hecho ping()\n")
    
    def suma(self, num1, num2):
        return self.realizar_operacion(num1, "+", num2)
    
    def resta(self, num1, num2):
        return self.realizar_operacion(num1, "-", num2)
    
    def multiplicacion(self, num1, num2):
        return self.realizar_operacion(num1, "*", num2)
    
    def division(self, num1, num2):
        return self.realizar_operacion(num1, "/", num2)
    
    def potencia(self, base, exponente):
        return self.realizar_operacion(base, "^", exponente)
    
    def raizCuadrada(self, numero):
        return self.realizar_operacion(numero, "sqrt", None)
    
    def logBase10(self, numero):
        return self.realizar_operacion(numero, "log10", None)
    
    def logNatural(self, numero):
        return self.realizar_operacion(numero, "ln", None)

    def seno(self, angulo):
        return self.realizar_operacion(angulo, "sin", None)
    
    def coseno(self, angulo):
        return self.realizar_operacion(angulo, "cos", None)

    def tangente(self, angulo):
        return self.realizar_operacion(angulo, "tan", None)

    def areaCuadrado(self, lado1, lado2):
        return self.realizar_operacion(lado1, "aCua", lado2)
    
    def areaTriangulo(self, base, altura):
        return self.realizar_operacion(base, "aTri", altura)
    
    def areaCirculo(self, radio):
        return self.realizar_operacion(radio, "aCir", None)
    

    def sumaMatrices(self, m1, m2):
        if (len(m1) != len(m2)) or (len(m1[0]) != len(m2[0])):
            raise CalculadoraValueError("Las dimensiones de las matrices son distintas")

        # Inicializamos la matriz resultado con ceros
        resultado = [[0 for _ in range(len(m1))] for _ in range(len(m1[0]))]

        for i in range (len(m1)):
            for j in range (len(m1[0])):
                resultado[i][j] = m1[i][j] + m2[i][j]
        
        return resultado
            
    
    def multiplicaMatrices(self, m1, m2):
        print("Me han pedido que multiplique dos matrices")
        if len(m1[0]) != len(m2):
            raise CalculadoraValueError("El número de columnas de la primera matriz" + 
                                        " debe ser igual al número de filas de la segunda matriz.")
        
        # Inicializamos la matriz resultado con ceros
        resultado = [[0 for _ in range(len(m2[0]))] for _ in range(len(m1))]
        
        for i in range(len(m1)):
            for j in range(len(m2[0])):
                for k in range(len(m2)):
                    resultado[i][j] += m1[i][k] * m2[k][j]

        return resultado
    
    def procesaLoteOperaciones(self, operaciones):

        resultados = []

        for operacion in operaciones:

            if operacion.operador in ["sqrt", "log10", "ln", "sin", "cos", "tan", "aCir"]:
                print(f"Procesando operación: {operacion.operador} ({operacion.num1})")
                resultados.append(self.realizar_operacion(operacion.num1, operacion.operador, None))

            else:
                print(f"Procesando operación: {operacion.num1} {operacion.operador} {operacion.num2}")
                resultados.append(self.realizar_operacion(operacion.num1, operacion.operador, operacion.num2))

        return resultados


# Programa principal para iniciar el servidor
if __name__ == "__main__":
    handler = CalculadoraHandler()
    processor = Calculadora.Processor(handler)
    transport = TSocket.TServerSocket(host="127.0.0.1", port=9090)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)

    print("iniciando servidor...")
    server.serve()
    print("fin del servidor")