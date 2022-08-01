/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import java.util.Map;
import bsg.util.CollectionUtils;
import bsg.util.MutableInt;

public strictfp class Vas extends Harness {
  private static final long serialVersionUID = -8304998209995218420L;

  public static enum VasType {IN, OUT};
  public VasType vasType;
  Map<String, Number> passedMobileObject = null;
  public Map<String,Number> getPassed() { return passedMobileObject; }
  
  public long unknownClearanceEliminated = -Long.MAX_VALUE;
  
  int perfusateFlux = 100;
  
  /**
   * Creates a new instance of Vas
   * @param r - pseudo-random number generator
   * @param l - hepStruct
   */
  public Vas (ec.util.MersenneTwisterFast r, HepStruct l ) {
    super(r,l);
    unknownClearanceEliminated = 0;
  }

  @Override
  public void stepPhysics () {
    if ( vasType == VasType.IN ) {
      super.stepPhysics();
    } else if ( vasType == VasType.OUT ) {
      log.info(getClass().getSimpleName()+":"+id+".stepPhysics() begin -- pool = "+CollectionUtils.describe(pool));
      //outputs = new HashMap<>(pool);
      outputs = CollectionUtils.deepCopy(pool);
      // retire mobile objects
      if ( CollectionUtils.sum_mi(pool) > 0 ) {
        if (passedMobileObject == null) passedMobileObject = CollectionUtils.deepCopy(pool);
        else passedMobileObject = CollectionUtils.add(passedMobileObject, pool);
        CollectionUtils.zero_mi(pool);
      }
    }
  }


  @Override
  public boolean accept ( MobileObject m, CompartmentType c ) {
    boolean add = true;
    if ( m == null || ! (m instanceof MobileObject) ) {
      throw new RuntimeException( "Vas(" + id + ").place() -- invalid argument." );
    }

    /** 
     * catch the Bile
     */
    if (c == SS.BILE) add = false;
    
    /** unknownClearance mechanism 
     * if a random draw > the amount cleared by this mechanism, then accept 
     * the mobile object, otherwise "clear" it by accepting it but not add it
     * to the mobileObjects list.
     */
    if (add && m.hasProperty("unknownClearance")) {
      double ucval = (Double)m.properties.get("unknownClearance");
      double draw = compRNG.nextDouble();
      if (draw <= ucval) add = false;
    }
    
    if (add) {
      if (pool.containsKey(m.getTypeString())) ((MutableInt)pool.get(m.getTypeString())).add(1);
      else pool.put(m.getTypeString(), new MutableInt(1));
    } else {
      unknownClearanceEliminated++;
    }

    // Vas accept all MobileObject, though their fate depends on their type
    return true;
  }

  @Override
  public double getInletCapPerMobileObject () {
    return perfusateFlux;
  }

  @Override
  public String describe () {

    StringBuilder sb = new StringBuilder();
    sb.append("Vas( ").append(vasType).append(" ): ").append(id).append(" contains ")
            .append(" mobile objects (").append(CollectionUtils.describe(pool));
    sb.append(") and ");

    if (vasType.equals(VasType.IN)){
      sb.append(" distributed (");
      if (distributed.isEmpty()) sb.append("[EMPTY])");
      else {
        int total = 0;
        for (Map.Entry<String,Number> me : distributed.entrySet()) {
          total += me.getValue().longValue();
          sb.append(" ").append(me.getKey()).append(": ").append(me.getValue().longValue());
        }
        sb.append(") = ").append(total).append(" mobile object.");
        sb.append(" Eliminated: ").append(unknownClearanceEliminated).append(".");
      }
    } else {
      long outSum = CollectionUtils.sum_mi(outputs);
      if (outSum > 0) {
        sb.append(" outputs = ").append(outSum).append(" (");
        outputs.entrySet().stream().forEach((me) -> {
          sb.append(" ").append(me.getKey()).append(":").append(me.getValue().longValue());
        });
        sb.append(") ");
      }
      sb.append(" passed (");
      if (passedMobileObject == null)
        sb.append("[EMPTY])");
      else {
        sb.append(CollectionUtils.describe(passedMobileObject));
        sb.append(")");
      }
      sb.append(" eliminated ").append(unknownClearanceEliminated).append(" mobile object.");
    }
    sb.append("\n");
    return sb.toString();
  }
  
  @Override
  public double calculatedCV() {
    double retVal = Double.NaN;
    if (vasType == VasType.OUT) retVal = 0;
    if (vasType == VasType.IN) retVal = super.calculatedCV();
    return retVal;
  }
  @Override
  public double calculatedPV() {
    double retVal = Double.NaN;
    if (vasType == VasType.IN) retVal = 0;
    if (vasType == VasType.OUT) retVal = super.calculatedPV();
    return retVal;
  }
}
