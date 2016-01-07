#include "NetCentricApp.h"
#include "mbed.h"

int main() {
    printf("Started NetCentric App\r\n");
    
    NetCentricApp app;
    
    USBInit();
    while (true) {
        USBLoop();
    }
}