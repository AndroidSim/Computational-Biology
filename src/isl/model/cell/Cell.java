/*
 * Copyright 2003-2020 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model.cell;

import isl.model.EnzymeGroup;
import isl.model.HepStruct;
import isl.model.MetabolicEnvironment;
import isl.model.SSSpace;
import isl.model.MobileObject;
import sim.util.Double2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Cell implements BindingInfo {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( Cell.class );
  private static int instanceCount = 0;
  public int id = -1;

  public static MetabolicEnvironment MET_ENV = null;
  
  /**
   * ∃ static CROSS_PROB and canCross() in each subclass to avoid using an IVar
   * and still allow each subclass to have its own CROSS_PROB. There must be
   * a better way to implement this pattern.
   */
  static sim.util.Double2D CROSS_PROB = new sim.util.Double2D(1.0, 1.0);
  public static void setMembraneCrossProb(sim.util.Double2D mcp) { log.info("Setting mcp = "+mcp); CROSS_PROB = mcp; }
  public isl.model.SSSpace mySpace = null;

  ec.util.MersenneTwisterFast cellRNG = null;
  protected ArrayList<MobileObject> mobileObjects = new ArrayList<>();
  
  public Map<String,Number> entries = new HashMap<>();
  public Map<String,Number> exits = new HashMap<>();
  public Map<String,Number> rejects = new HashMap<>();
  public Map<String,Number> traps = new HashMap<>();
  
  protected int repairCount = 0; // for counting Repair events
  public int getRepairCount() {return repairCount; }
  public void incRepairCount() {repairCount++;}
  
  private ArrayList<EnzymeGroup> groups = new ArrayList<>();

  ArrayList<Runnable> actionShuffler = new ArrayList<>();

  public int myX = -Integer.MAX_VALUE;
  public int myY = -Integer.MAX_VALUE;
  public void setLoc(int x, int y) {
    if (y < 0 || x < 0) throw new RuntimeException(this+" invalid position ("+x+", "+y+")");
    myX = x;
    myY = y;
    id = instanceCount++;
  }

  public Cell(isl.model.SSSpace p, ec.util.MersenneTwisterFast rng) {
    if (p != null) mySpace = p;
      cellRNG = rng;
      actionShuffler.add(new BindingHandler((BindingInfo) this, cellRNG, log));
  }
  
  protected int distPV = -Integer.MAX_VALUE;
  protected int distCV = -Integer.MAX_VALUE;
  public double uGradientvalue = 0.0;
  public void init() { 
      createEnzymeGroups(); 
      distPV = mySpace.ss.priorPathLength + myY;
      distCV = ((mySpace.ss.length-1) - myY) + mySpace.ss.postPathLength;
      // set the unified gradient value for the cell
      uGradientvalue = mySpace.ss.hepStruct.distances2UniGradient(distPV, distCV);
      log.info("UniGradient: init: Cell:"+id+" uGradientvalue = "+uGradientvalue+" dPV = "+distPV+" dCV = "+distCV+"\n");
  }
  
  public void createEnzymeGroups() {
    //Get list of EnzymeGroup names for this Cell typeString
    ArrayList<String> groupNames = MET_ENV.getCellTypeToEnzymeGroupNames().get(this.getClass().getSimpleName());
    if (groupNames == null) return;
    for(String name : groupNames) {
      EnzymeGroup eg = MET_ENV.enzymeGroups.get(name);
      if (eg == null) throw new RuntimeException("∄ group named "+name);
      int ic = cellRNG.nextInt(getBindmax()-getBindmin()) + getBindmin();
      if (eg.hasProperty("graded") && ((boolean)eg.getProperty("graded"))) {
        throw new RuntimeException(this.getClass().getSimpleName()+" has no basis for an "+eg.type+" gradient.");
      }
      EnzymeGroup egcopy = new EnzymeGroup(eg.type,
              ic,
              eg.getBindProb(),
              eg.getBindCycles(),
              eg.getAcceptedMobileObjects(),
              eg.getProperties());
      egcopy.downRegulatedBy = eg.downRegulatedBy;
      addEnzymeGroup(egcopy);
    }
  }
  public static enum Dir {IN, OUT};
  public boolean canCross(MobileObject m, Dir d) { return canCross(m,d,CROSS_PROB); }
  public boolean canCross(MobileObject m, Dir d, Double2D mcp) {
    boolean retVal = (m.hasProperty("membraneCrossing") && (boolean)m.getProperty("membraneCrossing"));
    
    // if not membraneCrossing, but can be transported out
    if (!retVal && d == Dir.OUT)
      retVal = m.hasProperty("transportOut") && ((boolean) m.getProperty("transportOut"));

    // if outward and bound, can't cross
    if (retVal && d == Dir.OUT && isBound(m)) retVal = false;
    
    // see if the MobileObject overrides the model-wide cellEnterExitProb
    if (retVal && m.hasProperty("cellEnterExitProb")) {
      Map<String, Double2D> ceepm = (Map<String,Double2D>)m.getProperty("cellEnterExitProb");
      String thisCellType = this.getClass().getSimpleName();
      if (ceepm.keySet().contains(thisCellType)) {
        Double2D newMCP = ceepm.get(thisCellType);
        //log.debug("Overriding cellEnterExitProb = "+mcp+" with "+newMCP);
        mcp = newMCP;
      }
      // else if this cell typeString isn't found, go ahead with model-wide mcp
    }
    // if pRNG draw fails, can't move
    if (retVal) {
      double draw = cellRNG.nextDouble();
      if (draw >= (d == Dir.IN ? mcp.x : mcp.y)) {  // can cross, but FAILED the draw
        retVal = false;
      }
    }
    // only count failures, success are counted in add() and remove()
    if (d == Dir.IN && !retVal) countMove(m,rejects);
    if (d == Dir.OUT && !retVal) countMove(m, traps);

    return retVal;
  }
  
  public void add(MobileObject m) {
    mobileObjects.add(m);
    countMove(m, entries);
  }
  
  public boolean remove(MobileObject m) {
    return remove(m,false);
  }
  public boolean remove(MobileObject m, boolean leak) {
    if (!mobileObjects.contains(m)) {
      throw new RuntimeException("Don't remove "+m.getTypeString()+"'s that aren't there!");
    }
    if (!leak) countMove(m, exits);
    return mobileObjects.remove(m);
  }

  /**
   * For MobileObject the Cell has created (by whatever means) and needs to present
   * in its data structures and the grid, etc.
   * @param m
   */
  public void present(MobileObject o) {
    mySpace.ss.mobileObjects.add(o);
    mySpace.putMobileObjectAt(o, myX, myY);
    mobileObjects.add(o);
  }
  
  /**
   * Remove the MobileObject from data structures and allow the system to reclaim the
   * memory.  In the analogy, this/these molecules no longer exist. This method
   * does NOT handle bound MobileObject.
   * @param s 
   */
  public void forget(MobileObject o) {
    mobileObjects.remove(o); //remove it from my list
    mySpace.ss.mobileObjects.remove(o); //remove it from parent's list
    // -- this will be a problem when/if hepatocytes can live in another grid
    mySpace.removeMobileObject(o); //remove it from the space
  }
  
  void countMove(MobileObject m, Map<String,Number> counter) {
    if (counter.containsKey(m.getTypeString())) counter.replace(m.getTypeString(),counter.get(m.getTypeString()).intValue() + 1);
    else counter.put(m.getTypeString(),1);
  }
  
  /**
   * Periodic "step" actions.  (Not named "step()" to avoid confusion with Steppables.
   */
  public void iterate() {
    bsg.util.CollectionUtils.shuffle(actionShuffler, cellRNG).stream().forEach((o) -> { ((Runnable)o).run(); });
  }

  protected void scheduleRelease(MobileObject m, long cycle) {
    isl.model.ISL model = mySpace.ss.hepStruct.model;
    //log.debug(tis+" scheduling "+b+" to release "+bound.get(b)+".typeString = "+bound.get(b).typeString+" at "+ cycle
    //        + " cycles = "+cycle);
    model.parent.schedule.scheduleOnce(cycle, 1, (sim.engine.SimState state) -> {
        //log.debug(this+" "+fb+" releases "+ bound.get(fb)+".typeString = "+bound.get(fb).typeString);
        getEnzymeGroups().forEach((eg) -> { eg.getBoundMobileObjects().remove(m); });
      });
  }
      
  //Returns all bound MobileObjects in this Cell.
  public ArrayList<MobileObject> getAllBoundMobileObjects() {
    ArrayList<MobileObject> retVal = new ArrayList<>();
    getEnzymeGroups().forEach((eg) -> {
      eg.getBoundMobileObjects().forEach((m) -> { retVal.add(m); });
    });
    return retVal;
  }
    
  //Returns whether MobileObject m is bound somewhere in this Cell.
  @Override
  public boolean isBound(MobileObject m) {
    //return getAllBoundMobileObjects().contains(s);
    return getEnzymeGroups().stream().anyMatch((eg) -> (eg.getBoundMobileObjects().contains(m)));
  }

  @Override
  public int getBindmin() { return MET_ENV.bindmin; }
  @Override
  public int getBindmax() { return MET_ENV.bindmax; }
  
  //Returns sum of capacity for all EnzymeGroups in this Cell.
  public int getTotalCapacity() {
    int retVal = 0;
    retVal = getEnzymeGroups().stream().map((eg) -> eg.getCapacity()).reduce(retVal, Integer::sum);
    return retVal;
  }
    
  //Implementations for BindingInfo
  
  /** 
   * Returns a flattened copy of List&lt;ArrayList&lt;EnzymeGroup&gt;&gt; groups
   * @return ArrayList&lt;EnzymeGroup&gt;
   */
  @Override
  public ArrayList<EnzymeGroup> getEnzymeGroups() {return groups;}
  
  @Override
  public List<MobileObject> listMobileObject() { return java.util.Collections.unmodifiableList(mobileObjects); }
  
  public List<MobileObject> listBoundMObject() { return java.util.Collections.unmodifiableList(getAllBoundMobileObjects()); }
  
  Object[] bsp_ivals = new Object[3];
  @Override
  public double getBindingProbability(EnzymeGroup eg) {
    double fraction = Double.NaN;
    int bound = eg.getBoundMobileObjects().size();
    long cycle = mySpace.ss.hepStruct.model.getCycle();
    //log.info("cycle = "+cycle+" Cellid = "+id+" Binding: bound = "+bound+" capacity = "+eg.capacity);
    switch (MET_ENV.getBindingMode()) {
      case "stepwise":
        fraction = (bound >= eg.capacity
                ? 0.0 
                : 1.0);
        break;
      case "linear":
        fraction = (bound >= eg.capacity
                ? 0.0
                : (eg.getCapacity() - bound) / eg.getInitialCapacity());
        break;
      default:
        fraction = 0.0;
        break;
    }
    double exp_factor = 1.0;
    if (eg.hasProperty("bindExpFactor") && eg.getProperty("bindExpFactor") != null) {
      exp_factor = ((Double) eg.getProperty("bindExpFactor"));
    }
    
    double pBind = Double.NaN;
    if (eg.hasProperty("bindProbGradient") && eg.getProperty("bindProbGradient") != null) {
      String bpg = (String)eg.getProperty("bindProbGradient");
      // check for start gradient value. 
      if (eg.hasProperty("bindProbStart")) {
          // setup rxnProbMap
          double bps = (Double)eg.getProperty("bindProbStart");
          double bpf = (Double)eg.getProperty("bindProbFinish");
          double bp = Double.NaN;
          double UGs = mySpace.ss.hepStruct.uniGradrange.x;
          double UGf = mySpace.ss.hepStruct.uniGradrange.y;
          if (bpg == null || bpg.contains("linear")) {
              bp = mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Linear,bps,bpf,1,1,uGradientvalue,UGs,UGf);
          } else if (bpg.contains("sigmoid")) {   
              if (eg.hasProperty("sigShiftSharp")) {
                  String sigSSstr = (String)eg.getProperty("sigShiftSharp");
                  sim.util.Double2D shiftsharp = isl.io.Parameters.parseTuple(sigSSstr);
                  double shift = shiftsharp.x;
                  double sharp = shiftsharp.y;
                  bp = mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Sigmoid,bps,bpf,sharp,shift,uGradientvalue,UGs,UGf);
              } else {
                  bp = mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Sigmoid,bps,bpf,1,1,uGradientvalue,UGs,UGf);
              }
          } else {
              throw new RuntimeException("Hepatocyte("+id+") - Unrecognized bindProbGradient value: "+bpg);
          }
          pBind = bp;
      } else {
          // if no start for binding gradient, then use un-propertied scalar value "bindProb"
          pBind = eg.getBindProb();
      }
    } else {
        // no binding gradient
        pBind = eg.getBindProb();
    }
    log.info("cycle = "+cycle+" Cellid = "+id+" Binding: pBind = "+eg.getBindProb());
    return java.lang.Math.pow(fraction, exp_factor) * eg.getBindProb();
    //log.info("cycle = "+cycle+" Cellid = "+id+" Binding: pBind = "+pBind);
    //return java.lang.Math.pow(fraction, exp_factor) * pBind;
  }
  
  @Override
  public void scheduleRelease(MobileObject m, EnzymeGroup eg) {
    scheduleRelease(m, mySpace.ss.hepStruct.model.getCycle()+eg.getBindCycles());
  }
    
  public void handleDegradation(SSSpace space) {
    ArrayList<MobileObject> toRemove = new ArrayList<>();
    //For each MobileObject in this Cell
    // If it's degradable and unbound, add to removal list
    mobileObjects.stream().filter((m)
                                  -> (m.hasProperty("pDegrade")
                                      && ((Double) m.getProperty("pDegrade")) > 0.0
                                      && !isBound(m))).forEach((m) -> {
                                          double draw = cellRNG.nextDouble();
                                          if (draw <= ((Double) m.getProperty("pDegrade"))) {
                                            toRemove.add(m);
                                          }
                                        });

    //Actually remove them
    toRemove.forEach((m) -> { forget(m); });
  }

  public void addEnzymeGroup(EnzymeGroup ng) {
    if (groups.size() <= 0) groups.add(ng);
    else {
      int ng_order = 0;
      if (ng.hasProperty("ordering")) ng_order = (int)ng.getProperty("ordering");
      for (int gNdx=groups.size()-1 ; gNdx>-1 ; gNdx--) {
        EnzymeGroup eg = groups.get(gNdx);
        int eg_order = 0;
        if (eg.hasProperty("ordering")) eg_order = (int)eg.getProperty("ordering");
        if (ng_order < eg_order)  groups.add(gNdx, ng);
        else groups.add(gNdx+1, ng);
        break;
      }
    }
  }
}
