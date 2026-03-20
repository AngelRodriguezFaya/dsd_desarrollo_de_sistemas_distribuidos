exception CalculadoraValueError{
    1: string message
}

service Calculadora{
    void ping(),
    i32 suma(1:i32 num1, 2:i32 num2),
    i32 resta(1:i32 num1, 2:i32 num2),
    i32 multiplicacion(1:i32 num1, 2:i32 num2),
    i32 division(1:i32 num1, 2:i32 num2) throws (1: CalculadoraValueError ex),

    i32 sumaTodos(1: list<i32> numeros),
    i32 restaTodos(1: list<i32> numeros),,

    i32 areaCuadrado(1:i32 lado1, 2:i32 lado2),
    double areaTriangulo(1:double base, 2:double altura),
    double areaCirculo(1:double radio)

    list<list<i32>> multMat(1: list<list<i32>> m1, 2: list<list<i32>> m2) throws (1: CalculadoraValueError ex)
}