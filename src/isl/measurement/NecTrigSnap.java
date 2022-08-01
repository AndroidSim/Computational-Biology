/*
 * Copyright 2018-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.measurement;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import isl.io.OutputLog;
import isl.model.ISL;
import isl.model.SS;
import sim.field.grid.ObjectGrid2D;
import isl.model.cell.Hepatocyte;

public class NecTrigSnap implements sim.engine.Steppable {

  private static final long serialVersionUID = 714338012161237L;
  isl.model.ISL model = null;  // hide the AbstractISL for access to state
  String outFilePref = null;
  boolean zip = false;

  public NecTrigSnap(ISL tgt, String ofp, ArrayList<Hepatocyte> cl, boolean z) {
    model = tgt;
    outFilePref = ofp;
    hStates = new HashMap<>(cl.size());
    doIt = new HashMap<>(model.hepStruct.allNodes.size()-2);
    cl.forEach((h) -> {
      hStates.put(h,1);
      doIt.put(h.mySpace.ss,true);
    });
    zip = z;
  }

  
  Map<Hepatocyte,Integer> hStates = null;
  Map<SS,Boolean> doIt = null;
  @Override
  public void step(sim.engine.SimState state) {
    hStates.entrySet().forEach((me) -> {
      int cellState = 0;
      Hepatocyte h = me.getKey();
      if (!h.necrotic) {
        cellState = 1;
        if (h.nh.isCellStressed()) cellState = 2;
        if (h.nh.necrosis_stop != null) cellState = 3;
      }
      if (cellState != me.getValue()) {
        doIt.put(h.mySpace.ss, true);
        hStates.replace(h,cellState);
      }
    });

    doIt.entrySet().stream().filter((me) -> (me.getValue())).forEach((me) -> {
      snap(me.getKey());
      me.setValue(false);
    });
  }
  
  public void snap(SS ss) {
    StringBuilder headers = new StringBuilder("SS:").append(ss.id).append(", ");
    for (int x=0 ; x<ss.circ-1 ; x++) headers.append(x).append(", ");
    headers.append(ss.circ-1);
    
    // cycle-1 because the observer executes after the model increments it's cycle counter
    String cycleString = String.format("%0"+countDigits(model.getCycleLimit())+"d", model.getCycle()-1);
    OutputLog ol = new OutputLog(outFilePref+"-SS:"+ss.id+"-"+cycleString+".csv", zip);
    ol.monln(headers.toString());

    ObjectGrid2D hcg = ss.hSpace.getCellGrid();
    for (int y=0 ; y<ss.length ; y++) {
      StringBuilder row = new StringBuilder().append(y).append(", ");
      for (int x=0 ; x<ss.circ; x++) {
        Hepatocyte h = (Hepatocyte)hcg.get(x,y);
        int state = 0;
        if (h != null) state = hStates.get(h);
        row.append(state);
        if (x < ss.circ-1) row.append(", ");
      }
      ol.monln(row.toString());
    }
    ol.finish();
  }
  
  private int countDigits(long x) {
    int length = 0;
    long temp = 1;
    while (temp <= x) {
      length++;
      temp *= 10;
    }
    return length;
  }
}
