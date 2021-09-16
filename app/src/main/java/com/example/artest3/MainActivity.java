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
import java.util.Random;

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
    private static int sampleSize = 128;
    private TextView frequencyText;
    private TextView probabilityText;
    private TextView sensorText;
    private TextView lAccellText;
    private TextView gyroText;

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
        lAccellText = findViewById(R.id.Text4);
        gyroText = findViewById(R.id.Text5);
    }

        //load model into the apps assets
        private MappedByteBuffer loadModelFile() throws IOException {
        String MODEL_ASSETS_PATH = "RNN - UCI_HAR.tflite";
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
                sensorText.setText("Accelerometer"+"\n"+"X: " + String.format("%.4f",event.values[0])+"\n"+"Y: "+ String.format("%.4f",event.values[1])+"\n"+"Z: " + String.format("%.4f",event.values[2])+"\n");
            }
        }
        else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if(linAcellX.size() < sampleSize) {
                linAcellX.add(event.values[0]);
                linAcellY.add(event.values[1]);
                linAcellZ.add(event.values[2]);
                lAccellText.setText("Linear"+"\n"+"X: " + String.format("%.4f",event.values[0])+"\n"+"Y: "+ String.format("%.4f",event.values[1])+"\n"+"Z: " + String.format("%.4f",event.values[2])+"\n");
            }
        }
        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if(gyroX.size() < sampleSize) {
                gyroX.add(event.values[0]);
                gyroY.add(event.values[1]);
                gyroZ.add(event.values[2]);
                gyroText.setText("Gyroscope"+"\n"+"X: " + String.format("%.4f",event.values[0])+"\n"+"Y: "+ String.format("%.4f",event.values[1])+"\n"+"Z: " + String.format("%.4f",event.values[2])+"\n");
            }
        }
        //sensorText.setText("Accelerometer"+"\n"+"X: " + String.format("%.4f",event.values[0])+"\n"+"Y: "+ String.format("%.4f",event.values[1])+"\n"+"Z: " + String.format("%.4f",event.values[2])+"\n");
        frequencyText.setText(String.valueOf("Update frequency:"+"\n"+ "Accelerometer: "+accellX.size())+"\n"+
                "Linear Accelerometer: "+String.valueOf(linAcellX.size())+"\n"+"Gyroscope: "+String.valueOf(gyroX.size()));
    }

    public void PredictActivity() {
        //Runs prediction when 200 samples are collected for each sensor
        if (accellX.size() == sampleSize && linAcellX.size() == sampleSize && gyroX.size() == sampleSize) {
            //Append all sensor data to array to send to classifier
            ArrayList<Float> data = new ArrayList<>();
            data.addAll(accellX);
            data.addAll(accellY);
            data.addAll(accellZ);
            data.addAll(linAcellX);
            data.addAll(linAcellY);
            data.addAll(linAcellZ);
            data.addAll(gyroX);
            data.addAll(gyroY);
            data.addAll(gyroZ);

//            Random r = new Random();
//            float max = 1;
//            float min = -1;
//            float theArray[][][] =  new float[1][200][3];
//            for (int i = 0; i < 200; i++) {
//                theArray[0][i][0] = r.nextFloat() * (max -  min) + min;
//            }
//            for (int i = 0; i < 200; i++) {
//                theArray[0][i][1] = r.nextFloat() * (max -  min) + min;
//            }
//            for (int i = 0; i < 200; i++) {
//                theArray[0][i][2] = r.nextFloat() * (max -  min) + min;
//            }
//            float[][] postures = doInference(theArray);
            float[][] postures = doInference(toFloatArray(data));
            probabilityText.setText("RNN - UCI_HAR Probabilities\n"+"Downstairs: "+String.format("%.2f",postures[0][0])+"\n"+"Jogging: "+
                    String.format("%.2f",postures[0][1])+"\n"+"Sitting: "+String.format("%.2f",postures[0][2])+"\n"+"Standing: "+
                    String.format("%.2f",postures[0][3])+"\n"+"Upstairs: "+String.format("%.2f",postures[0][4])+"\n"+"Walking: "+String.format("%.2f",postures[0][5]));
            //probabilityText.setText(theArray[0][100][0] + "\n" + theArray[0][100][1] + "\n" + theArray[0][100][2]);
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
        }
    }
    //convert sensor data into a 3d array in the format of float[1][200][3]
    private float[][][] toFloatArray(List<Float> list) {
        int i = 0;
        float theArray[][][] =  new float[1][sampleSize][9];
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
            else if(i < sampleSize * 4) {
                theArray[0][i - sampleSize * 3][3] = f;
            }
            else if(i < sampleSize * 5) {
                theArray[0][i - sampleSize * 4][4] = f;
            }
            else if(i < sampleSize * 6) {
                theArray[0][i - sampleSize * 5][5] = f;
            }
            else if(i < sampleSize * 7) {
                theArray[0][i - sampleSize * 6][6] = f;
            }
            else if(i < sampleSize * 8) {
                theArray[0][i - sampleSize * 7][7] = f;
            }
            else if(i < sampleSize * 9) {
                theArray[0][i - sampleSize * 8][8] = f;
            }
            i++;
        }
        //normalize values to be between 0-1
//            float min1 = findMin(theArray, 0);
//            float div1 = findMax(theArray, 0) - min1;
//            float min2 = findMin(theArray, 1);
//            float div2 = findMax(theArray, 1) - min2;
//            float min3 = findMin(theArray, 2);
//            float div3 = findMax(theArray, 2) - min3;
//            float max1 = findMax(theArray, 0);
//            float max2 = findMax(theArray, 1);
//            float max3 = findMax(theArray, 2);
            //probabilityText.setText(String.format("%.2f", max1) + "\n" + String.format("%.2f", max2));
            //probabilityText.setText(String.format("%.2f", theArray[0][50][2]));
            //probabilityText.setText(String.format("%.2f", mAccelerometer.getMaximumRange()));
//            float x_m = 0.662868f; float y_m = 7.255639f; float z_m = 0.411062f;
//             float x_s = 6.849058f; float y_s = 6.746204f; float z_s = 4.754109f;
//            for(int s = 0; s < 200; s++){
//                theArray[0][s][0] = theArray[0][s][0] / 20;
//                //theArray[0][s][0] = (theArray[0][s][0] - x_m) / x_s;
//            }
//            for(int s = 0; s < 200; s++){
//                theArray[0][s][1] = theArray[0][s][1] / 20;
//                //theArray[0][s][1] = (theArray[0][s][1] - y_m) / y_s;
//            }
//            for(int s = 0; s < 200; s++) {
//                theArray[0][s][2] = theArray[0][s][2] / 20;
//                //theArray[0][s][2] = (theArray[0][s][2] - z_m) / z_s;
//            }
        return theArray;
    }

//    private float findMax(float[][][] theArray, int index){
//        float m = theArray[0][0][index];
//        for (int i = 1; i < 200; i++) {
//            if(theArray[0][i][index] > m){
//                m = theArray[0][i][index];
//            }
//            //m = Math.max(m, theArray[0][i][index]);
//        }
//        return m;
//    }
//
//    private float findMin(float[][][] theArray, int index){
//        float m = theArray[0][0][index];
//        for (int i = 1; i < 200; i++) {
//            if(theArray[0][i][index] < m) {
//                m = theArray[0][i][index];
//            }
//            //m = Math.min(m, theArray[0][i][index]);
//        }
//        return m;
//    }

    //Provides the current accuracy of the sensor, OS may change it under certain
    //situations such as heavy processing loads or power saving mode
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}