/*
 * Copyright 2003-2015 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement.view;

public class EdgeView extends sim.portrayal.network.SimpleEdgePortrayal2D {
  private static final long serialVersionUID = 4277398274123302131L;
   isl.model.LiverEdge edge = null;
   String label = null;
   
   /** Creates a new instance of EdgeView */
   public EdgeView(isl.model.LiverEdge referent) {
      if (referent != null) edge = referent;
      else throw new RuntimeException("EdgeView: Referent must be non-null.");
      label = ((isl.model.LiverNode)edge.from()).id + "->" +
              ((isl.model.LiverNode)edge.to()).id;
   }
   public void setEdge(isl.model.LiverEdge referent) {
      if (edge != null) throw new RuntimeException("EdgeView: Can't reuse EdgeViews.");
      if (referent != null) edge = referent;
      else throw new RuntimeException("EdgeView: Referent must be non-null.");
   }
   public isl.model.LiverEdge getEdge() { return edge; }
   
   public void setLabel(String l) { label = l; }
   public String getLabel() { return label; }
  @Override
   public String toString() {
      int from = ((isl.model.LiverNode)edge.from()).id;
      int to = ((isl.model.LiverNode)edge.to()).id;
      return Integer.toString(from) + "->" + Integer.toString(to);
   }
   
  @Override
   public void draw(Object object, java.awt.Graphics2D graphics, sim.portrayal.DrawInfo2D info) {
      // this better be an EdgeDrawInfo2D!  :-)
      sim.portrayal.network.EdgeDrawInfo2D ei = (sim.portrayal.network.EdgeDrawInfo2D) info;
      // likewise, this better be an Edge!
      sim.field.network.Edge e = (sim.field.network.Edge) object;
      
      // our start (x,y), ending (x,y), and midpoint (for drawing the label)
      final int startX = (int)ei.draw.x;
      final int startY = (int)ei.draw.y;
      final int endX = (int)ei.secondPoint.x;
      final int endY = (int)ei.secondPoint.y;
      final int midX = (int)((ei.draw.x+ei.secondPoint.x) / 2);
      final int midY = (int)((ei.draw.y+ei.secondPoint.y) / 2);
      
      // draw line.
      graphics.setColor(java.awt.Color.black);
      graphics.drawLine(startX, startY, endX, endY);
      
   }
   
}
