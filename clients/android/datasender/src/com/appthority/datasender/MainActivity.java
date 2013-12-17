package com.appthority.datasender;


import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.SensorEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.Switch;
import android.util.Log;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.entity.*;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import android.util.Log;
import android.hardware.SensorEventListener;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import android.content.ComponentName;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity  {

    public static final String MSG_TAG = "OfficerUP -> MainActivity";
    private static final String URL = "http://54.215.9.95/api/store";

    private boolean isRunning = false;
    Context mContext;

    public SensorTrackerService mSensorTrackerService;
    public Timer mUpdateSensorViewTimer = new Timer();
    private View view;

    Switch   mySwitch;
    TextView lightView;
    TextView accelView;
    TextView altitudeView;
    TextView orientationView;
    EditText allSensorsText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = getApplicationContext();
        bindService(new Intent(this, SensorTrackerService.class), mConnection,Context.BIND_AUTO_CREATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);



        mySwitch  = (Switch) findViewById(R.id.startswitch);
        lightView = (TextView)findViewById(R.id.textView);
        accelView = (TextView)findViewById(R.id.textView2);
        altitudeView = (TextView)findViewById(R.id.textView3);
        orientationView = (TextView)findViewById(R.id.textView4);
        allSensorsText = (EditText) findViewById(R.id.allSensorsText);

        mySwitch.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    startService(new Intent(getApplicationContext(), SensorTrackerService.class));

                    // One-shot
                    mUpdateSensorViewTimer.schedule(new TimerTask()  {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    allSensorsText.setText(mSensorTrackerService.mAllSensors);
                                }
                            });
                        }
                    },1000);
                    // Re-occuring
                    mUpdateSensorViewTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateSensorViews();
                                }
                            });
                        }
                    },500,1000); //Before calling in ms, amount of time between execution in ms
                    Toast.makeText(mContext, "SENSORS ON", Toast.LENGTH_SHORT).show();



                } else {
                    //stopService(new Intent(getApplicationContext(), SensorTrackerService.class));
                    mUpdateSensorViewTimer.purge();
                    mSensorTrackerService.onDestroy();
                    Toast.makeText(mContext, "SENSORS OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /*
        isRunning = true;

        GPSTracker gps = new GPSTracker(this);
        while (isRunning == true) {
            try {
                if(gps.canGetLocation()){
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    PushDataToServer task = new PushDataToServer();
                    task.execute(latitude + "," + "n/a");

                } else{
                    gps.showSettingsAlert();
                }

                Thread.sleep(5000);

            } catch(Exception e) {
                Log.d(MSG_TAG,e.getMessage());
            }
        }

*/

    }


    public void updateSensorViews() {
        //stopService(new Intent(getApplicationContext(), SensorTrackerService.class));
        //mSensorTrackerService.onDestroy();
        lightView.setText(mSensorTrackerService.mLight + " light value: " + mSensorTrackerService.mLightValue);
        accelView.setText(mSensorTrackerService.mMovement + " linear speed: " + mSensorTrackerService.mAcceleration);
        altitudeView.setText(mSensorTrackerService.mPressure_Value + " pressure/" + mSensorTrackerService.mHeight + " height");
        orientationView.setText(mSensorTrackerService.mOrientation + " x: " + mSensorTrackerService.x + " y: " + mSensorTrackerService.y + " z: " + mSensorTrackerService.z);
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mSensorTrackerService = ((SensorTrackerService.MyBinder) binder).getService();
            Toast.makeText(MainActivity.this, "CONNECTED TO SENSORS",Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            mSensorTrackerService = null;
        }
    };
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Camera").setIcon(android.R.drawable.ic_menu_camera);
        menu.add(0, 2, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, 3, 0, "About").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        this.closeOptionsMenu();
        switch (item.getItemId()) {
            case 1:
                Intent   intent = new Intent(MainActivity.this, com.appthority.datasender.OpenCVCameraActivity.class);
                startActivity(intent);
                return true;
            case 2:
                return true;

            case 3:
                return true;
        }
        return false;

    }

    public class PushDataToServer extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
                       String loc = (String) params[0];
                       String orientation = (String) params[1];
                       try {
                            JSONObject jEntry = new JSONObject();
                            jEntry.put("guid", "106CD8DC-ADCD-4A37-BBF3-1A79D176F641");
                            jEntry.put("name", "Lara Croft");
                            jEntry.put("location",loc);
                            jEntry.put("orientation", orientation);
                            JSONObject jData = new JSONObject().put("data",jEntry);
                            JSONObject jsonObjRecv = RestClient.SendHttpPost(URL, jData);
                           // Toast.makeText(this.context, "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                        } catch(Exception e) {
                            Log.d(MSG_TAG,e.getMessage());
                        }


            return null;
        }


        protected void onPostExecute(String result) {
            //TextView txt = (TextView) findViewById(R.id.output);
        }


    }


}



