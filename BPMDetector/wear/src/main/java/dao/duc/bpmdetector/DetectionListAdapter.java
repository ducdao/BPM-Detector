package dao.duc.bpmdetector;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Duc Dao on 12/3/2017.
 */

public class DetectionListAdapter extends RecyclerView.Adapter {
   private final List<DetectionData> detections;

   public DetectionListAdapter(List<DetectionData> detections) {
      this.detections = detections;
   }

   @Override
   public DetectionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
      final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
      final View v = layoutInflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false); // Layout for every card

      return new DetectionViewHolder(v); // View holder
   }

   @Override
   public void onBindViewHolder(RecyclerView.ViewHolder detectionViewHolder, int index) {
      DetectionViewHolder d = (DetectionViewHolder) detectionViewHolder;
      d.detectionLabel.setText(Double.toString(detections.get(index).bpm));
   }

   @Override
   public int getItemCount() {
      return detections.size();
   }
}
