/*
 * IPRL - Abstract ISL Model
 *
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package isl.model;

import static isl.model.AbstractISLModel.ModelStatus.*;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TBD: Should the AbstractISLModel have type variables in the class
//      definition?
//
//      For example, instead of specify the output type to be Double
//      we could make that a type variable,e.g.
//          public abstract class AbstractISLModel<T> extends ...
//
//      Another type variable could be used to define what class
//      is used for keys in the output Map, e.g. String, Object
//      or something else.  Those are the only parts of this class
//      that I see which we might want to use type variables for.
//      Subclasses my need others, of course.
//

/**
 * TBD: Add class description
 *
 * @future
 *    - 
 *
 * @author Ken Cline
 * @see SimState
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
public abstract class AbstractISLModel  implements  sim.engine.Steppable {
  // TBD: Generate serialization ID using serialver tool
  // TBD: Add doc
  private static final long serialVersionUID = 1L;    

  // TBD: Add doc
  private static final Logger log = LoggerFactory.getLogger( AbstractISLModel.class );
  // Future: private static final Logger log = LoggingUtils.getLogger();

  public isl.measurement.view.BatchControl parent = null;
  
  // TBD: Move ModelStatus to it's own .java file or perhaps defined
  //      a Model interface with a Status enum ?

  /**
   * Model processing state.  A model can be in one of the following
   * states:
   * <ul>
   * <li>{@link #NEW}<br>
   *     A model that has been created but <i>not</i> yet started
   *     is in this state.
   *     </li>
   * <li>{@link #STARTED}<br>
   *     A model transitions to this state at the end of its {@link #start}
   *     method after all initialization steps have been completed.
   *     The model remains in this state until terminated.
   *     </li>
   * <li>{@link #STOPPED}<br>
   *     A model transitions to this state when its {@link #stop} or
   *     {@link #finish} method is called.  The change typically occurs
   *     at the end of the method after all termination steps have
   *     been completed.
   *     </li>
   * <li>{@link #KILLED}<br>
   *     A model transitions to this state at the end of its {@link #kill}
   *     method after all termination steps have been completed.
   *     </li>
   * </ul>
   *
   * <p>
   *
   * A model can be in only one state at a given point in time.
   * The user does not change the state directly; the model handles
   * this when a method such as {@link start} or {@link stop} is
   * called.
   *
   * @tbd  Currently there are two (2) termination states:
   *       {@link #STOPPED} and {@link #KILLED}.  These two states
   *       are distinct so that user can differentiate between
   *       natural exits and interrupted processing.  This may
   *       not be needed and perhaps we should consider simplifying
   *       to only one termination state.
   *
   * @note This enum was renamed from "ModelState" to "ModelStatus"
   *       so that it would not be confused with the {@link SimState}
   *       class.
   *
   * @see #getStatus
   */
  public enum ModelStatus {
    /**
     * State for a model which has <i>not</i> started yet.
     */
    NEW,
    /**
     * State for a model begun by calling the <code>start</code>
     * method.
     */
    STARTED,
    /**
     * State for a model terminated by the <code>stop</code> or
     * <code>finish</code> methods.  The model execution has
     * completed.
     */
    STOPPED,
    FINISHED,
    /**
     * State for a model terminated by the <code>kill</code> method.
     * The model execution has completed.
     */
    KILLED
  }


  // TBD: What name to using for the initial state before the model is
  //      started?  NEW, INIT, CREATED, PRE_START, ???

  // TBD: Do we need a RUNNING state after the STARTED state?
  //      I suppose that we could move from STARTED to RUNNING the
  //      first time that 'step' is called?
  //      Or perhaps, we could have a STARTING state which is used
  //      at the beginning of 'start' and at the end of the 'start'
  //      method we would transition to RUNNING.

  // TBD: Do we need a COMPLETE state after the STOPPED state?
  //      Should we have STOPPING and then a STOPPED or COMPLETE state?
  //
  //      Can model's be "reactivated"?  That is, could then be
  //      restarted after being stopped?  If so, then perhaps we
  //      need a state, e.g. TERMINATED, which implies that the
  //      model is not re-startable?

  // TBD: Do we need the KILLED state?


  public static int action_order = 100;
  public static final double  ACTION_INTERVAL  = 1.0;
  public static final long    MAX_CYCLE        = Long.MAX_VALUE;
  public static final long    BEFORE_START     = -1;


  /**
   * TBD: Add doc
   * TBD: Move to utility class
   * FLT_MIN value from C
   */
  public static final float FLT_MIN      = 1.17549435e-38F;
  public static final float FLT_EPSILON  = 1.19209290e-7F;

  // TBD: Is there a library/package out there that already provides
  //      all the C constants in Java for compatibility?
  //      I'm quite sure there probably is, but I need to look for it.
  //
  // TBD: I could create a utility that dynamically defines constants
  //      at runtime and we could design it to query gcc or parse
  //      the C header files to determine what the values should be
  //      for the platform that we are running on.  Or we could read
  //      them in from properties file and so we can decide which
  //      settings to use (for compatibility)


  /**
   * TBD: Add doc
   *
   */
  protected String name = getClass().getSimpleName();

  // TBD: Create "NamedModel" super class that all our models will likely extend.
  //      Actually we should (also) create an interface called "Named" or
  //      "Nameable", if that does not already exist, which marks object that
  //      support getName and optionally setName.


  /**
   * TBD: Add doc
   *
   */
  protected ModelStatus status = NEW;

  protected long cycle = BEFORE_START;
  protected long cycleLimit = -Integer.MAX_VALUE;

  protected Map<Object,sim.engine.Stoppable> actions = null;

  protected Map<String,Number> outputs = new java.util.LinkedHashMap<String,Number>();

  // TBD: What type should we use for 'outputs'?
  //      E.g. Map<?,?>, Map<String,Object>, Map<String,Double>,
  //      Map<Object,Double> or something else?

  /**
   * This is a list of Strings used to output the mobile object types to the header
   * of the run log for each monte carlo trial.  It should be set early enough
   * in the instantiation of the simulation so that it can be written to a
   * file prior to the first observation.  When/if we stop using files and
   * use a database like HDF5, we can determine this dynamically.
   */
  protected List<String> outputNames = null;

  protected Double outputFraction = Double.NaN;
  protected Double extractionRatio = Double.NaN;

  // === [ Constructors ] =================================================

  public AbstractISLModel ( isl.measurement.view.BatchControl p ) {
    parent = p;
    status = NEW;
  }

  
  // === [ Getters & Setters: Name ] ======================================

  /**
   * TBD: Add doc
   *
   */
  public String getName () { return name; }

  /**
   * TBD: Add doc
   *
   * TBD: Add validation, e.g. not null, valid chars, etc
   * TBD: Also we might want this to only be set once and/or not changeable
   *      after model construction... so we might need a status/mode/phase
   *      flag ivar for deciding if the name is (re)settable.
   *      (See discussion about creating a "phased" object below)
   */
  public void setName ( String name ) { this.name = name; }


  // === [ Getters & Setters: Status & Schedule ] ==========================

  public ModelStatus getStatus () { return status; }
  public boolean isStarted () { return ( status == STARTED ); }
  public boolean isStopped () { return ( status == STOPPED ); }
  public boolean isKilled () { return ( status == KILLED ); }
  public boolean isFinished () { return ( status == FINISHED ); }
  
  public int getActionOrder () { return action_order; }
  public double getActionInterval () { return ACTION_INTERVAL; }

  public sim.engine.Schedule getSchedule () { return parent.schedule; }


  // === [ Getters & Setters: Cycle & Time ] ==============================

  /**
   * TBD: Add doc
   *      note: cycle < 0 before first start, = 0 at start
   *            and up until the first time step is called
   *            via subclass's step override
   *            counts the number of times step method is called
   * TBD: Put this in a try-catch perhaps?  And return -1 on failure?
   * TBD: See "Implementation Detail: Cycle Count" discussion below.
   */
  public long getCycle () { return cycle; }

  /**
   * TBD: Add doc
   *      note: cycleLimit < 0 ==> infinity (ie disable cycle
   *            count test in isDone method
   * TBD: Should we allow the cycleLimit to be disabled by
   *      setting to a value < 0?  Afterall, the user could
   *      always use Integer.MAX_VALUE instead.
   *
   */
  public long getCycleLimit () { return cycleLimit; }
  public void setCycleLimit (long cl) { 
     if (0 < cl || cl < getCycle()) {
        cycleLimit = cl;
        log.info( "{}: Cycle limit set to {}", getName(), cycleLimit);
     } else throw new RuntimeException("Invalid value for cycleLimit : "+cl);
  }


  /**
   * Returns the data model's elapsed time value for the current
   * @{link Schedule#getTime() Schedule} cycle.  The data model's
   * time is a function of the number of cycles, or steps, that
   * have occurred.  For example, after 1 step, the time might be
   * 1.5 seconds; after 2 steps it could be 4.67 seconds.
   *
   * The model's time is often determined by the data loaded at
   * initialization.  Typically this data is sampling series which
   * prescribes when each measurement occurred.
   *
   * @note As indicated above, the data model time does <i>not</i>
   *       necessary flow at the same rate as the {@link Schedule}.
   *       And it may <i>not</i> flow at a uniform nor linear rate
   *       either.  In other words it may make "leaps" of varying
   *       sizes from one cycle to the next.  However, as a time
   *       measurement, it will always be monotonically increasing.
   *       
   * @return model time as a function of the current @{link Schedule}
   *         cycle
   * @throws IndexOutOfBoundsException if the @{link Schedule}'s cycle
   *         count > number of data entries in the model
   */
  public abstract double getTime ();


  // === [ Data Output Methods ] ==========================================

  // TBD: We could promote 'outputs' to be an ivar, i.e. reuse the
  //      same Map object and that improves performance slightly.

  // TBD: Should 'getOutputs' return a Map or perhaps an Iterator?
  //      If it returns a Map, should we wrapper that object in
  //      an instance "unmodifiable" Map.  I.e. are we worried that
  //      user code might "corrupt" the outputs map?

  // TBD: We could implement a "dirty" flag that indicates whether
  //      the current outputs ivar is valid or not.  If not valid,
  //      then 'getOutputs' calls some method, e.g. calcOutputs,
  //      and saves the result in outputs and updates the "dirty"
  //      flag.  The 'calcOutputs' method would be abstract, of
  //      course.  And by default, the 'step' method would flip the
  //      validity flag automatically (unless it actually updates
  //      the outputs).

  /**
   * TBD: Add doc
   *
   */
  public Map<String,Number> getOutputs () { return outputs; }

  /**
   * TBD: Add doc
   *
   * TBD: Does this method need to be protected or can it
   *      (should it be) public?
   * TBD: Possible validation checks:
   *         (1) if model is not started, then throw exception ?
   *         (2) if model is stopped, then throw exception ?
   *         (3) data must not be null or empty ?
   *         (4) track time when outputs is set and throw exception
   *             if there is an attempt to change the outputs more
   *             than once during the same cycle ?
   *         (5) type check the 'data' argument ?
   *         (6) other
   * TBD: Should we copy the 'data' argument instead of
   *      just doing a reference assignment?
   */
  protected void setOutputs ( Map<String,Number> data ) { outputs = data; }


  // TBD: Should the default definition of 'getOutputNames()' be:
  //          return getOutputs().keySet();
  //      I.e. the output names are the keys from 'outputs' map?
  //
  //      That raises another question: Should 'outputNames' be
  //      a List or a Set?  In other words, is it acceptable to
  //      have repeat names?
  //
  //      Why are these output names really needed?  Perhaps we
  //      don't need to support this separate ivar if it is always
  //      the same as the outputs.keySet()?
  //

  // TBD: Should 'getOutputs' return a List or perhaps an Iterator?
  //      If it returns a List, should we wrapper that object in
  //      an instance "unmodifiable" List.  I.e. are we worried that
  //      user code might "corrupt" the outputNames list?

  /**
   * TBD: Add doc
   *
   */
  public List<String> getOutputNames () { return outputNames; }

  /**
   * TBD: Add doc
   *
   * TBD: Does this method need to be protected or can it
   *      (should it be) public?
   * TBD: Possible validation checks:
   *         (1) if already set, then throw exception ?
   *         (2) if model started, then throw exception ?
   *         (3) if model stopped, then throw exception ?
   *         (4) 'names' must not be null or empty ?
   *         (5) type check the 'names' argument ?
   *         (6) other
   * TBD: Should we copy the 'names' argument instead of
   *      just doing a reference assignment?
   */
  protected void setOutputNames ( List<String> names ) { outputNames = names; }

  /**
   * TBD: Add doc
   *
   */
  protected void setOutputNames ( String ... names ) { setOutputNames( java.util.Arrays.asList(names) ); }


  // TBD: Is it better for 'getOutputFraction' to return a Double object
  //      or a primitive to the caller?
  //      An object allows us to use 'null' as a return value, e.g.
  //      if the method is called before the model is started or
  //      after it is stopped.  Of course, we can also use NaN for
  //      that purpose as well.

  /**
   * TBD: Add doc
   *
   */
  public Double getOutputFraction () { return outputFraction; }

  /**
   * TBD: Add doc
   *
   * TBD: Does this method need to be protected or can it
   *      (should it be) public?
   * TBD: Should we accept any Number type or just Double?
   *      If we accept any Number subclass, then we probably
   *      should copy the value since the value object could
   *      be mutable.
   * TBD: Possible validation checks:
   *         (1) if model is not started, then throw exception ?
   *         (2) if model is stopped, then throw exception ?
   *         (3) data must not be null or empty ?
   *         (4) track time when outputFraction is set and throw exception
   *             if there is an attempt to change the outputFraction more
   *             than once during the same cycle ?
   *         (5) other
   */
  protected void setOutputFraction ( Double value ) { outputFraction = value; }


  // === [ Build Methods ] ================================================

  /**
   * TBD: Add doc
   *
   */
  public abstract void buildObjects ();


  /**
   * TBD: Add doc
   *
   */
  public void buildActions () {
    sim.engine.Schedule   schedule  = getSchedule();
    double     start     = sim.engine.Schedule.EPOCH;
    int        order     = getActionOrder();
    double     interval  = getActionInterval();
    sim.engine.Steppable  target    = this;
    sim.engine.Stoppable  action    = null;
    String     key       = null;

    if ( actions == null ) {
      // Using LinkedHashMap because it preserves the insertion order.
      // This is absolutely necessary for this class's simple scheduling
      // needs, but may be important for subclasses.
      actions = new java.util.LinkedHashMap<Object,sim.engine.Stoppable>();
    }

    key = "step";
    if ( ! actions.containsKey(key) ) {
      target = this;
      log.info( "{}: Building model actions -- scheduling 'step'"
               + " method to repeat every {} intervals"
               + " starting at time {} (action order: {})",
                 new Object[]{getName(),interval,start,order} );
      action = schedule.scheduleRepeating( start, order, target, interval );
      actions.put( key, action );
    } else {
      log.info( "{}: Building model actions -- actions map already"
               + " contains an entry for key '{}'"
               + " ==> skipping '{}' action; actions map: {}",
                 new Object[]{getName(),key,key,actions} );
    }

    key = "stop";
    if ( ! actions.containsKey(key) ) {
      order = order + 1; // or Integer.MAX_VALUE;
      target = createStopAction(this);
      // TBD: Check if target is null?
      log.info( "{}: Building model actions -- scheduling 'isDone'"
               + " method to repeat every {} intervals"
               + " starting at time {} (action order: {})",
                 new Object[]{getName(),interval,start,order+1} );
      action = schedule.scheduleRepeating( start, order+1, target, interval );
      actions.put( key, action );
    } else {
      log.info( "{}: Building model actions -- actions map already"
               + " contains an entry for key '{}'"
               + " ==> skipping '{}' action; actions map: {}",
                 new Object[]{getName(),key,key,actions} );
    }

    // TBD: Schedule a 'clear' action to reset per-step data,
    //      this should occur just before the 'step' action.
    //      See "Per-Step Reset/Clear Action" discussion below.

  }

  // TBD: Perhaps I should divide buildActions up into a method that
  //      adds the 'step' action and another method that adds the 'isDone'
  //      action.  This makes it much easier for subclasses to override
  //      the actions that are scheduled.

  // TBD: Should the 'isDone' action be schedule to be at the end of
  //      every cycle or at the beginning?
  //
  //      That is, instead of having it go at the end of cycle 0, we
  //      could have it be the very first thing at the beginning of
  //      cycle 1.  The result is equivalent (in theory) but it might
  //      make easier to do more complicated schedules.
  //
  //      If we scheduled the 'isDone' check at the beginning of every
  //      cycle then it is possible to implement a model that never
  //      steps.  While that may never be needed, it would be nice
  //      to have that capability.
  //
  //      Also note that for-loops and while-loops check their
  //      termination conditions before the first iteration and hence
  //      may never enter the loop.  So arguably that's the way that
  //      a model's step-loop should be implemented as well.

  // TBD: Should we schedule 'isDone' at order + 1 or should we use
  //      Integer.MAX_VALUE so we are sure it is always the very last
  //      thing to be done within a particular cycle?

  // TBD: Should we make the start time and interval for the 'isDone'
  //      action use different ivars than the 'step' action so that
  //      subclasses can control those parameters, e.g. perhaps only
  //      check isDone every 10 cycles or something?


  /**
   * TBD: Add doc
   *
   */
  protected sim.engine.Steppable createStopAction (AbstractISLModel m) {
    final AbstractISLModel model = m;
    return new sim.engine.Steppable() {
      public void step ( sim.engine.SimState state ) {
        if ( model.isDone(state) ) {
           log.info("{}: Processing complete, 'isDone' method returned true;"
                   + " -- calling 'stop' method", model.getName());
          model.stop();
        }
      }

      // TBD: Generate serialization ID using serialver tool
      // TBD: Add doc
      private static final long serialVersionUID = 1L;    
    };

  }

  // === [ State Change Methods ] =========================================

  /**
   * TBD: Add doc
   *
   */
  public void start () {

    buildObjects();
    buildActions();
    status = STARTED;
  }

  /**
   * TBD: Add doc
   *
   */
  public void stop () {
    log.info(this.getName()+".stop() - begin.");
    sim.engine.Stoppable action = null;

    if ( actions != null ) {
      for ( Object key : actions.keySet() ) {
        try {
          //log.debug( "{} stop: calling stop() on action '{}'",
          //           getName(), key );
          action = actions.get( key );
          action.stop();
        } catch ( RuntimeException re ) {
          if ( action == null ) {
            log.info( "{} stop: action for key '{}' is null"
                     + " -- ignoring action", getName(), key );
          } else {
            log.error( "Unable to stop action '" + key + "'"
                     + " -- Error: {}", re );
            // TBD: Should we re-throw the exception?
          }
        }
      }
      // TBD: Should we clear the actions Map?
      actions.clear();
    }

    // log.debug( "{} stop: all actions stopped; calling finish()",
    //            getName() );
    finish();
    // TBD: Should we reset cycle to BEFORE_START ? Or create another
    //      constant: AFTER_STOP ?
  }
  
  /**
   * TBD: Add doc
   *
   */
  public void finish () { status = FINISHED; }

  // Notes: Termination status
  // In SimState, 'finish' calls 'kill' and termination steps are
  // performed by 'kill'.  So if the user code calls 'kill' directly
  // status is set to KILLED and that's it.  If the user code calls
  // 'finish' (or 'stop') then 'kill' is called indirectly and the
  // status changes to KILLED.  However, then flow returns to 'finish'
  // where the status changes to STOPPED which is what we want.

  /**
   * TBD: Add doc
   *
   */
  public void kill () { status = KILLED; }


  /**
   * TBD: Add doc
   *
   */
  @Override
  public abstract void step ( sim.engine.SimState state );

  /**
   * TBD: Add doc
   *
   */
  public boolean isDone (sim.engine.SimState s) {
    boolean retVal = false;
    log.info("{} isDone() - getCycle() = {}, getCycleLimit() = {}", new Object[] {getName(), getCycle(), getCycleLimit()});
    if (getCycle() >= getCycleLimit()) retVal = true;
    return retVal;
  }
}
