package uva.nc.app;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import uva.nc.ServiceActivity;
import uva.nc.bluetooth.BluetoothService;
import uva.nc.bluetooth.MasterManager;
import uva.nc.bluetooth.SlaveManager;
import uva.nc.mbed.MbedManager;
import uva.nc.mbed.MbedRequest;
import uva.nc.mbed.MbedResponse;
import uva.nc.mbed.MbedService;


public class MainActivity extends ServiceActivity {

    private static final String TAG = MainActivity.class.getName();
    // ID's for commands on mBed.
    private static final int COMMAND_SUM = 1;
    private static final int COMMAND_AVG = 2;
    private static final int COMMAND_LED = 3;
    private static final int COMMAND_REQUEST_POTENTIO = 4;
    //TODO: implement following commands on the mBed:
    private static final int COMMAND_REQUEST_STATUS = 5;
    private static final int COMMAND_GOTO = 6;
    private static final int BT_COMMAND_LEDPARTY = 20;
    private static final int BT_COMMAND_POTENTIO = 22;
    private static final int BT_COMMAND_SET_POTENTIO = 23;
    private static final int BT_COMMAND_REQUEST_POTENTIO = 24;
    // Receiver implemented in separate class, see bottom of file.
    private final MainActivityReceiver receiver = new MainActivityReceiver();
    Singleton m_Inst = Singleton.getInstance();
    // BT Controls.
    private TextView listenerStatusText;
    private TextView ownAddressText;
    private TextView deviceCountText;
    private Button listenerButton;
    private Button devicesButton;
    private Button pingMasterButton;
    private Button pingSlavesButton;

    private Button ledpartySlavesButton;
    private Button potentioSlavesButton;

    // Selected slaves
    private Button selectDevicesButton;
    private LinearLayout selectedSlavesTextFrame;
    private TextView selectedSlavesText;

    // mBed controls.
    private TextView mbedConnectedText;
    private Button mbedSumButton;
    private Button mbedPotentioButton;
    private Button mbedLedButton;

    // Knobs
    private RelativeLayout knobMasterFrame;
    private RoundKnobButton knobMaster;
    private RelativeLayout knobSlaveFrame;
    private RoundKnobButton knobSlave;

    // Random data for sample events.
    private Random random = new Random();

    // Accessory to connect to when service is connected.
    private UsbAccessory toConnect;

    private boolean sendPotentioToMaster = false;

    private ArrayList<String> deselectedDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Inst.InitGUIFrame(this);
        setContentView(R.layout.activity_main);
        attachControls();

        // If this intent was started with an accessory, store it temporarily and clear once connected.
        UsbAccessory accessory = getIntent().getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
        if (accessory != null) {
            this.toConnect = accessory;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, receiver.getIntentFilter());
        refreshBluetoothControls();
        refreshMbedControls();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    @Override
    protected void onBluetoothReady(BluetoothService bluetooth) {
        refreshBluetoothControls();
    }

    @Override
    protected void onMbedReady(MbedService mbed) {
        if (toConnect != null) {
            mbed.manager.attach(toConnect);
            toConnect = null;
        }
        refreshMbedControls();
    }


    private void attachControls() {
        // Bluetooth controls.
        ownAddressText = (TextView)findViewById(R.id.own_address);
        listenerStatusText = (TextView)findViewById(R.id.listener_status);
        listenerButton = (Button)findViewById(R.id.listener);
        deviceCountText = (TextView)findViewById(R.id.device_count);
        devicesButton = (Button)findViewById(R.id.devices);
        devicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launch = new Intent(MainActivity.this, DevicesActivity.class);
                startActivity(launch);
            }
        });
        mbedConnectedText = (TextView)findViewById(R.id.mbed_connected);
        pingMasterButton = (Button)findViewById(R.id.ping_master);
        pingMasterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothService bluetooth = getBluetooth();
                if (bluetooth != null) {
                    bluetooth.slave.sendToMaster(random.nextInt(2500));
                }
            }
        });
        pingSlavesButton = (Button)findViewById(R.id.ping_slaves);
        pingSlavesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothService bluetooth = getBluetooth();
                if (bluetooth != null) {
                    bluetooth.master.sendToAll(random.nextInt(10000) + 5000);
                }
            }
        });
        ledpartySlavesButton = (Button)findViewById(R.id.ledparty_slaves);
        ledpartySlavesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothService bluetoothService = getBluetooth();
                if (bluetoothService != null) {
                    bluetoothService.master.sendToDevicesExcludingAddresses(deselectedDevices, new BluetoothObject(BT_COMMAND_LEDPARTY, new float[]{}));
                }
            }
        });
        potentioSlavesButton = (Button)findViewById(R.id.potentio_slaves);
        potentioSlavesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothService bluetoothService = getBluetooth();
                if (bluetoothService != null) {
                    bluetoothService.master.sendToDevicesExcludingAddresses(deselectedDevices, new BluetoothObject(BT_COMMAND_REQUEST_POTENTIO, null));
                }
            }
        });
        selectDevicesButton = (Button)findViewById(R.id.selectdevices_slaves);
        selectDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectSlavesDialog newFragment = new SelectSlavesDialog();
                BluetoothService bluetoothService = getBluetooth();
                if (bluetoothService != null) {
                    newFragment.setMasterManager(bluetoothService.master);
                    newFragment.setDeselectedDevices(deselectedDevices);
                    newFragment.setCallback(new SelectSlavesDialog.FragmentCallbacks() {
                        @Override
                        public void SelectedDevicesUpdated(ArrayList<String> devices) {
                            deselectedDevices = devices;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshBluetoothControls();
                                }
                            });
                        }
                    });
                }
                newFragment.show(getFragmentManager(), "selectslaves");
            }
        });
        selectedSlavesTextFrame = (LinearLayout)findViewById(R.id.selected_slaves_text);
        selectedSlavesText = (TextView)findViewById(R.id.selected_slaves_count);


        // mBed controls.
        mbedSumButton = (Button)findViewById(R.id.mbed_sum);
        mbedSumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float[] args = getRandomFloatArray(10);
                toastShort("Sum of: \n" + Arrays.toString(args));
                getMbed().manager.write(new MbedRequest(COMMAND_SUM, args));
            }
        });
        mbedPotentioButton = (Button)findViewById(R.id.mbed_potentio);
        mbedPotentioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float[] args = new float[0];
                toastShort("Requesting potentio:\n");
                getMbed().manager.write(new MbedRequest(COMMAND_REQUEST_POTENTIO, args));
            }
        });
        mbedLedButton = (Button)findViewById(R.id.mbed_led);
        mbedLedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float[] args = getRandomLedArray();
                getMbed().manager.write(new MbedRequest(COMMAND_LED, args));
            }
        });

        createKnobs();
    }

    private void createKnobs() {


        //create master knob
        knobMaster = new RoundKnobButton(this, R.drawable.stator, R.drawable.rotoron, R.drawable.rotoroff,
                m_Inst.Scale(250), m_Inst.Scale(250));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        knobMaster.SetListener(new RoundKnobButton.RoundKnobButtonListener() {
            @Override
            public void onStateChange(boolean newstate) {

            }

            @Override
            public void onRotate(int percentage) {
                BluetoothService bluetoothService = getBluetooth();

                if (bluetoothService != null) {
                    bluetoothService.master.sendToDevicesExcludingAddresses(deselectedDevices, new BluetoothObject(BT_COMMAND_SET_POTENTIO, new float[] { percentage / 10f }));
                }
            }
        });
        knobMasterFrame = (RelativeLayout)findViewById(R.id.knob_frame_master);
        knobMasterFrame.addView(knobMaster, lp);

        //create slave knob
        knobSlave = new RoundKnobButton(this, R.drawable.stator, R.drawable.rotoron, R.drawable.rotoroff,
                m_Inst.Scale(250), m_Inst.Scale(250));
        knobSlaveFrame = (RelativeLayout)findViewById(R.id.knob_frame_slave);
        knobSlaveFrame.addView(knobSlave, lp);

        //TODO: set correct initial value
        knobSlave.setRotorPercentage(50);
        knobSlave.SetListener(new RoundKnobButton.RoundKnobButtonListener() {
            public void onStateChange(boolean newstate) {
                Toast.makeText(MainActivity.this,  "New state:"+newstate,  Toast.LENGTH_SHORT).show();
            }

            public void onRotate(final int percentage) {
                if(getMbed().manager.areChannelsOpen()) {
                    getMbed().manager.write(new MbedRequest(COMMAND_GOTO, new float[]{percentage / 10f}));
                }
            }
        });
    }

    private void refreshBluetoothControls() {
        String slaveStatus = "Status not available";
        String slaveButton = "Start listening";
        String ownAddress = "Not available";
        int devConnected = 0;
        int devSelected = 0;
        boolean slaveButtonEnabled = false;
        boolean devicesButtonEnabled = false;
        boolean allowPingMaster = false;
        boolean allowPingSlaves = false;

        // Well it's not pretty, but it (barely) avoids duplicate logic.
        final BluetoothService bluetooth = getBluetooth();
        if (bluetooth != null) {
            slaveButtonEnabled = true;
            devicesButtonEnabled = true;
            ownAddress = bluetooth.utility.getOwnAddress();

            devConnected = bluetooth.master.countConnected();
            if (devConnected > 0) {
                allowPingSlaves = true;
            }
            devSelected = bluetooth.master.countConnectedWithoutAddresses(deselectedDevices);

            if (bluetooth.slave.isConnected()) {
                slaveStatus = "Connected to " + bluetooth.slave.getRemoteDevice();
                slaveButton = "Disconnect";
                allowPingMaster = true;
                listenerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bluetooth.slave.disconnect();
                    }
                });
            } else if (bluetooth.slave.isListening()) {
                slaveStatus = "Waiting for connection";
                slaveButton = "Stop listening";
                listenerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bluetooth.slave.stopAcceptOne();
                    }
                });
            } else {
                slaveStatus = "Not listening";
                slaveButton = "Start listening";
                listenerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!bluetooth.utility.isDiscoverable()) {
                            bluetooth.utility.setDiscoverable();
                        }
                        bluetooth.slave.startAcceptOne();
                    }
                });
            }
        }

        listenerStatusText.setText(slaveStatus);
        listenerButton.setText(slaveButton);
        listenerButton.setEnabled(slaveButtonEnabled);
        ownAddressText.setText(ownAddress);
        deviceCountText.setText(String.valueOf(devConnected));
        devicesButton.setEnabled(devicesButtonEnabled);
        pingMasterButton.setEnabled(allowPingMaster);
        pingSlavesButton.setEnabled(allowPingSlaves);
        knobMasterFrame.setVisibility(devConnected > 0 ? View.VISIBLE : View.GONE);

        ledpartySlavesButton.setEnabled(devConnected > 0);
        potentioSlavesButton.setEnabled(devConnected > 0);
        selectDevicesButton.setEnabled(devConnected > 0);

        if (devConnected > 0) {
            selectedSlavesTextFrame.setVisibility(View.VISIBLE);
            if (devSelected < devConnected) {
                selectedSlavesText.setText(String.valueOf(devSelected));
            } else {
                selectedSlavesText.setText("all");
            }
        } else {
            selectedSlavesTextFrame.setVisibility(View.GONE);
        }
    }

    private void refreshMbedControls() {
        String connText = getString(R.string.not_connected); // if you want to localize
        boolean enableButtons = false;

        MbedService mbed = getMbed();
        if (mbed != null && mbed.manager.areChannelsOpen()) {
            connText = getString(R.string.connected);
            enableButtons = true;
        }

        mbedConnectedText.setText(connText);
        mbedPotentioButton.setEnabled(enableButtons);
        mbedSumButton.setEnabled(enableButtons);
        mbedLedButton.setEnabled(enableButtons);
        knobSlaveFrame.setVisibility(enableButtons ? View.VISIBLE : View.GONE);
    }


    // mBed random data.
    private float[] getRandomFloatArray(int maxLength) {
        if (maxLength < 1) {
            maxLength = 1;
        }

        int length = random.nextInt(maxLength + 1);
        float[] arr = new float[length];
        for (int i = 0; i < length; i++) {
            arr[i] = random.nextFloat();
        }

        return arr;
    }

    private float[] getRandomLedArray() {
        float[] arr = new float[4];
        for (int i = 0; i < 4; i++) {
            arr[i] = random.nextBoolean() ? 0.0f : 1.0f;
        }
        return arr;
    }


    // Broadcast receiver which handles incoming events. If it were smaller, inline it.
    private class MainActivityReceiver extends BroadcastReceiver {

        // Refresh BT controls on these events.
        private final String BLUETOOTH_REFRESH_ON[] = { MasterManager.DEVICE_ADDED,
                                                        MasterManager.DEVICE_REMOVED,
                                                        MasterManager.DEVICE_STATE_CHANGED,
                                                        SlaveManager.LISTENER_CONNECTED,
                                                        SlaveManager.LISTENER_DISCONNECTED,
                                                        SlaveManager.STARTED_LISTENING,
                                                        SlaveManager.STOPPED_LISTENING };

        private final String MBED_REFRESH_ON[] = {      MbedManager.DEVICE_ATTACHED,
                                                        MbedManager.DEVICE_DETACHED };


        // Returns intents this receiver responds to.
        protected IntentFilter getIntentFilter() {
            IntentFilter filter = new IntentFilter();

            // Notification updates.
            for (String action : BLUETOOTH_REFRESH_ON) {
                filter.addAction(action);
            }
            for (String action : MBED_REFRESH_ON) {
                filter.addAction(action);
            }

            // Data received events.
            filter.addAction(MbedManager.DATA_READ);
            filter.addAction(MasterManager.DEVICE_RECEIVED);
            filter.addAction(SlaveManager.LISTENER_RECEIVED);

            return filter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Refresh on most Bluetooth or mBed events.
            for (String update : BLUETOOTH_REFRESH_ON) {
                if (action.equals(update)) {
                    refreshBluetoothControls();
                    break;
                }
            }
            for (String update : MBED_REFRESH_ON) {
                if (action.equals(update)) {
                    refreshMbedControls();
                    break;
                }
            }

            // Process received data.
            if (action.equals(SlaveManager.LISTENER_RECEIVED)) {

                // Slave received data from master.
                Serializable obj = intent.getSerializableExtra(SlaveManager.EXTRA_OBJECT);
                if (obj != null) {
                    if (obj instanceof BluetoothObject) {
                        BluetoothObject btobj = (BluetoothObject)obj;
                        if (btobj.getCommand() == BT_COMMAND_LEDPARTY) {
                            toastShort("received ledparty from master\n");
                            if (getMbed().manager.areChannelsOpen()) {
                                float[] args = getRandomLedArray();
                                getMbed().manager.write(new MbedRequest(COMMAND_LED, args));
                            }
                        } else if (btobj.getCommand() == BT_COMMAND_REQUEST_POTENTIO) {
                            toastShort("received potentiorequest from master\n");
                            if (getMbed().manager.areChannelsOpen()) {
                                sendPotentioToMaster = true;
                                getMbed().manager.write(new MbedRequest(COMMAND_REQUEST_POTENTIO, new float[] {}));
                            }
                        } else if (btobj.getCommand() == BT_COMMAND_SET_POTENTIO && btobj.getData().length == 1) {
                            toastShort("set potentio from master\n");
                            if (getMbed().manager.areChannelsOpen()) {
                                getMbed().manager.write(new MbedRequest(COMMAND_GOTO, btobj.getData()));
                            }
                        }
                        else {
                            toastShort("received unknown btobject from master\n");
                        }
                    }
                    else {
                        toastShort("From master:\n" + String.valueOf(obj));
                    }
                } else {
                    toastShort("From master:\nnull");
                }
            } else if (action.equals(MasterManager.DEVICE_RECEIVED)) {

                // Master received data from slave.
                Serializable obj = intent.getSerializableExtra(MasterManager.EXTRA_OBJECT);
                BluetoothDevice device = intent.getParcelableExtra(MasterManager.EXTRA_DEVICE);
                if (obj != null) {
                    if (obj instanceof BluetoothObject) {
                        BluetoothObject btobj = (BluetoothObject) obj;
                        if (btobj.getCommand() == BT_COMMAND_POTENTIO && btobj.getData().length == 1) {
                            float percentage = btobj.getData()[0];
                            knobMaster.setRotorPercentage((int)(percentage * 10f));
                            toastShort("Potentio of " + device + " is " + String.valueOf(percentage) + "\n");
                        }
                        else {
                            toastShort("unknown BluetoothObject from " + device + "\n");
                        }
                    } else {
                        toastShort("From " + device + "\n" + String.valueOf(obj));
                    }
                } else {
                    toastShort("From " + device + "\nnull!");
                }
            } else if (action.equals(MbedManager.DATA_READ)) {

                // mBed data received.
                MbedResponse response = intent.getParcelableExtra(MbedManager.EXTRA_DATA);
                if (response != null) {
                    // Errors handled as separate case, but this is just sample code.
                    if (response.hasError()) {
                        toastLong("Error! " + response);
                        return;
                    }

                    float[] values = response.getValues();
                    if (response.getCommandId() == COMMAND_AVG) {
                        if (values == null || values.length != 1) {
                            toastShort("Error!");
                        } else {
                            toastShort("AVG: " + String.valueOf(values[0]));
                        }
                    } else if (response.getCommandId() == COMMAND_SUM) {
                        if (values == null || values.length != 1) {
                            toastShort("Error!");
                        } else {
                            toastShort("SUM: " + String.valueOf(values[0]));
                        }
                    } else if (response.getCommandId() == COMMAND_REQUEST_POTENTIO) {
                        if (values == null || values.length != 1) {
                            toastShort("error");
                        }
                        else {
                            toastShort("current potentio: " + String.valueOf(values[0]));
                            if (knobSlave != null) {
                                knobSlave.setRotorPercentage((int) (values[0] * 10));
                            }
                            if (sendPotentioToMaster) {
                                sendPotentioToMaster = false;
                                BluetoothService bluetoothService = getBluetooth();
                                if (bluetoothService != null) {
                                    bluetoothService.slave.sendToMaster(new BluetoothObject(BT_COMMAND_POTENTIO, values));
                                }
                            }
                        }
                    } else if (response.getCommandId() == COMMAND_GOTO) {
                        toastShort("succesful potentio change.");
                    }
                }
            }
        }
    }

}
