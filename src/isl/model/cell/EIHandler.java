/*
 * Copyright 2003-2016 - Regents of the University of California, San
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

public class EIHandler implements Runnable {
    
    //Cell parameters
    CellInfo cell_info;
    int bindmax = -Integer.MAX_VALUE;
    
    //Binding parameters    
    java.util.ArrayList<EnzymeGroup> groups = null;
      
    //Induction parameters
    int ei_thresh = -Integer.MAX_VALUE;
    double ei_response = Double.NaN;
    double ei_rate = Double.NaN;

    //Log
    private static org.slf4j.Logger log = null;
    
    //Variables local to EIHandler
    java.util.ArrayList<MutableInt> createQueue = null;
    int ei_accumulator = 0; //grows when there is bound mobile object
    int ei_decrement = 1; //ei decrements to relax, el increments to relax   
    java.util.ArrayList<EnzymeGroup> inducibleEnzymeGroups = new java.util.ArrayList<EnzymeGroup>();

    public EIHandler(CellInfo c, BindingInfo b, EIInfo ei, org.slf4j.Logger logger) {                     
        
        cell_info = c;        
        bindmax = b.getBindmax();
        
        groups = b.getEnzymeGroups();
        for(EnzymeGroup eg : groups)
        {
            if(eg.hasProperty("inducible") && (boolean)eg.getProperty("inducible"))
                inducibleEnzymeGroups.add(eg);
        }
        
        ei_thresh = ei.getEIThresh();
        ei_rate = ei.getEIRate();
        ei_response = ei.getEIResponse();
        
        log = logger;  
    }

    public void run() {
        createEnzymes();
        queueEI();
        //if (createQueue != null && createQueue.size() > 0)
        //  log.debug("H:"+id+" in SS:"+parent.id+" |createQueue| = "+(createQueue != null ? createQueue.size() : "[empty]"));
    }

    /*
     * create enzymes scheduled from previous iterations
     */
    private void createEnzymes() {
        if (createQueue == null || createQueue.size() < 1) {
            return;
        }
        int num_2_create = (int) createQueue.remove(0).val;           
        for(EnzymeGroup eg : inducibleEnzymeGroups) {
                eg.changeCapacity(num_2_create);
        }
    }

    /*
     * schedule amounts to induce based on mobile object seen and eiGrad
     * schedule amounts to eliminate based on eiGrad
     */
    private void queueEI() {
        for(EnzymeGroup eg : groups)
        {
            ei_accumulator += eg.boundMobileObjects.size();
        }

        //if (bound.size() > 1) log.debug(this+" bound.size() = "+bound.size());

        // when ei_accumulator > ei_thresh, induce some enzymes
        if (ei_accumulator > ei_thresh) {
            long num_2_induce = StrictMath.round(ei_response / cell_info.getResources() * ei_accumulator);

            //log.debug("SS:"+parent.id+" "+this+" ei_accumulator = "+ei_accumulator+", num_2_induce = "+num_2_induce);

            /**
             * Shunts potential runaway memory grabs by the EI. If there is
             * already lots of EI scheduled, then simply increment the
             * accumulator rather than decrementing it and avoid scheduling more
             * for now.
             *
             * Be wary. It could cause artifacts.
             */
            int totalInducibleCapacity = 0;
            for(EnzymeGroup eg : groups)
            {
                if(eg.hasProperty("inducible") && (boolean)eg.getProperty("inducible"))
                {
                    totalInducibleCapacity += eg.getCapacity();
                }
            }
            if (createQueue != null && createQueue.size() > totalInducibleCapacity) {
                ei_accumulator += ei_decrement;
                return;
            }

            if (num_2_induce > 10 * bindmax) {
                
                //The following log.warns are commented out to prevent EIInfo from needing that much information from parent: id, priorPathLength
                log.warn("Warning!  EI is very high despite scheduled EI being below enzyme count.");
                /*
                 log.warn("SS:"+parent.id+" H:"+id+" Warning!  EI is very high despite scheduled EI being below enzyme count.");
                 log.warn("\tSS:"+parent.id+" H:"+id+" at <"+myX+","+myY+"> prior = "+parent.priorPathLength+" : inducing "+num_2_induce+" <= "+parent.ei_response_factor+"/"+resource+"*"+ei_accumulator);
                 log.warn("\tSS:"+parent.id+" H:"+id+" |binders| = "+binders.size()+", |createQueue| = "
                 +(createQueue != null ? createQueue.size() : "null"));
                 log.warn("\tSS:"+parent.id+" H:"+id+" capping to 10 times max binders: "+10*parent.bindmax);*/

                num_2_induce = 10 * bindmax;
            }

            if (createQueue == null) {
                createQueue = new java.util.ArrayList<MutableInt>();
            }
            long rate_increment = StrictMath.round(1.0 / ei_rate);
            int create_ndx = 0; // init to next cycle
            while (num_2_induce > 0) {
                // get/create the value already scheduled for that slot in the queue
                MutableInt ndxInt = null;
                if (create_ndx < createQueue.size()) {
                    ndxInt = createQueue.get(create_ndx); // Changes to ndxInt will be reflected in createQueue.get(create_ndx)!
                } else {
                    // create as many as needed to fill createQueue up to create_ndx
                    for (int fillNdx = createQueue.size(); fillNdx <= create_ndx; fillNdx++) {
                        createQueue.add(ndxInt = new MutableInt(0));
                    }
                }
                // schedule as many as possible and decrement the number remaining
                if (ndxInt.val == 0 || ndxInt.val < ei_rate) {
                    int num_nxt = (ei_rate < 1.0 ? 1 : (int) (ei_rate - ndxInt.val));
                    ndxInt.add(num_nxt); // This changes createQueue.get(create_ndx)!
                    num_2_induce -= num_nxt;

                    //log.debug(this+" scheduled "+num_nxt+" enzymes for creation in cycle "+create_ndx);

                    create_ndx += rate_increment;
                } else // just increment by one if this slots full already
                {
                    create_ndx++;
                }
            }
        } // end if (ei_accumulator > ei_thresh) {

        // decrement ei_accumulator each cycle.  when this H is empty, ei_accumulator -> 0
        if (ei_accumulator > 0) {
            ei_accumulator -= ei_decrement;
        }
    }
}