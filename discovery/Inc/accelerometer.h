#ifndef __ACCELEROMETER_H
#define __ACCELEROMETER_H
#define ACCELERATION_BUFFER_SIZE 1000
float calcPitch(float x, float y, float z);
float calcRoll(float x, float y, float z);
void readAccelerometer(void);
int detectTap(void);
void FIR_C(float Input, float*Output);
int storeAccelValues(void);
void accelerometer_init(void);
void resetAccelIndex(void);
int howManyTaps(void);
#endif
