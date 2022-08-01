/*
 * Copyright 2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.measurement;

public class SSGeom extends Obs {
  public SSGeom(isl.model.AbstractISLModel tgt, String outputFile, boolean zip) {
    super(tgt, outputFile, zip);
  }
  
  @Override
  public void setHeaders() {
    // time string set by subclass
    headers.add("SS_Id");
    headers.add("Layer");
    headers.add("Circ");
    headers.add("Length");
  }
  
  @Override
  public void step(sim.engine.SimState state) {
    isl.model.ISL isl = (isl.model.ISL) model;
    for (int layer=0 ; layer<isl.hepStruct.layers.size() ; layer++) {
      java.util.ArrayList<isl.model.SS> layerNodes = isl.hepStruct.nodesInLayer.get(layer);
      for (isl.model.SS ss : layerNodes) {
        outputLog.monln(ss.id+", "+layer+", "+ss.circ+", "+ss.length);
      }
    }
  }
}
