/*
 * Copyright 2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.measurement;

import isl.model.ISL;
import isl.model.Vas.VasType;

/**
 * Simply retrieves the contents of the rxnProdsCount IntGrid2D in all the
 * SSGrids and writes them to a file.
 */
public class GetRxnProdCount extends Obs {
  private static final long serialVersionUID = 4654959151208193615L;
  ISL model = null;
  int maxY = -Integer.MAX_VALUE;
  VasType dir = VasType.IN;
  
  public GetRxnProdCount(ISL tgt, String outputFile, VasType io) {
    super(tgt,outputFile,true);
    if (tgt != null) model = tgt;
    dir = io;
  }
  public void init() {
    for (Object o : model.hepStruct.allNodes) {
      if (o instanceof isl.model.SS) {
        isl.model.SS ss = (isl.model.SS)o;
        int y = ss.length-1 + (dir == VasType.IN ? ss.priorPathLength : ss.postPathLength);
        if (y > maxY) maxY = y;
      }
    }
    headers.clear(); headers.add("Time");
    for (int y=0; y<=maxY ; y++) headers.add(Integer.toString(y));
  }

    
  @Override
  public void step ( sim.engine.SimState state ) {
    int[] sum = new int[maxY+1];
    java.util.Arrays.fill(sum, 0);
    isl.model.HepStruct hs = model.hepStruct;
    for (Object o : hs.allNodes) {
      if (o instanceof isl.model.SS) {
        isl.model.SS ss = (isl.model.SS)o;
        sim.field.grid.IntGrid2D rpc = ss.hSpace.rxnProdCount;
        int preY = (dir == VasType.IN ? ss.priorPathLength : ss.postPathLength);
        int sumX = 0;
        for (int y=0 ; y<ss.length ; y++) {
          int sourceY = (dir == VasType.IN ? y : ss.length-1-y);
          for (int x=0 ; x<ss.circ ; x++) sumX += rpc.get(x,sourceY);
          sum[y+preY] += sumX;
        }
      }
    }
    StringBuilder record = new StringBuilder();
    record.append(model.getTime());
    for (int y=0 ; y<sum.length ; y++) {
      record.append(", ").append(sum[y]);
    }
    outputLog.monln(record.toString());
  }

}
