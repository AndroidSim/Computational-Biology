/*
 * IPRL - CSV formatted Data Model
 *
 * Copyright 2003-2011 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

// TBD: Should I put the test classes in packages?  Should I use
//      the same package name as the test target class so that
//      the test class can access package-private information?
package isl.model.data;

import org.junit.*;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.engine.*;
//import sim.util.*;

import java.lang.reflect.*;


/**
 * Test class for the {@link CSVDataModel}.
 *
 * TBD: Add class description
 *
 * @future
 *    - 
 *
 * @author Ken Cline
 * @see CSVDataModel
 * @see SimState
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
// TBD: Should test classes subclass the test target class so that
//      they can access protected variables?
public class CSVDataModelTest
{

  // TBD: Add doc
  private static final Logger log = LoggerFactory.getLogger( CSVDataModelTest.class );


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


  /**
   * TBD: Move this to parent or utility tester class
   * @todo
   *    - Add optional args to specify the seeds
   *    - Move default seed list to constant, eg DEFAULT_SEEDS
   *
   */
  public void testSeedConstructors ( Class<?> cls ) {
    SimState model = null;
    Constructor cnstr = null;
    long [] seeds = { -1L, 0L, 1L, Long.MIN_VALUE, Long.MAX_VALUE };
    long r = 0L;

    try {
      cnstr = cls.getConstructor( long.class );
      for ( long s : seeds ) {
        model = (SimState)cnstr.newInstance( s );
// HACK: seed() method not provided < MASON 15
// TBD: Create mason15.jar and replace in islj/lib
        r = s; //model.seed();
        assertEquals( cnstr + " called with seed " + s, s, r );
      }
    } catch ( NoSuchMethodException nsme ) {
      fail( "Class " + cls + " has no 'seed' constructor\n" + nsme );
    } catch ( Exception e ) {
      fail( "Unable to create instance of " + cls
            + " using 'seed' constructor: " + cnstr + "\n" + e );
    }

  }

  /**
   *
   */
  @Test
  @Ignore( "Requires MASON 15" )
  public void testModelConstructors () {
    testSeedConstructors( CSVDataModel.class );
  }


  // Tests to do
  // -----------
  //    * Constructor Tests
  //       * Seed past to the contructor matches value returned by seed() method
  //       *
  //       *
  //    * buildObjects Tests
  //       *
  //       *
  //       *
  //    * buildActions Tests
  //       * Verify that expected actions (steppables) are defined on schedule
  //       *
  //       *
  //    * getTime Tests
  //       *
  //       *
  //       *
  //    * getLabels (aka getOutputNames) Tests
  //       *
  //       *
  //       *
  //    * getOutputFractions & getOutputFraction Tests
  //       *
  //       *
  //       *
  //    * geOutputs Tests
  //       *
  //       *
  //       *
  //    * getOutputsInterpolatedAt Tests
  //       *
  //       *
  //       *
  //    * load (aka loadData) Tests
  //       *
  //       *
  //       *
  //    *
  //       *
  //       *
  //       *
  //

}  // end of CSVDataModelTest class


// To run tests:
// -------------
//    $ javac -Xlint CSVDataModelTest.java
//    $ java org.junit.runner.JUnitCore isl.model.data.CSVDataModelTest
//

