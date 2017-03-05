package com.example.austin.myapplication;

import android.os.AsyncTask;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    public static final int SENDING = 1;
    public static final int ERROR = 2;
    public static final int SENT = 3;
    public static final int CONNECTING = 4;
    public static final int SHUTDOWN = 5;
    public static final int POLL_DELAY = 100;

    private String mTag = "MainActivity";
    private TextView tv;
    private TextView hitsText;
    private TCPClient mTcpClient;
    private int hitsReceived = 0;
    private int hitsScored = 0;
    private int numChecks = 0;
    Handler msgHandler;

    private Handler pollHandler = new Handler();

    private Runnable pollServer = new Runnable() {
        @Override
        public void run() {
            // poll server for hits
            mTcpClient.sendMessage("hits");
            // call again in POLL_DELAY ms
            pollHandler.postDelayed(this, POLL_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.sample_text);
        hitsText = (TextView) findViewById(R.id.hits_text);
        tv.setText(stringFromJNI());
        hitsText.setText("Hits Text");

        // Get button handles

        Button forward = (Button) findViewById(R.id.b_forward);
        Button reverse = (Button) findViewById(R.id.b_reverse);
        Button left = (Button) findViewById(R.id.b_left);
        Button right = (Button) findViewById(R.id.b_right);

        // Set click event listeners

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(R.string.forward);
                if (mTcpClient != null) {
                    mTcpClient.sendMessage("forward");
                }
                else {
                    Log.d("ForwardClick", "mTcpClient == null");
                }
            }
        });

        reverse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(R.string.reverse);
                mTcpClient.sendMessage("reverse");
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(R.string.left);
                mTcpClient.sendMessage("left");
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                tv.setText(R.string.right);
                mTcpClient.sendMessage("right");
            }
        });

        // Prevent screen form turning off

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (mTcpClient != null) {
            // if the client is connected, enable the connect button and disable the disconnect one
            menu.getItem(1).setEnabled(true);
            menu.getItem(0).setEnabled(false);
        } else {
            // if the client is disconnected, enable the disconnect button and disable the connect one
            menu.getItem(1).setEnabled(false);
            menu.getItem(0).setEnabled(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.connect:
                // connect to the server
                new ConnectTask().execute("");
                pollHandler.postDelayed(pollServer, POLL_DELAY);
                return true;
            case R.id.disconnect:
                // disconnect
                mTcpClient.stopClient();
                mTcpClient = null;
                pollHandler.removeCallbacks(pollServer);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class ConnectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... params) {

            try{
                mTcpClient = new TCPClient("192.168.1.232", 4444, new TCPClient.OnMessageReceived() {
                    @Override
                    public void messageReceived(String message) {
                        publishProgress(message);
                    }
                });

            }catch (NullPointerException e){
                Log.d("ConnectTask", "Caught null pointer exception");
                e.printStackTrace();
            }
            mTcpClient.run();

            return null;
        }

        /**
         * Overriden method from AsyncTask class. Here we're checking if server answered properly.
         * Called with the incoming message by the TCP Client when a message is received
         * @param values If "restart" message came, the client is stopped and computer should be restarted.
         *               Otherwise "wrong" message is sent and 'Error' message is shown in UI.
         */
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            tv.setText(String.format("Received: %s", values[0]));

            switch (values[0]) {
                case "hit":
                    hitsReceived++;
                    hitsText.setText(String.format("Hits Received: %s", Integer.toString(hitsReceived)));
                    break;
                case "none":
                    numChecks++;
                    hitsText.setText(String.format("numChecks: %s", Integer.toString(numChecks)));
                    break;
                case "OK":
                    break;
            }

            Log.d("ProgressUpdate", "values: " + values[0]);
        }

        @Override
        protected void onPostExecute(TCPClient result) {
            super.onPostExecute(result);

           // Log.d(mTAG, "In onPostExecute");
           /* if (result != null && result.isRunning()){
                result.stopClient();
            }*/

        }
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}

