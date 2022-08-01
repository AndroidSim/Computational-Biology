/*
 * Copyright 2014-2016 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 */
package isl.model.cell;

import isl.model.EnzymeGroup;
import isl.model.MobileObject;
import java.util.List;

public interface BindingInfo {
    
    //Accessors
    public java.util.ArrayList<EnzymeGroup> getEnzymeGroups();
//    public java.util.ArrayList<MobileObject> getMobileObjects();
    public List<MobileObject> listMobileObject();
    public double getBindingProbability(EnzymeGroup eg);
    public int getBindmin();
    public int getBindmax();
    public boolean isBound(MobileObject m);
    
    //Methods to manipulate state information
    public void scheduleRelease(MobileObject m, EnzymeGroup eg);
}
