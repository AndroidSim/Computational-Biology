/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement.view;
import isl.model.AbstractISLModel;

public class BatchView implements sim.engine.Steppable
{
  private static final long serialVersionUID = 1225233510248093839L;
  private static org.slf4j.Logger log = null;

  isl.measurement.view.BatchControl bc = null;
  isl.model.ISL isl = null;
  isl.model.data.CSVDataModel dataModel = null;

  public BatchView ( isl.measurement.view.BatchControl state, 
          isl.model.ISL exp, 
          isl.model.data.CSVDataModel d ) {
    if ( state != null ) {
      bc = state;
    } else {
      throw new RuntimeException("BatchView: state cannot be null");
    }

    isl = exp;
    dataModel = d;
    
  }
   public void setLogger(org.slf4j.Logger logger) {
      log = logger;
   }

  @Override
   public void step(sim.engine.SimState state) {
      log.info("BatchView.step() - step() - begin");
      boolean stop = true;
      if (isl != null && isl.getStatus() == AbstractISLModel.ModelStatus.STARTED) {
         log.info(describeISL());
         stop = false;
      } 
      if (dataModel != null && dataModel.getStatus() == AbstractISLModel.ModelStatus.STARTED) {
         log.info("BatchView: "+dataModel.getClass().getName());
         stop = false;
      } 
      // when none of the above executes, we're done
      if (stop) bc.stopMe(this);
   }
  /**
   * Iteratively describe the ISL and its components.
   * @return
   */
  String describeISL () { return isl.describe(); }

}
