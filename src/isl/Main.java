/*
 * Copyright 2003-2016 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package isl;

import bsg.util.SRControl;

/**
 *
 * @author gepr
 */
public class Main {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( Main.class );

   /**
    *pd - parameter database ala ec.utils
    */
   ec.util.ParameterDatabase pd = null;
   
   /**
    * Creates a new instance of Main
    */
   public Main() {
   }

   public static void main(String[] args) {

      Main c = new Main();
      try {
         c.pd = new ec.util.ParameterDatabase(c.getClass().getClassLoader().getResourceAsStream("cfg/batch_control.properties"));
      } catch (Exception e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
         System.exit(-1);
      }
      // load boolean SR parameter from "SR.properties"
      ec.util.ParameterDatabase SRpd = null;
      try {
         SRpd = new ec.util.ParameterDatabase(c.getClass().getClassLoader().getResourceAsStream("cfg/SR.properties"));
      } catch (Exception e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
         System.exit(-1);
      }
      ec.util.Parameter parameter = null;
      StringBuilder paramstr = null;
      paramstr = new StringBuilder("SR");
      boolean SR = SRpd.getBoolean(parameter = new ec.util.Parameter(paramstr.toString()), null, false);
      if (SR) {
          SRControl SRcontrol = new SRControl();
          SRcontrol.varyRandom();
          try {
                SRcontrol.writeParamFiles();
          } catch (Exception e) { e.printStackTrace(); }
      }
      
      // load gui parameter from pd
      ec.util.Parameter param = null;
      StringBuilder pk = null;
      pk = new StringBuilder("gui");
      boolean gui = c.pd.getBoolean(param = new ec.util.Parameter(pk.toString()), null, false);

      // parse in order to determine whether to use GUI given standalone arg
      if (!isl.measurement.view.BatchControl.canUseGUI(args)) gui = false;
      if (gui) {
         isl.measurement.view.GUIControl mv = new isl.measurement.view.GUIControl(args);
         sim.display.Console console = new sim.display.Console(mv);
         console.setVisible(true);
      } else {
         isl.measurement.view.BatchControl.doLoop(isl.measurement.view.BatchControl.class, args);
      }

   }
}
