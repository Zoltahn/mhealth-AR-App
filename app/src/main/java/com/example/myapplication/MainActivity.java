package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.media.MediaPlayer;
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
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.tensorflow.lite.Interpreter;


//Implement activity with SensorEventListener so it can receive SensorEvent though onSensorChanged()
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Interpreter interpreter;
    private static int sampleSize = 200;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
//    private Sensor mLinearAccelerometer;
//    private Sensor mGyroscope;
    private TextView activityText;
    private TextView probabilityText;
    private ImageView imageView;
    private Ringtone ringtone;
    private boolean soundSwitch;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private List<Float> accellX, accellY, accellZ;
    private Classifier Classifier = new Classifier();
    private int prevPostureNum = -1;
    public static String file = "log.txt";
    private int count = 0;
    String Predictions;
    String logged;

    ArrayList<String> logs = new ArrayList<String>();

    private int counter = 0;
    private MediaPlayer soundWalk, soundJog, soundSit, soundStand, soundLie, soundUp, soundDown;
    private String File = "log.txt";
//    private List<Float> linAcellX, linAcellY, linAcellZ;
//    private List<Float> gyroX, gyroY, gyroZ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //sound files for activities
        soundJog = MediaPlayer.create(this, R.raw.jogging);
        soundSit = MediaPlayer.create(this, R.raw.sitting);
        soundStand = MediaPlayer.create(this, R.raw.standing);
        soundLie = MediaPlayer.create(this, R.raw.lying);
        soundUp = MediaPlayer.create(this, R.raw.upstairs);
        soundDown = MediaPlayer.create(this, R.raw.downstairs);
        soundWalk = MediaPlayer.create(this, R.raw.walking);

        accellX = new ArrayList<>();
        accellY = new ArrayList<>();
        accellZ = new ArrayList<>();
//        linAcellX = new ArrayList<>();
//        linAcellY = new ArrayList<>();
//        linAcellZ = new ArrayList<>();
//        gyroX = new ArrayList<>();
//        gyroY = new ArrayList<>();
//        gyroZ = new ArrayList<>();

        activityText = findViewById(R.id.Text1);
        probabilityText = findViewById(R.id.Text2);
        imageView = findViewById(R.id.image1);
        //Create instance of system sensor service, allowing access to the devices sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //Create accelerometer object from SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //load model file, initialise interpreter
        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (Exception ex){
            ex.printStackTrace();
        }

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

    //load model into the apps assets
    private MappedByteBuffer loadModelFile() throws IOException {
        String MODEL_ASSETS_PATH = "CNN_test_-_WISDM_ar_ns.tflite";
        AssetFileDescriptor assetFileDescriptor = this.getAssets().openFd(MODEL_ASSETS_PATH);
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor() );
        FileChannel fileChannel = fileInputStream.getChannel();
        long startoffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength() ;
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength);
    }

    //Send data to interpreter, get results back
    public float[][] doInference(float[][][] data){
        float arr[][] = new float[1][6];
        interpreter.run(data, arr);
        return arr;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Register event listener using SensorManager
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
//        mSensorManager.unregisterListener(this, mLinearAccelerometer);
//        mSensorManager.unregisterListener(this, mGyroscope);
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
//        else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
//            if(linAcellX.size() < sampleSize) {
//                linAcellX.add(event.values[0]);
//                linAcellY.add(event.values[1]);
//                linAcellZ.add(event.values[2]);
//            }
//        }
//        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            if(gyroX.size() < sampleSize) {
//                gyroX.add(event.values[0]);
//                gyroY.add(event.values[1]);
//                gyroZ.add(event.values[2]);
//            }
//        }
    }
    public void save(){
        FileOutputStream fos = null;
        String s = Predictions;
        try {
            logs.add(s);
            String z = String.valueOf(logs);
            if (logs.size() > 10){
                for (String x : logs)
                {
                    logged += s + "\n";
                }
                logs.clear();
                logged = " ";
                fos = openFileOutput(file,MODE_PRIVATE);
                fos.write("\n".getBytes());
                fos.write(z.getBytes());

            };

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void PredictActivity() {
        //Runs prediction when 200 samples are collected for each sensor
        if (accellX.size() == sampleSize) {
            //&& linAcellX.size() == sampleSize && gyroX.size() == sampleSize && currentTime >= (pastTime + 5000)
            //Append all sensor data to array to send to classifier
            List<Float> data = new ArrayList<>();
            data.addAll(accellX);
            data.addAll(accellY);
            data.addAll(accellZ);
//            data.addAll(linAcellX);
//            data.addAll(linAcellY);
//            data.addAll(linAcellZ);
//            data.addAll(gyroX);
//            data.addAll(gyroY);
//            data.addAll(gyroZ);

            //Send sensor data to be classified
            //Classifier.runClassifier(data);
            float[][] postures = doInference(toFloatArray(data));
            //float[][] postures = Classifier.runClassifier(toFloatArray(data));


            float max = postures[0][0];
            int postureNum = 0;
            for(int i = 1; i < postures[0].length; i++){
                if(postures[0][i] > max){
                        max = postures[0][i];
                        postureNum = i;
                }
            }

            displayPrediction(postureNum, max);


            Predictions = "Probabilities:\n"+"Downstairs: "+String.format("%.2f",postures[0][0])+"\n"+"Jogging: "+
                    String.format("%.2f",postures[0][1])+"\n"+"Sitting: "+String.format("%.2f",postures[0][2])+"\n"+"Standing: "+
                    String.format("%.2f",postures[0][3])+"\n"+"Upstairs: "+String.format("%.2f",postures[0][4])+"\n"+"Walking: "+String.format("%.2f",postures[0][5]);
            save();
            //Remove data from arrays in preparation for next classification
            accellX.clear();
            accellY.clear();
            accellZ.clear();
//            linAcellX.clear();
//            linAcellY.clear();
//            linAcellZ.clear();
//            gyroX.clear();
//            gyroY.clear();
//            gyroZ.clear();
        }
    }

    private float[][][] toFloatArray(List<Float> list) {
        int i = 0;
        int signalAmount = 3;
        float theArray[][][] =  new float[1][sampleSize][signalAmount];
        for (Float f : list) {
            if(i < sampleSize) {
                theArray[0][i][0] = f;
            }
            else if(i < sampleSize * 2) {
                theArray[0][i - sampleSize][1] = f;
            }
            else if(i < sampleSize * 3) {
                theArray[0][i - sampleSize * 2][2] = f;
            }
            i++;
        }
        return theArray;
    }

    //Mock classifier for UI development purposes
    public void displayPrediction(int postureNum, float probability){

        switch(postureNum){
            case 0:
                imageView.setImageResource(R.drawable.downstairs);
                activityText.setText("Downstairs");
                probabilityText.setText("Probability: "+String.format("%.2f", probability));
                if(postureNum != prevPostureNum && soundSwitch == true){
                    if(soundSwitch) soundDown.start();
                    prevPostureNum = postureNum;
                }

                break;
            case 1:
                imageView.setImageResource(R.drawable.jogging);
                activityText.setText("Jogging");
                probabilityText.setText("Probability: "+String.format("%.2f", probability));
                if(postureNum != prevPostureNum && soundSwitch == true){
                    if(soundSwitch) soundJog.start();
                    prevPostureNum = postureNum;
                }

                break;
            case 2:
                imageView.setImageResource(R.drawable.sitting);
                activityText.setText("Sitting");
                probabilityText.setText("Probability: "+String.format("%.2f", probability));
                if(postureNum != prevPostureNum && soundSwitch == true){
                    if(soundSwitch) soundSit.start();
                    prevPostureNum = postureNum;
                }

                break;
            case 3:
                imageView.setImageResource(R.drawable.standing);
                activityText.setText("Standing");
                probabilityText.setText("Probability: "+String.format("%.2f", probability));
                if(postureNum != prevPostureNum && soundSwitch == true){
                    if(soundSwitch) soundStand.start();
                    prevPostureNum = postureNum;
                }

                break;
            case 4:
                imageView.setImageResource(R.drawable.upstairs);
                probabilityText.setText("Probability: "+String.format("%.2f", probability));
                if(postureNum != prevPostureNum && soundSwitch == true){
                    if(soundSwitch) soundUp.start();
                    prevPostureNum = postureNum;
                }

                break;
            case 5:
                imageView.setImageResource(R.drawable.walking);
                probabilityText.setText("Probability: "+String.format("%.2f", probability));
                if(postureNum != prevPostureNum && soundSwitch == true){
                    if(soundSwitch) soundWalk.start();
                    prevPostureNum = postureNum;
                }
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
        } else if (menuItem.getItemId() == R.id.credits_activity) {
            Intent intent = new Intent(this, CreditActivity.class);
            startActivity(intent);
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