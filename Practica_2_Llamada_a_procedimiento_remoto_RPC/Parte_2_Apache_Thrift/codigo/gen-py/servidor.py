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

    # Aquí implementamos las funciones definidas en el fichero .thrift
    def ping(self):
        print("\nMe han hecho ping()\n")
    
    def suma(self, num1, num2):
        print("Me han pedido que sume " + str(num1) + " y " + str(num2))
        return num1 + num2
    
    def resta(self, num1, num2):
        print("Me han pedido que reste " + str(num1) + " y " + str(num2))
        return num1 - num2
    
    def multiplicacion(self, num1, num2):
        print("Me han pedido que multiplique " + str(num1) + " y " + str(num2))
        return num1 * num2
    
    def division(self, num1, num2):
        print("Me han pedido que divida " + str(num1) + " y " + str(num2))
        if num2 == 0:
            raise CalculadoraValueError("No se puede dividir por cero.")
        return num1 / num2
    
    def potencia(self, base, exponente):
        print("Me han pedido que calcule " + str(base) + " elevado a " + str(exponente))
        return math.pow(base, exponente)
    
    def raizCuadrada(self, numero):
        print("Me han pedido que calcule la raíz cuadrada de " + str(numero))
        if numero < 0:
            raise CalculadoraValueError("No se puede calcular la raíz cuadrada de un número negativo.")
        return math.sqrt(numero)
    
    def logBase10(self, numero):
        print("Me han pedido que calcule el logaritmo base 10 de " + str(numero))
        if numero <= 0:
            raise CalculadoraValueError("No se puede calcular el logaritmo de un número no positivo.")
        return math.log10(numero)
    
    def logNatural(self, numero):
        print("Me han pedido que calcule el logaritmo natural de " + str(numero))
        if numero <= 0:
            raise CalculadoraValueError("No se puede calcular el logaritmo de un número no positivo.")
        return math.log(numero)
    
    def seno(self, angulo):
        print("Me han pedido que calcule el seno de " + str(angulo) + " grados")
        return math.sin(math.radians(angulo)) # hay que convertir el ángulo a radianes para usar la función seno de math
    
    def coseno(self, angulo):
        print("Me han pedido que calcule el coseno de " + str(angulo) + " grados")
        return math.cos(math.radians(angulo))

    def tangente(self, angulo):
        print("Me han pedido que calcule la tangente de " + str(angulo) + " grados")
        return math.tan(math.radians(angulo))

    def areaCuadrado(self, lado1, lado2):
        print("Me han pedido que calcule el área de un cuadrado con lados " 
              + str(lado1) + " y " + str(lado2))
        return lado1 * lado2
    
    def areaTriangulo(self, base, altura):
        print("Me han pedido que calcule el área de un triángulo con base " 
              + str(base) + " y altura " + str(altura))
        return (base * altura) / 2
    
    def areaCirculo(self, radio):
        print("Me han pedido que calcule el área de un círculo con radio " 
              + str(radio))
        return 3.14159 * radio * radio
    
    def multMat(self, m1, m2):
        if len(m1[0]) != len(m2):
            raise CalculadoraValueError("El número de columnas de la primera matriz" + 
                                        " debe ser igual al número de filas de la segunda matriz.")
        
        result = [[0 for _ in range(len(m2[0]))] for _ in range(len(m1))]
        
        for i in range(len(m1)):
            for j in range(len(m2[0])):
                for k in range(len(m2)):
                    result[i][j] += m1[i][k] * m2[k][j]

        return result
    

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