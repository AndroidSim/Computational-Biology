/*
 * Copyright 2003-2020 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import java.util.ArrayList;
import isl.io.Parameters;

public strictfp class HepStructParams implements isl.io.Parameters {
   
   /**
    * These are the strings used in the isl.properties file.
    */
   public static final String P_APERC = "ssTypeAPercent";
   public static final String P_BPERC = "ssTypeBPercent";
   public static final String P_ACIRCMIN = "ssTypeACircMin";
   public static final String P_ACIRCMAX = "ssTypeACircMax";
   public static final String P_ALENA = "ssTypeALen.alpha";
   public static final String P_ALENB = "ssTypeALen.beta";
   public static final String P_ALENS = "ssTypeALen.shift";
   public static final String P_BCIRCMIN = "ssTypeBCircMin";
   public static final String P_BCIRCMAX = "ssTypeBCircMax";
   public static final String P_BLENA = "ssTypeBLen.alpha";
   public static final String P_BLENB = "ssTypeBLen.beta";
   public static final String P_BLENS = "ssTypeBLen.shift";
   public static final String P_LLCLAMPED = "ssLastLayerClamped";
   public static final String P_LLCIRC = "ssLastLayerCirc";
   public static final String P_LLLENG = "ssLastLayerLength";
   public static final String INFLAM_STIM_THRESHOLD = "inflammatoryStimulusThreshold";
   public static final String EXPONENTIAL_FACTOR = "exponentialFactor";
   public static final String INDUCES_TOX_MED_TRESHOLD = "inducesToxicMediatorThreshold";
   public static final String INDUCES_PROT_MED_TRESHOLD = "inducesProtectiveMediatorThreshold";
   public static final String UNI_GRAD_TYPE = "uniGradtype";
   public static final String DEFAULT_UG_TYPE = "Linear";
   public static final String UNI_GRAD_RANGE = "uniGradrange";

   public static final String HEPSTRUCT_SPEC_FILENAME = "hepstructspec.properties";
   
   public static void loadSpec(HepStruct l) {
      
      java.io.StreamTokenizer specStream = new java.io.StreamTokenizer(
           new java.io.BufferedReader(
           new java.io.InputStreamReader(
           ClassLoader.getSystemResourceAsStream("cfg/hepstructspec.properties"))));
      
      specStream.commentChar('#');
      specStream.eolIsSignificant(true);
      specStream.parseNumbers();
      int tok;
      boolean startLayers = false;
      boolean doneLayers = false;
      ArrayList<Integer> layers = new ArrayList<>();
      boolean startEdges = false;
      boolean doneEdges = false;
      int[][] edges = null;
      int row = 0;
      int col = 0;
      
      try {
         
         while ((tok = specStream.nextToken()) != java.io.StreamTokenizer.TT_EOF) {
            if (tok == java.io.StreamTokenizer.TT_EOL) {
               if (startLayers && !doneLayers) {
                  doneLayers = true;
                  edges = new int[layers.size()][layers.size()];
               }
               if (doneLayers && startEdges && !doneEdges) {
                  row++;
                  col = 0;
                  if (row >= layers.size()) {
                     doneEdges = true;
                     break;
                  }
               }
            }
            
            if (tok == java.io.StreamTokenizer.TT_NUMBER) {
               if (specStream.nval != StrictMath.floor(specStream.nval))
                  throw new RuntimeException(specStream.nval +
                       " is invalid.  HepStruct spec should only contain integers.");
               int val = (int)specStream.nval;
               
               // is it number of nodes in a layer?
               if (!startLayers) startLayers = true;
               if (startLayers && !doneLayers) {
                  layers.add(val);
               }
               
               if (doneLayers && !startEdges) startEdges = true;
               if (startEdges && !doneEdges) {
                  if (row < layers.size() && col < layers.size()) {
                     edges[row][col++] = val;
                  } else {
                     throw new RuntimeException("Too many rows or columns in the hepstructspec.");
                  }
               }
            }
         }
      } catch (java.io.IOException ioe) {
         System.err.println(ioe.getMessage());
         System.exit(-1);
      }
      if (l.model.context.equals(ISLParams.CONTEXT_CULT)) {
        if (layers.size() > 1) log.warn("Hepatic structure specification layers coerced to 1 and edges coerced to 0 for Culture case!");
        int nodeSum = 0;
        nodeSum = layers.stream().map((nodes) -> nodes).reduce(nodeSum, Integer::sum);
        l.layers = new ArrayList<>(1);
        l.layers.add(nodeSum);
        l.edges = new int[1][1];
        l.edges[0][0] = 0;
      } else {
        l.layers = layers;
        l.edges = edges;
      }
   }

   public static void loadParams(HepStruct l, ec.util.ParameterDatabase pdb) {
      if (pdb == null) return;
      ec.util.Parameter param = null;
      
      float aperc = pdb.getFloatWithMax(param = new ec.util.Parameter(P_APERC), null, 0.0F, 1.0F);
      if (! (0.0F <= aperc && aperc <= 1.0F)) throw new RuntimeException("Invalid value for "+param);
      float bperc = pdb.getFloatWithMax(param = new ec.util.Parameter(P_BPERC), null, 0.0F, 1.0F);
      if (! (0.0F <= bperc && bperc <= 1.0F)) throw new RuntimeException("Invalid value for "+param);
      float sum = aperc + bperc;
      if ( 1.0F - Float.MIN_VALUE < sum || sum < 1.0F + Float.MIN_VALUE )
         throw new RuntimeException(P_APERC + " + " + P_BPERC + " = " + sum + " must sum to 1.");
      l.aperc = aperc;
      l.bperc = bperc;
      
      int circmin = pdb.getInt(param = new ec.util.Parameter(P_ACIRCMIN), null);
      if ( circmin < 2 ) throw new RuntimeException(param + " must be > 1.");
      l.acircmin = circmin;
      int circmax = pdb.getInt(param = new ec.util.Parameter(P_ACIRCMAX), null);
      if ( circmax < 2 ) throw new RuntimeException(param + " must be > 1.");
      if ( !(circmin <= circmax)) throw new RuntimeException(param + " < " + P_ACIRCMIN);
      l.acircmax = circmax;

      float alena = pdb.getFloat(param = new ec.util.Parameter(P_ALENA), null, 0.0F);
      if ( alena <= 0.0F ) throw new RuntimeException(param + " = " + alena + " must be > 0.0.");
      l.alena = alena;
      
      float alenb = pdb.getFloat(param = new ec.util.Parameter(P_ALENB), null, 0.0F);
      if ( alenb <= 0.0F ) throw new RuntimeException(param + " must be > 0.0.");
      l.alenb = alenb;
      
      float alens = pdb.getFloat(param = new ec.util.Parameter(P_ALENS), null, -Float.MAX_VALUE);
      l.alens = alens;
      
      circmin = pdb.getInt(param = new ec.util.Parameter(P_BCIRCMIN), null);
      if ( circmin < 2 ) throw new RuntimeException(param + " must be > 1.");
      l.bcircmin = circmin;
      circmax = pdb.getInt(param = new ec.util.Parameter(P_BCIRCMAX), null);
      if ( circmax < 2 ) throw new RuntimeException(param + " must be > 2.");
      if ( !(circmin <= circmax)) throw new RuntimeException(param + " < " + P_BCIRCMIN);
      l.bcircmax = circmax;
      
      float blena = pdb.getFloat(param = new ec.util.Parameter(P_BLENA), null, 0.0F);
      if ( blena <= 0.0F ) throw new RuntimeException(param + " must be > 0.0.");
      l.blena = blena;
      
      float blenb = pdb.getFloat(param = new ec.util.Parameter(P_BLENB), null, 0.0F);
      if ( blenb <= 0.0F ) throw new RuntimeException(param + " must be > 0.0.");
      l.blenb = blenb;
      
      float blens = pdb.getFloat(param = new ec.util.Parameter(P_BLENS), null, -Float.MAX_VALUE);
      l.blens = blens;

      l.lastLayerClamped = pdb.getBoolean(param = new ec.util.Parameter(P_LLCLAMPED), null, false);
      if (l.model.context.equals(ISLParams.CONTEXT_CULT) && l.lastLayerClamped) {
        log.warn("--------------------------------------------------------------");
        log.warn(P_LLCLAMPED + " = true & context = "+l.model.context);
        log.warn("All SSes will be the same size.");
        log.warn("All the Type A and B SS geometry parameters are being ignored!");
        log.warn("--------------------------------------------------------------");
      }
      if (l.lastLayerClamped) {
        l.lastLayerCirc = pdb.getInt(param=new ec.util.Parameter(P_LLCIRC), null, 2);
        l.lastLayerLength = pdb.getInt(param=new ec.util.Parameter(P_LLLENG), null, 1);
      }
      
      int infstimthresh = pdb.getInt(param = new ec.util.Parameter(INFLAM_STIM_THRESHOLD), null, 0);
      if ( infstimthresh < 0 ) throw new RuntimeException(INFLAM_STIM_THRESHOLD + " must be >= 0.");
      l.inflammatoryStimulusThreshold = infstimthresh;
      
      double expofact = pdb.getDouble(param = new ec.util.Parameter(EXPONENTIAL_FACTOR), null, 0.0);
      if ( expofact <= 0.0F ) throw new RuntimeException(EXPONENTIAL_FACTOR + " must be > 0.");
      l.exponentialFactor = expofact;

      int indutoxmedthresh = pdb.getInt(param = new ec.util.Parameter(INDUCES_TOX_MED_TRESHOLD), null, 0);
      if ( indutoxmedthresh < 0 ) throw new RuntimeException(INDUCES_TOX_MED_TRESHOLD + " must be >= 0.");
      l.inducesToxicMediatorThreshold = indutoxmedthresh;
      
      int induprotmedthresh = pdb.getInt(param = new ec.util.Parameter(INDUCES_PROT_MED_TRESHOLD), null, 0);
      if ( induprotmedthresh < 0 ) throw new RuntimeException(INDUCES_PROT_MED_TRESHOLD + " must be >= 0.");
      l.inducesProtectiveMediatorThreshold = induprotmedthresh;
      
      // read in unified gradient parameters
      // unified gradient type
      String uGtype = pdb.getStringWithDefault(param = new ec.util.Parameter(UNI_GRAD_TYPE), null, DEFAULT_UG_TYPE);
      uGtype = uGtype.trim();
      l.uniGradtype = uGtype;
      // unified gradient range
      String uGrange = pdb.getStringWithDefault(new ec.util.Parameter(UNI_GRAD_RANGE), null, "<empty>");
      l.uniGradrange = Parameters.parseTuple(uGrange);
      if (l.uniGradrange.x == l.uniGradrange.y) {
          throw new RuntimeException("unified gradient: the start "+l.uniGradrange.x+" == the end "+l.uniGradrange.y);
      }
   }
}
