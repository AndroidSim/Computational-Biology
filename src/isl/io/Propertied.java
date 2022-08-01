/*
 * Copyright 2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.io;
import java.util.LinkedHashMap;

public abstract class Propertied {
  
  public LinkedHashMap<String,Object> properties = null;
  public LinkedHashMap<String,Object> getProperties() { return properties; }
  
  public boolean hasProperty(String pname) {
     return properties.containsKey(pname);
  }
  public Object getProperty(String pname) {
     return properties.get(pname);
  }
  public void setProperties(LinkedHashMap<String,Object> p) {
     if (p != null) properties = p;
     else throw new RuntimeException(this.getClass().getSimpleName()+": properties cannot be null.");
  }
  public void setProperty(String pname, Object pvalue) {
    if (properties.containsKey(pname)) properties.put(pname, pvalue);
    else throw new RuntimeException(this.getClass().getSimpleName()+": property "+pname+" does not exist.");
  }

  public boolean isProductive() {
    double rps = Double.NaN, rpf = Double.NaN;
    return (hasProperty("rxnProbStart") && (rps = (double)getProperty("rxnProbStart")) > 0.0
                || hasProperty("rxnProbFinish") && (rpf = (double)getProperty("rxnProbFinish")) > 0.0);
  }
}
