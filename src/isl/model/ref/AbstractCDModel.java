/*
 * IPRL - Abstract Convection Dispersion Model
 *
 * Copyright 2003-2014 - Regents of the University of California, San
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

// import isl.model.*;


/**
 * TBD: Add class description
 * Super class for Conventional and Extended Convection Dispersion
 * Models
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
public abstract class AbstractCDModel
{
  // TBD: Generate serialization ID using serialver tool
  // TBD: Add doc
  private static final long serialVersionUID = 1L;    

  // TBD: Add doc
  private static final Logger log
                   = LoggerFactory.getLogger( AbstractCDModel.class );
  // Future: private static final Logger log = LoggingUtils.getLogger();

  /**
   * TBD: Add doc
   *
   */
  // Parameter values from Figure 4 [pg 363]

  protected double k1  = 0.03;   // PS * fup / Vp (units?) [pg 349]
  protected double k2  = 0.01;   // PS * fuh / Vh (units?) [pg 349]
  protected double ke  = 0.1;    // Clint * fuh / Vh (units?) [pg 349]

  protected double Dn  = 0.265;  // vascular dispersion number, Dn = D / (v*L)
                                 //    (units?) [pg 344,349]
  protected double T   = 6.35;   // L / v = Vp / Q (sec) [pg 349]

  protected double M   = 1.0;    // mass of bolus input injected (units?) [pg 348]
  protected double Q   = 0.312;  // perfusate flow (ml/sec?) [pg 344]

  // TBD: What are 'p', 'D2' and 'T2' needed for?
  // protected double p   = 0.858;  // from Gaussian model [pg 359?] ???
  // protected double D2  = 3.77;   // from Gaussian model [pg 359?] ???
  // protected double T2  = 35.4;   // from Gaussian model [pg 359?] ???

  // PS := permeability surface area product (units?) [pg 346]
  // fup := fraction unbound in the perfusate (units?) [pg 344]
  // fuh := fraction unbound in the hepatocytes ??? (units?) [pg ?]
  // Vp := volume of distribution of the solute in plasma (units?) [pg 346]
  // Vh := volume of distribution of the solute in hepatocytes (units?) [pg 346]

  // Clint := clearance due to metabolic enzyme activity or biliary extraction
  //            (units?) [pg 344,346]
  // D := effective axial dispersion coefficient (units?) [pg 346]
  // L := length of the organ (units?) [pg 349]
  // v := average axial velocity of perfusate (units?) [pg 346]

  // protected double k₁ = k1;   // k\u2081  
  // protected double k₂ = k2;   // k\u2082;
  // protected double kₑ = ke;   // k\u2091
  // protected double D?? = Dn   // D\u2099


  /**
   * TBD: Add doc
   *
   */
  protected boolean extracting = false;


  // === [ Constructors ] =================================================

  /**
   * TBD: Add doc
   *
   */
  public AbstractCDModel () {
    //log.debug( "Constructor called with args: {}", new Object[]{} );
  }


  // === [ Getters & Setters: Equation variables ] ========================

  /**
   * TBD: Add doc -- effective axial dispersion coefficient (units?) [pg 346]
   *
   */
  public double getDn () {
    return Dn;
  }

  /**
   * TBD: Add doc -- effective axial dispersion coefficient (units?) [pg 346]
   *
   */
  public void setDn ( double Dn ) {
    // TBD: Validation of Dn variable
    this.Dn = Dn;
    //log.debug( "Set Dn to {}", Dn );
  }


  /**
   * TBD: Add doc -- PS * fup / Vp (units?) [pg 349]
   *
   */
  public double getK1 () {
    return k1;
  }

  /**
   * TBD: Add doc -- PS * fup / Vp (units?) [pg 349]
   *
   */
  public void setK1 ( double k1 ) {
    // TBD: Validation of k1 variable
    this.k1 = k1;
    //log.debug( "Set k1 to {}", k1 );
  }


  /**
   * TBD: Add doc -- PS * fuh / Vh (units?) [pg 349]
   *
   */
  public double getK2 () {
    return k2;
  }

  /**
   * TBD: Add doc -- PS * fuh / Vh (units?) [pg 349]
   *
   */
  public void setK2 ( double k2 ) {
    // TBD: Validation of k2 variable
    this.k2 = k2;
    //log.debug( "Set k2 to {}", k2 );
  }


  /**
   * TBD: Add doc -- Clint * fuh / Vh (units?) [pg 349]
   *
   */
  public double getKe () {
    return ke;
  }

  /**
   * TBD: Add doc -- Clint * fuh / Vh (units?) [pg 349]
   *
   */
  public void setKe ( double ke ) {
    // TBD: Validation of ke variable
    this.ke = ke;
    //log.debug( "Set ke to {}", ke );
  }


  /**
   * TBD: Add doc -- L / v = Vp / Q (units?) [pg 349]
   *
   */
  public double getT () {
    return T;
  }

  /**
   * TBD: Add doc -- L / v = Vp / Q (units?) [pg 349]
   *
   */
  public void setT ( double T ) {
    // TBD: Validation of T variable
    this.T = T;
    //log.debug( "Set T to {}", T );
  }


  /**
   * TBD: Add doc -- mass of bolus input injected (units?) [pg 348]
   *
   */
  public double getM () {
    return M;
  }

  /**
   * TBD: Add doc -- mass of bolus input injected (units?) [pg 348]
   *
   */
  public void setM ( double M ) {
    // TBD: Validation of M variable
    this.M = M;
    //log.debug( "Set M to {}", M );
  }


  /**
   * TBD: Add doc -- perfusate flow ? (units?) [pg 344]
   *
   */
  public double getQ () {
    return Q;
  }

  /**
   * TBD: Add doc -- perfusate flow ? (units?) [pg 344]
   *
   */
  public void setQ ( double Q ) {
    // TBD: Validation of Q variable
    this.Q = Q;
    //log.debug( "Set Q to {}", Q );
  }



  // === [ Getters & Setters: Flag/Mode variables ] =======================

  /**
   * TBD: Add doc
   *
   */
  public boolean isExtracting () {
    return extracting;
  }

  /**
   * TBD: Add doc
   *
   */
  public void setExtracting ( boolean state ) {
    extracting = state;
  }


  // === [ Calculation Methods ] ==========================================

  /**
   * TBD: Add doc
   *
   */
  public abstract double ecd ( double z, double t );


  /**
   * TBD: Add doc
   *
   */
  public double ecd ( double z, double t, boolean extracting ) {
    boolean  state  = isExtracting();
    double   out    = 0.0;

    setExtracting( extracting );
    out = ecd( z, t );
    setExtracting( state );

    return out;
  }


  // === [ Main Method (for testing only) ] ===============================

  /**
   * TBD: Add doc
   *
   */
  //public static void main ( String[] args ) {
  //}

}  // end of AbstractCDModel class



// TODO: 6/5
// ====
//    * Using unicode for var names?  (ie so I can use subscripts
//      and non-latin characters so the code looks more like the
//      published equations)
//

// TODO: 6/9
// ====
//    * [DONE] Split CD class into AbstractCD and ConventionalCD
//    * Create number generators for CD params (ie for unit testing)
//       * What are defined value ranges for each parameter?
//       * What are boundary cases for each parameter?
//    * 
// 


