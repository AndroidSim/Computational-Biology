/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import isl.model.cell.NecrosisHandler;
import isl.model.cell.EXOHandler;
import isl.model.cell.Hepatocyte;
import isl.io.Parameters;
import isl.model.cell.Transporter;

public strictfp class SSParams implements Parameters {

  /*
   * Special biases for InnerSpace to represent vascular flow.
   */
  // forward bias is [0.0,1.0] 0 ⇒ all inward, 1 ⇒ all outward
  public static final String P_IFORB = "innerForwardBias";
  public static double inner_forward_bias = -Double.MAX_VALUE;
  // lateral bias is [0.0,1.0] 0=>inward, 1=>outward
  public static final String P_ILATB = "innerLateralBias";
  public static double inner_lateral_bias = -Double.MAX_VALUE;

  /*
   * Default biases for not InnerSpace.
   */
  public static final String P_OFORB = "outerForwardBias";
  public static double outer_forward_bias = -Double.MAX_VALUE;
  public static final String P_OLATB = "outerLateralBias";
  public static double outer_lateral_bias = -Double.MAX_VALUE;


   public static final String P_SoD = "ssUseSoD";
   public static boolean useSoD = false;
   public static final String P_FLOW = "ssFlowRate";
   public static double flowRate = Float.NaN;
   public static final String P_ECDENS = "ecDensity";
   public static float ecdens = Float.NaN;
   public static final String P_KCDENS = "kcDensity";
   public static float kcdens = Float.NaN;
   public static final String P_HEPDENS = "hepDensity";
   public static float hepdens = Float.NaN;
   public static final String P_EITHRESH = "eiThresh";
   public static final String P_EIRATE = "eiRate";
   public static final String P_EIRESP = "eiResponse";
   public static final String P_ELTHRESH = "elThresh";
   public static final String P_ELRATE = "elRate";
   public static final String P_ELRESP = "elResponse";
   static final String P_GSH_INC = "gshDepletionInc";
   static final String P_GSH_RANGE = "gshDepletionRange";
   static final String MEMBRANE_CROSS_PROB = "membraneCrossProb";
   static final String P_STRESS_RANGE = "stressRange";
   static final String P_NECROSIS_DELAY_MIN = "necrosisDelayMin";
   static final String P_NECROSIS_DELAY_MAX = "necrosisDelayMax";
   static final String ALT_AMOUNT = "ALTamount";
   static final String ALT_THRESHOLD = "ALTthreshold";
   static final String P_TRANSPORT_DELAY_MIN = "transportDelayMin";
   static final String P_TRANSPORT_DELAY_MAX = "transportDelayMax";
   
   public static final String VN_NEIGHBORHOODS = "vnNeighborhoods";
   public static final String MANHATTAN_DIST = "manhattanDist";
   public static final String ADJ_DIST = "adjDist";
   public static final String LAT_NEIGHBORS = "latNeighbors";
   public static final String DOWNSTREAM_COUNT = "downstreamCount";
   
   public static final String EXOS = "exos";
   public static final String EXO_MANHATTAN_DIST = "exoManhattanDist";
   public static final String P_EXO = "pEXO";
   
   /** Creates a new instance of SSParams */
   public SSParams() {
   }
   
   public static void loadParams(SS ss, ec.util.ParameterDatabase pdb) {
      if (pdb == null) return;
      ec.util.Parameter param = null;
            
      double iforb = pdb.getDoubleWithMax(param = new ec.util.Parameter(P_IFORB), null, 0.0, 1.0);
      if ( !(0.0 <= iforb && iforb <= 1.0) ) throw new RuntimeException(P_IFORB + " must be in [0,1].");
      inner_forward_bias = iforb;
      
      double ilatb = pdb.getDoubleWithMax(param = new ec.util.Parameter(P_ILATB), null, 0.0, 1.0);
      if ( !(0.0 <= ilatb && ilatb <= 1.0) ) throw new RuntimeException(P_ILATB+"("+ilatb+") must be in [0,1].");
      inner_lateral_bias = ilatb;
      
      double oforb = pdb.getDoubleWithMax(param = new ec.util.Parameter(P_OFORB), null, 0.0, 1.0);
      if ( !(0.0 <= oforb && oforb <= 1.0) ) throw new RuntimeException(P_OFORB + " must be in [0,1].");
      outer_forward_bias = oforb;
      
      double olatb = pdb.getDoubleWithMax(param = new ec.util.Parameter(P_OLATB), null, 0.0, 1.0);
      if ( !(0.0 <= olatb && olatb <= 1.0) ) throw new RuntimeException(P_OLATB+"("+olatb+") must be in [0,1].");
      outer_lateral_bias = olatb;
      
      double flow = pdb.getDoubleWithDefault(param = new ec.util.Parameter(P_FLOW), null, 1.0f);
      if ( flow < 0 ) throw new RuntimeException(P_FLOW + " must be ≥ 0.");
      flowRate = flow;
      
      useSoD = pdb.getBoolean(param = new ec.util.Parameter(P_SoD), null, false);
      if (ss.hepStruct.model.context.equals(ISLParams.CONTEXT_CULT) && useSoD) {
        log.warn(P_SoD + " coerced to false in Culture case.");
        useSoD = false;
      }

      float ecdens_l = pdb.getFloatWithMax(param = new ec.util.Parameter(P_ECDENS), null, 0.0F, 1.0F);
      if ( ss.hepStruct.model.context.equals(ISLParams.CONTEXT_CULT)) {
        if (ecdens_l > 0.0) log.warn(P_ECDENS + " is ignored in the Culture case.");
        ecdens = -Integer.MAX_VALUE;
      } else {
        if (ecdens_l < 0.0) throw new RuntimeException(P_ECDENS + " must be in [0,1].");
        ecdens = ecdens_l;
      }
      
      float kcdens_l = pdb.getFloatWithMax(param = new ec.util.Parameter(P_KCDENS), null, 0.0F, 1.0F);
      if (ecdens < 0.0F) {
        if (kcdens_l > 0.0F) log.warn(P_KCDENS + " is ignored if there is no EC space.");
        kcdens = -Integer.MAX_VALUE;
      } else {
        if (kcdens_l < 0.0F) throw new RuntimeException(P_KCDENS + " must be in [0,1].");
        else {
          kcdens = kcdens_l;
          if ((ecdens + kcdens) > 1.0F) throw new RuntimeException (P_ECDENS + " + " + P_KCDENS + " must be <= 1.");
        }
      }
      if (ecdens_l <= 0 && kcdens_l > 0) throw new RuntimeException (P_ECDENS + " ≤ 0 but "+P_KCDENS +" = "+kcdens_l);
      
      float hepdens_l = pdb.getFloatWithMax(param = new ec.util.Parameter(P_HEPDENS), null, 0.0F, 1.0F);
      if ( !(0.0F <= hepdens_l && hepdens_l <= 1.0F) ) throw new RuntimeException(P_HEPDENS + " must be in [0,1].");
      hepdens = hepdens_l;
      
      int eithresh = pdb.getInt(param = new ec.util.Parameter(P_EITHRESH), null);
      if ( eithresh < 1 ) throw new RuntimeException(P_EITHRESH + " must be > 0.");
      ss.ei_thresh = eithresh;
      
      double eirate = pdb.getDouble(param = new ec.util.Parameter(P_EIRATE), null, Double.NaN);
      ss.ei_rate = eirate;
      
      double eiresp = pdb.getDouble(param = new ec.util.Parameter(P_EIRESP), null, Double.NaN);
      if ( eirate < 0.0F ) throw new RuntimeException(P_EIRESP + " must be >= 0.");
      ss.ei_response_factor = eiresp;

      int elthresh = pdb.getInt(param = new ec.util.Parameter(P_ELTHRESH), null);
      if ( elthresh < 1 ) throw new RuntimeException(P_ELTHRESH + " must be > 0.");
      ss.el_thresh = elthresh;

      double elrate = pdb.getDouble(param = new ec.util.Parameter(P_ELRATE), null, Double.NaN);
      ss.el_rate = elrate;

      double elresp = pdb.getDouble(param = new ec.util.Parameter(P_ELRESP), null, Double.NaN);
      if ( elrate < 0.0 ) throw new RuntimeException(P_ELRESP + " must be >= 0.");
      ss.el_response_factor = elresp;

      double gsh_inc = pdb.getDouble(param = new ec.util.Parameter(P_GSH_INC), null, Double.NaN);
      if ( gsh_inc < 0.0 ) throw new RuntimeException(P_GSH_INC + " must be >= 0.");
      
      String val_s = pdb.getStringWithDefault(new ec.util.Parameter(P_GSH_RANGE), null, "<empty>");
      Hepatocyte.setGSHDepletion(Parameters.parseTuple(val_s),gsh_inc);

      val_s = pdb.getStringWithDefault(new ec.util.Parameter(P_STRESS_RANGE), null, "<empty>");
      NecrosisHandler.setStressRange(Parameters.parseTuple(val_s));
      
      int necrosis_delay_min = pdb.getInt(param = new ec.util.Parameter(P_NECROSIS_DELAY_MIN), null);
      if ( necrosis_delay_min < 0 ) throw new RuntimeException(P_NECROSIS_DELAY_MIN + " must be positive.");
      NecrosisHandler.NECROSIS_DELAY_MIN = necrosis_delay_min;
      
      int necrosis_delay_max = pdb.getInt(param = new ec.util.Parameter(P_NECROSIS_DELAY_MAX), null);
      if ( necrosis_delay_max < 0 ) throw new RuntimeException(P_NECROSIS_DELAY_MAX + " must be positive.");
      if ( !(necrosis_delay_max > necrosis_delay_min)) throw new RuntimeException(P_NECROSIS_DELAY_MAX + " must be > " + P_NECROSIS_DELAY_MIN);
      NecrosisHandler.NECROSIS_DELAY_MAX = necrosis_delay_max;
      
      int altAmount = pdb.getInt(param = new ec.util.Parameter(ALT_AMOUNT), null);
      if ( altAmount < 0 ) throw new RuntimeException(ALT_AMOUNT + " must be positive.");
      
      int altThreshold = pdb.getInt(param = new ec.util.Parameter(ALT_THRESHOLD), null);
      if ( altThreshold < 0 ) throw new RuntimeException(ALT_THRESHOLD + " must be positive.");
      Hepatocyte.setALT(altAmount, altThreshold);
      
      int transport_delay_min = pdb.getInt(param = new ec.util.Parameter(P_TRANSPORT_DELAY_MIN), null);
      if ( transport_delay_min < 0 ) throw new RuntimeException(P_TRANSPORT_DELAY_MIN + " must be positive.");
      Transporter.TRANSPORT_DELAY_MIN = transport_delay_min;
      
      int transport_delay_max = pdb.getInt(param = new ec.util.Parameter(P_TRANSPORT_DELAY_MAX), null);
      if ( transport_delay_max < 0 ) throw new RuntimeException(P_TRANSPORT_DELAY_MAX + " must be positive.");
      if ( !(transport_delay_max >= transport_delay_min)) throw new RuntimeException(P_TRANSPORT_DELAY_MAX + " must be > " + P_TRANSPORT_DELAY_MIN);
      Transporter.TRANSPORT_DELAY_MAX = transport_delay_max;

      boolean vnNeighborhoods = pdb.getBoolean(param = new ec.util.Parameter(VN_NEIGHBORHOODS), null, false);
      NecrosisHandler.vnNeighborhoods = vnNeighborhoods;
      
      int manhattanDist = pdb.getInt(param = new ec.util.Parameter(MANHATTAN_DIST), null);
      if ( manhattanDist < 0 ) throw new RuntimeException(MANHATTAN_DIST + " must be positive.");
      NecrosisHandler.manhattanDist = manhattanDist;
      
      int adjDist = pdb.getInt(param = new ec.util.Parameter(ADJ_DIST), null);
      if ( adjDist < 0 ) throw new RuntimeException(ADJ_DIST + " must be positive.");
      Hepatocyte.adjDist = adjDist;
      
      int latNeighbors = pdb.getInt(param = new ec.util.Parameter(LAT_NEIGHBORS), null);
      if ( latNeighbors < 0 ) throw new RuntimeException(LAT_NEIGHBORS + " must be positive.");
      NecrosisHandler.latNeighbors = latNeighbors;
      
      int downstreamCount = pdb.getInt(param = new ec.util.Parameter(DOWNSTREAM_COUNT), null);
      if ( downstreamCount < 0 ) throw new RuntimeException(DOWNSTREAM_COUNT + " must be positive.");
      NecrosisHandler.downstreamCount = downstreamCount;
      
      boolean exos = pdb.getBoolean(param = new ec.util.Parameter(EXOS), null, false);
      NecrosisHandler.exos = exos;
      Hepatocyte.exos = exos;
      
      int exoManhattanDist = pdb.getInt(param = new ec.util.Parameter(EXO_MANHATTAN_DIST), null);
      if ( exoManhattanDist < 0 ) throw new RuntimeException(EXO_MANHATTAN_DIST + " must be positive.");
      Hepatocyte.exoManhattanDist = exoManhattanDist;
      
      double pEXO = pdb.getDouble(param = new ec.util.Parameter(P_EXO), null);
      if ( pEXO < 0 ) throw new RuntimeException(P_EXO + " must be positive.");
      EXOHandler.pEXO = pEXO;

   }
   
}
