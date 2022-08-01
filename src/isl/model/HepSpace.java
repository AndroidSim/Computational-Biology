/*
 * Copyright 2013-2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

public class HepSpace extends SSSpace {
  private static final long serialVersionUID = 2134877189020604305L;
  public HepSpace(SS s, int w, int h, SSSpace in) {
    super(s,w,h,in, null);
    rxnProdCount = new sim.field.grid.IntGrid2D(w,h,0);
  }

}
