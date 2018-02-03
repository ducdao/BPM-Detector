package dao.duc.bpmdetector;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

public class MainActivity extends AppCompatActivity {
   private Button detectButton;
   private TextView xAxisLabel;
   private TextView yAxisLabel;
   private TextView zAxisLabel;
   private TextView totalAccelerationLabel;

   private static final String START_ACTIVITY = "/start_activity";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      // Link labels with view from UI
      xAxisLabel = findViewById(R.id.xAxisLabel);
      yAxisLabel = findViewById(R.id.yAxisLabel);
      zAxisLabel = findViewById(R.id.zAxisLabel);
      totalAccelerationLabel = findViewById(R.id.totalAccelerationLabel);

      setDetectButton();
   }

   // Initialize detect button and set its click action
   private void setDetectButton() {
      detectButton = findViewById(R.id.detectButton);
      //detectButton.getBackground().setColorFilter(Color.parseColor("B#212121"), PorterDuff.Mode.MULTIPLY);
      detectButton.setText("DETECT");
      detectButton.setAllCaps(true);
      detectButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            calcBPM();
         }
      });
   }

   public void calcBPM() {
      Log.d("calcBPM", "DETECTING");
   }


   class DataLayerListenerService extends WearableListenerService {
      private final String TAG = DataLayerListenerService.class.getName();
      private GoogleApiClient mGoogleApiClient;

      private static final String WEARABLE_DATA_PATH = "/audio";

      @Override
      public void onCreate() {
         // I can see this fires properly on the Android mobile phone
         Log.d(TAG, "onCreate");
      }

      @Override
      public void onDataChanged(DataEventBuffer dataEvents) {
         // This never fires on the Android mobile phone, even though Wear says data was sent successfully
         Log.d(TAG, "on change");
      }
   }
}
