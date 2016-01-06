package uva.nc.app;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import uva.nc.bluetooth.BluetoothService;
import uva.nc.bluetooth.DeviceState;
import uva.nc.bluetooth.MasterManager;

/**
 * Created by Sjoerd on 6-1-2016.
 */

public class SelectSlavesDialog extends DialogFragment {

    private MasterManager masterManager;
    private String[] devicesArr;
    private boolean[] checkedItems;
    private ArrayList<String> connectedDevices;
    private ArrayList<String> selectedDevices;

    public void setMasterManager(MasterManager masterManager) {
        this.masterManager = masterManager;
    }

    public void setSelectedDevices(ArrayList<String> selectedDevices) {
        this.selectedDevices = selectedDevices;
    }

    private String[] getDevices() {
        ArrayList<BluetoothDevice> deviceList = masterManager.getDeviceList();
        connectedDevices = new ArrayList<>();
        for (BluetoothDevice device : deviceList) {
            if (masterManager.getDeviceState(device) == DeviceState.Connected) {
                connectedDevices.add(device.getAddress());
            }
        }
        devicesArr = new String[connectedDevices.size()];
        return connectedDevices.toArray(devicesArr);
    }

    private boolean[] getCheckedItems() {
        checkedItems = new boolean[devicesArr.length];
        for (int i = 0; i < devicesArr.length; i++) {
            String device = connectedDevices.get(i);
            checkedItems[i] = selectedDevices.contains(device);
        }
        return checkedItems;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //mSelectedItems = new ArrayList();  // Where we track the selected items

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.selectslaves)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(getDevices(), getCheckedItems(),
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    if (!selectedDevices.contains(connectedDevices.get(which))) {
                                        selectedDevices.add(connectedDevices.get(which));
                                    }
                                } else if (selectedDevices.contains(connectedDevices.get(which))) {
                                    // Else, if the item is already in the array, remove it
                                    selectedDevices.remove(connectedDevices.get(which));
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        MainActivity.setSelectedDevices(selectedDevices);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }
}