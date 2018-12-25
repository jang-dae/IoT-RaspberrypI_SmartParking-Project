package com.example.user.smartparking;

import java.sql.Time;

public class Parking {
    String userId;
    long startTime;
    //long startTime;
    long endTime;
    int fee;

    public Parking(){} //호출을 위한 기본 생성자

    public Parking(String id, long st){
        this.userId = id;
        this.startTime = st;
    }
    public Parking(String id, long st,long et, int fee){
        this.userId = id;
        this.startTime = st;
        this.endTime = et;
        this.fee = fee;
    }

    public String getUserId() {
        return userId;
    }

    public long getTime() {
        return startTime;
    }
}
