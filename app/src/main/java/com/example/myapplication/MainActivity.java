package com.example.myapplication;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


//Implement activity with SensorEventListener so it can receive SensorEvent though onSensorChanged()
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mLinearAccelerometer;
    private Sensor mGyroscope;
    private TextView activityText;
    private TextView probabilityText;
    private TextView frequencyText;
    private ImageView imageView;
    private Ringtone ringtone;
    private boolean soundSwitch;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private List<Float> accellX, accellY, accellZ;
    private List<Float> linAcellX, linAcellY, linAcellZ;
    private List<Float> gyroX, gyroY, gyroZ;
    private static int sampleSize = 200;
    private long pastTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accellX = new ArrayList<>();
        accellY = new ArrayList<>();
        accellZ = new ArrayList<>();
        linAcellX = new ArrayList<>();
        linAcellY = new ArrayList<>();
        linAcellZ = new ArrayList<>();
        gyroX = new ArrayList<>();
        gyroY = new ArrayList<>();
        gyroZ = new ArrayList<>();

        activityText = findViewById(R.id.Text1);
        probabilityText = findViewById(R.id.Text2);
        frequencyText = findViewById(R.id.Text11);
        imageView = findViewById(R.id.image1);
        //Create instance of system sensor service, allowing access to the devices sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //Create accelerometer object from SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Loading image rotation
        imageView = findViewById(R.id.image1);
        Animation anim = AnimationUtils.loadAnimation(
                getApplicationContext(),
                R.anim.rotate_anim
        );
        imageView.startAnimation(anim);

        // Sound Notification
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);

        // get drawer for navigation view
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Sound Switch
        soundSwitch = true;

        // Navigation Highlight
        NavigationView navigationView = this.findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.main_activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Register event listener using SensorManager
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);

    }
    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mLinearAccelerometer);
        mSensorManager.unregisterListener(this, mGyroscope);
    }

    //Provides the sensor values inside the values[] array of the SensorEvent object
    @Override
    public void onSensorChanged(SensorEvent event) {
        PredictActivity();
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if(accellX.size() < sampleSize) {
                accellX.add(event.values[0]);
                accellY.add(event.values[1]);
                accellZ.add(event.values[2]);

            }
        }
        else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if(linAcellX.size() < sampleSize) {
                linAcellX.add(event.values[0]);
                linAcellY.add(event.values[1]);
                linAcellZ.add(event.values[2]);
            }
        }
        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if(gyroX.size() < sampleSize) {
                gyroX.add(event.values[0]);
                gyroY.add(event.values[1]);
                gyroZ.add(event.values[2]);
            }
        }
        //Displays how often sensor data is collected for troubleshooting(won't be in final product)
        frequencyText.setText(String.valueOf("Update frequency:"+"\n"+ "Accelerometer: "+accellX.size())+"\n"+
                         "Linear Accelerometer: "+String.valueOf(linAcellX.size())+"\n"+"Gyroscope: "+String.valueOf(gyroX.size()));
    }

    public void PredictActivity() {
        //Only does a prediction every 5 seconds for the mock model
        long currentTime = System.currentTimeMillis();
        //Runs prediction when 200 samples are collected for each sensor
        if (accellX.size() == sampleSize && linAcellX.size() == sampleSize && gyroX.size() == sampleSize && currentTime >= (pastTime + 5000) ) {
            //Append all sensor data to array to send to classifier
            List<Float> data = new ArrayList<>();
            data.addAll(accellX);
            data.addAll(accellY);
            data.addAll(accellZ);
            data.addAll(linAcellX);
            data.addAll(linAcellY);
            data.addAll(linAcellZ);
            data.addAll(gyroX);
            data.addAll(gyroY);
            data.addAll(gyroZ);

            //Send sensor data to be classified
            runClassifier(data);

            //Remove data from arrays in preparation for next classification
            accellX.clear();
            accellY.clear();
            accellZ.clear();
            linAcellX.clear();
            linAcellY.clear();
            linAcellZ.clear();
            gyroX.clear();
            gyroY.clear();
            gyroZ.clear();
            pastTime = System.currentTimeMillis();
        }
    }

    //Mock classifier for UI development purposes
    public void runClassifier(List<Float> values){
        //Generate random number to mock classification
        Random rand = new Random();
        int upperbound = 7;
        int int_random = rand.nextInt(upperbound);
        String probability = String.format("%.2f", rand.nextFloat());
        switch(int_random){
            case 0:
                imageView.setImageResource(R.drawable.standing);
                activityText.setText("Standing");
                probabilityText.setText("Probability: "+probability);
                ringtone.play();
                break;
            case 1:
                imageView.setImageResource(R.drawable.sitting);
                activityText.setText("Sitting");
                probabilityText.setText("Probability: "+probability);
                ringtone.play();
                break;
            case 2:
                imageView.setImageResource(R.drawable.jogging);
                activityText.setText("Jogging");
                probabilityText.setText("Probability: "+probability);
                ringtone.play();
                break;
            case 3:
                imageView.setImageResource(R.drawable.walking);
                activityText.setText("Walking");
                probabilityText.setText("Probability: "+probability);
                ringtone.play();
                break;
            case 4:
                imageView.setImageResource(R.drawable.upstairs);
                activityText.setText("Upstairs");
                probabilityText.setText("Probability: "+probability);
                ringtone.play();
                break;
            case 5:
                imageView.setImageResource(R.drawable.downstairs);
                activityText.setText("Downstairs");
                probabilityText.setText("Probability: "+probability);
                ringtone.play();
                break;
        }
    }

    //Provides the current accuracy of the sensor, OS may change it under certain
    //situations such as heavy processing loads or power saving mode
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // Change Activity
    public void changeActivity(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.analysis_activity){
                Intent intent = new Intent(this, AnalysingActivity.class);
                startActivity(intent);
                finish();
        }
    }

    public void SwitchSound(View view) {
        CompoundButton switchButton = (CompoundButton) view;
        soundSwitch = switchButton.isChecked();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }
}