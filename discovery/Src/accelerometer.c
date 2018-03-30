#include "accelerometer.h"
#include "lis3dsh.h"
#include "math.h"
#include "stdlib.h"
#include "main.h"

#define PI 3.14159265358979323846

void accelerometer_init(void) {

	/* initialise accelerometer */
	LIS3DSH_InitTypeDef Acc_InitDef;

	/* define field of the accelerometer initialisation structure */
	Acc_InitDef.Power_Mode_Output_DataRate = LIS3DSH_DATARATE_25;      									/* 25Hz */
	Acc_InitDef.Axes_Enable = LIS3DSH_XYZ_ENABLE;                     									/* XYZ */
	Acc_InitDef.Continous_Update = LIS3DSH_ContinousUpdate_Disabled;										/* continuous update */
	Acc_InitDef.AA_Filter_BW = LIS3DSH_AA_BW_50;																				/* 50Hz to filter gravity*/
	Acc_InitDef.Full_Scale = LIS3DSH_FULLSCALE_2;																				/* 2g */

	LIS3DSH_Init(&Acc_InitDef);
	
/*If we're doing this with an interrupt, also would need to declare this in 32f4xx_it.c
	
// initilalize accelerometer interupt
	LIS3DSH_DRYInterruptConfigTypeDef Acc_Interrupt_InitDef;

//define field of the accelerometer interupt initialisation structure
	Acc_Interrupt_InitDef.Dataready_Interrupt = LIS3DSH_DATA_READY_INTERRUPT_ENABLED;   							//enable
	Acc_Interrupt_InitDef.Interrupt_signal = LIS3DSH_ACTIVE_HIGH_INTERRUPT_SIGNAL;                  	//active high 
	Acc_Interrupt_InitDef.Interrupt_type = LIS3DSH_INTERRUPT_REQUEST_PULSED;                     			//pulse interupt 

	LIS3DSH_DataReadyInterruptConfig(&Acc_Interrupt_InitDef);

	HAL_NVIC_EnableIRQ(EXTI0_IRQn);
	HAL_NVIC_SetPriority(EXTI0_IRQn, 0, 1);*/

}

float calcPitch(float x, float y, float z){
float pitch = atan2(y,(sqrt(x*x + z*z)))* 180 / PI;
	return pitch;
}

float calcRoll(float x, float y, float z){
float roll = atan2(-x,z) * 180 / PI;
	return roll;
}
