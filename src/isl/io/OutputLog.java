/*
 * Copyright 2003-2015 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package isl.io;

public class OutputLog {
  java.io.File outFile = null;
  public java.io.PrintWriter outPW = null;
  public java.util.zip.GZIPOutputStream gzo = null;
  String logDir = "logs/";
   
  public OutputLog ( String outLogFileName, boolean gzip ) {
    java.io.File outDir = new java.io.File(logDir);
    if (!outDir.exists()) {
      if (!outDir.isDirectory())
        outDir.renameTo(new java.io.File(logDir+"-moved-out-of-the-way-by-ISHC"));
      outDir.mkdir();
    }
    outFile = new java.io.File( logDir+outLogFileName+(gzip ? ".gz" : ""));
    
    try {
      if (gzip) {
        gzo = new java.util.zip.GZIPOutputStream(new java.io.FileOutputStream(outFile));
        outPW = new java.io.PrintWriter(gzo);
      } else 
        outPW = new java.io.PrintWriter( outFile );
    } catch ( java.io.IOException e ) {
      System.err.println( e.getMessage() );
      throw new RuntimeException( "Couldn't open output log file.", e );
    }
  }

  public void mon ( String s ) {
     outPW.print(s);
     outPW.flush();
  }
  public void monln ( String s ) {
     outPW.println(s);
     outPW.flush();
  }

  public void finish() {
    if (gzo != null) {
      try {gzo.finish();gzo.close();} catch (java.io.IOException e) {throw new RuntimeException(e);}
    }
    outPW.close();
  }
}
