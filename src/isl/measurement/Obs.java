/*
 * Copyright 2003-2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package isl.measurement;

import isl.model.AbstractISLModel;

public class Obs implements sim.engine.Steppable
{
  private static final long serialVersionUID = 780407381085163588L;
  
  isl.model.AbstractISLModel model = null;
  isl.io.OutputLog outputLog = null;
  java.util.List<String> headers = new java.util.ArrayList<>();
    
  /**
   * Creates a new instance of Obs
   * @param tgt
   * @param outputFile
   * @param zip boolean true → compress the file
   */
  public Obs (isl.model.AbstractISLModel tgt, String outputFile, boolean zip) {
    if (tgt != null) model = tgt;
      else throw new RuntimeException( "Input model is null.\n");
      outputLog = new isl.io.OutputLog(outputFile, zip);
      setHeaders();
  }
  protected void setHeaders() {
    headers.add("Time");
    headers.addAll(model.getOutputNames());
  }
  protected static org.slf4j.Logger log = null;

  public void setLogger(org.slf4j.Logger logger) {
    log = logger;
  }

  void writeHeader() {
    java.util.ListIterator<String> hNdx = headers.listIterator();
    String h = null;
    while (hNdx.hasNext() && (h = hNdx.next()) != null) {
      outputLog.mon(h);
      if (hNdx.hasNext()) outputLog.mon(", ");
    }
    outputLog.monln("");
  }
  
  public boolean isTargetRunning() {
    return (model.getCycle() <= model.getCycleLimit() 
            || model.getStatus() == AbstractISLModel.ModelStatus.STARTED);
  }

  @Override
  public void step(sim.engine.SimState state) {
    java.util.Map<String, Number> outMap = model.getOutputs();
    writeOutputs(outMap);
  }
  
  /**
   * Use headers, thereby preserving order despite any LinkedHashMaps that may
   * implement the Map, to write outputs to the file.
   * @param outMap header → value
   */
  public void writeOutputs(java.util.Map<String, ? extends Number> outMap) {
    boolean commaCheck = false;

    java.util.ListIterator<String> hNdx = headers.listIterator();
    String key = null;
    while (hNdx.hasNext() && (key = hNdx.next()) != null) {
      Number val = outMap.get(key);
      outputLog.mon((val != null ? val.toString() : "0"));
      if (hNdx.hasNext()) {
        outputLog.mon(", ");
      }
      commaCheck = true;
    }
    if (commaCheck) {
      outputLog.monln("");
    }

  }
}
