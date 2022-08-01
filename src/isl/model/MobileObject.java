/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

public /*strictfp*/ class MobileObject extends isl.io.Propertied implements bsg.util.TypeString {

    private static int instanceCount = 0;
    public int id = -1;
    public MobileObjectType type = null;

    @Override
    public String getTypeString() { return type.tag; }
    public boolean isBindable() {
      boolean retVal = false;
      for (EnzymeGroup eg : isl.model.cell.Cell.MET_ENV.enzymeGroups.values()) {
        for (String st : eg.acceptedMobileObjects) retVal = true;
      }
      return retVal;
    }

    public MobileObject(MobileObjectType t) {
      if (t != null) type = t;
      else throw new RuntimeException("MobileObject: type cannot be null.");
      id = instanceCount++;
    }
}
