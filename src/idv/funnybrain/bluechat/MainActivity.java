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
import org.andengine.extension.multiplayer.protocol.client.connector.BluetoothSocketConnectionServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.exception.BluetoothException;
import org.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer;
import org.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;

import java.io.IOException;

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

    private BluetoothSocketServer<BluetoothSocketConnectionClientConnector> mBluetoothSocketServer;
    private ServerConnector<BluetoothSocketConnection> serverConnector;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_BLUETOOTH_ENABLE:
                showDialogFragment(Utils.DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
                break;
            case REQUESTCODE_BLUETOOTH_CONNECT:
                serverMacAddress = data.getExtras().getString(BluetoothListDevicesActivity.EXTRA_DEVICE_ADDRESS);
                initClient();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
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
        initServer();
        showDialogFragment(Utils.DIALOG_SHOW_SERVER_IP_ID);
    }

    public void doNegativeClick() { // Both
        toast("You can add sprites and move them, by dragging them.");
        initServerAndClient();
        showDialogFragment(Utils.DIALOG_SHOW_SERVER_IP_ID);
    }


    private void initServer() {
        serverMacAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
        try {
            mBluetoothSocketServer = new BluetoothSocketServer<BluetoothSocketConnectionClientConnector>(
                    BLUETOOTH_UUID,
                    new ChattingClientConnectorListener(),
                    new ChattingServerStateListener()) {
                @Override
                protected BluetoothSocketConnectionClientConnector newClientConnector(final BluetoothSocketConnection pBluetoothSocketConnection) throws IOException {
                    try {
                        return new BluetoothSocketConnectionClientConnector(pBluetoothSocketConnection);
                    } catch (final BluetoothException e) {
                        Log.e(TAG, e.getMessage());
						/* Actually cannot happen. */
                        return null;
                    }
                }
            };
        } catch (final BluetoothException e) {
            Log.e(TAG, e.getMessage());
        }

        this.mBluetoothSocketServer.start();
    }

    private void initClient() {
        try {
            serverConnector = new BluetoothSocketConnectionServerConnector(
                    new BluetoothSocketConnection(bluetoothAdapter,
                    serverMacAddress, BLUETOOTH_UUID),
                    new ChattingServerConnectorListener());
        } catch(final Throwable t) {
            Log.e(TAG, t.getMessage());
        }
    }

    private void initServerAndClient() {
        initServer();

		/* Wait some time after the server has been started, so it actually can start up. */
        try {
            Thread.sleep(500);
        } catch (final Throwable t) {
            Log.e(TAG, t.getMessage());
        }

        this.initClient();
    }

    // ---- inner class START ----
    private class ChattingClientConnectorListener implements BluetoothSocketConnectionClientConnector.IBluetoothSocketConnectionClientConnectorListener {
        @Override
        public void onStarted(final ClientConnector<BluetoothSocketConnection> pConnector) {
            toast("SERVER: Client connected: " + pConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
        }

        @Override
        public void onTerminated(final ClientConnector<BluetoothSocketConnection> pConnector) {
            toast("SERVER: Client disconnected: " + pConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
        }
    }

    private class ChattingServerConnectorListener implements BluetoothSocketConnectionServerConnector.IBluetoothSocketConnectionServerConnectorListener {
        @Override
        public void onStarted(final ServerConnector<BluetoothSocketConnection> pConnector) {
            toast("CLIENT: Connected to server.");
        }

        @Override
        public void onTerminated(final ServerConnector<BluetoothSocketConnection> pConnector) {
            toast("CLIENT: Disconnected from Server...");
            finish();
        }
    }

    private class ChattingServerStateListener implements BluetoothSocketServer.IBluetoothSocketServerListener<BluetoothSocketConnectionClientConnector> {
        @Override
        public void onStarted(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pBluetoothSocketServer) {
            toast("SERVER: Started.");
        }

        @Override
        public void onTerminated(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pBluetoothSocketServer) {
            toast("SERVER: Terminated.");
        }

        @Override
        public void onException(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pBluetoothSocketServer, final Throwable pThrowable) {
            Log.e(TAG, pThrowable.getMessage());
            toast("SERVER: Exception: " + pThrowable);
        }
    }
    // ---- inner class END ----

    // ---- Log and Toast START ----
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
    // ---- Log and Toast END ----

}