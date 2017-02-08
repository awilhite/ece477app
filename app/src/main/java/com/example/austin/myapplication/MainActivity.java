package com.example.austin.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        final TextView tv = (TextView) findViewById(R.id.sample_text);

        tv.setText(stringFromJNI());

        Button forward = (Button) findViewById(R.id.b_forward);
        Button reverse = (Button) findViewById(R.id.b_reverse);
        Button left = (Button) findViewById(R.id.b_left);
        Button right = (Button) findViewById(R.id.b_right);

        forward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(R.string.forward);
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

        //TCPClient client = new TCPClient();

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

