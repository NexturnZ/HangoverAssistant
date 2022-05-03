package com.example.hangoverassistent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private double MagnitudePrevious = 0;
    private Integer stepCount = 0;


    //brought
    private EditText mGetText;
    private EditText mGetNumber;
    private Button mSendBtn;
    private Button mFunction;
    private String phoneNumber;
    private String message;

    boolean mode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //brought
        getSmsPermission();

        mode = false;

        //brought
        mGetNumber = (EditText) findViewById(R.id.mGetNumber);
        mGetText = (EditText) findViewById(R.id.mGetText);
        mSendBtn = (Button) findViewById(R.id.mSendBtn);
        mFunction = (Button) findViewById(R.id.function);


        //function button

        mFunction.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                if(mode==false){
                    mode = true;
                    mFunction.setText("Turn off");





                }
                else{
                    mode = false;
                    mFunction.setText("Turn on");



                }


            }
        });



        //later we could just delete this and replace this with the if statement
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


                String phoneNo = mGetNumber.getText().toString();
                String sms = mGetText.getText().toString();


                if(!validNumber(phoneNumber))
                {
                    Toast.makeText(MainActivity.this, "Invalid phone number", Toast.LENGTH_LONG).show();
                    return;
                } else if (message == null)
                {
                    Toast.makeText(MainActivity.this, "Message cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    //전송
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                    Toast.makeText(getApplicationContext(), "Message Sent!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }



        });

/////////////////////////////////////////////////////////////////////
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        SensorEventListener stepDetector = new SensorEventListener() {
            @Override


            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent != null) {
                    float x_acceleration = sensorEvent.values[0];
                    float y_acceleration = sensorEvent.values[1];
                    float z_acceleration = sensorEvent.values[2];


                    String fileTitle = "title.txt";
                    File file = new File(Environment.getExternalStorageDirectory(), fileTitle);

                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileWriter writer = new FileWriter(file, true);

                        writer.write(x_acceleration + "");
                        writer.write(y_acceleration + "");
                        writer.write(z_acceleration + "");
                        writer.write("\n");
                        writer.close();
                    } catch (IOException e) {

                    }


                    double Magnitude = Math.sqrt(x_acceleration * x_acceleration + y_acceleration * y_acceleration + z_acceleration * z_acceleration);
                    double MagnitudeDelta = Magnitude - MagnitudePrevious;
                    MagnitudePrevious = Magnitude;

                    if (MagnitudeDelta > 6) {
                        stepCount++;
                    }

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount", stepCount);
        editor.apply();
    }


    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount", stepCount);
        editor.apply();
    }

    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("stepCount", 0);
    }




    public void readFile() {

        String fileTitle = "title.txt";
        File file = new File(Environment.getExternalStorageDirectory(), fileTitle);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String result = "";
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }

            System.out.println("name : " + result);

            reader.close();
        } catch (FileNotFoundException e1) {

        } catch (IOException e2) {

        }


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

    private boolean validNumber(String phoneNumber)
    {
        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(phoneNumber);

        if(!m.find())
            return false;
        return phoneNumber.length() == 10;
    }


}