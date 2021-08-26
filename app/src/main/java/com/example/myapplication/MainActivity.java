package com.example.myapplication;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Call Analysis View
        buttonAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AnalysingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Call Settings View
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });

        // Call Standing Activity View
        buttonStand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StandActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Call Sitting Activity View
        buttonSit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SitActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Call Walking Activity View
        buttonWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WalkActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Call Running Activity View
        buttonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RunActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Call Standing Activity View
        buttonLying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LyingActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}