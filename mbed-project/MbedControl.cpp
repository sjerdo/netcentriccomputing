#include "MbedControl.h"
#include "mbed.h"
#include "HBridge.h"
#include <math.h>       /* log */

AnalogIn pot(p16);   /* Potentiometer middle pin connected to p16, other two ends connected to GND and 3.3V */
HBridge motor(p28, p27, p21);
//
float getRealPotentioValue(float input)
{
    // if input is less than 0.165, use abc-formula for calculating the potentio
    if (input < 0.165) {
        return abc_getX(input);
    }
    
    // 0.0158139 e^(0.41411 x)
    input = input / 0.0158139;
    input = log(input);
    input = input / 0.41411;
    return input;
}

float getCurrentValue() {
    return getRealPotentioValue(pot.read());
}

void goToValue(float destination) {
    int seconds = 0.0;
    if (destination < getCurrentValue()) {
        motor.backward();
        while(destination < getCurrentValue() && seconds < 6.5) {
            wait(0.1);
            seconds += 0.1;
        }
    } else {
        motor.forward();
        while(destination > getCurrentValue() && seconds < 6.5) {
            wait(0.1);
            seconds += 0.1;
        }
    }
    motor.stop();
}

float abc_getX(float input) {
    // 0.00599767 x^2 - 0.000904196 x - 0.00289697 = input
    float a = 0.00599767;
    float b = 0.000904196;
    float c = 0.00289697 - input;
    float determinant = b*b - 4*a*c;
    float x1, x2;
    
    if (determinant > 0) {
        x1 = (-b + sqrt(determinant)) / (2*a);
        x2 = (-b - sqrt(determinant)) / (2*a);
        if (x1 < 0) {
            return x2;
        } else {
            if (x2 < x1) {
                return x1;        
            } else {
                return x2;
            }
        }
    } else if (determinant == 0) {
        x1 = (-b + sqrt(determinant)) / (2*a);
        return x1;
    }
    return -1;
}