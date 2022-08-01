/*
 * Copyright 2017-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement;

import java.util.ArrayList;
import java.util.Map;
import bsg.util.CollectionUtils;
import isl.model.ISL;
import isl.model.ISLParams;
import isl.model.cell.Hepatocyte;

public class CountIntraHPerSlice extends CountLSS {
  private static final long serialVersionUID = -85340650697098486L;
  ISL model = null; // hide the super.model
  ArrayList<Hepatocyte> cells = null;
  
  public CountIntraHPerSlice(ISL tgt, String outputFile, ArrayList<Hepatocyte> cl, ArrayList<String> stl, boolean pvorcv) {
    super(tgt, outputFile, stl, pvorcv);
    mobileObjectTypes.add(ISLParams.GSHKEY);
    model = tgt; // shadow super.model to get access to non-abstract parts
    if (cl != null && !cl.isEmpty()) cells = cl;
    else throw new RuntimeException(getClass().getSimpleName()+" -- cell list can't be empty.");
  }
   
  protected String getColumnPrefix(Hepatocyte h) {
    return (fromPV ? h.getDPV(true) : h.getDCV(true))+":";
  }
  
  @Override
  public void step(sim.engine.SimState state) {
    Map<String,Number> outMap = new java.util.LinkedHashMap<>();
    for (Hepatocyte h : cells) {
      // get intracellular mobile objects, both bound and unbound
      Map<String,Number> intraHCounts = CollectionUtils.countObjectsByType(h.listMobileObject());
      for (Map.Entry<String,Number> me : intraHCounts.entrySet()) {
        String column = getColumnPrefix(h)+me.getKey();
        if (outMap.keySet().contains(column)) {
          outMap.replace(column, outMap.get(column).intValue()+me.getValue().intValue());
        } else {
          outMap.put(column,me.getValue().intValue());
        }
      }
      // get Repair count, which is an int
      String repkey = getColumnPrefix(h)+ISLParams.REPKEY;
      if (outMap.keySet().contains(repkey)) {
        outMap.replace(repkey, outMap.get(repkey).intValue()+h.getRepairCount());
      } else {
        outMap.put(repkey, h.getRepairCount());
      }
      // do it a last time for gsh depletion, which is a double
      String gshkey = getColumnPrefix(h)+ISLParams.GSHKEY;
      if (outMap.keySet().contains(gshkey)) {
        outMap.replace(gshkey, outMap.get(gshkey).intValue()+h.getGSHDepletion());
      } else {
        outMap.put(gshkey,h.getGSHDepletion());
      }
      // get ALT amount count, which is an int
      String altkey = getColumnPrefix(h)+ISLParams.ALTKEY;
      if (outMap.keySet().contains(altkey)) {
        outMap.replace(altkey, outMap.get(altkey).intValue()+h.getALTAmount());
      } else {
        outMap.put(altkey, h.getALTAmount());
      }
    }
    outMap.put("Time", model.getTime());
    writeOutputs(outMap);
  }

}
