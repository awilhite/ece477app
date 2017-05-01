package com.example.austin.myapplication;

import android.os.AsyncTask;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends AppCompatActivity {

    public static final int POLL_DELAY = 100;
    private static final int VEHICLE_NUM = 0;

    private String mTag = "MainActivity";
    private TextView tv;
    private TextView hitsText;
    private TextView pointsText;
    private TCPClient mTcpClient;
    private TCPClient mTcpOppClient;
    private int hitsReceived = 0;
    private int hitsScored = 0;
    private int numChecks = 0;
    private int leftProgress = 127;
    private int rightProgress = 127;
    Handler msgHandler;

    private Handler pollHandler = new Handler();
    private Handler pollOppHandler = new Handler();

    private Runnable pollServer = new Runnable() {
        @Override
        public void run() {
            // poll server for hits
            mTcpClient.sendMessage("h");
            // call again in POLL_DELAY ms
            pollHandler.postDelayed(this, POLL_DELAY);
        }
    };

    private Runnable pollOppServer = new Runnable() {
        @Override
        public void run() {
            // poll server for hits
            mTcpOppClient.sendMessage("h");
            // call again in POLL_DELAY ms
            pollOppHandler.postDelayed(this, POLL_DELAY);
        }
    };

    private int toPercent(int value)
    {
        int percent;

        percent = (int) (((float)value)/127*100);
        if (percent == 100)
        {
            percent = 0;
        }
        else
        {
            percent = percent - 100;
        }
        return percent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create TextView References
        tv = (TextView) findViewById(R.id.sample_text);
        hitsText = (TextView) findViewById(R.id.hits_text);
        pointsText = (TextView) findViewById(R.id.points_text);

        final TextView rightText = (TextView)findViewById(R.id.rightText);
        final TextView leftText = (TextView)findViewById(R.id.leftText);

        rightText.setTextSize(24);
        leftText.setTextSize(24);

        final VerticalSeekBar leftRange = (VerticalSeekBar) findViewById(R.id.leftRange);
        final VerticalSeekBar rightRange = (VerticalSeekBar) findViewById(R.id.rightRange);

        leftRange.setProgress(leftProgress);
        rightRange.setProgress(rightProgress);

        leftText.setText("" + toPercent(leftProgress));
        rightText.setText("" + toPercent(rightProgress));

        leftRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mTcpClient != null)
                {
                    leftProgress = progress;
                    mTcpClient.sendMessage("l"+Integer.toHexString(progress));
                }
                leftText.setText("" + toPercent(progress));
            }
        });

        rightRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

               @Override
               public void onStopTrackingTouch(SeekBar seekBar) {

               }

               @Override
               public void onStartTrackingTouch(SeekBar seekBar) {

               }

               @Override
               public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                   if (mTcpClient != null) {
                       rightProgress = progress;
                       mTcpClient.sendMessage("r"+Integer.toHexString(progress));
                   }
                   rightText.setText("" + toPercent(progress));
               }
        });

        // Get button handles

        Button forward = (Button) findViewById(R.id.b_forward);
        Button reverse = (Button) findViewById(R.id.b_reverse);
        Button left = (Button) findViewById(R.id.b_left);
        Button right = (Button) findViewById(R.id.b_right);

        Button brake = (Button) findViewById(R.id.b_brake);

        Button fire = (Button) findViewById(R.id.b_fire);

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
                if (mTcpClient != null) {
                    mTcpClient.sendMessage("reverse");
                }
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(R.string.left);
                if (mTcpClient != null) {
                    mTcpClient.sendMessage("l"+Integer.toHexString(leftProgress));
                }
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(R.string.right);
                if (mTcpClient != null) {
                    mTcpClient.sendMessage("r"+Integer.toHexString(rightProgress));
                }
            }
        });

        brake.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText("Braking!");
                leftRange.setProgress(0x7F);
                rightRange.setProgress(0x7F);
            }
        });

        fire.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText("Shot Fired!");
                if (mTcpClient != null) {
                    mTcpClient.sendMessage("f"+VEHICLE_NUM);
                }
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
                new ConnectTask().execute("192.168.1.5");
                //new ConnectOppTask().execute("192.168.1.4");
                pollHandler.postDelayed(pollServer, POLL_DELAY);
                //pollOppHandler.postDelayed(pollOppServer, POLL_DELAY);
                return true;
            case R.id.disconnect:
                // disconnect
                mTcpClient.stopClient();
                mTcpClient = null;
                //mTcpOppClient.stopClient();
                //mTcpOppClient = null;
                pollHandler.removeCallbacks(pollServer);
                //pollOppHandler.removeCallbacks(pollOppServer);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class ConnectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... params) {

            try{
                mTcpClient = new TCPClient(params[0], 4444, new TCPClient.OnMessageReceived() {
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
            char arg;
            super.onProgressUpdate(values);

            tv.setText(String.format("Received: %s", values[0]));

            char command = values[0].charAt(0);

            switch (command) {
                case 'h':
                    if (values[0].length() > 1) {
                        arg = values[0].charAt(1);

                        if (arg == VEHICLE_NUM) {
                            hitsReceived++;
                        } else {
                            hitsScored++;
                        }

                        hitsText.setText(String.format("Hits Received: %s", Integer.toString(hitsReceived)));
                        pointsText.setText(String.format("Hits Scored: %s", Integer.toString(hitsScored)));
                    } else {
                        tv.setText(String.format("We have a problem"));
                    }
                    break;
                case 'r':
                    numChecks++;
                    hitsText.setText(String.format("numChecks: %s", Integer.toString(numChecks)));
                    break;
                default:
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


    public class ConnectOppTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... params) {

            try{
                mTcpOppClient = new TCPClient(params[0], 4444, new TCPClient.OnMessageReceived() {
                    @Override
                    public void messageReceived(String message) {
                        publishProgress(message);
                    }
                });

            }catch (NullPointerException e){
                Log.d("ConnectTask", "Caught null pointer exception");
                e.printStackTrace();
            }
            mTcpOppClient.run();

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
            char arg;
            super.onProgressUpdate(values);

            tv.setText(String.format("Received: %s", values[0]));

            char command = values[0].charAt(0);

            switch (command) {
                case 'h':
                    if (values[0].length() > 1) {
                        arg = values[0].charAt(1);

                        if (arg == VEHICLE_NUM) {
                            hitsReceived++;
                        } else {
                            hitsScored++;
                        }

                        hitsText.setText(String.format("Hits Received: %s", Integer.toString(hitsReceived)));
                        pointsText.setText(String.format("Hits Scored: %s", Integer.toString(hitsScored)));
                    } else {
                        tv.setText(String.format("We have a problem"));
                    }
                    break;
                case 'r':
                    numChecks++;
                    hitsText.setText(String.format("numChecks: %s", Integer.toString(numChecks)));
                    break;
                default:
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

