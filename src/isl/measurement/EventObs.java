/*
 * Copyright 2016 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement;

import isl.model.cell.Hepatocyte;
import isl.model.ISL;
import isl.model.SS;

import java.util.Map;
import java.util.HashMap;

public abstract class EventObs extends Obs {

  private static final long serialVersionUID = 3299727007880329113L;
  
  public EventObs(ISL tgt, String outputFile) {
      super(tgt, outputFile, false);
   }
  
  @Override
  public void setHeaders() {
    // time string set by subclass
    headers.add("Cell_ID");
    headers.add("Dist_from_PV");
    headers.add("Dist_from_CV");
  }

  protected abstract Map<Hepatocyte, Double> getTgtData(isl.model.SS ss);
  
  @Override
  public void step(sim.engine.SimState state) {
    Map<String, Number> outMap = new HashMap<>();
    for (Object ln : ((ISL)model).hepStruct.allNodes) {
      if (ln instanceof SS) {
        SS ss = (SS)ln;
        double t = model.getTime();
        for (Map.Entry<Hepatocyte,Double> me : getTgtData(ss).entrySet()) {
          if (t-me.getValue() > 1.0/((ISL)model).stepsPerCycle) continue;
          Hepatocyte h = me.getKey();
          outMap.clear();
          outMap.put(headers.get(0), me.getValue());
          outMap.put("Cell_ID", h.id);
          outMap.put("Dist_from_PV", h.getDPV(true));
          outMap.put("Dist_from_CV", h.getDCV(true));
          writeOutputs(outMap);
        } // end loop over necroticHepatocytes
      } // end if SS
    } // end loop over liver nodes
    
  } // end step()
  
}
