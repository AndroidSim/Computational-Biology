/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

// TBD: Should I put this base class under 'isl' or 'isl.util' or
//      'isl.util.junit'?
package isl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.*;

import bsg.util.MathUtils;
import bsg.util.Complex;
import bsg.util.ComplexMath;


/**
 * Abstract Base class for Tests
 *
 * TBD: Add class description
 *
 * @future
 *    - 
 *
 * @author Ken Cline
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
// TBD: Should test classes subclass the test target class so that
//      they can access protected variables?
public class AbstractTest
{

  // TBD: Add doc
  private static final Logger log = LoggerFactory.getLogger( AbstractTest.class);


  /**
   * TBD: Add doc
   *
   */
  public static final String TEST_SEPARATOR
    = "----------------------------------------------------------------------";


  /**
   * TBD: Add doc
   *
   */
  protected Map<String,Object> stats = new java.util.LinkedHashMap<String,Object>();


  // HACK HACK HACK
  // Track test names in a separate set for now
  protected Set<String> tests = new java.util.TreeSet<String>();

  // TBD: I'm still think about how best to collect, store and output
  //      testing statistics so this is just a quick-n-dirty hack for
  //      now.  Before I do much coding on this I need to see what we
  //      are plan to do (or have done) for the main codebase, ie the
  //      ISLJ models because I might be able to reuse some of that
  //      code here.  I also need to see what sort of stuff already
  //      exist for JUnit and related packages, especially for doing
  //      benchmarking and performance testing.

  /**
   * TBD: Add doc
   *
   */
  protected boolean statsEnabled = false;


  /**
   * TBD: Add doc
   *
   */
  protected boolean suppressStatErrors = true;


  // === [ Getter and Setter Methods ] ====================================

  /**
   * TBD: Add doc
   *
   */
  public boolean isStatsEnabled () {
    return statsEnabled;
  }

  /**
   * TBD: Add doc
   *
   */
  public void setStatsEnabled( boolean state ) {
    statsEnabled = state;
  }


  /**
   * TBD: Add doc
   *
   */
  public boolean isSuppressStatErrors () {
    return suppressStatErrors;
  }

  /**
   * TBD: Add doc
   *
   */
  public void setSuppressStatErrors( boolean state ) {
    suppressStatErrors = state;
  }

  /**
   * TBD: Add doc
   *
   */
  public void clearStats () {
    if ( stats != null ) {
      stats.clear();
    }
  }

  // TBD: public void clearStats ( String test, String type, String stat ) {
  //          ...
  //      }


  // === [ Test Statistics Calculation Methods ] ==========================

  // TBD:
  //    * Should I pass a stats object around to all the test methods
  //      or should I use the logging MDC (or other global registry)?
  //
  //      Originally I started to add a stats parameter to all testing
  //      methods but then I realized that this type of change, i.e.
  //      add statistics meta data collection to tests seemed to me
  //      like a reasonable use of a global registry like MDC.  Using
  //      the registry allows us to easily add further enhancements,
  //      e.g. a dedicated Statistics measurement object instead of
  //      just a Map, at a later point and not worry about overhauling
  //      the entire API.  And I can now easily add statistics to
  //      each method as I have time and desire.
  //
  //      On the other hand, using a global registry does hide somewhat
  //      what is happening and makes the code harder to understand.
  //

  //      Update:
  //      ------
  //      After implementing the MDC approach, I discovered that --
  //      unlike Log4J -- SLF4J and Logback only allow String data
  //      to be stored in the MDC.  I search the web for some
  //      justification for this change, but I could not find any.
  //      It is very disappointing to see the capability of Log4j
  //      being thrown away in their new "better" product, e.g.
  //      properties file configuration, more flexible message
  //      formatting and now a more generalized MDC replaced with
  //      a something much more myopic.  I can think of 100s of
  //      reasons to keep the Log4j capability and almost none for
  //      throwing it away as they have done.  Grrr.


  //public static final String STATS_MDC_KEY = "stats";


  /**
   * TBD: Add doc
   *
   */
  //public T calculateDiff ( T a, T b ) {
  public Object calculateDiff ( Object a, Object b ) {
    if (a == null || b == null) throw new RuntimeException("Args cannot be null.");
    // log.debug( "Calculating the difference between a and b,"
    //          + " i.e. ({}) - ({})", toString(a), toString(b) );

    if ( a instanceof Complex ) {
      // TBD: Check that b is also Complex and if not...
      return ((Complex)a).subtract( (Complex)b );

      // TBD: Remove this code after I'm sure 'calculateDiff' is working
      //      propertly
      //Complex diff = ((Complex)a).subtract( (Complex)b );
      //log.debug( "Diff: a - b = {} - {} = {}",
      //           new Object[]{toString(a),toString(b),toString(diff)} );
      //return diff;
    }

    if ( a instanceof Number ) {
      // TBD: Check that b is also Number and if not...
      return ( ((Number)a).doubleValue() - ((Number)b).doubleValue() );
    }

    String msg = "Argument must be an instance of Number or Complex"
               + " -- Argument type: '" + a.getClass().getName() + "'";
    throw new IllegalArgumentException( msg );
  }


  /**
   * TBD: Add doc
   *
   */
  // public T calculateUlps ( T a, T b ) {
  public Object calculateUlps ( Object a, Object b ) {
    if (a == null || b == null) throw new RuntimeException("Args cannot be null.");
    // log.debug( "Calculating the ulps (units in last place) distance"
    //          + " between a ({}) and b ({})", toString(a), toString(b) );

    if ( a instanceof Complex ) {
      // TBD: Check that b is also Complex and if not...
      return ComplexMath.ulps( (Complex)a, (Complex)b );
    }

    if ( a instanceof Number ) {
      // TBD: Check that b is also Number and if not...
      return MathUtils.ulps( (Number)a, (Number)b );
    }

    String msg = "Argument must be an instance of Number or Complex"
               + " -- Argument type: '" + a.getClass().getName() + "'";
    throw new IllegalArgumentException( msg );
  }


  /**
   * TBD: Add doc
   *
   */
  //public T calculateSum ( T a, T b ) {
  public Object calculateSum ( Object a, Object b ) {
    if (a == null || b == null) throw new RuntimeException("Args cannot be null.");
    // log.debug( "Calculating the sum of a and b, i.e. ({}) + ({})",
    //            toString(a), toString(b) );

    if ( a instanceof Complex ) {
      // TBD: Check that b is also Complex and if not...
      return ((Complex)a).add( (Complex)b );
    }

    if ( a instanceof Number ) {
      // TBD: Check that b is also Number and if not...
      return ( ((Number)a).doubleValue() + ((Number)b).doubleValue() );
    }

    String msg = "Argument must be an instance of Number or Complex"
               + " -- Argument type: '" + a.getClass().getName() + "'";
    throw new IllegalArgumentException( msg );
  }


  // === [ Test Statistics Update Methods ] ===============================

  /**
   * TBD: Add doc
   *
   */
  // public boolean updateMax ( Map<String,T> stats, String key,
  //                            T value, String msg )
  public boolean updateMax ( Map<String,Object> stats,
                             String name, String ctx,
                             String type, Object value )
  {
    String key = null;
    Object max = null;
    Object str = null;

    tests.add( name );

    key = name + '.' + type + '.' + "max";
    max = stats.get( key + ".value" );
    str = stats.get( key + ".ctx" );

    if ( max == null || MathUtils.isGreaterThan(value,max) ) {
      // log.debug( "Replacing stat '{}' current value: '{}', ctx: '{}'"
      //          + " with value: '{}', ctx: '{}'",
      //            new Object[]{key,toString(name,type,"max",max),
      //                         str,toString(name,type,"max",value),ctx} );
      stats.put( key + ".value", value );
      stats.put( key + ".ctx",  ctx  );
      return true;
    }

    // log.debug( "Not replacing stat '{}' current max value: '{}', ctx: '{}'"
    //          + " is greater than new value: '{}', ctx: '{}'",
    //            new Object[]{key,toString(name,type,"max",max),
    //                         str,toString(name,type,"max",value),ctx} );
    return false;
  }

  /**
   * TBD: Add doc
   *
   */
  // public boolean updateMin ( Map<String,T> stats, String key,
  //                            T value, String msg )
  public boolean updateMin ( Map<String,Object> stats,
                             String name, String ctx,
                             String type, Object value )
  {
    String key = null;
    Object min = null;
    Object str = null;

    tests.add( name );

    key = name + '.' + type + '.' + "min";
    min = stats.get( key + ".value" );
    str = stats.get( key + ".ctx" );

    if ( min == null || MathUtils.isLessThan(value,min) ) {
      // log.debug( "Replacing stat '{}' current value: '{}', ctx: '{}'"
      //          + " with value: '{}', ctx: '{}'",
      //            new Object[]{key,toString(name,type,"min",min),
      //                         str,toString(name,type,"min",value),ctx} );
      stats.put( key + ".value", value );
      stats.put( key + ".ctx",  ctx  );
      return true;
    }

    // log.debug( "Not replacing stat '{}' current min value: '{}', ctx: '{}'"
    //          + " is less than new value: '{}', ctx: '{}'",
    //            new Object[]{key,toString(name,type,"min",min),
    //                         str,toString(name,type,"min",value),ctx} );
    return false;
  }


  /**
   * TBD: Add doc
   *
   */
  // public boolean updateSum ( Map<String,T> stats, String key,
  //                            T value, String msg )
  public boolean updateSum ( Map<String,Object> stats,
                             String name, String ctx,
                             String type, Object value )
  {
    String key = null;
    Object sum = null;
    Object cnt = null;

    tests.add( name );

    key = name + '.' + type + '.' + "sum";
    sum = stats.get( key + ".value" );
    cnt = stats.get( key + ".count" );

    // Object prevSum = sum;
    // Object prevCnt = cnt;

    sum = ( sum == null ? value : calculateSum(value,sum) );
    cnt = ( cnt == null ? 1 : ((Integer)cnt) + 1 );

    // log.debug( "Replacing stat '{}' current value: '{}', count: '{}'"
    //          + " with value: '{}', count: '{}'",
    //            new Object[]{key,toString(name,type,"sum",prevSum),
    //                         prevCnt,toString(name,type,"sum",sum),cnt} );
    stats.put( key + ".value", sum );
    stats.put( key + ".count", cnt );
    return true;

  }


  /**
   * TBD: Add doc
   *
   */
  public Object calculateAvg ( Map<String,Object> stats,
                               String name, String type )
  {
    String key = null;
    Object sum = null;
    Object cnt = null;
    Object avg = null;

    key = name + '.' + type + '.' + "sum";
    sum = stats.get( key + ".value" );
    cnt = stats.get( key + ".count" );
    if (sum == null || cnt == null) throw new RuntimeException("sum nor cnt can be null.");

    if ( sum instanceof Complex ) {
      avg = ((Complex)sum).divide( (Integer)cnt );
      //log.debug( "Test: '{}', stat: '{}', sum ({}), count ({}) -- average: {}",
      //  new Object[]{name,type,toString(sum),cnt,toString(avg)} );
      return avg;
    }

    if ( sum instanceof Number ) {
      avg = ( ((Number)sum).doubleValue() / ((Number)cnt).doubleValue() );
      //log.debug( "Test: '{}', stat: '{}', sum ({}), count ({}) -- average: {}",
      //  new Object[]{name,type,toString(sum),cnt,toString(avg)} );
      return avg;
    }

    String msg = "Argument must be an instance of Number or Complex"
               + " -- Argument type: '" + sum.getClass().getName() + "'";
    throw new IllegalArgumentException( msg );
  }


  /**
   * TBD: Add doc
   *
   */
  public Object calculateMean ( Map<String,Object> stats,
                                String name, String type )
  {
    String key  = null;
    Object min  = null;
    Object max  = null;
    Object mean = null;

    key = name + '.' + type;
    min = stats.get( key + ".min.value" );
    max = stats.get( key + ".max.value" );
    if (min == null || max == null) throw new RuntimeException("min nor max can be null.");

    if ( MathUtils.isGreaterThan(min,max) ) {
      String msg = "Invalid State"
                 + " -- minimum (" + toString(min) + ")"
                 +  " > maximum (" + toString(max) + ")"
                 + " for test '" + name + "' and stat '" + type + "'";
      throw new IllegalStateException( msg );
    }

    if ( min instanceof Complex ) {
      Complex a = (Complex)min;
      Complex b = (Complex)max;
      mean = a.add( b.subtract(a).divide(2) );
      //log.debug( "Test: '{}', stat: '{}', min ({}), max ({}) -- mean: {}",
      //  new Object[]{name,type,toString(min),toString(max),toString(mean)} );
      return mean;
    }

    if ( min instanceof Number ) {
      double a = ((Number)min).doubleValue();
      double b = ((Number)max).doubleValue();
      mean = a + (b - a) / 2.0;
      //log.debug( "Test: '{}', stat: '{}', min ({}), max ({}) -- mean: {}",
      //  new Object[]{name,type,toString(min),toString(max),toString(mean)} );
      return mean;
    }

    String msg = "Argument must be an instance of Number or Complex"
               + " -- Argument type: '" + min.getClass().getName() + "'";
    throw new IllegalArgumentException( msg );
  }


  /**
   * TBD: Add doc
   *
   * @param name         test name used as a top-level (root) key for stats
   * @param ctx          test conditions used to document which test a
   *                     particular stat relates to, e.g. which test had
   *                     the max value, the min value and etc.
   * @param expected     the expected value for the current test run used to
   *                     update stats and also possibly to document stat
   *                     values
   * @param actual       the actual value for the current test run used to
   *                     update stats and also possibly to document stat
   *                     values
   */
  public void updateStats ( String name, String ctx,
                            Object expected, Object actual )
  {
    Object diff = null;
    Object ulps = null;

    try {
      if ( ! isStatsEnabled() ) {
        log.debug( "Stats disabled -- No updating performed" );
        return;
      }

      tests.add( name );

      diff = calculateDiff( expected, actual );
      ulps = calculateUlps( expected, actual );

      updateMax( stats, name, ctx, "diff", diff );
      updateMin( stats, name, ctx, "diff", diff );
      updateSum( stats, name, ctx, "diff", diff );

      updateMax( stats, name, ctx, "ulps", ulps );
      updateMin( stats, name, ctx, "ulps", ulps );
      updateSum( stats, name, ctx, "ulps", ulps );

    } catch ( RuntimeException re ) {
      if ( isSuppressStatErrors() ) {
        log.error( "Failed to update test statistics -- Error: ", re );
      } else {
        throw re;
      }
    }

  }


  /**
   * TBD: Add doc
   *
   * @param name         test name used as a top-level (root) key for stats
   * @param ctx          general description of test conditions (i.e. context)
   *                     stored and then reported with other stats by listStats
   *                     and related methods
   */
  public void setTestDescription ( String name, Object ctx ) {
    String key = name;

    try {
      //if ( ! isStatsEnabled() ) {
      //  log.debug( "Stats disabled -- ignoring call set test description" );
      //  return;
      //}
      // Decided to the test description to be set even while stats is disabled

      tests.add( name );

      stats.put( key + ".msg", ctx );

    } catch ( RuntimeException re ) {
      if ( isSuppressStatErrors() ) {
        log.error( "Failed to update test statistics -- Error: ", re );
      } else {
        throw re;
      }
    }

  }


  public Object getTestDescription ( String name ) {
    String key = name;
    return stats.get( key + ".msg" );
  }


  // === [ Test Statistics Output Methods ] ===============================

  /**
   * TBD: Add doc
   *
   */
  protected String toString ( String name, String type,
                              String stat, Object value )
  {
    if ( value instanceof Double ) {
      return String.format( "%15.5g", value );
    }

    if ( value instanceof Complex ) {
      return String.format( "%#15.5s", value );
    }

    return String.valueOf( value );
  }

  /**
   * TBD: Add doc
   *
   */
  protected String toString ( Object value ) {
    return toString( null, null, null, value );
  }


  /**
   * TBD: Add doc
   *
   */
  public String formatStat ( String name, String type,
                             String stat, String pfx )
  {
    String key    = null;
    Object value  = null;
    String str    = null;

    if ( "avg".equalsIgnoreCase(stat) ) {
      value = calculateAvg( stats, name, type );
      key = name + '.' + type + '.' + "sum";
      str = "sum: "   + toString( stats.get( key + ".value" ) )
          + ", count: " + stats.get( key + ".count" );
    } else {
      if ( "mean".equalsIgnoreCase(stat) ) {
        value = calculateMean( stats, name, type );
      } else {
        key = name + '.' + type + '.' + stat;
        value = stats.get( key + ".value" );
        str = "context: '" + stats.get( key + ".ctx" ) + "'";
      }
    }

    value = toString( name, type, stat, value );

    str = pfx + stat + ": " + value
        + ( str == null ? "" : "\t [ " + str + " ]" );

    //log.debug( "For stat '{}.{}.{}', value: '{}' string: '{}'",
    //           new Object[]{name,type,stat,value,str} );
    return str;
  }


  /**
   * TBD: Add doc
   *
   */
  public String listStats ( Set<String> names ) {
    String [] types     = { "diff", "ulps" };
    String [] stats     = { "min", "max", "avg", "mean" };
    List<String>  list  = new java.util.ArrayList<String>();
    Object    ctx       = null;
    String    str       = "";
    String    pfx       = "\t";
    String    sfx       = "\n";

    if ( names == null ) {
      names = tests;
    }

    for ( String name : names ) {
      list.add( name );

      ctx = getTestDescription( name );
      if ( ctx != null ) {
        list.add( pfx + "description: " + ctx );
      }

      for ( String type : types ) {
        list.add( pfx + type );
        for ( String stat : stats ) {
          list.add( formatStat( name, type, stat, pfx + pfx ) );
        }
      }

    }

    for ( String s : list ) {
      str += s + sfx;
    }

    return str;
  }

  /**
   * TBD: Add doc
   *
   */
  public String listStats ( String name ) {
    return listStats( (name == null ? null : Collections.singleton(name)) );
  }


  /**
   * TBD: Add doc
   *
   */
  // public void logStats ( Map<String,T> stats ) {
  // public void logStats ( Map<String,Object> stats ) {
  public void logStats () {

    //stats = (Map<String,Object>)MDC.get( STATS_MDC_KEY );
    //if ( stats == null ) {
    //  log.info( "No stats Map found in MDC -- Stats updating disabled" );
    //  return;
    //}

    if ( ! isStatsEnabled() ) {
      log.info( "Stats disabled -- No stats logged." );
      return;
    }

    log.info( "\nStats:\n------\n" + listStats((Set<String>)null) );

  }


  // === [ Set-up & Tear-down Methods ] ===================================

  /**
   *
   */
  @Before
  public void setUp () {
    // Per test setup
  }

  /**
   *
   */
  @After
  public void tearDown () {
    // Per test tearDown
  }


  // === [ Tests for ??? function ] =======================================


  // Tests to do
  // -----------
  //    *
  //       *
  //       *
  //       *
  //


  // === [ Misc Testing Utility Methods ] =================================


  public static void printMinMaxValues () {
    System.out.println ( "Byte.MIN_VALUE:    " + Byte.MIN_VALUE );
    System.out.println ( "Byte.MAX_VALUE:    " + Byte.MAX_VALUE );
    System.out.println ( "Short.MIN_VALUE:   " + Short.MIN_VALUE );
    System.out.println ( "Short.MAX_VALUE:   " + Short.MAX_VALUE );
    System.out.println ( "Integer.MIN_VALUE: " + Integer.MIN_VALUE );
    System.out.println ( "Integer.MAX_VALUE: " + Integer.MAX_VALUE );
    System.out.println ( "Long.MIN_VALUE:    " + Long.MIN_VALUE );
    System.out.println ( "Long.MAX_VALUE:    " + Long.MAX_VALUE );
    System.out.println ( "Float.MIN_VALUE:   " + Float.MIN_VALUE );
    System.out.println ( "Float.MAX_VALUE:   " + Float.MAX_VALUE );
    System.out.println ( "Double.MIN_VALUE:  " + Double.MIN_VALUE );
    System.out.println ( "Double.MAX_VALUE:  " + Double.MAX_VALUE );
  }

  public static void main ( String [] args ) {
    printMinMaxValues();
  }


}  // end of AbstractTest class


// To compile tests:
// -----------------
//    $ ( cd ~/tdi/dev/islj/test/isl/ ; \
//                                  javac -Xlint AbstractTest.java )
//

// To run tests:
// -------------
//    $ java org.junit.runner.JUnitCore isl.model.ref.AbstractTest
//

