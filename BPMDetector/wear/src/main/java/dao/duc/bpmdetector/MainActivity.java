package dao.duc.bpmdetector;

import android.graphics.Color;
import android.graphics.PorterDuff;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements SensorEventListener {
   private SensorManager mSensorManager;
   private Sensor mLinearAcceleration;

   final static int XAXIS = 0;
   final static int YAXIS = 1;
   final static int ZAXIS = 2;
   final static double SECONDS_PER_MINUTE = 60.0;
   final static int LAST_N_SIZE = 15;

   private double xAxis; // Current linear acceleration in x axis
   private double yAxis; // Current linear acceleration in y axis
   private double zAxis; // Current linear acceleration in z axis

   private long startTime;   // When detection was started
   private long currentTime; // Time when sensor detected change

   private double totalAcceleration; // Total linear acceleration, calc-ed from axises

   private ArrayBlockingQueue<Long> lastTimes;           // Used to determine when to plot time in plot
   private ArrayBlockingQueue<Double> lastAccelerations; // Used to determine when to plot acceleration in plot
   private Map<Long, Double> peeksMap;              // Plot representing time vs acceleration

   private double totalInterval;
   private double averageInterval; // Average interval between each beat
   private double bpm;             // Beats per minute

   private boolean detectionOn; // Flag determining if calculation should start

   // UI elements
   private Button startButton;
   //private TextView xAxisLabel;
   //private TextView yAxisLabel;
   //private TextView zAxisLabel;
   private TextView totalAccelerationLabel;
   private TextView timesLabel;
   private TextView bpmLabel;

   protected void onCreate(Bundle savedInstanceState) {
      // Keep the Wear screen always on (for testing only!)
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      // Connect to accelerometer for linear acceleration
      this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
      this.mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

      // Link labels with TextViews from activity_main.xml
      //xAxisLabel = findViewById(R.id.xAxisView);
      //yAxisLabel = findViewById(R.id.yAxisView);
      //zAxisLabel = findViewById(R.id.zAxisView);
      totalAccelerationLabel = findViewById(R.id.totalAccelerationView);
      timesLabel = findViewById(R.id.timesView);
      bpmLabel = findViewById(R.id.bpmView);

      initializeGlobals();
      updateUI();
      initializeStartButton();
   }

   public void initializeGlobals() {
      xAxis = 0;
      yAxis = 0;
      zAxis = 0;
      totalAcceleration = 0;
      totalInterval = 0;
      averageInterval = 0;
      bpm = 0;
      detectionOn = false;

      lastTimes = new ArrayBlockingQueue(LAST_N_SIZE);
      lastAccelerations = new ArrayBlockingQueue(LAST_N_SIZE);
      peeksMap = new TreeMap<>();
   }

   public void initializeStartButton() {
      startButton = findViewById(R.id.detect_button);
      startButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            startDetection(v);
         }
      });

      setButtonAttributes(startButton, "DETECT", "#212121");
   }

   public void setButtonAttributes(Button button, String buttonText, String hexColor) {
      button.getBackground().setColorFilter(Color.parseColor(hexColor), PorterDuff.Mode.MULTIPLY);
      button.setText(buttonText);
      button.setAllCaps(true);
   }

   protected void onResume() {
      super.onResume();
      mSensorManager.registerListener(this, mLinearAcceleration,
              SensorManager.SENSOR_DELAY_NORMAL);
   }

   protected void onPause() {
      super.onPause();
      mSensorManager.unregisterListener(this);
   }

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

         // Movement gate to prevent unnecessary calculations
         if (xAxis > .1 || yAxis > .1 || zAxis > .1) {
            totalAcceleration = calcTotalAcceleration();

            // Add to time and acceleration to temporary queue for processing
            try {
               addToProcessingQueues(currentTime, totalAcceleration);
            }
            catch (InterruptedException e) {
               e.printStackTrace();
            }

            //Log.d("TOTAL ACCELERATION", Double.toString(totalAcceleration));
            calcBPM();
         }

         updateUI();
      }
      else{
         startTime = event.timestamp;
      }
   }

   private void addToProcessingQueues(long time, double acceleration) throws InterruptedException {
      // Add another point for beat detection
      try {
         lastTimes.add(time);
         lastAccelerations.add(acceleration);
      }
      // Remove time and its acceleration from queue to make room for new one
      catch (IllegalStateException e) {
         e.printStackTrace();
      }
      // Run beat detection only if the queue is full
      finally {
         if (lastTimes.remainingCapacity() == 0) {
            beatDetection(time, acceleration);
         }
      }
   }

   private void beatDetection(long time, double acceleration) {
      if (isPeek()) {
         peeksMap.put(time, acceleration);
      }

      // Remove queue's head to make room for another addition
      if (lastTimes.poll() == null || lastAccelerations.poll() == null) {
         Log.d("POLL", "Removing value from queue results in null");
      }
   }

   // TODO: Continue peek detection
   private boolean isPeek() {
      // Convert queue to ArrayList so we can iterate through it
      List<Double> accelerations = Arrays.asList(lastAccelerations.toArray(new Double[0]));
      double previousAcceleration, currentAcceleration, nextAcceleration;

      // Start at one so that the previous acceleration is the first one
      for (int index = 1; index < accelerations.size(); index++) {
         previousAcceleration = accelerations.get(index - 1);
         currentAcceleration = accelerations.get(index);

         Log.d("CURRENT ACCELERATION", Double.toString(currentAcceleration));

         // Set next acceleration only if it's not the last one
         if (index < accelerations.size() - 1) {
            nextAcceleration = accelerations.get(index + 1);
         }
         // Reached end of list, peek not found
         else {
            return false;
         }

         // Look for peek
         if (currentAcceleration > previousAcceleration && currentAcceleration > nextAcceleration) {
            Log.d("PEEK", "Found peek of " + Double.toString(currentAcceleration));
            return true;
         }
      }

      return false;
   }

   private void updateUI() {
      DecimalFormat formatter = new DecimalFormat("#0.00000");

      String totalAccelerationDisplay = "Total A: " + formatter.format(totalAcceleration);
      String timeDisplay = "Time: " +
         Double.toString(TimeUnit.MILLISECONDS.
         convert(currentTime, TimeUnit.NANOSECONDS) / 1000.000) + "s";
      String bpmDisplay = "BPM: " + Double.toString(bpm);

      totalAccelerationLabel.setText(totalAccelerationDisplay);
      timesLabel.setText(timeDisplay);
      bpmLabel.setText(bpmDisplay);
   }

   // Debugging method that prints the X, Y, and Z values
   private void printXYZUI() {
      //String xDisplay = "X-Axis: " + formatter.format(xAxis);
      //String yDisplay = "Y-Axis: " + formatter.format(yAxis);
      //String zDisplay = "Z-Axis: " + formatter.format(zAxis);

      //xAxisLabel.setText(xDisplay);
      //yAxisLabel.setText(yDisplay);
      //zAxisLabel.setText(zDisplay);
   }

   private void startDetection(View v) {
      if (!detectionOn) {
         setButtonAttributes(startButton, "STOP", "#F44336");
         detectionOn = true;
      }
      else {
         setButtonAttributes(startButton, "DETECT", "#212121");
         detectionOn = false;
      }
   }

   // Total acceleration is the square root of the sum of the squares of each axis
   private double calcTotalAcceleration() {
      return Math.sqrt((Math.pow(xAxis, 2) + Math.pow(yAxis, 2) + Math.pow(zAxis, 2)));
   }

   // Calculate the beats-per-minute, the number of intervals that can occur in 60 seconds
   private void calcBPM() {
      // Compute bpm only beats are detected
      if (!peeksMap.isEmpty()) {
         calcAverageInterval();

         bpm = SECONDS_PER_MINUTE / averageInterval;
         Log.d("AVERAGE INTERVAL", Double.toString(this.averageInterval));
         Log.d("BPM", Double.toString(bpm));
      }
   }

   // Helper method for calcBPM(), used to get the interval between beat beat
   private void calcAverageInterval() {
      boolean previousTimeSet = false;
      long previousTime = 0;
      long interval;

      // Get all the times in plot
      for (Long currentTime : peeksMap.keySet()) {
         // Read first time in plot
         if (!previousTimeSet) {
            previousTime = currentTime;
            previousTimeSet = true;
         }
         // Subsequent times in plot
         else {
            interval = currentTime - previousTime;
            previousTime = currentTime;

            totalInterval += interval;
         }
      }

      Log.d("DETECTION PLOT SIZE", Double.toString(peeksMap.size()));

      // Number of intervals = peeksMap.size() - 1
       averageInterval = totalInterval / (peeksMap.size() - 1);
   }
}