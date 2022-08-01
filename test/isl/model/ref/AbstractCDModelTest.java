/*
 * Copyright 2003-2019 - Regents of the University of California, San
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
package isl.model.ref;

import static java.lang.Math.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.*;
import bsg.test.junit.Assert.*;
import bsg.test.junit.AssertTest.*;
import isl.AbstractTest;

//import isl.model.*;
import bsg.util.MathUtils;
import bsg.util.Complex;
import bsg.util.ComplexMath;
import bsg.util.LoggingUtils;


/**
 * Test class for the {@link AbstractCDModel}.
 *
 * TBD: Add class description
 *
 * @future
 *    - 
 *
 * @author Ken Cline
 * @see AbstractTest
 * @see AbstractCDModel
 * @see SimState
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
// TBD: Should test classes subclass the test target class so that
//      they can access protected variables?
public class AbstractCDModelTest extends AbstractTest
{

  // TBD: Add doc
  private static final Logger log = LoggerFactory.getLogger( AbstractCDModelTest.class);


  // === [ JNI/C++ Method Definitions ] ===================================

  // JNI to C++ AbstractCDModel library in Swarm/ObjC ISL model
  public native void setCppLogLevel  ( String level );
  public native long createCppCDM ( AbstractCDModel cd );
  public native void deleteCppCDM ( long ptr );
  public native double ecd ( long ptr, double z, double t );

  // Conventional CD Model Functions
  public native double integrand ( long ptr, double z, double u, double t );
  public native double bessel ( String func, double x );

  // Complex Math Functions
  public native Complex complexMath ( String func, Complex z );

  // Extended CD Model Functions
  public native Complex l_ecd ( long ptr, Complex s );
  public native Complex g ( long ptr, Complex s );

  // LD_LIBRARY_PATH must include location of libreflivermodel.so
  static {
    System.loadLibrary( "reflivermodel" );
  }


  // === [ Set-up & Tear-down Methods ] ===================================

  /**
   *
   */
  @Before
  @Override
  public void setUp () {
    // Per test setup
  }

  /**
   *
   */
  @After
  @Override
  public void tearDown () {
    // Per test tearDown
  }


  // === [ Tests for ecd function ] =======================================

  /**
   * TBD: Add doc
   *
   * The acceptable relative error (tolerance) between the C++ (expected)
   * and Java (actual) values returned by the 'ecd' method.
   * The tolerance value is defined in ULPs (units in the last place,
   * aka units of least precision).
   *
   */
  public static final int ECD_ULPS = 5;


  /**
   * TBD: Add doc
   * TBD: Move this to parent tester class (parent of both Conventional
   *      and Extended CD Models)
   *
   */
  public void testECD ( String name, AbstractCDModel javaCDM,
                        double z, double t )
  {
    long     cppCDMPtr  = 0L;
    double   cppCfp     = 0.0;
    double   javaCfp    = 0.0;
    String   test       = null;

    setCppLogLevel( LoggingUtils.getLevelName(log) );

    javaCfp = javaCDM.ecd( z, t );

    cppCDMPtr = createCppCDM( javaCDM );
    cppCfp = ecd( cppCDMPtr, z, t );
    deleteCppCDM( cppCDMPtr );

    try {
      test = "Flux concentration (ecd) for z =" + z + ", t = " + t
           + " ( extracting = " + javaCDM.isExtracting() + " )";
      updateStats( name, test, cppCfp, javaCfp );
      bsg.test.junit.Assert.assertEquals( test, cppCfp, javaCfp, ECD_ULPS );
    } catch ( Throwable e ) {
      log.error( e.getMessage()
               + " [ difference: " + ( cppCfp - javaCfp )
               + " = " + MathUtils.ulps(cppCfp,javaCfp) + " ulps ]"
               /* +   " -- test: '" + test + "'" */ );
    }

  }

  /**
   * TBD: Add doc
   *
   */
  //@Test
  public void testConventionalECD () {
    String name       = "ConventionalECD";
    ConventionalCDModel javaCDM = new ConventionalCDModel();
    double timeStart  =  7.0;
    double timeStop   =  8.0;  // 60.0;
    double timeStep   =  0.1;
    double z = javaCDM.getL(); // HACK

    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
      testECD( name, javaCDM, z, t );
      log.info( TEST_SEPARATOR );
    }

  }

  /**
   * TBD: Add doc
   *
   */
  //@Test
  public void testExtendedECD () {
    String name       = "ExtendedECD";
    AbstractCDModel javaCDM = new ExtendedCDModel();
    double timeStart  =  7.0;
    double timeStop   =  7.3;  // 8.0;  // 60.0;
    double timeStep   =  0.1;
    double z = (new ConventionalCDModel()).getL(); // HACK

    clearStats();

    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
    //for ( double t = timeStart ; t < timeStop ; t += timeStep ) {

      setStatsEnabled( (t > 0) );

      // non-extracting
      javaCDM.setExtracting( false );
      testECD( name, javaCDM, z, t );

      // extracting
      javaCDM.setExtracting( true  );
      testECD( name, javaCDM, z, t );
      log.info( TEST_SEPARATOR );
    }

    log.info( TEST_SEPARATOR );
    logStats();
    System.out.println(  "\nStats:\n------\n" + listStats(name) );

    // @see "Reference Model output from the Swarm/ObjC ISL model" (below)
  }


  // === [ Tests for bessel function ] ====================================

  /**
   * TBD: Add doc
   *
   * The acceptable relative error (tolerance) between the C++ (expected)
   * and Java (actual) values returned by the Bessel function(s), i.e. 'j0'.
   * The tolerance value is defined in ULPs (units in the last place,
   * aka units of least precision).
   *
   */
  public static final int BESSEL_ULPS = 1 * 1000 * 1000; // INTEGRAND_ULPS;


  /**
   * TBD: Add doc
   *
   */
  public void testBessel ( String name, double x ) {
    double  javaJ0  = 0.0;
    double  cppJ0   = 0.0;
    String  test    = null;

    setCppLogLevel( LoggingUtils.getLevelName(log) );

    javaJ0 = cern.jet.math.Bessel.j0( x );
    cppJ0 = bessel( "j0", x );

    try {
      test = "Bessel j0( " + x + " )";
      bsg.test.junit.Assert.assertEquals( test, cppJ0, javaJ0, BESSEL_ULPS );
    } catch ( Throwable e ) {
      log.error( e.getMessage()
               + " [ difference: " + ( cppJ0 - javaJ0 )
               + " = " + MathUtils.ulps(cppJ0,javaJ0) + " ulps ]"
               /* +   " -- test: '" + test + "'" */ );
    }

  }

  /**
   *
   */
  //@Test
  public void testBessel () {
    String name  = "Bessel";
    ConventionalCDModel cdm = new ConventionalCDModel();
    double timeStart  =  7.0;
    double timeStop   =  7.2;  // 60.0;
    double timeStep   =  0.1;
    double delta      = 0.01;
    double du         = 0.0005;
    double z   = cdm.getL(); // HACK
    double k1  = cdm.getK1(); // HACK
    double k2  = cdm.getK2(); // HACK
    double x   = 0.0;

    /*
    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
      for ( double u = (t+du) ; u < (t+delta) ; u += du ) {
        //x = 2 * sqrt( k2 * k1 * ( (u*u) - (t*u) ) );
        x = 2 * sqrt( k2 * k2 * ( (u*u) - (t*u) ) );
        testBessel( name, x );
      }
    }
    */

    for ( x = 0.0 ; x < 10.0 ; x += 1.0 ) {
      testBessel( name, x );
      log.info( TEST_SEPARATOR );
    }

  }


  // === [ Tests for integrand function (Conventional CD) ] ===============

  /**
   * TBD: Add doc
   *
   * The acceptable relative error (tolerance) between the C++ (expected)
   * and Java (actual) values returned by the 'integrand' method.
   * The tolerance value is defined in ULPs (units in the last place,
   * aka units of least precision).
   *
   */
  public static final int INTEGRAND_ULPS = ECD_ULPS;


  /**
   * TBD: Add doc
   *
   */
  public double javaIntegrandWithCppBessel ( ConventionalCDModel cdm,
                                             double z, double u, double t )
  {
    double numer    = 0.0;
    double denom    = 0.0;
    double besselx  = 0.0;
    double bessel   = 0.0;
    double integr   = 0.0;

    if ( cdm == null ) {
      cdm = new ConventionalCDModel();
    }

    double k1  = cdm.getK1(); // HACK
    double k2  = cdm.getK2(); // HACK
    double ke  = cdm.getKe(); // HACK
    double Dn  = cdm.getDn(); // HACK
    double v   = cdm.getV();  // HACK

    // TBD: Validate z, u and t -- checks: not NaN, range for z ?
    //      range for u ? range for t ?

    //numer = exp( u*(k2 + ke - k1) - ((z*z) + pow(u*v,2)) / (4*Dn*u*v*z) );
    numer = exp( u*(k2 + ke + k1) - ((z*z) + pow(u*v,2)) / (4*Dn*u*v*z) );
    denom = sqrt( Dn * PI * pow(u,3) );
    //besselx = 2 * sqrt( k2 * k1 * ( (u*u) - (t*u) ) );
    besselx = 2 * sqrt( k2 * k2 * ( (u*u) - (t*u) ) );
    //bessel = j0( besselx );
    bessel = bessel( "j0", besselx );
    integr = numer * bessel / denom;

    log.info( "Integrand for z = {}, u = {}, t = {}: {}"
             + " ( = {} * {} / {}, where bessel = j0({}) )",
               new Object[]{z,u,t,integr,numer,bessel,denom,besselx} );
    return integr;
  }


  /**
   * TBD: Add doc
   *
   */
  public void testIntegrand ( String name, double z, double u, double t ) {
    ConventionalCDModel  javaCDM  = null;
    long     cppCDMPtr         = 0L;
    double   cppIntegrand      = 0.0;
    double   javaIntegrand     = 0.0;
    double   hybridIntegrand   = 0.0;
    String   test              = null;

    setCppLogLevel( LoggingUtils.getLevelName(log) );

    javaCDM = new ConventionalCDModel();
    javaIntegrand = javaCDM.integrand( z, u, t );

    cppCDMPtr = createCppCDM( javaCDM );
    cppIntegrand = integrand( cppCDMPtr, z, u, t );
    deleteCppCDM( cppCDMPtr );

    try {
      test = "ECD integrand for z =" + z + ", u = " + u + ", t = " + t;
      bsg.test.junit.Assert.assertEquals( test, cppIntegrand, javaIntegrand, INTEGRAND_ULPS );
    } catch ( Throwable e ) {
      log.error( e.getMessage()
               + " [ difference: " + ( cppIntegrand - javaIntegrand )
               + " = " + MathUtils.ulps(cppIntegrand,javaIntegrand) + " ulps ]"
               /* +   " -- test: '" + test + "'" */ );
    }

    hybridIntegrand = javaIntegrandWithCppBessel( javaCDM, z, u, t );

    try {
      test = "ECD (hybrid) integrand for z =" + z + ", u = " + u + ", t = " + t;
      bsg.test.junit.Assert.assertEquals( test, cppIntegrand, hybridIntegrand, INTEGRAND_ULPS );
    } catch ( Throwable e ) {
      log.error( e.getMessage()
               + " [ difference: " + ( cppIntegrand - hybridIntegrand )
               + " = " + MathUtils.ulps(cppIntegrand,hybridIntegrand) + " ulps ]"
               /* +   " -- test: '" + test + "'" */ );
    }

  }

  /**
   *
   */
  //@Test
  public void testIntegrand () {
    String name       = "Integrand";
    double timeStart  =  7.0;
    double timeStop   =  7.2;  // 60.0;
    double timeStep   =  0.1;
    double delta      = 0.01;
    double du         = 0.0005;
    double z = (new ConventionalCDModel()).getL(); // HACK

    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
      for ( double u = (t+du) ; u < (t+delta) ; u += du ) {
        testIntegrand( name, z, u, t );
        log.info( TEST_SEPARATOR );
      }
    }

  }


  // === [ Tests for Complex mathematical operations: sqrt, exp ] =========

  /**
   * TBD: Add doc
   *
   * The acceptable relative error (tolerance) between the C++ (expected)
   * and Java (actual) values returned by the Complex mathematical
   * operations, e.g. 'sqrt', 'exp and etc.
   * The tolerance value is defined in ULPs (units in the last place,
   * aka units of least precision).
   *
   */
  public static final int COMPLEX_MATH_ULPS = 5;


  /**
   * TBD: Add doc
   *
   */
  public void testComplexSqrt ( String name, Complex z ) {
    Complex  javaSqrt  = null;
    Complex  cppSqrt   = null;
    String   test      = null;

    setCppLogLevel( LoggingUtils.getLevelName(log) );

    javaSqrt = ComplexMath.sqrt( z );
    cppSqrt  = complexMath( "sqrt", z );

    try {
      test = "Complex sqrt( " + toString(z) + " )";
      updateStats( name, test, cppSqrt, javaSqrt );
      bsg.test.junit.Assert.assertEquals( test, cppSqrt, javaSqrt, COMPLEX_MATH_ULPS );
    } catch ( Throwable e ) {
      log.error( e.getMessage()
               + " [ difference: " + cppSqrt.subtract(javaSqrt)
               +   " = " + ComplexMath.distance(cppSqrt,javaSqrt)
               +   " = " + ComplexMath.ulps(cppSqrt,javaSqrt) + " ulps ]"
               /* +   " -- test: '" + test + "'" */ );
    }

  }

  /**
   *
   */
  //@Test
  public void testComplexSqrt () {
    String   name   = "complex.sqrt";
    double   min    = -10.0;  // -2.0;
    double   max    = +10.0;  // +2.0;
    double   step   =   1.0;
    Complex  z      = null;
    int i = 0;

    clearStats();
    setStatsEnabled( true );
    setTestDescription( name, "complex number z = a+bi,"
                        + " where a in [ " + min + ", " + max + " ]"
                        +   " and b in [ " + min + ", " + max + " ]"
                        + ", step size: " + step );

    for ( double real = min ; real <= max ; real += step ) {
      for ( double imag = min ; imag <= max ; imag += step ) {
        z = new Complex( real, imag );
        testComplexSqrt( name, z );
        //log.debug( TEST_SEPARATOR );
        i++;
        //if ( i >= 2 ) break; // System.exit( 0 );
      }
    }

    log.info( TEST_SEPARATOR );
    logStats();

  }


  /**
   *
   */
  //@Test
  public void testComplexSqrt2 () {
    Complex ONE       = Complex.ONE;
    Complex FOUR      = Complex.FOUR;
    String name       = "complex.sqrt";
    double timeStart  =  7.0;
    double timeStop   =  7.1;  // 8.0;  // 60.0;
    double timeStep   =  0.1;
    double inf        = 10.0;
    double ds         =  0.01;
    double gamma      =  0.0;
    Complex dw        = new Complex( 0, ds );
    ExtendedCDModel javaCDM = null;
    double Dn         = 0.0;
    double T         = 0.0;
    Complex term1     = null;
    int i = 0;

    clearStats();
    //setStatsEnabled( true );
    setTestDescription( name, "complex number s = a+bi,"
                        + " where a = 1/t and b in [ " + -inf + ", " + inf + " ]"
                        + ", t in [ " + 0.0 + ", " + (timeStop - timeStart) + " ]"
                        + ", step size for t: " + timeStep
                        + " and, for each t, step size for s: " + dw );

    javaCDM = new ExtendedCDModel();
    Dn = javaCDM.getDn();
    T  = javaCDM.getT();

    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
    //for ( double t = timeStart ; t < timeStop ; t += timeStep ) {
      gamma = 1 / t;
      i = 0;

      setStatsEnabled( (t > 0) );

      for ( Complex s = new Complex(gamma,-inf) ; s.imag() < inf ; s = s.add(dw) ) {
        // non-extracting
        javaCDM.setExtracting( false );
        term1 = ONE.add( FOUR.multiply( Dn*T ).multiply( s.add( javaCDM.g(s) ) ) );
        testComplexSqrt( name, term1 );

        // extracting
        javaCDM.setExtracting( true  );
        term1 = ONE.add( FOUR.multiply( Dn*T ).multiply( s.add( javaCDM.g(s) ) ) );
        testComplexSqrt( name, term1 );

        log.info( TEST_SEPARATOR );
        i++;
        //if ( i >= 2 ) break; // System.exit( 0 );
      }
    }

    log.info( TEST_SEPARATOR );
    logStats();

  }


  /**
   * TBD: Add doc
   *
   */
  public void testComplexExp ( String name, Complex z ) {
    Complex  javaExp  = null;
    Complex  cppExp   = null;
    String   test      = null;

    setCppLogLevel( LoggingUtils.getLevelName(log) );

    javaExp = ComplexMath.exp( z );
    cppExp  = complexMath( "exp", z );

    try {
      test = "Complex exp( " + toString(z) + " )";
      updateStats( name, test, cppExp, javaExp );
      bsg.test.junit.Assert.assertEquals( test, cppExp, javaExp, COMPLEX_MATH_ULPS );
    } catch ( Throwable e ) {
      log.error( e.getMessage()
               + " [ difference: " + cppExp.subtract(javaExp)
               +   " = " + ComplexMath.distance(cppExp,javaExp)
               +   " = " + ComplexMath.ulps(cppExp,javaExp) + " ulps ]"
               /* +   " -- test: '" + test + "'" */ );
    }

  }

  /**
   *
   */
  //@Test
  public void testComplexExp () {
    String   name   = "complex.exp";
    double   min    = -10.0;  // -2.0;
    double   max    = +10.0;  // +2.0;
    double   step   =   1.0;
    Complex  z      = null;
    int i = 0;

    clearStats();
    setStatsEnabled( true );
    setTestDescription( name, "complex number z = a+bi,"
                        + " where a in [ " + min + ", " + max + " ]"
                        +   " and b in [ " + min + ", " + max + " ]"
                        + ", step size: " + step );

    for ( double real = min ; real <= max ; real += step ) {
      for ( double imag = min ; imag <= max ; imag += step ) {
        z = new Complex( real, imag );
        testComplexExp( name, z );
        //log.debug( TEST_SEPARATOR );
        i++;
        //if ( i >= 2 ) break; // System.exit( 0 );
      }
    }

    log.info( TEST_SEPARATOR );
    logStats();

  }


  /**
   *
   */
  //@Test
  public void testComplexExp2 () {
    Complex ONE       = Complex.ONE;
    Complex FOUR      = Complex.FOUR;
    String name       = "complex.exp";
    double timeStart  =  7.0;
    double timeStop   =  7.1;  // 8.0;  // 60.0;
    double timeStep   =  0.1;
    double inf        = 10.0;
    double ds         =  0.01;
    double gamma      =  0.0;
    Complex dw        = new Complex( 0, ds );
    ExtendedCDModel javaCDM = null;
    double Dn         = 0.0;
    double T          = 0.0;
    Complex term1     = null;
    Complex term2     = null;
    int i = 0;

    clearStats();
    //setStatsEnabled( true );
    setTestDescription( name, "complex number s = a+bi,"
                        + " where a = 1/t and b in [ " + -inf + ", " + inf + " ]"
                        + ", t in [ " + 0.0 + ", " + (timeStop - timeStart) + " ]"
                        + ", step size for t: " + timeStep
                        + " and, for each t, step size for s: " + dw );

    javaCDM = new ExtendedCDModel();
    Dn = javaCDM.getDn();
    T  = javaCDM.getT();

    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
    //for ( double t = timeStart ; t < timeStop ; t += timeStep ) {
      gamma = 1 / t;
      i = 0;

      setStatsEnabled( (t > 0) );

      for ( Complex s = new Complex(gamma,-inf) ; s.imag() < inf ; s = s.add(dw) ) {
        // non-extracting
        javaCDM.setExtracting( false );
        term1 = ONE.add( FOUR.multiply( Dn*T ).multiply( s.add( javaCDM.g(s) ) ) );
        term2 = ONE.subtract( ComplexMath.sqrt( term1 ) ).divide( 2*Dn );
        testComplexExp( name, term2 );

        // extracting
        javaCDM.setExtracting( true  );
        term1 = ONE.add( FOUR.multiply( Dn*T ).multiply( s.add( javaCDM.g(s) ) ) );
        term2 = ONE.subtract( ComplexMath.sqrt( term1 ) ).divide( 2*Dn );
        testComplexExp( name, term2 );

        log.debug( TEST_SEPARATOR );
        i++;
        //if ( i >= 2 ) break; // System.exit( 0 );
      }
    }

    log.info( TEST_SEPARATOR );
    logStats();

  }


  // === [ Tests for l_ecd function (Extended CD) ] =======================

  /**
   * TBD: Add doc
   *
   * The acceptable relative error (tolerance) between the C++ (expected)
   * and Java (actual) values returned by the 'l_ecd'/'laplaceECD' method.
   * The tolerance value is defined in ULPs (units in the last place,
   * aka units of least precision).
   *
   */
  public static final int LAPLACE_ECD_ULPS = ECD_ULPS;

  /**
   * TBD: Add doc
   *
   * Flags to indicate which complex math functions to switch from the
   * Java implementation to the C++ implementation when doing the "hybrid"
   * calculation...
   *
   */
  public static final int NONE = 0;
  public static final int SQRT = 1;  // 2^0
  public static final int EXP  = 2;  // 2^1


  /**
   * TBD: Add doc
   *
   */
  public Complex javaLaplaceECDWithCppComplexMath ( ExtendedCDModel cdm,
                                                    Complex s, int cppFuncs )
  {
    Complex  ONE    = Complex.ONE;
    Complex  FOUR   = Complex.FOUR;
    double   Dn     = 0.0;
    double   T      = 0.0;
    double   M      = 0.0;
    double   Q      = 0.0;
    Complex  z      = null;
    Complex  term1  = null;
    Complex  term2  = null;
    Complex  Cout   = null;

    // TBD: Validate s -- checks: not NaN, range for s ?
    Dn = cdm.getDn();
    T  = cdm.getT();
    M  = cdm.getM();
    Q  = cdm.getQ();

    term1 = ONE.add( FOUR.multiply( Dn*T ).multiply( s.add( cdm.g(s) ) ) );

    z = ( (cppFuncs & SQRT) != 0 ? complexMath( "sqrt",term1 )
                                 : ComplexMath.sqrt(   term1 ) );
    term2 = ONE.subtract( z ).divide( 2*Dn );

    z = ( (cppFuncs & EXP ) != 0 ? complexMath( "exp", term2 )
                                 : ComplexMath.exp(    term2 ) );
    Cout = new Complex( M/Q ).multiply( z );

    log.info( "Laplace domain flux concentration, Cout(s), for s = {}: {}"
             + " ( {} sqrt term: {}, {} exp term: {} )",
               new Object[]{ toString(s), toString(Cout),
                       ((cppFuncs & SQRT) != 0 ? "C++":"Java"), toString(term1),
                       ((cppFuncs & EXP ) != 0 ? "C++":"Java"), toString(term2)
                           } );
    return Cout;
  }


  /**
   * TBD: Add doc
   *
   */
  public void testLaplaceECD ( String name, double t, Complex s,
                               boolean extracting, int cppFuncs )
  {
    ExtendedCDModel  javaCDM     = null;
    long             cppCDMPtr   = 0L;
    Complex          cppCout     = null;
    Complex          javaCout    = null;
    //Complex          hybridCout  = null;
    String           test        = null;

    setCppLogLevel( LoggingUtils.getLevelName(log) );

    javaCDM = new ExtendedCDModel();
    javaCDM.setExtracting( extracting );

    if ( cppFuncs == NONE ) {
      javaCout = javaCDM.laplaceECD( s );
    } else {
      //name += ".hybrid";
      javaCout = javaLaplaceECDWithCppComplexMath( javaCDM, s, cppFuncs );
    }

    cppCDMPtr = createCppCDM( javaCDM );
    cppCout = l_ecd( cppCDMPtr, s );
    deleteCppCDM( cppCDMPtr );

    try {
      test = "At t = " + t
           + ", Laplace ECD " + ( cppFuncs != NONE ? "(hybrid)" : "" )
           + " for s = " + s
           + " ( extracting = " + extracting + " )";
      updateStats( name, test, cppCout, javaCout );
      bsg.test.junit.Assert.assertEquals( test, cppCout, javaCout, LAPLACE_ECD_ULPS );
    } catch ( Throwable e ) {
      //if ( e.getMessage() == null ) {
      //  e.printStackTrace();
      //  System.exit( -1 );
      //}
      log.error( e.getMessage()
               + " [ difference: " + cppCout.subtract(javaCout)
               +   " = " + ComplexMath.distance(cppCout,javaCout)
               +   " = " + ComplexMath.ulps(cppCout,javaCout) + " ulps ]"
               /* +   " -- test: '" + test + "'" */ );
    }

    // hybridCout = javaLaplaceECDWithCppComplexMath( javaCDM, s, SQRT );
    // hybridCout = javaLaplaceECDWithCppComplexMath( javaCDM, s, EXP  );
    // hybridCout = javaLaplaceECDWithCppComplexMath( javaCDM, s, SQRT + EXP );
    // 
    // try {
    //   name += ".hybrid";
    //   test = "At t = " + t + ", Laplace ECD (hybrid) for s = " + s
    //        + " ( extracting = " + extracting + " )";
    //   updatesStats( name, test, cppCout, hybridCout );
    //   assertEquals( test, cppCout, hybridCout, LAPLACE_ECD_ULPS );
    // } catch ( Throwable e ) {
    //   log.error( e.getMessage()
    //            + " [ difference: " + cppCout.subtract(hybridCout)
    //            +   " = " + ComplexMath.distance(cppCout,hybridCout)
    //            +   " = " + ComplexMath.ulps(cppCout,hybridCout) + " ulps ]"
    //            /* +   " -- test: '" + test + "'" */ );
    // }

  }

  /**
   *
   */
  //@Test
  public void testLaplaceECD () {
    String name       = "LaplaceECD";
    String sfx        = "";
    double timeStart  =  7.0;
    double timeStop   =  7.2;  // 8.0;  // 60.0;
    double timeStep   =  0.1;
    double inf        = 10.0;
    double ds         =  0.01;
    double gamma      =  0.0;
    Complex dw        = new Complex( 0, ds );
    int [] cppFuncs   = { NONE };  //, SQRT, EXP, SQRT + EXP };  // { NONE }; 
    int i = 0;

    clearStats();

    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
    //for ( double t = timeStart ; t < timeStop ; t += timeStep ) {
      gamma = 1 / t;
      i = 0;

      setStatsEnabled( (t > 0) );

      for ( Complex s = new Complex(gamma,-inf) ; s.imag() < inf ; s = s.add(dw) ) {

        for ( int flag : cppFuncs ) {
          switch ( flag ) {
            case NONE        : sfx = "";                  break;
            case SQRT        : sfx = ".hybrid.sqrt";      break;
            case EXP         : sfx = ".hybrid.exp";       break;
            case SQRT + EXP  : sfx = ".hybrid.sqrt+exp";  break;
            default          : sfx = ".?";                break;
          }

          testLaplaceECD( name + sfx, t, s, false, flag );  // non-extracting
          testLaplaceECD( name + sfx, t, s, true,  flag );  // extracting
          log.info( TEST_SEPARATOR );

          i++;
          //if ( i >= 2 ) break; // System.exit( 0 );
        }

      }
    }

    log.info( TEST_SEPARATOR );
    logStats();
    //System.out.println(  "\nStats:\n------\n" + listStats(name) );

  }


  // === [ Tests for g(s) function (Extended CD) ] ========================

  /**
   * TBD: Add doc
   *
   * The acceptable relative error (tolerance) between the C++ (expected)
   * and Java (actual) values returned by the 'g' method.
   * The tolerance value is defined in ULPs (units in the last place,
   * aka units of least precision).
   *
   */
  public static final int G_ULPS = ECD_ULPS;


  /**
   * TBD: Add doc
   *
   */
  public void testG ( String name, double t, Complex s ) {
    ExtendedCDModel  javaCDM    = null;
    long             cppCDMPtr  = 0L;
    Complex          cppGs      = null;
    Complex          javaGs     = null;
    String           test       = null;

    setCppLogLevel( LoggingUtils.getLevelName(log) );

    // The C++ g(s) method is only invoked when the CD model is in extracting
    // mode so, for comparability, the Java Extended CD model needs to be
    // in extracting mode as well.
    javaCDM = new ExtendedCDModel();
    javaCDM.setExtracting( true );
    javaGs = javaCDM.g( s );

    cppCDMPtr = createCppCDM( javaCDM );
    cppGs = g( cppCDMPtr, s );
    deleteCppCDM( cppCDMPtr );

    try {
      test = "At t = " + t + ", g(s) for s = " + s;
      updateStats( name, test, cppGs, javaGs );
      bsg.test.junit.Assert.assertEquals( test, cppGs, javaGs, G_ULPS );
    } catch ( Throwable e ) {
      log.error( "" + e.getMessage()
               + " [ difference: " + cppGs.subtract(javaGs)
               +   " = " + ComplexMath.distance(cppGs,javaGs)
               +   " = " + ComplexMath.ulps(cppGs,javaGs) + " ulps ]"
               /* +   " -- test: '" + test + "'" */ );
    }

  }

  /**
   *
   */
  //@Test
  public void testG () {
    String name       = "g(s)";
    double timeStart  =  7.0;
    double timeStop   =  7.2; // 8.0;  // 60.0;
    double timeStep   =  0.1;
    double inf        = 10.0;
    double ds         =  0.01;
    double gamma      =  0.0;
    Complex dw        = new Complex( 0, ds );
    int i = 0;

    clearStats();

    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
    //for ( double t = timeStart ; t < timeStop ; t += timeStep ) {
      gamma = 1 / t;
      i = 0;

      setStatsEnabled( (t > 0) );

      for ( Complex s = new Complex(gamma,-inf) ; s.imag() < inf ; s = s.add(dw) ) {
        testG( name, t, s );
        log.info( TEST_SEPARATOR );
        i++;
        //if ( i >= 2 ) break; // System.exit( 0 );
      }

    }

    log.info( TEST_SEPARATOR );
    logStats();
    //System.out.println(  "\nStats:\n------\n" + listStats(name) );

  }


  // === [ Tests for ??? function ] =======================================


  // Tests to do
  // -----------
  //    *
  //       *
  //       *
  //       *
  //


  //public static void main ( String [] args ) {
  //}


}  // end of AbstractCDModelTest class


// To compile tests:
// -----------------
//    $ ( cd ~/tdi/dev/islj/src/isl/model/ref ;  \
//                                      javac -Xlint *CDModel.java )
//    $ ( cd ~/tdi/dev/islj/test/isl/model/ref ; \
//                                  javac -Xlint AbstractCDModelTest.java )
//
//    $ ( cd ~/tdi/dev/islj/test/isl/model/ref ; \
//                           javah -jni isl.model.ref.AbstractCDModelTest \
//                    && mv -i *.h ~/tdi/dev/islj/test/isl/model/ref/refModel/ )
//
//    $ ( cd ~/tdi/dev/islj/test/isl/model/ref/refModel ;  \
//                                                    make -f Makefile.ecd lib )
//
//    $ ( cd ~/tdi/dev/islj/test/isl/model/ref/refModel ;  \
//                                              export LD_LIBRARY_PATH=`pwd`:. )
//

// To run tests:
// -------------
//    $ java org.junit.runner.JUnitCore isl.model.ref.AbstractCDModelTest
//


// Reference Model output from the Swarm/ObjC ISL model
// ----------------------------------------------------
//    * RefModel.m, 'step' method, line 177:
//       [Telem monitorOut: 1 printf: "%s:  %f   %e   %e\n",
//          [self getName],
//          [self getTime],
//          [self getOutputFraction],
//          [self getExtractedOutputFraction]
//       ]; 
//    * ExperAgent.m, 'buildModel' method, line 117:
//       refModel = [[[RefModel create: self] setName: "ECDModel"]
//                                             setParent: self];
//    * Example output:
//       $ grep -n 'ECDModel' ~/tdi/dev/isl/src/monitors/monitor-liver1.scm | more
//       2554:ECDModel:  7.000000   nan   nan
//       2556:ECDModel:  7.100000   -8.639570e-07   -8.557874e-07
//       2738:ECDModel:  7.200000   -1.270221e-05   -1.273882e-05
//       2740:ECDModel:  7.300000   -7.528388e-05   -7.453012e-05
//       2742:ECDModel:  7.400000   -1.119406e-04   -1.093014e-04
//       2744:ECDModel:  7.500000   4.365417e-04   4.327849e-04
//       2746:ECDModel:  7.600000   2.822935e-03   2.775118e-03
//       2926:ECDModel:  7.700000   8.826400e-03   8.642971e-03
//       2928:ECDModel:  7.800000   2.020900e-02   1.972673e-02
//       2930:ECDModel:  7.900000   3.805563e-02   3.703860e-02
//       2932:ECDModel:  8.000000   6.235484e-02   6.051292e-02
//       2934:ECDModel:  8.100000   9.203898e-02   8.905914e-02
//       3116:ECDModel:  8.200000   1.254237e-01   1.210025e-01
//       3118:ECDModel:  8.300000   1.607671e-01   1.546344e-01
//       3120:ECDModel:  8.400000   1.966491e-01   1.885788e-01
//       3122:ECDModel:  8.500000   2.320451e-01   2.218576e-01
//       3124:ECDModel:  8.600000   2.661885e-01   2.537485e-01
//       3304:ECDModel:  8.700000   2.984200e-01   2.836357e-01
//       3306:ECDModel:  8.800000   3.281642e-01   3.109864e-01
//       3308:ECDModel:  8.900000   3.550188e-01   3.354385e-01
//       3310:ECDModel:  9.000000   3.788374e-01   3.568794e-01
//       3312:ECDModel:  9.100000   3.997044e-01   3.754194e-01
//       3494:ECDModel:  9.200000   4.178066e-01   3.912638e-01
//       3496:ECDModel:  9.300000   4.332987e-01   4.045814e-01
//       3498:ECDModel:  9.400000   4.462607e-01   4.154644e-01
//       3500:ECDModel:  9.500000   4.567584e-01   4.239899e-01
//       3502:ECDModel:  9.600000   4.649331e-01   4.303076e-01
//       3682:ECDModel:  9.700000   4.710287e-01   4.346655e-01
//       3684:ECDModel:  9.800000   4.753298e-01   4.373488e-01
//       3686:ECDModel:  9.900000   4.780683e-01   4.385872e-01


// Tests Completed:
// ================
//    * Complex function: sqrt, part I
//      ----------------
//      DESCRIPTION: complex number z = a+bi,
//                   where a in [ -10.0, 10.0 ] and b in [ -10.0, 10.0 ],
//                   step size: 1.0
//      RESULTS: Stats:
//        diff
//          min: (0.0000,0.0000)     [ context: 'Complex sqrt( (-10.000,-10.000) )' ]
//          max: (2.2204e-16,4.4409e-16)  [ context: 'Complex sqrt( (-9.0000,-9.0000) )' ]
//          avg: (-6.9232e-18,0.0000)       [ sum: (-3.0531e-15,0.0000), count: 441 ]
//          mean: (1.1102e-16,2.2204e-16)
//        ulps
//          min: (0.0000,0.0000)     [ context: 'Complex sqrt( (-10.000,-10.000) )' ]
//          max: (2.0000,1.0000)     [ context: 'Complex sqrt( (-6.0000,-5.0000) )' ]
//          avg: (0.095238,0.12245)  [ sum: (42.000,54.000), count: 441 ]
//          mean: (1.0000,0.50000)
//      TIME: 2.894
//


//    * Complex function: sqrt, part II
//      ----------------
//      DESCRIPTION: complex number s = a+bi,
//                   where a = 1/t and b in [ -10.0, 10.0 ],
//                   t in [ 0.0, 1.0 ], step size for t: 0.1
//                   and, for each t, step size for s: 0 + 0.01i
//      RESULTS: Stats:
//        diff
//          min: (0.0000,0.0000)     [ context: 'Complex sqrt( (68.354,-67.310) )' ]
//          max: (-1.7764e-15,-1.7764e-15)   [ context: 'Complex sqrt( (34.699,-58.156) )' ]
//          avg: (-1.7200e-18,-9.0249e-19)   [ sum: (-6.8834e-14,-3.6118e-14), count: 40020 ]
//          mean: (-8.8818e-16,-8.8818e-16)
//        ulps
//          min: (0.0000,0.0000)     [ context: 'Complex sqrt( (68.354,-67.310) )' ]
//          max: (2.0000,3.0000)     [ context: 'Complex sqrt( (34.699,56.742) )' ]
//          avg: (0.13208,0.13118)   [ sum: (5286.0,5250.0), count: 40020 ]
//          mean: (1.0000,1.5000)
//      TIME: 606.711
//      NOTE: Statistics are only for t > 0.


//    * Complex function: exp, part I
//      ----------------
//      DESCRIPTION: complex number z = a+bi,
//                   where a in [ -10.0, 10.0 ] and b in [ -10.0, 10.0 ],
//                   step size: 1.0
//      RESULTS: Stats:
//        diff
//          min: (0.0000,0.0000)     [ context: 'Complex exp( (-8.0000,-9.0000) )' ]
//          max: (-7.2760e-12,5.4570e-12) [ context: 'Complex exp( (10.000,-10.000) )' ]
//          avg: (-3.6622e-14,0.0000)      [ sum: (-1.6150e-11,0.0000), count: 441 ]
//          mean: (-3.6380e-12,2.7285e-12)
//        ulps
//          min: (0.0000,0.0000)     [ context: 'Complex exp( (-8.0000,-9.0000) )' ]
//          max: (5.0000,5.0000)     [ context: 'Complex exp( (6.0000,-3.0000) )' ]
//          avg: (1.4422,1.3515)     [ sum: (636.00,596.00), count: 441 ]
//          mean: (2.5000,2.5000)
//      TIME: 3.228


//    * Complex function: exp, part II
//      ----------------
//      DESCRIPTION: complex number s = a+bi,
//                   where a = 1/t and b in [ -10.0, 10.0 ],
//                   t in [ 0.0, 1.0 ], step size for t: 0.1
//                   and, for each t, step size for s: 0 + 0.01i
//      RESULTS: Stats:
//        diff
//          min: (0.0000,0.0000)     [ context: 'Complex exp( (-15.163,6.8302) )' ]
//          max: (1.3878e-17,1.0408e-17) [ context: 'Complex exp( (-3.4263,0.74425) )' ]
//          avg: (1.2919e-21,2.0628e-21) [ sum: (5.1700e-17,8.2554e-17), count: 40020 ]
//          mean: (6.9389e-18,5.2042e-18)
//        ulps
//          min: (0.0000,0.0000)     [ context: 'Complex exp( (-15.163,6.8302) )' ]
//          max: (16.000,3.0000)     [ context: 'Complex exp( (-5.5882,-4.7124) )' ]
//          avg: (2.1253,2.1114)     [ sum: ( 85055, 84500), count: 40020 ]
//          mean: (8.0000,1.5000)
//      TIME: 662.529
//      NOTE: Statistics are only for t > 0.


//    * Extended CD Model, g(s) function
//      ----------------
//      DESCRIPTION:
//        t: from 0.0 to 1.0 [ step: 0.1 ]
//        and for each t,
//        s: from (1/t,-10) to (1/t,+10) [ step: (0.0,0.01) ]
//        and for each s, both extracting and non-extracting calculations tested
//
//      RESULTS: Java == C++ (+/- 5 ulps) for all values
//


//    * Extended CD Model, l_ecd(s) (ie LaplaceECD)
//      ----------------
//      DESCRIPTION:
//        t: from 0.0 to 1.0 [ step: 0.1 ]
//        and for each t,
//        s: from (1/t,-10) to (1/t,+10) [ step: (0.0,0.01) ]
//
//      RESULTS: Stats:
//        diff:
//          min: (    0.0000,      0.0000 )   [ context: 'At t = 0.1, Laplace ECD for s = 10 - 9.72i ( extracting = true )' ]
//          max: ( 1.3878e-16, 3.8164e-17 )   [ context: 'At t = 0.9999999999999999, Laplace ECD for s = 1 - 0.12i ( extracting = false )' ]
//          avg: ( -1.2826e-20, -2.1252e-20 ) [ sum: (-5.1331e-16,-8.5052e-16), count: 40020 ]
//          mean: ( 6.9389e-17,  1.9082e-17 )
//        ulps:
//          min: (  0.0000,     0.0000 )      [ context: 'At t = 0.1, Laplace ECD for s = 10 - 9.72i ( extracting = true )' ]
//          max: ( 13.000,  29130      )      [ context: 'At t = 0.9999999999999999, Laplace ECD for s = 1 - 8.49i ( extracting = false )' ]
//          avg: (  7.3245,  8.2278 )         [ sum: (2.9313e+05,3.2928e+05), count: 40020 ]
//          mean: ( 6.5000, 14565 )
//
//      TIME: 2,233.14  secs  = 37+ mins (w/ debug on)
//               82.749 secs  =  1+ mins (w/ logging redirected to file)
//
//      NOTE: Statistics are only for t > 0.
//


//    * Extended CD Model, l_ecd(s) (ie LaplaceECD) [ ** hybrid ** ]
//      ----------------
//      DESCRIPTION:
//        t: from 0.0 to 1.0 [ step: 0.1 ]
//        and for each t,
//        s: from (1/t,-10) to (1/t,+10) [ step: (0.0,0.01) ]
//
//      TEST: LaplaceECD
//        diff
//          min: (0.0000,0.0000)     [ context: 'At t = 0.1, Laplace ECD  for s = 10 - 9.72i ( extracting = true )' ]
//          max: (1.3878e-16,3.8164e-17)     [ context: 'At t = 0.9999999999999999, Laplace ECD  for s = 1 - 0.12i ( extracting = false )' ]
//          avg: (-1.2826e-20,-2.1252e-20)   [ sum: (-5.1331e-16,-8.5052e-16), count: 40020 ]
//          mean: (6.9389e-17,1.9082e-17)
//        ulps
//          min: (0.0000,0.0000)     [ context: 'At t = 0.1, Laplace ECD  for s = 10 - 9.72i ( extracting = true )' ]
//          max: (13.000, 29130)     [ context: 'At t = 0.9999999999999999, Laplace ECD  for s = 1 - 8.49i ( extracting = false )' ]
//          avg: (7.3245,8.2278)     [ sum: (2.9313e+05,3.2928e+05), count: 40020 ]
//          mean: (6.5000, 14565)
//
//      TEST: LaplaceECD.hybrid.sqrt
//        diff
//          min: (0.0000,0.0000)     [ context: 'At t = 0.1, Laplace ECD (hybrid) for s = 10 - 9.72i ( extracting = true )' ]
//          max: (9.7145e-17,-9.7145e-17)    [ context: 'At t = 0.8999999999999999, Laplace ECD (hybrid) for s = 1.11 + 0.32i ( extracting = false )' ]
//          avg: (3.3837e-21,1.3520e-20) [ sum: (1.3542e-16,5.4108e-16), count: 40020 ]
//          mean: (4.8572e-17,-4.8572e-17)
//        ulps
//          min: (0.0000,0.0000)     [ context: 'At t = 0.1, Laplace ECD (hybrid) for s = 10 - 9.72i ( extracting = true )' ]
//          max: ( 16537,5.0000)     [ context: 'At t = 0.30000000000000004, Laplace ECD (hybrid) for s = 3.33 - 7.9i ( extracting = false )' ]
//          avg: (3.7628,3.2145)     [ sum: (1.5059e+05,1.2865e+05), count: 40020 ]
//          mean: (8268.5,2.5000)
//
//      TEST: LaplaceECD.hybrid.exp
//        diff
//          min: (0.0000,0.0000)     [ context: 'At t = 0.1, Laplace ECD (hybrid) for s = 10 - 9.99i ( extracting = false )' ]
//          max: (1.1102e-16,-1.1102e-16)    [ context: 'At t = 0.8999999999999999, Laplace ECD (hybrid) for s = 1.11 + 0.32i ( extracting = false )' ]
//          avg: (-2.4737e-20,-2.9464e-20)   [ sum: (-9.8996e-16,-1.1791e-15), count: 40020 ]
//          mean: (5.5511e-17,-5.5511e-17)
//        ulps
//          min: (0.0000,0.0000)     [ context: 'At t = 0.1, Laplace ECD (hybrid) for s = 10 - 9.99i ( extracting = false )' ]
//          max: (10.000, 29135)     [ context: 'At t = 0.9999999999999999, Laplace ECD (hybrid) for s = 1 - 8.49i ( extracting = false )' ]
//          avg: (5.5440,6.4180)     [ sum: (2.2187e+05,2.5685e+05), count: 40020 ]
//          mean: (5.0000, 14568)
//
//      TEST: LaplaceECD.hybrid.sqrt+exp
//        diff
//          min: (0.0000,0.0000)     [ context: 'At t = 0.1, Laplace ECD (hybrid) for s = 10 - 10i ( extracting = true )' ]
//          max: (1.1102e-16,-1.1102e-16)    [ context: 'At t = 0.8999999999999999, Laplace ECD (hybrid) for s = 1.11 + 0.32i ( extracting = false )' ]
//          avg: (-7.6977e-21,3.7647e-21)    [ sum: (-3.0806e-16,1.5066e-16), count: 40020 ]
//          mean: (5.5511e-17,-5.5511e-17)
//        ulps
//          min: (0.0000,0.0000)     [ context: 'At t = 0.1, Laplace ECD (hybrid) for s = 10 - 10i ( extracting = true )' ]
//          max: ( 16538,5.0000)     [ context: 'At t = 0.30000000000000004, Laplace ECD (hybrid) for s = 3.33 - 7.9i ( extracting = false )' ]
//          avg: (1.7081,1.1371)     [ sum: ( 68358, 45508), count: 40020 ]
//          mean: (8269.0,2.5000)
//
//      TIME: 337.033
//
//      NOTE: Statistics are only for t > 0.
//

//      OBSERVATIONS:  Laplace ECD
//        C++ vs pure Java
//          max: ( 13.000,  29130 )
//          avg: ( 7.3245, 8.2278 )   [ sum: (2.9313e+05,3.2928e+05), count: 40020 ]
//
//        C++ vs hybrid Java (sqrt)
//          max: (  16537, 5.0000 )
//          avg: ( 3.7628, 3.2145 )   [ sum: (1.5059e+05,1.2865e+05), count: 40020 ]
//
//        C++ vs hybrid Java (exp)
//          max: ( 10.000, 29135  )
//          avg: ( 5.5440, 6.4180 )   [ sum: (2.2187e+05,2.5685e+05), count: 40020 ]
//
//        C++ vs hybrid Java (sqrt+exp)
//          max: (  16538, 5.0000 )
//          avg: ( 1.7081, 1.1371 )   [ sum: ( 68358, 45508),         count: 40020 ]
//
//        (a) So using the C++ version of the complex number 'exp' function,
//            instead of the Java version, makes almost not difference.
//
//            The real component error decreases slightly and
//            the imaginary component error increase slightly.
//
//        (b) Using the C++ version of the complex number 'sqrt' function,
//            instead of the Java version, makes a big difference.
//
//            The real component error max value goes from 13 to 16,537
//            but the average error for that component drops.  Interesting.
//
//            The imaginary component error max value does the opposite,
//            i.e. it goes from 29130 to only 5 but the average error
//            for that component only drops by 60%.  Hmmm.
//



//    * Extended CD Model, ecd(z,t)
//      ----------------
//      DESCRIPTION:
//        t: from 0.0 to 1.0 [ step: 0.1 ], z = 7
//        and for each t, both extracting and non-extracting calculations tested
//
//      RESULTS: Stats:
//        diff:
//          min:  -2.0817e-17     [ context: 'Flux concentration (ecd) for z =7.0, t = 0.7999999999999999 ( extracting = true )' ]
//          max:   6.9389e-18     [ context: 'Flux concentration (ecd) for z =7.0, t = 0.7 ( extracting = false )' ]
//          avg:   1.2340e-20     [ sum:      2.4680e-19, count: 20 ]
//          mean: -6.9389e-18
//        ulps:
//          min:    0            [ context: 'Flux concentration (ecd) for z =7.0, t = 0.8999999999999999 ( extracting = true )' ]
//          max:   70            [ context: 'Flux concentration (ecd) for z =7.0, t = 0.5 ( extracting = false )' ]
//          avg:   15.700        [ sum:          314.00, count: 20 ]
//          mean:  35.000
//
//      TIME:   909.958 secs  = 15+ mins (w/ debug on)
//               82.749 secs  =  1+ mins (w/ logging redirected to file)
//
//      NOTE: Statistics are only for t > 0.
//



// Testing Notes:
// ==============
//    * Re-ran 'ecd' and 'l_ecd' tests above using the C++ definition of PI,
//      i.e. new Complex( 3.14159265358979323, 0.0 ), and the min, max and
//      average error values were identical to what's recorded above.
//      (K.Cline, 7/6/2011)
//


