/*
 * Copyright 2018-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.measurement;

import java.util.HashMap;
import java.util.Map;
import isl.model.ISL;
import isl.model.cell.Hepatocyte;

public class CountStressedCells extends EventObs {

  private static final long serialVersionUID = 714321796181963L;
  
  CountHepState countHepState = null;
  public CountStressedCells(ISL tgt, String outputFile, CountHepState chs) {
    super(tgt, outputFile);
    countHepState = chs;
  }
  
  @Override
  public void setHeaders() {
    headers.add("Time");
    super.setHeaders();
  }
  
  @Override
  protected Map<Hepatocyte, Double> getTgtData(isl.model.SS ss) {
    return countHepState.stressEvents;
  }
  
  // loop over cells not SSes, promote this to EventObs when we refactor to use this for all cell state events
  @Override
  public void step(sim.engine.SimState state) {
    Map<String,Number> outMap = new HashMap<>();
    double t = model.getTime();
    for (Map.Entry<Hepatocyte,Double> me : getTgtData(null).entrySet()) {
      if (t-me.getValue() > 1.0/((ISL)model).stepsPerCycle) continue;
      Hepatocyte h = me.getKey();
      outMap.clear();
      outMap.put(headers.get(0), me.getValue());
      outMap.put("Cell_ID", h.id);
      outMap.put("Dist_from_PV", h.getDPV(true));
      outMap.put("Dist_from_CV", h.getDCV(true));
      writeOutputs(outMap);
    }
  }
}
