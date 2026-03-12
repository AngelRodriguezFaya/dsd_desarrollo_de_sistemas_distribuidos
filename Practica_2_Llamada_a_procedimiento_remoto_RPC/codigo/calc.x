/* calc.x : Protocolo de calculadora */
/* Estas son las operaciones que va a ofrecer nuestro servidor */

const MAX = 30; /*Nro máximo de operaciones que se pueden calcular por lotes */

struct operacion{
    int op1; /* Operando 1 */
    char operador;
    int op2; /* Operando 2 */
};

typedef operacion operaciones<MAX>; /* Operaciones a calcular por lotes */

typedef int resultados<MAX>; /* Resultados de las operaciones por lotes */

program CALCPROG{
    version CALCVER {
        int SUMA(int, int) = 1;
        int RESTA(int, int) = 2;
        int MULTIPLICACION(int, int) = 3;
        int DIVISION(int, int) = 4;
        resultados CALCULAR_LOTES(operaciones) = 5;
    } = 1;
} = 0x20000001;