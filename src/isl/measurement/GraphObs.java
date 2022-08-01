/*
 * Copyright 2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.measurement;

public class GraphObs extends Obs {
  public GraphObs (isl.model.AbstractISLModel tgt, String outputFile, boolean zip) {
    super(tgt, outputFile, zip);
  }
  
  @Override
  public void step(sim.engine.SimState state) {
    isl.model.ISL isl = (isl.model.ISL) model;
    sim.field.network.Edge[][][] mgam = isl.hepStruct.getMultigraphAdjacencyMatrix();
    // write the column headers in the 1st row, first write an emtpy cell
    for (int col=0 ; col<mgam[0].length ; col++) {
      outputLog.mon(","+col);
    }
    outputLog.monln(""); // new line
    
    for (int row=0 ; row<mgam.length ; row++) {
      outputLog.mon(""+row);
      for (int col=0 ; col<mgam[row].length ; col++) {
        outputLog.mon(","+mgam[row][col].length);
      }
      outputLog.monln(""); // new line
    }
  }
  
}
