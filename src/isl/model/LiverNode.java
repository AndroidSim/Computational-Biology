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
import bsg.util.MutableInt;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public strictfp abstract class LiverNode extends Compartment implements LiverNodes {
  private static final long serialVersionUID = 7917013348869340551L;
   public int id = -1;
   public HepStruct hepStruct = null;
   public int gshUpEliminated = 0;
   ArrayList<LiverNode> outNodes = null; // convenient holders for nodes
   ArrayList<LiverNode> inNodes = null;
   LinkedHashMap<LiverNode, Double> faninWeights = null;
   LinkedHashMap<LiverNode, Double> fanoutWeights = null;
   public int priorPathLength = -Integer.MAX_VALUE; // length in grid points from PV to my inlet
   public int postPathLength = -Integer.MAX_VALUE; // length in grid points from outlet to CV
   
   static org.slf4j.Logger log = null;
   public void setLogger(org.slf4j.Logger logger) {
     log = logger;
  }
   
   /** Creates a new instance of LiverNode */
   public LiverNode(ec.util.MersenneTwisterFast r, HepStruct l) {
     super(r);
     if ( l!=null ) hepStruct = l;
     else throw new RuntimeException("LiverNode: Parent hepStruct can't be null.");
   }
   
   public void setID(int i) {
      if ( i>=0 ) id = i;
      else throw new RuntimeException("LiverNode.setID(" + i + "): ID can't be negative.");
   }
   public int getID() { return id; }
   
  @Override
   public void step(sim.engine.SimState state) {
      stepPhysics();
      stepBioChem();
   }
   
  @Override
   public abstract void stepPhysics();

   /**
    * attempts to distribute the passed mobile object to the output nodes
    * those that it can move go into the return structure and are
    * removed from the array passed in
    * @param s
    * @return the mobile object we are moving
    */
   protected ArrayList<MobileObject> distribute(Map<String,Number> mm, CompartmentType c) {
     long mobileObject_map_size = CollectionUtils.sum_mi(mm);
     ArrayList<MobileObject> totalMoved = null;
     if (mm != null && mobileObject_map_size > 0) {
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
         mobileObject_map_size = CollectionUtils.sum_mi(mm); // recomputed as pool Δs
         long numToPush = (long) StrictMath.ceil(ratio*mobileObject_map_size);
         // min = 1 since distWeights might be too small and leave mobile object forever
         // if numToPush is too small, just try to push them all
         if (numToPush <= 0) numToPush = mobileObject_map_size;
         moved = push(numToPush, mm, n, c);
         //if (id == 0)
         //  log.debug("LN:"+id+ " moved " + moved.size() + "/" + numToPush + " to compartment " + c + " of " + n.id);
         totalMoved.addAll(moved);
       }
     }

     return totalMoved;
   }
   
   protected ArrayList<MobileObject> push(long number, Map<String,Number> mm, LiverNode n, CompartmentType c) {
     long mobileObject_map_size = CollectionUtils.sum_mi(mm);
      ArrayList<MobileObject> placed = new ArrayList<>();

      // push each typeString according to their ratios of the total      
      for (Map.Entry<String,Number> me : mm.entrySet()) {
        MobileObjectType de = hepStruct.model.allMobileObject.stream().filter((o) -> (o.tag.equals(me.getKey()))).findAny().get();
        long num_this_type = (long) StrictMath.ceil(number*me.getValue().doubleValue()/mobileObject_map_size);
        for (int count=0 ; count<num_this_type ; count++) {
          MobileObject mobileObject = new MobileObject(de);
          mobileObject.setProperties(de.properties);
          if (n.accept(mobileObject, c)) {
            placed.add(mobileObject);
           ((MutableInt)mm.get(mobileObject.getTypeString())).sub(1); // remove one from the pool
          } else break; // because that tgt node is full
        }
      }
      
      
      
      return placed;
   }

  @Override
   public abstract void stepBioChem();

  /**
   * @return Calculate/Return the amount of mobile object that can enter as if empty
   */
  @Override
   public abstract double getInletCapPerMobileObject();
   public abstract boolean accept(MobileObject m, CompartmentType c);

   protected LinkedHashMap<LiverNode, Double> computeDistWeights(SSSpace.Dir dir) {
      double totalInletCap = 0.0F;
      // first get the total CC and initialize [in|out]Nodes
      ArrayList<LiverNode> tgtNodes = (dir == SSSpace.Dir.S ? outNodes : inNodes);
      if ( tgtNodes==null ) {
         sim.util.Bag edges = (dir == SSSpace.Dir.S ? hepStruct.getEdgesOut(this) : hepStruct.getEdgesIn(this));
         tgtNodes = new ArrayList<>(edges.numObjs);
         for (Object e : edges) {
            LiverNode ln = (LiverNode) (dir == SSSpace.Dir.S
                    ? ((LiverEdge)e).to()
                    : ((LiverEdge)e).from());
            tgtNodes.add(ln);
            totalInletCap += ln.getInletCapPerMobileObject();
         }
         if (dir == SSSpace.Dir.S) outNodes = tgtNodes;
         else inNodes = tgtNodes;
      } else {
         for (LiverNode ln : tgtNodes) totalInletCap += ln.getInletCapPerMobileObject();
      }
      LinkedHashMap<LiverNode, Double> retVal = new LinkedHashMap<>(tgtNodes.size());
      for (Object n : tgtNodes) {
         LiverNode ln = (LiverNode) n;
         retVal.put(ln, ln.getInletCapPerMobileObject()/totalInletCap);
      }
      return retVal;
   }
   /**
    * @param tgt
    * @return
    */
   public boolean linksTo(LiverNode tgt) {
     boolean retVal = false;
     for (Object e : hepStruct.getEdgesOut(this)) {
       LiverEdge le = (LiverEdge)e;
       if (tgt.equals(le.to())) {
               retVal = true;
               break;
       }
     }
     return retVal;
   }
  
  /**
   * Recursively checks downstream nodes to see if they eventually lead
   * back to this node, which would indicate a cycle in the graph.
   * @param seen
   */
  public boolean isCycle(sim.util.Bag seen) {
    boolean retVal = false;
    if (seen.contains(this)) return true;
    seen.add(this);
    sim.util.Bag outlets = hepStruct.getEdgesOut(this);
    for (int oNdx=0 ; oNdx<outlets.numObjs ; oNdx++) {
      LiverNode tgt = (LiverNode)((LiverEdge) outlets.objs[oNdx]).getTo();
      if (seen.contains(tgt)) return true;
      else {
        retVal = tgt.isCycle(seen);
        if (retVal) return true;
	//log.debug("LN:"+id+".isCycle() -- LN:"+tgt.id+".isCycle() ⇒ false.");
      }
    }
    return retVal;
  }

  public double calculatedCV() {
    //log.debug("LN:"+id+".calculatedCV() -- begin.");
    double retVal = Double.NaN;
    // return my length + the avg of my outlets
    sim.util.Bag outlets = hepStruct.getEdgesOut(this);
    int outletSum = 0;
    for (int oNdx=0 ; oNdx<outlets.numObjs ; oNdx++) {
      LiverNode ln = (LiverNode)((LiverEdge) outlets.objs[oNdx]).getTo();
      //log.debug("LN:"+id+".calculatedCV() -- calling on LN:"+ln.id);
      outletSum += ln.calculatedCV();
      if (ln instanceof SS) outletSum += ((SS)ln).length;
    }
    retVal = outletSum/outlets.numObjs;
    //log.debug("LN:"+id+".calculatedCV() -- end.");
    return retVal;
  }
   
  public double calculatedPV() {
    double retVal = Double.NaN;
    // return my length + the avg of my inlets
    sim.util.Bag inlets = hepStruct.getEdgesIn(this);
    int inletSum = 0;
    for (int iNdx=0 ; iNdx<inlets.numObjs ; iNdx++) {
      LiverNode ln = (LiverNode)((LiverEdge) inlets.objs[iNdx]).getFrom();
      inletSum += ln.calculatedPV();
      if (ln instanceof SS) inletSum += ((SS)ln).length;
    }
    retVal = inletSum/inlets.numObjs;
    return retVal;
  }
   
}
