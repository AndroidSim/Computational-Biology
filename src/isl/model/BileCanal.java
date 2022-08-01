/*
 * Copyright 2003-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.model;

import java.util.Arrays;

public class BileCanal extends Tube {
  //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( BileCanal.class );

  isl.model.SS ss = null;

  public BileCanal(SS p, int c, int l) {
    super(c, l);
    if (p == null) throw new RuntimeException(this+" SS cannot be null");
    ss = p;
  }

  public double getCC() {
    return bsg.util.MathUtils.area((float)circ) * ss.hepStruct.model.gridScale;
  }
  public void flow(int slots) {
    int end = (slots > tube.length ? tube.length : slots);
    // distribute the mobile object in the 1st slot arrays
    for (int lNdx=0 ; lNdx<end ; lNdx++) {
      java.util.ArrayList<MobileObject> moved = ss.distribute(tube[lNdx], SS.BILE);
      if (moved != null) for (MobileObject m : moved) tube[lNdx].remove(m);
    }
    if (slots >= 1) advance(slots);
  }
  // flows backwards
  public void advance(int slots) {
    // flow() handles the whole list if slots >= tube.length
    if (slots < tube.length) {
      java.util.ArrayList copiedLists[] = Arrays.copyOf(tube, tube.length);
      // regress the list by slots
      for (int yNdx=tube.length-1 ; yNdx >= slots ; yNdx--)
        tube[yNdx-slots] = copiedLists[yNdx];
      // put lists that fell off back on top
      for (int yNdx=slots-1 ; yNdx >= 0 ; yNdx--) 
        tube[copiedLists.length-(slots-yNdx)] = copiedLists[yNdx];
    } // else if (slots >= tube.length) do nothing
  }
}
