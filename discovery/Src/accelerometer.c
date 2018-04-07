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
float accXWindow[10] = {0.0};
float accYWindow[10] = {0.0};
float accZWindow[10] = {0.0};
int windowSize = 10;

int accelIndex = 0;
#define bLength 5 //length of coefficient array for FIR filter
#define STORED_LENGTH 10 // length of stored output vector used in calculating RMS

	//for use by the FIR filter
	float fir_coefficients[bLength] = { 0.2, 0.2, 0.2, 0.2, 0.2 };
	float currentFilterWindow[bLength] = { 0, 0, 0, 0, 0 };

	//buffer for acceleration values
	float filteredAccX[ACCELERATION_BUFFER_SIZE] = {0.0};
	float filteredAccY[ACCELERATION_BUFFER_SIZE] = {0.0};
	float filteredAccZ[ACCELERATION_BUFFER_SIZE] = {0.0};
	float storedRoll[ACCELERATION_BUFFER_SIZE] = {0};
	float storedPitch[ACCELERATION_BUFFER_SIZE] = {0};

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
 void FIR_C(float Input, float*Output) {
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

void resetAccelIndex(void){
	accelIndex = 0;
}

int storeAccelValues(void){
	if(accelIndex >= ACCELERATION_BUFFER_SIZE ){
		printf("ACCELERATION BUFFER FULL OH NOS");
		return -1;
	}
	LIS3DSH_Read (&status, LIS3DSH_STATUS, 1);
	//The first four bits denote if we have new data on all XYZ axes, 
	//Z axis only, Y axis only or Z axis only. If any or all changed, proceed
	if ((status & 0x0F) != 0x00){
		LIS3DSH_ReadACC(&Buffer[0]);
		FIR_C((float)Buffer[0], &filteredAccX[accelIndex]);
		FIR_C((float)Buffer[1], &filteredAccY[accelIndex]);
		FIR_C((float)Buffer[2], &filteredAccZ[accelIndex]);

		storedPitch[accelIndex] = calcPitch(filteredAccX[accelIndex],filteredAccY[accelIndex],filteredAccZ[accelIndex]);
		storedRoll[accelIndex] = calcRoll(filteredAccX[accelIndex],filteredAccY[accelIndex],filteredAccZ[accelIndex]);
		accelIndex++;
	}
	return 0;
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
 
		//TODO can probably do this with a delay in the main loop instead
		//commenting out here, because for second tap detection, 200 ms between readings might not be enough - need to refresh the whole window 
//		if (MyFlag/10) //which means it's true every 0.2s
//		{
//			MyFlag = 0;
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
	//				printf("X: %4f     Y: %4f     Z: %4f	 \n", accX, accY, accZ);
				
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
//			}
	}

int detectTap(void){
	
  int tap = 0;
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
			printf("tap detected \n");
			tap = 1;	
	}
	return tap;
	
}

int howManyTaps(void){

	if (detectTap()){
		HAL_Delay(100); //TODO trying to delay 1ms
		for(int i = 0; i < 10; i++){ //TODO now need to refresh window contents, and make sure old values that flagged as a tap are gone before checking for a second tap 
			readAccelerometer();
			HAL_Delay(20); // 
		}
		for(int i = 0; i < 20; i++){ //TODO ok so with a delay of 200ms and 20 checks, a double tap must be detected within 2ish seconds
			readAccelerometer();
			if(detectTap()){
				return 2;
        }
			HAL_Delay(200);//new reading every 200ms, TODO select
      }
		return 1;
		}
	return 0;

}
	

