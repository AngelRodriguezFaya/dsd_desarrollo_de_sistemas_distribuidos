/* calc.x : Protocolo de calculadora */
/* Estas son las operaciones que va a ofrecer nuestro servidor */

program CALCPROG{
    version CALCVER {
        int SUMA(int, int) = 1;
        int RESTA(int, int) = 2;
        int MULTIPLICACION(int, int) = 3;
        int DIVISION(int, int) = 4;
    } = 1;
} = 0x20000001;