/*
 * Copyright 2018-2-19 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.measurement;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import isl.model.ISL;
import isl.model.SS;
import isl.model.SSSpace;
import isl.model.cell.Cell;
import isl.model.MobileObject;
import bsg.util.CollectionUtils;
import isl.model.ISLParams;

/**
 * CountCellAdj: Count Cell-Adjacent MobileObject
 */
public class CountCellAdj extends CountLSS {

  private static final long serialVersionUID = 701090997070313L;
  
  public CountCellAdj(ISL tgt, String outputFile, ArrayList<String> stl, boolean pvorcv) {
    super(tgt,outputFile,stl,pvorcv);
    mobileObjectTypes.remove(ISLParams.REPKEY);
  }

  @Override
  public void step(sim.engine.SimState state) {
    LinkedHashMap<String,Number> outMap = new LinkedHashMap<>();
    for (Object ln : model.hepStruct.allNodes) {
      if (ln instanceof SS) {
        SS ss = (SS) ln;
        // only counting extraCellular MobileObject in ESpace, SoD, and HepSpace
        SSSpace grids[] = { ss.getESpace(), ss.getSoD(), ss.getHSpace() };
        for (int y=0 ; y<ss.length ; y++) {
          int dist = y+(fromPV ? ss.priorPathLength : ss.postPathLength);
          ArrayList<MobileObject> sAtY = new ArrayList<>();
          for (SSSpace g : grids) {
            if (g == null) continue; // for useSoD = false
            sAtY.addAll(g.getMobileObjectAtY(y));
            // subtract out the intraCellular MobileObject (ECs and Hs)
            ArrayList<Cell> cells = g.getCellsAtY(y);
            if (cells != null) 
              cells.forEach((c) -> { sAtY.removeAll(c.listMobileObject()); });
          }
          Map<String,Number> typesAtY = CollectionUtils.countObjectsByType(sAtY);  
          typesAtY.entrySet().forEach((me) -> {
            String header = dist+":"+me.getKey();
            if (!outMap.containsKey(header)) outMap.put(header,me.getValue());
            else outMap.replace(header, outMap.get(header).intValue()+me.getValue().intValue());
          });
        }
      }
    }
    outMap.put("Time", model.getTime());
    writeOutputs(outMap);
  }
}
