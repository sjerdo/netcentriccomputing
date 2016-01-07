#include "NetCentricApp.h"
#include "MbedCommand.h"
#include "MbedControl.h"
#include <math.h>       /* log */

// Process commands here.
MbedResponse *NetCentricApp::getResponse(MbedRequest *request) {
    if (request->commandId == COMMAND_SUM) {
        return sumCommand(request);
    } else if (request->commandId == COMMAND_AVG) {
        return avgCommand(request);
    } else if (request->commandId == COMMAND_LED) {
        return ledCommand(request);
    } else if (request->commandId == COMMAND_REQUEST_POTENTIO) {
        return requestPotentioCommand(request);
    } else if (request->commandId == COMMAND_GOTO) {
        return gotoPotentioCommand(request);   
    }
    
    MbedResponse *commandNotFound = new MbedResponse();
    commandNotFound->requestId = request->id;
    commandNotFound->commandId = request->commandId;
    commandNotFound->error = ERR_COMMAND_NOT_FOUND;
    commandNotFound->n = 0;
    commandNotFound->values = NULL;
    
    return commandNotFound;
}

// Sample commands.
MbedResponse *NetCentricApp::sumCommand(MbedRequest *request) {
    float sum = 0.0f;
    for (int i = 0; i < request->n; i++) {
        sum += request->args[i];
    }
    
    MbedResponse *r = new MbedResponse();
    r->requestId = request->id;
    r->commandId = request->commandId;
    r->error = NO_ERROR;
    r->n = 1;
    r->values = new float[1];
    r->values[0] = sum;
    return r;
}

MbedResponse *NetCentricApp::avgCommand(MbedRequest *request) {
    float sum = 0.0f;
    for (int i = 0; i < request->n; i++) {
        sum += request->args[i];
    }
    
    MbedResponse *r = new MbedResponse();
    r->requestId = request->id;
    r->commandId = request->commandId;
    r->error = NO_ERROR;
    r->n = 1;
    r->values = new float[1];
    
    if (request->n > 0) {
        r->values[0] = sum / request->n;
    } else {
        r->values[0] = sum;
    }
    return r;
}

// Control the LED's.
MbedResponse *NetCentricApp::ledCommand(MbedRequest *request) {
    DigitalOut led1(LED1);
    DigitalOut led2(LED2);
    DigitalOut led3(LED3);
    DigitalOut led4(LED4);
    
    if (request->n > 0) led1 = request->args[0];
    if (request->n > 1) led2 = request->args[1];
    if (request->n > 2) led3 = request->args[2];
    if (request->n > 3) led4 = request->args[3];
    
    MbedResponse *r = new MbedResponse();
    r->requestId = request->id;
    r->commandId = request->commandId;
    r->error = NO_ERROR;
    r->n = 4;
    r->values = new float[4];
    r->values[0] = led1;
    r->values[1] = led2;
    r->values[2] = led3;
    r->values[3] = led4;
    
    return r;
}

MbedResponse *NetCentricApp::requestPotentioCommand(MbedRequest *request) {
    MbedResponse *r = new MbedResponse();
    r->requestId = request->id;
    r->commandId = request->commandId;
    r->error = NO_ERROR;
    r->n = 1;
    r->values = new float[1];
    r->values[0] = getCurrentValue();
    
    return r;
}

MbedResponse *NetCentricApp::gotoPotentioCommand(MbedRequest *request) {
    float ain = -1;
    if (request->n > 0) ain = request->args[0];
    
    MbedResponse *r = new MbedResponse();
    r->requestId = request->id;
    r->commandId = request->commandId;
    if (ain >=0 && ain <= 10) {
        goToValue(ain);
        r->error = NO_ERROR;
        r->n = 1;
        r->values = new float[1];
        r->values[0] = getCurrentValue();
    } else {
        r->error = ERR_POTENTIO_INVALID;
        r->n = 0;
    }
    
    return r;
}

// Setup once a device is connected.
void NetCentricApp::setupDevice() {
    printf("Connected to Android!\r\n");
}

// Called on disconnect.
void NetCentricApp::resetDevice() {
    printf("Disconnected\r\n");
}


// Construction of requests.
int NetCentricApp::callbackRead(u8 *buffer, int len) {
    if (len > 0) {
        // Parse request, format:
        //  int     - request ID
        //  int     - command ID
        //  ubyte   - # args
        //  float[] -- args
        
        // Note len is fixed as the packet is always equally big. Don't try to use
        // packets of variable size, the smallest size of a encountered packet is 
        // used.
        
        MbedRequest *request = new MbedRequest();
        
        request->id = getInt(buffer, 0, len);
        request->commandId = getInt(buffer, 4, len);
        request->n = getInt(buffer, 8, len);
        request->args = NULL;
        
        printf("request: %i, command: %i, n-args: %i\r\n", request->id, request->commandId, request->n);
        
        int n = request->n;
        if (n > 0) {
            request->args = new float[n];
            for (int i = 0; i < n; i++) {
                int offset = 12 + (i * 4);
                float f = getFloat(buffer, offset, len);
                request->args[i] = f;
            }
        }
        
        // Construct and send response.
        MbedResponse *response = getResponse(request);
        int responseSize = 4 + 4 + 4 + 4 + (response->n*4);
        u8 responseBuffer[responseSize];
        
        memcpy(responseBuffer + 0, reinterpret_cast<u8 const *>(&response->requestId), 4);
        memcpy(responseBuffer + 4, reinterpret_cast<u8 const *>(&response->commandId), 4);
        memcpy(responseBuffer + 8, reinterpret_cast<u8 const *>(&response->error), 4);
        memcpy(responseBuffer + 12, reinterpret_cast<u8 const *>(&response->n), 4);
        if (response->n > 0) {
            for (int i = 0; i < response->n; i++)  {
                float f = response->values[i];
                memcpy(responseBuffer + 16 + i*4, reinterpret_cast<u8 const *>(&f), 4);
            }
            
        }
        
        write(responseBuffer, responseSize);
        
        // Clean up.
        if (request->n > 0) {
            delete[] request->args;
        }
        delete request;
        
        if (response->n > 0) {
            delete[] response->values;
        }
        delete response;
    }
    
    return 0;
}

// Called to confirm a write operation.
int NetCentricApp::callbackWrite() {
    return 0;
}


/* Unsigned byte to primitives. Little endian assumed, Java sends Big endian by default. */
float NetCentricApp::getFloat(u8 *buffer, int offset, int bufferLen) {
    if (offset + 3 > bufferLen) {
        printf("float index out of bounds!\r\n");
        return 0.0;
    }
        
    float f;
    memcpy(&f, buffer + offset, sizeof(f));
    return f;
}

int NetCentricApp::getInt(u8 *buffer, int offset, int bufferLen) {
    if (offset + 3 > bufferLen) {
        printf("int index out of bounds!\r\n");
        return 0;
    }
        
    int i;
    memcpy(&i, buffer + offset, sizeof(i));
    return i;
}

u8 NetCentricApp::getUByte(u8 *buffer, int offset, int bufferLen) {
    if (offset > bufferLen) {
        printf("byte index out of bounds!\r\n");
        return 0;
    }
    
    u8 b;
    memcpy(&b, buffer + offset, sizeof(b));
    return b;
}