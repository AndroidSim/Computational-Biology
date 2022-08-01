/*
 * Copyright 2016 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

public class Lobule extends HepStruct {

  private static final long serialVersionUID = -8339811312958160197L;
  private static org.slf4j.Logger log = null;

  public Lobule ( ISL i, ec.util.ParameterDatabase pd, ec.util.MersenneTwisterFast rng ) {
    super(i,pd,rng);
  }

  public Vas portalVein = null;
  public Vas centralVein = null;
  
  @Override
  public boolean initIO() {
    // input and output
    portalVein = new Vas(hepStructRNG, this);
    portalVein.setLogger(log);
    portalVein.id = 0;
    portalVein.vasType = Vas.VasType.IN;
    structInput = portalVein;
    this.addNode(structInput );
    centralVein = new Vas( hepStructRNG, this );
    centralVein.setLogger(log);
    centralVein.id = 1;
    centralVein.vasType = Vas.VasType.OUT;
    structOutput = centralVein;
    this.addNode(structOutput );

    return true;
  }
}
