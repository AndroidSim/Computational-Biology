/*
 * Copyright 2003-2018 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model;

import ec.util.Parameter;
import ec.util.ParameterDatabase;
import isl.io.Parameters;
import isl.io.Parameters.TESTS;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

public strictfp class MobileObjectType extends isl.io.Propertied {
   static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MobileObjectType.class );
   public String tag = null;
   public boolean bindable = false;
   private boolean isAmplified = false;
   public boolean isAmplified() { return isAmplified; }
   public sim.util.Int2D ampRange = null;
    
   /**
    * Creates a new instance of DoseEntry
   * @param n - name of the MobileObject
   * @param b - bindable?
   * @param p - dose properties
    */
   public MobileObjectType(String n, boolean b, LinkedHashMap<String,Object> p) {
       init(n, b, p);
   }
   
   public void init(String n, boolean b, LinkedHashMap<String,Object> p) {
      if (n != null) tag = n;
      else throw new RuntimeException("DoseEntry: Name can't be null.");
      bindable = b;
      if ( p.size() > 0 ) properties = p;
      else throw new RuntimeException("DoseEntry: Must be at least one proprerty.");
      if (p.containsKey("Amplify")) {
        isAmplified = (boolean)p.get("Amplify");
        if (isAmplified) {
          if (!p.containsKey("AmpRange")) throw new RuntimeException(tag+" has Amplify property but no AmpRange.");
          sim.util.Double2D ar_d = isl.io.Parameters.parseTuple((String)properties.get("AmpRange"));
          double min = Math.floor(ar_d.x);
          double max = Math.ceil(ar_d.y);
          if (min != ar_d.x || max != ar_d.y) log.warn("\nWARNING!!! Converting "+tag+".AmpRange = <"+ar_d.x+","+ar_d.y+"> to <"+(int)min+","+(int)max+">!\n");
          ampRange = new sim.util.Int2D((int)Math.floor(ar_d.x), (int)Math.ceil(ar_d.y));
        }
      }
   }

  @Override
  public boolean equals (Object o) {
    boolean retVal = false;
    if (o == null || !(o instanceof MobileObjectType)) throw new RuntimeException("Cannot compare with other DoseEntry objects.");
    MobileObjectType be = (MobileObjectType)o;
    if (be.tag == null) throw new RuntimeException("DoseEntry tag cannot be null.");
    retVal = tag.equals(be.tag);
    if (retVal) {
      if (bindable != be.bindable)
        throw new RuntimeException("DoseEntry "+tag+".bindable != "+be.tag+".bindable");
      if (!propCompare(properties, be.properties))
        throw new RuntimeException("DoseEntry "+tag+".props != "+be.tag+".props");
    }
    return retVal;
  }
  private boolean propCompare(LinkedHashMap<String,Object> p1, LinkedHashMap<String,Object> p2) {
    boolean retVal = true;
    for (java.util.Map.Entry<String,Object> me : p1.entrySet()) {
      if (me.getValue() instanceof LinkedHashMap) {
        @SuppressWarnings("unchecked")  // because the properties structure is ignorant of the value type
        LinkedHashMap<String,Object> submap1 = (LinkedHashMap<String,Object>)me.getValue();
        if (p2.get(me.getKey()) instanceof LinkedHashMap) {
          @SuppressWarnings("unchecked") // because the properties structure is ignorant of the value type
          LinkedHashMap<String,Object> submap2 = (LinkedHashMap<String,Object>)p2.get(me.getKey());
          retVal = propCompare(submap1, submap2);
        } else
          throw new RuntimeException("DoseEntry "+tag+"."+me.getKey()+" mismatch.");
      } else {
        if (!(p2.containsKey(me.getKey()) && me.getValue().equals(p2.get(me.getKey()))))
          retVal = false;
      }
    }
    return retVal;
  }
  @Override
  public int hashCode() {
    return tag.hashCode();
  }
  
  
  static final String BODY_XFER_TYPES = "bodyXferTypes";
  static final String TYPES = "types";
  static final String TAG = "tag";
  static final String BINDABLE = "bindable";
  static final String NUMPROPS = "numProps";
  static final String KEY = "key";
  static final String TYPE = "type";
  static final String TEST = "test";
  static final String VAL = "val";
  public static void loadTypeParams(ISL isl) {
    ParameterDatabase pdb = null;
    try {
      pdb = new ec.util.ParameterDatabase( MobileObjectType.class.getClassLoader().getResourceAsStream("cfg/types.properties"));
    } catch (java.io.IOException ioe) {
      System.err.println( ioe.getMessage() );
      System.exit( -1 );
    }
    Parameter param = null;
    StringBuilder pk = null;
    HashSet<MobileObjectType> mthash = new HashSet<>();
    int tNdx = 0;
    while (true) {
      String typePrefix = TYPES + DeliveryParams.DELIM + tNdx + DeliveryParams.DELIM;
      pk = new StringBuilder(typePrefix + TAG);
      String tag = pdb.getString(param = new Parameter(pk.toString()), null);
      if (tag == null) {
        break;
      }
      pk = new StringBuilder(typePrefix + BINDABLE);
      boolean bindable = pdb.getBoolean(param = new Parameter(pk.toString()), null, false);
      pk = new StringBuilder(typePrefix + NUMPROPS);
      int numProps = pdb.getInt(param = new Parameter(pk.toString()), null);
      if (numProps < 1) {
        throw new RuntimeException(pk + " must be > 0.");
      }
      LinkedHashMap<String, Object> props = new LinkedHashMap<>(numProps);
      for (int pNdx = 0; pNdx < numProps; pNdx++) {
        String propPrefix = typePrefix + DeliveryParams.PROP_PRE + DeliveryParams.DELIM + pNdx + DeliveryParams.DELIM;
        pk = new StringBuilder(propPrefix + KEY);
        String propKey = pdb.getString(param = new Parameter(pk.toString()), null);
        if (propKey == null || propKey.equals("")) {
          throw new RuntimeException(pk + " must be non-null.");
        }
        pk = new StringBuilder(propPrefix + TYPE);
        String propType = pdb.getString(param = new Parameter(pk.toString()), null);
        if (propType == null || propType.equals("")) {
          throw new RuntimeException(pk + " must be non-null.");
        }
        
        pk = new StringBuilder(propPrefix + TEST);
        String test_s = pdb.getStringWithDefault(param = new ec.util.Parameter(pk.toString()), null, "sums");
        TESTS test = null;
        try {
          test = TESTS.valueOf(test_s);
        } catch (IllegalArgumentException iae) {
          throw new RuntimeException("Test "+test_s+" does not exist for "+pk.toString(), iae);
        }

        // if we recognize the type, then read the value
        Object val = Parameters.getPropVal(pdb, propType, propPrefix + VAL, test);
        props.put(propKey, val);
      } // end for (int pNdx = 0; pNdx < numProps; pNdx++)
      Parameters.log.debug("DoseParams: props = " + props);
      MobileObjectType mt = new MobileObjectType(tag, bindable, props);
      // test for multiple entries
      for (MobileObjectType mte : mthash) {
        if (mte.tag.equals(mt.tag) && !mte.equals(mt)) {
          throw new RuntimeException("\u2203 multiple " + mt.tag + " entries with different values.");
        }
      }
      mthash.add(mt);
      tNdx++;
    }
    // keep a unique, ordered list of all the entries
    isl.allMobileObject = new ArrayList<>(mthash);
    pk = new StringBuilder(BODY_XFER_TYPES);
    String xt_s = pdb.getString(param = new Parameter(pk.toString()), null);
    xt_s = xt_s.trim();
    if (xt_s == null || xt_s.equals("")) {
      throw new RuntimeException(BODY_XFER_TYPES + " can't be null.");
    }
    ArrayList<String> xt_al = Parameters.parseSimpleList(xt_s);
    for (String type : xt_al) {
      boolean matches = false;
      for (MobileObjectType mt : isl.allMobileObject) {
        if (mt.tag.equals(type)) {
          matches = true;
        }
      }
      if (!matches) {
        throw new RuntimeException("BodyXferType: " + type + " not found in dose entries.");
      }
    }
    isl.bodyXferTypes = xt_al;
  } // end loadTypeParams
  
}
