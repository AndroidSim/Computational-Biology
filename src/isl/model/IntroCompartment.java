/*
 * Generic intro site - e.g. GI tract, peritoneal, etc.
 *
 * Copyright 2014-2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import java.util.Map;

public class IntroCompartment extends SampledCompartment {
  private static final long serialVersionUID = -4041476365495746386L;
  //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( IntroCompartment.class );
 
  public IntroCompartment(ISL isl, ec.util.MersenneTwisterFast r, Compartment in, Injectable out, double sr) {
    super(isl, r,in,out,sr);
  }

  @Override
  public Map<String,Number> getAvailableMobileObject() { return null; }

}
