package com.example.artest3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.tensorflow.lite.Interpreter;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Interpreter interpreter;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mLinearAccelerometer;
    private Sensor mGyroscope;
    private List<Float> accellX, accellY, accellZ;
    private List<Float> linAcellX, linAcellY, linAcellZ;
    private List<Float> gyroX, gyroY, gyroZ;
    private static int sampleSize = 200;
    private TextView frequencyText;
    private TextView probabilityText;
    private TextView sensorText;
    private TextView arrayText;

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

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //load model file, initialise interpreter
        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (Exception ex){
            ex.printStackTrace();
        }

        frequencyText = findViewById(R.id.Text1);
        probabilityText = findViewById(R.id.Text2);
        sensorText = findViewById(R.id.Text3);
        //arrayText = findViewById(R.id.Text4);
    }

        //load model into the apps assets
        private MappedByteBuffer loadModelFile() throws IOException {
        String MODEL_ASSETS_PATH = "CNN_test - WISDM_at.tflite";
        AssetFileDescriptor assetFileDescriptor = this.getAssets().openFd(MODEL_ASSETS_PATH);
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor() );
        FileChannel fileChannel = fileInputStream.getChannel();
        long startoffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength() ;
        return fileChannel.map( FileChannel.MapMode.READ_ONLY, startoffset, declaredLength );
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
        sensorText.setText("Accelerometer"+"\n"+"X: " +event.values[0]+"\n"+"Y: "+event.values[1]+"\n"+"Z: " +event.values[2]+"\n");
        frequencyText.setText(String.valueOf("Update frequency:"+"\n"+ "Accelerometer: "+accellX.size())+"\n"+
                "Linear Accelerometer: "+String.valueOf(linAcellX.size())+"\n"+"Gyroscope: "+String.valueOf(gyroX.size()));
    }

    public void PredictActivity() {
        //Runs prediction when 200 samples are collected for each sensor
        if (accellX.size() == sampleSize) {
            //Append all sensor data to array to send to classifier
            ArrayList<Float> data = new ArrayList<>();
            data.addAll(accellX);
            data.addAll(accellY);
            data.addAll(accellZ);
            //Send sensor data to be classified
            float[][] postures = doInference(toFloatArray(data));
            probabilityText.setText("CNN WISM Probabilities\n"+"Downstairs: "+String.valueOf(postures[0][0])+"\n"+"Jogging: "+
                    String.valueOf(postures[0][1])+"\n"+"Sitting: "+String.valueOf(postures[0][2])+"\n"+"Standing: "+
                    String.valueOf(postures[0][3])+"\n"+"Upstairs: "+String.valueOf(postures[0][4])+"\n"+"Walking: "+String.valueOf(postures[0][5]));
            //Remove data from arrays in preparation for next classification
            accellX.clear();
            accellY.clear();
            accellZ.clear();
        }
    }

    //convert sensor data into a 3d array in the format of float[1][200][3]
    private float[][][] toFloatArray(List<Float> list) {
        int i = 0;
        int k = 0;
        int t = 0;
        float theArray[][][] =  new float[1][200][3];
        for (Float f : list) {
            if(i < 200) {
                theArray[0][i][0] = f;
            }
            else if(i < 400) {
                theArray[0][k][1] = f;
                k++;
            }
            else if(i < 600) {
                theArray[0][t][2] = f;
                t++;
            }
            i++;
        }
//        arrayText.setText("aX: " + String.valueOf(theArray[0][100][0]) + "\n" + "aY " + String.valueOf(theArray[0][100][1]) + "\n"
//                + "aZ: " + String.valueOf(theArray[0][100][2]));
        return theArray;
    }

    //Provides the current accuracy of the sensor, OS may change it under certain
    //situations such as heavy processing loads or power saving mode
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}