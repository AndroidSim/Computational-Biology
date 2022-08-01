/*
 * Copyright 2003-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import isl.io.Parameters;

public strictfp class ISLParams implements isl.io.Parameters {
  static final String CYCLE_LIMIT = "cycleLimit";
  static final String STEPS_PER_CYCLE = "stepsPerCycle";
  static final String CONTEXT = "context";
  public static final String CONTEXT_BODY = "body";
  public static final String CONTEXT_CULT = "culture";
  static final String BODY_XFER_RATE = "bodyXferRate";
  static final String BODY_XFER_MOD = "bodyXferMod";
  static final String RECIRCULATE = "recirculate";
  static final String USE_BODY = "useBody";
  static final String USE_INTRO = "useIntro";
  static final String INTRO_SAMPLE = "introSample";
  static final String GRID_SCALE = "gridScale";
  static final String CORE_SCALE = "coreScale";
  static final String HEPINIT_READ = "hepInitRead";
  static final String HEPINIT_WRITE = "hepInitWrite";
  static final String HEPINIT_FILE = "hepInitFile";

  public static final String GSHKEY = "GSH_Depletion";
  public static final String REPKEY = "Repair";
  public static final String ALTKEY = "ALT";
  
  public static void loadParams(ISL isl, ec.util.ParameterDatabase pdb) {
    if (pdb == null) throw new RuntimeException("ISLParams: parameter database cannot be null");

    ec.util.Parameter param = null;
    StringBuilder pk = null;

    String c = pdb.getStringWithDefault(param = new ec.util.Parameter(CONTEXT), null, CONTEXT_BODY);
    c = c.trim();
    isl.context = (c.equals(CONTEXT_CULT) ? CONTEXT_CULT : CONTEXT_BODY);
    
    pk = new StringBuilder(BODY_XFER_RATE);
    String bxr = pdb.getString(param = new ec.util.Parameter(pk.toString()),null);
    bxr = bxr.trim();
    if (isl.context.equals(CONTEXT_BODY) && (bxr == null || bxr.equals(""))) throw new RuntimeException(BODY_XFER_RATE + " can't be null.");
    if (isl.context.equals(CONTEXT_CULT) && (bxr != null || !bxr.equals(""))) {
      log.warn(BODY_XFER_RATE + " will be ignored!  "+ BODY_XFER_MOD + " is still used to modify the transfer from Body/medium.");
    }
    isl.bxrBand = Parameters.parseTuple(bxr);
    
    pk = new StringBuilder(BODY_XFER_MOD);
    double val_d = pdb.getDoubleWithDefault(param = new ec.util.Parameter(pk.toString()), null, 1.0D);
    isl.bxrM = val_d;
    
    pk = new StringBuilder(RECIRCULATE);
    boolean recirc = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);
    isl.recirculate = recirc;

    pk = new StringBuilder(STEPS_PER_CYCLE);
    int spc = pdb.getInt(param = new ec.util.Parameter(pk.toString()), null, 1);
    isl.stepsPerCycle = spc;

    pk = new StringBuilder(USE_BODY);
    boolean ub = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, true);
    isl.useBody = ub;

    pk = new StringBuilder(USE_INTRO);
    boolean ui = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);
    isl.useIntro = ui;

    if (ui) {
      pk = new StringBuilder(INTRO_SAMPLE);
      val_d = pdb.getDoubleWithMax(param = new ec.util.Parameter(pk.toString()), null, 0.0, 100.0);
      isl.introSample = val_d;
    }
    
    pk = new StringBuilder(GRID_SCALE);
    val_d = pdb.getDouble(param = new ec.util.Parameter(pk.toString()), null, 0.0D);
    isl.gridScale = val_d;

    pk = new StringBuilder(CORE_SCALE);
    val_d = pdb.getDouble(param = new ec.util.Parameter(pk.toString()), null, 0.0D);
    isl.coreScale = val_d;

    pk = new StringBuilder(HEPINIT_READ);
    boolean hir = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);
    isl.hepInitRead = hir;

    pk = new StringBuilder(HEPINIT_WRITE);
    boolean hiw = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);
    isl.hepInitWrite = hiw;
    
    if (pdb.containsKey("cellEnterExitProb")) {
      java.util.Map<String,sim.util.Double2D> ceep = (java.util.Map<String,sim.util.Double2D>) Parameters.getPropVal(pdb, "map2d", "cellEnterExitProb", TESTS.exists);
      ceep.entrySet().forEach((me) -> {
        try {
          Class<?> cls = Class.forName("isl.model.cell."+me.getKey());
          java.lang.reflect.Method m = cls.getMethod("setMembraneCrossProb",sim.util.Double2D.class);
          m.invoke(null, me.getValue());
        } catch (ReflectiveOperationException roe) {
          throw new RuntimeException("Failed to set cellEnterExitProb.", roe);
        }
      });
    } else {
      throw new RuntimeException("cellEnterExitProb parameter not found!");
    }
  }

}
