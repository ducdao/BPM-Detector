package dao.duc.bpmdetector;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import android.app.Activity;
import android.hardware.Sensor;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements SensorEventListener/*,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/ {
   //private static final String TAG = MainActivity.class.getName();

   private SensorManager mSensorManager;
   private Sensor mLinearAcceleration;

   final static int XAXIS = 0;
   final static int YAXIS = 1;
   final static int ZAXIS = 2;

   // Current linear acceleration in x, y, and z axises
   private double xAxis;
   private double yAxis;
   private double zAxis;
   private double totalAcceleration;
   private long startTime;
   private long currentTime;

   // Flag determining if calculation should start
   private boolean detectionOn;

   private Button startButton;
   private TextView xAxisLabel;
   private TextView yAxisLabel;
   private TextView zAxisLabel;
   private TextView totalAccelerationLabel;
   private TextView timesLabel;

   //private GoogleApiClient mGoogleApiClient;

   protected void onCreate(Bundle savedInstanceState) {
      Log.d(TAG, "Creating MainActivity");

      // Keep the Wear screen always on (for testing only!)
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

      detectionOn = false;

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
      this.mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

      // Link labels with view from UI
      xAxisLabel = findViewById(R.id.xAxisView);
      yAxisLabel = findViewById(R.id.yAxisView);
      zAxisLabel = findViewById(R.id.zAxisView);
      totalAccelerationLabel = findViewById(R.id.totalAccelerationView);
      timesLabel = findViewById(R.id.timesView);

      // Set default value for axises
      xAxis = 0;
      yAxis = 0;
      zAxis = 0;
      totalAcceleration = 0;

      updateText();
      setStartButton();
      /*mGoogleApiClient = new GoogleApiClient.Builder(this)
              .addApi(Wearable.API)
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .build();*/
   }

   public void setStartButton() {
      startButton = findViewById(R.id.detect_button);
      startButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            calcBPM();
         }
      });

      startButton.setText("DETECT");
   }

   // Connect to the data layer when the Activity starts
   /*@Override
   protected void onStart() {
      super.onStart();
      mGoogleApiClient.connect();
   }*/

   protected void onResume() {
      /*if (null != mGoogleApiClient && !mGoogleApiClient.isConnected()) {
         mGoogleApiClient.connect();
      }*/

      super.onResume();
      mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
   }

   protected void onPause() {
      super.onPause();
      mSensorManager.unregisterListener(this);
   }
   /*
   @Override
   protected void onStop() {
      if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
         mGoogleApiClient.disconnect();
      }

      super.onStop();
   }

   @Override
   public void onConnected(@Nullable Bundle bundle) {
      Log.d(TAG, "Connected successfully");
   }

   @Override
   public void onConnectionSuspended(int i) {
      Log.d(TAG, "Connection suspended");
   }

   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
      Log.d(TAG, "Connection failed");
   }*/

   public void onAccuracyChanged(Sensor sensor, int accuracy) {
   }

   public void onSensorChanged(SensorEvent event) {
      xAxis = event.values[XAXIS];
      yAxis = event.values[YAXIS];
      zAxis = event.values[ZAXIS];

      Log.d("I'M A TAG", Double.toString(xAxis));

      // App is detecting
      if (detectionOn) {
         totalAcceleration = calcTotalAcceleration();
         currentTime = event.timestamp - startTime;
         updateText();
      }
      else{
         startTime = event.timestamp;
      }
   }

   private void updateText() {
      DecimalFormat formatter = new DecimalFormat("#0.00000");

      String xDisplay = "X-Axis: " + formatter.format(xAxis);
      String yDisplay = "Y-Axis: " + formatter.format(yAxis);
      String zDisplay = "Z-Axis: " + formatter.format(zAxis);
      String totalAccelDisplay = "Total A: " + formatter.format(totalAcceleration);
      String timeDisplay = Double.toString(TimeUnit.SECONDS.convert(startTime, TimeUnit.NANOSECONDS))
              + " " + Double.toString(TimeUnit.MILLISECONDS.convert(currentTime, TimeUnit.NANOSECONDS));

      xAxisLabel.setText(xDisplay);
      yAxisLabel.setText(yDisplay);
      zAxisLabel.setText(zDisplay);
      totalAccelerationLabel.setText(totalAccelDisplay);
      timesLabel.setText(timeDisplay);
   }

   private void calcBPM() {
      if (!detectionOn) {
         detectionOn = true;
         //mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
      }
      else {
         detectionOn = false;
         //mSensorManager.unregisterListener(this);
      }
   }

   private double calcTotalAcceleration() {
      return Math.sqrt((Math.pow(xAxis, 2) + Math.pow(yAxis, 2) + Math.pow(zAxis, 2)));
   }
}