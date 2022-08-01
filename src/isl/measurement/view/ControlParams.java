/*
 * Copyright 2003-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package isl.measurement.view;

import isl.io.Parameters;

public strictfp class ControlParams implements Parameters {

  static final String SEED = "seed";
  static final String CYCLE_LIMIT = "cycleLimit";
  static final String MC_TRIALS = "monteCarloTrials";
  static final String MEASURE_H_MOBILEOBJECT = "measureHMobileObject";
  static final String MEASURE_ALL_MOBILEOBJECT = "measureAllMobileObject";
  static final String MEASURE_MIT = "measureMITs";
  static final String NT_SNAPS = "takeNTSnaps";

  public static void loadParams(BatchControl bc, ec.util.ParameterDatabase pdb) {
    if (pdb == null) {
      throw new RuntimeException("ControlParams: parameter database cannot be null");
    }
    /**
     * if a seed is present in the batch_control.properties db, use it to
     * replace random with a new one using the seed from the properties file
     * else use the default one generated from an OS time() call
     */
    ec.util.Parameter param = null;
    StringBuilder pk = null;
    pk = new StringBuilder(SEED);
    int newSeed = pdb.getInt (param = new ec.util.Parameter(pk.toString()), null, 0);
    if (newSeed < 0) 
      newSeed = (int)System.currentTimeMillis();
    bc.setLocalSeed(newSeed);

    pk = new StringBuilder(CYCLE_LIMIT);
    int cl = pdb.getInt(param = new ec.util.Parameter(pk.toString()), null);
    if (cl < 1) throw new RuntimeException(pk + " must be > 0.");
    bc.cycleLimit = cl;

    pk = new StringBuilder(MC_TRIALS);
    int trials = pdb.getInt(param = new ec.util.Parameter(pk.toString()), null, 1);
    bc.mc_trials = trials;

    pk = new StringBuilder(MEASURE_H_MOBILEOBJECT);
    boolean mrp = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);
    bc.measureHMobileObject = mrp;

    pk = new StringBuilder(MEASURE_ALL_MOBILEOBJECT);
    boolean mas = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);
    bc.measureAllMobileObject = mas;
    
    pk = new StringBuilder(MEASURE_MIT);
    boolean mmits = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);
    bc.measureMITs = mmits;

    pk = new StringBuilder(NT_SNAPS);
    boolean nts = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);
    bc.takeNTSnaps = nts;
    
  }
}
