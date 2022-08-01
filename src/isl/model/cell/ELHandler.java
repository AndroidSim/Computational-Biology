/*
 * Copyright 2014-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.model.cell;

import isl.model.cell.BindingInfo;
import isl.model.cell.CellInfo;
import bsg.util.MutableInt;
import isl.model.EnzymeGroup;
import isl.model.MobileObject;

public class ELHandler implements Runnable {
      
    //Cell parameters    
    CellInfo cell_info = null;
    int bindmax = -Integer.MAX_VALUE;

    //Binding info    
    java.util.ArrayList<EnzymeGroup> groups = null;
    
    //Elimination parameters
    int el_thresh = -Integer.MAX_VALUE;
    double el_response = Double.NaN;
    double el_rate = Double.NaN;
    java.util.Map<String,Double> el_inhib_types;

    //Log
    private static org.slf4j.Logger log = null;

    //Variables local to ELHandler
    java.util.ArrayList<MutableInt> elimQueue = null;
    int el_accumulator = 0; //grows when there is no bound mobile object
    int el_increment = 1; //ei decrements to relax, el increments to relax  
    java.util.ArrayList<EnzymeGroup> inducibleEnzymeGroups = new java.util.ArrayList<EnzymeGroup>();
    
    public ELHandler(CellInfo c, BindingInfo b, ELInfo el, org.slf4j.Logger logger) {
        
        cell_info = c;
        bindmax = b.getBindmax();        
                
        groups = b.getEnzymeGroups();
        for(EnzymeGroup eg : groups)
        {
            if(eg.hasProperty("inducible") && (boolean) eg.getProperty("inducible"))
            {
                inducibleEnzymeGroups.add(eg);
            }
        }
        
        el_thresh = el.getELThresh();
        el_rate = el.getELRate();
        el_response = el.getELResponse();        
        el_inhib_types = el.getELInhibTypes();
        
        log = logger;
    }

    public void run() {
              
        // if all EnzymeGroups are already back at their minimum, clear the queue and return
        int numGroupsAtMinimum = 0;
        for(EnzymeGroup eg : inducibleEnzymeGroups) {
          if (eg.getCapacity() <= eg.getInitialCapacity()) {
            numGroupsAtMinimum++;
          }
        }
        if(numGroupsAtMinimum == inducibleEnzymeGroups.size()) {
            if (elimQueue != null) {
                    elimQueue.clear();
            }
            return;
        }
        
        eliminateEnzymes();
        queueEL();
        //if (elimQueue != null && elimQueue.size() > 0)
        //  log.debug("H:"+id+" in SS:"+parent.id+" |elimQueue| = "+ elimQueue.size());
    }

    /*
     * eliminate the enzymes that were queued up to be eliminated this iterate
     * Note: if the process of eliminating some enzymes has already started,
     * then the referent is not likely to be able to stop it quickly or at all.
     * E.g. if elimination involves transcribing some new protein to bind to
     * the enzymes, package it up in a lysosome, and put it out.  Hence, we
     * continue to eliminate even if the ei_accumulator is large and there's
     * EI occurring.  We may even eliminate one that was just created.  This
     * seems more natural than shorting the EL process at the first whiff of EI.
     */
    private void eliminateEnzymes() {
        if (elimQueue == null || elimQueue.size() < 1) {
            return;
        }
        int num_2_elim = (int) elimQueue.remove(0).val;
        for(EnzymeGroup eg : inducibleEnzymeGroups) {
            eg.changeCapacity(-1*num_2_elim);
        }
    }

    /*
     * if the el_accumulator > el_thresh, then we try to remove some enzymes at
     * el_rate, bounded by however many we had at the start.
     */
    private void queueEL() {

        // increment the accumulator when we're dormant
        int totalBound = 0;
        for(EnzymeGroup eg : groups) {
            totalBound += eg.getBoundMobileObjects().size();
        }
        if (totalBound <= 0) {
            el_accumulator += el_increment;
        }

        /**
         * if there's lots of elimination waiting to be done already, skip
         * scheduling more elimination. This is mostly a hack to avoid huge
         * memory requirements. I doubt there's any biological analog.
         *
         * To make this parallel with the EI limit, we decrement by the
         * el_increment when there's too much EL scheduled. This adds a
         * "momentum" to the elimination so that even after it's eliminated all
         * it wants to, it keeps eliminating until it finally relaxes.
         */
        int totalInducibleCapacity = 0;
        for(EnzymeGroup eg : groups)
        {
            if(eg.hasProperty("inducible") && (boolean)eg.getProperty("inducible"))
            {
                totalInducibleCapacity += eg.getCapacity();
            }
        }
        if (elimQueue != null && elimQueue.size() > totalInducibleCapacity) {
            el_accumulator -= el_increment;
            return;
        }

        // if the accumulator is high enough, schedule some elimination
        if (el_accumulator > el_thresh) {
            if (elimQueue == null) {
                elimQueue = new java.util.ArrayList<MutableInt>();
            }
            // find out if EL is being inhibited and by how much
            long num_2_elim = StrictMath.round(el_response * cell_info.getResources() * el_accumulator - getELInhibFactor());

            /* 
             * spread the amount to eliminate along the end of the elimQueue,
             * where each spot in the queue corresponds to the el_rate
             */
            long rate_increment = StrictMath.round(1.0 / el_rate);
            int elim_ndx = 0;
            while (num_2_elim > 0) {
                MutableInt ndxInt = null;
                if (elim_ndx < elimQueue.size()) {
                    ndxInt = elimQueue.get(elim_ndx);
                } else {
                    // fill out the queue with new MutableInts
                    for (int fillNdx = elimQueue.size(); fillNdx <= elim_ndx; fillNdx++) {
                        elimQueue.add(ndxInt = new MutableInt(0));
                    }
                }

                if (ndxInt.val == 0 || ndxInt.val < el_rate) {
                    int num_nxt = (el_rate < 1.0 ? 1 : (int) (el_rate - ndxInt.val));
                    ndxInt.add(num_nxt);
                    num_2_elim -= num_nxt;

                    //log.debug(this+" scheduled "+num_nxt+" enzymes for elimination in cycle "+elim_ndx);

                    elim_ndx += rate_increment;
                } else // just increment by one if this slots full already
                {
                    elim_ndx++;
                }

            } // end while (num_2_elim > 0) {
        } // end if (el_accumulator > parent.el_thresh) {
    }
    double getELInhibFactor() {
      double retVal = 0.0;
      java.util.List<MobileObject> mobileObjects = cell_info.listMobileObject();
      for (java.util.Map.Entry<String,Double> me : el_inhib_types.entrySet()) {
        String tag = me.getKey();
        int count = bsg.util.CollectionUtils.countObjectsOfType(mobileObjects, tag);
        retVal += count*me.getValue();
      }
      return retVal;
    }
}
