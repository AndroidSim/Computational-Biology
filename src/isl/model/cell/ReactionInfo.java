/*
 * Copyright 2013-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * ReactionInfo.java
 * 
 */
package isl.model.cell;

import isl.model.EnzymeGroup;
import isl.model.MobileObject;
import isl.model.MobileObjectType;
import java.util.Map;

public interface ReactionInfo {
    
    //Accessors
    public Map<EnzymeGroup, Double> getRxnProbMap();
    public Map<EnzymeGroup, Map<String,Double>> getProductionMap();
    public java.util.ArrayList<MobileObjectType> getMobileObjectTypes();

    //Methods to manipulate state information
    public void present(MobileObject m, boolean bileAndAmp);
    public void forget(MobileObject m);
    public void incRxnProd(String st);
    public void incRepairCount();
}
