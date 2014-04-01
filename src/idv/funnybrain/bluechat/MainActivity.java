package idv.funnybrain.bluechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {
    // ---- constants START ----
    private static final boolean D = true;
    private static final String TAG = "MainActivity";
    private static final String BLUETOOTH_UUID = "54ab23e0-b949-11e3-9a7c-0002a5d5c51b"; // http://www.itu.int/ITU-T/asn1/uuid.html

    private static final int REQUESTCODE_BLUETOOTH_ENABLE = 0;
    private static final int REQUESTCODE_BLUETOOTH_CONNECT = REQUESTCODE_BLUETOOTH_ENABLE + 1;

//    private static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
//    private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;

    // ---- constants END ----

    // ---- local variable START ----
    private BluetoothAdapter bluetoothAdapter;
    private String serverMacAddress;
    // ---- local variable END ----

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        serverMacAddress = BluetoothAdapter.getDefaultAdapter().getAddress();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            if (bluetoothAdapter.isEnabled()) {
                //showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
                showDialogFragment(Utils.DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
            } else {
                final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUESTCODE_BLUETOOTH_ENABLE);
            }
        }
    }

    private void showDialogFragment(int type) {
        DialogFragment newFragment = new ChooseTypeDialogFragment().newInstance(type);
        newFragment.show(getFragmentManager(), "Choose Dialog");
    }

    public void doPositiveClick() { // Client
        final Intent intent = new Intent(MainActivity.this, BluetoothListDevicesActivity.class);
        startActivityForResult(intent, REQUESTCODE_BLUETOOTH_CONNECT);
    }

    public void doNeutralClick() { // Server
        toast("You can add and move sprites, which are only shown on the clients.");
        //initServer();
        showDialogFragment(Utils.DIALOG_SHOW_SERVER_IP_ID);
    }

    public void doNegativeClick() { // Both
        toast("You can add sprites and move them, by dragging them.");
        //initServerAndClient();
        showDialogFragment(Utils.DIALOG_SHOW_SERVER_IP_ID);
    }

    private void log(final String pMessage) {
        Log.d(TAG, pMessage);
    }

    private void toast(final String pMessage) {
        log(pMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, pMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}