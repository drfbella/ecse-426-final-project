#include "stm32xx_it.h"
#include "math.h"

#include <string.h>

#define TIMEOUT 10000 //TODO what is a good timeout?
#define TRANSMISSION_TYPE_ROLLPITCH 1
#define TRANSMISSION_TYPE_AUDIO 2
#define TX_BUFFER_SIZE 1 //size of buffer used for transmitting
#define RX_BUFFER_SIZE 32001 //size of buffer used for recieving

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


/**
  * @brief  Calls HAL_UART_Receive. If a message was received, checks if it is a roll/pitch or an audio message 
  * @param  None
  * @retval None
  */
uint8_t recieveMessage(){
  if(HAL_OK != HAL_UART_Receive(&uart_handle, (uint8_t*)rxBuffer, RX_BUFFER_SIZE, TIMEOUT)){
			//TODO error
    }
	else{
		if(rxBuffer[0] == TRANSMISSION_TYPE_AUDIO){
			//update audio
			//send BLE
			// response BLE
			transmit(3);

		}else if(rxBuffer[0] == TRANSMISSION_TYPE_ROLLPITCH){
			//update roll and ptch
			//transmit BLE
			transmit(UINT8_MAX);
		}			
	}
	return rxBuffer[0];
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
