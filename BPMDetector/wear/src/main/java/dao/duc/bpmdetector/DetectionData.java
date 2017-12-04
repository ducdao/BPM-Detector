package dao.duc.bpmdetector;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by Duc on 12/3/2017.
 */

public class DetectionData {
   public Double bpm;
   public Long duration;
   public String timestamp;

   public DetectionData(Double bpm, Long duration, String timestamp) {
      this.bpm = bpm;
      this.duration = duration;
      this.timestamp = timestamp;
   }
}
