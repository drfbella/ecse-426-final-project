#ifndef __ACCELEROMETER_H
#define __ACCELEROMETER_H

float calcPitch(float x, float y, float z);
float calcRoll(float x, float y, float z);
void readAccelerometer(void);
int detectTap(void);
void FIR_C(int Input, float*Output);
void accForTenSec(void);
void accelerometer_init(void);
#endif
