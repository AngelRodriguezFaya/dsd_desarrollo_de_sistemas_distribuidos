# Faltan funciones

# Para importar el cliente
from calc import Calculadora
from calc.ttypes import CalculadoraValueError 

def multMat(self, m1, m2):
    if len(m1[0]) != len(m2):
        raise CalculadoraValueError("El número de columnas de la primera matriz debe ser igual al número de filas de la segunda matriz.")
    
    result = [[0 for _ in range(len(m2[0]))] for _ in range(len(m1))]
    
    for i in range(len(m1)):
        for j in range(len(m2[0])):
            for k in range(len(m2)):
                result[i][j] += m1[i][k] * m2[k][j]

    # Otra forma de hacerlo: Forma clásica de multiplicar matrices
    r = [[0 for _ in range(len(m2[0]))] for _ in range(len(m1))]
    for i in range(len(m1)):
        for j in range(len(m2[0])):
            for k in range(len(m2)):
                r[i][j] += m1[i][k] * m2[k][j]
    #return r
    return result

    # Otra forma de hacerlo: 
