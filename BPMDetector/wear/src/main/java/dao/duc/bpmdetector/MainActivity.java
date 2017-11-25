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
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements SensorEventListener/*,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/ {
   private SensorManager mSensorManager;
   private Sensor mLinearAcceleration;

   final static int XAXIS = 0;
   final static int YAXIS = 1;
   final static int ZAXIS = 2;
   final static int SECONDS_PER_MINUTE = 60;

   private double xAxis;                      // Current linear acceleration in x axis
   private double yAxis;                      // Current linear acceleration in y axis
   private double zAxis;                      // Current linear acceleration in z axis

   private long startTime;                    // When detection was started
   private long currentTime;                  // Time when sensor detected change

   private double totalAcceleration;          // Total linear acceleration, calc-ed from axises

   private Queue<Long> lastNTimes;            // Used to determine when to plot time in plot
   private Queue<Double> lastNAccelerations; // Used to determine when to plot acceleration in plot
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

   //private GoogleApiClient mGoogleApiClient;

   protected void onCreate(Bundle savedInstanceState) {
      // Keep the Wear screen always on (for testing only!)
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
      detectionOn = false;

      lastNTimes = new ArrayBlockingQueue<Long>(20);
      lastNAccelerations = new ArrayBlockingQueue<Double>(20);
      detectionPlot = new TreeMap<Long, Double>();
   }

   public void setStartButton() {
      startButton = findViewById(R.id.detect_button);
      startButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            startDetection();
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

            // Save time and acceleration if it's a beat
            if (isBeat(currentTime, totalAcceleration)) {
               detectionPlot.put(currentTime, totalAcceleration);
            }

            Log.d("TOTAL ACCELERATION", Double.toString(totalAcceleration));
         }

         updateUI();
      }
      else{
         startTime = event.timestamp;
      }
   }

   // TODO: Continue beat detection
   private boolean isBeat(long time, double acceleration) {
      lastNTimes.add(time);
      lastNAccelerations.add(acceleration);

      Iterator<Double> iter = lastNAccelerations.iterator();

      while (iter.hasNext()) {
         
      }

      if (false) {
         lastNTimes.clear();
         lastNAccelerations.clear();
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
              TimeUnit.SECONDS.convert(startTime, TimeUnit.NANOSECONDS))
              + " " +
              Double.toString(TimeUnit.MILLISECONDS.convert(currentTime, TimeUnit.NANOSECONDS));

      xAxisLabel.setText(xDisplay);
      yAxisLabel.setText(yDisplay);
      zAxisLabel.setText(zDisplay);
      totalAccelerationLabel.setText(totalAccelerationDisplay);
      timesLabel.setText(timeDisplay);
   }

   private void startDetection() {
      if (!detectionOn) {
         detectionOn = true;
      }
      else {
         detectionOn = false;
      }
   }

   private double calcTotalAcceleration() {
      return Math.sqrt((Math.pow(xAxis, 2) + Math.pow(yAxis, 2) + Math.pow(zAxis, 2)));
   }

   private void calcBPM() {
      calcAverageInterval();

      bpm = SECONDS_PER_MINUTE / averageInterval;
   }

   private void calcAverageInterval() {
      long previousTime = 0;
      long currentTime = 0;
      long interval;
      long totalInterval = 0;

      // Get all the times in plot
      for (Long time : detectionPlot.keySet()) {
         // Read first time in plot
         if (previousTime == 0) {
            previousTime = time;
         }
         // Subsequent times in plot
         else {
            interval = currentTime - previousTime;
            totalInterval += interval;
         }
      }

      // Number of intervals = detectionPlot.size() - 1
      averageInterval = totalInterval / detectionPlot.size() - 1;
   }
}