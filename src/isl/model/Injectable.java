/*
 * injectable objects (PV, MB)
 * Copyright 2014-2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import java.util.ArrayList;
import java.util.Map;

public interface Injectable {
  public void initPool(ArrayList<MobileObjectType> types);
  public Map<String, Number> getDistributed();
  public Map<String, Number> getMobileObjectMap();
  public void inject ( Map<String, Number> sm);
}
