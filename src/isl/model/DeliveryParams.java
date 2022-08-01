/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import isl.io.Parameters;

public strictfp class DeliveryParams implements Parameters {

   static final String DELIM = ".";

   static final String DELIVERY_TYPE = "deliveryType";
   public static final String INFUSION_TYPE = "infusion";
   static final String INFUSION_STOP_TIME = "infusionStopTime";
   static final String INFUSION_CONC_MAX = "infusionConcMax";
   public static final String BOLUS_TYPE = "bolus";
   static final String DOSE_PRE = "dose";
   static final String MOBILEOBJECT_PRE = "mobileobject";
   static final String PROP_PRE = "property";

   static final String REF_DOSE = "referenceDose";
   static final String NUMDOSES = "numDoses";
   static final String REPEAT = "repeatDose";
   static final String TIME = "time";
   

  public static void loadDoseParams(DeliveryMethod delivery, ec.util.ParameterDatabase pdb) {
    if (pdb == null) throw new RuntimeException("DoseParams: parameter database cannot be null");
    ec.util.Parameter param = null;
    StringBuilder pk = null;
     
    // tells us whether or not the later doses are repeats of the first
    pk = new StringBuilder(REPEAT);
    boolean repeat = pdb.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);
    delivery.doseRepeats = repeat;

    pk = new StringBuilder(REF_DOSE);
    int refDose = pdb.getInt(param = new ec.util.Parameter(pk.toString()), null);
    if (refDose < 1) {
      throw new RuntimeException(pk + " must be > 0.");
    }
    delivery.referenceDose = refDose;

    pk = new StringBuilder(NUMDOSES);
    int numDoses = pdb.getInt(param = new ec.util.Parameter(pk.toString()), null);
    if (numDoses < 1) {
      throw new RuntimeException(pk + " must be > 0.");
    }
    delivery.numDoses = numDoses;
    // if it repeats, set numDoses = 1 to build the doses bag
    if (repeat) numDoses = 1;
    java.util.ArrayList<Dose> doses = new java.util.ArrayList<>(numDoses);

    // loop over the local numDoses, delivery.numDoses could be more if doses are repeated
    int alpha = Integer.MIN_VALUE, beta = alpha, gamma = beta;
    for (int dNdx = 0; dNdx < numDoses; dNdx++) {
      Injectable tgt = null;
      if (delivery.hepStruct.model.useBody) {
        if (delivery.hepStruct.model.useIntro) {
          tgt = delivery.hepStruct.model.introCompartment;
        } else {
          tgt = delivery.hepStruct.model.body;
        } 
      } else {
        tgt = delivery.hepStruct.structInput;
      }
     
      Dose dose = new Dose(dNdx, delivery, tgt);

      if (dNdx <= 0 || !repeat) {
        // convenient dose BOLUS_TYPE fix e.g. "delivery.dose."
        String dosePrefix = DOSE_PRE + DELIM + dNdx + DELIM;

        Object p = Parameters.getPropVal(pdb, Parameters.T_MAP1D, dosePrefix+MobileObjectType.TYPES);
        java.util.Map<String,Double> m = null;
        if (!(p instanceof java.util.Map)) throw new RuntimeException(dosePrefix+MobileObjectType.TYPES + " isn't a Map.");
        else m = (java.util.Map)p;
        if (m.isEmpty()) throw new RuntimeException(dosePrefix+MobileObjectType.TYPES + " is empty.");
        for (java.util.Map.Entry<String, Double> me : m.entrySet()) {
          if (!(me.getKey() instanceof String) || !(me.getValue() instanceof Double))
            throw new RuntimeException(dosePrefix+MobileObjectType.TYPES + " format confuses me.");
        }
        dose.solution = m;
 
        pk = new StringBuilder(dosePrefix + TIME);
        double time = pdb.getDouble(param = new ec.util.Parameter(pk.toString()), null, 0.0);
        if (time < 0.0) throw new RuntimeException(pk + " must be > 0.0.");
        dose.time = time;

        pk = new StringBuilder(dosePrefix + DELIVERY_TYPE);
        String deliveryType = pdb.getString(param = new ec.util.Parameter(pk.toString()), null);
        if ( deliveryType == null || deliveryType.equals("") )
          throw new RuntimeException(pk + " must be non-null.");
        dose.deliveryType = deliveryType;

        if (deliveryType.equals(INFUSION_TYPE)) {
          pk = new StringBuilder(dosePrefix + INFUSION_STOP_TIME);
          double infusionStopTime = pdb.getDouble(param = new ec.util.Parameter(pk.toString()), null, 0.0);
          if (infusionStopTime < 0) throw new RuntimeException(INFUSION_STOP_TIME + " must be > 0.0");
          dose.infusionStopTime = infusionStopTime;
           
        }
         
        doses.add(dose);
      } // end for (int dNdx = 0; dNdx < numDoses; dNdx++)
      delivery.doses = doses;

    } // end loadDoseParams
  }
}
