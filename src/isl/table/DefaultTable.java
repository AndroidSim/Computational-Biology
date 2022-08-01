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

// TBD: See future below
import javax.swing.table.DefaultTableModel;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static bsg.util.CollectionUtils.*;


/**
 * TBD: Add class description
 *
 * @future
 *    - Replace use of the DefaultTableModel which is Vector-based
 *      with our own extension of the AbstractTableModel that uses
 *      Lists or arrays
 *    - Either extend a JFreeChart class or implement a JFreeChart
 *      interface so that we can easily use it UI classes; possible
 *      classes/interfaces to use:
 *            org.jfree.data.xy.TableXYDataset
 *            org.jfree.data.xy.DefaultTableXYDataset
 *            org.jfree.data.time.TimeTableXYDataset
 *    - The Table class could possibly be an extension of MASON's
 *      2D grid class.  Afterall, it basically a 2D grid with a
 *      header and special terminology, (x,y) => (row,column) and
 *      so forth
 *    - 
 *    - 
 *
 * @author Ken Cline
 * @see Table
 * @see DefaultTableModel
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
public class DefaultTable<E>  extends     DefaultTableModel
                              implements  Table<E>, java.io.Serializable
{
  // TBD: Generate serialization ID using serialver tool
  // TBD: Add doc
  private static final long serialVersionUID = 1L;    

  // TBD: Add doc
  private static final Logger log = LoggerFactory.getLogger( DefaultTable.class);


  /**
   * TBD: Add doc
   */
  public static final Class<?> DEFAULT_COLUMN_TYPE = Object.class;
  //public static final Class<?> DEFAULT_COLUMN_TYPE = E.class;

  /**
   * TBD: Add doc
   */
  protected Class<?> defaultColumnType = DEFAULT_COLUMN_TYPE;
  //protected Class<?> defaultColumnType = E.class;

  // TBD: Change the defaultColumnType from Class<?> to Class<? extends E>
  //      Is that possible?
  // TBD: Change the columnTypes from List<Class<?>> to List<Class<? extends E>>
  //      Is that possible?


  /**
   * TBD: Add doc
   * TBD: Create an extension of Collection/List which supports a default
   *      element and/or a default Collection/List of values
   *      With the List + Default class (not sure what to call it yet)
   *      the class no longer needs to maintain two ivars.
   *      The collection/list/map with defaults would likely be very useful
   *      in other contexts as well (and maybe it exists already?)
   */
  protected List<Class<?>> columnTypes = null;
  //protected List<Class<?>> columnTypes = createColumnTypesList( E.class );
  //
  // If only it was that easy...
  //
  // Because Java's implementation of generics uses "type erasure",
  // we can not determine the class the type variable (i.e. <code>E</code>)
  // by calling <code>E.class</code>.  Adding the line above will cause
  // the compile-time error:
  //       cannot select from a type variable
  //
  // There are work-arounds for the limitation of Java Generics typing
  // functionality.  In particular I found the following possible
  // solutions:
  //    http://www.artima.com/weblogs/viewpost.jsp?thread=208860
  //    http://www.artima.com/forums/flat.jsp?forum=106&thread=208860
  //    http://www.artima.com/forums/flat.jsp?forum=106&thread=206350
  //    http://code.google.com/p/gentyref/
  //
  // These work-arounds look like they will work but that's a lot of code
  // to add and then write test cases for and so on.  That's more than
  // I really want to get into right now.  Maybe some other day.  For now,
  // I'll just have to require that the user specify the column type(s)
  // explicitly/redundantly as an argument to the constructor.
  //


  // === [ Constructors ] =================================================

  /**
   * Creates a <code>DefaultTable</code> object and initializes the
   * table with the provided <code>data</code>, column <code>names</code>
   * and column <code>types</code>.  The superclass's constructor calls
   * the <code>setDataVector</code> method to store the <code>data</code>
   * and column <code>names</code>.
   *
   * @note The <code>types</code> list does not need to have an entry
   *       for every column
   *
   *
   * @param data    the new data for the table; it is <i>(currently)</i>
   *                converted into a <code>Vector</code> of
   *                <code>Vector</code>s to satisfy the superclass'
   *                requirements
   * @param names   the names of table columns;  the elements within
   *                the <code>List</code> are converted into
   *                <code>String</code>s, if necessary, by the
   *                @{link #getColumnName} method
   * @param types   the types of data in each of the table's columns;
   *                if <code>null</code> then all columns will be the
   *                default type specified by last parameter
   * @param defaultType  the fallback column data type to use if no
   *                element exists in <code>types</code> list for a
   *                particular column index or if <code>types</code>
   *                is <code>null</code> or empty.
   * @see #setTableData
   * @see #getDataVector
   * @see #setDataVector
   */
  public DefaultTable ( List<List<? extends E>> data,
                        List<?> names,
                        List<Class<?>> types,
                        Class<?> defaultType )
  {
    setTableData( data, names, types, defaultType );
  }

  /**
   * TBD: Add doc
   */
  public DefaultTable ( List<?> names,
                        List<Class<?>> types,
                        Class<?> defaultType )
  {
    this( null, names, types, defaultType );
  }

  /**
   * TBD: Add doc
   */
  public DefaultTable ( int rows, int columns ) {
    List<List<? extends E>> data = createEmptyTableData( rows, columns );
    Vector<?> names = new Vector( columns );
    names.setSize( columns );
    setTableData( data, names, (List<Class<?>>)null, DEFAULT_COLUMN_TYPE );
  }


  /**
   * Creates an empty <code>DefaultTable</code>, i.e. zero columns and
   * zero rows, with no column names or class types defined.  Column
   * classes will default to the <code>Object.class</code> type;
   */
  public DefaultTable () {
    this( 0, 0 );
  }


  // TBD: Add additional constructors, perhaps replace some that I have
  //      Not sure which constructors will be most useful at this point


  // === [ Setter & Getter Methods ] ======================================

  /**
   * TBD: Add docs...
   *
   * Replaces the tables <code>dataVector</code> ...
   *
   * @note The <code>types</code> list does not need to have an entry
   *       for every column
   *
   * @param data    the new data for the table; it is <i>(currently)</i>
   *                converted into a <code>Vector</code> of
   *                <code>Vector</code>s to satisfy the superclass'
   *                requirements
   * @param names   the names of table columns;  the elements within
   *                the <code>List</code> are converted into
   *                <code>String</code>s, if necessary, by the
   *                @{link #getColumnName} method
   * @param types   the types of data in each of the table's columns;
   *                if <code>null</code> then all columns will be the
   *                default type specified by last parameter
   * @param defaultType  the fallback column data type to use if no
   *                element exists in <code>types</code> list for a
   *                particular column index or if <code>types</code>
   *                is <code>null</code> or empty.
   * @see #getDataVector
   * @see #setDataVector
   * @see #getColumnClasses
   * @see #setColumnClasses
   */
  public void setTableData ( List<List<? extends E>> data,
                             List<?> names,
                             List<Class<?>> types,
                             Class<?> defaultType )
  {
    setColumnClasses( types, defaultType, false );
    super.setDataVector( toVector2D(data), toVector(names) );
    validateDataTypes();
    // TBD: We are setting the columnTypes before calling the setDataVector
    //      method because setDataVector fires the table changed event.
    //      What should we do if setColumnClasses is called directly?
    //      That is, should setColumnClasses fire a table changed event
    //      as well?  But then, when this method is called, we would be
    //      firing 2 events for essentially 1 change.  We could add a flag
    //      that suppresses redundant change events from being fired.
    //      That is, this method would turn ON the suppress flag before
    //      calling setColumnClasses and then turn it OFF again before
    //      calling setDataVector.  Actually, instead of a flag, it could
    //      be Stack so you could push and pop state that way you can
    //      restore the suppress state that existed before the function
    //      was called (which may have been either ON or OFF).
  }

  // TBD: Override setDataVector to have it call setTableData (ie all
  //      data changes go through setTableData so we can update type info
  //      if necessary

  // TBD: Create method that extracts the data types from the data,
  //      either (a) get class for the object in the first row of
  //      every column, or (b) get class for the first non-null object
  //      in every column starting at row 0, or (c) get class for every
  //      object in the column and then (recursively) find a common
  //      supertype for all non-null entries for the column
  //      NOTE: getting a common supertype for a list of objects could
  //            be a useful utility method, if it doesn't already exist
  //
  // TBD: Possibly add a dynamic data typing inner class that would
  //      update the column types as data is added (and optionally removed)
  //      from the table.  Then column typing becomes more just about
  //      describing the contents of the table and not restricting the
  //      type of data that can be entered.  However, you could possibly
  //      load the data, then determine the types and then throw a switch
  //      that says 'data types are fixed/immutable', ie data values can
  //      change but new values must be same type.
  //
  // TBD: The more general concept here is a FilteredTable which is simply
  //      2D extension of my FilteredList class.  Filtering for a table
  //      could be by column number, by column name, by row number or
  //      by cell coordinates (ala rendering and editing).  Filters can
  //      accept/reject (column, row or cell) entries by either data value
  //      or data type criteria and of course that might depend on the
  //      context (ie the full table)...
  //        interface TableFilter<E> {
  //           public boolean accepts ( Table tbl, int row, int col, E value );
  //           public boolean accepts ( Table tbl, Table.Cell cell );
  //           public boolean rejects ( Table tbl, int row, int col, E value );
  //           public boolean rejects ( Table tbl, Table.Cell cell );
  //        }
  //
  // and so on ...
  //

  /**
   * TBD: Add docs...
   *
   * @note The <code>types</code> list does not need to have an entry
   *       for every column
   *
   * @param types   the types of data in each of the table's columns;
   *                if <code>null</code> then all columns will be the
   *                default type specified by last parameter
   * @param defaultType  the fallback column data type to use if no
   *                element exists in <code>types</code> list for a
   *                particular column index or if <code>types</code>
   *                is <code>null</code> or empty.
   * @param validate  a switch specifying whether @{link #validateDataTypes}
   *                should be called after changing column types and/or
   *                the default type; this flag should only be <code>false</code>
   *                if setting the column classes is part of a larger
   *                change to the table and the validation will be invoked
   *                after the other changes are complete
   * @see #getColumnClass
   * @see #setTableData
   * @see #getDataVector
   * @see #setDataVector
   * @see #validateDataTypes
   */
  public void setColumnClasses ( List<Class<?>> types, Class<?> defaultType,
                                 boolean validate )
  {
    this.columnTypes = types;
    // TBD: Should we assign or copy the types parameter?  It would
    //      be safer but slower to copy.  If types was an instance of
    //      an Observable List, then we register as a change listener?
    // TBD: Create a "safeMode" switch that decides whether to perform
    //      copies or assignments

    // TBD: setDefaultColumnClass( defaultType, false );
    this.defaultColumnType = defaultType;

    // TBD: Test if the columnTypes and defaultColumnType actually
    //      changed and only validate if necessary

    if ( validate ) {
      validateDataTypes();
    } 

    // TBD: Should we fire a table ??? changed event?
  }

  // TBD: setColumnClasses ( List<Class<?>> types, Class<?> defaultType )
  // TBD: setDefaultColumnClass( Class<?> defaultType, boolean validate )
  // TBD: setDefaultColumnClass( Class<?> defaultType )
  // TBD: setColumnClass( int columnIndex, Class<?> type, boolean validate )
  // TBD: setColumnClass( int columnIndex, Class<?> type )

  // TBD: getColumnClasses ()
  // TBD:   When we implement getColumnClasses, should it return the
  //        columnTypes list or should it create a List that uses
  //        the default (just as getColumnClass does) and also which
  //        throws IndexOutOfBounds when > getColumnCount even when
  //        the columnTypes list size > getColumnCount (ie hide types
  //        that are for non-existant columns)
  //        Should the returned List be immutable?  Should is be an
  //        an Observable List that will notify the table if the list
  //        is changed?


  /**
   * Returns the data type for the indicate column, if found in the
   * column @{link #columnTypes types} list; otherwise
   * @{link #defaultColumnType} is returned.
   * 
   * @note This method does <i>not</i> throw an {@link IndexOutOfBoundsException}.
   *       When the <code>columnIndex</code> exceeds the table bounds,
   *       @{link #defaultColumnType} is returned.
   *
   * @param columnIndex  the index of column being queried
   * @return a <code>Class</code> that indicates the column's data type,
   *         if defined; otherwise the @{link #defaultColumnType} is
   *         returned (see details in method description)
   */
  @Override
  public Class<?> getColumnClass ( int columnIndex ) {
    try {
      if ( columnTypes == null ) {
        //return super.getColumnClass( columnIndex );
        return defaultColumnType;
      }
      return columnTypes.get( columnIndex );
    } catch ( IndexOutOfBoundsException ioobe ) {
      log.info( "No type specified for column {}; returning Object.class",
                 columnIndex );
    }

    // Should this be null or super.getColumnClass( columnIndex ) ?
    //return null;
    return defaultColumnType;
  }


  /**
   * TBD: Add doc
   *
   */
  @Override
  public List<String> getColumnNames () {
    return columnIdentifiers;
  }

  // TBD: The getRowMap method probably should be moved to a utility
  //      or "decorator" class because it can be easily implemented
  //      using the more basic Table methods and we should try to
  //      constrain the Table interface to make it easier to implement

  /**
   * TBD: Add doc
   *
   */
  @Override
  public Map<String,E> getRowMap ( int row ) {
    Map<String,E>  map   = null;
    String    name  = null;
    String    tmp   = null;
    int       N     = getColumnCount();

    // Using LinkedHashMap because it preserves the insertion order
    // and hence the order of entries will be the same as the order
    // of columns in the table
    map = new java.util.LinkedHashMap<String,E>( N-1 );

    for ( int i = 0 ; i < N ; i++ ) {
      name = getColumnName( i );
      if ( name == null || map.containsKey(name) ) {
        tmp = createDefaultColumnName( i );
        //log.debug( "{} for column {} -- using default name '{}' instead",
        //           new Object[]{ (name == null ? "No name"
        //                          : "Duplicate name ('" + name + "')" ),
        //                         i, tmp } );
        name = tmp;
      }

      map.put( name, getValueAt(row,i) );
    }

    return map;
  }


  /**
   * TBD: Add doc
   *
   */
  @SuppressWarnings( "unchecked" )
  @Override
  public E getValueAt ( int row, int col ) {
    // HACK
    return (E)super.getValueAt( row, col );
  }
  // TBD: I rather not use the SuppressWarnings but I think that's
  //      the only thing I can do here _short of_ implementing a
  //      complex reflection mechanism capable of figuring out what
  //      class object E actually is.  I've found some information
  //      online about doing that, but it's not easy and it only
  //      works in particular circumstances.
  //      I plan to re-implement the class and replace the parent/super
  //      class with something that supports typing.


  /**
   * TBD: Add doc
   *
   */
  @Override
  public int columnIndexOf ( Object key ) {
    List<?> keys = getColumnNames();
    return ( keys == null ? -1 : keys.indexOf(key) );
  }

  /**
   * TBD: Add doc
   *
   */
  @Override
  public int columnIndexOf ( Object key, Comparator order ) {
    List<?> keys = getColumnNames();
    return ( keys == null ? -1 : indexOf(keys,key,order) );
  }

  // TBD: Allow user to specify a Comparator or an "Equate-tor" or Filter
  //      for comparing column names/identifiers/keys.
  //      Then add support for getting values using the column name/key
  //      e.g.
  //          public E getValueAt ( int row, Object key );
  //      If we support user defined equivalence of column names/keys
  //      then we should provide a key duplication check/validation method
  //      and maybe a way for the user to specify at create time whether
  //      or not allow duplicate column names/keys/identifiers, i.e. a
  //      unique column flag.
  // 
  //      Perhaps the user could control whether columns are unique
  //      and/or sorted (by name/key) by specifying the List subinterface
  //      to use.  I.e. instead of the Table being simply a List of Lists
  //      it could be List of SortedMaps?
  //

  // TBD: Implement serialization methods ...
  // TBD: private void writeObject ( java.io.ObjectOutputStream s )
  //                        throws java.io.IOException
  //      { ... }
  // TBD: private void readObject ( java.io.ObjectInputStream s )
  //                       throws java.io.IOException, ClassNotFoundException
  //      { ... }
  //

  // === [ Add & Remove Methods ] =========================================

  /**
   * TBD: Add doc
   *
   */
  @Override
  public void addColumn ( Object columnName, List<? extends E> columnData ) {
    // HACK
    super.addColumn( columnName, toVector(columnData) );
  }

  /**
   * TBD: Add doc
   *
   */
  @Override
  public void addRow ( List<? extends E> rowData ) {
    // HACK
    super.addRow( toVector(rowData) );
  }

  /**
   * TBD: Add doc
   *
   */
  //@Override
  //public void addRow ( E[] rowData ) {
  //  // HACK
  //  super.addRow( toVector(rowData) );
  //}


  // === [ Utility/Convenience Methods ] ==================================

  /**
   * TBD: Add doc
   * Convenience method for classes that need to maintain
   * Table indexes...
   */
  @Override
  public void validateColumnIndex ( int index ) {
    int N = getColumnCount() - 1;

    if ( index < 0 || index > N ) {
      String msg = "Invalid column index " + index
                 + " -- column index must be in range [0," + N + "]";
      throw new IndexOutOfBoundsException( msg );
    }

  }

  /**
   * TBD
   *
   */
  @Override
  public boolean isValidColumnIndex ( int index ) {
    try {
      validateColumnIndex( index );
      return true;
    } catch ( IndexOutOfBoundsException ioobe ) {
      log.info( ioobe.getMessage() );
    }
    return false;
  }

  /**
   * TBD
   *
   */
  public void validateColumnDataType ( int columnIndex ) {
    // TBD: HERE HERE
    // 1. get type for column (might be default type)
    // 2. if type is null or Object.class, then return
    // 3. for each row in the column, check that value
    //    isAssignable from type?
    //    if not, then throw exception (which exception?)
  }

  /**
   * TBD
   *
   */
  public void validateDataTypes () {
    for ( int i = 0 ; i < getColumnCount() ; i++ ) {
      validateColumnDataType( i );
    }
  }


  /**
   * TBD
   *
   */
  public static Vector toVector ( Object obj ) {
    if ( obj == null ) {
      return new Vector();
    }

    if ( obj instanceof Vector ) {
      return (Vector)obj;
    }

    if ( obj.getClass().isArray() ) {
      obj = Arrays.asList( obj );
    }

    if ( obj instanceof Collection ) {
      return new Vector<Object>( (Collection<?>)obj );
    }

    Vector<Object> v = new Vector<Object>();
    v.add( obj );
    return v;
  }

  /**
   * TBD
   *
   */
  public static <E> Vector toVector2D ( List<List<? extends E>> data ) {
    Vector<Vector> v = null;

    if ( data == null ) {
      return new Vector<Vector>();
    }

    v = new Vector<Vector>( data.size() );

    for ( int i = 0 ; i < data.size() ; i++ ) {
      v.set( i, toVector( data.get(i) ) );
    }

    return v;
  }

  /**
   * TBD
   *
   */
  public List<? extends E> createEmptyList ( int size ) {
    // TBD: Switch to something better than Vector, only using it
    //      because we subclassing DefaultTableModel and it uses
    //      Vector
    Vector<E> v = new Vector<E>( size );
    v.setSize( size );  // causes the Vector to be filled with null
    return v;
  }

  /**
   * TBD
   *
   */
  public List<List<? extends E>> createEmptyTableData ( int rows, int columns )
  {
    // TBD: Switch to something better than Vector, only using it
    //      because we subclassing DefaultTableModel and it uses
    //      Vector
    List<List<? extends E>> data = null;

    data = new Vector<List<? extends E>>( rows );
    for ( int i = 0 ; i < rows ; i++ ) {
      data.add( createEmptyList(columns) );
    }

    return data;
  }

  /**
   * TBD: Add doc
   */
  public List<Class<?>> createHomogeneousColumnTypesList ( final Class<?> cls ) {
    return new AbstractList<Class<?>> () {
      public int size () {
        return DefaultTable.this.getColumnCount();
      }

      public Class<?> get ( int index ) {
        return cls;
      }
    };
  }


  // TBD: Move the createDefaultColumnName method to a Tables
  //      utilities class
  // TBD: For performance, if necessary, the createDefaultColumnName method
  //      could use a cache, ie memoization, so it doesn't bother recalculating
  //      the first 25, 50 or 100 default column names; 25 is probably good

  /**
   * Returns a default name for the column using spreadsheet
   * conventions: A, B, C, ... Z, AA, AB, etc.
   *
   * @param column  the column number to create a name for
   * @return a string containing the default name of <code>column</code>
   */
  public static String createDefaultColumnName ( int column ) {
    StringBuilder result = new StringBuilder();

    // This implementation comes from the 'getColumnName' method
    // in the javax.swing.table.AbstractTableModel class found
    // in OpenJDK Java 6 source code.

    for ( ; column >= 0 ; column = column / 26 - 1 ) {
      result.insert( 0, (char)( (char)(column % 26) + 'A') );
    }

    return result.toString();
  }


  // === [ Experimental Code: When I have time ... ] ======================

/*
  / **
   * TBD: Add doc
   *
   * /
  public static <E> void validateColumnSorted ( Table<E> data, int col,
                                            Comparator<? super E> cmp )
  {
// HERE HERE
  }

  / **
   * TBD: Add doc
   *
   * /
  // TBD: Should we call the value we are looking for the "predecessor"
  //      or the "antecedent" or the "floor" or ... "prior", "preceding",
  //      "anterior"
  //      Note: this search method also finds the location where a new
  //            row would be inserted _if_ the table was sorted on the
  //            the specified column... So perhaps we should call this
  //            findInsertionRow, findInsertRow, find{Insert|Insertion}RowIndex
  //            or something like that?
  //            Or maybe just findRow or findRowIndex?
  // TBD: Replace this search method with one that takes a filter and
  //      returns the last accepted row.
  // public static int findPredecessorRow ( Table<E> data, int col, E value,
  //                                        Comparator<? super E> cmp )
  public static <E> int findRowIndex ( Table<E> data, int col, E value,
                                   Comparator<? super E> cmp )
  {
    int N = data.getRowCount();
    E   entry = null;
    int row = 0;
    int status = 0;
    boolean found = false;

    validateColumnSorted( data, col, cmp );
    // TBD: check for duplicates, ie multiple rows with same value in col

    for ( row = 0 ; row < N ; row++ ) {
      entry = data.getValueAt( row, col );
      status = cmp.compare( value, entry );

      if ( status == 0 ) {
        found = true;
      }

      if ( status < 0 ) {
        break;
      }
    }

    row--;
    return ( found ? row : -row );

    // TBD: Or instead, use:
    //         List<?> colList = data.getColumn( col );
    //         CollectionUtils.validateSorted( colList );
    //         row = Collections.binarySearch( colList, value, cmp );
    //         return row;
  }


  / **
   * TBD: Add doc
   *
   * /
  public static <E extends Number> Map<?,E> getInterpolatedRowMap ( Table<E> data, int col,
                                                 E value, E tolerance,
                                                 Comparator<? super E> cmp )
  or ...
  public static <? extends Number> Map<?,? extends Number> getInterpolatedRowMap ( Table<? extends Number> data, int col,
                                                 Number value, Number tolerance,
                                                 Comparator<? super E> cmp )
  {
    Map<?,E>  map   = null;
    int row = -1;
    E lower = null;
    E upper = null;
    E numer = null;
    E denom = null;
    double ratio = Double.NaN;
    Object key = null;
    E inter = null;

    row = findRowIndex( data, col, value, cmp );
    // check row value

    map = data.getRowMap( Math.abs(row) );
    if ( row >= 0 ) {  // value matched a row
      return map;
    }

    row = -row;
    if ( row == 0 || row >= data.getRowCount() -1 ) {  // first or last row
      return map;
    }

    lower = data.getValueAt( row, col );
    numer = ( value - lower );
    if ( cmp.compare(tolerance,numer) >= 0 ) {  // diff <= tolerance
      return map;
    }

    upper = data.getValueAt( row+1, col );
    denom = ( upper - lower );
    ratio = numer.doubleValue() / denom.doubleValue();

    int N = data.getColumnCount();

    for ( int i = 0 ; i < N ; i++ ) {
      key = data.getColumnName( i );

      if ( i == col ) {
        map.put( key, value );
        continue;
      }

      lower = data.getValueAt( row,   i );
      upper = data.getValueAt( row+1, i );
      inter = lower + ratio * ( upper - lower );
      map.put( key, inter );
    }

    return map;
  }

*/


}  // end of DefaultTable<E> class

