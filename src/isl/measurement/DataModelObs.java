/*
 * Copyright 2003-2015 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package isl.measurement;

public class DataModelObs extends Obs {
  private static final long serialVersionUID = -128046356321409418L;

   public DataModelObs(isl.model.data.CSVDataModel dm, String outputFile) {
      super(dm, outputFile, false);
      headers.remove(0); // remove superfluous "Time" added by super()
   }
   @Override
   public void step(sim.engine.SimState state) {
      /* data model is a special case that need not be stepped, so we'll 
       * execute what normally would be model code, here
       * stop() to set the status to STOPPED
       */
      isl.model.data.CSVDataModel datModel = (isl.model.data.CSVDataModel) model;
      if(datModel.isDone(state)) { datModel.stop(); }
      else {
         java.util.Map<String, Number> obs =
                 datModel.getInterpolatedOutputs(datModel.parent.getISL().mason2IslTime(state.schedule.getTime()));
         log.info("{} -- savedOutputs = {}", this.getClass().getName(), obs);
         writeOutputs(obs);
      }
   }
}
