package com.example.hangoverassistent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class setting extends AppCompatActivity {

    private EditText mGetText;
    private EditText mGetNumber;
    private String phoneNumber;
    private String message;
    private Button mPrevious;
    private Switch sw1;
    private Switch sw2;

    String phoneNo,sms;
    boolean sms_flag;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mPrevious = (Button) findViewById(R.id.previous);
        mGetNumber = (EditText) findViewById(R.id.mGetNumber);
        mGetText = (EditText) findViewById(R.id.mGetText);

        sw1 = findViewById(R.id.sw1);
        sw2 = findViewById(R.id.sw2);


        Intent mainPage = getIntent();
        sms_flag = mainPage.getExtras().getBoolean("sms_flag");
        phoneNo = mainPage.getExtras().getString("phoneNo");
        sms = mainPage.getExtras().getString("sms");


        if(phoneNo!=null){
            mGetNumber.setText(phoneNo);
        }

        if(sms!=null){
            mGetText.setText(sms);
        }
        sw1.setChecked(sms_flag);


        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sms_flag = b;
            }
        });

        mPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNo = mGetNumber.getText().toString();
                sms = mGetText.getText().toString();

                onButtonPreviousClicked(v);
            }
        });
    }

    public void onButtonPreviousClicked(View v){
        Intent result = new Intent();
        result.putExtra("phoneNo",phoneNo);
        result.putExtra("sms",sms);
        result.putExtra("sms_flag",sms_flag);
        setResult(RESULT_OK,result);
        finish();


    }


    private boolean validNumber(String phoneNumber)
    {
        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(phoneNumber);

        if(!m.find())
            return false;
        return phoneNumber.length() == 10;
    }







}