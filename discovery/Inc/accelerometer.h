#ifndef __ACCELEROMETER_H
#define __ACCELEROMETER_H

float calcPitch(float x, float y, float z);
float calcRoll(float x, float y, float z);
void readAccelerometer(void);
int detectTap(void);
int detect2Tap(void);

#endif
