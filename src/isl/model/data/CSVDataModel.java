/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package isl.model.data;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import sim.engine.*;
import sim.engine.Schedule;
import sim.engine.SimState;

import isl.model.*;
import bsg.util.MathUtils;
import bsg.util.CollectionUtils;
import isl.table.Table;


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
public class CSVDataModel extends AbstractISLModel {
  // TBD: Generate serialization ID using serialver tool
  // TBD: Add doc
  private static final long serialVersionUID = 1L;    

  // TBD: Add doc
  private static final Logger log = LoggerFactory.getLogger( CSVDataModel.class );
  // Future: private static final Logger log = LoggingUtils.getLogger();


  /**
   * TBD: Add doc
   * TBD: Move to utility class
   * Tolerance for time values when deciding if to interpolate...
   */
  // public static final double TIME_EPSILON = Float.MIN_VALUE;
  public static final double TIME_EPSILON = FLT_MIN; // FLT_EPSILON;

  /**
   * TBD: Add doc
   *
   */
  protected File dataSource = null;

  /**
   * TBD: Add doc
   * TBD: What to call this ivar? Just 'data' or 'table' or
   *      'drugData' or ???
   *
   */
  protected Table<Double> data;

  /**
   * TBD: Add doc
   *
   */
  protected String timeColumnKey = "time";

  /**
   * TBD: Add doc
   * Assuming here that the data Table does not change after loading
   * or, at the very least, that the column does not move/shift.
   */
  protected int timeColumnIndex = 0;

  // TBD: Perhaps get rid of the timeColumnIndex ivar and simply use
  //      the timeColumnKey once we add the 'getValueAt(row,columnKey)'
  //      method to Table, ie Table would handle the lookup

  /**
   * TBD: Add doc
   * Assuming here that the data Table does not change after loading
   * or, at the very least, that the columns does not move/shift.
   */
  protected int outputColumnIndex = 1;


  // === [ Constructors ] =================================================

  /**
   * TBD: Add doc
   *
   */
  public CSVDataModel ( isl.measurement.view.BatchControl p ) {
    super( p );
    //log.debug( "Constructor called with args: {}", new Object[]{seed} );
     log.info( "Constructor called with args: {}", p.getClass().getName());
    // HACK
    setDataSource( "data/jpet297-fig2-all.csv" );
  }


  // === [ Getters & Setters ] ============================================

  /**
   * TBD: Add doc
   *
   */
  public File getDataSource () {
    return dataSource;
  }

  /**
   * TBD: Add doc
   *
   */
  public void setDataSource ( File file ) {
    // TBD: Add validation, e.g. not null, file exists, file is readable, etc
    // TBD: Also we might not want the user to reset the data source
    //      after it has been set or after the data has been read/loaded
    //      so we might need a status/mode/phase flag ivar for deciding
    //      if the data source is resettable
    //      Perhaps create a dynamic wrapper/proxy factory that can
    //      create an object of any time that is, or flagged as, unmodifiable
    //      E.g.
    //         File dataSource = Factory.createUnmodifiable( dataSource );
    //         if ( dataSource instanceof Unmodifiable ) {
    //            throw new BlahBlahException( "Can not modify " + dataSource );
    //         }

    // TBD: Should we load the data as soon as the data source is set
    //      or wait until buildObjects runs or have the user explicitly
    //      call a load or setData method?

    dataSource = file;
  }

  /**
   * TBD: Add doc
   *
   */
  public void setDataSource ( String file ) {
    try {
      setDataSource( bsg.util.ClassUtils.getResourceFile(file) );
    } catch ( IOException ioe ) {
      String msg = "Invalid data source"
                 + " -- unable to find file '" + file + "' in CLASSPATH";
      String clsName = bsg.util.ClassUtils.class.getName();
      // throw new java.util.MissingResourceException( msg, clsName, file, ioe );
      msg += "\nCaused by: " + ioe;
      throw new java.util.MissingResourceException( msg, clsName, file );
    }
  }

  /**
   * TBD: Add doc
   *
   */
  public Table<Double> getData () {
  //public Table getData () {
    // TBD: possible check if data is null and throw exception
    //      (we'd probably want that to only occur after buildObjects
    //      has run, so we'd need a status/mode/phase flag ivar)
    return data;
  }

  /**
   * TBD: Add doc
   *
   */
  public int getCycleRow () {
    return (int)getCycle();
  }

  /**
   * TBD: Add doc
   *
   */
  public int getTimeColumnIndex () {
    return timeColumnIndex;
  }

  /**
   * TBD: Add doc
   *
   */
  public void setTimeColumnIndex ( int index ) {
    Table<Double> data_l  = getData();

    if ( data_l != null ) {
      data_l.validateColumnIndex( index );
    }

    timeColumnIndex = index;
    log.info( "Time column index set to {}", index );
  }

  // TBD: Do we need and/or want to allow user's to set the time column
  //      index using a key?  I was planning to call this from the
  //      buildObjects (or setData) method, but now I'm leaning towards
  //      loading and validating the data into a new Table and once validated,
  //      then (only then) would the 'data' ivar be set/updated.  Hence this
  //      method would not be useful unless it also takes the Table as
  //      an arg, ie it becomes a static, stateless convenience method.
  //
  // TBD: Maybe I should switch the code around... The user calls the
  //      setTimeColumnKey method and it updates the time column index
  //      (even if the key has not changed).  Or maybe I just need an
  //      'updateTimeColumnIndex' method that 'setData' can call?

  /**
   * TBD: Add doc
   *
   */
  public int getOutputColumnIndex () {
    return outputColumnIndex;
  }

  /**
   * TBD: Add doc
   *
   */
  public void setOutputColumnIndex ( int index ) {
    Table<Double> data_l  = getData();

    if ( data_l != null ) {
      data_l.validateColumnIndex( index );
    }

    outputColumnIndex = index;
    log.info( "Output column index set to {}", index );
  }

  // TBD: How should we handle index validation if the data has not been
  //      loaded yet?
  //      Should we throw an exception, e.g. NPE, or just ignore the call
  //      to the validation method?

  // TBD: Like other ivars, we might want the index variables to only be
  //      set _once_ and/or not changeable after model construction.
  //      (See discussion about creating a "phased" object in parent class)


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
  @Override
  public double getTime () {
    return getTime( getCycleRow() );
  }

  protected double getTime ( int row ) {
    // TBD: Should we simply cast the getTime() double to an int or
    //      instead use a rounding function in the Math library?
    Double time = getData().getValueAt( row, timeColumnIndex );
    // TBD: Add a generics/typed version of getValueAt to the Table
    //      interface and then the cast will not be necessary

    //log.debug( "Time value for cycle {} is: {}", row, time );
    return time;
  }

  // TBD: How to handle cycle > data rows?  Should we throw an
  //      IndexOutOfBoundsException? Or perhaps NoSuchElementException?
  //      Or perhaps another time of exception?  Or perhaps create
  //      new exception class which is more specific to the fact
  //      that we have no data, e.g. DataValuesExceededException or
  //      DataExhaustedException or ???
  //      Or should we see this as a time exception, e.g.
  //      InvalidTimeException, MaxTimeExceededException or ???
  //
  //      The advantage of creating our own exception is that
  //         (a) we can store information in the exception, such as
  //             the schedule time and perhaps reference to data or
  //             schedule object
  //         (b) the IOOBE and NSEE exceptions in essence reveal the
  //             implementation detail (ie using lists) were as an
  //             exception data exceed is not implementation specific
  //
  // TBD: Instead of throwing an exception when cycle > data rows,
  //      we could return null or NaN?  Is there an advantage to
  //      returning a value?  IOW, how is getTime used elsewhere
  //      and would it better for the calling code to receive null
  //      or NaN or an exception?  Is this really an exception-al
  //      situation (pardon the pun)?
  //

  // TBD: Create a sub-interface of Table for data that has a time
  //      column (i.e. time series data) and, with that interface,
  //      you would just call table.getTime(rowIndex).
  //
  //      NOTE: A time series table (ie 2D array) class exists in the
  //            JFreeChart package (naturally).  When I have some
  //            "free time" [ahem] I need to get to know their API
  //            better and figure out how to use it for our needs.
  //


  // === [ Data Output Methods ] ==========================================

  /**
   * TBD: Add doc
   *
   */
  @Override
  public List<String> getOutputNames () {
    return getData().getColumnNames();
  }

  // TBD: What should we return? List<?>, List<String> or List<Name>
  //      where Name is some "extension" of String?
  //      Or perhaps just an Iterator or ListIterator?

  // TBD: Wrapper the getColumnNames return value in an unmodifiable List
  //      object (if it isn't already).  Really this is a job for the
  //      Table object though.

  // TBD: Create a utility method for converting any Collection into a
  //      List<String> and then pass getColumnNames return value through
  //      the converter.
  //      Or maybe we should just require that the column keys/names/identifiers
  //      always be String objects?

  // TBD: The CollectionUtils could implement a "conversion" wrapper factory
  //      methods that at like the unmodifiable<Type> methods found in
  //      Java Collections class.  That is the factory method returns
  //      a particular type of collection that wrappers a "backing"
  //      collection of the same type.  In our "conversion" wrapper, we
  //      would use a Convertor object to 'convert' elements from the
  //      backing object to a different form upon requent (i.e. conversion
  //      occurs when the collection is accessed, not a one-time, static
  //      conversion).  The Convertor would need to support at least a
  //      one way conversion, ie from backing element to fronting element
  //      (read only access) or vice versa (write only access) or both
  //      ways, ie 2-way convesion (read-write access).
  //
  //      An obvious use of these conversion factory methods would be to
  //      turn a collection of any type T into a collection of String
  //      representations of T.


  // TBD: (Ask Glen, when I get the chance...)
  //      In the Swarm version of the CSV Data Model (CSVDatModel.m),
  //      the getOutputFractions method returns all but the time column
  //      for the current row from the data table.  The getOutputs
  //      method calls getOutputFractions and then _adds_ the time
  //      value back into the Map before returning it.
  //
  //      Questions:
  //         (a) Why not flip-flop the design, i.e. have getOutputs
  //             build the Map and then have getOutputFractions 
  //             call getOutputs and remove the time value.
  //         (b) Why skip/remove the time value at all?  Since the
  //             methods are returning a Map, the caller should be
  //             responsible for selecting which values to use.
  //             Alternatively, have the caller passing a list of
  //             keys to include and/or exclude and we can return
  //             the data as a list or array.
  //
  // TBD: Create a utility method (if it doesn't already exist) for
  //      converting Map + Key List ==> Value List.  Instead of an
  //      explicit Key List, it could be a Key Filter or Predicate
  //      as well.  And there might be cases where the user wants
  //      the return object to be a subMap (ala SortMap.subMap) instead
  //      of a list.

  // TBD: I would rather not use SuppressWarning but this is the only
  //      thing I can do at this point until I rewrite Table to be
  //      full generics-ized and that's will likely be several days
  //      of work at least.  Mostly there are a lot of design questions
  //      on how best to implement the table and whether/how to mesh
  //      our table with MASON's 2D grid/spaces + JFreeChart's tables
  //      (ie XY data series) + Swing table classes.  Anyway ...

  protected Map<String, ? extends Number> getOutputs ( int row ) {
    Table<Double> data_l = getData();

    if ( row < 0 || data_l.getRowCount() <= row ) {
      // TBD: Should we check both upper and lower bounds?  Or it this
      //      unnecessary?  Maybe we should just let the Table throw
      //      an IOOB exception?
      // TBD: Is it better for the caller to return an empty Map or null?
      return null;
    }

    return (Map<String,? extends Number>)data_l.getRowMap( row );
  }

  /**
   * TBD: Add doc
   *
   */
  @Override
  @SuppressWarnings("unchecked")
  public Map<String,Number> getOutputs () {
    return (Map<String, Number>) getOutputs( getCycleRow() - 1 );  // post-step data gathering
  }


  /**
   * TBD: Add doc
   *
   */
  public Map<String,Number> getOutputFractions () {
    Map<String,Number> values = getOutputs();
    values.remove( timeColumnKey );
    return values;
  }

  /**
   * TBD: Add doc
   *
   */
  // TBD: Is it better for the caller to return a Double object or a primitive?
  @Override
  public Double getOutputFraction () {
    Table<Double> data_l  = getData();
    int   row   = getCycleRow() - 1;  // post-step data gathering
    int   col   = getOutputColumnIndex();

    if ( row >= 0 ) {
      return data_l.getValueAt( row, col );
    }

    // TBD: Is it better for the caller to return null or NaN?
    return Double.NaN;
  }

  /**
   * TBD: Add doc
   *
   */
  @SuppressWarnings("unchecked")
  public Map<String,Number> getInterpolatedOutputs ( double time,
                                                     double tolerance )
  {
    Table<Double> data_l  = getData();
    int   row   = getCycleRow() - 1;  // post-step data gathering
    if (row < 0) row = 0; // clamp to the first
    if (row >= data_l.getRowCount()-1) row = data_l.getRowCount()-1; // clamp to the last
    double t    = getTime( row );
    int    N    = data_l.getRowCount();
    Map<String,Number>   values  = null;

    //log.debug( "Getting interpolated outputs for time {} with tolerance {};"
    //         + " cycle-1: {}, time for cycle {}: {}",
    //           new Object[]{time,tolerance,row,row,t} );

    //if ( time < t ) {
    //  log.error( "Specified time ({}) is before t-1 ({})", time, t );
      // TBD: Should we throw an exception?
      //      This case is not handled by the Swarm code, from what
      //      I can tell... Seems like it might be a bug
      // [gepr] I don't think we should throw an exception or do anything, actually
    //}

    // TBD: Validate data is monotonically increasing (and not duplicates)
    //      w.r.t. the time column

    // TBD: This for-loop search starts at _zero_ (0) whereas the Swarm
    //      version starts at 'dataNdx - 1' (previous cycle).  That seems
    //      to me to be in a bug in the Swarm version but likely one that
    //      is never exposed
    for ( row = 0 ; row < N ; row++ ) {
      t = getTime( row );
      if ( time < t ) {
        // log.debug( "Upper bound row for target time {} found"
        //          + " at row: {}, time value: {}", new Object[]{time,row,t} );
        break;
      }
    }
    // TBD: Replace for-loop search with a call to Collections.binarySearch
    //      method _after_ we add the 'getColumn( int )' method to Table

    // Case 1: time out of range
    // -------
    // Two sub-cases:
    //    (a) time < min time (ie time for first row, row 0)
    //    (b) time > max time (ie time for last row, row N-1)
    if ( row <= 0 || N <= row ) {
      row = ( row <= 0 ? 0 : N-1 );
      // TBD: Should this be a error, warning, info or debug?
      log.warn( "Specified time ({}) {} data time ({})",
                new Object[]{ time, (row == 0 ? "< min" : "> max"), t} );
      values = (Map<String, Number>) getOutputs( row );
      values.put( timeColumnKey, time );  // see assumptions below
      //log.debug( "Returning output values from {} row (ie row {})"
      //        + " of data with time value set to {} -- values: {}",
      //          new Object[]{ (row == 0 ? "first" : "last"), row,
      //                        time, values} );
      return values;
    }

    // Case 2: time approx same as time for row n
    // -------
    if ( MathUtils.equals(t,time,tolerance) ) {
      log.info( "Specified time ({}) is within +/- tolerance ({})"
              + " of data time ({}) at row {}",
                new Object[]{ time, tolerance, t, row} );
      values = (Map<String, Number>) getOutputs( row );
      values.put( timeColumnKey, time );  // see assumptions below
      //log.debug( "Returning output values from (within tolerance) row {}"
      //        + " with time value set to {} -- values: {}",
      //          new Object[]{ row, time, values} );
      return values;
    }

    // Case 3: time approx same as time for row n-1
    // -------
    // This is the same as Case 2 except we are checking if time
    // is close to the previous row in the data.
    //
    // TBD: The Swarm implementation does not handle this case,
    //      so I'm not sure if I should

    // Verify time < t (see for-loop)
    // ---------------
    // This should never happen (unless the code above is changed).
    // I should put this in an assert or validation method.
    if ( t < time ) {
      String msg = "Time value (" + t + ") for found row (" + row + ")"
                 + " should be >= target time (" + time + ")";
      log.error( msg );
      throw new IllegalStateException( msg );
      // TBD: What exception class should I use here?  Or should we
      //      throw an exception?  We could shift the row number,
      //      ie row++ and check that row < N and update t and...
    }
    // TBD: Add test that row > 0 and row <= N-1
    //      If row == 0, then Case 1 should have triggered.

    // Case 4: interpolate outputs
    // ------
    values = getInterpolatedRowMap( data_l, row-1, row, timeColumnKey, time );
    //log.debug( "Returning output values interpolated between"
    //         + " row {} and row {} using ratio derived from"
    //         + " the time column for time value {} -- values: {}",
    //           new Object[]{ row-1, row, time, values} );

    return values;
  }

  // TBD: I think there's possible bug in the Swarm implementation:
  //      the while loop searches for a time value near >= tmid
  //      by _moving_ the tmpDataNdx (local temporary data index).
  //      That's okay, but then if a row is not found (Case 1 above),
  //      or a row is found and the tmid is within a tolerance of
  //      the time for that row (Case 2 above), _then_ the Swarm
  //      code calls 'getOuputs' but it does NOT specify the temporary
  //      data index.  So that output returned could be for row n
  //      but the tmid time value actually matched row n+10.
  //      At least it looks that way to me just walking the code.
  //      When I have time, I'll try to test the Swarm code or
  //      email Glen.

  // TBD: The Swarm implementation checks if row time is within
  //      tolerance of the tmid and the implementation above checks
  //      flips it around, ie is tmid within the tolerance of the
  //      row time.
  //
  //      An example might explain better:
  //         row = 3, row time = 3.5, tmid = 3.4, tolerance = 0.1
  //         Swarm:
  //            (3.4 - 0.1) <= 3.5 && 3.5 <= (3.4 + 0.1)
  //         Above:
  //            (3.5 - 0.1) <= 3.4 && 3.4 <= (3.5 + 0.1)
  //
  //      The results should be the same is all cases, i.e. if
  //      tmid is within tolerance of row time then the row time
  //      is within tolerance of tmid.

  // TBD: Does the getInterpolatedOutputs method need to be strictfp?
  //      Do we need to worry about under/overflows and would the
  //      results be inconsistent?

  // TBD: The getInterpolatedOutputs method makes several assumptions:
  //         1. table column names used as keys in map,
  //         2. keys are unique and
  //         3. keys unchanged since table load
  //      Should we create "assert" statements to test some or all of
  //      these?

  // TBD: The getInterpolatedOutputs method assumes that the tolerance
  //      argument is very small.  Should we check/assert that this
  //      is indeed true?  Is it enough to check its size relative
  //      to the time argument or do we need to check the tolerance
  //      size relative to the time interval sizes in the data, i.e.
  //      w.r.t. data[r][0] - data[r-1][0] for all rows r >= 1 ?

  // TBD: We could re-implement the getInterpolatedOutputs method to
  //      use a Comparator (or Filter) when searching for a matching row.
  //      This would work well if we switch to using Collections
  //      binarySearch instead of our own for-loop.
  //
  //      The advantage of the comparator (or filter) is that it could
  //      handle the tolerance checking.  This simplifies our code a
  //      little because then there are only 3 cases: (a) row not found,
  //      (b) row within tolerance found and (c) "insertion row" found,
  //      i.e. row out of tolerance where insertion should occur.
  //
  //      Note: the assumption that the tolerance is "small" is especially
  //            important if we are using a comparator to do a binary
  //            search.

  /**
   * TBD: Add doc
   *
   */
  public Map<String,Number> getInterpolatedOutputs ( double time ) {
    return getInterpolatedOutputs( time, TIME_EPSILON );
  }

  /**
   * TBD: Add doc
   * TBD: Move to a Table utilities class
   *
   */
  protected Map<String,Number> getInterpolatedRowMap ( Table<Double> data_l,
                                                       int lowerRow,
                                                       int upperRow,
                                                       double ratio )
  {
    Map<String,Number> map = null;
    int    N      = data_l.getColumnCount();
    double lower  = Double.NaN;
    double upper  = Double.NaN;
    String key    = null;
    double value  = Double.NaN;

    //log.debug( "Interpolating values between rows {} and {} with ratio {}",
    //           new Object[]{lowerRow,upperRow,ratio} );

    // Using LinkedHashMap because it preserves the insertion order
    // and hence the order of entries will be the same as the order
    // of columns in the table
    map = new java.util.LinkedHashMap<String,Number>( N-1 );

    for ( int col = 0 ; col < N ; col++ ) {
      key = data_l.getColumnName( col );
      lower  = data_l.getValueAt( lowerRow, col );
      upper  = data_l.getValueAt( upperRow, col );
      value = lower + ratio * ( upper - lower );
      map.put( key, value );
      // log.debug( "For column '{}' ({}) -- row {} value: {}, row {} value: {},"
      //          + " interpolated value {} (ratio: {})",
      //           new Object[]{key,col,lowerRow,lower,upperRow,upper,value,ratio} );
    }

    //log.debug( "Interpolated values between rows {} and {} with ratio {} -- {}",
    //           new Object[]{lowerRow,upperRow,ratio,map} );
    return map;
  }

  /**
   * TBD: Add doc
   * TBD: Move to a Table utilities class
   *
   */
  protected Map<String,Number> getInterpolatedRowMap ( Table<Double> data_l,
                                                       int lowerRow,
                                                       int upperRow,
                                                       String key,
                                                       double value )
  {
    Map<String,Number> map = null;
    double lower  = Double.NaN;
    double upper  = Double.NaN;
    double ratio  = Double.NaN;
    int    col    = data_l.columnIndexOf( key );

    //log.debug( "Interpolating values between rows {} and {}"
    //         + " based on the '{}' column ({}) and value {}...",
    //           new Object[]{lowerRow,upperRow,key,col,value} );

    lower  = data_l.getValueAt( lowerRow, col );
    upper  = data_l.getValueAt( upperRow, col );

    ratio = ( value - lower ) / ( upper - lower );

    map = getInterpolatedRowMap(data_l, lowerRow, upperRow, ratio );
    if ( map.get(key).doubleValue() != value ) {
      log.warn( "Interpolated value for the {} column != expected value"
              + " -- expected value: {}, interpolated value: {};"
              + " possible calculation error",
                new Object[]{key,value,map.get(key)} );
      map.put( key, value );
    }

    // log.debug( "Interpolated values between rows {} and {}"
    //          + " based on the '{}' column ({}), value {} and ratio {} -- {}",
    //            new Object[]{lowerRow,upperRow,key,col,value,ratio,map} );
    return map;
  }

  // TBD: What names should I use for the column key and value for
  //      which the interpolation is based?
  //      Perhaps 'basisKey' and 'basisVal'?  Or 'xAxisKey'/'xAxisVal'?
  //      The formal term for x-axis is abscissa, but 'abscissaKey'
  //      and 'abscissaVal' is a bit to verbose.

  // TBD: Should the getInterpolatedRowMap method take the column key
  //      or the column index as its argument?
  //      It needs both (right now, see next TBD) but it should only
  //      take one of them since you can look up the index from the
  //      key and vice versa and having the user provide both could
  //      lead to mismatch errors.

  // TBD: Add support for getValueAt( int row, Object colKey ) to the
  //      Table interface.

  // TBD: Consider adding a column list or iterator argument to the
  //      getInterpolatedRowMap methods so that the caller can specify
  //      which columns to include in the map. This would be useful
  //      because we can skip the time column since the ratio is based
  //      on that column

  // TBD: Possibly add support for other types of interpolation, ie
  //      not just piece-wise linear.  The interpolation algorithms
  //      could be provided in classes that implement the Interpolator
  //      interface and then the getInterpolatedRowMap methods would
  //      be passed an Interpolator argument and, if null, then default
  //      to LinearInterpolator.


  // === [ Build Methods ] ================================================

  /**
   * TBD: Add doc
   *
   */
  @Override
  public void buildObjects () {
    File src = null;

    log.info( "Building model objects" );

    // TBD: See comments in parent class that discusses creating a
    //      "phased" model/agent class
    // validatePhase( SETUP );

    try {
      // TBD: Perhaps buildObjects should load a separate/new Table object
      //      and then, after successful load & validation, set the ivar
      //      (perhaps create a setData method too).  This prevents the
      //      data ivar from changing until we have a "good" object
      data = new isl.table.DefaultTable<Double>( null, null, Double.class );
      src = getDataSource();

      isl.table.CSVTableLoader.load( data, src );
      log.info( "Data loaded successfully from source '{}'", src.getClass().getName() );


// -- create setData method starting here 
      timeColumnIndex = data.columnIndexOf( timeColumnKey,
                                            CollectionUtils.CASE_INSENSITIVE );
      if ( timeColumnIndex < 0 ) {
        throw new isl.io.MissingDataException( src, timeColumnKey );
      }

      // HACK
      // For output functions which use Map objects we need to have
      // the exact key value for the time column, so let's update
      // the ivar to ensure that it matches:
      timeColumnKey = data.getColumnName( timeColumnIndex );

      // TBD: Of course the Table's colummn names could be changed,
      //      so there are some additional things we _should_ do
      //         * Create utility method for creating unmodifiable tables
      //           ala Collections
      //         * Wrapper data with an unmodifiable table that prevents
      //           changes to column names and column order and ...
      //         * The Table should have generic parameter for the column
      //           name/key/identifier and thereby we could declare that
      //           the table's column's are keyed with case insensitive
      //           strings
      //         * This would make the comparator for column names/keys
      //           part of the table class and that comparator would
      //           always be used
      //         * Modify the getRowMap method in Table to make sure
      //           that the Map returned also makes its keys case
      //           insensitive  (a "Row Map" is just a special case of
      //           a "Sub-Table", ie its a Table with 1 row... we should
      //           probably implement it like that, if possible, because
      //           that should simplify things)


      // TBD: Add data validation, e.g.
      //         * check that time column is at index 0
      //           (current hacks to format table assume time column
      //           is first; we could add code to actually move the
      //           table column if necessary [grin])
      //         * check that time column is monotonically increasing
      //           (with no duplicate values as well)
      //         * check that there is ONLY ONE time column in the table
      //           (currently 'columnIndexOf' stops on first match)
      //         * maybe check for null values (ie missing data)
      //         * maybe check that all rows are the same length
      //           (ie missing data or parsing error)
      //

      // TBD: Create Drug-To-Sucrose Map?  What is it used for???

      // NOTE: Using row count - 1 because the cycle count begins at 0.
      //       However, I am considering changing the scheduling such
      //       that 'isDone' is called at the beginning of each cycle
      //       instead of the end.  _If_ that change is made, then
      //       we'll probably need to change the cycle limit back to
      //       row count. 
      setCycleLimit( data.getRowCount() - 1 );
      // TBD: Get max time value and store it in ivar for the isDone
      //      method to use instead of row count

// -- end of setData method (or maybe include log.info below too)

      // HACK HACK HACK
      dataColFmts = new java.util.AbstractList<String> () {
        public int size () { return data.getColumnCount(); }
        public String get ( int index ) {
          return ( index == timeColumnIndex ? " %7.2f": " %13e" );
        }
      };

      log.info( "{} loaded: labels = {}", getName(), data.getColumnNames() );
      log.info( "data[{}][{}] =\n{}",
                new Object[]{ data.getRowCount(),
                              data.getColumnCount(),
                              formatRows(data,null,0,-1,dataColFmts) } );

    } catch ( IOException ioe ) {
      log.error( "Build objects failed loading data", ioe );
      throw new RuntimeException( "Load data failed", ioe );
    }
  }

  /**
   * DataModel is really just a table lookup
   */
  @Override
  public void buildActions() {
      // no-op
  }

  // === [ State Change Methods ] =========================================

  /**
   * DataModel need not be stepped.
   */
  @Override
  public void step ( SimState state ) {
    log.info("CSVDataModel.step() - begin.");
  }

  /**
   * TBD: Add doc
   *
   */
  @Override
  public boolean isDone( SimState state) {
     boolean retVal = false;
     double time = parent.getISL().mason2IslTime(state.schedule.getTime());
     double t    = getTime( data.getRowCount()-1 );
     if ( time > t) {
        log.info("CSVDataModel.isDone() - stick a fork in me.");
        retVal = true;
     }
     return retVal;
  }

  // === [ Convenience Utility Methods ] ==================================

  // TBD: Most, if not all, these methods should eventually be moved to
  //      utility classes or into other classes

  // HACK HACK HACK
  protected static List<String> dataColFmts = null;

  /**
   * TBD: Add doc
   *
   */
  // HACK
  // TBD: Should this method take an Appendable (ie Buffer) as I
  //      initially wrote it, or a Formatter?
  public static Formatter formatTable ( Table<Double> data_l,   Formatter fmt,
                                        int rowBegin, int colBegin,
                                        int rowEnd,   int colEnd,
                                        List<String> colFmts,
                                        boolean checkBounds )
  {
    int     N       = data_l.getRowCount();
    int     M       = data_l.getColumnCount();
    String  colFmt  = null;
    String  rowPfx  = "";
    String  rowSfx  = "%n";
    String  defaultFmt = "%s";

    //log.debug( "Formatting data_l from ({},{}) to ({},{}) with formatter",
    //           new Object[]{rowBegin,colBegin,rowEnd,colEnd} );
    // note: Formatter toString calls toString on its output destination
    //       and I figured that wouldn't be useful for logging

    if ( rowBegin < 0 ) rowBegin = N + rowBegin;
    if ( rowEnd   < 0 ) rowEnd   = N + rowEnd;
    if ( colBegin < 0 ) colBegin = M + colBegin;
    if ( colEnd   < 0 ) colEnd   = M + colEnd;

    if ( checkBounds ) {
      rowBegin = Math.max( rowBegin,   0 );
      rowEnd   = Math.min( rowEnd,   N-1 );
      colBegin = Math.max( colBegin,   0 );
      colEnd   = Math.min( colEnd,   M-1 );
    }

    // TBD: Should we check bounds or just let the getValueAt method
    //      handle that, ie it throws IndexOutOfBoundsExceptions?
    // TBD: Should we check if end < begin and, if so, then count
    //      backwards?  IOW, the increment value is a variable and
    //      could be + or -
    // TBD: We could support user specified increments for the row
    //      and column, but then we should just have them pass in
    //      a List<int> or Iterator<int> which is even more flexible.
    // TBD: Support row and/or table prefix and suffix strings?
    //      Or a flag incidating whether if row numbers should be
    //      included.
    // TBD: Create a Collection wrapper class that supports a built-in
    //      default so that instead of having the try-catch inside
    //      the for-loop below, it would be in the wrapper's get(int)
    //      method.  This is a capability that would probably be useful
    //      in several places and it cleans up the formatting code.
    //         colFmts = new DefaultingList<String>( colFmts, "%s" );

    if ( fmt == null ) {
      int n = Math.abs( (rowEnd - rowBegin + 1) * (colEnd - colBegin + 1) );
      fmt = new Formatter( new StringBuilder(n * 16) ); // column width ~ 15
      //log.debug( "Created Formatter with StringBuffer for {} values"
      //         + " (length: {})", n, (n * 16) );
    }

    for ( int row = rowBegin ; row <= rowEnd ; row++ ) {
      fmt.format( rowPfx, row );

      for ( int col = colBegin ; col <= colEnd ; col++ ) {
        try {
          colFmt = colFmts.get( col );
        } catch ( IndexOutOfBoundsException ioobe ) {
          log.warn( "No format defined for column {} -- {};"
                  + " using default format {}",
                    new Object[]{col,ioobe,defaultFmt} );
          colFmt = defaultFmt;
        }
        fmt.format(colFmt, data_l.getValueAt(row,col) );
      }

      if ( row < rowEnd ) fmt.format( rowSfx, row );

    }

    return fmt;
  }  // end of method formatTable(Table,Formatter,int,int,int,int,List,boolean)

  public static Formatter formatRows ( Table<Double> data_l, Formatter fmt,
                                       int rowBegin, int rowEnd,
                                       List<String> colFmts )
  {
    // TBD: Should this method be catching the IOOBE or just letting
    //      it propogate to the caller?
    //      I'm starting to lean towards removing the try-catch from
    //      this method and letting the caller worry about it...
    // try {
    //   return formatTable( fmt, rowBegin, 0, rowEnd, -1, true );
    // } catch ( IndexOutOfBoundsException ioobe ) {
    //   log.debug( "Unable to format rows {} - {} -- {}",
    //              new Object[]{rowBegin,rowEnd,ioobe} );
    // }
    // return fmt;
    return formatTable(data_l, fmt, rowBegin, 0, rowEnd, -1, colFmts, true );
  }

  // HACK
  public static Formatter formatRows ( Table<Double> data_l, Formatter fmt,
                                       int rowBegin, int rowEnd,
                                       String colFmt )
  {
    int N = data_l.getColumnCount();
    List<String> colFmts = java.util.Collections.nCopies( N, colFmt );
    return formatRows(data_l, fmt, rowBegin, rowEnd, colFmts );
  }

  public static String formatRow ( Table<Double> data_l, int row ) {
    Formatter fmt = formatRows(data_l, null, row, row, dataColFmts );
    // TBD: Check if fmt.toString() returns an empty string, if so
    //      the return the 'no data_l' default.
    // TBD: Should we return a default ('no data_l') string or just
    //      return null or an empty string instead and leave it up
    //      to the caller to decide what to do?
    //      Or perhaps allow the caller to provide the default string
    //      which will be echoed back if fmt is empty?
    if ( fmt == null ) {
      return "[ no data for row " + row + "]";
    }
    return fmt.toString();
  }

  // TBD: Add Formatter support to the Logging API
  // TBD: TableFormat.formatRow( getData(), getCycleRow() );

  // TBD: Create a TableFormat class (extension of Format or possibly
  //      MessageFormat) that can convert Table objects into strings
  //      Surprisingly, I haven't found any classes in Java or other
  //      popular 3rd party libraries that have List or Map formatters.
  //      I had create fairly complete versions of those classes back
  //      at AMS/IOT but don't have the code with me.[sigh]  It is
  //      such a basic thing, I'm shocked that there are lots of versions
  //      out there. Of course, Java's MessageFormat is very useful for
  //      formatting a variable size object because it assumes that
  //      you are going to pass a fixed number of a few objects and
  //      has no notion of specifying default formats or formats for
  //      a range/sublist of elements.
  //
  //      Anyway, I have a fairly good idea how I'll code the TableFormat
  //      but it will likely take a few days to complete (w/o testing)
  //      so it will have to wait for another time. [sigh]
  //


  // === [ Main Method (for testing only) ] ===============================

}  // end of CSVDataModel class
