/*
 * Copyright 2003-2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement;

import java.util.ArrayList;
import java.util.Map;
import isl.model.cell.Hepatocyte;
import isl.model.EnzymeGroup;

public class ISLEnzymeObs extends Obs {
  private static final long serialVersionUID = -85340650697098486L;
  isl.model.ISL model = null; // hide the super.model
  ArrayList<Hepatocyte> cells = null;
  private boolean fromPV = true;
  // only output when outMap != priorOutMap
//  Map<String,Number> priorOutMap = null;
  
  public ISLEnzymeObs(isl.model.ISL tgt, ArrayList<Hepatocyte> cl, String outputFile, boolean pvorcv) {
     super(tgt, outputFile, true);
     model = tgt;
     if (cl != null && !cl.isEmpty()) cells = cl;
     else throw new RuntimeException(getClass().getSimpleName()+" -- cell list can't be empty.");
     fromPV = pvorcv; // T→ΣδPV, F→ΣδCV
     
     // set the headers
     headers.clear(); // clear those the super added
     headers.add("Time");
     // all cells should be Hepatocytes
     for (Hepatocyte h : cells) {
       for (String eg : Hepatocyte.MET_ENV.enzymeGroups.keySet()) {
         String column = getColumnPrefix(h)+eg;
         if (!headers.contains(column))
           headers.add(getColumnPrefix(h)+eg);
       }
    }
  }

  private String getColumnPrefix(Hepatocyte h) {
//    return h.getDPV(true)+":"+h.getDCV(true)+":";
    return (fromPV ? h.getDPV(true) : h.getDCV(true))+":";
  }
  
  @Override
  public void step(sim.engine.SimState state) {
    Map<String,Number> outMap = new java.util.LinkedHashMap<>();
    for (Hepatocyte h : cells) {
      for(EnzymeGroup eg : h.getEnzymeGroups()) {
        String column = getColumnPrefix(h)+eg.type;
        if (outMap.keySet().contains(column)) {
          outMap.replace(column, outMap.get(column).intValue() + eg.capacity);
        } else {
          outMap.put(column, eg.capacity);
        }
      }
    }
//    if (!outMap.equals(priorOutMap)) {
      outMap.put("Time", model.getTime());
      writeOutputs(outMap);
//      priorOutMap = outMap;
//      priorOutMap.remove("Time");
//    }

  }

  public static void main(String[] args) {
    Map<String,Number> m1 = new java.util.LinkedHashMap<>(10);
    Map<String,Number> m2 = new java.util.LinkedHashMap<>(10);
    for (int i=0 ; i<10 ; i++) {
      m1.put(Integer.toString(i), i);
      m2.put(Integer.toString(i), i);
    }
    if (!m1.equals(m2)) System.out.println("case 1: m1 != m2");
    for (int i=0 ; i<10 ; i++) {
      m1.put(Integer.toString(i), i);
      m2.put(Integer.toString(i), i+1);
    }
    if (m1.equals(m2)) System.out.println("case 2: m1 == m2");
    
    System.out.println("Done.");
  }
}
