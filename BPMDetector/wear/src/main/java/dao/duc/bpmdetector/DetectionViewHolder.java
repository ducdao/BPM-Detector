package dao.duc.bpmdetector;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Duc Dao on 12/3/2017.
 */

public class DetectionViewHolder extends RecyclerView.ViewHolder {
   public TextView detectionLabel;

   DetectionViewHolder(View itemView) {
      super(itemView);
      detectionLabel = itemView.findViewById(R.id.detectionView);
   }
}
