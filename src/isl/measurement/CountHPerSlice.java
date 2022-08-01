/*
 * Copyright 2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement;

import java.util.ArrayList;
import java.util.Map;
import isl.model.ISL;
import isl.model.cell.Hepatocyte;

public class CountHPerSlice extends Obs {
  private static final long serialVersionUID = -85340650697098486L;
  ISL model = null; // hide the super.model
  ArrayList<Hepatocyte> cells = null;
  private boolean fromPV = true;
  
  public CountHPerSlice(isl.model.ISL tgt, String outputFile, ArrayList<Hepatocyte> cl, boolean pvorcv) {
     super(tgt, outputFile, false);
     model = tgt;
     if (cl != null && !cl.isEmpty()) cells = cl;
     else throw new RuntimeException(getClass().getSimpleName()+" -- cell list can't be empty.");
     fromPV = pvorcv;
     init();
  }
  
  private void init() {
    ArrayList<Integer> dists = new ArrayList<>();
    cells.stream().map((h) -> (fromPV ? h.getDPV(true) : h.getDCV(true))).filter((d) -> (!dists.contains(d))).forEachOrdered((d) -> {
      dists.add(d);
    });
    java.util.Collections.sort(dists);
    
    headers.clear(); // clear those the super added
    dists.forEach((d) -> { headers.add(String.valueOf(d)); });
  }
  
  @Override
  public void step(sim.engine.SimState state) {
    // One time measure, nothing to step
    throw new RuntimeException("CountHPerSlice is a one-time measure.");
  }

  public void countH() {
      Map<String,Number> outMap = new java.util.LinkedHashMap<>();
      for (Hepatocyte h : cells) {
        String column = (fromPV ? String.valueOf(h.getDPV(true)) : String.valueOf(h.getDCV(true)));
        if (outMap.keySet().contains(column)) {
            outMap.replace(column, outMap.get(column).intValue() + 1);
        } else {
            outMap.put(column, 1);
        }
      }
      writeOutputs(outMap);
  }
  
}
