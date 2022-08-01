/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import bsg.util.CollectionUtils;
import bsg.util.MutableInt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public strictfp class Dose implements sim.engine.Steppable
{
  private static final long serialVersionUID = -7237258166355250947L;
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( Dose.class );
  int id = -Integer.MAX_VALUE;
  DeliveryMethod delivery = null;
  public String deliveryType = null;
  double time = Double.NaN; // start time for the dose
  double infusionStopTime = Double.NaN; // if delivery is an infusion
  double infusion_conc_max = Double.NaN;
  double firstTime = Double.NEGATIVE_INFINITY;
  Injectable target = null;
  int total_created = 0;

  public Map<String,Double> solution = null; // bag of dose entries

  /**
   * Creates a new instance of BolusDose
   */
  public Dose ( int i, DeliveryMethod bl, Injectable tgt) {
    id = i;
    if ( bl != null ) delivery = bl;
    else throw new RuntimeException( "Dose: delivery can't be null." );

    if (tgt != null) target = tgt;
    else throw new RuntimeException( "Dose: delivery target can't be null.");
    // intialize the target's pool to zeros
    tgt.initPool(delivery.hepStruct.model.allMobileObject);
  }
  
  public Map<String,Double> getSolution() { return solution; }
   
  @Override
  @SuppressWarnings("unchecked") // for mobile objects cast from shuffle
  public void step ( sim.engine.SimState state ) {
    log.info("Dose."+id+".step() - begin.");
    Map<String, Number> mobileObject_map = null;
    double t = delivery.hepStruct.model.getTime();
    if (firstTime < 0.0) firstTime = t;
    double shiftedTime = t - firstTime;

    // bail if its not yet time
    if ( shiftedTime < 0 ) {
      throw new RuntimeException("Dose scheduled before its time.");
    }

    if (deliveryType.equals(DeliveryParams.BOLUS_TYPE)) mobileObject_map = bolusStep(shiftedTime);
    else
      if (t < infusionStopTime) {// normalize if direct to portal vein
        mobileObject_map = (target instanceof Vas ? constantConcStep(shiftedTime) : bodyInfusionStep(shiftedTime));
      }
    //log.info("Dose.step() - |mobileObjects| = "+(mobileObjects != null ? mobileObjects.size() : "null"));
    log.info("Dose."+id+".step() - mobileObject_map = " + mobileObject_map);
    long mobileObject_map_sum = (mobileObject_map == null ? 0 : CollectionUtils.sum_mi(mobileObject_map));
    if ( mobileObject_map != null && mobileObject_map_sum > 0 ) {
      // inject 
      target.inject( mobileObject_map );
      total_created += mobileObject_map_sum;
      // log with the delivery method
      delivery.registerDose(mobileObject_map);
    }
    /* reschedule if I'm a bolus and I have more to produce
     * OR if I'm an infusion and we haven't reached our stop time */
    if ((deliveryType.equals(DeliveryParams.BOLUS_TYPE) &&  ((total_created <= 0) || ((mobileObject_map != null) && (mobileObject_map_sum > 0)) )) 
            || t < infusionStopTime)
      state.schedule.scheduleOnce( this, ISL.action_order+1 );

  } // end public void step( sim.engine.SimState state ) {
  
  private Map<String,Number> bolusStep(double time) {
    Map<String,Number> ret_al = null;
    long result = calcDose(time);
    if (result > 0) {
      ret_al = new HashMap<>((int) result);
      int beNdx = 0;

      for (Map.Entry<String,Double> me : solution.entrySet()) {
        int num_of_type = (int) StrictMath.round(me.getValue() * result);
        ret_al.put(me.getKey(), num_of_type);
      }
    } // (result <= 0)
    return ret_al;
  } // end bolusStep()

  public Map<String,Number> constantConcStep(double time) {
    Map<String,Number> ret_al = new HashMap<>(); // constant conc. dosing assumes |distributed|>0
    bsg.util.CollectionUtils.zero_mi(target.getDistributed());

    java.util.Map<String, Number> manifest = target.getMobileObjectMap();
    double total_ref_dose = calcDose(time);
    // compare PV to the spec and construct the difference
    for (Map.Entry<String,Double> me : solution.entrySet()) {
      String tag = me.getKey();
      Double doseRatio = me.getValue();
      bsg.util.MutableInt pvCountInt = (MutableInt)manifest.get(tag);
      long pvCount_i = (pvCountInt == null ? 0 : pvCountInt.val);
      long deficit = (long) StrictMath.floor(total_ref_dose * doseRatio) - pvCount_i;
      // log with the input data structure
      if (target.getDistributed().get(tag) != null) {
        ((MutableInt)target.getDistributed().get(tag)).add(deficit);
      } else {
        target.getDistributed().put(tag, new MutableInt(deficit));
      }
      ret_al.put(tag,deficit);
    } // for (Object o : refDose.solution) {
    
    return ret_al;
  }

  public Map<String, Number> bodyInfusionStep(double time) {
    double total_ref_dose = Double.NaN;
    total_ref_dose = calcDose(time);
    int total_ref_dose_i = (int)StrictMath.round(total_ref_dose);
    Map<String,Number> ret_al = new HashMap<>();
    for (Map.Entry<String,Double> me : solution.entrySet()) {
      String tag = me.getKey();
      double doseRatio = me.getValue();
      long numOfThisType = (long)StrictMath.floor(total_ref_dose * doseRatio);
      ret_al.put(tag, numOfThisType);
    } // done creating all of them
    // if too few, due to floor(), then fill it randomly
    for (int sNdx=0 ; sNdx<(total_ref_dose_i-CollectionUtils.sum_mi(ret_al)) ; sNdx++) {
      //java.util.List<MobileObjectType> possibleTypes = solution.stream().filter((de) -> (de.doseRatio > Float.MIN_VALUE)).collect(Collectors.toList()) ;
      //MobileObjectType e = possibleTypes.get(delivery.hepStruct.hepStructRNG.nextInt(possibleTypes.size()));
      int t_i = delivery.hepStruct.hepStructRNG.nextInt(solution.size());
      String t_s = new ArrayList<>(solution.keySet()).get(t_i);
      ((MutableInt) ret_al.get(t_s)).add(1);
    }
    return ret_al;
  }
  
  public long calcDose(double time) {
    long result_i = (time <= 0.0 || deliveryType.equals(DeliveryParams.INFUSION_TYPE) ? delivery.referenceDose : 0);
    return result_i;
  }
  
}
