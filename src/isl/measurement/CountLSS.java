/*
 * Copyright 2018-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement;

import java.util.ArrayList;
import isl.model.ISL;
import isl.model.SS;

/**
 * CountLSS: Count Location-Specific MobileObject
 */
public abstract class CountLSS extends Obs {

  private static final long serialVersionUID = 701049265313986L;
  
  ISL model = null; // hide the super.model
  ArrayList<String> mobileObjectTypes = null;
  protected boolean fromPV = true;
  
  public CountLSS(ISL tgt, String outputFile, ArrayList<String> stl, boolean pvorcv) {
    super(tgt, outputFile, true);
    model = tgt; // shadow super.model to get access to non-abstract parts
    if (stl != null && !stl.isEmpty()) mobileObjectTypes = stl;
    else throw new RuntimeException(getClass().getSimpleName()+" -- mobile object types cannot be empty.");
    fromPV = pvorcv; // T→ΣδPV, F→ΣδCV
  }

  protected void init() {
    headers.clear(); // clear those set by Obs
    headers.add("Time");
    // walk the graph to get every valid location
    ArrayList<String> tmpHeaders = new ArrayList<>();
    for (Object ln : model.hepStruct.allNodes) {
      if (ln instanceof SS) {
        SS ss = (SS) ln;
        for (int y=0 ; y<ss.length ; y++) {
          int dist = y+(fromPV ? ss.priorPathLength : ss.postPathLength);
          mobileObjectTypes.forEach((t) -> {
            String column = dist+":"+t;
            if (!tmpHeaders.contains(column)) tmpHeaders.add(dist+":"+t);
          });
        }
      }
    }
    headers.addAll(tmpHeaders);
  }
  
}
