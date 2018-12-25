package com.example.user.smartparking;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button btncal;  //정산버튼
    Button btncheckFee; //요금확인버튼
    Button a;   //자리 a
    String statusA;    //자리 a의 주차유무 변수(0 = 주차중, 1 = 주차가능)
    String userID = "cho";  //임의 사용자 id
    String slot = "A";
    int fee = 2000; //주차 기본 요금
    long startM;    //주차시작시간 millis단위
    long startTime = startM / (1000 * 60 *60);; //주차시작시간 시간 단위
    long endM; //출차시간 millis단위
    long endTime = endM / (1000 * 60 *60);   //시간 단위 변환, 출차시간 or 요금확인시간
    String TAB = "MainActivity";
    FirebaseDatabase database;
    DatabaseReference sensorRef, parkingRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();

        parkingRef = database.getReference("Parking");
        sensorRef = database.getReference("available");



        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                   // statusA = "" + snapshot.getValue();
                   statusA= snapshot.getValue().toString();
                   if(statusA.equals("false")){
                        a = (Button)findViewById(R.id.a);
                        a.setEnabled(false);
                    }
                    if(statusA.equals("true")){
                        a = (Button)findViewById(R.id.a);
                        a.setEnabled(true);
                    }
                    Log.d(TAB, "available: " + statusA);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        parkingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Parking parking = new Parking();
                    parking = snapshot.getValue(Parking.class);
                    startM = parking.getTime();
                    userID = parking.getUserId();
                    Log.d(TAB, "chohee: " + startM);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void park(View v){
        switch (v.getId()){
            case R.id.a:
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                if(statusA.equals("true")){
                    alert.setMessage("주차 가능한 자리입니다. 주차하시겠습니까?");
                    alert.setPositiveButton("예", new DialogInterface.OnClickListener() {


                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startM =  System.currentTimeMillis();  //현재시간 얻기(Millis단위)
                            Date startDate = new Date(startM);  //사용자에게 보여주기 위해 Date타입으로 변환

                            Parking parking = new Parking(userID,startM);   //주차장을 사용하는 사용자 아이디와 시작시간을 보냄
                            parkingRef.child(slot).setValue(parking);   //구역별로 데이터를 씀

                            Available available = new Available("false");
                            //USsensor ussensor = new USsensor(false);
                            sensorRef.setValue(available); //상태 변경

                            Toast.makeText(MainActivity.this, userID+"님 주차 완료! \n" + startDate, Toast.LENGTH_SHORT).show();
                            Button a = (Button)findViewById(R.id.a);
                            a.setEnabled(false);    //자리 비활성화
                        }
                    });
                    alert.setNegativeButton("아니오", null);
                    alert.show();
                }else{
                    alert.setMessage("!** 주차 불가능 **! \n다른 자리를 찾아보세요");
                    alert.setNegativeButton("확인", null);
                    alert.show();
                }
                break;
        }
    }

    public void calClick(View v){
        btncal = findViewById(R.id.btncal);
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        if(statusA.equals("false")){
            endM =  System.currentTimeMillis();    //현재 시간 얻기

            int parkingTime = (int)(endTime - startTime);
            fee += parkingTime * 1000;

            alert.setMessage("출차하시겠습니까?\n" + "이용시간 : " + parkingTime + "\n이용요금 : " + fee);

            alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //USsensor ussensor = new USsensor(true);
                    Available available = new Available("true");
                    sensorRef.setValue(available);

                    Parking parking = new Parking(userID,startM, endM, fee);   //주차장을 사용하는 사용자 아이디와 시작시간을 보냄
                    parkingRef.child(slot).setValue(parking);   //구역별로 데이터를 씀

                    Toast.makeText(MainActivity.this, "이용해주셔서 감사합니다!", Toast.LENGTH_SHORT).show();
                    Button a = (Button)findViewById(R.id.a);
                    a.setEnabled(true);    //자리 비활성화
                }
            });
            alert.setNegativeButton("아니오", null);
            alert.show();
        }
        else{
            alert.setMessage(userID + "님의 주차이용내역이 없습니다.");
            alert.setNegativeButton("확인", null);
            alert.show();
        }

    }
    //요금확인
    public void checkFeeClick(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        if(statusA.equals("false")){
            endM =  System.currentTimeMillis();    //현재 시간 얻기

            int parkingTime = (int)(endTime - startTime);
            fee += parkingTime * 1000;

            alert.setMessage(userID + "님의 이용시간 : " + parkingTime + "시간 \n요금 : " + fee + "원");
            alert.setNegativeButton("확인", null);
            alert.show();
        }else{
            alert.setMessage(userID + "님의 주차이용내역이 없습니다.\n 주차 후 확인해주세요 ");
            alert.setNegativeButton("확인", null);
            alert.show();
        }

    }
}
