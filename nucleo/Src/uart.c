#include "stm32xx_it.h"
#include "math.h"
#include "uart.h"
#include <string.h>

#define TIMEOUT 500 //TODO what is a good timeout?

#define TX_BUFFER_SIZE 1 //size of buffer used for transmitting
#define RX_BUFFER_SIZE 32001 //size of buffer used for recieving

#define AUDIO_MAX_INDEX 32001 //if transmission type was audio, it will take 32000 bytes (first byte is transmission type)
#define ROLL_MAX_INDEX 2001 //if transmission type was roll/pitch, it will take the first 2000 bytes for roll (first byte is transmission type
#define PITCH_MAX_INDEX 4001 //and the next 2000 bytes for pitch


UART_HandleTypeDef uart_handle;

uint8_t txBuffer[TX_BUFFER_SIZE]; // somewhere to store messages to transmit
uint8_t rxBuffer[RX_BUFFER_SIZE]; // somewhere to store received messages

//Need to forward received messages to BLE
#define BLE_PACKET_SIZE 20 
int pending = 0;
int indexOfPending = 1;


/**
  * @brief  Initializes USART1 in async mode
  * @param  none
  * @retval none
*/	
void UART_Initialize(void)
{
	__HAL_RCC_USART1_CLK_ENABLE();
  
	uart_handle.Instance = USART1;
  
	// Initialize TX
	uart_handle.Init.BaudRate = 115200;
	uart_handle.Init.WordLength = UART_WORDLENGTH_8B;
	uart_handle.Init.StopBits = UART_STOPBITS_1;
	uart_handle.Init.Parity = UART_PARITY_NONE;
	uart_handle.Init.Mode = UART_MODE_TX_RX;
	uart_handle.Init.HwFlowCtl = UART_HWCONTROL_NONE;
	uart_handle.Init.OverSampling = UART_OVERSAMPLING_16;	
	if (HAL_UART_Init(&uart_handle) != HAL_OK)
	{
		printf("uart not initialized\n");
	}	
}



/**
  * @brief  Calls HAL_UART_Transmit to send an amount of data in blocking mode using uart_handle and TIMEOUT. 
  * @param  response: integer to be transmitted
  * @retval None
  */
void transmit(uint8_t response){
	txBuffer[0] = response;
	while(HAL_OK != HAL_UART_Transmit(&uart_handle, txBuffer, TX_BUFFER_SIZE, TIMEOUT)){
					printf("error transmitting");
	}
}


void transmitTest(){
	transmit(55);
}

/**
  * @brief  Calls HAL_UART_Receive. If a message was received, checks if it is a roll/pitch or an audio message 
  * @param  None
  * @retval None
  */
uint8_t recieveMessage(){
	HAL_StatusTypeDef ret = HAL_UART_Receive(&uart_handle, (uint8_t*)rxBuffer, RX_BUFFER_SIZE, TIMEOUT);
  if(HAL_OK != ret){
			return 0;
    }
	else{
		if(rxBuffer[0] == TRANSMISSION_TYPE_AUDIO){
			//mark a pending transmission
			pending = TRANSMISSION_TYPE_AUDIO;
			//[0] is the transmission type
			indexOfPending = 1;
		}else if(rxBuffer[0] == TRANSMISSION_TYPE_ROLLPITCH){
			pending = TRANSMISSION_TYPE_ROLLPITCH;
			indexOfPending = 1;
		}			
	}
	return rxBuffer[0];
}

/**
  * @brief  Checks if need to relay some received data to bluetooth
  * @param  packet, pointer to array of bytes to point to chunk of data
  * @retval type of data to be transmitted (TRANSMISSION_TYPE_AUDIO, TRANSMISSION_TYPE_ROLLPITCH or 0 if there is no data left unpacketted)
*/
int getNext20BytePacket(uint8_t*packet){
	int maxIndex = -1;
	switch(pending){
		case TRANSMISSION_TYPE_AUDIO:
			maxIndex = AUDIO_MAX_INDEX;
			break;
		case TRANSMISSION_TYPE_ROLLPITCH:
			maxIndex = PITCH_MAX_INDEX;
			break;			
	}
	if(indexOfPending <= maxIndex-BLE_PACKET_SIZE){ // check just in case
		memcpy(packet,&rxBuffer[indexOfPending], BLE_PACKET_SIZE);//just brute force set address of packet to chunk of rx buffer
		indexOfPending+=BLE_PACKET_SIZE;//increment index
	}else{
		pending = 0;
	}	
	return pending;
}
