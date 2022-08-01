/*
 * Copyright 2016-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.io;

import java.util.LinkedHashMap;
import sim.util.Double2D;

public interface Parameters {
  public static com.owlike.genson.Genson genson = new com.owlike.genson.Genson();
  static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( Parameters.class );
  public static void loadParams(Object tgt, ec.util.ParameterDatabase pdb) {
    System.err.println("Error!  Parameters should always be specialized.");
    System.exit(-1);
  }
  
  static final String T_BOOL = "boolean";
  static final String T_INTEGER = "integer";
  static final String T_REAL = "real";
  static final String T_STRING = "string";
  static final String T_STRING_LIST = "slist";
  static final String T_MAP1D = "map1d";
  static final String T_MAP2D = "map2d";
  public static enum TESTS {sums, exists};
  public static Object getPropVal(ec.util.ParameterDatabase pdb, String propType, String propKey) {
    return getPropVal(pdb, propType, propKey, TESTS.sums);
  }
  public static Object getPropVal(ec.util.ParameterDatabase pdb, String propType, String propKey, TESTS test) {
    Object val = null;
    ec.util.Parameter param = null;
    switch (propType) {
      case T_REAL:
        double val_d = pdb.getDouble(param = new ec.util.Parameter(propKey), null, 0.0D);
        val = val_d;
        break;
      case T_BOOL:
        boolean val_b = pdb.getBoolean(param = new ec.util.Parameter(propKey), null, false);
        val = val_b;
        break;
      case T_INTEGER:
        int val_i = pdb.getIntWithDefault(param = new ec.util.Parameter(propKey), null, 0);
        val = val_i;
        break;
      case T_STRING:
        {
          String val_s = pdb.getStringWithDefault(new ec.util.Parameter(propKey), null, "<empty>");
          val = val_s;
          break;
        }
      case T_STRING_LIST:
        {
          String val_s = pdb.getStringWithDefault(new ec.util.Parameter(propKey), null, "<empty>");
          String[] val_sa = val_s.split(";");
          // should return ArrayList<String>
          val = java.util.Arrays.stream(val_sa).map(s -> s.trim()).collect(java.util.stream.Collectors.toList());
          break;
        }
      case T_MAP1D:
        {
          String val_s = pdb.getStringWithDefault(new ec.util.Parameter(propKey), null, "<empty>");
          String[] val_sa = val_s.split(";");
          LinkedHashMap<String,Double> val_lhm = new LinkedHashMap<>(val_sa.length);
          for (String me : val_sa) {
            String[] me_sa = me.split("=>");
            String k = me_sa[0].trim();
            Double v = Double.parseDouble(me_sa[1]);
            val_lhm.put(k,v);
          }   val = val_lhm;
          break;
        }
      case T_MAP2D:
        {
          log.info("Reading "+propKey);
          String val_s = pdb.getStringWithDefault(new ec.util.Parameter(propKey), null, "<empty>");
          String[] val_sa = val_s.split(";");
          LinkedHashMap<String, Double2D> val_lhm = new LinkedHashMap<>(val_sa.length);
          for (String me : val_sa) {
            String[] me_sa = me.split("=>");
            String k = me_sa[0].trim();
            log.info("\t"+k+" = "+me_sa[1]);
            String[] startStop_s = me_sa[1].split(",");
            double v1 = Double.parseDouble(startStop_s[0].replace('<', ' '));
            double v2 = Double.parseDouble(startStop_s[1].replace('>', ' '));
            val_lhm.put(k, new Double2D(v1, v2));
          }   // validate map ratios
          if (test == TESTS.sums) {
            java.math.BigDecimal bdsum1 = java.math.BigDecimal.ZERO;
            java.math.BigDecimal bdsum2 = java.math.BigDecimal.ZERO;
            for (Double2D d2d : val_lhm.values()) {
              bdsum1 = bdsum1.add(java.math.BigDecimal.valueOf(d2d.x));
              bdsum2 = bdsum2.add(java.math.BigDecimal.valueOf(d2d.y));
            }
            log.info("Parameters - " + propKey + " => <" + bdsum1 + ", " + bdsum2 + ">");
            if ((bdsum1.compareTo(java.math.BigDecimal.ONE) != 0)
                    || (bdsum2.compareTo(java.math.BigDecimal.ONE) != 0)) {
              throw new RuntimeException(propKey + " sum of map values <" + bdsum1 + "," + bdsum2 + "> != 1.0");
            }
          }   if (test == TESTS.exists) {
            for (String class_s : val_lhm.keySet()) {
              try {
                Class.forName("isl.model.cell."+class_s);
              } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException(class_s+" is an invalid type of Cell.", cnfe);
              }
            }
          }   val = val_lhm;
          break;
        }
      default:
        throw new RuntimeException("Don't understand property type: " + propType); // end if (propType.equals(T_REAL))
    }

    return val;
  }
  
  public static java.util.ArrayList<String> parseSimpleList (String raw) {
    String[] sa = raw.split(";");
    java.util.ArrayList<String> al = new java.util.ArrayList<>(sa.length);
    for (String s : sa) al.add(s.trim());
    return al;
  }
  public static sim.util.Double2D parseTuple(String tuple_s) {
    String[] tuple_sa = tuple_s.split(",");
    double x = Double.parseDouble(tuple_sa[0].replace('<', ' ').trim());
    double y = Double.parseDouble(tuple_sa[1].replace('>', ' ').trim());
    return new sim.util.Double2D(x,y);
  }
}
