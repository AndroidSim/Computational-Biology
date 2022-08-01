/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package isl.measurement.view;

public class GUIControl extends sim.display.GUIState
{
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( GUIControl.class );

  ec.util.ParameterDatabase pd = null;
  isl.measurement.view.ISLView islView = null;
  BatchControl modelBatch = null;
  public sim.display.Display2D display = null;
  public javax.swing.JFrame displayFrame = null;
   
  public GUIControl ( String[] args ) {
     super ( new BatchControl(System.currentTimeMillis(), args) );
     modelBatch = (BatchControl)state;
   }

  @Override
  public void start () {
    super.start();
    log.info( "GUIControl.start() -- begin." );
    islView = new ISLView( this, modelBatch.isl );
    log.info( "GUIControl.start() -- end." );
  }

  @Override
  public void load ( sim.engine.SimState st ) {
    super.load(st);
    log.info( "GUIControl.load()" );
    islView = new ISLView( this, modelBatch.isl );
  }
   
  @Override
  public void init ( sim.display.Controller console ) {
    super.init( console );
    log.info( "GUIControl.init()" );
      
    // make the displayer
    display = new sim.display.Display2D( 800, 600, this);
      
    displayFrame = display.createFrame();
    displayFrame.setTitle( "ISL HepStruct Display" );
    console.registerFrame( displayFrame );   // register the frame so it appears in the "Display" list
    displayFrame.setVisible( true );
  }
   
  @Override
  public void quit () {
    super.quit();
    log.info( "GUIControl.quit()" );
    if ( displayFrame != null ) {
      displayFrame.dispose();
    }
    displayFrame = null;
    display = null;
  }

  @Override
  public Object getSimulationInspectedObject () { return state; } // non-volatile 

}
