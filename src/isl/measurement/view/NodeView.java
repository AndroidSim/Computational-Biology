/*
 * Copyright 2003-2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement.view;

public class NodeView extends sim.portrayal.SimplePortrayal2D {
  private static final long serialVersionUID = -4833187328967485171L;
   private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( NodeView.class );
   public HepStructView hepStructView = null;
   public isl.model.LiverNode node = null;
   public int layout_x = Integer.MIN_VALUE;
   public int layout_y = Integer.MIN_VALUE;
   public int layer = Integer.MIN_VALUE;
   public java.awt.Color body_color = java.awt.Color.blue;
   public java.awt.Color font_color = java.awt.Color.black;
   /**
    * Creates a new instance of NodeView
    */
   public NodeView(HepStructView lv, isl.model.LiverNode referent) {
      if (lv != null) hepStructView = lv;
      else throw new RuntimeException("NodeView: HepStructView can't be null");
      if (referent != null) node = referent;
      else throw new RuntimeException("NodeView: Referent must be non-null.");
   }
   public void setNode(isl.model.LiverNode referent) {
      if (node != null) throw new RuntimeException("NodeView:  Can't reuse NodeViews.");
      if (referent != null) node = referent;
      else throw new RuntimeException("NodeView: Referent must be non-null.");
   }
   public isl.model.LiverNode getNode() { return node; }
   
   public void setX(int x) {
      if ( x>=0 ) layout_x = x;
      else throw new RuntimeException("NodeView: X can't be negative.");
   }
   public int getX() { return layout_x; }
   
   public void setY(int y) {
      if ( y>=0 ) layout_y = y;
      else throw new RuntimeException("NodeView: Y can't be negative.");
   }
   public int getY() { return layout_y; }
   
   public void setLayer(int z) {
      if ( z>=0 ) layer = z;
      else throw new RuntimeException("NodeView: Layer can't be negative.");
   }
   public int getLayer() { return layer; }
   
   public String toString() { return "Node_" + Integer.toString(node.id); }
   
   /**
    * graphcs methods
    */
   
   public java.awt.Font nodeFont = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 6);
   
   public static final int DIAMETER = 5;
  @Override
   public final void draw(Object object, java.awt.Graphics2D graphics, sim.portrayal.DrawInfo2D info) {
      
      double diamx = info.draw.width*DIAMETER;
      double diamy = info.draw.height*DIAMETER;

      long level = 0;
      java.util.Map<String,Number> sm = node.getMobileObjectMap();
      if (sm != null)
         level = bsg.util.CollectionUtils.sum_mi(sm);
      //long max = hepStructView.islView.maxMobileObject;
      long max = 10;
      long numMobileObject = level;
      double solRatio = (double)numMobileObject/(double)max;
      if (solRatio > 1) solRatio = 1.0; // to handle Vas vs. SS colors
      solRatio *= (Math.PI)/2.0;
      if (level > 0) body_color = new java.awt.Color((float)Math.sin(solRatio), 0.0F, (float)Math.cos(solRatio));

      graphics.setColor( body_color );
      graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
      graphics.setFont(nodeFont.deriveFont(nodeFont.getSize2D()*(float)info.draw.width));
      graphics.setColor( font_color );
      graphics.drawString( Integer.toString(node.id), (int)(info.draw.x-diamx/2), (int)(info.draw.y-diamy/2) );
   }
   
  @Override
   public boolean hitObject(Object object, sim.portrayal.DrawInfo2D info) {
      double diamx = info.draw.width*DIAMETER;
      double diamy = info.draw.height*DIAMETER;
      
      java.awt.geom.Ellipse2D.Double ellipse = new java.awt.geom.Ellipse2D.Double( (int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy) );
      return ( ellipse.intersects( info.clip.x, info.clip.y, info.clip.width, info.clip.height ) );
   }
   
}
