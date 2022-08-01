/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package isl.measurement.view;

import ec.util.ParameterDatabase;
import isl.model.AbstractISLModel;
import isl.model.ISL;
import isl.model.data.CSVDataModel;
import isl.model.ref.RefModel;
import java.io.IOException;

public class BatchControl extends sim.engine.SimState
{
  private static final long serialVersionUID = -5692983819707456272L;
  public BatchControl getThis() { return this; }
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( BatchControl.class );

  public String svnversion = null;
  public ParameterDatabase pd = null;
  public int cycleLimit = -Integer.MAX_VALUE;
  public int mc_trials = -Integer.MAX_VALUE;
  public int trial_count = 0;
  public String run_file_base = null;
  public boolean measureHMobileObject = false;
  public boolean measureAllMobileObject = false;
  public boolean measureMITs = true;
  public boolean takeNTSnaps = false;

  isl.measurement.Observer observer = null;
  isl.measurement.view.BatchView batchView = null;
  java.util.LinkedHashMap<sim.engine.Steppable, sim.engine.Stoppable> stops = null;
  /**
   * isl - the models
   */
  isl.model.ISL isl = null;
  public isl.model.ISL getISL() { return isl; }
  isl.model.data.CSVDataModel dataModel = null;
  public isl.model.data.CSVDataModel getDataModel() { return dataModel; }
  isl.model.ref.RefModel refModel = null;
  public isl.model.ref.RefModel getRefModel() { return refModel; }
  String mode = null;
  public int localSeed = -Integer.MAX_VALUE;;
  public void setLocalSeed(int s) { localSeed = s; }
  public ec.util.MersenneTwisterFast bcRNG = null;
  
  /**
   * Creates a new instance of BatchControl
   */
  public BatchControl ( long seed, String[] args ) {
    super( seed ); // create with this seed which might be replaced
    
    try {
      ParameterDatabase buildpd = new ParameterDatabase( this.getClass().getClassLoader().getResourceAsStream("cfg/build.prop") );
      svnversion = buildpd.getStringWithDefault(new ec.util.Parameter("SVNVERSION"), null, "NOT SET!");
      if (svnversion.equals("NOT SET!")) throw new RuntimeException("SVNVERSION not set.");
      pd = new ParameterDatabase( this.getClass().getClassLoader().getResourceAsStream("cfg/batch_control.properties") );
    } catch ( IOException | RuntimeException e ) {
      System.err.println( e.getMessage() );
      System.exit( -1 );
    }

    ControlParams.loadParams(this,pd);
    // replace the pRNG regardless
    setSeed(localSeed); // doesn't seem to have any affect
    bcRNG = new ec.util.MersenneTwisterFast(localSeed);
    random = bcRNG;
    
    /**
     * running a standAlone sim?
     */
    mode = standAlone(args);
  }

  @Override
  public void start () {
    super.start();
    log.info("BatchControl.start() -- svnversion = " + svnversion + ", seed = " + seed() + ", localSeed = " + localSeed );// + ", bcRNG.nextDouble() "+ bcRNG.nextDouble());
    stops = new java.util.LinkedHashMap<>(2);
    sim.engine.Stoppable stopper = null;
    if (mode == null || mode.equals("experimental")) {
       ISL.action_order = 100;
       isl = new isl.model.ISL( this );
       isl.start();
    }
    
    if (trial_count == 0 && (mode == null || mode.equals("data"))) {
       CSVDataModel.action_order = 96; // need 2, isDone at action_order+1
       dataModel = new CSVDataModel(this); // seed is irrelevant
       dataModel.start();
    }

    if (trial_count == 0 && (mode == null || mode.equals("reference"))) {
       RefModel.action_order = 98; // need 2, isDone at action_order+1
       refModel = new RefModel(this);
       refModel.start();
    }
    
    // if no models exist, then don't schedule an observer
    // if this is the second or higher mc trial, don't observe the ref and dat models
    isl.model.data.CSVDataModel trialDM = (trial_count == 0 ? dataModel : null);
    isl.model.ref.RefModel trialRM = (trial_count == 0 ? refModel : null);
    if (isl != null || dataModel != null) {
       observer = new isl.measurement.Observer( this, isl, trialDM, trialRM );
       stopper = schedule.scheduleRepeating( observer, /* ordering */ ISL.action_order+100, /* interval */ 1);
       stops.put(observer, stopper); 
       // HACK -- Calling observer.init() here
       // It seems clear that the Observer's init method needs to be called
       // but I'm not sure when or where it makes the most sense until I
       // learn the code a little better. -- K.Cline, 4/25/2011
       observer.init();
    }
    
    batchView = new isl.measurement.view.BatchView( this, isl, trialDM );
    batchView.setLogger(log);
    stopper = schedule.scheduleRepeating( batchView, /* ordering */ ISL.action_order+800, /* interval*/ 1);
    stops.put(batchView, stopper);
    
    installMonteCarloCheck();
    trial_count++;
  }

  /**
   * installMonteCarloCheck() - places a Steppable on the schedule that checks
   * to see if we need to restart BatchControl because there are more trials to
   * be executed in this VM.
   */
  public void installMonteCarloCheck() {
    schedule.scheduleRepeating((sim.engine.SimState state) -> {
      if (isl.isFinished() && dataModel.isFinished() && refModel.isFinished()) {
        if (trial_count < mc_trials) {
          log.info("BatchControl: trial " + trial_count + " of " + mc_trials + " finished.");
          schedule.reset();
          observer.finish();
          start();
        } else {
          log.info("BatchControl: finished!");
          finish();
          observer.finish();
        } // end of simulation
      } else {
        log.info("BatchControl: continuing trial " + trial_count
                + " isl: " + isl.getStatus()
                + " dataModel: " + dataModel.getStatus()
                + " refModel: " + refModel.getStatus());
      }
    } /* end new lambda Steppable */,
            /* ordering */ ISL.action_order + 900,
            /* interval */ 1);
  } // end installMonteCarloCheck() {
  
  /*
   * stops the repeating events scheduled by BatchControl at the request of 
   * the objects being scheduled
   */
  public void stopMe(sim.engine.Steppable s) {
    log.info("BatchControl.stopMe("+s.getClass().getName()+") - begin.");
     if (s == observer) {
        /**
         * check if isl exists and is stopped
         * if so, stop the dataModel
         */
        if (isl != null && isl.getStatus() != AbstractISLModel.ModelStatus.STARTED)
           if (dataModel != null) dataModel.stop();
     }
     stops.get(s).stop();
  }
  
  public float getTime () {
    if ( isl == null ) return 0.0F;
    double t = isl.mason2IslTime( schedule.getTime() );
    // need the below so that "Start" doesn't show negative time
    return (float)( t < 0.0 ? 0.0 : t );
  }

  public long getCycleLimit () {
    if ( isl == null ) {
      return 0L;
    }
    return isl.getCycleLimit();
  };
  public void setCycleLimit( long cl ) {
     if ( isl != null ) isl.setCycleLimit(cl);
     if (refModel != null) refModel.setCycleLimit(cl);
     if (dataModel != null) dataModel.setCycleLimit(cl);
  }

  // can't use sim.engine.SimState because they're private bla
  public static boolean keyExists(String key, String[] args, int startingAt) {
    for (String arg : args) if (arg.equalsIgnoreCase(key)) return true;
    return false;
  }
  public static String argumentForKey(String key, String[] args, int startingAt) {
    for (int x = 0; x < args.length - 1; x++) // key can't be the last string
      if (args[x].equalsIgnoreCase(key)) return args[x + 1];
    return null;
  }

   static String standAlone(String[] args) {
      String retVal = null;
      if (keyExists("-standalone", args, 0)) {
         retVal = argumentForKey("-standalone", args, 0);
      }
      return retVal;
   }
   
   // some configurations disallow using the GUI, regardless of the properties file
   public static boolean canUseGUI(String[] args) {
     boolean canUseGUI = true;
     String mode = standAlone(args);
     log.info("BatchControl: "+mode);
     if (mode == null) {
       // do nothing
     } else if (mode.equals("experimental")) {
       // do nothing
     } else if (mode.equals("data")) {
       // override properties gui setting
       log.warn("Overriding batch_control.properties GUI setting to run data model stand alone.");
       canUseGUI = false;
     } else if (mode.equals("reference")) {
       canUseGUI = false;
       log.warn("Overriding batch_control.properties GUI setting to run reference model stand alone.");
     } else {
       throw new RuntimeException("Invalid Stand Alone option: " + mode + ".\n");
     }
      return canUseGUI;
   }

   // stolen from sim.engine.SimState so we can parse our own args first
   @SuppressWarnings("rawtypes") // we could fork MASON's class
   public static void doLoop(final Class c, String[] args) {
     log.info("BatchControl: in local doLoop()");
     doLoop(
             new sim.engine.MakesSimState() {
               @Override
               public sim.engine.SimState newInstance(long seed, String[] args) {
                 // ignore the seed passed in from super
                 return new isl.measurement.view.BatchControl(seed, args);
               }
               @Override
               public Class<?> simulationClass() { return c; }
               }, args);
   }

}
