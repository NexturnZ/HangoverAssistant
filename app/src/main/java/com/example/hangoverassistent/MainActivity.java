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
import android.os.Handler;
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
import java.util.Arrays;
import java.lang.Math;


public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private TextView textView;
    private EditText fileNameInput;
    private Button start;

    SensorManager sm;
    Sensor accelerator;

    Calculation cal = new Calculation();

    private static String fileTitle;
    public static File file;

    boolean START_RECORDING = false;


    int window_size = 50; // 1-sec window when sample rate is 0.02 (SENSOR_DELAY_GAME)
    int idx = 0;
    double[] x_acceleration = new double[window_size];
    double[] y_acceleration = new double[window_size];
    double[] z_acceleration = new double[window_size];
    long[] time = new long[window_size];
    double[] features = new double[31]; // 31 features

    double[] svm_coef = {0.683704,-0.740570,-0.825600,0.845597,-0.217621,
            0.255930,0.012187,0.000414,-0.016522,0.255709,0.311292,
            -0.079229,0.646723,0.246519,-1.040270,0.156335,-0.199792,
            0.117137,0.053464,-0.328856,-0.609530,0.356513,-0.247528,
            0.902920,-0.818884,-0.459921,0.316210,0.099498,0.370671,
            -0.240437,0.237050};
    double intercept = -1.16633028;


    private Handler mHandler1;

    private Integer stepCount = 0;


//    private Runnable activity_recognition = new Runnable() {
//        @Override
//        public void run() {
//            if (stepCount == null) {
//                Log.e(TAG1, "is null");
//            }
//
//
//            String mode;
//            if (stepCount <= 2) {
//                mode = "Standing";
//            } else if (stepCount > 2 && stepCount <= 6) {
//                mode = "Walking";
//            } else {
//                mode = "Drunken";
//            }
//
//
//        }
//    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerator = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mHandler1 = new Handler();

        textView = findViewById(R.id.textView1);

//        textView = findViewById(R.id.textView);
//        fileNameInput = findViewById(R.id.fileNameInput);
//        start = findViewById(R.id.start);

//        start.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        fileTitle = fileNameInput.getText().toString();
//                        file = new File(Environment.getExternalStorageDirectory(), fileTitle);
//                        START_RECORDING = true;
//                    }
//                }
//        );



    }

    protected void onResume() {
        super.onResume();
        sm.registerListener(this,accelerator,SensorManager.SENSOR_DELAY_GAME);

        long period = 1000; /* unit: ms */

        mHandler1.removeCallbacks(activity_recognition);
        mHandler1.postDelayed(activity_recognition,period);

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("stepCount", 0);
    }

    protected void onPause() {
        super.onPause();

        mHandler1.removeCallbacks(activity_recognition);
        

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
//        editor.putInt("stepCount", stepCount);
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



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[0];
        double y = sensorEvent.values[1];
        double z = sensorEvent.values[2];
        long t = sensorEvent.timestamp;

        if(idx < window_size){
            x_acceleration[idx] = x;
            y_acceleration[idx] = y;
            z_acceleration[idx] = z;
            time[idx] = t;

            idx += 1;
        }
        else {
            double[] x_new = new double[window_size];
            System.arraycopy(x_acceleration,1,x_new,0,window_size-1);
            x_acceleration = x_new;
            x_acceleration[window_size-1] = x;

            double[] y_new = new double[window_size];
            System.arraycopy(y_acceleration,1,y_new,0,window_size-1);
            y_acceleration = y_new;
            y_acceleration[window_size-1] = y;

            double[] z_new = new double[window_size];
            System.arraycopy(z_acceleration,1,z_new,0,window_size-1);
            z_acceleration = z_new;
            z_acceleration[window_size-1] = z;

            long[] t_new = new long[window_size];
            System.arraycopy(time,1,t_new,0,window_size-1);
            time = t_new;
            time[window_size-1] = t;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private Runnable activity_recognition = new Runnable() {
        @Override
        public void run() {

            // {x:ML,y:V,z:AP}

            features[0] = cal.mean(x_acceleration); // mu_ML
            features[1] = cal.mean(z_acceleration); // mu_AP
            features[2] = cal.mean(y_acceleration); // mu_V

            features[3] = cal.var(x_acceleration); // sigma_ML^2
            features[4] = cal.var(z_acceleration); // sigma_AP^2
            features[5] = cal.var(y_acceleration); // sigma_V^2

            features[6] = cal.correlate(x_acceleration,z_acceleration); // cor(ML,AP)
            features[7] = cal.correlate(x_acceleration,y_acceleration); // cor(ML,V)
            features[8] = cal.correlate(y_acceleration,z_acceleration); // cor(V,AP)

            features[9] = cal.covariance(x_acceleration,z_acceleration); // cov(ML,AP)
            features[10] = cal.covariance(x_acceleration,y_acceleration); // cov(ML,V)
            features[11] = cal.covariance(y_acceleration,z_acceleration); // cov(V,AP)

            features[12] = cal.max(x_acceleration) - cal.min(x_acceleration); // d_ML
            features[13] = cal.max(z_acceleration) - cal.min(z_acceleration); // d_AP
            features[14] = cal.max(y_acceleration) - cal.min(y_acceleration); // d_V
            features[15] = (double) Math.sqrt(Math.pow(features[12],2)+Math.pow(features[13],2)); // d_ML_AP
            features[16] = (double) Math.sqrt(Math.pow(features[12],2)+Math.pow(features[14],2)); // d_ML_V
            features[17] = (double) Math.sqrt(Math.pow(features[14],2)+Math.pow(features[13],2));// d_V_AP
            features[18] = (double) Math.sqrt(Math.pow(features[12],2)+Math.pow(features[13],2)+Math.pow(features[14],2));// d_ML_AP_V

            double[] tr_x = cal.trend(x_acceleration,features[0],features[3]);
            double[] tr_z = cal.trend(z_acceleration,features[1],features[4]);
            double[] tr_y = cal.trend(y_acceleration,features[2],features[5]);
            features[19] = tr_x[0];// muT_ML
            features[20] = tr_z[0];// muT_AP
            features[21] = tr_y[0];// muT_V

            features[22] = tr_x[1];// muD_ML
            features[23] = tr_z[1];// muD_AP
            features[24] = tr_y[1];// muD_V

            features[25] = tr_x[2];// (sigma^2)T_ML
            features[26] = tr_z[2];// (sigma^2)T_AP
            features[27] = tr_y[2];// (sigma^2)T_V

            features[28] = tr_x[3];// (sigma^2)D_ML
            features[29] = tr_z[3];// (sigma^2)D_AP
            features[30] = tr_y[3];// (sigma^2)D_V

            double pred = cal.correlate(features,svm_coef) + intercept;

            if(pred>0){
                textView.setText(String.format("Pred value:%f\nDRUNK",pred));
                Log.i("Detection Result", String.format("DRUNK:%f\n",pred));
//                System.out.printf("DRUNK:%f\n",pred);
            }
            else{
                textView.setText(String.format("Pred value:%f\nNOT DRUNK",pred));
//                System.out.printf("NOT DRUNK:%f\n",pred);
                Log.i("Detection Result", String.format("NOT DRUNK:%f\n",pred));
            }

//            textView.setText(String.format("%f",(time[0]-time[1])/1e9));


            mHandler1.postDelayed(activity_recognition,1000);
        }
    };
}