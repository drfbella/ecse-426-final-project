#include "accelerometer.h"
#include "lis3dsh.h"
#include "math.h"
#include "stdlib.h"
#include "main.h"

#define PI 3.14159265358979323846
//tap detection
#define THRESHOLD 25
#define NUM_CHECKS_FOR_TWO_TAPS	20
uint8_t status;
float Buffer[3];
float accX, accY, accZ;
int taps = 0;
float accXWindow[10] = {0.0};
float accYWindow[10] = {0.0};
float accZWindow[10] = {0.0};
int windowSize = 10;
extern int counter;


//for use by the FIR filter
#define bLength 5 //length of coefficient array for FIR filter
#define STORED_LENGTH 10 // length of stored output vector used in calculating RMS
float fir_coefficients[bLength] = { 0.2, 0.2, 0.2, 0.2, 0.2 };
float currentFilterWindow[bLength] = { 0, 0, 0, 0, 0 };


//index in roll and pitch arrays to store new values
int accelIndex = 0;	
//stored acceleration values
float storedRoll[ACCELERATION_BUFFER_SIZE] = {0};
float storedPitch[ACCELERATION_BUFFER_SIZE] = {0};


/**
  * @brief  Initializes the accelerometer to run at 100Hz in interrupt mode
  * @param  none
  * @retval none
*/	
void accelerometer_init(void) {

	/* initialise accelerometer */
	LIS3DSH_InitTypeDef Acc_InitDef;

	/* define field of the accelerometer initialisation structure */
	Acc_InitDef.Power_Mode_Output_DataRate = LIS3DSH_DATARATE_100;      									/* 100Hz */
	Acc_InitDef.Axes_Enable = LIS3DSH_XYZ_ENABLE;                     									/* XYZ */
	Acc_InitDef.Continous_Update = LIS3DSH_ContinousUpdate_Disabled;										/* continuous update */
	Acc_InitDef.AA_Filter_BW = LIS3DSH_AA_BW_50;																				/* 50Hz to filter gravity*/
	Acc_InitDef.Full_Scale = LIS3DSH_FULLSCALE_2;																				/* 2g */

	LIS3DSH_Init(&Acc_InitDef);
	
	
// initilalize accelerometer interupt
	LIS3DSH_DRYInterruptConfigTypeDef Acc_Interrupt_InitDef;

//define field of the accelerometer interupt initialisation structure
	Acc_Interrupt_InitDef.Dataready_Interrupt = LIS3DSH_DATA_READY_INTERRUPT_ENABLED;   							//enable
	Acc_Interrupt_InitDef.Interrupt_signal = LIS3DSH_ACTIVE_HIGH_INTERRUPT_SIGNAL;                  	//active high 
	Acc_Interrupt_InitDef.Interrupt_type = LIS3DSH_INTERRUPT_REQUEST_PULSED;                     			//pulse interupt 

	LIS3DSH_DataReadyInterruptConfig(&Acc_Interrupt_InitDef);

	//enable interrupt
	HAL_NVIC_EnableIRQ(EXTI0_IRQn);
	HAL_NVIC_SetPriority(EXTI0_IRQn, 0, 1);

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

/**
  * @brief  resets the roll and pitch storage
  * @param  none
  * @retval none
*/	
void resetAccelIndex(void){
	accelIndex = 0;
}

/**
  * @brief  reads the accelerometer and updates the the roll and pitch storage
  * @param  none
  * @retval -1 if the roll and pitch storage is full
*/
int storeAccelValues(void){
	if(accelIndex >= ACCELERATION_BUFFER_SIZE ){
	//	printf("ACCELERATION BUFFER FULL OH NOS\n");
		return -1;
	}
		float filteredX, filteredY, filteredZ;

		LIS3DSH_ReadACC(&Buffer[0]);
		FIR_C((float)Buffer[0], &filteredX);
		FIR_C((float)Buffer[1], &filteredY);
		FIR_C((float)Buffer[2], &filteredZ);

		storedPitch[accelIndex] = calcPitch(filteredX,filteredY,filteredZ);
		storedRoll[accelIndex] = calcRoll(filteredX,filteredY,filteredZ);
		accelIndex++;

	return 0;
}

/**
  * @brief  calculates pitch
  * @param  acceleration in the x direction
  * @param  acceleration in the y direction
  * @param  acceleration in the z direction
	* @retval pitch
*/
float calcPitch(float x, float y, float z){
float pitch = atan2(y,(sqrt(x*x + z*z)))* 180 / PI;
	return pitch;
}

/**
  * @brief  calculates roll
  * @param  acceleration in the x direction
  * @param  acceleration in the y direction
  * @param  acceleration in the z direction
	* @retval roll
*/
float calcRoll(float x, float y, float z){
float roll = atan2(-x,z) * 180 / PI;
	return roll;
}
/**
  * @brief  reads the accelerometer
  * @param  none
	* @retval none
*/
void readAccelerometer(void){
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
	}

/**
  * @brief  checks the accWindow for signs of a tap
  * @param  none
	* @retval 1 if a tap has been detected, 0 otherwise
*/
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

/**
  * @brief  checks the accWindow for signs of a tap. If one tap is detected, resets a tap counter and continues to check until it expires to determine if there has been a double tap
  * @param  none
	* @retval 1 if a tap has been detected but the counter has reached it's limit, 2 if two taps have been detected, 0 otherwise
*/
int howManyTaps(void){
	
	if (detectTap()){
		taps++;
		if(taps == 1){
			counter = 0;
		}
		else if (taps == 2){
			counter = 0;
			taps = 0;
			return 2;
		}
	}else if( taps ==1){
		if(counter > 100){
			counter = 0;
			taps = 0;
			return 1;
		}
	}
	return 0;
}
	

