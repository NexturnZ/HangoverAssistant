package com.example.hangoverassistent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private String TAG1 = "step";
    private TextView textView;
    private EditText fileNameInput;
    private Button start;

    private double MagnitudePrevious = 0;
    private Integer stepCount = 0;

    private static String fileTitle;
    public static File file;

    boolean START_RECORDING = false;

    private Runnable activity_recognition = new Runnable() {
        @Override
        public void run() {
            if (stepCount == null) {
                Log.e(TAG1, "is null");
            }


            String mode;
            if (stepCount <= 2) {
                mode = "Standing";
            } else if (stepCount > 2 && stepCount <= 6) {
                mode = "Walking";
            } else {
                mode = "Drunken";
            }


        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = findViewById(R.id.textView);
        fileNameInput = findViewById(R.id.fileNameInput);
        start = findViewById(R.id.start);

        start.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fileTitle = fileNameInput.getText().toString();
                    file = new File(Environment.getExternalStorageDirectory(), fileTitle);
                    START_RECORDING = true;
                }
            }
        );

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);



        askForPermissions();

        SensorEventListener stepDetector = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent != null && START_RECORDING==true) {
                    float x_acceleration = sensorEvent.values[0];
                    float y_acceleration = sensorEvent.values[1];
                    float z_acceleration = sensorEvent.values[2];



                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                            FileWriter writer = new FileWriter(file, true);

                            writer.write(x_acceleration+" ");
                            writer.write(y_acceleration+" ");
                            writer.write(z_acceleration+" ");
                            writer.write("\n");
                            writer.close();
                    } catch (IOException e) {
                        int a = 1;
                    }


                    double Magnitude = Math.sqrt(x_acceleration * x_acceleration + y_acceleration * y_acceleration + z_acceleration * z_acceleration);
                    double MagnitudeDelta = Magnitude - MagnitudePrevious;
                    MagnitudePrevious = Magnitude;

                    if (MagnitudeDelta > 6) {
                        stepCount++;
                    }
                    textView.setText(stepCount.toString());
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_NORMAL);
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

    public void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            }
//            createDir();
        }
    }

//    public void createDir(){
//        if (!dir.exists()){
//            dir.mkdirs();
//        }
//    }

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
}
