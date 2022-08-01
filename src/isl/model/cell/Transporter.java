/*
 * Copyright 2003-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model.cell;

import isl.model.ISL;
import isl.model.MobileObject;
import isl.model.MobileObjectType;

/**
 *
 * @author gepr, aks(2018)
 */
public class Transporter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NecrosisHandler.class );
    
    public Hepatocyte cell = null;  // only Hepatocytes transport MobileObjects for now
    public MobileObjectType transportedMobileObjectType = null;
    //public MobileObject transportedMobileObject = null;
    //public long transportTime = 5000;
    public static int TRANSPORT_DELAY_MIN = Integer.MAX_VALUE;
    public static int TRANSPORT_DELAY_MAX = -Integer.MAX_VALUE;
    
    /** Creates a new instance of Transporter */
    public Transporter(Hepatocyte c) {
        cell = c;
        java.util.ArrayList<MobileObjectType> be = cell.getMobileObjectTypes();
        if (membDamageTypes == null) { 
            membDamageTypes = determineMembDamageTypes(cell.getMobileObjectTypes());
        }
    }
    
    // MobileObject types that have the "membraneDamage" property
    public java.util.ArrayList<String> membDamageTypes = null;
    public java.util.ArrayList<String> determineMembDamageTypes(java.util.ArrayList<MobileObjectType> bolusEntries) {
      java.util.ArrayList<String> mdt = new java.util.ArrayList<>();
      for (MobileObjectType be : bolusEntries) {
        if (be.properties.containsKey("membraneDamage") && ((Boolean)be.properties.get("membraneDamage"))) mdt.add(be.tag);
      }
      return mdt;
    }
    
    public int getmembDamageCount() {
      int sum = 0;
      for (String t : membDamageTypes)
        sum += bsg.util.CollectionUtils.countObjectsOfType(cell.mobileObjects, t);
      return sum;
    }
    
    public void scheduleTransport(MobileObject m) {
      long cycle = cell.mySpace.ss.hepStruct.model.getCycle();
      //log.debug("Transporter.scheduleTransport() for cell "+cell.id+" at cycle "+Long.toString(cycle));

      // Uniform distribution:
      long sched = 0;
      if (TRANSPORT_DELAY_MAX == TRANSPORT_DELAY_MIN) {
          sched = TRANSPORT_DELAY_MIN;
      } else {
          sched = TRANSPORT_DELAY_MIN + cell.cellRNG.nextInt(TRANSPORT_DELAY_MAX-TRANSPORT_DELAY_MIN);
      }
      //log.debug("Scheduling cell.transportMobileObject(MobileObject m) at cycle = "+(cycle)+", getTime() => "+cell.myGrid.ss.hepStruct.model.parent.schedule.getTime());
      
      sim.engine.Steppable transport_step = (sim.engine.SimState state) -> { transportEvent(m); };
      sim.engine.Schedule schedule = cell.mySpace.ss.hepStruct.model.parent.schedule;
      //necrosis_stop = new sim.engine.TentativeStep(necrosis_step);
      boolean success = schedule.scheduleOnce(cycle + sched, ISL.action_order+1, transport_step);
      //if (success) cell.myGrid.ss.necTrig(cell);
      if (!success) throw new RuntimeException("Failed to schedule a transportMobileObject event for cell "+cell.id);

      //log.debug("Transporter:"+cell.id+".scheduleTransport() at "+cycle+" for "+(cycle + sched));
    } // end scheduleNecrosis();
    
    public void transportEvent(MobileObject m) {
      //log.debug("TransportHandler:"+cell.id+".transportEvent()");
      cell.transportMobileObject(m);
    }
}
