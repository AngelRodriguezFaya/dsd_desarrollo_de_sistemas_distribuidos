try{
    mensaje = multMat(matriz1, matriz2)

}
except CalculadoraValueError as e:
    print("Error:", e.message)