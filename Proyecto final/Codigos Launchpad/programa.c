#include <msp430.h> 
#include <inttypes.h>
/*
 * main.c
 */

void prender_apagar(P1){
    if(BIT0){
        while(1){
            __delay_cycles(100000);
            P1OUT ^= BIT6;
            P1OUT ^= BIT0;
        }
    }
}

int main(){                    
    WDTCTL = WDTPW + WDTHOLD;
    P1SEL = 0x00;
    P1DIR |= (BIT0 + BIT6);    //Pines 0 y 6 como salida
    P1OUT |=BIT0;             	
    P1OUT &= ~BIT6;
    prender_apagar(BIT0 + BIT6);
    return 0;
}

