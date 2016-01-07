#ifndef __NETCENTRICAPP_H__
#define __NETCENTRICAPP_H__

#include "AndroidAccessory.h"
#include "mbed.h"
#include "MbedCommand.h"

#define READ_BUFF   2048
#define WRITE_BUFF  2048

#define NO_ERROR                    0
#define ERR_COMMAND_NOT_FOUND       1
#define ERR_POTENTIO_INVALID        2

#define COMMAND_SUM                 1
#define COMMAND_AVG                 2
#define COMMAND_LED                 3
#define COMMAND_REQUEST_POTENTIO    4
#define COMMAND_GOTO                6

class NetCentricApp : private AndroidAccessory {
    public:
        NetCentricApp():
            AndroidAccessory(READ_BUFF, WRITE_BUFF,
                                "ARM",
                                "mbed",
                                "mbed - NetCentric",
                                "0.2",
                                "http://www.uva.nl",
                                "0000000012345678")
            {};
        
    protected:
        virtual void setupDevice();
        virtual void resetDevice();
        virtual int callbackRead(u8 *buffer, int len);
        virtual int callbackWrite();
        
        MbedResponse *getResponse(MbedRequest *request);
    
    private:
        static float getFloat(u8 *buffer, int offset, int bufferLen);
        static int getInt(u8 *buffer, int offset, int bufferLen);
        static u8 getUByte(u8 *buffer, int offset, int bufferLen);
        
        float getRealValue(float input);
        
        MbedResponse *sumCommand(MbedRequest *request);
        MbedResponse *avgCommand(MbedRequest *request);
        MbedResponse *ledCommand(MbedRequest *request);
        MbedResponse *requestPotentioCommand(MbedRequest *request);
        MbedResponse *gotoPotentioCommand(MbedRequest *request);
};

#endif