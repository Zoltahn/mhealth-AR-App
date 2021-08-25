package com.example.myapplication;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

//Impement activity with SensorEventListener so it can receive SensorEvent though onSensorChanged()
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.Text1);
        //Create instance of system sensor service, allowing access to the devices sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //Create accelerometer object from SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    //Using onResume/onPause methods will turn off the listener whilst it's in the background,
    //can register the listener in onCreate to keep it running continuously
    @Override
    public void onResume() {
        super.onResume();
        //Register event listener using SensorManager
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    //Provides the sensor values inside the values[] array of the SensorEvent object
    @Override
    public void onSensorChanged(SensorEvent event) {
        textView.setText("X: " +event.values[0]+"\n"+"Y: "+event.values[1]+"\n"+"Z: " +event.values[2]+"\n");
    }

    //Provides the current accuracy of the sensor, OS may change it under certain
    //situations such as heavy processing loads or power saving mode
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}