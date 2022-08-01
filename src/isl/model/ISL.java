/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import isl.model.cell.KupfferCell;
import isl.model.cell.Hepatocyte;
import isl.model.cell.EC;
import isl.model.cell.Cell;
import isl.io.HepInit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public strictfp class ISL extends AbstractISLModel
{
  // TBD: Generate serialization ID using serialver tool
  private static final long serialVersionUID = 1L;

  protected Map<String,Number> inputs = new java.util.LinkedHashMap<>();
  public ArrayList<MobileObjectType> allMobileObject = new java.util.ArrayList<>();
  public Map<String,Number> getInputs() { return inputs; }
  
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( ISL.class );

  public ec.util.ParameterDatabase pd = null;

  public int stepsPerCycle = -Integer.MAX_VALUE;
  public String context = null;
  
  public boolean useBody = true;
  /**
   * Panmictic space into which doses are injected, then passed on to the PV, 
   * used when "useBody = true" in the properties file.
   */
  public Body body = null;
  sim.util.Double2D bxrBand = null;
  java.util.ArrayList<String> bodyXferTypes = null;
  double bxrM = Double.NaN;
  boolean recirculate = false;
  boolean useIntro = false;
  /**
   * Panmictic space into which doses are injected, then passed on to the Body, 
   * used when "useIntro = true" in the properties file.
   */
  IntroCompartment introCompartment = null;
  public double introSample = Double.NaN;

  public double gridScale = Double.NaN;
  public double coreScale = Double.NaN;
  
  public HepStruct hepStruct = null;
  public HepStruct getHepStruct() { return hepStruct; }
  public boolean hepInitRead = false;
  public boolean hepInitWrite = false;
  public DeliveryMethod delivery = null;

  public ISL ( isl.measurement.view.BatchControl p ) { 
    super(p); // create a dummy random
    log.info("ISL child of " + parent + " started.\n");
    pd = loadParams();
    initConstituents(pd);
  }

  /**
   * TBD: We may not want to call this from start(), since the parameters may
   * not change between multiple executions.
   * @return 
   */
  private ec.util.ParameterDatabase loadParams() {
    ec.util.ParameterDatabase retVal = null;
    try {
      retVal = new ec.util.ParameterDatabase( this.getClass().getClassLoader().getResourceAsStream("cfg/isl.properties") );
    } catch ( IOException e ) {
      System.err.println( e.getMessage() );
      System.exit( -1 );
    }
    ISLParams.loadParams( this, retVal );
    cycleLimit = parent.cycleLimit;
    return retVal;
  }

  /**
   * TBD: We may not want to call this from start().  We have the option of
   * re-initializing objects or recreating them from scratch.
   * @param pdb 
   */
  private void initConstituents(ec.util.ParameterDatabase pdb) {
    cycle = 0;
    
    MobileObjectType.loadTypeParams(this);
    
    hepStruct = new Lobule( this, pdb, parent.bcRNG );
    hepStruct.setLogger(log);
    if ( ! hepStruct.init() ) {
      throw new RuntimeException("HepStruct init failed.");
    }
    
    // set unified gradient
    hepStruct.setUnifiedGradient(hepStruct.uniGradrange, hepStruct.uniGradtype);
    
    /**    
    sim.util.Bag edges = hepStruct.getEdgesOut(((Lobule)hepStruct).portalVein);
    LiverEdge first = (LiverEdge) edges.objs[0];
    for (Object o : edges) {
      LiverEdge e = (LiverEdge) o;
      LiverNode tgt = (LiverNode)e.to();
      log.debug("PV → LN:"+tgt.id);
      sim.util.Bag inEdges = hepStruct.getEdgesIn(tgt);
      log.debug("LN:"+tgt.id+ " has "+inEdges.numObjs+" inputs.");
    }
    **/    
    
    double bxr = bxrBand.x+parent.bcRNG.nextDouble()*(bxrBand.y-bxrBand.x);
    if (useBody) body = new Body(this, parent.bcRNG, hepStruct.structOutput, hepStruct.structInput, bxr);
    if (useIntro) {
      log.info("introSample = "+introSample);
      introCompartment = new IntroCompartment(this, parent.bcRNG, null, (useBody ? body : hepStruct.structInput), introSample);
    }
    
    
    Cell.MET_ENV = new MetabolicEnvironment();
    Cell.MET_ENV.setLogger(log);
    Cell.MET_ENV.init();

    delivery = new DeliveryMethod( hepStruct );
    delivery.setLogger(log);
    delivery.init( mason2IslTime(cycleLimit) );

    // must have both delivery and metabolic environment loaded to set bindable flags    
    for (EnzymeGroup eg : Cell.MET_ENV.enzymeGroups.values()) {
      for (String mt : eg.acceptedMobileObjects) {
        for (MobileObjectType de : allMobileObject) {
          if (mt.equals(de.tag)) de.bindable = true;
        }
      }
    }

    HepInit hinit = null;
    if (hepInitRead) {
      java.io.InputStream is = null;
      try {
        is = new java.io.FileInputStream(bsg.util.ClassUtils.getResourceFile(
        String.format("cfg/hepinit-%04d",parent.trial_count)+".json"));
      } catch (java.io.IOException ioe) {
        System.err.println(ioe.getMessage());
        System.exit(-1);
      }
      hinit = HepInit.readOneOfYou(is);
      System.out.println("HepInit has "+hinit.getHepCount());
    }
    
    // Hepatocytes need initializing _after_ delivery is loaded.
    // All Cells need initializing _after_ environment is loaded.
    for (Object o : hepStruct.allNodes) {
      if (o instanceof SS) {
        SS ss = (SS)o;
        for (Object c : ss.cells) {
          if (c instanceof Hepatocyte) {
              Hepatocyte h = (Hepatocyte) c;
              if (hepInitRead) h.init(hinit);
              else h.init();
          } else if (c instanceof EC) {
              EC ec = (EC) c;
              ec.init();
          } else if (c instanceof KupfferCell) {
              KupfferCell kc = (KupfferCell) c;
              kc.init();
          }
        }

        for (Object c : ss.cells) {
          if (c instanceof Hepatocyte) {
            Hepatocyte h = (Hepatocyte) c;
            StringBuilder out = new StringBuilder("SS:"+ss.id+" - Hepatocyte:"+h.id+" enzyme groups = ");
            ((Hepatocyte)c).getEnzymeGroups().forEach((eg) -> { out.append(eg.type).append(" "); });
            log.info(out.toString());
            break;
          }
        }
        
      }
    }
    
    //Add VN interface neighbors to the data structure neighbors upon creation
    sim.util.Bag result = new sim.util.Bag();
    sim.util.IntBag xPos = new sim.util.IntBag();
    sim.util.IntBag yPos = new sim.util.IntBag();
    ArrayList<Hepatocyte> neighbors = null;
    ArrayList<Hepatocyte> mNeighbors = null;

    for (Object o : hepStruct.allNodes) {
      if (o instanceof SS) {
        SS ss = (SS)o;
        for (Object c : ss.cells) {
          if (c instanceof Hepatocyte) {
              Hepatocyte h = (Hepatocyte) c;
              h.mySpace.getCellGrid().getVonNeumannNeighbors(h.myX, h.myY, 1, sim.field.grid.Grid2D.TOROIDAL, false, result, xPos, yPos);
              mNeighbors = new ArrayList<>(result);
              neighbors = new ArrayList<>();
                for(Hepatocyte hN: mNeighbors) {
                    if(Math.abs(h.myY-hN.myY) <= 2) //Manhattan Dist of 2
                        neighbors.add(hN);
                }
          }
        }
      }
    }

    if (hepInitWrite) {
      hinit = new HepInit();
      hinit.setDescription(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date()));
      hinit.setNetwork(hepStruct.layers, hepStruct.edges);
      double max_grid = 0.0;
      for (int zNdx=1 ; zNdx < hepStruct.layers.size() ; zNdx++) {
        max_grid += hepStruct.hepStructStats.get(zNdx-1).get("max");
      }
      hinit.setRsrcMaxGrid((int)max_grid);
      for (Object o : hepStruct.allNodes) {
        if (o instanceof SS) {
          SS ss = (SS)o;
          for (Object oc : ss.hSpace.celGrid.elements()) {
            Hepatocyte h = (Hepatocyte)oc;
            hinit.addHepatocyte(h.id, h.getDPV(false), h.getDCV(false), h.getRxnProbMap());
          }
        }
      }
      isl.io.OutputLog hepInitOutputLog = new isl.io.OutputLog(String.format("hepinit-%04d",parent.trial_count)+".json", false);
      hepInitOutputLog.monln(hinit.describe());
      hepInitOutputLog.finish();
    }
    
    // initialize the outputNames list
    outputNames = new java.util.ArrayList<>();
    java.util.ArrayList<MobileObjectType> bolus_entries = allMobileObject;
    bolus_entries.forEach((be) -> { outputNames.add(be.tag); });
  }
  
  @Override
  public void buildObjects() {}

  sim.engine.Steppable isDoneStepper = null;
  @Override
  public void buildActions() {
    
    delivery.start(parent); // schedules Doses

    if (useIntro) parent.schedule.scheduleOnce(introCompartment,ISL.action_order+1);
    if (useBody) parent.schedule.scheduleOnce(body,ISL.action_order+1);
    
    // promoted direct call to hepStruct.step() from isl.step() to an event
    parent.schedule.scheduleOnce(hepStruct, ISL.action_order+1);

    if (!parent.schedule.scheduleOnce(this, ISL.action_order)) {
      throw new RuntimeException("Failed to schedule ISL.step()\n");
    }

    // schedule isDone()
    if (isDoneStepper == null) isDoneStepper = createStopAction(this);
    log.info("ISL.buildActions() -- scheduling ISL.isDone() at {}.", new Object[]{getCycleLimit()});
    parent.schedule.scheduleOnce(0.0, ISL.action_order+700, isDoneStepper);
  }

  double model_time = Double.NaN;
  @Override
  public double getTime() {
     return model_time;
  }
  
  @Override
  public void step ( sim.engine.SimState state ) {
    model_time = mason2IslTime(state.schedule.getTime());
    log.info("ISL.step() -- " + status + ": cycle = " + cycle + 
            ", cycleLimit = " + cycleLimit + 
            ", ISL time = " + model_time + 
            ", MASON time = " + state.schedule.getTime());
    
    // herein we calculate the model's output fraction, which waits to be measured
    updateOutputs();

    // reschedule myself
    if ( (status == ModelStatus.STARTED) 
            && !parent.schedule.scheduleOnce(isl2MasonTime(getTime())+1, ISL.action_order,
      this))
       throw new RuntimeException("Finished or otherwise failed to schedule ISL.step()\n");

    cycle++;
  }
   
  public double mason2IslTime ( double masonTime ) {
    return masonTime/stepsPerCycle;
  }

  public double isl2MasonTime ( double islTime ) {
    return islTime*stepsPerCycle;
  }

  public int diffTime ( double t1, double t2 ) {
    return (int)Math.floor((t1-t2) * stepsPerCycle);
  }

  /**
   * Checks to see if the ISL has reached its stopping criteria.
   * @param s
   * @return 
   */
  @Override
  public boolean isDone (sim.engine.SimState s) {
    boolean retVal = false;
    log.info("ISL.isDone() - getCycle() = {}, getCycleLimit() = {}", new Object[] {getCycle(), getCycleLimit()});
    if (getCycle() > getCycleLimit()) retVal = true;
    if (!retVal) parent.schedule.scheduleOnce(isl2MasonTime(getTime()+1), ISL.action_order+700, isDoneStepper);
    return retVal;
  }

  long output = 0;
  public void updateOutputs () {
    log.info("ISL.updateOutputs() - cycle = "+cycle 
            + ", mason time = "+parent.schedule.getTime()
            + ", isl time = "+getTime());
     long newCVCount = (hepStruct.structOutput.outputs == null ? 0 
             : bsg.util.CollectionUtils.sum_mi(hepStruct.structOutput.outputs));
     output = newCVCount;
     outputFraction = (double)output/(double)delivery.maxMobileObject;
     
     outputs.clear();
     inputs.clear();
     outputs.put("Time", getTime());
     inputs.put("Time", getTime());
     // output mobile objects in CV is kept track of in CV.outputs map
     // input mobile objects in the PV is kept track of in the PV.distributed map
     //for (String key : outputNames) {
     outputNames.stream().forEach((key) -> {
        Number outInt = (hepStruct.structOutput.outputs == null ? null 
                : hepStruct.structOutput.outputs.get(key));
        long nowOut = (outInt != null ? outInt.longValue() : 0);
        outputs.put(key,nowOut);

        Number inInt = hepStruct.structInput.getDistributed().get(key);
        long nowIn = (inInt != null ? inInt.intValue() : 0);
        inputs.put(key,nowIn);
     });
     log.info("ISL.updateOutputs() -- inputs = "+bsg.util.CollectionUtils.describe(inputs));
  }

  /**
   * iterative describe prints out all the info at this level and calls
   * describe at all the sub-levels.
   * @return 
   */
  public String describe () {
    StringBuilder sb = new StringBuilder();
    sb.append( "cycles " ).append( cycle )
      .append( "/"  ).append( cycleLimit  ).append( "\n");
    sb.append(hepStruct.describe() );
    return sb.toString();
  }

}
