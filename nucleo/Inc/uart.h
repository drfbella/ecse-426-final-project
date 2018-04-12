#ifndef __UART_H
#define __UART_H
#include <stdint.h>
#define TRANSMISSION_TYPE_ROLLPITCH 1
#define TRANSMISSION_TYPE_AUDIO 2

void transmit(uint8_t response);
void UART_Initialize(void);
uint8_t recieveMessage(void);
void transmitTest(void);
int getNext20BytePacket(uint8_t*packet);
#endif 
