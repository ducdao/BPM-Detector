package dao.duc.bpmdetector;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import android.app.Activity;
import android.hardware.Sensor;

import java.text.DecimalFormat;

public class MainActivity extends Activity implements SensorEventListener {
   private SensorManager mSensorManager;
   private Sensor mLinearAcceleration;

   final static int XAXIS = 0;
   final static int YAXIS = 1;
   final static int ZAXIS = 2;

   public double xAxis;
   public double yAxis;
   public double zAxis;

   private boolean detectionOn;

   Button startButton;
   TextView xAxisLabel;
   TextView yAxisLabel;
   TextView zAxisLabel;

   protected void onCreate(Bundle savedInstanceState) {
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

      // Set default value for axises
      xAxis = 0;
      yAxis = 0;
      zAxis = 0;

      updateText();

      setStartButton();
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

   protected void onResume() {
      super.onResume();
      mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
   }

   protected void onPause() {
      super.onPause();
      mSensorManager.unregisterListener(this);
   }

   public void onAccuracyChanged(Sensor sensor, int accuracy) {
   }

   public void onSensorChanged(SensorEvent event) {
      xAxis = event.values[XAXIS];
      yAxis = event.values[YAXIS];
      zAxis = event.values[ZAXIS];

      if (detectionOn)
         updateText();
   }

   private void updateText() {
      DecimalFormat formatter = new DecimalFormat("#0.00000");

      String xDisplay = "X-Axis: " + formatter.format(xAxis) + "\n";
      String yDisplay = "Y-Axis: " + formatter.format(yAxis) + "\n";
      String zDisplay = "Z-Axis: " + formatter.format(zAxis) + "\n";

      xAxisLabel.setText(xDisplay);
      yAxisLabel.setText(yDisplay);
      zAxisLabel.setText(zDisplay);
   }

   private void calcBPM() {
      if (!detectionOn) {
         detectionOn = true;
      }
      else {
         detectionOn = false;
      }
   }
}