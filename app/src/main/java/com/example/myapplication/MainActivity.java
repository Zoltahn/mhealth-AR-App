package com.example.myapplication;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//Impement activity with SensorEventListener so it can receive SensorEvent though onSensorChanged()
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons (Main Menus)
        Button buttonMain = findViewById(R.id.button2);
        Button buttonAnalysis = findViewById(R.id.button3);
        Button buttonSettings = findViewById(R.id.button4);

        // Buttons (Activity Menus)
        Button buttonStand = findViewById(R.id.button7);
        Button buttonSit = findViewById(R.id.button8);
        Button buttonWalk = findViewById(R.id.button9);
        Button buttonRun = findViewById(R.id.button10);
        Button buttonLying = findViewById(R.id.button11);

        // Call Main View (Refresh)
        buttonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Call Analysis View
        buttonAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                AnalysisMenu analysisMenu = new AnalysisMenu();
                transaction.replace(R.id.linearLayout1, analysisMenu);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Call Settings View
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                SettingMenu settingMenu = new SettingMenu();
                transaction.replace(R.id.linearLayout1, settingMenu);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Call Standing Activity View
        buttonStand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                StandActivity standActivity = new StandActivity();
                transaction.replace(R.id.linearLayout1, standActivity);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Call Sitting Activity View
        buttonSit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                SitActivity sitActivity = new SitActivity();
                transaction.replace(R.id.linearLayout1, sitActivity);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Call Walking Activity View
        buttonWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                WalkActivity walkActivity = new WalkActivity();
                transaction.replace(R.id.linearLayout1, walkActivity);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Call Running Activity View
        buttonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                RunActivity runActivity = new RunActivity();
                transaction.replace(R.id.linearLayout1, runActivity);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Call Lying Activity View
        buttonLying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                LyingActivity lyingActivity= new LyingActivity();
                transaction.replace(R.id.linearLayout1, lyingActivity);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
}