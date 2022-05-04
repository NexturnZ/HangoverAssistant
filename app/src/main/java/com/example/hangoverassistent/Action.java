package com.example.hangoverassistent;

import android.os.Parcelable;
import java.io.Serializable;

public class Action implements Serializable {
    public String type;
    public long time;

    public Action(String type,long time){
        this.type = type;
        this.time = time;
    }
}
