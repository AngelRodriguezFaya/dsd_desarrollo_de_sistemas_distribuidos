/* calc.x : Protocolo de calculadora */
/* Estas son las operaciones que va a ofrecer nuestro servidor */

/* Enumerado para nro de operaciones en el menú */
enum num_operaciones{
    OP_SUMA = 1,
    OP_RESTA = 2,
    OP_MULTIPLICACION = 3,
    OP_DIVISION = 4,
    OP_POTENCIA = 5,
    OP_LOG10 = 6,
    OP_LOGN = 7,
    OP_SENO = 8,
    OP_COSENO = 9,
    OP_TANGENTE = 10,
    OP_LOTES = 11
};

const MAX_OP = 30; /*Nro máximo de operaciones que se pueden calcular por lotes */

/* Encapsula los operandos y el operador en una sola estructura */
struct operacion{
    double op1; /* Operando 1 */
    char operador;
    double op2; /* Operando 2 */
};

typedef operacion operaciones<MAX_OP>; /* Operaciones a calcular por lotes */

typedef double resultados<MAX_OP>; /* Resultados de las operaciones por lotes */

program CALCPROG{
    version CALCVER {
        /* Operaciones en MODO BÁSICO */
        double SUMA(double, double) = 1;
        double RESTA(double, double) = 2;
        double MULTIPLICACION(double, double) = 3;
        double DIVISION(double, double) = 4;

        /* Operaciones en MODO AVANZADO */
        double POTENCIA(double, double) = 5;
        double LOG10(double) = 6;
        double LOGN(double) = 7;
        double SENO(double) = 8;
        double COSENO(double) = 9;
        double TANGENTE(double) = 10;
        resultados CALCULAR_LOTES(operaciones) = 11;

    } = 1;
} = 0x20000001;