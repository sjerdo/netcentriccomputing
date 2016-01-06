package uva.nc.app;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;

import uva.nc.bluetooth.DeviceState;
import uva.nc.bluetooth.MasterManager;

/**
 * Created by Sjoerd on 6-1-2016.
 */

public class SelectSlavesDialog extends DialogFragment {

    private MasterManager masterManager;
    private String[] devicesArr;
    private ArrayList<String> connectedDevices;
    private ArrayList<String> deselectedDevices;

    private FragmentCallbacks mCallbacks;

    public void setMasterManager(MasterManager masterManager) {
        this.masterManager = masterManager;
    }

    public void setDeselectedDevices(ArrayList<String> deselectedDevices) {
        this.deselectedDevices = deselectedDevices;
    }

    public void setCallback(FragmentCallbacks callback) {
        this.mCallbacks = callback;
    }

    private String[] getDevices() {
        ArrayList<BluetoothDevice> deviceList = masterManager.getDeviceList();
        connectedDevices = new ArrayList<>();
        ArrayList<String> connectedDeviceNames = new ArrayList<>();
        for (BluetoothDevice device : deviceList) {
            if (masterManager.getDeviceState(device) == DeviceState.Connected) {
                connectedDevices.add(device.getAddress());
                connectedDeviceNames.add(device.getName());
            }
        }
        devicesArr = new String[connectedDeviceNames.size()];
        return connectedDeviceNames.toArray(devicesArr);
    }

    private boolean[] getCheckedItems() {
        boolean[] checkedItems = new boolean[devicesArr.length];
        for (int i = 0; i < devicesArr.length; i++) {
            String device = connectedDevices.get(i);
            checkedItems[i] = !deselectedDevices.contains(device);
        }
        return checkedItems;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

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
                                    if (deselectedDevices.contains(connectedDevices.get(which))) {
                                        deselectedDevices.remove(connectedDevices.get(which));
                                    }
                                } else if (!deselectedDevices.contains(connectedDevices.get(which))) {
                                    // Else, if the item is already in the array, remove it
                                    deselectedDevices.add(connectedDevices.get(which));
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        if (mCallbacks != null) {
                            mCallbacks.SelectedDevicesUpdated(deselectedDevices);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }

    public interface FragmentCallbacks {
        void SelectedDevicesUpdated(ArrayList<String> deselectedDevices);
    }

}