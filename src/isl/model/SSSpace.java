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
import java.util.EnumMap;
import sim.field.grid.SparseGrid2D;
import isl.model.cell.Cell;

//public class SSSpace extends sim.field.grid.SparseGrid2D {
public class SSSpace {
  private static final long serialVersionUID = -4798219758652047850L;

  private static org.slf4j.Logger log = null;
  public void setLogger(org.slf4j.Logger logger) {
     log = logger;
  }

  int width=-Integer.MAX_VALUE, height=-Integer.MAX_VALUE;
  public MobileObjectType getMobileObjectType(MobileObject m) {
    MobileObjectType mt = ss.hepStruct.model.allMobileObject.stream().filter((t) -> (t.tag.equals(m.getTypeString()))).findFirst().get();  
    return mt;
  }
  /**
   * MobileObjectGrids ≡ 1 grid for each MobileObjectType we'll be moving around
   */
  public Map<MobileObjectType, SparseGrid2D> grids = new java.util.HashMap<>();
  public Map<String, EnumMap<Dir,Double>> probVs = new java.util.HashMap<>();
  
  public ArrayList<MobileObject> getAllObjects() {
    ArrayList<MobileObject> objects = new ArrayList<>();
    grids.values().forEach((g) -> { objects.addAll(g.allObjects); });
    return objects;
  }
  public int countAllMobileObject() {
    int count = 0;
    count = grids.values().stream().map((g) -> g.allObjects.numObjs).reduce(count, Integer::sum);
    return count;
  }
  public int countMobileObjectAt(MobileObjectType mt, int w, int h) {
    return grids.get(mt).numObjectsAtLocation(w, h);
  }
  public boolean putMobileObjectAt(MobileObject m, int w, int h) {
    return grids.get(getMobileObjectType(m)).setObjectLocation(m, w, h);
  }
  public sim.util.Int2D removeMobileObject(MobileObject m) {
    return (sim.util.Int2D) grids.get(getMobileObjectType(m)).remove(m);
  }
  

  /**
   * rxnProdCount -- contains the sum of all reaction products we were told
   * to count.
   */
  public sim.field.grid.IntGrid2D rxnProdCount = null;

  /**
   * Increments the rxnProdCount value for this X,Y
   * @param st String name of the MobileObject type -- ignored for now because which
   * types are counted is handled in the caller (ReactionHandler)
   * @param x X Position of the Hepatocyte reporting the reaction
   * @param y Y Position of the Hepatocyte reporting the reaction
   */
  public void incRxnProd(String st, int x, int y) {
    rxnProdCount.set(x, y, rxnProdCount.get(x,y)+1);
  }

  // MobileObject kept in main grid
  protected sim.field.grid.ObjectGrid2D celGrid = null;  // for cells
  public sim.field.grid.ObjectGrid2D getCellGrid() { return celGrid; }

  public SS ss = null;
  SSSpace inwardSpace = null;
  SSSpace outwardSpace = null;

  /**
   * allows us to p-randomly shuffle the order in which the inward, outward, and
   * moore movements execute
   */
  sim.util.Bag flowShuffler = null;

  public SSSpace(SS s, int w, int h, SSSpace in, SSSpace out) {
    //super(w, h);
    width = w; height = h;
    ss = s;
    ss.hepStruct.model.allMobileObject.forEach((st) -> {
      grids.put(st,new SparseGrid2D(w,h));
      probVs.put(st.tag,new EnumMap<Dir,Double>(Dir.class));
    });
    inwardSpace = in;
    outwardSpace = out;

    flowShuffler = new sim.util.Bag(3);
    if (out != null) setOutwardSpace(out);
    final SSSpace grid = this;
    flowShuffler.add(
            new Runnable() {
              // move around within the grid
              public void run() { moveMoore(ss); }
            });
    if (in != null) setInwardSpace(in);
  }

  public void setInwardSpace(SSSpace in) {
    inwardSpace = in;
    flowShuffler.add(new Runnable() {
              public void run() {
                // if we're middle or outer, allow inward movement
                if (inwardSpace != null) flowInOrOut(Dir.In);
              }
            });
  }
  public void setOutwardSpace(SSSpace out) {
    outwardSpace = out;
    flowShuffler.add(new Runnable() {
              public void run() {
                // if we're inner or middle, allow outward movement
                if (outwardSpace != null) flowInOrOut(Dir.Out);
              }
            });
  }

  public void flow () {
    flowShuffler.shuffle(ss.compRNG);
    for (Object o : flowShuffler) ((Runnable)o).run();
    /*
    // if we're inner or middle, allow outward movement
    if (outwardSpace != null) flowOutward();
    moveMoore(this, ss);
    // if we're middle or outer, allow inward movement
    if (inwardSpace != null) flowInward();
    */
  }

  void flowInOrOut(Dir d) {
    if (d != Dir.In && d != Dir.Out) throw new RuntimeException("Must flow In or Out.");
    ArrayList<MobileObject> jumped = new ArrayList<>();
    for (int wNdx = 0; wNdx < width; wNdx++) {
      for (int hNdx = 0; hNdx < height; hNdx++) {
        Cell c = (celGrid == null ? null : (Cell)celGrid.get(wNdx,hNdx));
        // shuffle the types so their movement isn't biased
        ArrayList<MobileObjectType> types = (ArrayList<MobileObjectType>)CollectionUtils.shuffle(ss.hepStruct.model.allMobileObject, ss.compRNG);
        for (MobileObjectType mt : types) {
          jumped.clear();
          SparseGrid2D grid = grids.get(mt);
          sim.util.Bag objects = grid.getObjectsAtLocation(wNdx, hNdx);
          if (objects != null) {  // if there are objects to move
//            if (mt.tag.equals("G")) log.debug("Flowing "+objects.numObjs+" G "+d);
            for (Object o : objects) {
              MobileObject m = (MobileObject) o;
              boolean isInOldCell = false;
              if (c == null || !(isInOldCell = c.listMobileObject().contains(m)) || c.canCross(m,Cell.Dir.OUT)) {
                double draw = ss.compRNG.nextDouble();
                // min for the draw changes depending on which dir we're going
                double min = probVs.get(m.getTypeString()).get((d == Dir.In ? Dir.Out : Dir.W));
                if (min <= draw
                    && draw < probVs.get(m.getTypeString()).get(d)) {
                  // might be more efficient to send the grid/MobileObjecttype along too
                  if ((d == Dir.Out ? jump2Space(mt, m, outwardSpace) : jump2Space(mt, m, inwardSpace))) {
//                    if (mt.tag.equals("G")) log.debug("Flowed "+s+" "+d);
                    jumped.add(m);
                    // remove the moving MobileObject from the cell's list
                    if (isInOldCell) c.remove(m);
                  }
                }
              } // end if (c == null || !c.bound.containsValue(s)) {
            }  // end for (Object o : objects) {
          } // no objects to move
          jumped.forEach((mo) -> { grid.remove(mo); });
        } // end for each MobileObjecttype/grid
      } // end - for (int hNdx = 0; hNdx < height; hNdx++) {
    } // end - for (int wNdx = 0; wNdx < width; wNdx++) {
  }
  
 /**
  * Jump2Space checks properties of destination and adds the MobileObject to the 
  * destination.  FlowInOrOut() checks source properties and removes it from
  * it's current location.
  * @param mt -- The typeString used for grid selection.
  * @param m -- The MobileObject to add.
  * @param tgtSpace -- The SSSpace to add it to.
  * @return 
  */
  boolean jump2Space(MobileObjectType mt, MobileObject m, SSSpace tgtSpace) {
    boolean retVal = false;
    SparseGrid2D srcGrid = grids.get(mt);
    SparseGrid2D tgtGrid = tgtSpace.grids.get(mt);
    sim.util.Int2D loc = srcGrid.getObjectLocation(m);
    Cell cell = (tgtSpace.celGrid == null ? null : (Cell) tgtSpace.celGrid.get(loc.x, loc.y));
    boolean isRoom = (tgtGrid.numObjectsAtLocation(loc.x,loc.y) < StrictMath.floor(ss.hepStruct.model.gridScale));
    boolean cellOK = true;
    if (isRoom) {
      if (cell != null) { // cell exists, query and add
        cellOK = cell.canCross(m, Cell.Dir.IN);
        if (cellOK)  cell.add(m);
      }
      if (cellOK) { // cell null or not, add to grid
        tgtGrid.setObjectLocation(m,loc);
        retVal = true;
      }
    }
    return retVal;
  }

  public enum Dir { E, W, Out, In, NE, N, NW, SW, S, SE };

  /**
   * Calculates the baseline vector of movement probabilities.
   * @param inSpace
   * @param outSpace
   * @param for_bias ∈ [0,1] amount by which movement is biased forward.
   * @param lateral_bias ∈ [0,1] 0 ⇒ 100% In, 1 ⇒ 100% Out
   * @return 
   */
  static EnumMap<Dir,Double> baseProbV(SSSpace inSpace, SSSpace outSpace, double for_bias, double lateral_bias) {
    EnumMap<Dir,Double> retVal = new EnumMap<>(Dir.class);
    int total_directions = Dir.values().length+1; // +1 to include staying put
    if (inSpace == null) total_directions--;
    if (outSpace == null) total_directions--;

    double base = 1.0/total_directions;
    retVal.put(Dir.E,base);
    retVal.put(Dir.W,base);
    
    if (inSpace == null && outSpace == null) {
      retVal.put(Dir.Out, 0.0); retVal.put(Dir.In, 0.0);
    } else if (inSpace == null && outSpace != null) {
      retVal.put(Dir.Out, base); retVal.put(Dir.In, 0.0);
    } else if (inSpace != null && outSpace == null) {
      retVal.put(Dir.Out, 0.0); retVal.put(Dir.In, base);
    } else { // inSpace != null && outSpace != null
      retVal.put(Dir.Out, lateral_bias*2.0*base); retVal.put(Dir.In, (1.0-lateral_bias)*2.0*base);
    }
    
    double northward = base*(1.0-for_bias);
    retVal.put(Dir.NE, northward);
    retVal.put(Dir.N, northward);
    retVal.put(Dir.NW, northward);
    
    double southward = base*(1.0+for_bias);
    retVal.put(Dir.SW, southward);
    retVal.put(Dir.S, southward);
    retVal.put(Dir.SE, southward);
    
    return retVal;
  }

  EnumMap<Dir,Double> applySpeedToProbV(EnumMap<Dir,Double> pv, double speed) {
    EnumMap<Dir, Double> retVal = new EnumMap<>(Dir.class);
    double sum = 0.0;
    sum = pv.values().stream().reduce(sum, Double::sum);
    if (sum > 1.0) throw new RuntimeException("base probV "+pv.getClass().getSimpleName()+" sums to "+sum);
    double p_stay_put = 1.0-sum;
    double total = pv.size();
    if (inwardSpace == null) total--;
    if (outwardSpace == null) total--;
    double p_to_add = p_stay_put/total*(1.0-1.0/speed);
    log.debug("Distributing "+(1.0-1.0/speed)*100.0+"% of "+p_stay_put+" over "+total+" other directions.");
    pv.entrySet().forEach((me) -> { 
      double p = me.getValue();
      retVal.put(me.getKey(), (p>0.0 ? p+p_to_add : p));
    });
    sum = 0.0;
    sum = retVal.values().stream().reduce(sum, Double::sum);
    if (sum > 1.0) throw new RuntimeException("modified probV "+retVal.getClass().getSimpleName()+" sums to "+sum);
    
    return retVal;
  }
  
  /**
   * Distributes the baseline probV so that a single pRNG draw selects one.
   * @param pv
   * @return 
   */
  static EnumMap<Dir,Double> normProbV(EnumMap<Dir,Double> pv) {
    EnumMap<Dir,Double> retVal = new EnumMap<>(Dir.class);
    double floor = 0.0;
    for (Map.Entry<Dir,Double> me : pv.entrySet()) {
      double ceiling = me.getValue()+floor;
      retVal.put(me.getKey(), ceiling);
      floor = ceiling;
    }
    return retVal;
  }
  
  protected void setProbV() {
    setProbV(SSParams.outer_forward_bias, SSParams.outer_lateral_bias);
  }
  public void setProbV(double spacefb, double spacelb) {
    for (Map.Entry<MobileObjectType,SparseGrid2D> me : grids.entrySet()) {
      MobileObjectType mt = me.getKey();
      // space's forward bias is the minimum, MobileObject's is a precentage of the rest
      double fB = (mt.hasProperty("forwardBias") 
              ? spacefb + ((double)mt.getProperty("forwardBias"))*(1.0 - spacefb)
              : spacefb);

      double lB = Double.NaN;
      double mobileObjectlb = Double.NaN;
      if (!mt.hasProperty("lateralBias") 
              // side-effect!
              || spacelb == (mobileObjectlb = (double)mt.getProperty("lateralBias"))) {
        lB = spacelb;
      } else { // ≠
        // both < ½ → ↑left; both > ½ → ↑right; opposite Δ/2
        // max and min work because we've handled if both = 0.5 already
        if ( Math.max(mobileObjectlb, spacelb) <= 0.5) {
          lB = mobileObjectlb*spacelb;
        } else if (Math.min(mobileObjectlb, spacelb) >= 0.5) {
          lB = 1.0 - (1.0 - mobileObjectlb)*(1.0 - spacelb);
        } else {
          lB = Math.min(mobileObjectlb,spacelb) + Math.max(mobileObjectlb,spacelb) - 0.5;
        }
      }
      
      EnumMap<Dir,Double> baseV = SSSpace.baseProbV(inwardSpace, outwardSpace, fB, lB);
      log.debug(this+":"+mt.tag+" grid: base probV("+fB+","+lB+") = "+describeProbV(baseV));
      EnumMap<Dir,Double> ssProbV = normProbV(baseV);
      log.debug(this+":"+mt.tag+" grid: normed probV = "+describeProbV(ssProbV));
      probVs.put(mt.tag,ssProbV);
    }
  }

/* debug for probV */
  String describeProbV(EnumMap<Dir,Double> pv) {
    StringBuilder output = new StringBuilder("{");
    java.util.Iterator<Map.Entry<Dir,Double>> i = pv.entrySet().iterator();
    while(i.hasNext()) {
      Map.Entry<Dir,Double> me = i.next();
      output.append(me.getKey()).append(" ⇒ ").append(me.getValue());
      output.append((i.hasNext() ? ", " : "}"));
    }
    double sum = 0.0;
    sum = pv.values().stream().reduce(sum, Double::sum);
    output.append(" Σ = ").append(sum);
    return output.toString();
  }

  public void moveMoore(SS ss) {
    for (Map.Entry<MobileObjectType,SparseGrid2D> me : grids.entrySet()) {
      
      SparseGrid2D grid = me.getValue();
      EnumMap<Dir,Double> probV = probVs.get(me.getKey().tag);
      /*
       * move MobileObject within the rim MobileObjects unchanged
       */
      ArrayList<MobileObject> exitingRim = new ArrayList<>();
      for (int wNdx=0 ; wNdx<grid.getWidth() ; wNdx++) {
        for (int hNdx=0 ; hNdx<grid.getHeight() ; hNdx++) {
          sim.util.Bag objects = grid.getObjectsAtLocation(wNdx,hNdx);
          // continue to next point if nothing's here
          if (objects != null) {
            // if celGrid == null, we're an innerspace
            Cell oldCell = (celGrid == null ? null : (Cell)celGrid.get(wNdx,hNdx));
            boolean isInOldCell = false;
            // try to move the MobileObject(m)
            for (Object o : objects) {
              // MobileObject bound inside cells or unable to partition out do not move
              MobileObject m = (MobileObject) o;
              if (oldCell == null || !(isInOldCell = oldCell.listMobileObject().contains(m)) || oldCell.canCross(m, Cell.Dir.OUT)) {

                sim.util.Int2D newLoc = null;
                sim.util.Int2D oldLoc = new sim.util.Int2D(wNdx,hNdx);
                double draw = ss.compRNG.nextDouble();

                if (0.0 <= draw
                        && draw < probV.get(SSSpace.Dir.E))
                  newLoc = new sim.util.Int2D(grid.tx(wNdx+1), hNdx);
                else if (probV.get(SSSpace.Dir.E) <= draw
                        && draw < probV.get(SSSpace.Dir.W))
                  newLoc = new sim.util.Int2D(grid.tx(wNdx-1), hNdx);
                else if (probV.get(SSSpace.Dir.In) <= draw
                        && draw < probV.get(SSSpace.Dir.NE))
                  newLoc = new sim.util.Int2D(grid.tx(wNdx+1), grid.ty(hNdx-1));
                else if (probV.get(SSSpace.Dir.NE) <= draw
                        && draw < probV.get(SSSpace.Dir.N))
                  newLoc = new sim.util.Int2D(wNdx, grid.ty(hNdx-1));
                else if (probV.get(SSSpace.Dir.N) <= draw
                        && draw < probV.get(SSSpace.Dir.NW))
                  newLoc = new sim.util.Int2D(grid.tx(wNdx-1), grid.ty(hNdx-1));
                else if (probV.get(SSSpace.Dir.NW) <= draw
                        && draw < probV.get(SSSpace.Dir.SW))
                  newLoc = new sim.util.Int2D(grid.tx(wNdx-1), hNdx+1);
                else if (probV.get(SSSpace.Dir.SW) <= draw
                        && draw < probV.get(SSSpace.Dir.S))
                  newLoc = new sim.util.Int2D(wNdx, hNdx+1);
                else if (probV.get(SSSpace.Dir.S) <= draw
                        && draw < probV.get(SSSpace.Dir.SE))
                  newLoc = new sim.util.Int2D(grid.tx(wNdx+1), hNdx+1);

                // else stay put

                // actually move the MobileObject(m)
                /** restrict movement to grid points with rejecting Cells */
                if (newLoc != null) {
                  if (newLoc.y < height) {
                    Cell newCell = (celGrid == null ? null : (Cell) celGrid.get(newLoc.x, newLoc.y));
                    boolean isRoom = (grid.numObjectsAtLocation(newLoc.x, newLoc.y) < StrictMath.floor(ss.hepStruct.model.gridScale));
                    boolean cellOK = true;
                    if (isRoom) {
                      //cellOK = (newCell == null ? true : newCell.accept(s));
                      if (newCell != null) { // newCell exists, query and add
                        cellOK = newCell.canCross(m, Cell.Dir.IN);
                        if (cellOK) newCell.add(m);
                      }
                      if (cellOK) { // newCell null ornot, add to the grid
                        grid.setObjectLocation(m, newLoc);
                        if (isInOldCell) oldCell.remove(m);
                      }
                    }
                  } else {
                    // only allow exiting from the InnerSpace
                    if (this instanceof isl.model.InnerSpace) exitingRim.add(m);
                  }
                } // end if (newLoc != null) {

              } // end  if (cell == null || (!cell.bound.containsValue(s) && membraneCrossing)) {
            } // end for (Object s : objects) {

            // before changing x,y handle those that exit
            ArrayList<MobileObject> exitedRim = ss.distribute(exitingRim, CompartmentType.GRID);
            if (exitedRim != null) {
              for (MobileObject m : exitedRim) {
              //log.debug(this+" "+s+" at "+grid.getObjectLocation(s)+" exitedRim from cell "+oldCell+" at <"+(oldCell != null ? oldCell.myX+", "+oldCell.myY : "null, null")+">");
              grid.remove(m);
              // remove from its old cell if it was in one
              if (isInOldCell) {
                oldCell.remove(m);
              }
            }
            exitingRim.clear();
          }

        } // end if (objects != null) {
      } // end - for (int hNdx=0 ; hNdx<height ; hNdx++)
    } // end - for (int wNdx=0 ; wNdx<width ; wNdx++)

    } // end loop over grids
  } // end moveMoore()

  public ArrayList<MobileObject> getMobileObjectAtY(int y) {
    ArrayList<MobileObject> retVal = new ArrayList<>();
    for (int x=0 ; x<width ; x++) {
      for (Map.Entry<MobileObjectType,SparseGrid2D> me : grids.entrySet()) {
        SparseGrid2D grid = me.getValue();
        sim.util.Bag b = grid.getObjectsAtLocation(x,y);
        if (b != null) retVal.addAll(b);
      }
    }
    return retVal;
  }
  
  public ArrayList<Cell> getCellsAtY(int y) {
    ArrayList<Cell> retVal = null;
    if (celGrid != null) {
      retVal = new ArrayList<>();
      for (int x=0 ; x<width ; x++) {
        Cell c = (Cell)celGrid.get(x,y);
        if ( c != null ) retVal.add(c);
      }
    }
    return retVal;
  }
}
