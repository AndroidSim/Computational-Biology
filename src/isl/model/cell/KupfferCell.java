/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.model.cell;

import isl.model.SSSpace;
import isl.model.MobileObject;
import isl.model.MobileObjectType;
import isl.model.Solute;

public class KupfferCell extends Cell {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KupfferCell.class);
  static sim.util.Double2D CROSS_PROB = new sim.util.Double2D(1.0, 1.0);
  public static void setMembraneCrossProb(sim.util.Double2D mcp) { log.info("Setting mcp = "+mcp); CROSS_PROB = mcp; }
  @Override
  public boolean canCross(MobileObject m, Dir d) { return canCross(m,d,CROSS_PROB); }

  public KupfferCell(SSSpace p, ec.util.MersenneTwisterFast rng, int x, int y) {
    super(p,rng);
    setLoc(x,y);
        
    actionShuffler.clear(); //Removes BindingHandler        
    actionShuffler.add((Runnable) () -> { handleInflammation(); });
    actionShuffler.add((Runnable) () -> { handleToxicMediator(); });
    actionShuffler.add((Runnable) () -> { handleProtectiveMediator(); });
    //Must add handleDegradation() again because actionShuffler was just cleared.
    actionShuffler.add((Runnable) () -> { handleDegradation(mySpace); });
  }
  
  public static MobileObjectType cytokineType = null;
  public static MobileObjectType protectiveMediatorType = null;
  public static MobileObjectType toxicMediatorType = null;

  public void handleInflammation() {
    //Count the number of inflammatory stimuli and Cytokines in the Cell
    int numInflammatoryStimuli = 0;
    int numCytokines = 0;
    for (Object o : mobileObjects) {
      MobileObject m = (MobileObject) o;
      if (m.hasProperty("inflammatory") && ((Boolean) m.getProperty("inflammatory")))
        numInflammatoryStimuli++;
      if (m.getTypeString().equals("Cytokine"))
        numCytokines++;
    }

    //If past inflammatory threshold, there's a chance to produce Cytokine
    if (numInflammatoryStimuli >= mySpace.ss.hepStruct.inflammatoryStimulusThreshold) {
      double probability = 1.0 - Math.exp(-1 * (numInflammatoryStimuli - mySpace.ss.hepStruct.inflammatoryStimulusThreshold) / mySpace.ss.hepStruct.exponentialFactor);

      double draw = cellRNG.nextDouble();
      if (draw <= probability) addCytokine();
    }
  }
    
  public MobileObject addCytokine() {
    //Set the Cytokine BolusEntry once
    if (cytokineType == null) {
      java.util.ArrayList<MobileObjectType> mobileObjectTypes = mySpace.ss.hepStruct.model.allMobileObject;
      Boolean foundCytokine = false;
      for (MobileObjectType mt : mobileObjectTypes) {
        if (mt.tag.equals("Cytokine")) {
          cytokineType = mt;
          foundCytokine = true;
          break;
        }
      }
      if (!foundCytokine)
        throw new RuntimeException("There must be a Cytokine MobileObject type in order to create Cytokine objects.");
    }
        
    //Create the Cytokine
    Solute cytokine = new Solute(cytokineType);
    cytokine.setProperties(cytokineType.properties);

    //Add the Cytokine
    present(cytokine);
    
    return cytokine;
  }
    
  public void handleProtectiveMediator() {
    //Count the number of toxic stimuli in the cell
    int numToxicStimuli = 0;
    int numProtectiveMediators = 0;
    for (Object o : mobileObjects) {
      MobileObject m = (MobileObject) o;
      if (m.hasProperty("inducesProtectiveMediator") && ((Boolean) m.getProperty("inducesProtectiveMediator")))
        numToxicStimuli++;
      if (m.getTypeString().equals("ProtectiveMediator"))
        numProtectiveMediators++;
    }
 
    //If past inducesProtectionMediatorThreshold, there's a chance to produce ProtectiveMediator
    if (numToxicStimuli >= mySpace.ss.hepStruct.inducesProtectiveMediatorThreshold) {
      double probability = 1.0 - Math.exp(-1 * (numToxicStimuli - mySpace.ss.hepStruct.inducesProtectiveMediatorThreshold) / mySpace.ss.hepStruct.exponentialFactor);

      double draw = cellRNG.nextDouble();
      if (draw <= probability) addProtectiveMediator();
    }
  }
    
  public MobileObject addProtectiveMediator() {

    //Set the ProtectiveMediator BolusEntry once
    if (protectiveMediatorType == null) {
      java.util.ArrayList<MobileObjectType> mobileObjectTypes = mySpace.ss.hepStruct.model.allMobileObject;
      Boolean foundProtectiveMediator = false;
      for (MobileObjectType be : mobileObjectTypes) {
        if (be.tag.equals("ProtectiveMediator")) {
          protectiveMediatorType = be;
          foundProtectiveMediator = true;
          break;
        }
      }
      if (!foundProtectiveMediator)
        throw new RuntimeException("There must be a Protective Mediator bolus entry.");
    }
        
    //Create the Protective Mediator
    Solute protectiveMediator = new Solute(protectiveMediatorType);
    protectiveMediator.setProperties(protectiveMediatorType.properties);

    //Add the Protective Mediator
    present(protectiveMediator);

    return protectiveMediator;
  }
    
  public void handleToxicMediator() {
    //Count the number of inducers and inhibitors in the Cell
    int numInduceToxicMediator = 0;
    int numInhibitsToxicMediator = 0;
    int numToxicMediator = 0;
    for (Object o : mobileObjects) {
      MobileObject m = (MobileObject) o;
      if (m.hasProperty("inducesToxicMediator") && ((Boolean) m.getProperty("inducesToxicMediator")))
        numInduceToxicMediator++;
      if (m.hasProperty("inhibitsToxicMediator") && ((Boolean) m.getProperty("inhibitsToxicMediator")))
        numInhibitsToxicMediator++;
      if (m.getTypeString().equals("ToxicMediator")) 
        numToxicMediator++;
    }
        
    //If past inflammatory threshold, there's a chance to produce Cytokine
    if ((numInduceToxicMediator - numInhibitsToxicMediator) >= mySpace.ss.hepStruct.inducesToxicMediatorThreshold) {
      double probability = 1.0 - Math.exp(-1 * ((numInduceToxicMediator - numInhibitsToxicMediator) - mySpace.ss.hepStruct.inducesToxicMediatorThreshold) / mySpace.ss.hepStruct.exponentialFactor);

      double draw = cellRNG.nextDouble();
      if (draw <= probability) addToxicMediator();
    }
  }
    
  public MobileObject addToxicMediator() {

    //Set the ProtectiveMediator BolusEntry once
    if (toxicMediatorType == null) {
      java.util.ArrayList<MobileObjectType> mobileObjectTypes = mySpace.ss.hepStruct.model.allMobileObject;
      Boolean foundToxicMediator = false;
      for (MobileObjectType be : mobileObjectTypes) {
        if (be.tag.equals("ToxicMediator")) {
          toxicMediatorType = be;
          foundToxicMediator = true;
          break;
        }
      }
      if (!foundToxicMediator)
        throw new RuntimeException("There must be a Toxic Mediator bolus entry.");
    }
        
    //Create the toxic Mediator
    Solute toxicMediator = new Solute(toxicMediatorType);
    toxicMediator.setProperties(toxicMediatorType.properties);

    //Add the Toxic Mediator
    present(toxicMediator);
    
    return toxicMediator;
  }
}

