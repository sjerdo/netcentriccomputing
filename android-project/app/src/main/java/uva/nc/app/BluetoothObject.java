package uva.nc.app;

import java.io.Serializable;

/**
 * Created by Sjoerd on 6-1-2016.
 */
public class BluetoothObject implements Serializable {

    private static final long serialVersionUID = 23452345234532L;

    private int command;
    private float[] data;

    public BluetoothObject(int command, float[] data) {
        this.command = command;
        this.data = data;
    }

    public int getCommand() {
        return command;
    }

    public float[] getData() {
        return data;
    }
}
