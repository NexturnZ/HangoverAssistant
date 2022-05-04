package com.example.hangoverassistent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener{


    /* Sensing and detection */
    SensorManager sm;
    Sensor accelerator;
    private Handler mHandler1;

    Calculation cal = new Calculation();
    int window_size = 50; // 1-sec window when sample rate is 0.02 (SENSOR_DELAY_GAME)
    int idx = 0;
    double[] x_acceleration = new double[window_size];
    double[] y_acceleration = new double[window_size];
    double[] z_acceleration = new double[window_size];
    long[] time = new long[window_size];


//    /* SVM parameters set 1 (seems to perform good) */
//    double[] svm_coef = {0.031053,0.001120,0.004053,0.007002,-0.049769,0.006411,0.005582,-0.006079,-0.045106,-0.005188,0.002751,-0.004942,-0.107163,0.249802,-0.090019,0.051073,-0.138872,0.060385,-0.013575,0.119103,0.025736,0.088006,0.166005,0.024176,-0.011855,-0.084300,-0.023110,0.093216,0.009598,-0.034145,0.031699};
//    double intercept = -1.029632;

    /* SVM parameter set 2 */
    private double[] svm_coef = {-0.000417,-0.004369,0.007100,0.005774,-0.026227,0.000889,0.016376,-0.022719,-0.076439,-0.010959,0.004071,-0.002835,-0.054202,0.166098,-0.005136,0.054100,-0.025855,0.072041,0.037173,0.118530,0.116273,0.077862,0.080757,0.047996,-0.011626,-0.079853,0.001354,0.111593,0.029098,-0.082564,0.032931};
    private double intercept = -2.305152;

    private ArrayList<Integer> detection;
    private ArrayList<Long> detection_time;
    private long starting_time;
    private long ending_time;

    private int decision_window_size = 10; /* detection result in 10 seconds are used to decide user state */
    private int decision_threshold = 6; /* 8s in 10s are detected drunk */




    private boolean drunken = false;
    private boolean Activation = false; /* whether the function is turn on */
    private boolean sms_sent = false; /* flag recording whether message is sent, assuming only send sms once */


    /* Views initialization*/
    private Button mLog;
    private Button mSetting;
    private TextView mActSign,mDrunkIndicator;
    private Switch mActivate;
    private Intent setting; /* intent for setting page activity */
    private Intent logging;


    /*configuration page variables*/
    ActivityResultLauncher<Intent> configurationActivityResultLauncher;
    String phoneNo, sms;
    boolean sms_flag = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detection = new ArrayList<>();
        detection_time = new ArrayList<>();
        Date date = new Date();
        starting_time = 0;


        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerator = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mHandler1 = new Handler();

        //brought
        getSmsPermission();

        //brought
        mLog = findViewById(R.id.log);
        mSetting = findViewById(R.id.setting);
        mActivate = findViewById(R.id.mActivate);
        mActSign = findViewById(R.id.mActSign);
        mDrunkIndicator = findViewById(R.id.DrunkIndicator);


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

        mActivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mActSign.setText("Activated");
                }
                else{
                    mActSign.setText("Ready to Activate");
                    sms_sent = false; /* reset sms_sent flag */
                }

                Activation = b;

                if(starting_time==0){
                    starting_time = date.getTime();
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

    protected void onResume() {
        super.onResume();
        sm.registerListener(this,accelerator,SensorManager.SENSOR_DELAY_GAME);

        long period = 1000; /* unit: ms */

        mHandler1.removeCallbacks(activity_recognition);
        mHandler1.postDelayed(activity_recognition,period);

    }

    protected void onPause() {
        super.onPause();

        mHandler1.removeCallbacks(activity_recognition);
    }

    protected void onStop() {
        super.onStop();
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

    public void onButtonSettingClicked(View v){
        setting = new Intent(this, setting.class);
        setting.putExtra("phoneNo",phoneNo);
        setting.putExtra("sms",sms);
        setting.putExtra("sms_flag",sms_flag);
        configurationActivityResultLauncher.launch(setting);
    }

    public void onButtonLogClicked(View v){
        Date date = new Date();
        ending_time = date.getTime();

        logging = new Intent(this, userlog.class);
        logging.putExtra("detection",detection);
//        logging.putExtra("detection_time",detection_time);
        logging.putExtra("starting_time",starting_time);
        logging.putExtra("ending_time",ending_time);

        startActivity(logging);
    }

    private Runnable activity_recognition = new Runnable() {
        @Override
        public void run() {
            if(Activation){

                /* drunk detection */
                double[] features = feature_extraction();
                int pred = prediction(features);
                detection.add(pred);
                detection_time.add(time[0]);

                if(detection.size()>decision_window_size){
                    List<Integer> decision_window = detection.subList(detection.size()-decision_window_size,detection.size());
//                    int[] decision_window = detection.subList(detection.size()-decision_window_size,detection.size()).toArray();

                    int sum = 0;
                    for(int i1=0;i1<decision_window_size;i1++){
                        sum += decision_window.get(i1);
                    }

                    if(sum>decision_threshold){
                        drunken = true;
                    }

                    if(drunken&sms_flag&!sms_sent){
                        sendSMS();
                    }
                }


            }
            mHandler1.postDelayed(activity_recognition,1000);


        }
    };

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

    private void sendSMS(){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, sms, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent!", Toast.LENGTH_LONG).show();
            sms_sent = true;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private double[] feature_extraction(){
        /* {x:ML,y:V,z:AP} */
        double[] features = new double[31]; // 31 features
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
        features[15] = Math.sqrt(Math.pow(features[12],2)+Math.pow(features[13],2)); // d_ML_AP
        features[16] = Math.sqrt(Math.pow(features[12],2)+Math.pow(features[14],2)); // d_ML_V
        features[17] = Math.sqrt(Math.pow(features[14],2)+Math.pow(features[13],2));// d_V_AP
        features[18] = Math.sqrt(Math.pow(features[12],2)+Math.pow(features[13],2)+Math.pow(features[14],2));// d_ML_AP_V

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

        return features;
    }

    private int prediction(double[] features){
        double pred = cal.correlate(features,svm_coef) + intercept;

        if(pred>0){
            mDrunkIndicator.setText(String.format("DRUNK",pred));
            Log.d("Detection Result", String.format("DRUNK:%f\n",pred));
        }
        else{
            mDrunkIndicator.setText(String.format("NOT DRUNK",pred));
            Log.d("Detection Result", String.format("NOT DRUNK:%f\n",pred));
        }

        return (int)Math.signum(pred);
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