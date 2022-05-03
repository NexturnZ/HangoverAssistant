package com.example.hangoverassistent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;

import android.hardware.SensorManager;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private String TAG1 = "step";
    private TextView textView;
    private double MagnitudePrevious = 0;
    private Integer stepCount = 0;
    private boolean flag = false;
    //brought
    private Button mLog;
    private Button mFunction;

    private Button mSetting;

    private TextView numbers;

    Intent setting; /* intent for setting page activity */
    Intent logging;


    /*configuration page variables*/
    ActivityResultLauncher<Intent> configurationActivityResultLauncher;
    String phoneNo, sms;
    boolean sms_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //brought
        getSmsPermission();

        //brought

        mLog = findViewById(R.id.log);
        mFunction = (Button) findViewById(R.id.function);
        mSetting = (Button) findViewById(R.id.setting);

        numbers = findViewById(R.id.numbers);

        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                onButtonSettingClicked(v);
            }
        });

        //function button

        mLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onButtonLogClicked(v);
            }
        });

        mFunction.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if (flag == false){
                    flag = true;
                    mFunction.setText("Turn Off");

                    String phone = setting.getExtras().getString("phoneNo");
                    numbers.setText(phone);

                }else{

                    flag = false;
                    mFunction.setText("Turn On");

                    String phone = setting.getExtras().getString("phoneNo");
                    numbers.setText(phone);

                }
            }
        });




        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);


        /* Configuration page intent result set up */
        configurationActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){

                    phoneNo = result.getData().getExtras().getString("phoneNo");
                    sms = result.getData().getExtras().getString("sms");
                    sms_flag = result.getData().getExtras().getBoolean("sms_flag");
                }
            }
        });
    }

    public void onButtonSettingClicked(View v){
        setting = new Intent(this, setting.class);
        setting.putExtra("phoneNo",phoneNo);
        setting.putExtra("sms",sms);
        setting.putExtra("sms_flag",sms_flag);
        configurationActivityResultLauncher.launch(setting);
    }

    public void onButtonLogClicked(View v){
        logging = new Intent(this, userlog.class);
        startActivity(logging);
    }



    protected void onPause() {
        super.onPause();
    }



    protected void onStop() {
        super.onStop();
    }
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("stepCount", 0);
    }

    private void getSmsPermission()
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        1);
            }
        }
    }
//    private void sendMessage(String phoneNumber, String message)
//    {
//        final SmsManager smsManager = SmsManager.getDefault();
//        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
//
//        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
//
//        // Ensure message is not too long
//        while(message.length() > 160)
//        {
//            String subMessage = message.substring(0, 160);
//            smsManager.sendTextMessage(phoneNumber, null, subMessage, null, null);
//            sendIntent.putExtra("sms_body", "default content");
//            sendIntent.setType("vnd.android-dir/mms-sms");
//
//            message = message.substring(160, message.length());
//        }
//
//        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
//        sendIntent.putExtra("sms_body", "default content");
//        sendIntent.setType("vnd.android-dir/mms-sms");
//        startActivity(sendIntent);
//    }



}