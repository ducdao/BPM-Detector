package dao.duc.bpmdetector;

import android.app.Activity;
import android.content.Intent;
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
   private Intent intent;
   private boolean save;

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.confirmation_main);

      yesButton = findViewById(R.id.yesButton);
      noButton = findViewById(R.id.noButton);

      initializeButton(yesButton, "YES", "#4CAF50", true);
      initializeButton(noButton, "NO", "#F44336", false);
   }

   private void initializeButton(Button button, String text, String hexColor, boolean save) {
      new MainActivity().setButtonAttributes(button, text, hexColor);

      this.save = save;

      button.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            buttonAction();
         }
      });
   }

   // Send user's option back to parent activity
   private void buttonAction() {
      intent = new Intent();
      intent.putExtra("save", save);
      finish();
   }
}
