package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.navigation.NavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

public class AnalysingActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mLinearAccelerometer;
    private Sensor mGyroscope;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private Viewport viewport ;
    private int pointsPlotted = 0;

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

        //Create instance of system sensor service, allowing access to the devices sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //Create accelerometer object from SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // get drawer for navigation view
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Navigation Highlight
        NavigationView navigationView = this.findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.analysis_activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Register event listener using SensorManager
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

    }
    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mLinearAccelerometer);
        mSensorManager.unregisterListener(this, mGyroscope);
    }

    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //initialises graph
            GraphView graph = (GraphView) findViewById(R.id.graph);
            viewport = graph.getViewport();
            // allows graph to scroll through live data
            viewport.setScrollable(true);

            // adds line series to line graph
            graph.addSeries(series);
            graph.addSeries(series1);
            graph.addSeries(series2);

            // assigns colours to each line series representing x , y,z accelerometer data
            series.setColor(Color.RED); // x axis
            series1.setColor(Color.GREEN); // y axis
            series2.setColor(Color.BLUE); // z xis

            // auto increments x axis of graph
            pointsPlotted ++;
            // adds live sensor data to initialised graph
            series.appendData(new DataPoint(pointsPlotted,event.values[0]), true , pointsPlotted);
            series1.appendData(new DataPoint(pointsPlotted,event.values[1]), true , pointsPlotted);
            series2.appendData(new DataPoint(pointsPlotted,event.values[2]), true , pointsPlotted);
            //sets min and max value of graph to auto incrementing value to allow for constant data to be displayed in graph
            viewport.setMaxX(pointsPlotted);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            viewport.setMinX(0);

            if (pointsPlotted > 10){
                viewport.setMinX(pointsPlotted - 10);
            }

            if (pointsPlotted > 100){
                pointsPlotted = 0;
                series.resetData( new DataPoint[] { new DataPoint(1,0)});
                series1.resetData( new DataPoint[] { new DataPoint(1,0)});
                series2.resetData( new DataPoint[] { new DataPoint(1,0)});
            }
        }
        else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
        }
        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
        }
    }

    //Provides the current accuracy of the sensor, OS may change it under certain
    //situations such as heavy processing loads or power saving mode
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // Change Activity
    public void changeActivity(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.main_activity:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.settings_activity:
                intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }
}