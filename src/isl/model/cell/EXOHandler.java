/*
 * Copyright 2003-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model.cell;

import isl.model.EXO;
import isl.model.MobileObject;
import isl.model.MobileObjectType;
import isl.model.cell.Hepatocyte;
import sim.field.grid.SparseGrid2D;
import java.util.ArrayList;

public class EXOHandler implements Runnable {

    Hepatocyte cell = null;

    public static double pEXO = Integer.MAX_VALUE;

    public EXOHandler(Hepatocyte c) {
        cell = c;
    }
        
    public void receiveEXO() {
        sim.util.Bag result = new sim.util.Bag();
        ArrayList<MobileObjectType> mobileObjectTypes = cell.mySpace.ss.hepStruct.model.allMobileObject;
        MobileObjectType EXOType = null;
        for (MobileObjectType mt : mobileObjectTypes) {
            if (mt.tag.equals("EXO")) {
                EXOType = mt;
                break;
            }
        }
        SparseGrid2D grid = cell.mySpace.grids.get(EXOType);
        int numEXOsHere = 0;
        if (grid != null) {
            numEXOsHere = cell.mySpace.grids.get(EXOType).allObjects.size();
        }
        
        ArrayList<EXO> exoList = null;

        if (numEXOsHere > 0) {
          result = cell.mySpace.grids.get(EXOType).getObjectsAtLocation(cell.myX, cell.myY);
          if (result != null && cell.cellRNG.nextDouble() < pEXO) {
              cell.nh.scheduleNecrosis();
              exoList = new ArrayList<>(result);

              for (EXO e : exoList) {
                  //cell.forget(e);
              }
          }
        }
    }

    @Override
    public void run() {
        receiveEXO();
    }
}
