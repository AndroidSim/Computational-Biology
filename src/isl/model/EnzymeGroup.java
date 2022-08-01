/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import java.util.ArrayList;

public class EnzymeGroup extends isl.io.Propertied implements bsg.util.TypeString {        
    
  final int initial_capacity;
  public int getInitialCapacity() { return initial_capacity; }
   
  public String type = null;
  @Override
  public String getTypeString() { return type; }
  
  public int capacity = -Integer.MAX_VALUE;
  public int getCapacity() { return capacity; }
    
  public double bindProb = -Double.MAX_VALUE;
  public double getBindProb() { return bindProb; }
    
  public int bindCycles = -Integer.MAX_VALUE;
  public int getBindCycles() { return bindCycles; }
    
  public ArrayList<MobileObject> boundMobileObjects = null;
  public ArrayList<MobileObject> getBoundMobileObjects() { return boundMobileObjects; }    
    
  public ArrayList<String> acceptedMobileObjects = null;
  public ArrayList<String> getAcceptedMobileObjects() { return acceptedMobileObjects; }
  
  public ArrayList<String> downRegulatedBy = null;
  public ArrayList<String> getDownRegulatedBy() { return downRegulatedBy; }
  
  public EnzymeGroup(String t, int cap, double bind_prob, int bind_cycles, java.util.ArrayList<String> mobileObjects, java.util.LinkedHashMap<String, Object> props) {
    type = t;
    initial_capacity = cap;
    capacity = cap;
    bindProb = bind_prob;
    bindCycles = bind_cycles;
    acceptedMobileObjects = mobileObjects;
    properties = props;

    boundMobileObjects = new java.util.ArrayList<MobileObject>();
  }
    
  public void setCapacity(int cap) {
    if(cap < 0)
      throw new IllegalArgumentException("Capacity must be >= 0.");
    capacity = cap;
  }
  public void changeCapacity(int amt) { capacity = Math.max(0, capacity + amt); }
    
  public boolean accept(MobileObject m) {
    for (String str : acceptedMobileObjects) {
      if (m.getTypeString().equalsIgnoreCase(str)) {
        return true;
      }
    }
    return false;
  }
    
}
