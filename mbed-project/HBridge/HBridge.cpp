#include "HBridge.h"

HBridge::HBridge(PinName A, PinName B, PinName en): enable(en),Adrive(A),Bdrive(B)
{
    speed_value = 1;
    power_status = false;
    A_value = false;
    B_value = false;
    stored_direction = true;
    set();
}
    
void HBridge::stop()
{
    power_status = true;
    A_value = false;
    B_value = false;
    set();
}

void HBridge::start()
{
    direction(true);
    power_status = true;
    set();
}

void HBridge::power(float power_in)
{
    if(power_in <= 0)
    {
        speed_value = 0;
    }
    else
    {
        speed_value = power_in;
    }
    set();
}

void HBridge::speed(float speed_in)
{
    if(speed_in < 0)
    {
        power(speed_in * -1);
        direction(false);
    }
    else
    {
        power(speed_in);
        direction(true);
    }
}

void HBridge::power(bool onoff)
{
    power_status = onoff;
    set();
}

void HBridge::soft_stop()
{
    power_status = false;
    set();
}


void HBridge::forward()
{
    direction(true);
    power(true);
}


void HBridge::backward()
{
    direction(false);
    power(true);
}


void HBridge::forward(float speed_in)
{
    speed(speed_in);
    forward();
}


void HBridge::backward(float speed_in)
{
    speed(speed_in);
    backward();
}

void HBridge::direction(bool direction_in)
{
    stored_direction = direction_in;
    A_value = stored_direction;
    B_value = !stored_direction;
    set();
}

void HBridge::A(bool highlow)
{
    A_value = highlow;
}

void HBridge::B(bool highlow)
{
    B_value = highlow;
}

void HBridge::set()
{
    Adrive = A_value & power_status;
    Bdrive = B_value & power_status;
    enable = speed_value * float(power_status);
}