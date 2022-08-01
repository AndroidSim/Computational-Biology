/*
 * Copyright 2003-2014 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.model;

/**
 *
 * @author gepr
 */
import java.util.ArrayList;

public class Tube {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( ISL.class );
  ArrayList<MobileObject> tube[] = null;
  public ArrayList<MobileObject>[] getTube() { return tube; }
  int circ = -Integer.MAX_VALUE;
  public int getCirc() { return circ; }
  public int getLength() { return tube.length; }
  public Tube(int c, int l){
    if (l <= 0) throw new RuntimeException("Tube cannot have "+l+" length.");
    tube = new ArrayList[l];
    for (int lNdx=0 ; lNdx<l ; lNdx++) tube[lNdx] = new ArrayList<>();
    if (c <= 0) throw new RuntimeException("Tube cannot have "+c+" circumference.");
    circ = c;
  }
  public ArrayList<MobileObject> get(int ndx) {
    if (ndx < 0 || tube.length <= ndx) throw new IndexOutOfBoundsException(ndx + " invalid index for "+this);
    return tube[ndx];
  }
  public void put(int ndx, ArrayList<MobileObject> array) {
    if (ndx < 0 || tube.length <= ndx) throw new IndexOutOfBoundsException(ndx + " invalid index for "+this);
    tube[ndx] = array;
  }

  public void advance(int slots) {
    // no advance is necessary if slots >= tube.length
    if (slots < tube.length) {
      ArrayList copiedLists[] = new ArrayList[tube.length];
      for (int yNdx=0 ; yNdx<tube.length ; yNdx++) {
        copiedLists[yNdx] = tube[yNdx];
      }
      // shuffle the core lists forward by slots
      for (int yNdx=0 ; yNdx<(tube.length-slots) ; yNdx++) {
        tube[slots+yNdx] = copiedLists[yNdx];
      }
      // put lists shifted off the end back onto the beginning
      int start = copiedLists.length-slots;
      for (int yNdx = start; yNdx < copiedLists.length; yNdx++) {
        tube[yNdx-start] = copiedLists[yNdx];
      }
    }
  }
  public ArrayList<MobileObject> getMobileObjects() {
    ArrayList<MobileObject> retVal = null;
    for (int tNdx=0 ; tNdx<tube.length ; tNdx++)
      if (tube[tNdx].size() > 0) {
        if (retVal == null) retVal = new ArrayList<>();
        retVal.addAll(tube[tNdx]);
      }
    return retVal;
  }
}
