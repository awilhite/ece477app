package com.example.austin.myapplication;

import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    public static final int SENDING = 1;
    public static final int ERROR = 2;
    public static final int SENT = 3;
    public static final int CONNECTING = 4;
    public static final int SHUTDOWN = 5;

    private String mTag = "MainActivity";
    private TextView tv;
    Handler msgHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.sample_text);

        tv.setText(stringFromJNI());

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
                new ShutdownAsyncTask(getmHandler()).execute();
            }
        });

        reverse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(R.string.reverse);
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(R.string.left);
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(R.string.right);
            }
        });

        // Prevent screen form turning off

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private Handler getmHandler() {
        msgHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SHUTDOWN:
                        Log.d(mTag, "In Handler's shutdown");
                        tv.setText(R.string.shutdown);
                        break;
                    case ERROR:
                        Log.d(mTag, "In Handler's error");
                        tv.setText(R.string.error);
                        break;
                    case SENDING:
                        Log.d(mTag, "In Handler's sending");
                        tv.setText(R.string.sending);
                        break;
                    case CONNECTING:
                        Log.d(mTag, "In Handler's connecting");
                        tv.setText(R.string.connecting);
                        break;
                    case SENT:
                        Log.d(mTag, "In Handler's sent");
                        tv.setText(R.string.sent);
                        break;
                }
            }
        };
        return msgHandler;
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

