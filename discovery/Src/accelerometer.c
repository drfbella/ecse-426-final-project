#include "accelerometer.h"
#include "lis3dsh.h"
#include "math.h"
#include "stdlib.h"
#include "main.h"

#define PI 3.14159265358979323846

uint8_t status;
float Buffer[3];
float accX, accY, accZ;
uint32_t MyFlag = 0;
float accValue[3] = {0,0,0};
extern int counter;
extern float accXWindow[];
extern float accYWindow[];
extern float accZWindow[];
extern int windowSize;
int tap = 0;
int tap2 = 0;
int z = 0;
#define bLength 5 //length of coefficient array for FIR filter
#define STORED_LENGTH 10 // length of stored output vector used in calculating RMS

//for use by the FIR filter
float fir_coefficients[bLength] = { 0.2, 0.2, 0.2, 0.2, 0.2 };
int currentFilterWindow[bLength] = { 0, 0, 0, 0, 0 };

	float accXphaseTwo[100] = {0.0};
	float accYphaseTwo[100] = {0.0};
	float accZphaseTwo[100] = {0.0};
	float filteredAccX[100] = {0.0};
	float filteredAccY[100] = {0.0};
	float filteredAccZ[100] = {0.0};

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
/**
  * @brief calculates the moving average based ont he values stored in the currentFilterWIndow and the new value passed as Input
	*					FIR filter of order 4 (from lab 1)
  * @param  Input new incoming value to be filtered
						Output pointer to location resultant float is to be stored
  * @retval void
 */
 void FIR_C(int Input, float*Output) {
	int i;

	//take window, shift, add input to the end
	for (i = bLength - 1; i > 0; i--) {
		currentFilterWindow[i] = currentFilterWindow[i - 1];
	}
	currentFilterWindow[0] = Input;

	//take weighted average
	for (i = 0; i < bLength; i++) {
		*Output = *Output + (float)currentFilterWindow[i] * fir_coefficients[i];
	}
}

void accForTenSec(void){
	MyFlag = 0;

	while (MyFlag <= 1000){
		LIS3DSH_Read (&status, LIS3DSH_STATUS, 1);
				//The first four bits denote if we have new data on all XYZ axes, 
		   	//Z axis only, Y axis only or Z axis only. If any or all changed, proceed
				if ((status & 0x0F) != 0x00)
				{
					LIS3DSH_ReadACC(&Buffer[0]);
					accXphaseTwo[z] = (float)Buffer[0];
					accYphaseTwo[z] = (float)Buffer[1];
					accZphaseTwo[z] = (float)Buffer[2];
			
					FIR_C(accXphaseTwo[z], &filteredAccX[z]);
					FIR_C(accYphaseTwo[z], &filteredAccY[z]);
					FIR_C(accZphaseTwo[z], &filteredAccZ[z]);
					
					z++;
				}
	}

}

float calcPitch(float x, float y, float z){
float pitch = atan2(y,(sqrt(x*x + z*z)))* 180 / PI;
	return pitch;
}

float calcRoll(float x, float y, float z){
float roll = atan2(-x,z) * 180 / PI;
	return roll;
}

void readAccelerometer(){

 
		if (MyFlag/10) //which means it's true every 0.2s
		{
			counter = counter + 1; //This counter is gonna count to about 200 until I care about the value of the accelerometer, to allow it to stabilize
			MyFlag = 0;
			//Reading the accelerometer status register
				LIS3DSH_Read (&status, LIS3DSH_STATUS, 1);
				//The first four bits denote if we have new data on all XYZ axes, 
		   	//Z axis only, Y axis only or Z axis only. If any or all changed, proceed
				if ((status & 0x0F) != 0x00)
				{
					LIS3DSH_ReadACC(&Buffer[0]);
					accX = (float)Buffer[0];
					accY = (float)Buffer[1];
					accZ = (float)Buffer[2];
					calcPitch (accX, accY, accZ);
					calcRoll (accX, accY, accZ);
					printf("X: %4f     Y: %4f     Z: %4f	 \n", accX, accY, accZ);
				
					//This block is implementing a sliding window and storing fetched values
				 for (int j = 0; j < windowSize -1; j++){
				 
						accXWindow[j] = accXWindow [j+1];
						accYWindow[j] = accYWindow [j+1];
						accZWindow[j] = accZWindow [j+1];
				 }
				 
						accXWindow[windowSize-1] = accX;
						accYWindow[windowSize-1] = accY;
						accZWindow[windowSize-1] = accZ;
				}
			}
	}

int detectTap(void){
	
	/*Accelerometer data changes most notably in the y axis upon tap
		This could be optimized but say we detext a tap if there's a change of 25mm/s^2 */
	
		float largeY = -50000.0;
		float smallY = 50000.0;
		
		for (int j = 0; j < windowSize; j++){
			
			//This way, I'm always storing the current value of acc
			if (accYWindow[j] > largeY){
			largeY = accYWindow[j];
			}
			
			if (accYWindow[j] < smallY){
			smallY = accYWindow[j];
			}
		}
			if ((fabs(largeY) - fabs(smallY) > 25)){
			
			tap = 1;
			
		}
			return tap;
	
	}
	
	int detect2Tap(void){
		
		float largeY = -50000.0;
		float smallY = 50000.0;
		
		for (int j = 0; j < windowSize; j++){
			
			//This way, I'm always storing the current value of acc
			if (accYWindow[j] > largeY){
			largeY = accYWindow[j];
			}
			
			if (accYWindow[j] < smallY){
			smallY = accYWindow[j];
			}
		}
			if ((fabs(largeY) - fabs(smallY) > 25)){
			
			tap2 = 1;
			
		}
			return tap2;
	
	}

