package dao.duc.bpmdetector;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableRecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import android.app.Activity;
import android.hardware.Sensor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainActivity extends WearableActivity implements SensorEventListener {
   private SensorManager mSensorManager;
   private Sensor mLinearAcceleration;

   final static int XAXIS = 0;
   final static int YAXIS = 1;
   final static int ZAXIS = 2;
   final static double SECONDS_PER_MINUTE = 60.0;
   final static int LAST_N_SIZE = 20;

   private double xAxis; // Current linear acceleration in x axis
   private double yAxis; // Current linear acceleration in y axis
   private double zAxis; // Current linear acceleration in z axis

   private long startTime;   // When detection was started
   private long currentTime; // Time when sensor detected change

   private double totalAcceleration; // Total linear acceleration, calc-ed from axises

   private LinkedHashMap<Long, Double> graphMap; // Graph representing all values in time vs acceleration
   private LinkedHashMap<Long, Double> peeksMap; // Graph representing peek time vs acceleration

   private double averageInterval; // Average interval between each beat
   private double bpm;             // Beats per minute

   private boolean detectionOn; // Flag determining if calculation should start
   private boolean save;        // Whether to save the latest detection or not

   private List<DetectionData> detectionData;

   // UI elements
   private Button startButton;
   //private TextView xAxisLabel;
   //private TextView yAxisLabel;
   //private TextView zAxisLabel;
   private TextView totalAccelerationLabel;
   private TextView timesLabel;
   private TextView bpmLabel;

   private WearableRecyclerView pastDetections;


   protected void onCreate(Bundle savedInstanceState) {
      // Keep the Wear screen always on (for testing only!)
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      // Connect to accelerometer for linear acceleration
      this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
      this.mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

      // Link labels with TextViews from activity_main.xml
      totalAccelerationLabel = findViewById(R.id.totalAccelerationView);
      timesLabel = findViewById(R.id.timesView);
      bpmLabel = findViewById(R.id.bpmView);

      initializeGlobals();
      initializeStartButton();
      updateUI();
   }

   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      // Get user's choice on whether to save the latest detection information
      if (requestCode == 1 && resultCode == RESULT_OK) {
         save = data.getBooleanExtra("save", false);
         addDetectionData((long) 100.00);
         updateUI();
      }

      Log.d("SAVE", Boolean.toString(this.save));
   }

   public void setDetectionListUI() {
      //pastDetections = findViewById(R.id.recyclerLauncherView);
      LinearLayoutManager llm = new LinearLayoutManager(this);
      llm.setOrientation(LinearLayoutManager.VERTICAL);
      pastDetections.setLayoutManager(llm);
      pastDetections.setHasFixedSize(true);
      updateDetectionUI();
   }

   public void updateDetectionUI() {
      pastDetections.setAdapter(new DetectionListAdapter(detectionData));
   }

   public void addDetectionData(Long time) {
      detectionData.add(new DetectionData(this.bpm, time,
              new SimpleDateFormat("HHmmss, MMddyyyy").format(Calendar.getInstance().getTime())));
   }

   public void initializeGlobals() {
      xAxis = 0;
      yAxis = 0;
      zAxis = 0;
      totalAcceleration = 0;
      averageInterval = -1;
      bpm = 0;
      detectionOn = false;

      graphMap = new LinkedHashMap<>();
      peeksMap = new LinkedHashMap<>();

      detectionData = new ArrayList<>();
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
            graphMap.put(currentTime, totalAcceleration);

            // Start beat detection and find peeks
            if (graphMap.size() > LAST_N_SIZE) {
               //Log.d("TOTAL ACCELERATION", Double.toString(totalAcceleration) + " " + Double.toString(currentTime));

               BeatDetectionTask beatDetectionTask = new BeatDetectionTask(this.graphMap, this.peeksMap);
               beatDetectionTask.execute();

               peeksMap = new LinkedHashMap<>(beatDetectionTask.peeks);
               graphMap.clear();
            }
         }
         // Not moving
         else {
            totalAcceleration = 0;
         }

         calcBPM();
         updateUI();
      }
      else{
         startTime = event.timestamp;
      }
   }

   private void updateUI() {
      DecimalFormat formatter = new DecimalFormat("#0.000");

      String totalAccelerationDisplay = "Total A: " + formatter.format(totalAcceleration) + " m/s";
      String timeDisplay = "Time: " +
         Double.toString(TimeUnit.MILLISECONDS.
         convert(currentTime, TimeUnit.NANOSECONDS) / 1000.000) + "s";
      String bpmDisplay = "BPM: " + Double.toString(bpm);

      totalAccelerationLabel.setText(totalAccelerationDisplay);
      timesLabel.setText(timeDisplay);
      bpmLabel.setText(bpmDisplay);
   }

   private void startDetection(View v) {
      if (!detectionOn) {
         bpm = 0;
         updateUI();
         setButtonAttributes(startButton, "STOP", "#F44336");
         detectionOn = true;
      }
      else {
         bpm = 130;
         updateUI();
         startActivityForResult(new Intent(this, QuerySaveActivity.class), 1);
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
      long totalInterval = 0;

      // Get all the times in plot
      for (Long currentTime : peeksMap.keySet()) {
         // Read first time in plot
         if (!previousTimeSet) {
            previousTime = currentTime;
            previousTimeSet = true;
         }
         // Subsequent times in plot
         else {
            totalInterval += currentTime - previousTime;
            previousTime = currentTime;
         }
      }

      Log.d("DETECTION PLOT SIZE", Double.toString(peeksMap.size()));

      // Number of intervals = peeksMap.size() - 1
       averageInterval = totalInterval / (peeksMap.size() - 1);
   }

   // Perform beat detection in the background w/o thread manipulation
   private class BeatDetectionTask extends AsyncTask<LinkedHashMap<Long, Double>, Void, Void> {
      private LinkedHashMap<Long, Double> allPoints;
      public LinkedHashMap<Long, Double> peeks;

      public BeatDetectionTask(LinkedHashMap<Long, Double> graphMap, LinkedHashMap<Long, Double> peekMap) {
         this.allPoints = new LinkedHashMap<>(graphMap);
         this.peeks = new LinkedHashMap<>(peekMap);
      }

      @Override
      protected Void doInBackground(LinkedHashMap<Long, Double>... map) {
         peeks = zScoreAlgorithm(allPoints, peeks, LAST_N_SIZE, 3.5, .1);

         return null;
      }

      @Override
      protected void onProgressUpdate(Void... progress) {}

      @Override
      protected void onPreExecute() {}

      @Override
      protected void onPostExecute(Void result) {}
   }

   private LinkedHashMap<Long, Double> zScoreAlgorithm(LinkedHashMap<Long, Double> allPoints, LinkedHashMap<Long, Double> peeks, int lag, Double threshold, Double influence) {
      // Init stats instance
      //SummaryStatistics stats = new SummaryStatistics();

      List<Long> times = new ArrayList<>(allPoints.keySet());
      List<Double> accelerations = new ArrayList<>(allPoints.values());

      List<Double> filteredY = new ArrayList<>(accelerations);                                      // Filter out signals (peaks) from our original list (using influence arg)
      List<Double> avgFilter = new ArrayList<>(Collections.nCopies(accelerations.size(), 0.0d)); // Current average of rolling window
      List<Double> stdFilter = new ArrayList<>(Collections.nCopies(accelerations.size(), 0.0d)); // Current standard deviation of rolling window

      avgFilter.add(lag, calcMean(accelerations.subList(0, lag)));
      stdFilter.add(lag, calcStandardDeviation(accelerations.subList(0, lag)));
      //stats.clear();

      for (int index = lag + 1; index < accelerations.size(); index++) {
         //Log.d("CHECKING", Double.toString(accelerations.get(index)));

         // Distance between current value and average is enough standard deviations (threshold) away
         if (Math.abs(accelerations.get(index) - avgFilter.get(index - 1)) > threshold * stdFilter.get(index - 1)) {
            // Found a peek
            if (accelerations.get(index) > avgFilter.get(index - 1)) {
               Log.d("PEEK", Double.toString(accelerations.get(index)));
               this.peeksMap.put(times.get(index), accelerations.get(index));
            }

            // Filter this signal using influence
            filteredY.add(index, (influence * accelerations.get(index)) + ((1 - influence) * filteredY.get(index - 1)));
         } else {
            filteredY.set(index, accelerations.get(index));
         }

         // Update rolling average and deviation
         avgFilter.set(index, calcMean(filteredY.subList(index - lag, index)));
         stdFilter.set(index, calcStandardDeviation(filteredY.subList(index - lag, index)));
      }

      return peeks;
   }

   private double calcMean(List<Double> list) {
      double total = 0;

      // Calculate sum
      for (double value : list) {
         total += value;
      }

      return total / list.size();
   }

   // Standard deviation calculation: https://www.easycalculation.com/statistics/standard-deviation.php
   private double calcStandardDeviation(List<Double> list) {
      double mean = calcMean(list);
      double squaredDiff;
      double variance = 0;

      // Find variance
      for (int index = 0; index < list.size(); index++) {
         squaredDiff = list.set(index, Math.pow(list.get(index) - mean, 2));

         variance += squaredDiff;
      }

      variance = variance / list.size() - 1;

      // Square root of variance is standard deviation
      return Math.sqrt(variance);
   }
}