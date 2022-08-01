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
import java.util.HashMap;
import java.util.Map;
import isl.model.cell.KupfferCell;
import isl.model.cell.Hepatocyte;
import isl.model.cell.EC;
import isl.model.cell.Cell;

public class SS extends LiverNode implements LiverNodes {
  
  private static final long serialVersionUID = 2911232015318652318L;
  public static final CompartmentType BILE = new CompartmentType("QUEUE");
  public ArrayList<MobileObject> mobileObjects = new ArrayList<>();
  public ArrayList<MobileObject> getMobileObjects() { return mobileObjects; }
  @Override
  public Map<String,Number> getMobileObjectMap() { return CollectionUtils.countObjectsByType(mobileObjects); }
  public BileCanal bileCanal = null;
  public BileCanal getBileCanal() { return bileCanal; }
  InnerSpace innerSpace = null;
  public InnerSpace getInnerSpace() { return innerSpace; }
  public HepSpace hSpace = null;
  public HepSpace getHSpace() {return hSpace;}
  SSSpace eSpace = null;
  public SSSpace getESpace() {return eSpace;}
  public SSSpace sod = null;
  public SSSpace getSoD() {return sod;}

  public ArrayList<Cell> cells = new ArrayList<>();
  
  /**
   * collect the cells that have necrosed to make measurement more convenient
   */
  public Map<Hepatocyte,Double> necroticHepatocytes = new HashMap<>();
  public void necrotic(Hepatocyte h) {
    necroticHepatocytes.put(h,hepStruct.model.getTime());
  }
  
  /**
   * collect the cells that have been triggered for necrosis
   */
  public HashMap<Hepatocyte,Double> necTrigCells = new HashMap<>();
  public void necTrig(Hepatocyte h) { necTrigCells.put(h,hepStruct.model.getTime()); }
  
  public int layer = -1;
  public int getLayer() {return layer;}
  public int circ = 5, length = 10;
  public int getCirc() {return circ;} public int getLength() {return length;}
  
  public int ei_thresh = -Integer.MAX_VALUE;
  public double ei_rate = Double.NaN;
  public double ei_response_factor = Double.NaN;
  public int el_thresh = -Integer.MAX_VALUE;
  public double el_rate = Double.NaN;
  public double el_response_factor = Double.NaN;

  public SS ( ec.util.MersenneTwisterFast r, HepStruct l, ec.util.ParameterDatabase pd ) {
    super(r, l);
    SSParams.loadParams(this,pd);
  }
  public void setGeometry(int c, int l) {
    if (c < 0) throw new RuntimeException("SS circumference must be > 0");
    circ = c;
    if (l < 0) throw new RuntimeException("SS length must be > 0");
    length = l;
    innerSpace = new InnerSpace(this, circ, length, SSParams.flowRate);
    innerSpace.setLogger(log);
    bileCanal = new BileCanal(this, circ, length);

    // construct the spaces in concentric order
    ArrayList<SSSpace> space_array = new ArrayList<>();
    space_array.add(innerSpace);
      
    if (SSParams.ecdens > 0.0) {
      eSpace = new SSSpace(this, circ, length, null, null);
      //log.debug("SS.setGeometry() - eSpace = "+eSpace);
      eSpace.setLogger(log);
      space_array.add(eSpace);
    }
    if (SSParams.useSoD) {
      sod = new SSSpace(this, circ, length, null, null);
      sod.setLogger(log);
      space_array.add(sod);
    }
    // create the hepSpace without the sod first
    hSpace = new HepSpace(this, circ, length, null);
    hSpace.setLogger(log);
    space_array.add(hSpace);

    // now hook them up in the order they were constructed
    for (int gNdx=1 ; gNdx<space_array.size() ; gNdx++) {
      space_array.get(gNdx-1).setOutwardSpace(space_array.get(gNdx));
      space_array.get(gNdx).setInwardSpace(space_array.get(gNdx-1));
      if (gNdx < space_array.size()-1) space_array.get(gNdx).setOutwardSpace(space_array.get(gNdx+1));
    }

    // can't setProbV until they're wired together
    innerSpace.setProbV();
    if (eSpace != null) eSpace.setProbV();
    if (sod != null) sod.setProbV();
    hSpace.setProbV();
  }
  
  public void fillSpaces() {
    assert(priorPathLength != -Integer.MAX_VALUE
            && postPathLength != -Integer.MAX_VALUE);
    
    int cellCount = 0;
    if (eSpace != null) {
      eSpace.celGrid = new sim.field.grid.ObjectGrid2D(circ, length);
      int numECells = Math.round(circ*length*SSParams.ecdens);
      int numKCells = Math.round(circ*length*SSParams.kcdens);
      int totalEKCells = Math.round(circ*length*(SSParams.ecdens + SSParams.kcdens));
      //Due to potential rounding errors when ecdens + kcdens = 1.0, numKCells + numECells may be > totalEKCells,
      ////which may exceed the number of available grid spaces.
      ////To prevent the following do-while loop from never ending, use totalEKCells.
      do {
        int x = compRNG.nextInt(circ);
        int y = compRNG.nextInt(length);
        if (eSpace.celGrid.get(x, y) == null) {
          if(cellCount < numECells) {
              EC cell = new EC(eSpace, compRNG);
              cell.setLoc(x, y);
              cells.add(cell);
              eSpace.celGrid.set(x,y,cell);
          } else {
              KupfferCell cell = new KupfferCell(eSpace, compRNG, x, y);
              cells.add(cell);
              eSpace.celGrid.set(x,y,cell);
          }
          cellCount++;
        }
      } while (cellCount < totalEKCells );
    } // end if (eSpace != null)
    
    hSpace.celGrid = new sim.field.grid.ObjectGrid2D(circ, length);
    int numHCells = Math.round(circ*length*SSParams.hepdens);
    cellCount = 0;
    do {
      int x = compRNG.nextInt(circ);
      int y = compRNG.nextInt(length);
      if (hSpace.celGrid.get(x,y) == null) {
        Hepatocyte h = new Hepatocyte(hSpace, compRNG, x, y);
        //h.setLoc(x, y);
        cells.add(h);
        hSpace.celGrid.set(x,y, h);
        cellCount++;
      }
    } while (cellCount < numHCells);
  }

  @Override
  public void stepPhysics () {
    innerSpace.flow();
    double fr_f = Math.floor(SSParams.flowRate);
    int fr_i = (int) fr_f;
    if (compRNG.nextDouble() < (SSParams.flowRate-fr_f)) fr_i++;
    bileCanal.flow(fr_i);
    if (eSpace != null) eSpace.flow();
    if (sod != null) sod.flow();
    hSpace.flow();
  }

  @Override
  public void stepBioChem () {
    ArrayList<Cell> shuffledCells = (ArrayList<Cell>) bsg.util.CollectionUtils.shuffle(cells, compRNG);
    shuffledCells.forEach((c) -> { c.iterate(); });
  }

  /**
   * Polymorphic with LiverNode.distribute().  Like LiverNode's, the MobileObjects are
   * moved after this call.
   * @param m list of mobileobject to transfer
   * @param c the type of source compartment
   * @return 
   */
   protected ArrayList<MobileObject> distribute(ArrayList<MobileObject> m, CompartmentType c) {
     
     ArrayList<MobileObject> totalMoved = null;
     if (m != null && m.size() > 0) {
       totalMoved = new ArrayList<>();
       // handle fan[in|out]Weights
       sim.util.Bag tmpNodes = null;
       java.util.HashMap<LiverNode, Double> weights = null;
       if (c == SS.BILE) {
         if (faninWeights == null)
           faninWeights = computeDistWeights(SSSpace.Dir.N);
         weights = faninWeights;
         tmpNodes = new sim.util.Bag(inNodes);
       } else {
         if (fanoutWeights == null)
           fanoutWeights = computeDistWeights(SSSpace.Dir.S);
         weights = fanoutWeights;
         tmpNodes = new sim.util.Bag(outNodes);
       }
       tmpNodes.shuffle(hepStruct.hepStructRNG);
       
       for (Object o : tmpNodes) {
         LiverNode n = (LiverNode) o;
         ArrayList<MobileObject> moved = null;
         double ratio = weights.get(n);
         long numToPush = StrictMath.round(ratio * m.size());
         // min = 1 since distWeights might be too small and leave mobileobject forever
         // if numToPush is too small, just try to push them all
         if (numToPush <= 0) numToPush = m.size();
         moved = push(numToPush, m, n, c);
         //if (id == 0)
         //  log.debug("LN:"+id+ " moved " + moved.size() + "/" + numToPush + " to compartment " + c + " of " + n.id);
         totalMoved.addAll(moved);
       }
     }

     return totalMoved;
   }
   
   /**
    * Polymorphic with LiverNode.push().  This method modifies the list and
    * removes the MobileObject from the SS.mobileObjects.
    * @param number
    * @param mobileObjectList
    * @param n
    * @param c
    * @return 
    */
   protected ArrayList<MobileObject> push(long number, ArrayList<MobileObject> mobileObjectList, LiverNode n, CompartmentType c) {
      ArrayList<MobileObject> placed = new ArrayList<>();
      for ( int sNdx=0 ; (sNdx < mobileObjectList.size()) && (placed.size() < number) ; sNdx++ ) {
         MobileObject mobileObject = mobileObjectList.get(sNdx);
         int tries = 0;
         if (n.accept(mobileObject, c)) placed.add(mobileObject);
      }
      placed.stream().forEach((m) -> {
        mobileObjectList.remove(m);
        mobileObjects.remove(m);
      });
      return placed;
   }


   
   
  /**
   * just in case we want to cache the CC
   * @return 
   */
  @Override
  public double getInletCapPerMobileObject () { return calcInletCapPerMobileObject(); }
  /**
   * calculates the inlet capacity, currently the area of the inlet
   * @return 
   */
  public double calcInletCapPerMobileObject () {
    // use the area of the inlet circle
//    return bsg.util.MathUtils.area(circ) * hepStruct.model.gridScale;
    return getCoreCapPerMobileObject() + circ * hepStruct.model.gridScale;
  }

  /**
   * calculates the effective volume of the entire SS
   * @return 
   */
  public double getWholeCapPerMobileObject () { return getInletCapPerMobileObject()*length; }
  public double getCoreCapPerMobileObject() { 
    return  circ*circ/(4*Math.PI) // area as function of circumference
            * hepStruct.model.coreScale; }
  
  boolean tryCore(MobileObject m) {
    boolean retVal = false;
    ArrayList<MobileObject> inlet = innerSpace.getCoreAt(m,0);
    if (getCoreCapPerMobileObject() - inlet.size() >= 1.0) {
      inlet.add(m);
      retVal = true;
    }
    return retVal;
  }
  
  boolean tryRim(MobileObject m) {
    int randX = compRNG.nextInt(circ);
    boolean retVal = false;
    for (int xNdx = 0; xNdx < circ; xNdx++) {
      if (innerSpace.countMobileObjectAt(innerSpace.getMobileObjectType(m), ((xNdx + randX) % circ), 0) < hepStruct.model.gridScale) {
        innerSpace.putMobileObjectAt(m, ((xNdx + randX) % circ), 0);
        retVal = true;
        break;
      }
    }
    return retVal;
  }
  
  @Override
  public boolean accept(MobileObject m, CompartmentType c) {
    boolean retVal = false;
    if (c == CompartmentType.GRID) {

      // find an empty place in the 1st grid or core, depending on the
      // mobile object particle's core2Rim ratio.
      double draw = compRNG.nextDouble();
      double c2r = (m.hasProperty("core2Rim") ? ((Double)m.getProperty("core2Rim")) : 0.5d);
      if (draw < c2r) {
        retVal = tryCore(m);
        if (!retVal) retVal = tryRim(m);
      } else {
        retVal = tryRim(m);
        if (!retVal) retVal = tryCore(m);
      }
    } else if (c == SS.BILE) {
      int last = bileCanal.tube.length-1;
      if (bileCanal.getCC() - bileCanal.tube[last].size() > 1) {
        bileCanal.tube[last].add(m);
        retVal = true;
      }
    }
    // if it was accepted, add it to my mobile object's list
    if (retVal) {
      if (mobileObjects == null) {
        mobileObjects = new ArrayList<MobileObject>();
      }
      mobileObjects.add(m);
    }
    return retVal;
  }
  
  @Override
  public String describe () {
    int coreNum = innerSpace.getCoreCount();
    // count the bound mobile object in the ECs and Hepatocytes
    int totalECMobileObjectCount = 0; int totalHMobileObjectCount = 0;
    int boundECMobileObjectCount = 0; int boundHMobileObjectCount = 0;
    ArrayList<MobileObject> ecMobileObject = new ArrayList<>();
    for (Object o : cells) {
      if (o instanceof EC) {
        java.util.List<MobileObject> mo = ((EC)o).listMobileObject();
        ecMobileObject.addAll(mo);
        totalECMobileObjectCount += ((EC)o).listMobileObject().size();
        boundECMobileObjectCount += ((EC)o).getAllBoundMobileObjects().size();
      }
      if (o instanceof Hepatocyte) {
        totalHMobileObjectCount += ((Hepatocyte)o).listMobileObject().size();
        boundHMobileObjectCount += ((Hepatocyte)o).getAllBoundMobileObjects().size();
      }
    }
    int totalBileMobileObjectCount = 0;
    for (ArrayList<MobileObject> tube : bileCanal.tube) totalBileMobileObjectCount += tube.size();

    StringBuilder sb = new StringBuilder();
    sb.append( "SS:" ).append( id ).append( " (layer=" ).append(layer ).append( ", circ=" )
        .append( circ ).append( ", length=" ).append( length ).append( ", mobileobjectss=" )
        .append( (mobileObjects == null ? "[EMPTY]" : mobileObjects.size()) )
            .append(", core=").append(coreNum)
            .append(", rim=").append(CollectionUtils.describe(CollectionUtils.countObjectsByType(innerSpace.getAllObjects())))
            .append(", es=").append((eSpace != null ? CollectionUtils.describe(CollectionUtils.countObjectsByType(eSpace.getAllObjects())) : "<null>"))
            .append(", es.intra=").append(CollectionUtils.describe(CollectionUtils.countObjectsByType(ecMobileObject)))
            .append(", ds=").append((sod != null ? CollectionUtils.describe(CollectionUtils.countObjectsByType(sod.getAllObjects())) : "<null>"))
            .append(", hs=").append(CollectionUtils.describe(CollectionUtils.countObjectsByType(hSpace.getAllObjects())))
            .append(", last(hs).mobileObjects=").append(hSpace.getMobileObjectAtY(hSpace.height-1).size())
            .append(", hs.intra=").append(totalHMobileObjectCount)
            .append(", last(hs).cells=").append(hSpace.getCellsAtY(hSpace.height-1).size())
            .append(", bile=").append(totalBileMobileObjectCount)
            .append(", gshUpEliminated=").append(gshUpEliminated)
            .append("\n" )
         ;
    // below is helpful for determining where a MobileObject is stuck
    //sb.append("\tTotal MobileObjects = ").append(CollectionUtils.describe(CollectionUtils.countTypes(mobileObjects))).append("\n");
    return sb.toString();
  }
}
