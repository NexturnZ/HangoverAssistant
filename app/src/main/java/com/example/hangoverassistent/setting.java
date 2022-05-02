package com.example.hangoverassistent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class setting extends AppCompatActivity {

    private EditText mGetText;
    private EditText mGetNumber;
    private Button mSendBtn;
    private String phoneNumber;
    private String message;
    private Button mPrevious;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mPrevious = (Button) findViewById(R.id.previous);
        mGetNumber = (EditText) findViewById(R.id.mGetNumber);
        mGetText = (EditText) findViewById(R.id.mGetText);
        mSendBtn = (Button) findViewById(R.id.mSendBtn);

        mPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPreviousClicked(v);
            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


                String phoneNo = mGetNumber.getText().toString();
                String sms = mGetText.getText().toString();


                if(!validNumber(phoneNumber))
                {
                    Toast.makeText(setting.this, "Invalid phone number", Toast.LENGTH_LONG).show();
                    return;
                } else if (message == null)
                {
                    Toast.makeText(setting.this, "Message cannot be empty", Toast.LENGTH_LONG).show();
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






    }

    public void onButtonPreviousClicked(View v){
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