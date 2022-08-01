/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package isl.model.ref;

import isl.model.AbstractISLModel;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sim.engine.SimState;


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
public class RefModel extends AbstractISLModel
{
  private static final long serialVersionUID = -3282075617674598148L;

  private static final Logger log = LoggerFactory.getLogger( RefModel.class );

  protected double timeStart  =  0.0F;
  protected double timeStop   = 100.0F;
  protected double timeStep   =  0.1F;

  protected double epsilon    =  FLT_MIN;

  // TBD: Should this be called outputEpsilon since we might have other
  //      epsilon variables, e.g. timeEpsilon?
  //
  // TBD: It seems that output epsilon should be FLT_EPSILON instead of
  //      FLT_MIN, but I'll have to see what the numbers look like and
  //      then come back to this.

  protected AbstractCDModel cdModel  =  null;
  protected java.util.Properties cdModelParameters  =  null;
  protected Map<String,Double> bolusContents  =  null;
  protected Double extractedOutputFraction = Double.NaN;

  // === [ Constructors ] =================================================

  public RefModel ( isl.measurement.view.BatchControl p ) {
    super( p );
    log.info( "Constructor called with args: {}", p.getClass().getName() );

    setCycleLimit( p.cycleLimit );
    // HACK HACK HACK
    // Hardcoding initialization data for now ...
    setTimeStart( 0.0 );  //  simulation time at which to start the CD ref model
    setTimeStop( 100.0 );
    setTimeStep( 0.1 );  // integration step for the ref model

    setEpsilon( 1.0e-24 );  // 0.000000000000000000000001D0

    try {
      java.util.Properties cdParams = new java.util.Properties();
      cdParams.load( new java.io.StringReader(
         "type = ExtendedCDModel" + "\n"
         + "k1 = 0.03"            + "\n"
         + "k2 = 0.01"            + "\n"
         + "ke = 0.1"             + "\n"
         + "dn = 0.265"           + "\n"
         + "t  = 6.35"            + "\n"
         + "m  = 1.0"             + "\n"
         + "q  = 0.312"           + "\n"
         + "a  = 0.00654"         + "\n"
         + "b  = 0.0248"          + "\n" ) );

      setCDModelParameters( cdParams );

    } catch ( IOException e ) {
      log.error( "Failed setting CD Model parameters -- Error: '{}'", e );
      throw new RuntimeException( "Failed setting CD Model parameters", e );
    }

    // TBD: As mentioned above I have some questions about bolus contents
    //      and how it is being using in the reference model.  It seems
    //      like we only need the keys from the map, ie for 'getOutputs'
    //      and yet we store the whole map.  Also there seems to be an
    //      implicit assumption about the order of the keys and yet the
    //      map data comes from a configuration file and hence the order
    //      could be anything right?
    //
    //      In any case, since the key order would seem to be important,
    //      I'm using a LinkedLinkedHashMap here because that preserves entry
    //      insertion order.

    Map<String,Double> bc = null;

    bc = new java.util.LinkedHashMap<String,Double>();
    bc.put( "Compound",    0.5 );
    bc.put( "Marker",      0.5 );
    bc.put( "Metabolite",  0.0 );

    setBolusContents( bc );

  }


  // === [ Getters & Setters ] ============================================

  // TBD: Create an interface and base implementation class for an object
  //      that is a time keeper, i.e. has a start, stop, step and possibly
  //      other time related variables (e.g. time units) and methods
  //      (e.g. getStepCount(), getTimeSince(t), getTimeRemaining(),
  //      getStepsRemaining() and so forth). I'm not sure what I would
  //      call this interface, perhaps "TimeKeeper", "TimeMgr", "Clock",
  //      "ModelTimer" or ???
  //
  //      A time keeper interface could be written to take a 'type variable'
  //      which would define whether we are measuring time with integers
  //      or floating point values (or possibly some other type of object
  //      e.g. Number subclass such as BigDecimal, or Date/Calendar objects
  //      or whatever).

  /**
   * TBD: Add doc
   *
   */
  @Override
  public long getCycle () {
    // TBD: See "Implementation Detail: Cycle count" discussion in the
    //      AbstractISLModel class
    return super.getCycle() + 1;
  }

  public double getTimeStart () {
    // TBD: Should we do any validation here?
    return timeStart;
  }

  public void setTimeStart ( double start ) {
    if ( start < 0.0F ) {
      throw new IllegalArgumentException ( "Start time (" + start + ") < 0.0F" );
    }
    //log.debug( "Setting start time to {}", start );
    timeStart  = start;
  }

  public double getTimeStop () {
    // TBD: Should we do any validation here?
    return timeStop;
  }

  public void setTimeStop ( double stop ) {
    if ( stop < timeStart ) {
      throw new IllegalArgumentException ( "Stop time (" + stop + ")"
                                         + " < start time (" + timeStart + ")" );
    }
    //log.debug( "Setting stop time to {}", stop );
    timeStop   = stop;
  }

  public double getTimeStep () {
    // TBD: Should we do any validation here?
    return timeStep;
  }

  public void setTimeStep ( double step ) {
    if ( step <= 0.0F ) {
      throw new IllegalArgumentException ( "Time step (" + step + ") <= 0.0F" );
    }
    //log.debug( "Setting time step to {}", step );
    timeStep   = step;
  }

  public void setTimeRange ( double start, double stop, double step ) {
    setTimeStart( start );
    setTimeStop(  stop  );
    setTimeStep(  step  );
  }

  // TBD: I'm not sure if I like the method name "setTimeRange"...
  //      I considered "setTimeSeries" and "setTimeInterval", but don't
  //      particularly like those either.  Maybe "setTimeParameters"?
  //      Or just "setTime"?


  @Override
  public double getTime () {
    return parent.getTime();
  }

  // TBD: See Design Discussion: Time below


  public double getEpsilon () {
    return epsilon;
  }

  public void setEpsilon ( double e ) {
    // TBD: Do we need to do any validation on e?  I think e > 0 is
    //      a requirement/expectation, but I'll need to look at this
    //      again after I have the RefModel running
    //log.debug( "Setting epsilon to {}", e );
    epsilon = e;
  }

  public AbstractCDModel getCDModel () {
    // TBD: Should we do any validation here?
    return cdModel;
  }

  public java.util.Properties getCDModelParameters () {
    // TBD: Should we do any validation here?
    return cdModelParameters;
  }

  public void setCDModelParameters ( java.util.Properties params ) {
    // TBD: What validation(s) should we do???
    cdModelParameters = params;
  }

  public Map<String,Double> getBolusContents () {
    // TBD: Should we do any validation here?
    return bolusContents;
  }

  public void setBolusContents ( Map<String,Double> bolus ) {
    // TBD: What validation(s) should we do???
    log.debug( "Setting bolus contents to {}", bolus );
    bolusContents = bolus;
  }


  // === [ Data Output Methods ] ==========================================

  @Override
  public Map<String,Number> getOutputs () {
    Iterator<String>    keys     = null;
    String              key      = null;
    Double              value    = null;

    if ( ! isStarted() ) {
      // TBD: Create an InvalidModelStateException class
      throw new IllegalStateException( "Model not started" );
    }

    outputs = new java.util.LinkedHashMap<String,Number>();
    outputs.put( "Time", getTime() );

    try {
      keys = getBolusContents().keySet().iterator();

      if ( keys.hasNext() ) {
        key = keys.next();
        value = getOutputFraction();
        outputs.put( key, value );
        //log.debug( "Adding output fraction ({}) under key '{}'",
        //           value, key );
      } else {
        log.info( "No key in bolus contents for output fraction"
                 + " -- value skipped in outputs map" );
      }

      if ( keys.hasNext() ) {
        key = keys.next();
        value = getExtractedOutputFraction();
        outputs.put( key, value );
        //log.debug( "Adding extracted output fraction ({}) under key '{}'",
        //           value, key );
      } else {
        log.info( "No key in bolus contents for extracted output fraction"
                 + " -- value skipped in outputs map" );
      }

    } catch ( RuntimeException re ) {
      log.error( "Failed creating outputs map for time " + getTime()
               + " -- Error: {};"
               + " ignoring error and returning outputs as is: {}",
                 re, outputs );
    }

    //log.debug( "Returning outputs: {}", outputs );
    return outputs;
  }

  public Double getExtractedOutputFraction () {
    return extractedOutputFraction;
  }

  protected void setExtractedOutputFraction ( Double value ) {
    // TBD: Does this method need to be protected or can it
    //      (should it be) public?
    // TBD: Should we accept any Number type or just Double?
    //      If we accept any Number subclass, then we probably
    //      should copy the value since the value object could
    //      be mutable.
    // TBD: Possible validation checks:
    //         (1) if model is not started, then throw exception ?
    //         (2) if model is stopped, then throw exception ?
    //         (3) data must not be null or empty ?
    //         (4) track time when extractedOutputFraction is set and
    //             throw exception if there is an attempt to change
    //             the extractedOutputFraction more than once during
    //             the same cycle ?
    //         (5) other
    extractedOutputFraction = value;
  }


  // === [ Build Methods ] ================================================

  @Override
  public void buildObjects () {
    String propname = null;
    Class<?> cls = null;
    java.util.Properties params = null;

    log.debug( "Building model objects" );

    // TBD: See comments in parent class that discusses creating a
    //      "phased" model/agent class
    // validatePhase( SETUP );

    try {
      setOutputNames( "outputFraction" );

      params = getCDModelParameters();

      // TBD: Should the model 'type' be part of the model parameters
      //      or should we make it an ivar for the RefModel class?
      //      I could see either way.  Hmmm.
      propname = params.get( "type" ).toString();

      cls = bsg.util.ClassUtils.findClass( this, propname );
      cdModel = (AbstractCDModel)bsg.util.ClassUtils.createInstance( cls );
      bsg.util.ObjectUtils.set( cdModel, params );

      // TBD: Is there any validation that I should do here?

    } catch ( ClassNotFoundException | NoSuchMethodException 
            | InstantiationException | IllegalAccessException 
            | InvocationTargetException e ) {
      log.error( "Build objects failed creating CD Model", e );
      throw new RuntimeException( "Unable to create CD Model", e );
    }

  }


  // === [ State Change Methods ] =========================================

  @Override
  public void step ( SimState state ) {
    double t = getTime();
    double z      = 7.0; // HACK

    setOutputFraction(          cdModel.ecd( z, t, false ) );
    setExtractedOutputFraction( cdModel.ecd( z, t, true  ) );

    log.info( "{} 'step' at time: {} (cycle: {})"
             + " -- t: {}, output fraction: {}, extracted: {}",
               new Object[]{name,getTime(),cycle,t,getOutputFraction(),
                            getExtractedOutputFraction()} );
    cycle++;
  }

  // TBD: See Design Discussion: Time below


  @Override
  public boolean isDone ( SimState state ) {
    double output = getOutputFraction();

    log.info( "{} checking 'isDone' at time: {} (cycle: {})"
             + " -- output fraction: {}, epsilon: {}, FLT_MIN: " + FLT_MIN,
               new Object[]{name,getTime(),getCycle(),output,epsilon} );

    // Check if output fraction is < epsilon, if so
    // then time to terminate...
    if ( epsilon < FLT_MIN && output < epsilon ) {
      log.info( "Processing complete -- epsilon ({}) < FLT_MIN ({})"
               + " && output fraction ({}) < epsilon; returning true",
                 new Object[]{epsilon,FLT_MIN,output} );
      return true;
    }

    // Otherwise, call the super isDone which returns true
    // if there is a cycle limit _and_ cycle count > limit.
    return super.isDone( state );
  }

}  // end of RefModel class
