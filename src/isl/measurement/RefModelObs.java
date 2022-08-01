/*
 * Copyright 2003-2015 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package isl.measurement;

public class RefModelObs extends Obs {
  private static final long serialVersionUID = 5773087573593222972L;
  public RefModelObs(isl.model.AbstractISLModel tgt, String outputFile) {
    super(tgt, outputFile, false);
  }
  @Override
  public void step(sim.engine.SimState state) {
    outputLog.mon(model.getTime()+", ");
    outputLog.monln(model.getOutputFraction().toString());
  }
}
