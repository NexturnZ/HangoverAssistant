package com.example.hangoverassistent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class userlog extends AppCompatActivity {

    private Button mPrevious2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlog);
        mPrevious2 = (Button) findViewById(R.id.previous2);

        mPrevious2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                onButtonPreviousClicked(v);
            }
        });

    }



    public void onButtonPreviousClicked(View v){



        finish();


    }


}