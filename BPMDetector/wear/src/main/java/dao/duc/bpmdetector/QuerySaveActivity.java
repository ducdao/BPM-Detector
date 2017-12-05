package dao.duc.bpmdetector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by Duc Dao on 12/3/2017.
 */

public class QuerySaveActivity extends Activity {
   private Button yesButton;
   private Button noButton;
   private Intent intent;

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.confirmation_main);

      yesButton = findViewById(R.id.yesButton);
      noButton = findViewById(R.id.noButton);

      initializeButton(yesButton, "YES", "#4CAF50", true, "Saved detection!");
      initializeButton(noButton, "NO", "#F44336", false, "Deleted detection.");
   }

   private void initializeButton(Button button, String text, String hexColor,
                                 final boolean save, final String message) {
      new MainActivity().setButtonAttributes(button, text, hexColor);

      button.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            buttonAction(save, message);
         }
      });
   }

   // Send user's option back to parent activity
   private void buttonAction(boolean save, String message) {
      int animationType = save ? ConfirmationActivity.SUCCESS_ANIMATION : ConfirmationActivity.FAILURE_ANIMATION;

      intent = new Intent();
      intent.putExtra("save", save);

      showConfirmationActivity(animationType, message);

      finish();
   }

   private void showConfirmationActivity(int animationType, String message) {
      Intent intent = new Intent(this, ConfirmationActivity.class);
      intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animationType);
      intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);
      startActivity(intent);
   }
}
