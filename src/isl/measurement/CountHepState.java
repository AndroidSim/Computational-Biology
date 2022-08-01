/*
 * Copyright 2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import isl.model.cell.Hepatocyte;
import isl.model.ISL;

public class CountHepState {
  
  public CountHepState(ISL tgt, ArrayList<Hepatocyte> cl) {
    model = tgt;
    hStates = new HashMap<>(cl.size());
    cl.forEach((h) -> {
      hStates.put(h,1);
    });
  }
  
  ISL model = null; // hide the AbstractISL
  public Map<Hepatocyte,Integer> hStates = null;
  protected Map<Hepatocyte, Double> stressEvents = new HashMap<>();
  protected Map<Hepatocyte, Double> triggerEvents = new HashMap<>();
  protected Map<Hepatocyte, Double> necroticEvents = new HashMap<>();

  public void getHepStates() {
    for (Map.Entry<Hepatocyte, Integer> me : hStates.entrySet()) {
      int cellState = 0;
      Hepatocyte h = me.getKey();
      if (!h.necrotic) {
        cellState = 1;
        if (h.nh.isCellStressed()) cellState = 2;
        if (h.nh.necrosis_stop != null) cellState = 3;
      }
      int oldState = me.getValue();
      if (cellState != oldState) {
        System.out.println("H:"+h.id+" state change "+oldState+" → "+cellState+", |stressEvents| = "+stressEvents.size());
        if (cellState == 0) necroticEvents.put(h,model.getTime());
        // transitions back to normal are ignored
        if (cellState == 2) stressEvents.put(h,model.getTime());
        if (cellState == 3) triggerEvents.put(h,model.getTime());
        me.setValue(cellState);
      }
    }
  }
  
}
