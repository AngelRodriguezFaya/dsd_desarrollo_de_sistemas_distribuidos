/* calculadora.thrift */

struct Operacion {
    1: double num1,
    2: double operador,
    3: double num2
}

exception CalculadoraValueError{
    1: string message
}

service Calculadora{
    void ping(),
    double suma(1:double num1, 2:double num2),
    double resta(1:double num1, 2:double num2),
    double multiplicacion(1:double num1, 2:double num2),
    double division(1:double num1, 2:double num2), throws (1: CalculadoraValueError ex),

    /*double potencia(1:double base, 2:double exponente),
    double raizCuadrada(1:double numero),
    double logBase10(1:double numero),
    double logNatural(1:double numero),
    double seno(1:double angulo),
    double coseno(1:double angulo),
    double tangente(1:double angulo)*/22

    double areaCuadrado(1:double lado1, 2:double lado2),
    double areaTriangulo(1:double base, 2:double altura),
    double areaCirculo(1:double radio)

    list<list<i32>> multiplicaMatrices(1: list<list<i32>> m1, 2: list<list<i32>> m2) throws (1: CalculadoraValueError ex)
}