package dao.duc.bpmdetector;

/**
 * Created by Duc Dao on 2/2/2018.
 */

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FirstTabActivity extends Fragment {
   private View rootView;
   private Button detectFAB;
   private boolean detectionOn;

   public FirstTabActivity() {
      this.detectionOn = false;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      this.rootView = inflater.inflate(R.layout.first_tab, container, false);
      //initializeButton();
      initializeFAB();
      return this.rootView;
   }

   private void initializeFAB() {
      FloatingActionButton fab = this.rootView.findViewById(R.id.fab);
      fab.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Snackbar.make(view, "Detection", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
         }
      });
   }

   public void detect() {
      // Start detection
      if (!this.detectionOn) {
         setButtonAttributes("STOP", "#F44336");
         detectionOn = true;
      }
      // End detection
      else {
         setButtonAttributes("DETECT", "#212121");
         detectionOn = false;
      }
   }

   public void setButtonAttributes(String text, String hexColor) {
      detectFAB.getBackground().
              setColorFilter(Color.parseColor(hexColor), PorterDuff.Mode.MULTIPLY);
      detectFAB.setText(text);
      detectFAB.setAllCaps(true);
   }
}
