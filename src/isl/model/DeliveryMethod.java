/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import isl.model.cell.Cell;

public strictfp class DeliveryMethod {
  private static org.slf4j.Logger log = null;
  HepStruct hepStruct = null;  // where to send the dose
  int referenceDose = -1;
  boolean doseRepeats = false;
  int numDoses = -1;
  public java.util.ArrayList<Dose> doses = null;
  public MobileObjectType getMobileObjectType(MobileObject m) {
    MobileObjectType mt = null;
    try { mt = hepStruct.model.allMobileObject.stream().filter((t) -> (t.tag.equals(m.getTypeString()))).findFirst().get(); }
    catch(java.util.NoSuchElementException nse) { throw new RuntimeException("Couldn't find "+m.getTypeString()+".", nse); }
    return mt;
  }
  
  /* mobileObjectIn is a map with <MobileObject.tag, MutableInt> entries */
  java.util.LinkedHashMap<String,Number> mobileObjectIn = new java.util.LinkedHashMap<>();
  
  public DeliveryMethod ( HepStruct l ) {
    if ( l != null ) hepStruct = l;
    else throw new RuntimeException( "DeliveryMethod: HepStruct can't be null." );
  }
  public void setLogger(org.slf4j.Logger logger) {
     log = logger;
  }
  public void init ( double maxTime ) {
    ec.util.ParameterDatabase dpd = null, tpd = null;
    try {
      dpd = new ec.util.ParameterDatabase( this.getClass().getClassLoader().getResourceAsStream("cfg/delivery.properties"));
    } catch (java.io.IOException ioe) {
      System.err.println( ioe.getMessage() );
      System.exit( -1 );
    }
    
    StringBuilder buff = new StringBuilder("allMobileObject = {");
    java.util.Iterator<MobileObjectType> mti = hepStruct.model.allMobileObject.iterator();
    do { buff.append(mti.next().tag).append((mti.hasNext()?", ": "")); } while (mti.hasNext());
    log.info(buff.append("}").toString());
    
    DeliveryParams.loadDoseParams( this, dpd );

    estimateTotal( maxTime );
    
    // initialize the dosage log
    mobileObjectIn.put("Time",0.0);
    hepStruct.model.allMobileObject.forEach((be) -> { mobileObjectIn.put(be.tag,new bsg.util.MutableInt(0)); });
  }
  
  public void start( sim.engine.SimState state ) {
    for ( int dNdx=0 ; dNdx<numDoses ; dNdx++ ) {
      Dose d = (doseRepeats ? doses.get(0) : doses.get(dNdx));
      state.schedule.scheduleOnce(hepStruct.model.isl2MasonTime(d.time), ISL.action_order+1, d);
    }
  }

  public long maxMobileObject = -Integer.MAX_VALUE;
  public java.util.Map<String, bsg.util.MutableInt> maxPerMobileObject = new java.util.LinkedHashMap<>();
  public void estimateTotal ( double maxTime ) {
    maxMobileObject = 0;

    /** refMaxMobileObject -- dynamically constructed mobile object present a problem with 
     * derived measures, so we hack it to match a bindable and metabolizable 
     * bolus entry.  It is defined as the last bindable, rxnProb>0 bolus mobile object
     * over all doses.
     */
    bsg.util.MutableInt refMaxMobileObject = null;
    
    for ( int dNdx=0 ; dNdx<doses.size() ; dNdx++ ) {
      Dose d = doses.get(dNdx);
      long dose_total = 0;
      double stopTime = (d.deliveryType.equals(DeliveryParams.INFUSION_TYPE) ? d.infusionStopTime : maxTime);
      for ( double time = 0.0 ; time < stopTime ; time++ ) {
        dose_total += d.calcDose( time );
      }
      java.util.Map<String,Double> doseEntries = d.getSolution();
      // this calc goes here because each dose can have different mobile object ratios
      for ( java.util.Map.Entry<String,Double> me : doseEntries.entrySet()) {
         long tmp = StrictMath.round(((double)dose_total)*me.getValue());
         String type = me.getKey();
         if (maxPerMobileObject.containsKey(type))
            maxPerMobileObject.get(type).add(tmp);
         else maxPerMobileObject.put(type, new bsg.util.MutableInt(tmp));
      }
      
      // refMaxMobileObject is the first MobileObject type in the first EnzymeGroup that is also dosed
      for (String tag : d.getSolution().keySet()) {
        for (EnzymeGroup eg : Cell.MET_ENV.enzymeGroups.values()) {
          log.info("Checking for "+tag+" in "+eg.type);
          if (eg.acceptedMobileObjects.contains(tag) && eg.isProductive()) {
            log.info("Setting refMaxMobileObject:  maxPerMobileObject.get("+tag+") = "+maxPerMobileObject.get(tag));
            refMaxMobileObject = maxPerMobileObject.get(tag);
            break; // break out of the EG loop
          }
        }
        if (refMaxMobileObject != null) break; // break out of the tag loop
      }
      // if none of the dosed types have a rxnProb, then choose the 1st in the dose
      if (refMaxMobileObject == null) refMaxMobileObject = maxPerMobileObject.get(d.getSolution().keySet().stream().findFirst().get());
      
      // now set the rest of the maxPerMobileObject to the refMaxMobileObject
      for ( MobileObjectType mt : hepStruct.model.allMobileObject) {
        log.info("maxPerMobileObject.get("+mt.tag+") = "+ maxPerMobileObject.get(mt.tag));
        if (!maxPerMobileObject.containsKey(mt.tag))
          maxPerMobileObject.put(mt.tag, new bsg.util.MutableInt(refMaxMobileObject.val));
        else if (maxPerMobileObject.get(mt.tag).val <= 0)
          maxPerMobileObject.get(mt.tag).val = refMaxMobileObject.val;
      }
      log.info("Dose."+dNdx+" estimated total = "+dose_total);
      
      maxMobileObject += dose_total;
    }
    log.info("estimateTotal("+maxTime+") = "+maxMobileObject);
  }

  public void registerDose(java.util.Map<String,Number> sm) {
    bsg.util.CollectionUtils.addIn(mobileObjectIn,sm);
    mobileObjectIn.put("Time", hepStruct.model.getTime());
  }
  
  public java.util.Map<String,Number> getMobileObjectIn() { return mobileObjectIn; }
  public void clearMobileObjectIn() { 
    mobileObjectIn.remove("Time");
    bsg.util.CollectionUtils.zero_mi(mobileObjectIn); 
    mobileObjectIn.put("Time",Double.NaN);
  }
}
