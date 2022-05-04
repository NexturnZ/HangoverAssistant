package com.example.hangoverassistent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class userlog extends AppCompatActivity {

    private Button mPrevious2;
    private LineChart lineChart;
    private TextView label1,label2,label3,label4,actionHistoryView;

    private ArrayList<Integer> detection;
    private long starting_time, ending_time, oneThird_time, twoThird_time;
    private Action actionHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlog);
        mPrevious2 = findViewById(R.id.previous2);

        mPrevious2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPreviousClicked(v);
            }
        });

        Intent MainPage = getIntent();
        detection = MainPage.getExtras().getIntegerArrayList("detection");
        starting_time = MainPage.getExtras().getLong("starting_time");
        ending_time = MainPage.getExtras().getLong("ending_time");
        actionHistory = (Action)MainPage.getSerializableExtra("actionHistory");


        oneThird_time = starting_time + (ending_time-starting_time)/3;
        twoThird_time = starting_time + (ending_time-starting_time)*2/3;


        label1 = findViewById(R.id.time1);
        label2 = findViewById(R.id.time2);
        label3 = findViewById(R.id.time3);
        label4 = findViewById(R.id.time4);
        actionHistoryView = findViewById(R.id.actionHistory);


        lineChart = findViewById(R.id.chart);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* transform  timestamp to date */
        String xlabel_start = labelGeneration(starting_time,2);
        String xlabel_onethird = labelGeneration(oneThird_time,2);
        String xlabel_twothird = labelGeneration(twoThird_time,2);
        String xlabel_end = labelGeneration(ending_time,2);

        label1.setText(xlabel_start);
        label2.setText(xlabel_onethird);
        label3.setText(xlabel_twothird);
        label4.setText(xlabel_end);

        if(actionHistory != null){
            String action = "1." + actionHistory.type + ": " + labelGeneration(actionHistory.time,1);
            actionHistoryView.setText(action);
        }





        //data entry
        List<Entry> entries = new ArrayList<>();
        for(int i1=0;i1<detection.size();i1++){
            entries.add(new Entry(i1,detection.get(i1)));
        }

        //Line dataset and colors
        LineDataSet lineDataSet = new LineDataSet(entries,"");


        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(0);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);



        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);



        //X axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        xAxis.enableGridDashedLine(8, 24, 0);




        //Y axis
        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(true);
        yRAxis.setDrawAxisLine(true);
        yRAxis.setDrawGridLines(false);


        //show  Y axis at left
        lineChart.getAxisLeft().setEnabled(true);


        Description description = new Description();
        description.setText("");

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.animateY(2000, Easing.EaseInCubic);
        lineChart.invalidate();

    }

    public void onButtonPreviousClicked(View v){
        finish();
    }

    private String labelGeneration(long time, int line){
        String time_str = String.valueOf(time);
        SimpleDateFormat s3;
        if(line==2){
            s3 = new SimpleDateFormat("YYYY-MM-dd\nHH:mm:ss");
        }
        else{
            s3 = new SimpleDateFormat("YYYY-MM-dd:HH:mm");
        }


        return s3.format(Long.parseLong(time_str));
    }

}