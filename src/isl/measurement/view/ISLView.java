/*
 * ISLView.java
 *
 * Created on July 30, 2007, 2:44 PM
 *
 * Copyright 2003-2014 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.measurement.view;

/**
 *
 * @author gepr
 */
public class ISLView {
   private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( ISLView.class );
   isl.model.ISL isl = null;

   public long maxMobileObject = Integer.MAX_VALUE; // cached from the ISL referent

   HepStructView hepStructView = null;
   /** Creates a new instance of ISLView */
   public ISLView(GUIControl gui, isl.model.ISL i) {
      if (i != null) isl = i;
      else throw new RuntimeException("ISLView must have non-null referent.");
      hepStructView = new HepStructView(gui, this, isl.hepStruct);
      setupPortrayals(gui);

      maxMobileObject = isl.delivery.maxMobileObject;
      log.info("Estimated maximum mobile objects for all doses: "+maxMobileObject);
   }
   
   private void setupPortrayals(GUIControl gui) {
      // do nothing for now
   }
}
