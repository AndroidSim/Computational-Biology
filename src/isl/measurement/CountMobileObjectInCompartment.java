/*
 * Copyright 2014-2016 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement;
import isl.model.ISL;
import isl.model.Compartment;

public class CountMobileObjectInCompartment extends Obs {
  private static final long serialVersionUID = -2570821045860629020L;
  Compartment tgtCompartment = null;
  public CountMobileObjectInCompartment(ISL tgt, Compartment c, String outputFile) {
    super(tgt,outputFile, false);
    if (c != null) tgtCompartment = c;
    else throw new RuntimeException("compartment can't be null");
  }
  
  @Override
  public void step(sim.engine.SimState state) {
    java.util.Map<String, Number> contentsMap = new java.util.HashMap<>(tgtCompartment.getMobileObjectMap());
    double t = model.getTime();
    contentsMap.put("Time", t);
    writeOutputs(contentsMap);
  }

}
