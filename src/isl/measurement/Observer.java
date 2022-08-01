/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.measurement;
import java.util.ArrayList;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import isl.model.cell.Cell;
import isl.model.cell.Hepatocyte;
import isl.model.Vas;
import isl.model.Vas.VasType;
import isl.measurement.CountMITsPerSlice.MIT;
import isl.model.ISL;

public class Observer implements Steppable {
  private static final long serialVersionUID = 6591834964321409301L;

   private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Observer.class);

   private final boolean rawIO = true; // placeholder for parameter if we ever want it
   
   isl.measurement.view.BatchControl batchControl = null;
   isl.model.ISL isl = null;
   isl.model.data.CSVDataModel dataModel = null;
   isl.model.ref.RefModel refModel = null;
   ArrayList<String> mobileObjectTypes = null;
   ArrayList<Obs> onceObservations = new ArrayList<>();
   ArrayList<Obs> observations = new ArrayList<>();
   protected java.util.Map<Steppable, Stoppable> stops = new java.util.HashMap<>();

   /**
    * Creates a new instance of Observer
    * @param bc SimState we'll use to operate as if we have our own
    * schedule.
    * @param im The model being observed.
    * @param dm Data model representing experimental data taken from referent.
    * @param rm Reference model -- typical model used for this referent.
    */
   public Observer(isl.measurement.view.BatchControl bc, 
           isl.model.ISL im, isl.model.data.CSVDataModel dm,
           isl.model.ref.RefModel rm) {
      isl = im;
      dataModel = dm;
      refModel = rm;

      if (bc != null) {
         batchControl = bc;
      } else {
         throw new RuntimeException("Observer: SimState batchControl cannot be null.");
      }

   }
   
   /**
    * all_h: convenience list of all Hepatocytes is used for intraH MobileObject, 
    * EG, and HCount measures.
    */
   ArrayList<Hepatocyte> all_h = new ArrayList<>();
   CountHepState countHepState = null;
   
   public void init() {
     if (isl != null) {
       
       /* one-time measures */
       GraphObs go = new GraphObs(isl, String.format("graph-%04d", batchControl.trial_count)+".csv", true);
       onceObservations.add(go);
       SSGeom ssg = new SSGeom(isl, String.format("ssgeom-%04d", batchControl.trial_count)+".csv", true);
       ssg.writeHeader();
       onceObservations.add(ssg);
       
       mobileObjectTypes = chooseMobileObjectTypes(batchControl.measureAllMobileObject);

       if (batchControl.measureHMobileObject) {
         /* special observations that run at a different rate */
         for (int z=0 ; z<isl.hepStruct.layers.size() ; z++ ) {
            for (isl.model.SS ss : isl.hepStruct.nodesInLayer.get(z)) {
                ss.cells.stream().filter((o) -> (o instanceof Hepatocyte)).forEach((o) -> {
                all_h.add(((Hepatocyte)o));
            });
            }
         }
         scheduleHMobileObjectMeasure();
       }

       /* standard observations that execute every cycle */
       for (Object o[] : new Object[][]{{"dCV",VasType.OUT},{"dPV",VasType.IN}}) {
         GetRxnProdCount rpc = new GetRxnProdCount(isl, String.format("rxnprod-"+o[0]+"-%04d",batchControl.trial_count)+".csv", (VasType)o[1]);
         rpc.setLogger(log);
         rpc.init();
         rpc.writeHeader();
         observations.add(rpc);
       }

       CountExtraCellularMobileObject cecs = new CountExtraCellularMobileObject(isl, String.format("extra-%04d",batchControl.trial_count)+".csv", false);
       cecs.setLogger(log);
       cecs.writeHeader();
       observations.add(cecs);

       for (Object o[] : new Object[][]{{"dCV",false},{"dPV",true}}) {
          CountCellAdj celladj = new CountCellAdj(isl, String.format("celladj-"+o[0]+"-%04d",batchControl.trial_count)+".csv", new ArrayList<>(mobileObjectTypes), (boolean)o[1]);
          celladj.setLogger(log);
          celladj.init();
          celladj.writeHeader();
          observations.add(celladj);
       }
       
       if (rawIO) {
         ProfileMeasures rawIn = new ProfileMeasures(isl, "rawInput"
                 +String.format("-%04d",batchControl.trial_count)
                 +".csv", Vas.VasType.IN);
         rawIn.setLogger(log);
         rawIn.writeHeader();
         observations.add(rawIn);
         ProfileMeasures rawOut = new ProfileMeasures(isl, "rawOutput"
                 +String.format("-%04d",batchControl.trial_count)
                 +".csv", Vas.VasType.OUT);
         rawOut.setLogger(log);
         rawOut.writeHeader();
         observations.add(rawOut);
       } else { 
         ProfileMeasures df = new ProfileMeasures(isl, "doseFract"
                 +String.format("-%04d",batchControl.trial_count)
                 +".csv", ProfileMeasures.MeasureType.doseFraction);
         df.setLogger(log);
         df.writeHeader();
         observations.add(df);
         ProfileMeasures of = new ProfileMeasures(isl, "outFract"
                 +String.format("-%04d",batchControl.trial_count)
                 +".csv", ProfileMeasures.MeasureType.outputFraction);
         of.setLogger(log);
         of.writeHeader();
         observations.add(of);
         ProfileMeasures ex = new ProfileMeasures(isl, "extRatio"
                 +String.format("-%04d",batchControl.trial_count)
                 +".csv", ProfileMeasures.MeasureType.extRatio);
         ex.setLogger(log);
         ex.writeHeader();
         observations.add(ex);
       }
       
       scheduleEGMeasures(all_h);

       // countHepState provides the data for necrotic, necrosis triggers, and stressed observations
       countHepState = new CountHepState(isl, all_h);
       
       // count the necrotic hepatocytes
       CountNecroticCells cdc = new CountNecroticCells(isl,String.format("necrotic-%04d", batchControl.trial_count)+".csv");
       cdc.setLogger(log);
       cdc.writeHeader();
       observations.add(cdc);
       
       // count the hepatocytes which have been triggered for necrosis
       CountNecTrigs cnt = new CountNecTrigs(isl, String.format("nectrig-%04d", batchControl.trial_count)+".csv");
       cnt.setLogger(log);
       cnt.writeHeader();
       observations.add(cnt);

       // count the hepatocytes that have entered the stressed state
       CountStressedCells csc = new CountStressedCells(isl, String.format("stressed-%04d",batchControl.trial_count)+".csv", countHepState);
       csc.setLogger(log);
       csc.writeHeader();
       observations.add(csc);
       
       // take snapshots if there's been a Hepatocyte state change
       if (batchControl.takeNTSnaps) {
         NecTrigSnap nts = new NecTrigSnap(isl, String.format("snap-%04d", batchControl.trial_count), all_h, false);
         batchControl.schedule.scheduleRepeating(nts, ISL.action_order+100, 1.0);
       }
       
       if (isl.useBody) {
         CountMobileObjectInCompartment csbody = new CountMobileObjectInCompartment(isl, isl.body, String.format("body-%04d", batchControl.trial_count) + ".csv");
         csbody.writeHeader();
         observations.add(csbody);
       }
       CountMobileObjectInCompartment csPV = new CountMobileObjectInCompartment(isl, isl.hepStruct.structInput, String.format("pv-%04d", batchControl.trial_count) + ".csv");
       csPV.writeHeader();
       observations.add(csPV);
       
       CountMobileObjectInCompartment csCV = new CountMobileObjectInCompartment(isl, isl.hepStruct.structOutput, String.format("cv-%04d", batchControl.trial_count) + ".csv");
       csCV.writeHeader();
       observations.add(csCV);

       // count the membrane interactions
       if (batchControl.measureMITs) {
         for (Object type[] : new Object[][]{{"entries",MIT.ENTRY},{"exits",MIT.EXIT},{"rejects",MIT.REJECT},{"traps",MIT.TRAP}}) {
           for (Object dir[] : new Object[][]{{"dCV",false},{"dPV",true}}) {
             CountMITsPerSlice mits = new CountMITsPerSlice(isl, String.format(type[0]+"-"+dir[0]+"-%04d", batchControl.trial_count)+".csv", all_h, new ArrayList<>(mobileObjectTypes), (boolean)dir[1], (MIT)type[1]);
             mits.init(); mits.setLogger(log); mits.writeHeader();
             observations.add(mits);
           }
         }
       }
       
       String outFileName = String.format("hcount-dPV-%04d.csv",batchControl.trial_count);
       CountHPerSlice chpslice = new CountHPerSlice(isl, outFileName, all_h, true);
       chpslice.writeHeader();
       chpslice.countH();
       
       chpslice.outputLog.outPW.flush(); chpslice.outputLog.finish();
       outFileName = String.format("hcount-dCV-%04d.csv",batchControl.trial_count);
       chpslice = new CountHPerSlice(isl, outFileName, all_h, false);
       chpslice.writeHeader();
       chpslice.countH();
       
       chpslice.outputLog.outPW.flush(); chpslice.outputLog.finish();
       
     }
     if (dataModel != null) {
        isl.measurement.DataModelObs dmo = new DataModelObs(dataModel, "datModel"+
                String.format("-%04d",batchControl.trial_count)+
                ".csv");
        dmo.setLogger(log);
        dmo.writeHeader();
        observations.add(dmo);
     }
     if (refModel != null) {
       isl.measurement.RefModelObs rmo = new RefModelObs(refModel, "refModel"+
               String.format("-%04d",batchControl.trial_count)+
               ".csv");
       rmo.setLogger(log);
       rmo.writeHeader();
       observations.add(rmo);
     }
     
   } // end init()

   /**
    * scheduleHMobileObjectMeasure - creates the measure, schedules it, adds the stop
                          to the stop map
    * @param layer - [0-2] corresponding to layers [1-3]
    * @param sample - what percentage of cells to measure
    */
   private void scheduleHMobileObjectMeasure() {
     // 2nd the 2 slices
     String outFileName = String.format("mobileObject-dPV-%04d.csv",batchControl.trial_count);
     CountIntraHPerSlice cihps = new CountIntraHPerSlice(isl,outFileName,all_h,new ArrayList<>(mobileObjectTypes), true);
     cihps.init();
     cihps.setLogger(log);
     cihps.writeHeader();
     observations.add(cihps);
     outFileName = String.format("mobileObject-dCV-%04d.csv",batchControl.trial_count);
     cihps = new CountIntraHPerSlice(isl,outFileName,all_h,new ArrayList<>(mobileObjectTypes), false);
     cihps.init();
     cihps.setLogger(log);
     cihps.writeHeader();
     observations.add(cihps);
     // same as above but for bound mobile objects
     String boutFileName = String.format("boundMObject-dPV-%04d.csv",batchControl.trial_count);
     CountBoundIntraHPerSlice cbihps = new CountBoundIntraHPerSlice(isl,boutFileName,all_h,new ArrayList<>(mobileObjectTypes), true);
     cbihps.init();
     cbihps.setLogger(log);
     cbihps.writeHeader();
     observations.add(cbihps);
     boutFileName = String.format("boundMObject-dCV-%04d.csv",batchControl.trial_count);
     cbihps = new CountBoundIntraHPerSlice(isl,boutFileName,all_h,new ArrayList<>(mobileObjectTypes), false);
     cbihps.init();
     cbihps.setLogger(log);
     cbihps.writeHeader();
     observations.add(cbihps);
   }
   
   private void scheduleEGMeasures(ArrayList<Hepatocyte> cl) {
     ISLEnzymeObs dPVo = new ISLEnzymeObs(isl, cl, String.format("enzymes-dPV-%04d", batchControl.trial_count)+".csv",true);
     dPVo.writeHeader();
     observations.add(dPVo);
     ISLEnzymeObs dCVo = new ISLEnzymeObs(isl, cl, String.format("enzymes-dCV-%04d", batchControl.trial_count)+".csv",false);
     dCVo.writeHeader();
     observations.add(dCVo);
   }

   public ArrayList<String> chooseMobileObjectTypes(boolean allMobileObject) {
     ArrayList<String> retVal = new ArrayList<>();
     for (isl.model.MobileObjectType st : isl.allMobileObject) {
       if (allMobileObject) { retVal.add(st.tag); }
       else {
         // if we find an EG that's productive and accepts this MobileObjectType
         if (Cell.MET_ENV.enzymeGroups.entrySet().stream()
                 .filter((me) -> (me.getValue().acceptedMobileObjects.contains(me.getKey()) 
                         && me.getValue().isProductive())).findAny().isPresent()
                 )
           retVal.add(st.tag);
       }
     }
    return retVal;
   }
   
  @Override
   public void step(sim.engine.SimState state) {
     boolean islRunning = false;
     // execute the one-time measures
     for (Obs obs : onceObservations) {
       obs.step(state);
       obs.outputLog.outPW.flush();
       obs.outputLog.finish();
     }
     onceObservations.clear();
     
     // before executing the individual Observations, gather the Hepatocyte states
     countHepState.getHepStates();

     // for each observation, remove it from the sequence if it's tgt has stopped
     for (Obs obs : observations) {
       boolean thisOneRunning = obs.isTargetRunning();
       if (obs.model == isl) islRunning = thisOneRunning;
       if (thisOneRunning) obs.step(state);
     }

     // if the isl is not running, then stop myself
     if (!islRunning) {
       stops.values().stream().forEach((stop) -> { stop.stop(); });
       batchControl.stopMe(this);
     }
   }

   
   public void flush() {
     observations.stream().forEach((o) -> { o.outputLog.outPW.flush(); });
   }
   public void finish() {
     flush();
     observations.stream().forEach((o) -> { o.outputLog.finish(); });
   }
}
