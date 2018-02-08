package dao.duc.bpmdetector;

/**
 * Created by Duc Dao on 2/2/2018.
 */

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FirstTabActivity extends Fragment {
   private View rootView;
   private FloatingActionButton detectFAB;
   private TextView detectLabel;
   private boolean detectionOn;

   public FirstTabActivity() {
      this.detectionOn = false;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      this.rootView = inflater.inflate(R.layout.first_tab, container, false);
      initializeFAB();
      return this.rootView;
   }

   private void initializeFAB() {
      this.detectLabel = this.rootView.findViewById(R.id.detect_label);

      this.detectFAB = this.rootView.findViewById(R.id.fab);
      this.detectFAB.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            detect();
         }
      });
   }

   public void detect() {
      // Start detection
      if (!this.detectionOn) {
         setButtonAttributes("Detecting!", "#F44336");
         detectionOn = true;
      }
      // End detection
      else {
         setButtonAttributes("Ready to detect!", "#212121");
         detectionOn = false;
      }
   }

   public void setButtonAttributes(String text, String hexColor) {
      this.detectFAB.getBackground().
              setColorFilter(Color.parseColor(hexColor), PorterDuff.Mode.MULTIPLY);
      this.detectLabel.setText(text);
   }
}
