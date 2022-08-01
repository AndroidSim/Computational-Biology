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
import java.util.LinkedHashMap;

public strictfp abstract class HepStruct extends sim.field.network.Network implements sim.engine.Steppable
{
  private static final long serialVersionUID = 1220592307434261751L;
  private static org.slf4j.Logger log = null;

  public ISL model = null;
  public final int NUM_LEAF_TRIES = 10;
  public final int NUM_EDGE_TRIES = 10;
  public ArrayList<ArrayList<SS>> nodesInLayer = new ArrayList<>();

  ec.util.ParameterDatabase pdb = null;
  ec.util.MersenneTwisterFast hepStructRNG = null;
  bsg.util.PRNGWrapper prngw = null;
  cern.jet.random.Gamma typeAGammaDist = null;
  cern.jet.random.Gamma typeBGammaDist = null;

  public float aperc = 0.85F;
  public float bperc = 0.15F;
  public int acircmin = 5;
  public int acircmax = 10;
  public float alena = 2.0F;
  public float alenb = 0.215F;
  public float alens = 0.0F;
  public int bcircmin = 4;
  public int bcircmax = 4;
  public float blena =  10.0F;
  public float blenb =  0.125F;
  public float blens =  -30.0F;
  public boolean lastLayerClamped = false;
  public int lastLayerCirc = -Integer.MAX_VALUE;
  public int lastLayerLength = -Integer.MAX_VALUE;
  
  //Inflammation mechanism parameters
  public int inflammatoryStimulusThreshold = -Integer.MAX_VALUE;
  public double exponentialFactor = -Double.MAX_VALUE;
   
  //ProtectionMediator mechanism parameters
  public int inducesProtectiveMediatorThreshold = -Integer.MAX_VALUE;
  
  //ToxicMediator mechanism parameters
  public int inducesToxicMediatorThreshold = -Integer.MAX_VALUE;
  
  public ArrayList<Integer> layers = null;
  public int[][] edges = null;
   
  public Harness structInput = null;
  public Harness structOutput = null;
   
  /** Creates a new instance of HepStruct */
  public HepStruct ( ISL i ) {
    super( true );
    if ( i != null ) model = i;
    else throw new RuntimeException( "HepStruct: ISL cannot be null." );
  }

  public HepStruct ( ISL i, ec.util.ParameterDatabase pd, ec.util.MersenneTwisterFast rng ) {
    this( i );
    pdb = pd;
    hepStructRNG = rng;
    HepStructParams.loadParams( this, pdb );
    prngw = new bsg.util.PRNGWrapper(hepStructRNG);
    typeAGammaDist = new cern.jet.random.Gamma(alena, alenb, prngw);
    typeBGammaDist = new cern.jet.random.Gamma(blena, blenb, prngw);
    HepStructParams.loadSpec( this );
  }

  public void setLogger(org.slf4j.Logger logger) {
     log = logger;
  }

  // declare unified gradient parameters
  public String uniGradtype = null;
  public sim.util.Double2D uniGradrange = null;
  public bsg.util.Gradient uniGradient = null;
  // set unified gradient
  public void setUnifiedGradient(sim.util.Double2D uGrange, String uGt) {
    double p[];
    p = new double[2];
    p[0] = uGrange.x;
    p[1] = uGrange.y;
    if (uGt.equalsIgnoreCase("Linear")) {
        bsg.util.LinearGradient uniGradient = new bsg.util.LinearGradient(p);
    }
    if (uGt.equalsIgnoreCase("Sigmoid")) {
        bsg.util.SigmoidGradient uniGradient = new bsg.util.SigmoidGradient(p);
    }
  }
  
  public bsg.util.Gradient getUnifiedGradient() {
      return uniGradient;
  }
  
  // function to go from distances from PV and CV, dPV and dCV, respectively,
  // to unified gradient value
  public double distances2UniGradient(int dPV, int dCV) {
      //bsg.util.LinearGradient uG = (bsg.util.LinearGradient) mySpace.ss.hepStruct.getUnifiedGradient();
      double uGs = uniGradrange.x;
      double uGf = uniGradrange.y;
      double uGradvalue = 0.0;
      //uGradvalue = uniGradient.eval(uGs,uGf,0.0,(double)(dPV+dCV),(double)dCV);
      if (uniGradtype.equalsIgnoreCase("Linear")) {
        uGradvalue = evalGradient(GradType.Linear,uGs,uGf,dPV,dCV);
      } 
      if (uniGradtype.equalsIgnoreCase("Sigmoid")) {
        uGradvalue = evalGradient(GradType.Sigmoid,uGs,uGf,dPV,dCV);  
      }
      //log.info("UniGradient: dist2uniG: uGradtype = "+uniGradtype+" uniGradvalue = "+uGradvalue);
      return uGradvalue;
  }
  
  // function to go from unified gradient value to distances from PV and CV.
  // Note: going from unified gradient value to normalized/parameterized distance
  // is not unique; therefore, the total Lobule length at that cell (dPV + dCV)
  // is also needed to get the separate dPV and dCV.
  //public sim.util.Double2D distances = null;
  public sim.util.Int2D uniGradient2distances(double unigrad, int totalDistance) {
      // gval(vHPC) = ((ugval(CV) - ugval(PV)) * parameterized distance - ugval(PV)
      double uGs = uniGradrange.x;
      double uGf = uniGradrange.y;
      //double pDistance = (uGf - unigrad)/(uGs - uGf);
      double pDistance = (unigrad - uGs)/(uGf - uGs);
      // x = dCV
      double x = pDistance * totalDistance;
      // y = dPV
      int y = totalDistance - (int) x;
      //log.info("UniGradient: totalDistance = "+totalDistance+" uniGradvalue = "+unigrad+" uGs = "+uGs+" uGf = "+uGf+" pDistance = "+pDistance+" ux = "+x+" uy = "+y);
      return new sim.util.Int2D((int) x,y);
  }
  
  // evaluate a Gradient with unified gradient
  public double evalGradientfromUniG(GradType gt, double upstream, double downstream, double uGvalue, int totalDistance) {
      sim.util.Int2D dCVdPV = uniGradient2distances(uGvalue,totalDistance);
      int dCV = dCVdPV.x;
      int dPV = dCVdPV.y;
      double valueGradient = evalGradient(gt,upstream,downstream,dPV,dCV);
      return valueGradient;
  }
  
  public double evalGradientfromUniG(GradType gt, double upstream, double downstream, double sharp, double shift, double uGval, double uGs, double uGf) {
      double retVal = Double.NaN;
      double pDistance = 0.0;
      if (uniGradtype.equalsIgnoreCase("Linear")) {
          pDistance = (uGval - uGs)/(uGf - uGs);
      } 
      if (uniGradtype.equalsIgnoreCase("Sigmoid")) {
          // z = intensity*(val-5);
          double z = StrictMath.log(((uGf - uGs)/(uGval - uGs)) - 1.0);
          //double z = StrictMath.log((((uGf - uGs)/(uGval - uGs)) - 1.0)/shift);
          double val = z/1.0 + 5;
          //double val = z/sharp + 5;
          //pDistance = (val - 0.0)/(10.0 - 0.0);
          pDistance = (val - 10.0)/(0.0 - 10.0);
          //log.info("UniGradient: evalGuniG: uGradtype = "+uniGradtype+" z = "+z+" val = "+val+" pDistance = "+pDistance);
      }
      if (gt == GradType.Linear) {
        retVal = bsg.util.LinearGradient.eval(upstream,downstream,0.0,1.0,pDistance);
      } else {
        //retVal = bsg.util.SigmoidGradient.eval(upstream,downstream,1.0,0.0,1.0,pDistance);
        retVal = bsg.util.SigmoidGradient.eval(upstream,downstream,sharp,shift,0.0,1.0,pDistance);
      }
      if (retVal < 0.0) {
          retVal = 0.0;
      }
      return retVal;
  }
  
  public static enum GradType {Linear, Sigmoid}
  public static double evalGradient(GradType gt, double upstream, double downstream, int dPV, int dCV) {
    double retVal = Double.NaN;
    if (gt == GradType.Linear)
      retVal = bsg.util.LinearGradient.eval(upstream,downstream,0.0,(double)(dPV+dCV),(double)dCV);
    else 
      retVal = bsg.util.SigmoidGradient.eval(upstream,downstream,1.0,0.0,(double)(dPV+dCV),(double)dCV);
    return retVal;
  }
  
  /* Constructs and assigns the input and output nodes for the structure. */  
  public abstract boolean initIO();
  
  public boolean init () {
    boolean retVal = true;
    
    log.info("HepStruct.init() -- begin." );

    initIO();

    // create the SSes -- IO nodes already created
    int nc = layers.stream().reduce(0,Integer::sum);
    if (nc > 1 || (nc == 1 && !lastLayerClamped)) {
      if (lastLayerClamped) nc -= layers.get(layers.size()-1);
      ArrayList<SS> sses = new ArrayList<>(nc);
      int first = 2;
      for (int nNdx=first ; nNdx<(nc+first) ; nNdx++ ) {
        float draw = hepStructRNG.nextFloat();
        SS ss = new SS( hepStructRNG, this, pdb );
        ss.setLogger(log);
        if ( draw < aperc ) {
          int d = acircmax-acircmin;
          int l = typeAGammaDist.nextInt();
          l += alens;
          if (l <= 0) l = 1; // don't allow zero length
          ss.setGeometry((d <= 0 ? acircmin : hepStructRNG.nextInt(acircmax-acircmin)+acircmin), l);
        } else {
          int d = bcircmax-bcircmin;
          int l = typeBGammaDist.nextInt();
          l += blens;
          if (l <= 0) l = 1; // don't allow zero length
          ss.setGeometry((d <= 0 ? bcircmin : hepStructRNG.nextInt(bcircmax-bcircmin)+bcircmin), l);
        }
        ss.id = nNdx;
        sses.add(ss);
        this.addNode(ss);
      }
      // place them in their layers
      int strt = 0, end = 0;
      for (Integer nn : (lastLayerClamped ? layers.subList(0,layers.size()-1) : layers)) {
        end = strt+nn;
        ArrayList<SS> nodes = new ArrayList<>(sses.subList(strt, end));
        // tell those SSes what layer they're in
        nodes.stream().forEach((ss) -> {ss.layer = nodesInLayer.size();});
        nodesInLayer.add(nodes);
        strt = end;
      }
    }
    if (lastLayerClamped) {
      int last = layers.get(layers.size()-1);
      ArrayList<SS> nodes = new ArrayList<>(last);
      for (int n=0 ; n<last ; n++) {
        SS ss =  new SS( hepStructRNG, this, pdb);
        ss.setLogger(log);
        ss.setGeometry(lastLayerCirc, lastLayerLength);
        ss.id = allNodes.numObjs;
        ss.layer = last;
        nodes.add(ss);
        this.addNode(ss);
      }
      nodesInLayer.add(nodes);
    }

    // hook them up
    
    // first hook 0th node to those in 1st layer
    nodesInLayer.get(0).forEach((ss) -> { this.addEdge(new LiverEdge(structInput, ss, null)); });
    
    for ( int srcLayer = 0 ; srcLayer < layers.size() ; srcLayer++ ) {
      int cachedEdges = 0;
         
      // move on if there are no nodes in this srcLayer
      if ( layers.get(srcLayer).equals(0) ) continue;

      for ( int tgtLayer = 0 ; tgtLayer < layers.size() ; tgtLayer++ ) {
        int edgeNum = 0;
        edgeNum = cachedEdges + edges[ srcLayer ][ tgtLayer ];

        // if the tgt layer has no nodes, cache them to go to the next layer
        if ( layers.get(tgtLayer).equals(0) ) {
          cachedEdges = edgeNum;
          continue;
        }
            
        // randomly select src and tgt nodes and hook them up
        ArrayList<SS> srcNodes = nodesInLayer.get(srcLayer );
        ArrayList<SS> tgtNodes = nodesInLayer.get( tgtLayer );
        int edgeNdx = 0;
        int tries = 0;

        while ( edgeNdx < edgeNum && tries < NUM_EDGE_TRIES ) {
          SS n1, n2;
          n1 = srcNodes.get(hepStructRNG.nextInt(srcNodes.size()) );
          n2 = tgtNodes.get(hepStructRNG.nextInt(tgtNodes.size()) );

          // no self-self links && no immediate back-links
          //if ( ! n1.equals(n2) && ! n2.linksTo(n1) ) {
          // no cycles!
          sim.util.Bag seen = new sim.util.Bag();
          seen.add(n1);
          if (!n2.isCycle(seen)) {
            // finally make the edge
            log.debug("HepStruct: "+n1.id + "->"+n2.id);
            this.addEdge( new LiverEdge(n1, n2, null) );
            edgeNdx++;
            tries = 0;
          } else {
            tries++;
          }
        }

        if ( edgeNdx < edgeNum )
          throw new RuntimeException( "Couldn't create enough edges between source layer " +
                                      srcLayer + " and target layer " + tgtLayer + ".");
      }

    } // end first pass edge creation

    // now handle leaf nodes left behind by first pass
    for ( int layerNdx=0 ; layerNdx < layers.size() ; layerNdx++ ) {
      ArrayList<SS> nodes = nodesInLayer.get( layerNdx );
      for (SS node : nodes) {
        // first link all the nodes in the last layer to the cv
        if ( layerNdx == layers.size() - 1 ) {
          log.debug("HepStruct: link node to CV: "+node.id + "->" + structOutput.id);
          this.addEdge(new LiverEdge(node, structOutput, null) );
        }
            
        // handle leaves
        if ( layerNdx < layers.size()-1 && this.getEdgesOut(node).isEmpty() ) {
          // try to link this node to each subsequent layer
          int z = 0;
          while ( ! this.linkNodeToLayer(node, layerNdx+1) && z++ < NUM_LEAF_TRIES );
          if ( z >= NUM_LEAF_TRIES ) {
            // if all else fails, leave the recalcitrant node to be pruned
            log.warn("HepStruct: Warning!  Couldn't find an outlet for node " + node.id + ". It will be pruned.");
          }
        }
        
        // handle dangling source nodes
        if ( this.getEdgesIn(node).isEmpty() ) {
          if (layerNdx==0) throw new RuntimeException("There was a problem linking the "+structInput.id+" to "+node.id);
          // try to link this node to a previous layer
          ArrayList<SS> prevNodes = nodesInLayer.get(layerNdx-1);
          int prevNodeId = hepStructRNG.nextInt(prevNodes.size());
          SS prevNode = prevNodes.get(prevNodeId);
          log.debug("HepStruct: linkNodeToLayer: "+prevNode.getClass().getName()+":"+prevNode.id + "->" + node.getClass().getName()+":"+node.id);
          this.addEdge( new LiverEdge(prevNode, node, null) );
        }
      }
    }
    
    // prune any remaining leaves
    prune();

    // get max path length from struct[Input|Output] and set gradient
    hepStructStats = getSSLengthStats();
    double min = Double.NaN, max = Double.NaN;
    int total_min = 0, total_max = 0;
    double mean = Double.NaN, median = Double.NaN;
    double total_mean = 0.0, total_median = 0.0;
    for (int layerNdx=0 ; layerNdx<layers.size() ; layerNdx++) {
      min = hepStructStats.get(layerNdx).get("min");
      total_min += min;
      max = hepStructStats.get(layerNdx).get("max");
      total_max += max;
      mean = hepStructStats.get(layerNdx).get("mean");
      total_mean += mean;
      median = hepStructStats.get(layerNdx).get("median");
      total_median += median;
      log.info("HepStruct.init() - layer "+ layerNdx + ": min = "+ min + ", "
               + "mean = "+ mean + ", "
               + "median = "+ median + ", "
               + "max = "+ max);
    }
    log.info("HepStruct.init() - Total: min = "+total_min + ", "
            + "mean = "+ total_mean + ", "
            + "median = "+ total_median + ", "
            + "max = "+ total_max);

    max_grid = 0.0;
    // for each LiverNode, get dCV and dPV and keep track of the max
    sim.util.Bag nodes = getAllNodes();
    for (Object o : nodes) {
      LiverNode ln = (LiverNode) o;
      double dPV = ln.calculatedPV();
      max_grid = Math.max(dPV, max_grid);
      double dCV = ln.calculatedCV();
      max_grid = Math.max(dCV, max_grid);
      ln.priorPathLength = (int) dPV;
      ln.postPathLength = (int) dCV;
    }
      
    for (Object o : allNodes) {
      LiverNode ln = (LiverNode) o;
      log.info("HepStruct: LN:" + ln.id + " priorPathLength = " + ln.priorPathLength + ", postPathLength = " + ln.postPathLength);
    }
    
    log.info("HepStruct.init() -- end." );

    /**
     * Hepatocytes need to know how far they are from the PV. Hence we call 
     * SS.fillSpaces() after we calculate the SSLengthStats.
     */
    for (Object o : allNodes) if (o instanceof SS) ((SS)o).fillSpaces();
    return retVal;
  }
  public double max_grid = Double.NaN;
  
  private void prune() {
    java.util.Map<Integer,SS> leaves = null;
    while( ! (leaves = getLeaves()).isEmpty()) {
      for (java.util.Map.Entry<Integer,SS> me : leaves.entrySet()) {
        nodesInLayer.get(me.getKey()).remove(me.getValue());
        removeNode(me.getValue());
      }
    }
  }
  private java.util.Map<Integer,SS> getLeaves() {
    java.util.Map<Integer, SS> retVal = new java.util.HashMap<>();
      for ( int layerNdx=0 ; layerNdx < layers.size() ; layerNdx++ ) {
        ArrayList<SS> nodes = nodesInLayer.get( layerNdx );
        for (SS node : nodes) 
          if (getEdgesOut(node).isEmpty()) retVal.put(layerNdx,node);
      }
    return retVal;
  }
  /**
   * hepStructStats contains the min,max,mean,median for the whole hepStruct (-1)
 and each of the layers [0,numLayers).
   */
  public LinkedHashMap<Integer,LinkedHashMap<String,Double>> hepStructStats = null;
  private LinkedHashMap<Integer,LinkedHashMap<String,Double>> getSSLengthStats() {
    LinkedHashMap<Integer,LinkedHashMap<String,Double>> retVal = null;
    LinkedHashMap<String,Double> layerStats = null;
    if (layerStats == null) {
      if (allNodes == null) return null;  // if called before init()
      retVal = new LinkedHashMap<Integer,LinkedHashMap<String,Double>>();
      // hepStruct wide stats at index -1
      layerStats = new LinkedHashMap<String,Double>(4);
      cern.colt.list.DoubleArrayList lengths = 
              new cern.colt.list.DoubleArrayList(allNodes.numObjs);
      for (Object o : allNodes) 
        if (o instanceof SS) lengths.add((double)((SS)o).length);
      layerStats.put("min", cern.jet.stat.Descriptive.min(lengths));
      layerStats.put("max", cern.jet.stat.Descriptive.max(lengths));
      layerStats.put("mean", cern.jet.stat.Descriptive.mean(lengths));
      layerStats.put("median", cern.jet.stat.Descriptive.median(lengths));
      retVal.put(-1, layerStats);

      // now the actual layers
      for (int zNdx=0 ; zNdx<nodesInLayer.size() ; zNdx++) {
        layerStats = new LinkedHashMap<String,Double>(4);
        lengths.clear();
        for (SS ss : nodesInLayer.get(zNdx)) lengths.add((double)ss.length);
        layerStats.put("min", cern.jet.stat.Descriptive.min(lengths));
        layerStats.put("max", cern.jet.stat.Descriptive.max(lengths));
        layerStats.put("mean", cern.jet.stat.Descriptive.mean(lengths));
        layerStats.put("median", cern.jet.stat.Descriptive.median(lengths));
        retVal.put(zNdx, layerStats);
      }
    }
    return retVal;
  }

  private boolean linkNodeToLayer ( LiverNode src, int layer ) {
    boolean retVal = false;
    if ( layer >= nodesInLayer.size() ) {
      // link to hv
      log.info("HepStruct: linkNodeToLayer: "+src.id+"->"+structOutput.id);
      this.addEdge(new LiverEdge(src, structOutput, null) );
      retVal = true;
    } else {
      ArrayList<SS> nodes = nodesInLayer.get( layer );
      if ( nodes.size() > 0 ) {
        int try_count = 0;
        do {
          int draw = hepStructRNG.nextInt( nodes.size() );
          LiverNode tgt = (LiverNode) nodes.get( draw );

          // no cycles!
          sim.util.Bag seen = new sim.util.Bag();
          seen.add(src);
          if (!tgt.isCycle(seen)) {
            log.info("HepStruct: linkNodeToLayer: "+src.getClass().getName()+":"+src.id + "->" + tgt.getClass().getName()+":"+tgt.id);
            this.addEdge( new LiverEdge(src, tgt, null) );
            retVal = true;
          }
        } while ( !retVal && try_count++ < NUM_LEAF_TRIES );
      }

    }
    return retVal;
  }

  @Override
  public void step ( sim.engine.SimState state ) {
    sim.util.Bag tmpNodes = new sim.util.Bag(getAllNodes());
    // remove the PV and CV so we can sync them
    tmpNodes.remove(structInput);
    tmpNodes.remove(structOutput);
    tmpNodes.shuffle(hepStructRNG);
    structInput.step(state);
    for (Object o : tmpNodes) ((LiverNode)o).step(state);
    structOutput.step(state);
    state.schedule.scheduleOnce(this, ISL.action_order+1);
  }

  /**
   * Prints the state of the object and iterates over its component objects
   * @return
   */
  public String describe () {
    StringBuilder sb = new StringBuilder();
    sb.append(structInput.describe() );

    for ( int zNdx = 0 ; zNdx < layers.size() ; zNdx++ ) {
      sb.append( " Layer ").append(zNdx).append(" nodes:\n");
      for ( int nNdx = 0 ; nNdx < nodesInLayer.get( zNdx ).size() ; nNdx++ ) {
        SS ss = nodesInLayer.get( zNdx ).get( nNdx );
        sb.append( ss.describe() );
      }
    }

    sb.append(structOutput.describe() );
    return sb.toString();
  }

}
