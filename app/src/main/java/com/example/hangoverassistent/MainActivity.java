package com.example.hangoverassistent;

//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//
//import java.io.File;
//import java.lang.Math;
//
//
//public class MainActivity extends AppCompatActivity implements SensorEventListener{
//    private TextView textView;
//
//    SensorManager sm;
//    Sensor accelerator;
//
//    Calculation cal = new Calculation();
//
//    int window_size = 50; // 1-sec window when sample rate is 0.02 (SENSOR_DELAY_GAME)
//    int idx = 0;
//    double[] x_acceleration = new double[window_size];
//    double[] y_acceleration = new double[window_size];
//    double[] z_acceleration = new double[window_size];
//    long[] time = new long[window_size];
//    double[] features = new double[31]; // 31 features
//
////    /* SVM parameters set 1 (seems to perform good) */
////    double[] svm_coef = {0.031053,0.001120,0.004053,0.007002,-0.049769,0.006411,0.005582,-0.006079,-0.045106,-0.005188,0.002751,-0.004942,-0.107163,0.249802,-0.090019,0.051073,-0.138872,0.060385,-0.013575,0.119103,0.025736,0.088006,0.166005,0.024176,-0.011855,-0.084300,-0.023110,0.093216,0.009598,-0.034145,0.031699};
////    double intercept = -1.029632;
//
//    /* SVM parameter set 2 */
//    double[] svm_coef = {-0.000417,-0.004369,0.007100,0.005774,-0.026227,0.000889,0.016376,-0.022719,-0.076439,-0.010959,0.004071,-0.002835,-0.054202,0.166098,-0.005136,0.054100,-0.025855,0.072041,0.037173,0.118530,0.116273,0.077862,0.080757,0.047996,-0.011626,-0.079853,0.001354,0.111593,0.029098,-0.082564,0.032931};
//    double intercept = -2.305152;
//
//
//    private Handler mHandler1;
//
//    private Integer stepCount = 0;
//
//
//
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        accelerator = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//
//        mHandler1 = new Handler();
//
//        textView = findViewById(R.id.textView1);
//
//
//    }
//
//    protected void onResume() {
//        super.onResume();
//        sm.registerListener(this,accelerator,SensorManager.SENSOR_DELAY_GAME);
//
//        long period = 1000; /* unit: ms */
//
//        mHandler1.removeCallbacks(activity_recognition);
//        mHandler1.postDelayed(activity_recognition,period);
//
//        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
//        stepCount = sharedPreferences.getInt("stepCount", 0);
//    }
//
//    protected void onPause() {
//        super.onPause();
//
//        mHandler1.removeCallbacks(activity_recognition);
//
//
////        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
////        SharedPreferences.Editor editor = sharedPreferences.edit();
////        editor.clear();
////        editor.apply();
//    }
//
//    protected void onStop() {
//        super.onStop();
//
////        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
////        SharedPreferences.Editor editor = sharedPreferences.edit();
////        editor.clear();
////        editor.putInt("stepCount", stepCount);
////        editor.apply();
//    }
//
//
//
//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        double x = sensorEvent.values[0];
//        double y = sensorEvent.values[1];
//        double z = sensorEvent.values[2];
//        long t = sensorEvent.timestamp;
//
//        if(idx < window_size){
//            x_acceleration[idx] = x;
//            y_acceleration[idx] = y;
//            z_acceleration[idx] = z;
//            time[idx] = t;
//
//            idx += 1;
//        }
//        else {
//            double[] x_new = new double[window_size];
//            System.arraycopy(x_acceleration,1,x_new,0,window_size-1);
//            x_acceleration = x_new;
//            x_acceleration[window_size-1] = x;
//
//            double[] y_new = new double[window_size];
//            System.arraycopy(y_acceleration,1,y_new,0,window_size-1);
//            y_acceleration = y_new;
//            y_acceleration[window_size-1] = y;
//
//            double[] z_new = new double[window_size];
//            System.arraycopy(z_acceleration,1,z_new,0,window_size-1);
//            z_acceleration = z_new;
//            z_acceleration[window_size-1] = z;
//
//            long[] t_new = new long[window_size];
//            System.arraycopy(time,1,t_new,0,window_size-1);
//            time = t_new;
//            time[window_size-1] = t;
//        }
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int i) {
//
//    }
//
//    private Runnable activity_recognition = new Runnable() {
//        @Override
//        public void run() {
//
//            // {x:ML,y:V,z:AP}
//
//            features[0] = cal.mean(x_acceleration); // mu_ML
//            features[1] = cal.mean(z_acceleration); // mu_AP
//            features[2] = cal.mean(y_acceleration); // mu_V
//
//            features[3] = cal.var(x_acceleration); // sigma_ML^2
//            features[4] = cal.var(z_acceleration); // sigma_AP^2
//            features[5] = cal.var(y_acceleration); // sigma_V^2
//
//            features[6] = cal.correlate(x_acceleration,z_acceleration); // cor(ML,AP)
//            features[7] = cal.correlate(x_acceleration,y_acceleration); // cor(ML,V)
//            features[8] = cal.correlate(y_acceleration,z_acceleration); // cor(V,AP)
//
//            features[9] = cal.covariance(x_acceleration,z_acceleration); // cov(ML,AP)
//            features[10] = cal.covariance(x_acceleration,y_acceleration); // cov(ML,V)
//            features[11] = cal.covariance(y_acceleration,z_acceleration); // cov(V,AP)
//
//            features[12] = cal.max(x_acceleration) - cal.min(x_acceleration); // d_ML
//            features[13] = cal.max(z_acceleration) - cal.min(z_acceleration); // d_AP
//            features[14] = cal.max(y_acceleration) - cal.min(y_acceleration); // d_V
//            features[15] = Math.sqrt(Math.pow(features[12],2)+Math.pow(features[13],2)); // d_ML_AP
//            features[16] = Math.sqrt(Math.pow(features[12],2)+Math.pow(features[14],2)); // d_ML_V
//            features[17] = Math.sqrt(Math.pow(features[14],2)+Math.pow(features[13],2));// d_V_AP
//            features[18] = Math.sqrt(Math.pow(features[12],2)+Math.pow(features[13],2)+Math.pow(features[14],2));// d_ML_AP_V
//
//            double[] tr_x = cal.trend(x_acceleration,features[0],features[3]);
//            double[] tr_z = cal.trend(z_acceleration,features[1],features[4]);
//            double[] tr_y = cal.trend(y_acceleration,features[2],features[5]);
//            features[19] = tr_x[0];// muT_ML
//            features[20] = tr_z[0];// muT_AP
//            features[21] = tr_y[0];// muT_V
//
//            features[22] = tr_x[1];// muD_ML
//            features[23] = tr_z[1];// muD_AP
//            features[24] = tr_y[1];// muD_V
//
//            features[25] = tr_x[2];// (sigma^2)T_ML
//            features[26] = tr_z[2];// (sigma^2)T_AP
//            features[27] = tr_y[2];// (sigma^2)T_V
//
//            features[28] = tr_x[3];// (sigma^2)D_ML
//            features[29] = tr_z[3];// (sigma^2)D_AP
//            features[30] = tr_y[3];// (sigma^2)D_V
//
//            double pred = cal.correlate(features,svm_coef) + intercept;
//
//            if(pred>0){
//                textView.setText(String.format("Pred value:%f\nDRUNK",pred));
//                Log.d("Detection Result", String.format("DRUNK:%f\n",pred));
//            }
//            else{
//                textView.setText(String.format("Pred value:%f\nNOT DRUNK",pred));
//                Log.d("Detection Result", String.format("NOT DRUNK:%f\n",pred));
//            }
//            mHandler1.postDelayed(activity_recognition,1000);
//        }
//    };
//}



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.DetectedActivity;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    BroadcastReceiver broadcastReceiver;

    private TextView txtActivity, txtConfidence;
    private ImageView imgActivity;
    private Button btnStartTrcking, btnStopTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtActivity = findViewById(R.id.txt_activity);
        txtConfidence = findViewById(R.id.txt_confidence);
        imgActivity = findViewById(R.id.img_activity);
        btnStartTrcking = findViewById(R.id.btn_start_tracking);
        btnStopTracking = findViewById(R.id.btn_stop_tracking);

        Intent intent = new Intent(this,BackgroundDetectedActivitiesService.class);
        startService(intent);


    }


    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}