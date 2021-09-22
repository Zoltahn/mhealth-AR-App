package com.example.myapplication;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.tensorflow.lite.Interpreter;

public class Classifier extends Activity{
    private int sampleSize = 200;
    private Interpreter interpreter;

    public float[][] runClassifier(float[][][] d) {
        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //float[][][] d = toFloatArray(list);
        return doInference(d);
    }

    //load model into the apps assets
    private MappedByteBuffer loadModelFile() throws IOException {
        String MODEL_ASSETS_PATH = "RNN-WISDM_ar_ns.tflite";
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
}
