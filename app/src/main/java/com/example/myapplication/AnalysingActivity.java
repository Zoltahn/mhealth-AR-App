package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.navigation.NavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnalysingActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mLinearAccelerometer;
    private Sensor mGyroscope;
    private Interpreter interpreter;
    private static int sampleSize = 200;
    private List<Float> accellX, accellY, accellZ;
    //    private List<Float> linAcellX, linAcellY, linAcellZ;
//    private List<Float> gyroX, gyroY, gyroZ;
    private TextView probabilityText;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private Viewport viewport;
    private int pointsPlotted = 0;
    Date date = new Date();
    long time = date.getTime();
    public static int act = 1;
    private TextView sensValue, metric;

    //initialise line series according to tri-axial data
    LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]  {
            // x axis
    });
    LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(new DataPoint[] {
            // y axis
    });
    LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(new DataPoint[] {
            // z axis
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysing);

        accellX = new ArrayList<>();
        accellY = new ArrayList<>();
        accellZ = new ArrayList<>();
//        linAcellX = new ArrayList<>();
//        linAcellY = new ArrayList<>();
//        linAcellZ = new ArrayList<>();
//        gyroX = new ArrayList<>();
//        gyroY = new ArrayList<>();
//        gyroZ = new ArrayList<>();

        //Create instance of system sensor service, allowing access to the devices sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //Create accelerometer object from SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        probabilityText = findViewById(R.id.probText);

        //load model file, initialise interpreter
        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (Exception ex){
            ex.printStackTrace();
        }

        // get drawer for navigation view
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Navigation Highlight
        NavigationView navigationView = this.findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.analysis_activity);

        Button buttonAccel = findViewById(R.id.sel1);
        Button buttonLinAccel = findViewById(R.id.sel2);
        Button buttonGyro = findViewById(R.id.sel3);

        metric = findViewById(R.id.text8);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        viewport = graph.getViewport();
        // allows graph to scroll through live data
        viewport.setXAxisBoundsManual(true);
        viewport.setScrollable(true);
        graph.getLegendRenderer().setVisible(true);

        // adds line series to line graph
        graph.addSeries(series);
        graph.addSeries(series1);
        graph.addSeries(series2);

        // assigns colours to each line series representing x , y,z accelerometer data
        series.setColor(Color.RED); // x axis
        series1.setColor(Color.GREEN); // y axis
        series2.setColor(Color.BLUE); // z xis

        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.NONE );

        series.setTitle("X");
        series1.setTitle("Y");
        series2.setTitle("Z");
        buttonGyro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                act = 3;
            }
        });

        buttonAccel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                act = 1;

            }
        });
        buttonLinAccel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view  ) {
                act = 2;
            }
        });
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
        //mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        //mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);

    }
    public static void ResetData(){

    }
    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        //mSensorManager.unregisterListener(this, mLinearAccelerometer);
        //mSensorManager.unregisterListener(this, mGyroscope);
    }

    public void onSensorChanged(SensorEvent event) {
        //extra clause to validate whether user has  chosen specified sensor
        PredictActivity();
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && act== 1){
            metric.setText("m/s");

            //initialises graph
            // auto increments x axis of graph
            pointsPlotted ++;
            // adds live sensor data to initialised graph
            series.appendData(new DataPoint(pointsPlotted,event.values[0]), true , pointsPlotted);
            series1.appendData(new DataPoint(pointsPlotted,event.values[1]), true , pointsPlotted);
            series2.appendData(new DataPoint(pointsPlotted,event.values[2]), true , pointsPlotted);
            //sets min and max value of graph to auto incrementing value to allow for constant data to be displayed in graph
            viewport.setMaxX(pointsPlotted);

            viewport.setMinX(0);

            if (pointsPlotted > 10){
                viewport.setMinX(pointsPlotted - 10);
            }

            if (pointsPlotted > 200 ){
                pointsPlotted = 0;
                series.resetData( new DataPoint[] { new DataPoint(0,0)});
                series1.resetData( new DataPoint[] { new DataPoint(0,0)});
                series2.resetData( new DataPoint[] { new DataPoint(0,0)});
            }
            accellX.add(event.values[0]);
            accellY.add(event.values[1]);
            accellZ.add(event.values[2]);
        }
        else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION && act== 2) {
            metric.setText("m/s^2");
            // auto increments x axis of graph
            pointsPlotted ++;
            // adds live sensor data to initialised graph
            series.appendData(new DataPoint(pointsPlotted,event.values[0]), true , pointsPlotted);
            series1.appendData(new DataPoint(pointsPlotted,event.values[1]), true , pointsPlotted);
            series2.appendData(new DataPoint(pointsPlotted,event.values[2]), true , pointsPlotted);
            //sets min and max value of graph to auto incrementing value to allow for constant data to be displayed in graph
            viewport.setMaxX(pointsPlotted);

            viewport.setMinX(0);

            if (pointsPlotted > 10){
                viewport.setMinX(pointsPlotted - 10);
            }

            if (pointsPlotted > 200){
                pointsPlotted = 0;
                series.resetData( new DataPoint[] { new DataPoint(0,0)});
                series1.resetData( new DataPoint[] { new DataPoint(0,0)});
                series2.resetData( new DataPoint[] { new DataPoint(0,0)});
            }

        }
        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE && act== 3) {
            metric.setText("°/s");
            GraphView graph = (GraphView) findViewById(R.id.graph);
            // auto increments x axis of graph
            pointsPlotted ++;
            // adds live sensor data to initialised graph
            series.appendData(new DataPoint(pointsPlotted,event.values[0]), true , pointsPlotted);
            series1.appendData(new DataPoint(pointsPlotted,event.values[1]), true , pointsPlotted);
            series2.appendData(new DataPoint(pointsPlotted,event.values[2]), true , pointsPlotted);
            //sets min and max value of graph to auto incrementing value to allow for constant data to be displayed in graph
            viewport.setMaxX(pointsPlotted);
            viewport.setMinX(0);

            if (pointsPlotted > 10){
                viewport.setMinX(pointsPlotted - 10);
            }

            if (pointsPlotted > 200){
                pointsPlotted = 0;
                series.resetData( new DataPoint[] { new DataPoint(0,0)});
                series1.resetData( new DataPoint[] { new DataPoint(0,0)});
                series2.resetData( new DataPoint[] { new DataPoint(0,0)});
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

            probabilityText.setText("Probabilities:\n"+"Downstairs: "+String.format("%.2f",postures[0][0])+"\n"+"Jogging: "+
                    String.format("%.2f",postures[0][1])+"\n"+"Sitting: "+String.format("%.2f",postures[0][2])+"\n"+"Standing: "+
                    String.format("%.2f",postures[0][3])+"\n"+"Upstairs: "+String.format("%.2f",postures[0][4])+"\n"+"Walking: "+String.format("%.2f",postures[0][5]));

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

    //Provides the current accuracy of the sensor, OS may change it under certain
    //situations such as heavy processing loads or power saving mode
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // Change Activity
    public void changeActivity(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.main_activity){
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
        } else if (menuItem.getItemId() == R.id.credits_activity) {
            Intent intent = new Intent(this, CreditActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }
}