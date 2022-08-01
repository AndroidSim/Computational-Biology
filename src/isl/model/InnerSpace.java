/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.model;

import bsg.util.CollectionUtils;
import java.util.ArrayList;
import java.util.Map;

public class InnerSpace extends SSSpace {
  private static final long serialVersionUID = 688049923018459132L;

  Map<MobileObjectType, Tube> cores = new java.util.HashMap<>();
  ArrayList<MobileObject> getCoreAt(MobileObject m, int y) { return cores.get(getMobileObjectType(m)).get(y); }
  double flow = Double.NaN;
  private static org.slf4j.Logger log = null;

  @Override
  public void setLogger(org.slf4j.Logger logger) {
    log = logger;
  }

  /**
   * Creates a new instance of InnerSpace
   */
  public InnerSpace(SS s, int w, int h, double fr) {
    super(s, w, h, null, null);
    // add coreOut() to the flowShuffler to give core mobileobject a chance to jump to the rim
    flowShuffler.add((Runnable) () -> { coreOut(); });

    if (s != null) {
      ss = s;
    } else {
      throw new RuntimeException("InnerSpace: parent SS cannot be null.");
    }
    grids.keySet().forEach((st) -> { cores.put(st, new Tube(w,h)); });
    //core = new Tube(w, h);
    if (fr > 0) {
      flow = fr;
    }
    /*
     * Add the core to the flow shuffler
     */
    flowShuffler.add((Runnable) () -> { advanceCore(flow); });
  }

  public int getCoreCount() {
    int retVal = 0;
    for (Tube t : cores.values()) {
      ArrayList<MobileObject> t_a[] = t.getTube();
      for (int y=0 ; y<t.getLength() ; y++) {
        retVal += t_a[y].size();
      }
    }
    return retVal;
  }
  
  private void advanceCore(double flowRate) {
    // shuffle types to avoid always pushing MobileObjectTypes in the same order
    ArrayList<MobileObjectType> types = new ArrayList<>(cores.keySet());
    types = (ArrayList<MobileObjectType>)CollectionUtils.shuffle(types, ss.hepStruct.model.parent.bcRNG);
    for (MobileObjectType mt : types) {
      Tube t = cores.get(mt);
      if (mt.hasProperty("flowRate")) flowRate = (double)mt.getProperty("flowRate");
      double intPartFR = Math.floor(flowRate);

      int fr_i = (int)intPartFR;
      if (ss.hepStruct.model.parent.bcRNG.nextDouble() < (flowRate-intPartFR)) fr_i++;
      if (fr_i == 0) continue;

      // mobileobject in the last flowRate cells of the core gets distributed
      int start = height - fr_i;
      if (start < 0) start = 0;

      for (int y=start ; y<height ; y++) {
        ArrayList<MobileObject> mobileObjectAtY = t.get(y);
        ArrayList<MobileObject> move = ss.distribute(mobileObjectAtY, CompartmentType.GRID);
        if (move != null) move.forEach((s) -> { mobileObjectAtY.remove(s); });
      }
      // candidates that weren't distributed from the end of the core go up flowRate
      t.advance(fr_i); 
    }

  }

  public void coreOut() {
    /*
     * first give the chance for core mobileobject to jump to the rim mobileobjects list is
     * not changed
     * Dir = { E, W, Out, In, NE, N, NW, SW, S, SE }
     */
    ArrayList<MobileObject> jumped2Rim = new ArrayList<>();
    // shuffle cores to avoid a bias to a particular MobileObject typeString
    ArrayList<MobileObjectType> types = new ArrayList<>(cores.keySet());
    types = (ArrayList<MobileObjectType>)CollectionUtils.shuffle(types, ss.hepStruct.model.parent.bcRNG);
    for (MobileObjectType type : types) {
      Tube core = cores.get(type);
      for (int coreNdx = 0; coreNdx < core.getLength(); coreNdx++) {
        ArrayList<MobileObject> corePt = core.get(coreNdx);
        for (MobileObject m : corePt) {
          double draw = ss.hepStruct.model.parent.bcRNG.nextDouble();
          if (probVs.get(m.getTypeString()).get(SSSpace.Dir.W) <= draw
              && draw < probVs.get(m.getTypeString()).get(SSSpace.Dir.Out)) {
            if (jump2Rim(m, coreNdx)) jumped2Rim.add(m);
          }
        }
        jumped2Rim.forEach((s) -> { corePt.remove(s); });
      jumped2Rim.clear();
    }
    }
  }

  private boolean jump2Rim(MobileObject m, int coreNdx) {
    boolean retVal = false;
    for (int xNdx = 0; xNdx < width; xNdx++) {
      if (countMobileObjectAt(getMobileObjectType(m), xNdx, coreNdx) < ss.hepStruct.model.gridScale) {
        putMobileObjectAt(m, xNdx, coreNdx);
        retVal = true;
        break;
      }
    }
    return retVal;
  }

  /**
   * Overrides SSSpace.jump2Space() to accommodate the core.  Does not remove
   * the MobileObject from its current location.
   * @param mt -- The type of the MobileObject for choosing the grid.
   * @param m -- The MobileObject to add to the new space.
   * @param tgtSpace -- The SSSpace to add it to.
   * @return
   */
  @Override
  boolean jump2Space(MobileObjectType mt, MobileObject m, SSSpace tgtSpace) {
    boolean retVal = false;
    if (tgtSpace == outwardSpace) retVal = super.jump2Space(mt, m, tgtSpace);
    else {
      sim.util.Int2D loc = grids.get(mt).getObjectLocation(m);
      ArrayList<MobileObject> coreSlot = getCoreAt(m,loc.y);
      if (ss.getCoreCapPerMobileObject() - coreSlot.size() >= 1.0) {
        coreSlot.add(m);
        retVal = true;
      }
    }
    return retVal;
  }

  @Override
  public void setProbV() {
    setProbV(SSParams.inner_forward_bias, SSParams.inner_lateral_bias);
  }
}
