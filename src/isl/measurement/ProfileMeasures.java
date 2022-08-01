/*
 * Copyright 2017-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package isl.measurement;

import java.util.Map;
import java.util.LinkedHashMap;
import bsg.util.CollectionUtils;
import isl.model.Vas.VasType;

public class ProfileMeasures extends Obs {
  private static final long serialVersionUID = -8885201082265831052L;

  /**
   * measureType Options:
   *   doseFraction ≡ Output_s/Dose_s
   *   outputFraction ≡ Output_s/Input_s
   *   extraction (ratio) ≡ (Input_s - Output_s)/Input_s
   * Notes:
   *   • Ideally, for Marker, DF → throughput/CC
   *   • Ideally, for Marker, OF → 1.0
   *     But OF < 1.0 ∀ cycles.  The input is clamped at bodySample*|Body|.
   *     If the input weren't clamped, then the Lobule would fill up and there would (eventually) only be Output empty grid points at the Input, making OF = 1.0.
   *     The only way around this is to change the model to a real concentration and account for empty space.
   *   • Ideally, for Marker, ER → 0.0
   *     But ER > 0.0 ∀ cycles, because the input is clamped at bodySample*|Body|.
   */
  public static enum MeasureType {doseFraction, outputFraction, extRatio};
  
  MeasureType measureType = null;
  public ProfileMeasures(isl.model.ISL tgt, String outputFile, MeasureType pm) {
    super(tgt, outputFile, false);
    measureType = pm;
  }
  boolean rawIO = false;
  VasType dir = null;
  public ProfileMeasures(isl.model.ISL tgt, String outputFile, VasType inorout) {
    super(tgt, outputFile, false);
    rawIO = true;
    dir = inorout;
  }

  @Override
  public void step(sim.engine.SimState state) {
    isl.model.ISL isl = (isl.model.ISL) model;
    Map<String, Number> inMap = isl.getInputs();
    log.info(getClass().getSimpleName()+" - inputs = "+CollectionUtils.describe(inMap));
    Map<String, Number> outMap = isl.getOutputs();
    log.info(getClass().getSimpleName()+" - outputs = "+CollectionUtils.describe(outMap));
    Map<String, Number> ratMap = null;
    
    if (rawIO) ratMap = (dir == VasType.IN ? inMap : outMap);
    else ratMap = derivedStep(isl,inMap,outMap);
    
    log.info(getClass().getSimpleName()+" -- "+measureType+" ratios = "+CollectionUtils.describe(ratMap));
    log.info("isl.getCycle() = "+isl.getCycle()+", isl.stepsPerCycle = "+isl.stepsPerCycle+", cycle%spc = "+isl.getCycle() % isl.stepsPerCycle);
    writeOutputs(ratMap);
     
    isl.delivery.clearMobileObjectIn();
  }

  private Map<String,Number> derivedStep(isl.model.ISL isl, Map<String,Number> in, Map<String,Number> out) {
    Map<String,Number> retVal = new LinkedHashMap<>(out.size());

    out.entrySet().forEach((me) -> {
      String ok = me.getKey();
      Number ov = me.getValue();
      double ov_d = ov.doubleValue();

      if (!ok.equals("Time")) {
        double rat = Double.NaN;
        if (measureType == MeasureType.doseFraction) {
          bsg.util.MutableInt max = isl.delivery.maxPerMobileObject.get(ok);
          rat = ov.doubleValue()/max.doubleValue();
          //rat *= isl.gridScale;
        } else {
          Number iv = in.get(ok);
          double iv_d = iv.doubleValue();
          if (measureType == MeasureType.extRatio)
            rat = (iv_d <= 0.0 ? 0.0 : (iv_d-ov_d)/iv_d);
          else if (measureType == MeasureType.outputFraction)
            rat = (iv_d <= 0.0 ? 0.0 : ov_d/iv_d);
        }
        retVal.put(ok,rat);
      } else retVal.put(ok,ov); // put Time entry
    });
    return retVal;
  }
}
