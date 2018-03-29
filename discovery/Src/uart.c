#include "stm32f4xx_it.h"


UART_HandleTypeDef uart_handle;

void UART_Initialize(void)
{
	UART_InitTypeDef uart_init;
	
	__HAL_RCC_USART2_CLK_ENABLE();
	
	// Initialize TX
	uart_init.BaudRate = 115200;
	uart_init.WordLength = UART_WORDLENGTH_8B;
	uart_init.StopBits = UART_STOPBITS_1;
	uart_init.Parity = UART_PARITY_NONE;
	uart_init.HwFlowCtl = UART_HWCONTROL_NONE;
	uart_init.Mode = UART_MODE_TX_RX;
	uart_init.OverSampling = UART_OVERSAMPLING_16;
	
	uart_handle.Init = uart_init;
	uart_handle.Instance = USART2;
	
	if (HAL_UART_Init(&uart_handle) != HAL_OK)
	{
		//Print statements for possible errors. Errors to be determined see main 347
	}
	
	HAL_NVIC_SetPriority(USART2_IRQn, 0, 1);
  HAL_NVIC_EnableIRQ(USART2_IRQn);
}
