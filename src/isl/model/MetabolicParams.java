/*
 * Copyright 2003-2010 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import isl.io.Parameters;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public class MetabolicParams implements Parameters {

  static final String DELIM = ".";

  static final String P_BINDMIN = "bindersPerCellMin";
  static final String P_BINDMAX = "bindersPerCellMax";
   
  static final String BINDING_MODE = "bindingMode";

  static final String NUMCELLS = "numCells";
  static final String CELL_PRE = "cell";
  static final String CELL_TAG = "tag";
  static final String GROUPS = "groups";

  static final String RXN_PRODS_TO_COUNT = "rxnProdsToCount";
  
  static final String NUMGROUPS = "numGroups";
  static final String GROUP_PRE = "group";
  static final String GROUP_TAG = "tag";
  static final String BIND_PROB = "bindProb";
  static final String BIND_CYCLES = "bindCycles";
  static final String ACCEPTED_MOBILEOBJECTS = "acceptedMobileObjects";
  public static final String DR_REPLENISH = "drReplenish";
  public static final String DR_REMOVE = "drRemove";
  public static final String DR_INTERVAL = "drInterval";
  public static final String DR_CAP_DELTA = "drCapDelta";
  public static final String DR_PR_DELTA = "drPrDelta";
  static final String DOWN_REGULATED_BY = "downRegulatedBy";

  static final String NUMPROPS = "numProps";
  static final String PROP_PRE = "property";

  static final String KEY = "key";
  static final String TYPE = "type";
  static final String VAL = "val";

  public static void loadParams(MetabolicEnvironment environment, ec.util.ParameterDatabase pdb) {
    if (pdb == null) throw new RuntimeException("MetabolicParams: parameter database cannot be null");
    ec.util.Parameter param = null;
    StringBuilder pk = null;

    int bindmin = pdb.getInt(param = new ec.util.Parameter(P_BINDMIN), null);
    if ( bindmin < 0 ) throw new RuntimeException(P_BINDMAX + " must be positive.");
    environment.bindmin = bindmin;
      
    int bindmax = pdb.getInt(param = new ec.util.Parameter(P_BINDMAX), null);
    if ( bindmax < 0 ) throw new RuntimeException(P_BINDMAX + " must be positive.");
    if ( !(bindmax > bindmin)) throw new RuntimeException(P_BINDMAX + " must be > " + P_BINDMIN);
    environment.bindmax = bindmax;
      
    pk = new StringBuilder(BINDING_MODE);
    String bindingMode = pdb.getString(param = new ec.util.Parameter(pk.toString()), null);
    if ( bindingMode == null || bindingMode.equals("") )
      throw new RuntimeException(pk + " must be non-null.");
    environment.bindingMode = bindingMode;

    pk = new StringBuilder(NUMCELLS);
    int numCells = pdb.getInt(param = new ec.util.Parameter(pk.toString()), null);
    if(numCells < 1) {
      throw new RuntimeException(pk + " must be > 0.");
    }
        
    //Keep track of unique EnzymeGroup tags to verify against numGroups.
    ArrayList<String> allGroups = new ArrayList<>();
        
    //Keep track of unique Cell tags to ensure there are no repeat entries.
    ArrayList<String> allCells = new ArrayList<>();
        
    for(int i = 0; i < numCells; i++) {
      String cellPrefix = CELL_PRE + DELIM + i + DELIM;
            
      pk = new StringBuilder(cellPrefix + CELL_TAG);
      String cell_tag = pdb.getString(param = new ec.util.Parameter(pk.toString()), null);
      if(cell_tag == null || cell_tag.equals(""))
        throw new RuntimeException(pk + " must be non-null.");
      if(!allCells.contains(cell_tag)) allCells.add(cell_tag);
      else
        throw new RuntimeException(cell_tag + "entry already exists.");

      pk = new StringBuilder(cellPrefix + GROUPS);
      ArrayList<String> groups = (ArrayList<String>)Parameters.getPropVal(pdb,T_STRING_LIST,pk.toString());
      //If it isn't already in allGroups, add it there too
      groups.stream().filter((group) -> 
              (!allGroups.contains(group))).forEachOrdered((group) -> { 
                allGroups.add(group); 
              });
      environment.cellTypeToEnzymeGroupNames.put(cell_tag, groups);
    }
    
    isl.model.cell.ReactionHandler.rxnProdsToCount = (ArrayList<String>)Parameters.getPropVal(pdb,T_STRING_LIST,RXN_PRODS_TO_COUNT);
    
    pk = new StringBuilder(NUMGROUPS);
    int numGroups = pdb.getInt(param = new ec.util.Parameter(pk.toString()), null);
    if(numGroups < 1) 
      throw new RuntimeException(pk + " must be > 0.");
    //Verify that all groups are assigned to a cell type
    if(numGroups != allGroups.size())
      log.warn(pk + " Cell:EnzymeGroup mismatch -- numGroups = "+numGroups+", "+allGroups.size()+" groups assigned to cells.");
        
    for(int gNdx = 0; gNdx < numGroups; gNdx++) {
      String groupPrefix = GROUP_PRE + DELIM + gNdx + DELIM;
            
      pk = new StringBuilder(groupPrefix + GROUP_TAG);
      String group_tag = pdb.getString(param = new ec.util.Parameter(pk.toString()), null);
      if(group_tag == null || group_tag.equals(""))
        throw new RuntimeException(pk + " must be non-null.");
      if(environment.enzymeGroups.containsKey(group_tag))
        throw new RuntimeException(group_tag + " entry already exists.");
            
      pk = new StringBuilder(groupPrefix + BIND_PROB);
      double bind_prob = pdb.getDouble(param = new ec.util.Parameter(pk.toString()), null, 0.0D);
      if(bind_prob < 0) throw new RuntimeException(bind_prob + " must be > 0.0.");
            
      pk = new StringBuilder(groupPrefix + BIND_CYCLES);
      int bind_cycles = pdb.getInt(param = new ec.util.Parameter(pk.toString()), null, 1);
      if(bind_cycles < 1) throw new RuntimeException(bind_cycles + " must be > 1.");
            
      ArrayList<String> acceptedMobileObjects = new ArrayList<>();
      pk = new StringBuilder(groupPrefix + ACCEPTED_MOBILEOBJECTS);
      String val_s_mobileObjects = pdb.getStringWithDefault(new ec.util.Parameter(pk.toString()), null, "<empty>");
      String[] val_sa_mobileObjects = val_s_mobileObjects.split(";");
      for(String str : val_sa_mobileObjects) {
        String mobileObject = str.trim();
        acceptedMobileObjects.add(mobileObject);
      }            

      double drreplenish = pdb.getDouble(param = new ec.util.Parameter(DR_REPLENISH), null, 0.0);
      if ( drreplenish < 0.0F ) throw new RuntimeException(DR_REPLENISH + " must be >= 0.");
      environment.drReplenish = drreplenish;  
      
      double drremove = pdb.getDouble(param = new ec.util.Parameter(DR_REMOVE), null, 0.0);
      if ( drremove <= 0.0F ) throw new RuntimeException(DR_REMOVE + " must be > 0.");
      environment.drRemove = drremove;
      
      double drinterval = pdb.getDouble(param = new ec.util.Parameter(DR_INTERVAL), null, 0.0);
      if ( drinterval < 0.0 ) throw new RuntimeException(DR_INTERVAL + " must be ≥ 0.0.");
      environment.drInterval = drinterval;

      int drCapΔ = pdb.getIntWithDefault(param = new ec.util.Parameter(DR_CAP_DELTA), null, 1);
      environment.drCapΔ = drCapΔ;
      if (drCapΔ < 1) {
        log.warn(DR_CAP_DELTA +" < 1 ⇒ "+DR_INTERVAL+" ≡ 0, thereby turning down regulation off.");
        environment.drInterval = 0.0;
      }
      
      double drPrΔ = pdb.getDoubleWithMax(param= new ec.util.Parameter(DR_PR_DELTA), null, 0.0, 1.0);
      environment.drPrΔ = drPrΔ;
      
      java.util.ArrayList<String> drb = (ArrayList<String>)Parameters.getPropVal(pdb, T_STRING_LIST, groupPrefix+DOWN_REGULATED_BY);
      log.info(groupPrefix+DOWN_REGULATED_BY+" = "+drb);
      
      pk = new StringBuilder(groupPrefix + NUMPROPS);
      int numProps = pdb.getInt(param = new ec.util.Parameter(pk.toString()), null);
      if(numProps < 1)
        throw new RuntimeException(pk + " must be > 0.");
            
      LinkedHashMap<String,Object> props = new LinkedHashMap<>(numProps);
      for (int pNdx = 0; pNdx < numProps; pNdx++) {
        String propPrefix = groupPrefix + PROP_PRE + DELIM + pNdx + DELIM;
                
        pk = new StringBuilder(propPrefix + KEY);
        String propKey = pdb.getString(param = new ec.util.Parameter(pk.toString()), null);
        if (propKey == null || propKey.equals(""))
          throw new RuntimeException(pk + " must be non-null.");
     
        pk = new StringBuilder(propPrefix + TYPE);
        String propType = pdb.getString(param = new ec.util.Parameter(pk.toString()), null);
        if (propType == null || propType.equals(""))
          throw new RuntimeException(pk + " must be non-null.");

        // if we recognize the type, then read the value
        Object val = Parameters.getPropVal(pdb, propType, propPrefix+VAL);
        props.put(propKey, val);
      }
        
           // binding probability property testing
           if (props.containsKey("bindProbGradient")) {
             if (props.containsKey("bindProbStart") || props.containsKey("bindProbFinish")) {
               if (!(props.containsKey("bindProbStart") && props.containsKey("bindProbFinish")))
                 throw new RuntimeException(group_tag+": One of bindProb[Start|Finish] is absent.");
               double bps = ((Double)props.get("bindProbStart"));
               double bpf = ((Double)props.get("bindProbFinish"));
               if ((bps > 1.0 || bps < 0.0) || (bpf > 1.0 || bpf < 0.0))
                 throw new RuntimeException(group_tag
                         + "bindProb[Start|Finish] > 1.0 or bindProb[Start|Finish] < 0.0.");
               log.info("binding-gradient: "+props.get("bindProbGradient")
                       +", start = "+bps+", finish = "+bpf+"\n");
             }
           }
        
           // reaction probability property testing
             if (props.containsKey("rxnProbStart") || props.containsKey("rxnProbFinish")) {
               if (!(props.containsKey("rxnProbStart") && props.containsKey("rxnProbFinish")))
                 throw new RuntimeException(group_tag+": One of rxnProb[Start|Finish] is absent.");
               double rps = ((Double)props.get("rxnProbStart"));
               double rpf = ((Double)props.get("rxnProbFinish"));
               if ((!props.containsKey("rxnProducts") && (rps > 0.0 || rpf > 0.0)))
                 throw new RuntimeException(group_tag
                         + "Does not contain a rxnProducts map."
                         + "And rxnProb[Start|Finish] > 0.0.");
             }

           // testing sigmoid gradient parameters
           if (props.containsKey("rxnProbGradient")) {
               String gradtype = (String)props.get("rxnProbGradient");
               if (gradtype.equals("sigmoid")) {
                   if (props.containsKey("sigShiftSharp")) {
                       // parameter is a string
                       String sh2str = ((String)props.get("sigShiftSharp"));
                       sim.util.Double2D shiftsharp = Parameters.parseTuple(sh2str);
                       // sharp/intensity and shift can not be zero
                       double shift = shiftsharp.x;
                       double sharp = shiftsharp.y;
                       if (shift == 0.0 || sharp == 0.0) {
                           throw new RuntimeException("either shift "+shift+" or sharp "+sharp+"can not be zero");
                       }
                       //log.info("reaction sigmoid: shift = "+shift+", sharp = "+sharp+"\n");
                    } 
               }   
           }
             
        
        
      EnzymeGroup eg = new EnzymeGroup(group_tag, -1, bind_prob, bind_cycles, acceptedMobileObjects, props);
      eg.downRegulatedBy = drb;
      environment.enzymeGroups.put(group_tag, eg);
    } // end     for(int gNdx = 0; gNdx < numGroups; gNdx++) {
    
    /*
    StringBuilder out = new StringBuilder("MetabolicEnvironment.loadParams() - enzymeGroups = ");
    for (EnzymeGroup eg : environment.enzymeGroups.values()) out.append(eg.type+" ");
    log.info(out.toString());    /*    /*
    StringBuilder out = new StringBuilder("MetabolicEnvironment.loadParams() - enzymeGroups = ");
    for (EnzymeGroup eg : environment.enzymeGroups.values()) out.append(eg.type+" ");
    log.info(out.toString());    /*
    */
    
  } // end loadParams()
}
