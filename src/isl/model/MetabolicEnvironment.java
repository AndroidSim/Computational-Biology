/*
 * Copyright 2003-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MetabolicEnvironment {
  private static org.slf4j.Logger log = null;
    
  public int bindmin = 1;
  public int bindmax = 1;
  
  //Format: <Cell type name, list of EnzymeGroups>
  LinkedHashMap<String, ArrayList<String> > cellTypeToEnzymeGroupNames = null;
  public LinkedHashMap<String, ArrayList<String>> getCellTypeToEnzymeGroupNames() { return cellTypeToEnzymeGroupNames; }
    
  //Master list of EnzymeGroup types
  public LinkedHashMap<String, EnzymeGroup> enzymeGroups = null;
    
  String bindingMode = null;
  public String getBindingMode() { return bindingMode; }

  //P450 down-regulation mechanism parameters
  public double drReplenish = -Double.MAX_VALUE;
  public double drRemove = -Double.MAX_VALUE;
  public double drInterval = -Double.MAX_VALUE;
  public int drCapΔ = -Integer.MAX_VALUE;
  public double drPrΔ = -Double.MAX_VALUE;

  public MetabolicEnvironment() {}
    
  public void setLogger(org.slf4j.Logger logger) { log = logger; }
    
  public void init() {
    cellTypeToEnzymeGroupNames = new LinkedHashMap<>();
    enzymeGroups = new LinkedHashMap<>();
        
    ec.util.ParameterDatabase pd = null;
    try {
      pd = new ec.util.ParameterDatabase( this.getClass().getClassLoader().getResourceAsStream("cfg/metabolic.properties"));
    } catch(java.io.IOException ioe) {
      System.err.println( ioe.getMessage() );
      System.exit(-1);
    }
        
    //populates 
    MetabolicParams.loadParams(this, pd);
    
 }
 
}
