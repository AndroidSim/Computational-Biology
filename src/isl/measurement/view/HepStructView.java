/*
 * Copyright 2003-2017 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement.view;

public class HepStructView {
   ISLView islView = null;
   isl.model.HepStruct hepStruct = null;
   NodeView pv = null, cv = null;  // portal and central veins
   sim.field.network.Network mirror = null;
   java.util.ArrayList<java.util.ArrayList<NodeView>> nodeViewsInLayer = null;
   
   sim.portrayal.network.NetworkPortrayal2D edgePortrayal =
       new sim.portrayal.network.NetworkPortrayal2D();
   sim.field.grid.SparseGrid2D layoutGrid = null;
   sim.portrayal.grid.SparseGridPortrayal2D nodePortrayal =
       new sim.portrayal.grid.SparseGridPortrayal2D();
   
   public static final int NODE_SIZE = 10;
   public static final int LAYER_SIZE = 20;
   public static final int NODE_SPACE = 5;

   /**
    * Creates a new instance of the HepStructView viewer
    */
   public HepStructView(GUIControl gui, ISLView iv, isl.model.HepStruct l) {
      if (iv != null) islView = iv;
      else throw new RuntimeException("HepStructView: ISLView cannot be null.");
      if (l != null) hepStruct = l;
      else throw new RuntimeException("HepStructView referent must be non-null.");
      mirror = mirrorHepStruct(hepStruct);
      setupPortrayals(gui);
   }
   
   private sim.field.network.Network mirrorHepStruct(isl.model.HepStruct l) {
      nodeViewsInLayer = new java.util.ArrayList<>(l.nodesInLayer.size());
      int layerNdx = 0;
      for (layerNdx=0 ; layerNdx<l.nodesInLayer.size() ; layerNdx++ ) {
         nodeViewsInLayer.add(new java.util.ArrayList<NodeView>(l.nodesInLayer.get(layerNdx).size()));
      }
      
      sim.field.network.Network retVal = new sim.field.network.Network();
      sim.util.Bag nodes = hepStruct.getAllNodes();
      for (int nodeNdx=0 ; nodeNdx<nodes.size() ; nodeNdx++ ) {
         isl.model.LiverNode ln = (isl.model.LiverNode) nodes.get(nodeNdx);
         NodeView nv = new NodeView(this, ln);
         retVal.addNode(nv);
         if (ln == hepStruct.structInput) {
            pv = nv;
         } else if (ln == hepStruct.structOutput) {
            cv = nv;
         } else {
           isl.model.SS ss = (isl.model.SS)ln;
            for ( layerNdx=0 ; layerNdx<l.nodesInLayer.size() ; layerNdx++ ) {
               if (l.nodesInLayer.get(layerNdx).contains(ss)) {
                  nodeViewsInLayer.get(layerNdx).add(nv);
                  nv.setLayer(layerNdx);
                  break;
               }
            }
         }
      }
      for (int nodeNdx=0 ; nodeNdx<nodes.size() ; nodeNdx++ ) {
         sim.util.Bag edges = hepStruct.getEdgesIn(nodes.get(nodeNdx));
         for (int edgeNdx=0 ; edgeNdx<edges.size() ; edgeNdx++ ) {
            isl.model.LiverEdge ine = (isl.model.LiverEdge) edges.get(edgeNdx);
            NodeView from = findNodeView(retVal, (isl.model.LiverNode)ine.from());
            NodeView to = findNodeView(retVal, (isl.model.LiverNode)ine.to());
            if (from != null || to != null)
               retVal.addEdge(from, to, new EdgeView(ine));
         }
      }
      return retVal;
   }
   
   /**
    * findNodeView - search the mirror network for the NodeView that refers to
    * the parameter LiverNode.
    */
   private NodeView findNodeView(sim.field.network.Network net, isl.model.LiverNode r) {
      NodeView retVal = null;
      for (Object o : net.getAllNodes()) {
        NodeView nv = (NodeView)o;
        if (r == nv.node) {
          retVal = nv; 
          break;
        }
      }
      return retVal;
   }
   
   private void setupPortrayals(GUIControl gui) {
      gui.display.attach(edgePortrayal, "Edges");
      gui.display.attach(nodePortrayal, "SSes");
      
      layout(hepStruct, layoutGrid);
      
      edgePortrayal.setField( new sim.portrayal.network.SpatialNetwork2D(layoutGrid, mirror) );

      // since an EdgeView is both the info for the edge and the portrayal
      // get all edges, get each edges info, set that info as the portrayal
      sim.util.Bag nodes = mirror.getAllNodes();
      sim.util.Bag edges = new sim.util.Bag();
      for (int nodeNdx=0 ; nodeNdx<nodes.numObjs ; nodeNdx++ ) {
         mirror.getEdges(nodes.objs[nodeNdx], edges);
         for (int edgeNdx=0 ; edgeNdx<edges.numObjs ; edgeNdx++ ) {
            sim.field.network.Edge e = (sim.field.network.Edge)edges.objs[edgeNdx];
            EdgeView portrayal = (EdgeView)e.info;
            edgePortrayal.setPortrayalForObject(e, portrayal);
         }
      }
      nodePortrayal.setField( layoutGrid );
      
      // reschedule the displayer
      gui.display.reset();
      gui.display.setBackdrop(java.awt.Color.white);
      
      // redraw the display
      gui.display.repaint();
      
   }
   
   private void layout(isl.model.HepStruct l, sim.field.grid.SparseGrid2D grid) {
      int numLayers = nodeViewsInLayer.size();
      int maxNodesPerLayer = Integer.MIN_VALUE;
      for (int layerNdx=0 ; layerNdx<numLayers ; layerNdx++ ) {
         int numNodes = nodeViewsInLayer.get(layerNdx).size();
         if (numNodes > maxNodesPerLayer)
            maxNodesPerLayer = numNodes;
      }
      int width = maxNodesPerLayer*NODE_SIZE;
      int height = (numLayers+2)*LAYER_SIZE;
      layoutGrid = new sim.field.grid.SparseGrid2D(width, height);
      
      // place nodes on grid layer 0 at top, layers start at left
      pv.layout_x = width/2; pv.layout_y = NODE_SIZE/2;
      layoutGrid.setObjectLocation(pv, pv.layout_x, pv.layout_y);
      cv.layout_x = width/2; cv.layout_y = height-NODE_SIZE/2;
      layoutGrid.setObjectLocation(cv, cv.layout_x, cv.layout_y);
      for (int layerNdx=0 ; layerNdx<numLayers ; layerNdx++ ) {
         int layerHeight = layerNdx*LAYER_SIZE + LAYER_SIZE;
         java.util.ListIterator<NodeView> nodeNdx = nodeViewsInLayer.get(layerNdx).listIterator();
         int count = 0;
         while(nodeNdx.hasNext()) {
            int nodePosition = Integer.MIN_VALUE;
            if ((count % 2) == 0)
               nodePosition = width/2 + count*NODE_SPACE;
            else
               nodePosition = width/2 - count*NODE_SPACE;
            
            NodeView nv = nodeNdx.next();
            nv.layout_x = nodePosition;
            nv.layout_y = layerHeight;
            layoutGrid.setObjectLocation(nv, nv.layout_x, nv.layout_y);
            count++;
         }
      }
   }
   public void updateLayoutGrid() {
      
   }
}
