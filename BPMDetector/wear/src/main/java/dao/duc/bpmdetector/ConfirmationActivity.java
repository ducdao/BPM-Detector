package dao.duc.bpmdetector;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by Duc Dao on 12/3/2017.
 */

public class ConfirmationActivity extends Activity {
   private Button yesButton;
   private Button noButton;

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.confirmation_main);

      yesButton = findViewById(R.id.yesButton);
      noButton = findViewById(R.id.noButton);

      setButtonAttributes(yesButton, "YES", "#4CAF50");
      setButtonAttributes(noButton, "NO", "#F44336");

      yesButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Log.d("CONFIRMATION", "SAVING");
            finish();
         }
      });

      noButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Log.d("CONFIRMATION", "NOT SAVING");
            finish();
         }
      });
   }

   public void setButtonAttributes(Button button, String buttonText, String hexColor) {
      button.getBackground().setColorFilter(Color.parseColor(hexColor), PorterDuff.Mode.MULTIPLY);
      button.setText(buttonText);
      button.setAllCaps(true);
   }
}
