/*
 * Copyright 2003-2016 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package isl.model.ref;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsg.util.Complex;
import static bsg.util.Complex.*;
import static bsg.util.ComplexMath.*;


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
public class ExtendedCDModel extends AbstractCDModel
{
  // TBD: Generate serialization ID using serialver tool
  // TBD: Add doc
  private static final long serialVersionUID = 1L;    

  // TBD: Add doc
  private static final Logger log
                   = LoggerFactory.getLogger( ExtendedCDModel.class );
  // Future: private static final Logger log = LoggingUtils.getLogger();


  /**
   * TBD: Add doc
   *
   */
  // Parameter values from Figure 4 [pg 363]
  // Value of L chosen to visually fit Figure 4A for the traditional model???
  protected Complex a  = new Complex( 0.00654, 0.0 );  // f/V1 (units?) [pg 352]
  protected Complex b  = new Complex( 0.0248,  0.0 );  // f/V2 (units?) [pg 352]

  // f := flow from the first to second compartment and vice versa (units?) [pg 352]
  // V1 := flux volume in the first compartment (units?) [pg 352]
  // V1 := flux volume in the second compartment (units?) [pg 352]

  // === [ Constructors ] =================================================

  /**
   * TBD: Add doc
   *
   */
  public ExtendedCDModel () {
    //log.debug( "Constructor called with args: {}", new Object[]{} );
  }


  // === [ Getters & Setters: Equation variables ] ========================

  /**
   * TBD: Add doc -- f/V1 (units?) [pg 352]
   *
   */
  public Complex getA () {
    return a;
  }

  /**
   * TBD: Add doc -- f/V1 (units?) [pg 352]
   *
   */
  public void setA ( Complex a ) {
    // TBD: Validation of a variable
    this.a = a;
    //log.debug( "Set a to {}", a );
  }

  public void setA ( double a ) {
    setA( new Complex(a) );
  }


  /**
   * TBD: Add doc -- f/V2 (units?) [pg 352]
   *
   */
  public Complex getB () {
    return b;
  }

  /**
   * TBD: Add doc -- f/V2 (units?) [pg 352]
   *
   */
  public void setB ( Complex b ) {
    // TBD: Validation of b variable
    this.b = b;
    //log.debug( "Set b to {}", b );
  }

  public void setB ( double b ) {
    setB( new Complex(b) );
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
    // TBD: Validate z and t -- checks: not NaN, range for z ?
    //      range for t ?

    // Perform numerical inverse laplace transform on laplaceECD.
    return inverseLaplace( t, 10.0, 0.01 );
  }

  // TBD: Should we pass the 'inf' (infinity) and 'ds' (delta s) values
  //      as parameters to the 'inverseLaplace' method?  Or perhaps
  //      the should be constant ivars set when the object is created?
  //      Or class/static constants?  Or ???


  /**
   * TBD: Add doc
   *
   */
  // public double inverseLaplace ( double ɣ, double inf, double ds ) {
  public double inverseLaplace ( double t, double inf, double ds ) {
    // Contour of integration chosen to avoid numerical blowups
    // double ɣ = 1/t; // \u02E0 = 1/t;
    double  gamma      = 1 / t;
    Complex dw         = new Complex( 0, ds );
    Complex Fs         = null;
    Complex integrand  = null;
    Complex sum        = Complex.ZERO;
    int i = 0;

    // TBD: Validate t, inf and ds -- checks: not NaN, range for t ?
    //      range for inf ?  range for ds ?

    for ( Complex s = new Complex(gamma,-inf) ; s.imag() < inf ; s = s.add(dw) ) {
      Fs = laplaceECD( s );
      integrand = Fs.multiply( exp(s.multiply(t)) ).multiply( ds );
      sum = sum.add( integrand );
      // sum = sum.add( Fs.×( exp(s.×(t)) ).×(ds) );
      // sum = sum.add( Fs.multiply( exp(s.multiply(t)) ).multiply(ds) );

      //log.debug( "{}: s = {}, F(s) = {}, integrand = {}, sum = {}",
      //           new Object[]{i,s,Fs,integrand,sum} );

      i++;
      //if ( i == 10 ) System.exit( 0 );
    }

    sum = sum.divide( TWO_PI );

    // HACK
    //log.debug( String.format( "Inverse Laplace for gamma = %f, inf = %f,"
    //                        + " ds = %f: %s", gamma, inf, ds, sum ) ); 

    // TBD: Add check that sum.imag() is negligible
    return sum.real();
  }

  // TBD: The 'inverseLaplace' method is using the 'TWO_PI' constant
  //      which is defined in the ComplexMath class.  This constant,
  //      in turn, is defined using the PI constant from the core
  //      Java Math class, i.e. java.lang.Math.
  //
  //      The C++ code is using locally defined 'pi_c' constant that
  //      has slightly less precision.  In particular, the local C++
  //      PI value is defined to 17 places and the Java PI goes out
  //      to 20 places.
  //
  //      I need to do some testing to see how much difference this
  //      small change to the value of PI makes.

  // TBD: The inverse Laplace operation is not (as I understand it)
  //      specific to the function is operates on.  I wonder therefore
  //      if we could/should define an Laplace utility class that
  //      wrappers another function and performs the "transform" on
  //      values.  I hestitated on pursuing this approach because
  //      (a) it seemed like it would require either reflection or
  //      a new interface (so the LaplaceTransform class would know
  //      what function to wrapper/invoke).  And (b) the ExtendedCDModel
  //      is the only place, that I'm aware of, where we use this
  //      transform.  In other words, it would a lot framework to
  //      create for only 1 know use and the framework would likely
  //      be a performance hit.  Oh well, something to consider if
  //      we add other calculations that perform Laplace transforms.


  /**
   * TBD: Add doc
   *
   */
  public Complex laplaceECD ( Complex s ) {
    Complex  term1  = null;
    Complex  term2  = null;
    Complex  Cout   = null;

    // TBD: Validate s -- checks: not NaN, range for s ?

    term1 = ONE.add( FOUR.multiply( Dn*T ).multiply( s.add(g(s)) ) );
    term2 = ONE.subtract( sqrt( term1 ) ).divide( 2*Dn );
    Cout = new Complex( M/Q ).multiply( exp(term2) );

    //log.debug( "Laplace domain flux concentration, Cout(s), for s = {}: {}"
    //         + " ( sqrt term: {}, exp term: {} )",
    //           new Object[]{s,Cout,term1,term2} );
    return Cout;
  }

  // TBD: MutableComplex class
  //   The Complex number class that we are using is immutable,
  //   ala Integer, Double and etc.  That is, the value does not
  //   change when an operations such as 'add' is performed;
  //   instead a new instance of Complex is created and returned.
  //   This has a number of advantages but also a performance
  //   cost.
  //
  //   We should consider creating a mutable Complex number class
  //   as well (i.e. have both) and see if there's much performance
  //   gain from using MutableComplex for our calculations above
  //   and below.

  // TBD: Caching semi-constant values
  //   It appears that there parts of the equations for the
  //   Extended Convection Dispersion model that do not depend
  //   on the value of 't' or 's'.   These values could hence
  //   be pre-calculated and thereby improve performance.
  //   However, we would need to (1) override the necessary set
  //   methods so that the "semi-constant" values were updated
  //   appropriately and (2) we would need to handle the
  //   initialization phase when only part of values had been
  //   set, i.e. for each parameter that is part of a "semi-
  //   constant" we would need to insert a call to re-calculate
  //   the derived value and in that calculation function check
  //   whether or not all necessary values had been to set yet.
  //
  //   Some possible derived "semi-constant" values:
  //     // TBD: Need a better name for some of these...
  //     M_over_Q = new Complex( M/Q );
  //     _4DnT = FOUR.multiply( Dn*T );
  //     Two_x_Dn = 2 * Dn;
  //     ab = a.multiply( b );
  //     k1_x_k2 = new Complex( k1 ).multiply( k2 );
  //     ke_t_k2 = new Complex( ke ).add( k2) );
  //     k1_t_a  = new Complex( k1 ).add( a );
  //     k1_t_b  = new Complex( k1 ).add( b );
  // 
  //   I would imaging that the equation-based models are not
  //   much of a performance bottleneck when compared to the
  //   ABM code, so I initially opted not to implement this
  //   caching because I think it would make the code much less
  //   readable/debuggable (unless I could come up with good
  //   names for these "semi-constant" values).
  //


  /**
   * TBD: Add doc
   *
   */
  public Complex g ( Complex s ) {
    Complex k1 = null;
    Complex k2 = null;
    Complex ke = null;
    Complex ab = null;
    Complex x  = null;
    Complex gs = null;

    // TBD: Pre-calculate 'ab' whenever 'a' or 'b' is set and store this
    //      in an ivar
    ab = a.multiply( b );

    //log.debug( "Calculating g(s) for s = {}, a = {}, b = {}, ab = {}"
    //         + " extracting = {}", new Object[]{s,a,b,ab,isExtracting()} );

    if ( ! isExtracting() ) {
      gs = a.subtract( ab.divide( s.add(b) ) );
      //log.debug( String.format( "Non-extracting g(s) = %#s, for s = %#s,"
      //                + " a = %#s, b = %#s, ab = %#s", gs, s, a, b, ab ) );
      return gs;
    }

    k1 = new Complex( getK1() );
    k2 = new Complex( getK2() );
    ke = new Complex( getKe() );
    x = k1.multiply( k2 ).divide( s.add(ke).add(k2) );

    gs = k1.subtract( x )
           .add( a )
           .subtract( ab.divide( s.add(k1).subtract(x).add(b) ) );

    //log.debug( String.format( "Extracting g(s) = %#s, for s = %#s,"
    //                        + " a = %#s, b = %#s, ab = %#s,"
    //                        + " k1*k2 / (s + ke + k2 ) = %#s",
    //                          gs, s, a, b, ab, x ) );
    return gs;
  }


  // === [ Main Method (for testing only) ] ===============================

  /**
   * TBD: Add doc
   *
   */
  public static void main ( String[] args ) {
    ExtendedCDModel cd = new ExtendedCDModel();
    double timeStart  =  7.0;
    double timeStop   =  7.5;  // 60.0;
    double timeStep   =  0.1;
    double z = (new ConventionalCDModel()).getL();
    double C = 0.0;
    java.util.List<String> cols = null;
    isl.table.Table<Double> data = null;

    cols = java.util.Arrays.asList( new String[]{"Time", "z","C"} );
    data = new isl.table.DefaultTable<Double>( cols, null, Double.class );

    for ( double t = 0.0 ; t < (timeStop - timeStart) ; t += timeStep ) {
      C = cd.ecd( z, t );
      data.addRow( new Double[]{ t, z, C } );
    }

    Object rows = isl.model.data.CSVDataModel.formatRows(data,null,0,-1,"%15e");
    log.info( "data[{}][{}] =\n{}",
              new Object[]{ data.getRowCount(), data.getColumnCount(), rows } );

  }

}  // end of ExtendedCDModel class


// Design Discussion: Subclasses for non-/extracting implementations
// ------------------
// Background:
//   The only difference between the extracting and non-extracting
//   equation-based models (EBMs) described in the Roberts and Anissimov
//   paper[1], is the use of the 'g(s)' function in the extracting
//   version and the 'a - ab/(s+b)' term in the non-extracting version.
//   Otherwise the equations are the same.
//
//   Therefore, it seemed to make sense to view the 'a - ab/(s+b)' term
//   as another form of the 'g(s)' function, i.e. a non-extracting 'g(s)'
//   which returns 'a - ab/(s+b)' versus an extracting 'g(s)' which returns
//   'k1 - (k1k2)/(s+ke+k2) + a - ( (ab) / (s + k1 - (k1k2)/(s+ke+k2) +b) )'.
//   This is how this Java implementation of the Extended Convection
//   Dispersion EBMs is written.
//
// Alternative Design:
//   Right now, the 'g' method checks the status of the 'extracting' ivar
//   (inherited from the parent class, AbstractCDModel) and decides
//   which calculation to perform.  An alternative design would be to
//   subclasses for each version of the 'g(s)' calculation, i.e. an
//   extracting and a non-extracting subclass.  The ExtendedCDModel class
//   could be abstract, i.e. the 'g' method would be un-implemented,
//   or we could have it implement one of the EBMs (likely the simpler
//   non-extracting version) and then we'd only need one subclass.
//   Note, if we went with an abstract ExtendedCDModel, then the concrete
//   subclasses could be defined with inner classes, e.g.:
//      public static final ExtendedCDModel NonExtracting =
//           = new ExtendedCDModel () {
//                public Complex g ( Complex s ) {
//                    ...
//                }
//             };
//
//      public static final ExtendedCDModel Extracting =
//           = new ExtendedCDModel () {
//                public Complex g ( Complex s ) {
//                    ...
//                }
//             };
//
//
//   Computationally there probably isn't much advantage to using this
//   design.  It does eliminate the need for the 'if' statement inside
//   the 'g' method but that's probably a negligible performance hit.
//
//   However, the paper[1] does mention other types of extended convection
//   dispersion; specifically on pages 354-355, in the section titled
//   "Alternative Tissue Disposition Models":
//      "... These alternative forms of disposition models in
//       hepatocytes can be readily incorporated in this model
//       by using the appropriate function in place of g(s). ..."
//
//   The paper then provides two additional definitions of the 'g(s)'
//   function that might be used.  If we wished to explore using these
//   alternative EBMs in our project then perhaps it makes sense to
//   use subclass (as opposed to "flag variable") design described
//   above.
//
// ________
// [1] Michael S. Roberts and Yuri G.Anissimov, "Modeling of Hepatic Elimination and Organ Distribution Kinetics with the Extended Convection-Dispersion Model", Journal of Pharmacokinetics and Biopharmaceutics, Vol 27, No 4 (1999)
//


// TODO: 6/18
//    * Implement g function for extracting
//    * Implement g function for non-extracting
//    * Check that code compiles
//    * Create Test class
//    *  ...
//

