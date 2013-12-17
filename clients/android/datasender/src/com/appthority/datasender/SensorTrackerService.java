package com.appthority.datasender;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.util.Timer;
import android.os.AsyncTask;
import android.os.Binder;
import java.util.TimerTask;
import java.util.List;

/**
 * Created by kwatts on 5/18/13.
 */

    public class SensorTrackerService extends Service implements SensorEventListener {

        public static final String MSG_TAG = "OfficerUP -> SensorTracker";
        private final IBinder mBinder = new MyBinder();


        // flag for network status
        boolean isSensorEnabled = false;

        public  SensorManager mSensorManager;
        public  Sensor mSensor;



        public float mAccel; // acceleration apart from gravity
        public float mAccelCurrent; // current acceleration including gravity
        public float mAccelLast; // last acceleration including gravity

        public float mLightValue = 0.0f;

        public float mAcceleration = 0.0f;
        public float mPressure_Value = 0.0f;
        public float mHeight = 0.0f;

        public float x,y,z = 0f;
        public String mLight = "UNKNOWN";
        public String mOrientation = "UNKNOWN";
        public String mMovement = "UNKNOWN";

        public String mAllSensors;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.d(MSG_TAG, "Sensor Service started");
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            List sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            StringBuilder message = new StringBuilder(2048);
            for(int i=0; i<sensors.size(); i++) {
                Sensor sensor = (Sensor) sensors.get(i);
                message.append(sensor.getName() + "\n");
                message.append("  Type: " + sensor.getType() + "\n");
                message.append("  Vendor: " + sensor.getVendor() + "\n");
                message.append("  Version: " + sensor.getVersion() + "\n");
                message.append("  Resolution: " + sensor.getResolution() + "\n");
                message.append("  Max Range: " + sensor.getMaximumRange() + "\n");
                message.append("  Power: " + sensor.getPower() + " mA\n");
                message.append("---------------\n");
            }
            mAllSensors = message.toString();
            Log.d(MSG_TAG, mAllSensors);

            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
           // return START_STICKY;
            return Service.START_NOT_STICKY;
        }

        @Override
        public void onDestroy(){
            Log.v(MSG_TAG,"Sensor Service killed");
            mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
            mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
            mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
            super.onDestroy();
        }


       public void stop() {
           Log.v(MSG_TAG,"Sensor Service killed");
           mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
           mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
           mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
           mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
       }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            if(Sensor.TYPE_PRESSURE == sensorEvent.sensor.getType()) {
                mPressure_Value = sensorEvent.values[0];
                mHeight = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, mPressure_Value);
                Log.d(MSG_TAG, "pressure value: " + mPressure_Value + " height: " + mHeight);
            }

            if(Sensor.TYPE_LINEAR_ACCELERATION == sensorEvent.sensor.getType()) {
                float min = 0.01f;
                float mid = 0.04f;
                float max = 0.10f; //TODO, if above this then driving etc

                mAcceleration = sensorEvent.values[0];
                if (mAcceleration <= min && mAcceleration >= -min) {
                    mMovement = "SITTING";
                }
                else if ((mAcceleration >= min || mAcceleration <= -min) && (mAcceleration <= -mid)) {
                    mMovement = "WALKING";
                }
                else if ((mAcceleration >= mid || mAcceleration <= -mid) && (mAcceleration <= -max)) {
                    mMovement = "RUNNING";
                }
                else if (mAcceleration >= max && mAcceleration <= -max) {
                    mMovement = "DRIVING";
                }
                Log.d(MSG_TAG,mMovement + " Linear Speed: " + mAcceleration);



            }
            if(Sensor.TYPE_LIGHT == sensorEvent.sensor.getType()) {
                mLightValue = sensorEvent.values[0];
                if (mLightValue <= SensorManager.LIGHT_NO_MOON) {
                    mLight = "LIGHT_NO_MOON";
                }
                else if (mLightValue >= SensorManager.LIGHT_NO_MOON && mLightValue <= SensorManager.LIGHT_FULLMOON) {
                    mLight = "LIGHT_FULLMOON";
                }
                else if (mLightValue >= SensorManager.LIGHT_FULLMOON && mLightValue <= SensorManager.LIGHT_CLOUDY) {
                    mLight = "LIGHT_CLOUDY";
                }
                else if (mLightValue >= SensorManager.LIGHT_CLOUDY && mLightValue <= SensorManager.LIGHT_SUNRISE) {
                    mLight = "LIGHT_SUNRISE";
                }
                else if (mLightValue >= SensorManager.LIGHT_SUNRISE && mLightValue <= SensorManager.LIGHT_OVERCAST) {
                    mLight = "LIGHT_OVERCAST";
                }
                else if (mLightValue >= SensorManager.LIGHT_OVERCAST && mLightValue <= SensorManager.LIGHT_SHADE) {
                    mLight = "LIGHT_SHADE";
                }
                else if (mLightValue >= SensorManager.LIGHT_SHADE && mLightValue <= SensorManager.LIGHT_SUNLIGHT) {
                    mLight = "LIGHT_SUNLIGHT";
                }
                else if (mLightValue >= SensorManager.LIGHT_SUNLIGHT && mLightValue <= SensorManager.LIGHT_SUNLIGHT_MAX) {
                    mLight = "LIGHT_SUNLIGHT_MAX";
                }
                else if (mLightValue > SensorManager.LIGHT_SUNLIGHT_MAX) {
                    mLight = "DAMN_BRIGHT";
                }

                Log.d(MSG_TAG,mLight + " Light Value: " + mLightValue);
            }

            if(Sensor.TYPE_ACCELEROMETER == sensorEvent.sensor.getType()) {
                x = sensorEvent.values[0];
                y = sensorEvent.values[1];
                z = sensorEvent.values[2];

                float tol = 0.25f;


                //
                mAccelLast = mAccelCurrent;
                mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
                float delta = mAccelCurrent - mAccelLast;
                mAccel = mAccel * 0.9f + delta; // perform low-cut filter
                //


                if ((x <= tol && x >= -tol) && (y < -0.9f)) {
                    mOrientation = "VERTICAL";
                }
                else if((x >= 0.9f) && (y <= tol && y >= -tol)) {
                    mOrientation = "SIDE";
                }
                else if((x < -0.9f) && (y <= tol && y >= -tol))  {
                    mOrientation = "SIDE_2";
                }
                else if((x <= tol && x >= -tol) && (y >= 0.9f)) {
                    mOrientation = "UPSIDE_DOWN";
                }
                else if((x <= tol && x >= -tol) && (y <= tol && y >= -tol)) {
                    mOrientation = "FLAT";
                }

                Log.d(MSG_TAG,mOrientation + " Orientation: x: " + x + " y: " + y + " z: " + z);
            }

            new SensorEventTask().execute(sensorEvent);


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
                // do nothing
        }

        @Override
        public IBinder onBind(Intent arg0) {
         //   return null;
            return mBinder;
        }

        public class MyBinder extends Binder {
             SensorTrackerService getService() {
                return SensorTrackerService.this;
            }
        }
    private class SensorEventTask extends AsyncTask<SensorEvent, Void, Void> {
        @Override
        protected Void doInBackground(SensorEvent... events) {
            SensorEvent event = events[0];
            // log the value or send to server
            //Log.d(MSG_TAG, event.toString());
            return null;
        }
    }

    }







