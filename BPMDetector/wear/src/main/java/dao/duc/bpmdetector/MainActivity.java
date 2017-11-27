package dao.duc.bpmdetector;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import android.app.Activity;
import android.hardware.Sensor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements SensorEventListener/*,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/ {
   private SensorManager mSensorManager;
   private Sensor mLinearAcceleration;

   final static int XAXIS = 0;
   final static int YAXIS = 1;
   final static int ZAXIS = 2;
   final static int SECONDS_PER_MINUTE = 60;
   final static int LAST_N_SIZE = 15;

   private double xAxis;                      // Current linear acceleration in x axis
   private double yAxis;                      // Current linear acceleration in y axis
   private double zAxis;                      // Current linear acceleration in z axis

   private long startTime;                    // When detection was started
   private long currentTime;                  // Time when sensor detected change

   private double totalAcceleration;          // Total linear acceleration, calc-ed from axises

   private List<Long> lastNTimes;             // Used to determine when to plot time in plot
   private List<Double> lastNAccelerations;   // Used to determine when to plot acceleration in plot
   private Map<Long, Double> detectionPlot;   // Plot representing time vs acceleration

   private double averageInterval;            // Average interval between each beat
   private double bpm;                        // Beats per minute

   private boolean detectionOn;               // Flag determining if calculation should start

   // UI elements
   private Button startButton;
   private TextView xAxisLabel;
   private TextView yAxisLabel;
   private TextView zAxisLabel;
   private TextView totalAccelerationLabel;
   private TextView timesLabel;
   private TextView bpmLabel;

   //private GoogleApiClient mGoogleApiClient;

   protected void onCreate(Bundle savedInstanceState) {
      // Keep the Wear screen always on (for testing only!)
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      // Connect to accelerometer for linear acceleration
      this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
      this.mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

      // Link labels with TextViews from activity_main.xml
      xAxisLabel = findViewById(R.id.xAxisView);
      yAxisLabel = findViewById(R.id.yAxisView);
      zAxisLabel = findViewById(R.id.zAxisView);
      totalAccelerationLabel = findViewById(R.id.totalAccelerationView);
      timesLabel = findViewById(R.id.timesView);
      bpmLabel = findViewById(R.id.bpmView);

      initializeGlobals();

      updateUI();
      setStartButton();
      /*mGoogleApiClient = new GoogleApiClient.Builder(this)
              .addApi(Wearable.API)
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .build();*/
   }

   public void initializeGlobals() {
      xAxis = 0;
      yAxis = 0;
      zAxis = 0;
      totalAcceleration = 0;
      averageInterval = 0;
      bpm = 0;
      detectionOn = false;

      lastNTimes = new ArrayList<>(LAST_N_SIZE);
      lastNAccelerations = new ArrayList<>(LAST_N_SIZE);
      detectionPlot = new TreeMap<>();
   }

   public void setStartButton() {
      startButton = findViewById(R.id.detect_button);
      startButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            startDetection(v);
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
      mSensorManager.registerListener(this, mLinearAcceleration,
              SensorManager.SENSOR_DELAY_NORMAL);
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
      // App is detecting
      if (detectionOn) {
         // Set axis variables from sensor event
         xAxis = event.values[XAXIS];
         yAxis = event.values[YAXIS];
         zAxis = event.values[ZAXIS];

         currentTime = event.timestamp - startTime;

         // Movement gate so to prevent unnecessary calculations
         if (xAxis > .1 || yAxis > .1 || zAxis > .1) {
            totalAcceleration = calcTotalAcceleration();

            // Add another point for beat detection
            if (lastNTimes.size() < LAST_N_SIZE) {
               lastNTimes.add(currentTime);
               lastNAccelerations.add(totalAcceleration);

               if (lastNTimes.size() == LAST_N_SIZE) {
                  beatDetection(currentTime, totalAcceleration);
               }
            }
            // Check last LAST_N_SIZE points and see if it's a beat
            else {
               lastNTimes.remove(currentTime);
               lastNAccelerations.remove(totalAcceleration);

               lastNTimes.add(currentTime);
               lastNAccelerations.add(totalAcceleration);

               beatDetection(currentTime, totalAcceleration);
            }

            Log.d("TOTAL ACCELERATION", Double.toString(totalAcceleration));
         }

         updateUI();
         calcBPM();
      }
      else{
         startTime = event.timestamp;
      }
   }

   // TODO: Continue beat detection
   private void beatDetection(long time, double acceleration) {
      // Check for increasing total acceleration
      if (!isIncreasing()) {
         lastNTimes.clear();
         lastNAccelerations.clear();

         return;
      }

      detectionPlot.put(time, acceleration);
   }

   private boolean isIncreasing() {
      double previous, current;

      if (lastNAccelerations.size() > 0) {
         previous = lastNAccelerations.get(0);
      }
      else {
         return false;
      }

      for (int index = 1; index < lastNAccelerations.size(); index++) {
            current = lastNAccelerations.get(index);

            if (previous > current) {
               return true;
            }

            previous = current;
      }

      return true;
   }

   private void updateUI() {
      DecimalFormat formatter = new DecimalFormat("#0.00000");

      String xDisplay = "X-Axis: " + formatter.format(xAxis);
      String yDisplay = "Y-Axis: " + formatter.format(yAxis);
      String zDisplay = "Z-Axis: " + formatter.format(zAxis);
      String totalAccelerationDisplay = "Total A: " + formatter.format(totalAcceleration);
      String timeDisplay = Double.toString(
              TimeUnit.SECONDS.convert(startTime, TimeUnit.NANOSECONDS)) + " " +
              Double.toString(TimeUnit.MILLISECONDS.convert(currentTime, TimeUnit.NANOSECONDS));
      String bpmDisplay = "BPM: " + Double.toString(bpm);

      xAxisLabel.setText(xDisplay);
      yAxisLabel.setText(yDisplay);
      zAxisLabel.setText(zDisplay);
      totalAccelerationLabel.setText(totalAccelerationDisplay);
      timesLabel.setText(timeDisplay);
      bpmLabel.setText(bpmDisplay);
   }

   private void startDetection(View v) {
      if (!detectionOn) {
         detectionOn = true;
      }
      else {
         detectionOn = false;
      }
   }

   // Total acceleration is the square root of the sum of the squares of each axis
   private double calcTotalAcceleration() {
      return Math.sqrt((Math.pow(xAxis, 2) + Math.pow(yAxis, 2) + Math.pow(zAxis, 2)));
   }

   // Calculate the beats-per-minute, the number of intervals that can occur in 60 seconds
   private void calcBPM() {
      String bpmDisplay;

      // Compute bpm only beats are detected
      if (!detectionPlot.isEmpty()) {
         calcAverageInterval();

         bpm = SECONDS_PER_MINUTE / averageInterval;
      }

      bpmDisplay = "BPM: " + Double.toString(bpm);
      bpmLabel.setText(bpmDisplay);
   }

   // Helper method for calcBPM(), used to get the interval between beat beat
   private void calcAverageInterval() {
      long previousTime = -1;
      long interval;
      long totalInterval = 0;

      // Get all the times in plot
      for (Long currentTime : detectionPlot.keySet()) {
         // Read first time in plot
         if (previousTime == -1) {
            previousTime = currentTime;
         }
         // Subsequent times in plot
         else {
            interval = currentTime - previousTime;
            totalInterval += interval;
         }
      }

      // Number of intervals = detectionPlot.size() - 1
      averageInterval = totalInterval / (detectionPlot.size());
   }
}