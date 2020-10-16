package com.chakapon.flowopengl;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private GLSurfaceView glSurfaceView;
    private OpenGLRenderer openGLRenderer;
    private ConstraintLayout constraintLayout;
    private float touchX, touchY;
    private boolean isFingerDown;
    private boolean isDoubleTapped;
    private final String LOG_TAG = "MainActivity";
    private long lastTappedTime;
    private long lastDoubleTappedTime;
    private long back_pressed;

    //Работа с сенсорами
    SensorManager sensorManager;
    Sensor sensorAccel;
    float[] valuesAccel = new float[3];



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!supportES2()) {
            Toast.makeText(this, "OpenGL ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //Сенсоры
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_main);
        constraintLayout = findViewById(R.id.constraintLayout);
        isFingerDown = false;
        isDoubleTapped = false;
        constraintLayout.setOnTouchListener(this);

        back_pressed=lastTappedTime=System.currentTimeMillis()-5000;

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        openGLRenderer = new OpenGLRenderer(this);
        openGLRenderer.loadInfo();//Загружай данные

        glSurfaceView.setRenderer(openGLRenderer);
        constraintLayout.addView(glSurfaceView);

        Log.wtf(LOG_TAG,"onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        sensorManager.unregisterListener(listener);//Сенсоры
        Log.wtf(LOG_TAG,"onPause");
    }

    @Override
    public void onBackPressed() {
        //Log.wtf(LOG_TAG, "onBackPressed");
        if (back_pressed+2000>System.currentTimeMillis()) {
            super.onBackPressed();
        }
        else{
            Toast toast = Toast.makeText(getBaseContext(),"Глубина, глубина, я не твой...\nОтпусти меня, глубина...",Toast.LENGTH_SHORT);
            TextView textView = toast.getView().findViewById(android.R.id.message);
            if (textView!=null) textView.setGravity(Gravity.CENTER);
            toast.show();//Сообщение о выходе
        }
        back_pressed=System.currentTimeMillis();
    }

    @SuppressLint("ClickableViewAccessibility")//test Убрать предупреждение
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // нажатие

                isFingerDown = true;
                long currentTouchTime=System.currentTimeMillis();
                if (currentTouchTime-lastTappedTime<=500){//Тапнули дважды
                    lastDoubleTappedTime=currentTouchTime;
                }
                lastTappedTime=currentTouchTime;

                break;
            case MotionEvent.ACTION_MOVE: // движение
                break;
            case MotionEvent.ACTION_UP: // отпускание
            case MotionEvent.ACTION_CANCEL:
                lastDoubleTappedTime=lastDoubleTappedTime-5000;//Убрать эффект от даблтапа при отпускании
                isFingerDown = false;
                isDoubleTapped = false;

                break;
        }

        //Даблтапнули, ускорение не закончилось, палец не убрали
        isDoubleTapped = (System.currentTimeMillis() - lastDoubleTappedTime < 5000) && (isFingerDown);
        openGLRenderer.sendTouch(isFingerDown, isDoubleTapped, touchX, touchY);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_GAME);//Сенсоры
        openGLRenderer.setIsFirstTimeTrue();
        Log.wtf(LOG_TAG,"onResume");
    }


    @Override
    protected void onStart() {
        super.onStart();Log.wtf(LOG_TAG,"onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();Log.wtf(LOG_TAG,"onDestroy");
    }

    private boolean supportES2() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i = 0; i < 3; i++) {
                        valuesAccel[i] = event.values[i];
                    }
                    for (int i = 0; i < 3; i++) {
                        if (Float.isNaN(valuesAccel[i])){ Log.wtf(LOG_TAG,"NYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAN");break;}
                    }
                    openGLRenderer.sendValuesAccel(valuesAccel);
                    break;
            }
        }
    };

}
