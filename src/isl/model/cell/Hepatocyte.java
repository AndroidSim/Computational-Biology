/*
 * Copyright 2003-2020 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model.cell;

import static isl.io.Parameters.log;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import sim.util.Double2D;
import sim.util.distribution.Binomial;
import isl.model.EnzymeGroup;
import isl.model.HepStruct;
import isl.model.MetabolicParams;
import isl.model.SSSpace;
import isl.model.MobileObject;
import isl.model.MobileObjectType;
import isl.model.Solute;

public class Hepatocyte extends Cell implements CellInfo, EIInfo, ELInfo, ReactionInfo {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( Hepatocyte.class );
  static Double2D CROSS_PROB = new Double2D(1.0,1.0);
  public static void setMembraneCrossProb(Double2D mcp) { log.info("Setting mcp = "+mcp); CROSS_PROB = mcp; }
  @Override
  public boolean canCross(MobileObject m, Dir d) { return canCross(m,d,CROSS_PROB); }
  public ArrayList<Hepatocyte> neighbors = new java.util.ArrayList<>();
  public static boolean exos = Boolean.TRUE;
  public static int exoManhattanDist = Integer.MAX_VALUE;

  /**
   * rxnProbMap - map of metabolism probabilities for each Enzyme Group
   */
  Map<EnzymeGroup, Double> rxnProbMap = new LinkedHashMap<>();
  Map<EnzymeGroup, Double> rxnProbMapOrig = new LinkedHashMap<>();
  /**
   * productionMap - map from reaction product to it's ration of 1.0
   */
  Map<EnzymeGroup, Map<String,Double>> productionMap = new LinkedHashMap<>();
  public ArrayList< ArrayList<Integer> > elimQueues = null;    
  Binomial myBinomial = null;  

  public NecrosisHandler nh = null;
  public EXOHandler eh = null;
  
  public Hepatocyte(SSSpace p, ec.util.MersenneTwisterFast rng, int x, int y) {
    super(p, rng);
    setLoc(x,y); 
    myBinomial = new Binomial(5,0.5,rng);
    actionShuffler.add((Runnable) () -> { handleDegradation(mySpace); });
  }
  
  Map<String,Double> elInhibTypes = new HashMap<>();

  @Override
  public void present(MobileObject m, boolean bileAndAmp) {
    super.present(m);
    if (bileAndAmp) bileAndAmpHandling(m);
  }
  
  boolean adjCV = false; //true if connected to CV in HepStruct
  public static int adjDist = Integer.MAX_VALUE;
  
  // initialization currently only used if hepInitRead = true
  public void init(isl.io.HepInit hinit) {
    Map<String,Object> hr = hinit.popHepatocyteRecord(id, cellRNG);
    dist_pv = ((Number)hr.get("dPV")).intValue();
    dist_cv = ((Number)hr.get("dCV")).intValue();
    if(dist_cv <= adjDist) adjCV = true;
    uniGradientvalue = mySpace.ss.hepStruct.distances2UniGradient(dist_pv, dist_cv);
    super.init();
    finishInit();
  }
  @Override
  public void init() {
    dist_pv = mySpace.ss.priorPathLength + myY;
    dist_cv = ((mySpace.ss.length-1) - myY) + mySpace.ss.postPathLength;
    if(dist_cv <= adjDist) adjCV = true;
    // set the unified gradient value for the cell
    uniGradientvalue = mySpace.ss.hepStruct.distances2UniGradient(dist_pv, dist_cv);
    //log.info("UniGradient: init: H:"+id+" uniGradientvalue = "+uniGradientvalue+" dPV = "+dist_pv+" dCV = "+dist_cv+"\n");
    // create EnzymeGroups, which has a gradient specification, in class Cell
    super.init();
    // add handlers to action shuffler, and initializes ALT mechanism and 
    // GSH threshold, which has a gradient specification
    finishInit();
  }
  public double uniGradientvalue = 0.0;
  
  public void createEXO(String message) {
    java.util.ArrayList<MobileObjectType> mobileObjectTypes = mySpace.ss.hepStruct.model.allMobileObject;
    MobileObjectType exoType = null;

    try {
      exoType = mobileObjectTypes.stream().filter((t) -> (t.tag.equals("EXO"))).findFirst().get();
    } catch(java.util.NoSuchElementException nse) { throw new RuntimeException("Couldn't find EXO type.", nse); }

    //works best with hepDensity = 1.0
    ArrayList<Hepatocyte> neighbors = nh.getNeighbors(false, myX, myY, exoManhattanDist, true); //includeOrigin
    for(Hepatocyte h: neighbors) {
      isl.model.EXO e = new isl.model.EXO(exoType, 2, 3, message);
      e.setProperties(exoType.properties);
      h.presentXY(e,h.myX,h.myY);
    }

  }
  
  public void presentXY(MobileObject o, int x, int y) {
    mySpace.ss.mobileObjects.add(o);
    mySpace.putMobileObjectAt(o, x, y);
    mobileObjects.add(o);
  }
  
  float ENZYME_INIT_FACTOR = 3.0f;
  @Override
  public void createEnzymeGroups() {
    //Get list of EnzymeGroup names for this Cell typeString
    java.util.ArrayList<String> groupNames = MET_ENV.getCellTypeToEnzymeGroupNames().get(this.getClass().getSimpleName());
    for(String name : groupNames) {
      EnzymeGroup eg = MET_ENV.enzymeGroups.get(name);
      if (eg == null) throw new RuntimeException("Can't find "+name+".");
      int ic = -Integer.MAX_VALUE;
      if (eg.hasProperty("graded") && ((boolean)eg.getProperty("graded"))) {
        //float ratio = 1.0f + ENZYME_INIT_FACTOR * dist_pv/(dist_pv + dist_cv);
        //double oldGvalue = HepStruct.evalGradient(HepStruct.GradType.Linear,1,0,dist_pv,dist_cv);;
        double UGs = mySpace.ss.hepStruct.uniGradrange.x;
        double UGf = mySpace.ss.hepStruct.uniGradrange.y;
        double Gradvalue = mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Linear,1,0,1,1,uniGradientvalue,UGs,UGf);
        //log.info("UniGradient: creatEnzymeGroups: H:"+id+" oldGvalue = "+oldGvalue+" Gradvalue = "+Gradvalue+"\n");
        float ratio = 1f + ENZYME_INIT_FACTOR * (float)Gradvalue;
        int min = StrictMath.round(ratio * getBindmin());
        int max = StrictMath.round(ratio * getBindmax());
        ic = cellRNG.nextInt(max-min) + min;
        //log.debug("H:"+id+":EG."+eg.typeString+" -- dist_pv = "+dist_pv+", dist_cv = "+dist_cv+", ratio = "+ratio+", init capacity = "+ic);
      } else {
        ic = cellRNG.nextInt(getBindmax()-getBindmin()) + getBindmin();
        //log.debug("H:"+id+":EG."+eg.typeString+" -- init capacity = "+ic);
      }
      // deep copy of eg
      EnzymeGroup egd = new EnzymeGroup(eg.type,
              ic,
              eg.getBindProb(),
              eg.getBindCycles(),
              eg.getAcceptedMobileObjects(),
              eg.getProperties());
      // calculate gradients
      setGradients(egd, dist_pv, dist_cv);
      egd.downRegulatedBy = eg.downRegulatedBy;
      addEnzymeGroup(egd);
    }
  }
  
  // debug gradients
  static int gradDebugHs = 0;
  
  private void setGradients(EnzymeGroup eg, int dPV, int dCV) {
    // bail if this EG does not catalyze
    if (!eg.hasProperty("rxnProbStart")) return;
    // setup rxnProbMap
    double rps = (Double)eg.getProperty("rxnProbStart");
    double rpf = (Double)eg.getProperty("rxnProbFinish");
    double rp = Double.NaN;
    String rpg = (String)eg.getProperty("rxnProbGradient");
    if (rpg == null || rpg.contains("linear")) {
      //rp = bsg.util.LinearGradient.eval(rps,rpf,0.0,(double)(dPV+dCV),(double)dPV);
      //double orp = mySpace.ss.hepStruct.evalGradient(HepStruct.GradType.Linear, rps, rpf, dPV, dCV);
      double UGs = mySpace.ss.hepStruct.uniGradrange.x;
      double UGf = mySpace.ss.hepStruct.uniGradrange.y;
      rp = mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Linear,rps,rpf,1,1,uniGradientvalue,UGs,UGf);
      //log.info("UniGradient: reaction linear: H:"+id+" orp = "+orp+" rp = "+rp+"\n");
    } else if (rpg.contains("sigmoid")) {
      //rp = bsg.util.SigmoidGradient.eval(rps,rpf,0.0,(double)(dPV+dCV),(double)dPV);
      //double orp = mySpace.ss.hepStruct.evalGradient(HepStruct.GradType.Sigmoid, rps, rpf, dPV, dCV);
      double UGs = mySpace.ss.hepStruct.uniGradrange.x;
      double UGf = mySpace.ss.hepStruct.uniGradrange.y;
      if (eg.hasProperty("sigShiftSharp")) {
          String sigSSstr = (String)eg.getProperty("sigShiftSharp");
          sim.util.Double2D shiftsharp = isl.io.Parameters.parseTuple(sigSSstr);
          double shift = shiftsharp.x;
          double sharp = shiftsharp.y;
          //log.info("reaction sigmoid: shift = "+shift+", sharp = "+sharp+"\n");
          rp = mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Sigmoid,rps,rpf,sharp,shift,uniGradientvalue,UGs,UGf);
      } else {
          rp = mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Sigmoid,rps,rpf,1,1,uniGradientvalue,UGs,UGf);
      }
      //log.info("UniGradient: reaction sigmoid: H:"+id+" rp = "+rp+"\n");
    } else 
      throw new RuntimeException("Hepatocyte("+id+") - Unrecognized rxnProbGradient value: "+rpg);
    rxnProbMap.put(eg, rp);

    // log rxnPro[bd] gradients for 5 Hs
    boolean debugGrad = (gradDebugHs < 5*MET_ENV.enzymeGroups.size()); gradDebugHs++;
    StringBuilder gradDebug = new StringBuilder();
    if (debugGrad) gradDebug.append("H:"+id+".rxnProb(eg="+eg.type+",start="+rps+",finish="+rpf+",dPV="+dPV+",dCV="+dCV+") ⇒ rp="+rp+"\n");
      
    // setup productionMap
    if (rp > 0.0) {
      Map<String, Double2D> rxnprodmap = (Map<String,Double2D>)eg.getProperty("rxnProducts");
      Map<String, Double> rxnProdMap = new LinkedHashMap<>();
      rxnprodmap.entrySet().forEach((me) -> {
        Double2D d2d = me.getValue();
        double prmin = d2d.x, prmax = d2d.y;
        //double prodRate = bsg.util.LinearGradient.eval(prmin, prmax, 0.0, (double) dPV+dCV, (double)dPV);
        //double oprodRate = mySpace.ss.hepStruct.evalGradient(HepStruct.GradType.Linear, prmin, prmax, dPV, dCV);
        double UGs = mySpace.ss.hepStruct.uniGradrange.x;
        double UGf = mySpace.ss.hepStruct.uniGradrange.y;
        double prodRate = mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Linear,prmin,prmax,1,1,uniGradientvalue,UGs,UGf);
        //log.info("UniGradient: production: H:"+id+" oprodRate = "+oprodRate+" prodRate = "+prodRate+"\n");
        rxnProdMap.put(me.getKey(), prodRate);
        
        // gradient debug
        if (debugGrad) gradDebug.append(me.getKey()+":<"+prmin+","+prmax+"> ⇒ "+prodRate+"\n");
        
      });

      productionMap.put(eg, rxnProdMap);

      // gradient debug
      if (debugGrad) log.debug(gradDebug.toString());
      
    }
  }
  
  // this initialization does the following:
  // adds EXO, EI, EL, Reaction, and Necrosis handler to actionShuffler
  // adds down regulation to actionShuffler
  // initializes ALT release mechanism
  // initializes GSH threshold, which has a gradient
  public void finishInit() {
    // list of EnzymeGroups is needed for E[IL]Handler constructors and downRegulation runnable
    if (mySpace.ss.ei_rate > 0.0) {
      actionShuffler.add(new EIHandler((CellInfo) this, (BindingInfo) this, (EIInfo) this, log));
    }
    if (mySpace.ss.el_rate > 0.0) {
      actionShuffler.add(new ELHandler((CellInfo) this, (BindingInfo) this, (ELInfo) this, log));
    }
    if (MET_ENV.drInterval > 0) {
      //The entire mechanism is repeated for each EnzymeGroup for which downRegulated = true
      elimQueues = new ArrayList< ArrayList<Integer> >();
      getEnzymeGroups().stream().filter((eg) -> (eg.downRegulatedBy != null && !eg.downRegulatedBy.isEmpty())).forEach((eg) -> {
        ArrayList<Integer> elimQueue = new ArrayList<>();
        elimQueues.add(elimQueue);
        actionShuffler.add((Runnable) () -> { handleDownRegulation(eg, elimQueue);  });
      });
    }
    getMobileObjectTypes().forEach((de) -> {
      Double inhfact = Double.NaN;
      if (de.properties.containsKey("elInhibitFactor") && (inhfact = ((Double)de.properties.get("elInhibitFactor"))) > 0.0) {
        elInhibTypes.put(de.tag,inhfact);
      }
    });

    // set gsh threshold based on an inverse map to PV-CV position
    //gsh_threshold = bsg.util.LinearGradient.eval(GSH_DEPLETION_RANGE.x, GSH_DEPLETION_RANGE.y,0.0,(double)(dist_pv+dist_cv),(double)dist_pv);
    //double ogsh_threshold = mySpace.ss.hepStruct.evalGradient(HepStruct.GradType.Linear, GSH_DEPLETION_RANGE.x, GSH_DEPLETION_RANGE.y, getDPV(false), getDCV(false));
    double UGs = mySpace.ss.hepStruct.uniGradrange.x;
    double UGf = mySpace.ss.hepStruct.uniGradrange.y;
    gsh_threshold = mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Linear,GSH_DEPLETION_RANGE.x,GSH_DEPLETION_RANGE.y,1,1,uniGradientvalue,UGs,UGf);
    //log.info("UniGradient: gsh threshold: H:"+id+" ogsh_threshold = "+ogsh_threshold+" gsh_threshold = "+gsh_threshold+"\n");
    if (!rxnProbMap.isEmpty()) {
      actionShuffler.add(new ReactionHandler((BindingInfo) this, (ReactionInfo) this, cellRNG, log));
    }
    
    // check if ALT is a MobileObject typeString, and if true use the ALT mechanism 
    // also, add a transporter that does the actual release mechanism
    String alt = "ALT";
    for (MobileObjectType mt : getMobileObjectTypes()) {
        if (alt.equals(mt.tag)) {
            // use ALT mechanism
            useALT = true;
            altMT = mt;
            // create ALT Transporter
            altTransporter = new Transporter(this);
            // set MobileObject typeString for ALT Transporter to the ALT MobileObject typeString
            altTransporter.transportedMobileObjectType = altMT;
            // calculate initial membrane damage, should be zero
            init_membrane_damage = altTransporter.getmembDamageCount();
            // set the amount of ALT to the initial ALT amount
            altAmount = initaltAmount;
            // determine if internal ALT is released from necrotic cells
            if (altMT.hasProperty("necroticRelease")) {
                if ((boolean)altMT.getProperty("necroticRelease")== false) {
                    necroticRelease = false;
                }
            }
            // add ALT release mechanism to the actionShuffler
            actionShuffler.add((Runnable) () -> { altRelease(); });
            break;
        }
    }
    
    // suicide action
    nh = new NecrosisHandler(this);
    actionShuffler.add(nh);
    
    if(exos) {
      eh = new EXOHandler(this);
      actionShuffler.add(eh);
    }
  } // end of finishInit()
    
  public void handleDownRegulation(EnzymeGroup eg, ArrayList<Integer> elimQueue) {
    ArrayList<String> drb = eg.downRegulatedBy; // to avoid 4 extra f() calls
    if (drb == null || drb.isEmpty()) return;
    
    Map<String,Number> counts = bsg.util.CollectionUtils.countObjectsByType(listMobileObject());
    
    /**
     * drCapΔ - amount to change EG.Capacity when we execute a DR event or when we replenish instantly 
     */
    int drCapΔ = eg.hasProperty(MetabolicParams.DR_CAP_DELTA)
            ? ((Integer) eg.getProperty(MetabolicParams.DR_CAP_DELTA))
            : MET_ENV.drCapΔ;
    /**
     * drPrΔ - amount to change the prob. of a reaction for that EG
     */
    double drPrΔ = eg.hasProperty(MetabolicParams.DR_PR_DELTA)
            ? ((Double) eg.getProperty(MetabolicParams.DR_PR_DELTA))
            : MET_ENV.drPrΔ;
    // If (1) we lack capacity && (2) the queue is empty && (3) there are no DRing MobileObjects, there is a chance to replenish
    ////Capacity can only increase by 1 each SCyc

    //If (1) && (2)
    if(eg.getCapacity() < eg.getInitialCapacity() && elimQueue != null && elimQueue.isEmpty()) {
      //Check for (3): whether there is at least one DR causing MobileObject present
      ///This is done within the current if-statement so no Binomial draw is required if (1) or (2) aren't met.
      boolean drMobileObjectPresent = false;
      double drReplenish = eg.hasProperty(MetabolicParams.DR_REPLENISH)
              ? ((Double)eg.getProperty(MetabolicParams.DR_REPLENISH)) 
              : MET_ENV.drReplenish;
      for (String type : drb) {
        Number n = counts.get(type);
        int count = (n != null ? n.intValue() : 0);
        if (count > 0) {
          drMobileObjectPresent = true;
          int repEvents = myBinomial.nextInt(count,drReplenish);
          if (repEvents > 0) {
            //log.debug("Replenishing H:"+id+"."+eg.typeString+".Capacity by "+drCapΔ*repEvents);
            eg.changeCapacity(drCapΔ*repEvents); // each event adds DR_CAP_DELTA
            if (rxnProbMapOrig.containsKey(eg)) {
              double ori = rxnProbMapOrig.get(eg);
              double cur = rxnProbMap.get(eg);
              if (cur+drPrΔ <= ori) {
                //log.debug("Raising H:"+id+"."+eg.typeString+" Pr ("+rxnProbMap.get(eg)+") by "+drPrΔ);
                rxnProbMap.replace(eg,rxnProbMap.get(eg)+drPrΔ);
              } else {
                rxnProbMap.replace(eg,ori);
                rxnProbMapOrig.remove(eg);
              }
            }
          }
        }
      }
      /*
       * Bail if there are no DRing mobile objects and either we're at capacity or 
       * elimQueue is empty.  Note that this could store events in the elimQueue
       * that will be executed if we ever move back below capacity.
       */
      if (!drMobileObjectPresent) return; 
    }
    
    //Pop the queue
    if(elimQueue != null && elimQueue.size() > 0) {
      int numElimEvents = (int) elimQueue.remove(0);
      //log.debug("Popped "+numElimEvents+" to decrement capacity by "+-drCapΔ*numElimEvents+", new elimQueue = "+elimQueue);
      if (numElimEvents > 0) {
        eg.changeCapacity(-drCapΔ*numElimEvents);
        double cur = rxnProbMap.get(eg);
        if (cur > 0.0) {
          if (!rxnProbMapOrig.containsKey(eg)) rxnProbMapOrig.put(eg,cur);
          log.info("Reducing H:"+id+"."+eg.type+" Pr ("+cur+") by "+drPrΔ);
          double set = cur-drPrΔ;
          if (set < 0.0) set = 0.0;
          rxnProbMap.replace(eg,set);
        }
      }
    }

    //Bail early if there is too much elimination scheduled.
    double elim_interval = (eg.hasProperty(MetabolicParams.DR_INTERVAL) 
            ? ((Double)eg.getProperty(MetabolicParams.DR_INTERVAL))
            : MET_ENV.drInterval);
    if(elimQueue != null && elimQueue.size() > 10*elim_interval) {
      return;
    }
      
    /*
     * For each DRing MobileObject object, there's a chance to schedule decrease in capacity.
     * If so, it's added to the queue.
     * Capacity can only be scheduled to be reduced by 1 each SCyc.
     */
    double drRemove = eg.hasProperty(MetabolicParams.DR_REMOVE) 
            ? ((Double)eg.getProperty(MetabolicParams.DR_REMOVE)) 
            : MET_ENV.drRemove;
    for (String type : drb) {
      if (counts.containsKey(type)) {
        Number n = counts.get(type);
        int elimEvents = myBinomial.nextInt(n.intValue(), drRemove);
        if(elimQueue == null) elimQueue = new ArrayList<Integer>();

        //log.debug("Adding "+elimEvents+" decrements of "+drCapΔ+" every "+elim_interval+" to elimQueue = "+elimQueue);
        scheduleElimEvents(elimQueue, elimEvents, elim_interval);
        //log.debug(elimEvents+" decrements of "+drCapΔ+" added every "+elim_interval+" to elimQueue = "+elimQueue);
        
      } // end if (counts.containsKey(typeString)) {
    } // end for (String typeString : drb) {
    
  } // end handleDownRegulation()

  private void scheduleElimEvents(ArrayList<Integer> q, int total, double interval) {
    int qndx = 0;
    int added = 0;
    double accumulator = 0.0;
    double eventsPerCycle = 1/interval;
    while (added < total) {
      double left = total-added;
      accumulator += (left < eventsPerCycle ? left : eventsPerCycle);
      if (accumulator < 1.0) foldEventsToQueue(q, qndx, 0); 
      else { 
        int acc_i = (int)accumulator;
        foldEventsToQueue(q, qndx, acc_i); 
        accumulator -= acc_i;
        added += acc_i;
      }
      qndx++;
    }
  }
  private void foldEventsToQueue(ArrayList<Integer> q, int ndx, int v) {
    if (ndx < q.size()) q.set(ndx,q.get(ndx)+v);
    else q.add(v);
  }
  
  /*
   * Implementations for CellInfo
   */
  protected int dist_pv = -Integer.MAX_VALUE;
  protected int dist_cv = -Integer.MAX_VALUE;
  @Override
  public int getDPV(boolean actual) { return (actual ? myY+mySpace.ss.priorPathLength : dist_pv); }
  @Override
  public int getDCV(boolean actual) { return (actual ? (mySpace.ss.length-1-myY)+mySpace.ss.postPathLength : dist_cv); }
  @Override
  public double getResources() {
    double max_grid = mySpace.ss.hepStruct.max_grid;
    double retVal = bsg.util.ExpGradient.eval(1, 0, 4.5316, 0, max_grid, getDPV(false));
    return retVal;
  }
  @Override
  public Map<String,Double> getELInhibTypes() { return elInhibTypes; }
    
  /*
   * Implementations for EIInfo
   */
  @Override
  public int getEIThresh() {return mySpace.ss.ei_thresh;}
  @Override
  public double getEIRate() {return mySpace.ss.ei_rate;}
  @Override
  public double getEIResponse() {return mySpace.ss.ei_response_factor;}
    
  /*
   * Implementations for ELInfo
   */
  @Override
  public int getELThresh() {return mySpace.ss.el_thresh;}
  @Override
  public double getELRate() {return mySpace.ss.el_rate;}
  @Override
  public double getELResponse() {return mySpace.ss.el_response_factor;}
    
  /*
   * Extra Implementations for ReactionInfo
   */
  @Override
  public Map<EnzymeGroup, Double> getRxnProbMap() {return rxnProbMap;}
  @Override
  public Map<EnzymeGroup, Map<String,Double>> getProductionMap() {return productionMap; }
  @Override
  public java.util.ArrayList<MobileObjectType> getMobileObjectTypes() {return mySpace.ss.hepStruct.model.allMobileObject; }
  
  static double GSH_DEPLETION_INC = Double.NaN;
  static sim.util.Double2D GSH_DEPLETION_RANGE = null;
  public static void setGSHDepletion(sim.util.Double2D range, double inc) {
    GSH_DEPLETION_RANGE = range;
    GSH_DEPLETION_INC = inc;
  }
  
  double gsh_threshold = Double.NaN;
  double gsh_accumulator = 0.0;
  public double getGSHDepletion() { return gsh_accumulator; }

  Transporter altTransporter = null;
  static int initaltAmount = 0;
  static int altThreshold = 0;
  int altAmount = 0;
  public int membrane_damage = 0;
  public int init_membrane_damage = 0;
  private MobileObjectType altMT = null;
  public boolean useALT = false;
  public boolean necroticRelease = true;
  public static void setALT(int amount, int threshold) {
      initaltAmount = amount;
      altThreshold = threshold;
  }
  
  public int getALTAmount() { return altAmount; }
  
  public void altRelease(){
    // if ALT mobileobject exists (checked in initialization), then use ALT release mechanism
    if (useALT) {
        //int numnMD = bsg.util.CollectionUtils.countType(this.mobileObjects, "nMD");
        //log.debug("H:"+id+" - number of nMD = "+numnMD);
        membrane_damage = altTransporter.getmembDamageCount(); 
        long cycle = this.mySpace.ss.hepStruct.model.getCycle();
        //log.debug("time: "+cycle+" H:"+id+" - membrane_damage = "+membrane_damage);
        if (membrane_damage > altThreshold) {
            //int nALT = membrane_damage-init_membrane_damage;
            // if membrane damage is > alt threshold, then create an ALT MobileObject
            //log.debug("at time: "+cycle+" H:"+id+" - nALT = "+nALT);
            if (altAmount > 0) {
                Solute ALT = new Solute(altMT);
                ALT.setProperties(altMT.properties);
                altTransporter.scheduleTransport(ALT);
                altAmount--;
            }  
        } // end of membrane damage checks
        //init_membrane_damage = membrane_damage;
        //log.debug("time: "+cycle+" H:"+id+" - alt_amount = "+altAmount);
    } // end if useALT  
  }
  
  public void transportMobileObject(MobileObject m) {
    // transporting the ALT MobileObject is presenting the MobileObject to the Hepatocyte
    //log.debug("Hepatocyte:"+id+" transporting MobileObject at cycle = "+ myGrid.ss.hepStruct.model.getCycle());
    // first present it, then immediately remove it as a leak.
    present(m);
    remove(m, true);
  }

  @Override
  public void add(MobileObject m) {
    super.add(m);
    mySpace.putMobileObjectAt(m, myX, myY);
    if (!eliminatedByGSH(m)) bileAndAmpHandling(m);
  } // end public void add(MobileObject m)

  private boolean eliminatedByGSH(MobileObject m) {
    boolean retVal = false;
    double accuDec = Double.NaN;
    if (m.hasProperty("gshUp") && (accuDec = ((Double)m.getProperty("gshUp"))) > 0.0 ) {
      if (!m.hasProperty("pGSHUp")) throw new RuntimeException(m.getTypeString()+" has gshUp but not pGSHUp probability.");
      else {
        double pGSHUp = (Double)m.getProperty("pGSHUp");
        if (cellRNG.nextDouble() < pGSHUp) {
          gsh_accumulator -= accuDec;
          if (gsh_accumulator < 0.0) gsh_accumulator = 0.0;
          forget(m);
          mySpace.ss.gshUpEliminated++;
          retVal = true;
        } 
      }
    }
    return retVal;
  }
  
  private void bileAndAmpHandling(MobileObject m) {
    if (sendToBile(m)) {
      // remove it from me and mySpace, but not from SS, and count as an exit
      remove(m); mySpace.removeMobileObject(m);
    } else {
      amplify(m);
    } // end Bile else clause
  }
  
  private boolean sendToBile(MobileObject m) {
    boolean retVal = false;
    boolean depletesGSH = false;
    Object dgsh = null;
    if ((dgsh = m.getProperty("depletesGSH")) != null) depletesGSH = ((Boolean)dgsh);
    
    // Set bileRatio to zero if GSH threshold is breached
    double br = Double.NaN;
    boolean is_GSH_breached = false;
    if (gsh_accumulator >= gsh_threshold) {
      is_GSH_breached = true;
      br = 0.0;
    } else {
      br = ((Double)m.getProperty("bileRatio"));
    }

    // if draw < bileRatio and there's room, move it to bile
    if (cellRNG.nextDouble() < br && (mySpace.ss.bileCanal.getCC() 
            - mySpace.ss.bileCanal.getTube()[myY].size()) > 1) {
      retVal = true;
      // if a GSH-depleting MobileObject is added to Bile, increment the accumulator
      if (depletesGSH) gsh_accumulator += GSH_DEPLETION_INC;
      mySpace.ss.bileCanal.getTube()[myY].add(m);
    }
    return retVal;
  }
  
  private void amplify(MobileObject m) {
      String sType = null;
      sType = m.getTypeString();
      MobileObjectType mt = mySpace.ss.hepStruct.model.delivery.getMobileObjectType(m);
      boolean amplify = mt.isAmplified();
      
      //if ((gsh_accumulator >= gsh_threshold) && amplify) {
      if (amplify) {
        // get random int between 0 and 2 from unifrom distribution
        //int nAmplify = cellRNG.nextInt(3);
        int ampmin = mt.ampRange.x;
        int ampmax = mt.ampRange.y;
        int nAmplify = ampmin + cellRNG.nextInt(ampmax-ampmin);

        // get random int from a gaussian distribution with mean 10 and std dev 1
        // nextGaussian() produces a double from a gaussian dist with mean 0 and std dev 1
        //double mean = 4;
        //double std = 1;
        //int nAmplify = (int) StrictMath.round(cellRNG.nextGaussian()*std + mean);
        //if (nAmplify < 0) {
        //  nAmplify = StrictMath.abs(nAmplify);
        //}

        // make n copies of amplified MobileObject objects and add to arrays
        MobileObject ampedSolute = null;
        // MobileObjectTypes contain the tag, bindable, doseRatio, and props for ampedMobileObject
        java.util.ArrayList<MobileObjectType> MobileObjectTypes = this.getMobileObjectTypes();

        for (int i=0 ; i<nAmplify ; i++) {
          ampedSolute = new Solute(mt);
          ampedSolute.setProperties(mt.properties);
          // add it to SS's and Cell's MobileObject list
          present(ampedSolute, false);
        } // loop over the amount of ampedSolute amplification
      } // end if (amplify)
  }
  
  @Override // from ReactionHandler
  public void incRxnProd(String st) { mySpace.incRxnProd(st,myX,myY); }
  
  public boolean necrotic = false;
  public void necrose() {
    //log.debug("Hepatocyte:"+id+" dying at cycle = "+ parent.hepStruct.model.getCycle());
    necrotic = true;
    actionShuffler.clear();
    mySpace.ss.necrotic(this);
    // if necrotic release, release remaining ALT
    if (necroticRelease) {
        this.altTransporter.TRANSPORT_DELAY_MIN = 0;
        this.altTransporter.TRANSPORT_DELAY_MAX = 0;
        //int sched = this.altTransporter.TRANSPORT_DELAY_MIN;
        //long cycle = this.myGrid.ss.hepStruct.model.getCycle();
        //log.debug("time: "+cycle+" dead H:"+this.id+" - pre_alt_amount = "+this.altAmount);
        while (this.altAmount > 0) {
            Solute ALT = new Solute(altMT);
            ALT.setProperties(altMT.properties);
            this.altTransporter.scheduleTransport(ALT);
            this.altAmount--;
        } 
        //log.debug("time: "+cycle+" dead H:"+this.id+" - post_alt_amount = "+this.altAmount);
        //log.debug("altTransporter: dead H:"+this.id+".scheduleTransport() at "+cycle+" for "+(cycle + sched));
    }

    //unbind all the bound MobileObject
    getEnzymeGroups().forEach((eg) -> { eg.boundMobileObjects.clear(); });
    // bypass official removal because necrosis is different from membrane cross
    mobileObjects.clear();
    // it should be safe to remove myself from the SSGrid because the SS executes
    // me via the SS.cells list in SS.stepBioChem().
    mySpace.getCellGrid().set(myX, myY, null);
  }
  
  public static void main(String[] args ) {
    ec.util.MersenneTwisterFast rng = new ec.util.MersenneTwisterFast(234567890);
    Binomial b = new Binomial(5,0.99,rng);
    for (int trials=0 ; trials<30 ; trials++) {
      int indsuc = 0;
      for (int i=0 ; i<1000 ; i++ ) {
        if (rng.nextDouble() < 0.99) indsuc++;
      }
      int msuc = 0;
      for (int i=0 ; i<1000 ; i++ ) msuc += b.nextInt(1,0.99);
      System.out.println("individual, binomial, mbinomial ⇒ "+indsuc+", "+b.nextInt(1000,0.99)+", "+msuc);
    }
  }
}
