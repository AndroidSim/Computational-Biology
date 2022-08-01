/*
 * Copyright 2003-2019 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package isl.table;

// TBD: import Table;
// TBD: import java.text.Format;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsg.util.ClassUtils;
import isl.io.DataScanner;


/**
 * TBD: Add class description
 *
 * @future
 *    - 
 *
 * @author Ken Cline
 * @see Table
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
public class CSVTableLoader  // TBD: extends Format
                             // TBD: implements ???
{
  // TBD: Add doc
  private static final Logger log = LoggerFactory.getLogger( CSVTableLoader.class);


  // TBD: What to name this class?  CSVTableFormat, CSVTableParser,
  //      CSVTableInputStream, CSVTableInputReader, CSVTableLoader,
  //      other?

  // Thoughts on class name:
  //    * This class is reading/writing from streams and it needs
  //      an existing table (likely empty when loading) to provide
  //      parsing context, i.e. column data types
  //    * Format classes convert objects to/from strings, so this
  //      class is more/different than a TableFormat
  //    * Reader classes stream characters into a buffer, but this
  //      class is going to the next step of converting the characters
  //      into particular data types
  //    * ObjectInput/Output implementations read/write entire objects,
  //      but this class does not actually create a Table, it just
  //      puts/gets data in/out of the Table
  //

  // TBD: Should the load methods be static?  Does the loader need
  //      to maintain any state?
  //      I guess it could store the delimiter.
  //
  //      I would like to store/reuse an instance of Scanner but it
  //      seems the only way to provide the data to scan is via the
  //      constructor, so a new instance has to be created for each
  //      line in the file, afaict.

  // TBD: Create Table and Table Reader class
  // TBD: Skip comments
  // TBD: Interpolative Table class
  //          getValuesAt( colKey, colVal )
  // TBD: Should we convert Lists to arrays?
  // TBD: Should the Table object track its source?
  //      i.e. for logging purposes and also so that you can implement
  //      a save and/or reload capability
  // TBD: 


  /**
   * TBD: Add doc
   *
   */
  public static final String COMMA_SEPARATOR = "\\s*,\\s*";
  // TBD: Add other common separators, eg tab, whitespace, colon, semicolon...

  /**
   * TBD: Add doc
   *
   */
  protected Pattern valueDelimiter = Pattern.compile( COMMA_SEPARATOR );
  // TBD: Add rowDelimiter maybe, but really CSV rows should always
  //      be new line delimited... maybe this could become a generalized
  //      TableDataLoader?


  // TBD: Add flag variable to indicate if input can have comments
  // TBD: Or better yet, add support for Filter objects; both line and
  //      value/cell filters and then define a default filter instance
  //      for comment lines
  // TBD: Add flag variable to indicate if table header should be read
  //      from file
  // TBD: Add support for skipping n lines of input before loading table
  //      (this is actually another kind of line filter)
  // TBD: Possible filters:
  //         * Line skip filter, e.g. skip every other line of input
  //         * Column skip filter, e.g. skip first column of input
  //         * Value skip filter, e.g. skip negative values
  // TBD: Eventually we might need ability to do value conversions (e.g.
  //      apply arbitrary calculation to parsed value based on column
  //      number, name, type, etc) and/or table data translations (e.g.
  //      swap columns or rows) and/or table data rotations.
  //      Not sure how many of those types of things would occur during
  //      loading or performed on the table after the raw data was
  //      loaded
  //


  /**
   * TBD: Add doc
   *
   */
  public CSVTableLoader ( Pattern valDelim ) {
    setValueDelimiter( valDelim );
  }

  /**
   * TBD: Add doc
   *
   */
  public CSVTableLoader ( String valDelim ) {
    setValueDelimiter( valDelim );
  }

  /**
   * TBD: Add doc
   *
   */
  public CSVTableLoader () {
  }


  /**
   * TBD: Add doc
   *
   */
  public Pattern getValueDelimiter () {
    // TBD: Add validation?
    return valueDelimiter;
  }

  /**
   * TBD: Add doc
   *
   */
  public void setValueDelimiter ( Pattern valDelim ) {
    // TBD: Add validation of Pattern?
    // TBD: Add synchronization so the delimiter can not be changed
    //      during the parsing?
    valueDelimiter = valDelim;
  }

  /**
   * TBD: Add doc
   *
   */
  public void setValueDelimiter ( String valDelim ) {
    // TBD: Add validation of pattern string?
    setValueDelimiter( Pattern.compile(valDelim) );
  }



  // TBD: Create method:
  //     public List parseRow( String line, Iterator<Class> types, String delim )
  // TBD: Create ResettableIterator class that wrappers the
  //      Table.getColumnClass(int) method; resettable so we can reuse it;
  //      This way the parseRow is not specific to the Table class, it just
  //      needs a list/sequence of class types so it knows how to parse
  //

  /**
   * TBD: Add doc
   * TBD: Should I use a scanner or BufferedReader to read input by line?
   * TBD: Should I track line numbers or use LineNumberReader?
   * 
   * @note This method does <i><b>not</b></i> close the provided
   *       <code>Reader</code> object; that is the responsibility of
   *       the caller.
   */
  // public void load ( javax.swing.table.DefaultTableModel tbl,
  //                    BufferedReader src )
  //           throws IOException
  // {
  public void load ( Table<?> tbl, BufferedReader src ) throws IOException {
    String       line     = null;
    Class<?>     type     = null;
    DataScanner  csvScan  = null;
    List<Object> row      = new ArrayList<Object>();
    int          i        = -1;

    // TBD: Create a utility method that describe a Reader, e.g. if the
    //      Reader was created by a file or a Reader of a Reader of a File
    //      or ..., then the method would report the name of file being
    //      read.  I'm not sure though, if there's really a way to dig out
    //      this info from readers.
    //      When/if I create this utility method, then modify the logging
    //      framework, e.g. register a object formatter, so that methods
    //      like the debug below will report something more informative
    //      than the class name + hashcode.
    //log.debug( "Loading data from source: '{}'", src );
    
    try {
      i = -1;
      while ( (line = src.readLine()) != null ) {
        i++;

        //log.debug( "Parsing line {} from source {}", i, src );
        //TBD: if ( isComment(line) ) continue;
        
        csvScan = new DataScanner( line ).useDelimiter( getValueDelimiter() );
        row.clear();

        for ( int j = 0 ; csvScan.hasNext() ; j++ ) {
          if ( i == 0 ) {
            tbl.addColumn( csvScan.next() );
            //log.debug( "added column header ({}) {}", j, tbl.getColumnName(j) );
          } else {
            type = tbl.getColumnClass( j );
            row.add( csvScan.next(type) );
            //log.debug( "value at ({},{}) = {}", new Object[]{i-1,j,row.get(j)} );
          }
        }

        if ( i > 0 ) {
          //log.debug( "Table row {}: {}", i, row );
          tbl.addRow( row.toArray() );
        }

      }

      log.info( "Table data loading complete from source: '{}'", src );
      // TBD: Format table method/class
      //log.info( "Table[{}][{}] =\n{}",
      //           new Object[]{ tbl.getRowCount(), tbl.getColumnCount(),
      //                         tbl.getDataVector() } );

    } catch ( NoSuchElementException nsee ) {
      String msg = null;
      if ( tbl.getColumnCount() < 1 ) {
        msg = "Empty table -- No data read from source: " + src;
      } else {
        msg = "Error reading line " + i + " from source: " + src;
      }
      log.error( msg );
      // TBD: Create a MissingDataException and/or EmptyFileException ...
      throw new RuntimeException( msg, nsee );
    }
  }


  /**
   *
   */
  // TBD: public void load ( Table<T> tbl, URL src ) throws IOException
  // TBD: public void load ( Table<T> tbl, InputStream src ) throws IOException
  // TBD: public void load ( Table<T> tbl, Readable src ) throws IOException


  /**
   * TBD: Add doc
   *
   * @note As a convenience, this method <i><b>does</b></i> close the
   *       provided <code>Reader</code> object.  If you do not want the
   *       Reader automatically closed, then please use the instance
   *       @{link #load(Table<?>,BufferedReader)} method.
   */
  // public static void load ( javax.swing.table.DefaultTableModel tbl,
  //                           Reader src )
  //                  throws IOException
  // {
  public static void load ( Table<?> tbl, Reader src ) throws IOException {
    CSVTableLoader loader = null;

    try {
      //log.debug( "Loading data from source: '{}'", src );

      if ( ! (src instanceof BufferedReader) ) {
        src = new LineNumberReader( src );
      }

      loader = new CSVTableLoader();
      loader.load( tbl, (BufferedReader)src );

    } finally {
      if ( src != null ) {
        src.close();
      }
    }

  }

  /**
   * TBD: Add doc
   *
   */
  // public static void load ( javax.swing.table.DefaultTableModel tbl,
  //                           File src )
  //                   throws IOException
  // {
  public static void load ( Table<?> tbl, File src ) throws IOException {
    log.info( "Loading data from file: '{}'", src );
    load( tbl, new FileReader(src) );
    // Note: The reader will be closed for us by the other load method.
  }


  // ======================================================================

  // Main method for command line testing
  // ------------------------------------

  /**
   * Create the GUI and show it.  For thread safety, this method should be
   * invoked from the event-dispatching thread.
   *
   * @note: Adapted from JTable example at {@link http://download.oracle.com/javase/tutorial/uiswing/examples/components/TableToolTipsDemoProject/src/components/TableToolTipsDemo.java}
   *
   */
  protected static void createAndShowGUI ( String file ) {
    javax.swing.JFrame frame = null;
    javax.swing.JTable view = null;
    javax.swing.JScrollPane pane = null;
    // javax.swing.table.DefaultTableModel tbl = null;
    Table<?> tbl = null;

    //Create and set up the window.
    frame = new javax.swing.JFrame( "Data Table Test: '" + file + "'" );
    frame.setDefaultCloseOperation( javax.swing.JFrame.EXIT_ON_CLOSE );

    //Create and set up the content pane.
    //tbl = new javax.swing.table.DefaultTableModel () {
    //    private static final long serialVersionUID = 1L;    
    //    
    //    public Class<?> getColumnClass ( int columnIndex ) {
    //      return Double.class;
    //    }
    //  };
    tbl = new DefaultTable<Double>( null, null, Double.class );

    view = new javax.swing.JTable( tbl );
    view.setPreferredScrollableViewportSize( new java.awt.Dimension(2000,600) );
    view.setFillsViewportHeight( true );

    pane = new javax.swing.JScrollPane( view );
    pane.setOpaque( true ); //content panes must be opaque
    frame.setContentPane( pane );

    //javax.swing.JComponent newContentPane = new TableToolTipsDemo();
    //newContentPane.setOpaque(true); //content panes must be opaque
    //frame.setContentPane( newContentPane );

    //Display the window.
    frame.pack();
    frame.setVisible(true);

    try {
      CSVTableLoader.load( tbl, ClassUtils.getResourceFile(file) );
    } catch ( Exception e ) {
      String msg = "Unable to load data from '" + file + "'"; 
      log.error( msg, e );
      //throw new RuntimeException( msg, e );
    }

  }

  /**
   *
   */
  public static void main ( String[] args ) {

    for ( final String arg : args ) {
      //Schedule a job for the event-dispatching thread:
      //creating and showing this application's GUI.
/**/
      javax.swing.SwingUtilities.invokeLater( new Runnable() {
          public void run() {
            createAndShowGUI( arg );
          }
        });
/**/

/*
      try {
        org.jfree.data.io.CSV csv = null;
        org.jfree.data.category.CategoryDataset cds = null;
        java.io.FileReader rdr = null;

        csv = new org.jfree.data.io.CSV();
        rdr = new java.io.FileReader( ClassUtils.getResourceFile(arg) );
        cds = csv.readCategoryDataset( rdr );
        log.debug( "JFreeChart Category Dataset object read in from file '{}'",
                   arg );
        log.debug( "Category Dataset object:\n"
                 + "  implementation class returned by CSV: "
                 +                                     cds.getClass() + "\n"
                 + "  size [ rows ][ columns ] ="
                 +                " [ " + cds.getRowCount()    + " ]"
                 +                 "[ " + cds.getColumnCount() + " ]" + "\n"
                 + "  row keys: {}"                                   + "\n"
                 + "  column keys: {}"                                + "\n",
                   cds.getRowKeys(), cds.getColumnKeys() );

        log.debug( "Data from Category Dataset object:" );
        for ( int i = 0 ; i < cds.getRowCount()    ; i++ ) {
          StringBuilder buf = new StringBuilder( "Row " + i + ": " );
          java.util.Formatter fmt = new java.util.Formatter( buf );

          for ( int j = 0 ; j < cds.getColumnCount() ; j++ ) {
            //fmt.format( " %s", cds.getValue(i,j).toString() );
            //fmt.format( " %s (%s)", cds.getValue(i,j).toString(),
            //            cds.getValue(i,j).getClass().getSimpleName() );
            fmt.format( " %12.9g", cds.getValue(i,j) );
          }

          log.debug( "{}", buf );
        }

        // (K.Cline, 5/6/2011)
        // Thoughts...
        // It looks like JFreeChart CSV class can load basic CSV data but
        // what about handling:
        //    * comments
        //    * mixed data types (by column, of course)
        //    * ???
        // There doesn't appear to be a way to specify which implementation
        // of CategoryDataset is created without subclassing the CSV parser.
        // I also have not figured out how you can use the CategoryDataset
        // with other parts of the JFreeChart library.  Or how you can
        // convert it to a Swing TableModel for presentation.  Perhaps I
        // should create some classes to glue Swing and JFreeChart together.
        //

      } catch ( Exception e ) {
        log.error( "Unable to create JFreeChart Category Dataset"
                   + " from file '" + arg + "'", e );
      }
*/

    }

  }

}  // end of CSVTableLoader class

