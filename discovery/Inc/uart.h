#ifndef __UART_H
#define __UART_H

void UART_Initialize(void);
void transmitTest(void);
void receiveTest(void);
void transmitFreakinHugeRollAndPitchArrays(float roll[], float pitch[], int size);
void transmitFreakinHugeAudioArray(uint32_t audio[], int size);
int receiveResponseInt(void);
#endif 
