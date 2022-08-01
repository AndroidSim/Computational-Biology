/*
 * Copyright 2014-2019 - Regents of the University of California, San
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
import java.util.Map;
import java.util.LinkedHashMap;

public abstract class SampledCompartment extends Compartment implements Injectable {
  private static final long serialVersionUID = 6786391968832515684L;
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( SampledCompartment.class );

  ISL model = null;

  Compartment input = null;
  Injectable output = null;

  double sample = Double.NaN;
  
  /**
   * Don't yet know if we'll use this as a mirror for
   * Vas.distributed and how it's used in measures.
   */
  private Map<String,Number> distributed = new LinkedHashMap<>();
  @Override
  public Map<String,Number> getDistributed() {return distributed;}

  public SampledCompartment(ISL isl, ec.util.MersenneTwisterFast r, Compartment in, Injectable out, double sample) {
    super(r);
    model = isl;
    cType = CompartmentType.POOL;
    if (in != null) input = in;
    log.info(this+" Warning: input == null");
    if (out != null) output = out;
    else throw new RuntimeException("output can't be null");
    this.sample = sample;
  }
  
  @Override
  public void initPool(ArrayList<MobileObjectType> types) {
    super.initPool(types);
    output.initPool(types);
  }

  public abstract Map<String,Number> getAvailableMobileObject();

  @Override
  public void step(sim.engine.SimState state) {
    log.info("SampledCompartment.step() -- begin: pool = "+CollectionUtils.describe(pool));
    // get the old ones from the input
    Map<String,Number> incoming = getAvailableMobileObject();
    if (incoming != null && incoming.size() > 0) {
      //pool.addAll(incoming);
      log.info("SampledCompartment.step() -- Adding "+CollectionUtils.describe(incoming)+" to pool.");
      CollectionUtils.addIn(pool,incoming);
      incoming.clear();
    }
    log.info("SampledCompartment.step() -- after adding incoming, pool = "+CollectionUtils.describe(pool));

    Map<String,Number> mobileObject_to_distrib = calculateSample();
    distribute(mobileObject_to_distrib, CompartmentType.POOL);

    state.schedule.scheduleOnce(this, isl.model.ISL.action_order+1);
  }

  protected Map<String,Number> calculateSample() {
    //Specific use case for CZN.
    //Count LPS in pool
    int LPSthreshold = 100;
    int totalLPS = 0;
    totalLPS = (pool.containsKey("LPS") ? pool.get("LPS").intValue() : 0);
    
    Map<String,Number> to_push = new java.util.HashMap<>();

    // calculate the amount of MobileObject of each type to sample and inject into output
    for (Map.Entry<String,Number> me : pool.entrySet()) {
      MobileObjectType de = model.allMobileObject.stream().filter((o) -> (me.getKey().equals(o.tag))).findAny().get();
      double sample_ratio_factor = (de.properties.containsKey("sampleRatioFactor")
              ? ((Double)de.properties.get("sampleRatioFactor"))
              : 1.0);
      double vdchange = (totalLPS > LPSthreshold && de.properties.containsKey("VdChange")
              ? ((Double)de.properties.get("VdChange"))
              : 1.0);
      MutableInt amount_of_this_type = (MutableInt)pool.get(de.tag);
      double amount_to_move = amount_of_this_type.doubleValue() *
              sample * sample_ratio_factor / vdchange;
      to_push.put(de.tag, (long)amount_to_move);
    }
    return to_push;
  }
  
  protected void distribute(Map<String,Number> sm, CompartmentType c) {
    long sm_sum = CollectionUtils.sum_mi(sm);
    if (sm_sum > 0) {
      output.inject(sm);
      // decrement the pool
      for (Map.Entry<String,Number> me : sm.entrySet()) {
        MutableInt pool_amount = (MutableInt)pool.get(me.getKey());
        if (pool_amount == null) continue;
        if (pool_amount.val >= me.getValue().intValue()) pool_amount.sub(me.getValue().intValue());
        else throw new RuntimeException("∄ enough "+me.getKey()+" in the pool.");
      }
    }
  }
  
}
