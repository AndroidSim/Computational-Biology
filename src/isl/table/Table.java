/*
 * IPRL - Table interface
 *
 * Copyright 2003-2015 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package isl.table;

// TBD: Should we extend TableModel?  Swing is GUI library so I don't
//      really like tying this interface to a GUI library.  However
//      the TableModel is mostly a data interface except for the
//      listener idiom.  But really the listener idiom is not GUI
//      specific, ie data objects can be (should be, arguably) observable.
import javax.swing.table.TableModel;
import java.util.List;
import java.util.Map;
import java.util.Comparator;


/**
 * TBD: Add interface description
 *
 * @future
 *    - 
 *
 * @author Ken Cline
 * @see TableModel
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
public interface Table<E>  extends TableModel
                           // TBD: extends List<List<E>>
{
  // TBD: Switch from using 'int' to 'long' for row indices so that
  //      tables can have more rows if necessary.  My first thought
  //      is that columns can still be 'int' because who'd need a
  //      table with that many columns.  However, you could imagine
  //      needing to rotate/pivot a table and so if you allow that
  //      many rows, you should also allow that many columns.

  // TBD: Should I create TableRow, TableColumn and TableCell interfaces?
  // TBD: Should I create a TableCoordinate (ie row + column) class?
  // TBD: Create a Tables utility class that provides the convenience
  //      wrappers for synchronized and immutable tables ala Collections class

  // TBD: Create Table and Table Reader class
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
  public List<String> getColumnNames ();

  /**
   * TBD: Add doc
   *
   */
  // TBD: public List<E> getRow ( int rowIndex );
  // For this to work, the Row object (which is a type of List)
  // would need to be observable, ie the Table would need to be
  // able to listen for changes to the Row
  // Or, alternatively, the getRow method returns either
  // (a) a copy of the row data or (b) an immutable list
  // or (c) an immutable iterator or (d) an iterator that fires
  // the data change notifications
  // Should them method be called 'getRow', 'getRowData',
  // 'getRow{List|Iterator}' or ???
  // TBD: public Iterator<E> getRow ( int rowIndex );

  /**
   * TBD: Add doc
   *
   */
  // TBD: public List<E> getColumn ( int columnIndex );
  // TBD: public List<E> getColumn ( String columnName );
  // For this to work, the Column object (which is a type of List)
  // would need to be observable, ie the Table would need to be
  // able to listen for changes to the Column
  // Or, alternatively, the getColumn method returns either
  // (a) a copy of the column data or (b) an immutable list
  // or (c) an immutable iterator or (d) an iterator that fires
  // the data change notifications
  // Should them method be called 'getColumn', 'getColumnData',
  // 'getColumn{List|Iterator}' or ???
  // TBD: public Iterator<E> getColumn ( int columnIndex );
  // TBD: public Iterator<E> getColumn ( String columnName );

  /**
   * TBD: Add doc
   *
   */
  public Map<String,E> getRowMap ( int row );

  // TBD: The getRowMap method probably should be moved to a utility
  //      or "decorator" class because it can be easily implemented
  //      using the more basic Table methods and we should try to
  //      constrain the Table interface to make it easier to implement

  /**
   * TBD: Add doc
   *
   */
  @Override
  public E getValueAt ( int row, int col );


  /**
   * TBD: Add doc
   *
   */
  // TBD: public int getRowCount ( int columnIndex );
  // TBD: public int getRowCount ( String columnName );
  // TBD: public int getColumnCount ( int rowIndex );


  /**
   * TBD: Add doc
   *
   */
  // TBD: public E getValueAt ( int rowIndex, int columnIndex );
  // TBD: public E getValueAt ( int rowIndex, String columnName );
  // TBD: public E setValueAt ( E value, int rowIndex, int columnIndex );
  // TBD: public E setValueAt ( E value, int rowIndex, String columnName );


  /**
   * TBD: Add doc ... 
   */
  // public int columnIndexOf ( String name );
  // public int columnIndexOf ( String name, Comparator order );
  public int columnIndexOf ( Object key );
  public int columnIndexOf ( Object key, Comparator<Object> order );

  // TBD: Should column identifiers be Strings or Objects?

  // TBD: Should the column name/identifier/key comparator have a
  //      a generics type, e.g. should it be Comparator<String>
  //      or perhaps Comparator<? super String> ?

  // TBD: Create a Collection utilities method:
  //            indexOf( List, Object, Comparator )

  // TBD: Should the index lookup methods be called columnIndexOf
  //      (ala the List interface) or getColumnIndex (ie like an
  //      extension of the getColumn methods)?
  //
  // TBD: What should happen if no column found?
  //      Return -1 (ala the List interface), throw an exception
  //      or we could change the method to return Integer objects
  //      and return null when no column is found.
  //
  // TBD: What should happen if more than one column is found?
  //      Should we just return the first match (ala the List interface)
  //      and not bother checking for additional matches?
  //      Or should we check the entire List and throw an exception
  //      if more than one column matches?
  //      This is more of an issue when the user is allowed to specify
  //      a Comparator.
  //
  // TBD: Add support for getting the last matching index (ala the
  //      'lastIndexOf' method in the List interface).
  //
  // TBD: Add support for getting all indices of columns that
  //      match a particular name/key + comparator.
  //
  //      Is there a Collections method that gets all indices from
  //      a List that match a value?  If not, there should be.
  //
  //      There should also be a Collections method that creates
  //      a wrapper list that only contains the elements of the
  //      backing list if they are accepted or rejected by a filter
  //      (ie a FilteredList) and one obvious way to filter a
  //      Collection would be all items that equal to (or less than
  //      or great than) an Object X as determined by a Comparator
  //      C, i.e. the list of matching elements.
  //
  // TBD: Add support for getting row indexes, e.g.
  //         public int rowIndexOf ( columnIndex, value );
  //         public int rowIndexOf ( columnName,  value );
  //         public int rowIndexOf ( columnName,  value, comparator );
  //
  //      Note: the comparator in the last rowIndexOf method would
  //            be for comparing the provided value with the other
  //            values, on each row, in the specified column.  If
  //            the user needs to find a column using a name comparator
  //            then they could call columnIndexOf first, e.g.
  //               int column = table.columnIndexOf( name, NAME_COMPARATOR );
  //               int row = table.rowIndexOf( column, value, VALUE_COMPARATOR );
  //
  // TBD: Add support for getting the _last_ row indexes, i.e. same
  //      methods as above except returning the last occurrence.
  //
  // TBD: Add support for getting _all_ row indices that match
  //      a search value.

  // TBD: Consider creating a ColumnIndex interface/class.  This would be
  //      a mutable integer that maintains a reference to its parent
  //      Table object.  The motivation for creating this class is that
  //      table columns can moved (eg column reordered or columns added
  //      or removed).  It would be convenient if the table user could
  //      be given an object that automatically handled table changes
  //      and so the user would not have search the table's column list
  //      everytime he/she needed to reference a column by name.  That is,
  //      search by name once and get a Table.ColumnIndex object and from
  //      then on, simply call ColumnIndex.intValue().
  //
  //      Note: the ColumnIndex user would have to handle the fact that
  //            column could have been removed from the table and, if so,
  //            then the ColumnIndex.intValue() would throw an exception
  //            when evaluated.
  //
  //      Maybe just have a Table.Column interface/class is enough and
  //      the user can hold onto the Column object and retrieve the index
  //      from it.  The Column class would be "listener" on the Table
  //      and would automatically update if the columns changed.

  // TBD: Instead of calling them column "names" should we be calling
  //      them column "keys" since there's no real reason to restrict
  //      the column identifier to only strings.  The only time we care
  //      is when trying to render the column, ie converting the column
  //      key into a string is a function of a view class.  And even
  //      if the identifier was a string (as they would typically be)
  //      the view would likely still need to process that string value,
  //      e.g. capitalize or upper case the characters, during rendering.
  //
  // TBD: Instead of considering a Table a List of Lists, perhaps we
  //      should defined it as a Map of Maps or Map of Lists.  The
  //      column identifiers/keys/names would map to column objects
  //      which are either Lists or Maps.  Of course a List is just
  //      a special case of Map where the keys are integers, so I guess
  //      I'm splitting hairs.
  // 

  // TBD: Create a Tables utilities class (ala Collections) that provides
  //      useful static methods, e.g. for sorting, searching and creating
  //      synchronized or unmodifiable wrappers around table objects.
  //      For tables (or any multiple dimensional data structure) there
  //      are several ways to define immutability/unmodifiability.
  //      For example, the user might want the column order or names or
  //      indices to be static/unmodifiable but would need allow changes
  //      to the rows.  Likewise (but very unlikely) the user might need
  //      only the rows to be unmodifiable.  And of course the user
  //      might need the entire table to be unmodifiable/immutable,
  //      but that could mean just that number and order of the rows
  //      and columns can not change but the contents can or it could
  //      mean that the data is also fixed.

  // TBD: Add support for using a Filter object to search for
  //      columns object (ie Lists), column indexes, rows objects (ie Lists),
  //      row indexes or values, e.g.
  //          findColumn ( Filter )
  //          findRow( Filter )
  //          findValues( Filter )
  //
  // TBD: Filter interface would be like I used at AMS/IOT and also at JHU.
  //      We want a factory class that could build Filters from things
  //      such as Pattern, Comparator + Object + Relation (ie <, <=, ==, >=, >)
  //      and so forth.  Then we could replace a lot of the 'get' and
  //      'indexOf' type methods with Filter-based searching:
  //          Filter f = Filters.create( NAME_COMPARATOR, name );
  //          int column = table.findColumnIndex( f );
  //
  //      Note: I've seen this Filter idea in several other packages/libraries
  //            (I should have patented it :); sometimes called Predicates.

  /**
   * TBD: Add doc ... Adds a column to the table...
   */
  public void addColumn ( Object columnName );
  public void addColumn ( Object columnName, E[] columnData );
  public void addColumn ( Object columnName, List<? extends E> columnData );

  /**
   * TBD: Add doc ... Adds a row to the end of the table...
   */
  public void addRow ( Object[] rowData );
  public void addRow ( List<? extends E>  rowData );

  // TBD: Change the addColumn and addRow methods that take a List type
  //      for the data, into methods that take any subclass of Collection
  // TBD: Should the add{Column|Row} methods return a boolean or void?
  //      If we consider Table a specialized type of Collection then
  //      the return type should probably be boolean.  Although the
  //      add(int,E) method in the List interface returns void, so
  //      perhaps we should do that?
  // TBD: Since Table is a List of List, we should also support:
  //        addColumn( name, index, data ) inserts column at index
  //        addRow( name, index, data ) inserts row at index
  //        removeColumn( name )
  //        removeColumn( index )
  //        removeRow( index )
  //        removeAll( data )
  //        retainAll( data )  (more properly named: retainOnly(data) )
  //        subTable( fromColumn, fromRow, toColumn, toRow )
  //        tableIterator( traversalDirectionIndicator )
  //        tableIterator( traversalDirectionIndicator, startColumn, startRow )
  //        contains
  //        containsAll
  //        containsNone  (not part of Collection, but arguably should be)
  //        Object[][] toArray()
  //        T[][] toArray( T[][]
  // TBD: Add support for a table coordinate object, i.e. a Point
  //      to specify location, e.g.:
  //        getValueAt( Point )
  //        setValueAt( Point, value )
  //        subTable( Point, Point )
  //        tableIterator( traversalDirectionIndicator, Point )
  //

  // === [ Utility/Convenience Methods ] ==================================

  /**
   * TBD: Add doc
   * Convenience method for classes that need to maintain
   * Table indexes...
   */
  public void validateColumnIndex ( int index );

  public boolean isValidColumnIndex ( int index );

}  // end of Table<E> interface

