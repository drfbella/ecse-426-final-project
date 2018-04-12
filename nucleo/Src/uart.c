#include "stm32xx_it.h"
#include "math.h"
#include "uart.h"
#include <string.h>

#define TIMEOUT 500 //TODO what is a good timeout?

#define TX_BUFFER_SIZE 1 //size of buffer used for transmitting
#define RX_BUFFER_SIZE 32001 //size of buffer used for recieving
#define AUDIO_MAX_INDEX 32000
#define ROLL_MAX_INDEX 2000
#define PITCH_MAX_INDEX 4000
#define BLE_PACKET_SIZE 20

UART_HandleTypeDef uart_handle;

uint8_t txBuffer[TX_BUFFER_SIZE];
uint8_t rxBuffer[RX_BUFFER_SIZE];

void UART_Initialize(void)
{
//  HAL_UART_MspInit(&uart_handle);

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
		//Print statements for possible errors. Errors to be determined see main 347
	}
	
//	HAL_NVIC_SetPriority(USART1_IRQn, 0, 1);
//  HAL_NVIC_EnableIRQ(USART1_IRQn);
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

int pending = 0;
int indexOfPending = 1;
/**
  * @brief  Calls HAL_UART_Receive. If a message was received, checks if it is a roll/pitch or an audio message 
  * @param  None
  * @retval None
  */
uint8_t recieveMessage(){
	HAL_StatusTypeDef ret = HAL_UART_Receive(&uart_handle, (uint8_t*)rxBuffer, RX_BUFFER_SIZE, TIMEOUT);
  if(HAL_OK != ret){
			
    }
	else{
		if(rxBuffer[0] == TRANSMISSION_TYPE_AUDIO){
			//update audio
			//send BLE
			// response BLE
			pending = TRANSMISSION_TYPE_AUDIO;
			indexOfPending = 1;
		//	transmit(3); //TODO remove this is for testing transmit
		}else if(rxBuffer[0] == TRANSMISSION_TYPE_ROLLPITCH){
			//update roll and ptch
			//transmit BLE
			pending = TRANSMISSION_TYPE_ROLLPITCH;
			indexOfPending = 1;
			transmit(UINT8_MAX);
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
/*		if(indexOfPending > maxIndex){ // check again - if we have now surpassed the index, set pending to 0 (no packets left)
			printf("%d", indexOfPending);
			pending = 0;
			indexOfPending = 1;
		}*/
	}else{
		pending = 0;
	}	
	return pending;
}



//Do we need/want interrupts? try with polling first

//interupt callbacks
//if using them need HAL_UART_Receive_IT(&uart_handle, (uint8_t *)rxBuffer, RX_BUFFER_SIZE)
//and wait for done maybe.
//same for transmit

/*
void HAL_UART_TxCpltCallback(UART_HandleTypeDef *UartHandle)
{
}

void HAL_UART_RxCpltCallback(UART_HandleTypeDef *UartHandle)
{
}
*/
