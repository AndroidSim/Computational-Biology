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
import java.util.function.Function;
import java.util.Map;
import isl.model.ISL;
import isl.model.ISLParams;
import isl.model.cell.Hepatocyte;

/**
 * Count the number of membrane interactions (MITs) for a Cell in
 * each d[PC]V slice.
 */
public class CountMITsPerSlice extends CountIntraHPerSlice {

  private static final long serialVersionUID = 701486891215094L;
  static public enum MIT {ENTRY, EXIT, REJECT, TRAP};
  Function<Hepatocyte, Map<String,Number>> f = null;
  public CountMITsPerSlice(ISL tgt, String outputFile, ArrayList<Hepatocyte> cl, ArrayList<String> stl, boolean pvorcv, MIT mit) {
    super(tgt,outputFile,cl,stl,pvorcv);
    mobileObjectTypes.remove(ISLParams.REPKEY);
    mobileObjectTypes.remove(ISLParams.GSHKEY); // super.init() added GSH
    switch (mit) {
      case ENTRY: 
        f = (Hepatocyte h) -> { return h.entries; }; break;
      case EXIT:
        f = (Hepatocyte h) -> { return h.exits; }; break;
      case REJECT:
        f = (Hepatocyte h) -> { return h.rejects; }; break;
      case TRAP:
        f = (Hepatocyte h) -> { return h.traps; }; break;
      default:
        throw new RuntimeException("Invalid type of membrane interaction: "+mit);
    }
  }

  @Override
  public void step(sim.engine.SimState state) {
    Map<String,Number> outMap = new java.util.LinkedHashMap<>();
    for (Hepatocyte h : cells) {
      Map<String,Number> counts = f.apply(h);
      for (Map.Entry<String,Number> me : counts.entrySet()) {
        String column = getColumnPrefix(h)+me.getKey();
        if (outMap.containsKey(column))
          outMap.replace(column, outMap.get(column).intValue()+me.getValue().intValue());
        else
          outMap.put(column,me.getValue().intValue());
      }
    }
    outMap.put("Time", model.getTime());
    writeOutputs(outMap);
  }
}
  