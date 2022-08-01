/*
 * Copyright 2003-2020 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model.cell;

import isl.model.HepStruct;
import java.util.ArrayList;
import isl.model.ISL;
import isl.model.MobileObjectType;

public class NecrosisHandler implements Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NecrosisHandler.class );

    Hepatocyte cell = null;  // only Hepatocytes necrose so far
    static sim.util.Double2D stressRange = null;
    public static void setStressRange(sim.util.Double2D range) { stressRange = range; }

    // preliminary stub for necrosis tipping point hypothesis
    private final double DIGESTED_MATERIAL = 1.0;
    // MobileObject types that have the "causesNecrosis" property
    static java.util.ArrayList<String> stressorTypes = null;
    static java.util.ArrayList<String> determineNecrosisTypes(java.util.ArrayList<MobileObjectType> bolusEntries) {
      java.util.ArrayList<String> nt = new java.util.ArrayList<>();
      for (MobileObjectType be : bolusEntries) {
        if (be.properties.containsKey("causesNecrosis") && ((Boolean)be.properties.get("causesNecrosis"))) nt.add(be.tag);
      }
      return nt;
    }
    // MobileObject types with the "inhibitsNecrosis" property
    static java.util.Map<String,Number> inhTypes = null;
    static java.util.Map<String,Number> determineNecrosisInhTypes(java.util.ArrayList<MobileObjectType> bolusEntries) {
      java.util.HashMap<String,Number> it = new java.util.HashMap<>();
      for (MobileObjectType be : bolusEntries) {
        if (be.properties.containsKey("inhibitsNecrosis") && ((Boolean)be.properties.get("inhibitsNecrosis"))) {
          if (be.properties.containsKey("inhibitionPotency")) 
            it.put(be.tag, ((Double)be.properties.get("inhibitionPotency")));
          else
            throw new RuntimeException(be.tag+" inhibitsNecrosis but has no associated potency.");
        }
      }
      return it;
    }
    
    public static int NECROSIS_DELAY_MIN = Integer.MAX_VALUE;
    public static int NECROSIS_DELAY_MAX = -Integer.MAX_VALUE;
    double stress_thresh = Double.NaN;
    public sim.engine.TentativeStep necrosis_stop = null;
    
    public NecrosisHandler(Hepatocyte c) {
        cell = c;
        // set the necrosis threshold based on an inverse map PV-CV position
        //int dist_pv = cell.getDPV(false);
        //stress_thresh = bsg.util.LinearGradient.eval(stressRange.x, stressRange.y, 0.0, (double)(dist_pv+cell.getDCV(false)),(double)dist_pv);
        //double ostress_thresh = cell.mySpace.ss.hepStruct.evalGradient(HepStruct.GradType.Linear, stressRange.x, stressRange.y, cell.getDPV(false), cell.getDCV(false));
        double UGs = cell.mySpace.ss.hepStruct.uniGradrange.x;
        double UGf = cell.mySpace.ss.hepStruct.uniGradrange.y;
        stress_thresh = cell.mySpace.ss.hepStruct.evalGradientfromUniG(HepStruct.GradType.Linear,stressRange.x,stressRange.y,1,1,cell.uniGradientvalue,UGs,UGf);
        //log.info("UniGradient: stress threshold: H:"+cell.id+" ostress_thresh = "+ostress_thresh+" stress_thresh = "+stress_thresh+"\n");
        java.util.ArrayList<MobileObjectType> be = cell.getMobileObjectTypes();
        if (stressorTypes == null) 
          stressorTypes = determineNecrosisTypes(cell.getMobileObjectTypes());
        if (inhTypes == null)
          inhTypes = determineNecrosisInhTypes(be);
    }
    private int getStressorCount() {
      int sum = 0;
      sum = stressorTypes.stream().map(
          (t) -> bsg.util.CollectionUtils.countObjectsOfType(cell.listMobileObject(), t))
          .reduce(sum, Integer::sum);
      return sum;
    }
    /**
     * @param types A Map from inhibiting type => inhibition potency
     * @return null || a Map from |inhibiting type| => inhibition potency
     */
    private java.util.Map<Number,Number> getInhibitorCount() { 
      java.util.Map<Number,Number> retVal = null;
      int sum = 0;
      for (java.util.Map.Entry<String,Number> me : inhTypes.entrySet()) {
        sum += bsg.util.CollectionUtils.countObjectsOfType(cell.listMobileObject(),me.getKey());
        if (sum > 0) {
          if (retVal == null) retVal = new java.util.HashMap<>(1);
          retVal.put(sum, me.getValue());
        }
      }
      return retVal;
    }

    public static int manhattanDist = Integer.MAX_VALUE;
    public static boolean vnNeighborhoods = Boolean.TRUE;
    public static int downstreamCount = Integer.MAX_VALUE;
    public static int latNeighbors = Integer.MAX_VALUE;
    
    public static boolean exos = Boolean.TRUE;
    
    public boolean isCellStressed() {
      return (getStressorCount() > stress_thresh);
    }
    
    private boolean downStreamStatus(boolean adj, ArrayList<Hepatocyte> neighbors) {
      int necTrigNeighborsDownstream = 0;

      necTrigNeighborsDownstream = 0;
      necTrigNeighborsDownstream = neighbors.stream()
              .filter((h) -> (h.nh.necrosis_stop != null))
              .filter((h) -> (h.myY > cell.myY))
              .map((_item) -> 1)
              .reduce(necTrigNeighborsDownstream, Integer::sum); // have they triggered?
      // are they downstream of me?
      return (adj || necTrigNeighborsDownstream >= downstreamCount);
    }
    
    private boolean latStatus(ArrayList<Hepatocyte> neighbors) {
      int latCount = 0;
      latCount = neighbors.stream()
              .filter((h) -> (h.nh.necrosis_stop != null // have they triggered?
                      && h.myY == cell.myY   // are they lateral to me?
                      && Math.abs(h.myX - cell.myX) <= latNeighbors/2 // are they close enough?
                      ))
              .map((_item) -> 1)
              .reduce(latCount, Integer::sum);
      return (latCount >= latNeighbors);
    }
    
    public ArrayList<Hepatocyte> getNeighbors(boolean wrapY, int x, int y, int dist, boolean includeOrigin) {
      sim.util.Bag result = new sim.util.Bag();
      sim.util.IntBag xPos = new sim.util.IntBag();
      sim.util.IntBag yPos = new sim.util.IntBag();
      ArrayList<Hepatocyte> neighbors = null;
      ArrayList<Hepatocyte> mNeighbors = null;
      
      if(vnNeighborhoods) cell.mySpace.getCellGrid().getVonNeumannNeighbors(x, y, dist, sim.field.grid.Grid2D.TOROIDAL, includeOrigin, result, xPos, yPos);
      else cell.mySpace.getCellGrid().getMooreNeighbors(x, y, dist, sim.field.grid.Grid2D.TOROIDAL, false, result, xPos, yPos);
      mNeighbors = new ArrayList<>(result);
      
      if(wrapY) {
        neighbors = new ArrayList<>(result);
      } else {
        neighbors = new ArrayList<>();
        for(Hepatocyte h: mNeighbors) {
          if(Math.abs(y-h.myY) <= dist) 
            neighbors.add(h);
        }
      }
      return neighbors;
    }
    
    public boolean shouldCellNecrose() {
      boolean retVal = false;
            
      ArrayList<Hepatocyte> neighbors = getNeighbors(false, cell.myX, cell.myY, manhattanDist, false);

      if (isCellStressed()) {
        retVal = downStreamStatus(cell.adjCV, neighbors);
      } else retVal = latStatus(neighbors);
      
      return retVal;
    }
    
    @Override
    public void run() {

      if (exos) { //stressed vHPCs release an EXO
        if (necrosis_stop == null && isCellStressed()) {
          scheduleNecrosis();
          cell.createEXO("Necrosis Initiated at " + cell.myX + "," + cell.myY + " for cell " + cell.id);
        }
      } else { //neighborhood-based events
        if (necrosis_stop == null && shouldCellNecrose())
          scheduleNecrosis();
      }
                  
      if (necrosis_stop != null) {
        java.util.Map<Number, Number> inh_counts = getInhibitorCount();
        if (inh_counts != null && !inh_counts.isEmpty()) {
          double numerator = 0; // ∑(|inhibitor|*potency)
          for (java.util.Map.Entry<Number, Number> me : inh_counts.entrySet()) {
            numerator += me.getKey().doubleValue() * me.getValue().doubleValue();
          }
          double nec_inh_prob = numerator / (getStressorCount() + DIGESTED_MATERIAL);
          if (cell.cellRNG.nextDouble() <= nec_inh_prob) {
            //log.debug("NecrosisHandler.run() cycle = "+cell.parent.hepStruct.model.getCycle()+", removing necrosis event for cell " + cell.id);
            necrosis_stop.stop();  // remove it from the schedule
            necrosis_stop = null;  // forget it
          }
        }

      }
    }
    
    public void scheduleNecrosis() {
      long cycle = cell.mySpace.ss.hepStruct.model.getCycle();

      // Beta distribution
      //isl.util.PRNGWrapper prngw = new isl.util.PRNGWrapper(cell.cellRNG);
      //double beta_a = 2;
      //double beta_b = 2;
      //cern.jet.random.Beta betaDist = new cern.jet.random.Beta(beta_a, beta_b, prngw);
      //long sched = (long)(NECROSIS_DELAY_MIN + betaDist.nextDouble()*(NECROSIS_DELAY_MAX - NECROSIS_DELAY_MIN));
      
      // Gaussian distribution:
      //double mean = (NECROSIS_DELAY_MIN + NECROSIS_DELAY_MAX) / 2;
      //double std = 3024;
      //long sched = (long) (cell.cellRNG.nextGaussian() * std + mean);
      //if (sched < 0) {
      //  sched = StrictMath.abs(sched);
      //}

      //log.debug("NecrosisHandler.scheduleNecrosis() for cell "+cell.id+" at cycle "+Long.toString(cycle+sched));

      // Uniform distribution:
      long sched = NECROSIS_DELAY_MIN + cell.cellRNG.nextInt(NECROSIS_DELAY_MAX-NECROSIS_DELAY_MIN);
      //log.debug("Scheduling cell.necrose() at cycle = "+(cycle+sched)+", getTime() => "+cell.parent.hepStruct.model.parent.schedule.getTime());
      
      sim.engine.Steppable necrosis_step = (sim.engine.SimState state) -> { necrosisEvent(); };
      sim.engine.Schedule schedule = cell.mySpace.ss.hepStruct.model.parent.schedule;
      necrosis_stop = new sim.engine.TentativeStep(necrosis_step);
      boolean success = schedule.scheduleOnce(cycle + sched, ISL.action_order+1, necrosis_stop);
      if (success) cell.mySpace.ss.necTrig(cell);
      if (!success) throw new RuntimeException("Failed to schedule a necrosis event for cell "+cell.id);

      //log.debug("NecrosisHandler:"+cell.id+".scheduleNecrosis() at "+cycle+" for "+cycle+sched);
    } // end scheduleNecrosis();
  
    
    void necrosisEvent() {
      //log.debug("NecrosisHandler:"+cell.id+".necrosisEvent()");
      cell.necrose();
    }
}
