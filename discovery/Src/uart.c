#include "stm32f4xx_it.h"
#include <math.h>
#include <string.h>

#define TIMEOUT 10000 //TODO what is a good timeout?
#define TRANSMISSION_TYPE_ROLLPITCH 1
#define TRANSMISSION_TYPE_AUDIO 2
#define TX_BUFFER_SIZE 16000 //size of buffer used for transmitting
#define RX_BUFFER_SIZE 5 //size of buffer used for recieving

//pin tx A2, rx   on stm32f407

UART_HandleTypeDef uart_handle;

//buffer for outgoing data
uint8_t txBuffer[TX_BUFFER_SIZE];
//buffer for incoming data
uint8_t rxBuffer[RX_BUFFER_SIZE];

void UART_Initialize(void)
{
	
  uart_handle.Instance = UART5;
  uart_handle.Init.BaudRate = 115200;
  uart_handle.Init.WordLength = UART_WORDLENGTH_8B;
  uart_handle.Init.StopBits = UART_STOPBITS_1;
  uart_handle.Init.Parity = UART_PARITY_NONE;
  uart_handle.Init.Mode = UART_MODE_TX_RX;
  uart_handle.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  uart_handle.Init.OverSampling = UART_OVERSAMPLING_16;
  if (HAL_UART_Init(&uart_handle) != HAL_OK)
  {
    _Error_Handler(__FILE__, __LINE__);
  }
}


HAL_StatusTypeDef retTxTest = HAL_OK; 
void transmitTest(){
	txBuffer[0] = 'F';
	txBuffer[1] = 'U';
	txBuffer[2] = 'C';
	txBuffer[3] = 'K';
	
  retTxTest = HAL_UART_Transmit(&uart_handle, (uint8_t*)txBuffer, 4, TIMEOUT);
	if(HAL_OK != retTxTest){
		printf("TX TEST FAILED \n");
		if(HAL_TIMEOUT == retTxTest){
			printf("tx timed out");
		}
  }
}

HAL_StatusTypeDef retRxTest = HAL_OK; 
void receiveTest(){
	retRxTest = HAL_UART_Receive(&uart_handle, rxBuffer, RX_BUFFER_SIZE, TIMEOUT);
	if(HAL_OK != retRxTest){
		printf("RX TEST FAILED \n");
		if(HAL_TIMEOUT == retRxTest){
			printf("rx timed out");
		}
	}	
}

/**
  * @brief  maps an angle in degrees stored as a float to 2 bytes
  * @param 	toEncode a float value to to encode into 2 bytes (0 -360 degrees)
  * @param  encoded a byte array of size 2 representing the float value 
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
	if(HAL_OK != HAL_UART_Transmit(&uart_handle, txBuffer, 5, TIMEOUT)){
		printf("transmit single roll pitch failed");
	}
}

/**
  * @brief  maps an array of angle in degrees stored as a float to an array of bytes (2 bytes per value)
	* @note if a value to encode is negative, then it will have 360 added to it until is is above 0
  * @param 	toEncode a float array to to encode (representing a value from 0 - 360 degrees)
  * @param  encoded a byte array of size representing the float value (must have 2x the number of bytes available) 
  * @retval None
  */
void encodeDegreeArray(float toEncode[], uint8_t*destination, int size){
	for(int i = 0; i < size; i++){
		encodeFloatDegree(toEncode[i], &destination[2*i]);
	}
}

/**
  * @brief  transmits a roll and pitch array, each of size size floats, with one byte at the beginning to indicate that it is roll and pitch being transmitted, then all the rolls, then all the pitches
  * @param 	roll
  * @param  pitch
	* @param  size number of data points for each
  * @retval None
  */
void transmitFreakinHugeRollAndPitchArrays(float roll[], float pitch[], int size){
	if(size > TX_BUFFER_SIZE){
		printf("TOO MANY DATA POINTS FOR THIS TRANSFER (roll pitch)\n");
		size = TX_BUFFER_SIZE;
	}
	txBuffer[0] = TRANSMISSION_TYPE_ROLLPITCH; //identify type of data being transmitted
	encodeDegreeArray(roll, (txBuffer+1), size);//convert roll and pitch to byte[] and add to transmission mesage
	encodeDegreeArray(pitch, (txBuffer+(2*size)+1), size);
	HAL_StatusTypeDef ret = HAL_UART_Transmit(&uart_handle, txBuffer, size, TIMEOUT);
	if(HAL_OK != ret){
		printf("transmission of ridiculously huge roll and pitch failed. Time to cry now\n");
  }
}

//TODO I want to test to make sure this is fine, so for the watch
uint16_t twoBytesEnough = 0;
// The ADC is 12 bit resolution, but ADC get value returns a uint32... 2 bytes should be enough

/**
  * @brief  transmits an array of ints (which in this case represent raw ADC readings from the mic) 
  * @param 	audio array of readings to transmit. Each value should be less than uint16 max
  * @param  size number of data points
  * @retval None
  */
void transmitFreakinHugeAudioArray(uint32_t audio[], int size){
	if(size > TX_BUFFER_SIZE){
		printf("TOO MANY DATA POINTS FOR THIS TRANSFER (audio)\n");
		size = TX_BUFFER_SIZE;
	}
	txBuffer[0] = TRANSMISSION_TYPE_AUDIO; //identify type of data being transmitted

	for(int i = 0; i < size; i++){
		twoBytesEnough = audio[i];
		memcpy(&txBuffer[(2*i)+1], &twoBytesEnough, 2);		
	}
	HAL_StatusTypeDef ret = HAL_UART_Transmit(&uart_handle, txBuffer, size, TIMEOUT);
	if(HAL_OK != ret){
		printf("transmission of ridiculously huge audio array. Time to cry now\n");
  }
}

/**
  * @brief  calls HAL_UART_Receive and returns the value received as an int
  * @param 	none
  * @retval integer value received
*/
int receiveResponseInt(void){
	if(HAL_OK != HAL_UART_Receive(&uart_handle, rxBuffer, 1, TIMEOUT)){
		    printf("There was no answer. There was much sadness \n");
		return 0;
		
	}
	else{
		return rxBuffer[0];
	}
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


