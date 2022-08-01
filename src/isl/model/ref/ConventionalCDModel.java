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

import static java.lang.Math.*;
import static cern.jet.math.Bessel.j0;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import sim.engine.*;
// import isl.model.*;


/**
 * TBD: Add class description
 *
 * @future
 *    - 
 *
 * @author Ken Cline
 * @see 
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
public class ConventionalCDModel extends AbstractCDModel
{
  // TBD: Generate serialization ID using serialver tool
  // TBD: Add doc
  private static final long serialVersionUID = 1L;    

  // TBD: Add doc
  private static final Logger log
                   = LoggerFactory.getLogger( ConventionalCDModel.class );
  // Future: private static final Logger log = LoggingUtils.getLogger();

  /**
   * TBD: Add doc
   *
   */
  // Parameter values from Figure 4 [pg 363]
  // Value of L chosen to visually fit Figure 4A for the traditional model???
  protected double L  = 7;   // length of the organ (units?) [pg 349]
  protected double v  = 1;   // average axial velocity of perfusate (units?) [pg 346]


  // === [ Constructors ] =================================================

  /**
   * TBD: Add doc
   *
   */
  public ConventionalCDModel () {
    //log.debug( "Constructor called with args: {}", new Object[]{} );
  }


  // === [ Getters & Setters: Equation variables ] ========================

  /**
   * TBD: Add doc -- length of the organ (units?) [pg 349]
   *
   */
  public double getL () {
    return L;
  }

  /**
   * TBD: Add doc -- length of the organ (units?) [pg 349]
   *
   */
  public void setL ( double L ) {
    // TBD: Validation of L variable
    this.L = L;
    //log.debug( "Set L to {}", L );
  }


  /**
   * TBD: Add doc -- average axial velocity of perfusate (units?) [pg 346]
   *
   */
  public double getV () {
    return v;
  }

  /**
   * TBD: Add doc -- average axial velocity of perfusate (units?) [pg 346]
   *
   */
  public void setV ( double v ) {
    // TBD: Validation of v variable
    this.v = v;
    //log.debug( "Set v to {}", v );
  }



  // === [ Calculation Methods ] ==========================================

  /**
   * TBD: Add doc
   *
   */
  // flux concentration of solute in the liver plasma [pg 349]
                     // Equation (11), = inv Laplace Eq (10)
  // Now, integrate the exp*j_0 function, and find derivative with respect to t.
  // Essentially, the expression is (d/dt) INTEGRAL_0^t  {  f(u,t) } du
  //   Numerically, this is (INTEGRAL_t^{t+delta} { f(u,t) } du) / delta
  //   So, choose a small delta (~.01), and then choose even smaller du (~.0005)
  @Override
  public double ecd ( double z, double t ) {
    double Cfp   = 0.0;
    double term1 = 0.0;
    double term2 = 0.0;
    double delta = 0.01;
    double du    = 0.0005;
    double sum   = 0.0;

    // TBD: Validate z and t -- checks: not NaN, range for z ?
    //      range for t ?

    term1 = (M/Q) * (z*sqrt(T)) / (2*L) * exp( z / (2*Dn*L) - (t*(k2 + ke)) );

    for ( double u = (t+du) ; u < (t+delta) ; u += du ) {
      sum += integrand( z, u, t ) * du;
    }
    term2 = sum / delta;

    Cfp = term1 * term2;

    //log.debug( "Flux concentration for z = {}, t = {}: {}"
    //         + " ( = {} * {} = {} * ({} / {}) )",
    //           new Object[]{z,t,Cfp,term1,term2,term1,sum,delta} ); 
    // HACK
    log.info( String.format("Flux concentration for z = %13e, t = %13e: %13e"
             + " ( = %13e * %13e = %13e * (%13e / %13e) )",
               z, t, Cfp, term1, term2, term1, sum, delta ) ); 
    return Cfp;
  }

  /**
   * TBD: Add doc
   *
   */
  public double integrand ( double z, double u, double t ) {
    double numer    = 0.0;
    double denom    = 0.0;
    double besselx  = 0.0;
    double bessel   = 0.0;
    double integr   = 0.0;

    // TBD: Validate z, u and t -- checks: not NaN, range for z ?
    //      range for u ? range for t ?

    //numer = exp( u*(k2 + ke - k1) - ((z*z) + pow(u*v,2)) / (4*Dn*u*v*z) );
    numer = exp( u*(k2 + ke + k1) - ((z*z) + pow(u*v,2)) / (4*Dn*u*v*z) );
    denom = sqrt( Dn * PI * pow(u,3) );
    //besselx = 2 * sqrt( k2 * k1 * ( (u*u) - (t*u) ) );
    besselx = 2 * sqrt( k2 * k2 * ( (u*u) - (t*u) ) );
    bessel = j0( besselx );
    integr = numer * bessel / denom;

    log.info( "Integrand for z = {}, u = {}, t = {}: {}"
             + " ( = {} * {} / {}, where bessel = j0({}) )",
               new Object[]{z,u,t,integr,numer,bessel,denom,besselx} );
    return integr;
  }

// TBD:
// Calculation discrepancies in the 'integrand' method
// ----------------------------------------------------
// The Java code in the 'integrand' method defined above implements the
// calculations the same way that the ISL/Swarm/C++ ConvectionDispersion
// class does.  However, these equations do not match exactly what is
// defined in equation 11 found on page 349 of the Roberts and Anissimov
// paper[1].  There exists two (2) small discrepancies:
//
//    1. In the integrand numerator, the first term inside the
//       parenthesis for the exponential:
//          paper has: u * ( k2 +  ke -  k1 )
//          C++ has:   u * ( k2 +  ke +  k1 )
//       Difference: '+' instead of '-' at the end
//
//    2. In the integrand, the term for the Bessel J0 function,
//       aka the bessel substrate:
//          paper has: 2 * sqrt( k2 * k1 ( u * u - t * u )  )
//          C++ has:   2 * sqrt( k2 * k2 ( u * u - t * u )  )
//       Difference: ' k2 * k2' instead of ' k2 * k1'
//
// To date, I have noted these differences but I have not performed
// the necessary testing to determine whether the paper or the
// C++/Java code is correct.  That is, I have not yet looked for
// the numerical output that the model should produce and run tests
// to see if published equations actually produce the expected values.
// It is possible that there are typos in the paper and it is also
// possible that these differences are too small to cause any
// noticeable effect (which is another fairly easy test to perform
// that I still need to do).
//
// Note, since the Conventional Convection Dispersion model is not
// currently used by the larger ISL model, determining which version
// of the equation is correct and what impact the discrepancies have
// (if any) is not a major priority.  (However, I do need to check
// with Glen et al. to see if this may have impacted earlier work.)
//
// ________
// [1] Michael S. Roberts and Yuri G.Anissimov, "Modeling of Hepatic Elimination and Organ Distribution Kinetics with the Extended Convection-Dispersion Model", Journal of Pharmacokinetics and Biopharmaceutics, Vol 27, No 4 (1999)
//


  // === [ Main Method (for testing only) ] ===============================

  /**
   * TBD: Add doc
   *
   */
  public static void main ( String[] args ) {
    ConventionalCDModel cd = new ConventionalCDModel();
    double timeStart  =  7.0;
    double timeStop   =  8.0;  // 60.0;
    double timeStep   =  0.1;
    double z = cd.getL();
    double C = 0.0;
    java.util.List<String> cols = null;
    isl.table.Table<Double> data = null;

    cols = java.util.Arrays.asList( new String[]{"Time", "z","C"} );
    data = new isl.table.DefaultTable<Double>( cols, null, Double.class );

    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
      C = cd.ecd( z, t );
      data.addRow( new Double[]{ t, z, C } );
    }

    Object rows = isl.model.data.CSVDataModel.formatRows(data,null,0,-1,"%13e");
    log.info( "data[{}][{}] =\n{}",
              new Object[]{ data.getRowCount(), data.getColumnCount(), rows } );

  }

}  // end of ConventionalCDModel class


