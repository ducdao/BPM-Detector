package dao.duc.bpmdetector;

import java.util.Date;

/**
 * Created by Duc on 12/3/2017.
 */

public class DetectionData {
   public Double bpm;
   public Long duration;
   public Date dateOfDetection;

   public DetectionData(Double bpm, Long duration, Date dateOfDetection) {
      this.bpm = bpm;
      this.duration = duration;
      this.dateOfDetection = dateOfDetection;
   }
}
