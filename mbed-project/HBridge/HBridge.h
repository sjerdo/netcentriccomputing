/**
* @author Giles Barton-Owen
*
* @section LICENSE
*
* Copyright (c) 2012 mbed
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*
* @section DESCRIPTION
*    A simple library to control an H-Bridge driver using two DIO and a PWM pin
*
*/ 


#ifndef HBRIDGERAW_H
#define HBRIDGERAW_H

#include "mbed.h"

/** A simple HBridge controller library
 */
class HBridge
{
    public:
    /** Creates an instance of the HBridge class
     *
     * @param A The pin the "A" line is connected to (naming taken from the L293D).
     * @param B The pin the "B" line is connected to (naming taken from the L293D).
     * @param en The pin the enable line is connected to.]
     */
    HBridge(PinName A, PinName B, PinName en);
    
    /** Hard stops the motor
     */
    void stop();
    
    /** Starts the motor
     */
    void start();
    
    /** Sets the PWM output of the enable pin and therefore the speed of the motor
     *
     * @param speed_in The float value of the desired speed between -1 and 1
     */
    void speed(float speed_in);
    
    /** Turn off and on the enable pin
     *
     * @param onoff Enable the motor or not (on(true))
     */
    void power(bool onoff);
    
    /** Set the pwm value of the enable pin
     *
     * @param power_in The value of the pwm enable pins 
     */
    void power(float power_in);
    
    /** Soft stop the motor
     */
    void soft_stop();
    
    /** Go forward, starts the motor
     */
    void forward();
    
    /** Go backward, starts the motor
     */
    void backward();
    
    /** Go forward, starts the motor
     *
     * @param speed_in The float value of the desired speed between 0 and 1
     */
    void forward(float speed_in);
    
    /** Go backward, starts the motor
     *
     * @param speed_in The float value of the desired speed between 0 and 1
     */
    void backward(float speed_in);
    
    /** Set A high or low
     *
     * @param highlow Set the high/low state of the A half-bridge (high(true))
     */
    void A(bool highlow);
    
    /** Set B high or low
     *
     * @param highlow Set the high/low state of the B half-bridge (high(true))
     */
    void B(bool highlow);
    
    /** Set the direction of travel
     * 
     * @param direction_in Forward = true
     */
    void direction(bool direction_in);
    
    
    private:
    PwmOut enable;
    DigitalOut Adrive;
    DigitalOut Bdrive;
    
    bool power_status;
    float speed_value;
    bool A_value;
    bool B_value;
    bool stored_direction;
    
    void set();
    
    
};



#endif