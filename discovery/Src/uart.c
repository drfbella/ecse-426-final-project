#include "stm32f4xx_it.h"
#include <math.h>
#include <string.h>

#define TIMEOUT 10000 //TODO what is a good timeout?
#define TRANSMISSION_TYPE_ROLLPITCH 1
#define TRANSMISSION_TYPE_AUDIO 2
#define TX_BUFFER_SIZE 10 //size of buffer used for transmitting
#define RX_BUFFER_SIZE 10 //size of buffer used for recieving

//pin tx A2, rx   on stm32f407

UART_HandleTypeDef uart_handle;

//buffer for outgoing data
uint8_t txBuffer[TX_BUFFER_SIZE];
//buffer for incoming data
uint8_t rxBuffer[RX_BUFFER_SIZE];

void UART_Initialize(void)
{
	
	__HAL_RCC_USART2_CLK_ENABLE();
	
	// Initialize
	uart_handle.Instance = USART2;

	uart_handle.Init.BaudRate = 115200;
	uart_handle.Init.WordLength = UART_WORDLENGTH_8B;
	uart_handle.Init.StopBits = UART_STOPBITS_1;
	uart_handle.Init.Parity = UART_PARITY_NONE;
	uart_handle.Init.HwFlowCtl = UART_HWCONTROL_NONE;
	uart_handle.Init.Mode = UART_MODE_TX_RX;
	uart_handle.Init.OverSampling = UART_OVERSAMPLING_16;
	
	
	if (HAL_UART_Init(&uart_handle) != HAL_OK)
	{
		//Print statements for possible errors. Errors to be determined see main 347
	}

	HAL_NVIC_SetPriority(USART2_IRQn, 0, 1);
  HAL_NVIC_EnableIRQ(USART2_IRQn);
}



/**
  * @brief  Calls HAL_UART_Transmit to send data in txBuffer in blocking mode using uart_handle and TIMEOUT. 
  * @retval None
  */
void transmit(){
  HAL_StatusTypeDef ret = HAL_UART_Transmit(&uart_handle, txBuffer, TX_BUFFER_SIZE, TIMEOUT);
	if(HAL_OK != ret){
  }
}

/**
  * @brief  Calls HAL_UART_Recieve to recieve data into rxBuffer in blocking mode using uart_handle and TIMEOUT. 
  * @retval None
  */
void recieve(){
	if(HAL_OK != HAL_UART_Receive(&uart_handle, rxBuffer, RX_BUFFER_SIZE, TIMEOUT)){
		    _Error_Handler(__FILE__, __LINE__);
	}	

	//format data somehow
}

/**
  * @brief  maps an angle in degrees stored as a float to 2 bytes
  * @param 	toEncode a float value to to encode into 2 bytes (0 -360 degrees)
  * @brief  encoded a byte array of size 2 representing the float value 
  * @retval None
  */
void encodeFloatDegree(float toEncode, uint8_t encoded[]){
	
	//in case the given roll or pitch value is negative, want an unsigned value
	if(toEncode < 0){
		toEncode = toEncode + 360;
	}
	//TODO this could be more modular
	// /360 * 65535, 2^(resolution) - 1
	uint16_t temp = (toEncode/360) * (pow(2, (16))-1);
	memcpy(encoded, &temp, 2);
}



//TODO this is just taking one value. In fact, we need an array of roll pitch values 
void transmitRollAndPitch(float roll, float pitch){
//	memset(txBuffer, 0, TX_BUFFER_SIZE); //clear transmitting buffer of junk, maybe unnecessary
	txBuffer[0] = TRANSMISSION_TYPE_ROLLPITCH; //identify type of data being transmitted
	encodeFloatDegree(roll, (txBuffer+1));//convert roll and pitch to byte[] and add to transmission mesage
	encodeFloatDegree(pitch, (txBuffer+3));
	transmit();//transmit message	
}

void transmitAudioData(){
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


