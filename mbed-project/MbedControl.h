#ifndef __MBED_CONTROL_H__
#define __MBED_CONTROL_H__

#include "mbed.h"

float getCurrentValue();
float getRealPotentioValue(float input);
void goToValue(float input);
float abc_getX(float input);

#endif