package com.apg.app.qrcodeimagecapture;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;

import static com.apg.app.qrcodeimagecapture.ActivityCameraTakePicture.EXTRA_SCAN_GENERATE_QR;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CAMERA_GET_SHARE = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnCapture = (Button)findViewById(R.id.btnCapture);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ActivityCameraTakePicture.class);
                startActivityForResult(intent,REQUEST_CAMERA_GET_SHARE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

        }
    }
}
