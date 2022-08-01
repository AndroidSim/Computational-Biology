/*
 * IPRL - Exception for missing data
 *
 * Copyright 2003-2014 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

// TBD: Which package would be the best place for this class?
//      It is a general data-related exception...        so perhaps 'isl.data'?
//      However I don't have a 'data' package yet and I don't want
//      create a new package just for an exception class...
//      Arguably the 'util' package should be used for objects with
//      no natural "home" until there's enough (2 or 3 at least)
//      that seem to "go together"...                    so perhaps 'isl.util'?
//      However, I didn't want the 'util' package to keep growing
//      and the DataScanner is "lonely" in the 'io' package all
//      by itself and this is somewhat related to IO...  so perhaps 'isl.io'?
//      I'll put it in 'isl.io' for now, but may change my mind later...

package isl.io;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TBD: Class name and purpose...
//      Initially this exception was going to be called:
//      MissingTableColumnException, but I thought that that
//      might be too specific and only useful in rare situations.
//      Then I considered calling it TableException (ie any type
//      of exception from a table) but that's not really what
//      I needed.  I want an exception to indicate that data used
//      to load a table was incomplete (e.g. did not contain a
//      column for time values).  That's a data exception, not a
//      table exception.  So for now I go with MissingDataException
//      but I fear that that is too general/broad.
//
//      The more specific the exception type is, then easier it
//      is for the calling code because the exception can handle
//      more of the message creation and such.  And the name
//      of the exception explains why it was throw, ie self-
//      documenting code.  However, if it is too specific then
//      it is a waste, i.e. a few dozen lines of code + .java and
//      .class files that will never be used.  It all about the
//      balance.
//
// ASIDE: The MASON framework only defined _one_ exception class
//      (CausedException) that actually obsolete since Java now
//      supports storing one exception as the cause of the other.
//
//      How is possible to write a library as big as MASON and
//      only define one tiny little exception?  MASON is a modeling
//      tool which encodes concepts; it (like Swarm) defines what
//      amounts to a language, e.g. Schedule, Agent, ....  There
//      is NO possible way you could create something like that
//      and not have circumstances that demand a new exception
//      type.  The MASON developers either don't get how to write
//      good object-oriented code or they are too lazy to do the
//      leg work.
//

/**
 * TBD: Add class description
 *
 * @future
 *    - 
 *
 * @author Ken Cline
 * @see Table
 * @see java.lang.RuntimeException
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
public class MissingDataException extends RuntimeException
{
  // TBD: Generate serialization ID using serialver tool
  // TBD: Add doc
  // NOTE: I would not have made an exception class serializable
  //       but it must inherit that from RuntimeException because
  //       the Java compiler complains about needing serialVersionUID.
  //       I suppose exceptions need to be serializable for RMI
  //       to work.
  private static final long serialVersionUID = 1L;    

  // TBD: Add doc
  private static final Logger log
             = LoggerFactory.getLogger( MissingDataException.class);


  /**
   * The data object or source that did not contain the desired
   * data entries, content or subcomponent.  The data object
   * might be a {@link java.util.Collection Collection},
   * {@link java.io.File File} or almost any type of object.
   *
   */
  protected Object data;

  /**
   * The name or identifier used to search for the data entry or
   * subcomponent that was not found, e.g. if <code>data</code>
   * is an instance of {@link java.util.Map Map}, then <code>key</code>
   * might be object passed to the {@link java.util.Map#get get} method
   * which returned <code>null</code>
   *
   */
  protected Object key;


  /**
   * TBD: Add doc
   *
   */
  protected static String defaultMsgFmt
                       = "Value for key ''{1}'' missing in data ''{0}''";


  /**
   * Constructs a <code>MissingResourceException</code> with
   * <code>message</code>, <code>className</code>, <code>key</code>,
   * and <code>cause</code>. This constructor is package private for
   * use by <code>ResourceBundle.getBundle</code>.
   *
   * @param msg     the detail message
   * @param data    the structure or source object that was searched
   * @param key     the key for the entry or subcomponent not found in
   *                <code>data</code>
   * @param cause   the cause (which is saved for later retrieval by the
   *                {@link Throwable.getCause()} method). (A null value is
   *                permitted, and indicates that the cause is nonexistent
   *                or unknown.)
   */
  MissingDataException( String msg, Object data, Object key, Throwable cause ) {
    super( msg, cause );
    this.data = data;
    this.key = key;
  }

  /**
   * Constructs a MissingDataException with the specified information.
   * A detail message is a String that describes this particular exception.
   *
   * @param msg     the detail message
   * @param data    the structure or source object that was searched
   * @param key     the key for the entry or subcomponent not found in
   *                <code>data</code>
   */
  public MissingDataException ( String msg, Object data, Object key ) {
    super( msg );
    this.data = data;
    this.key = key;
  }

  // TBD: Should the constructor w/o the Throwable cause simply call the
  //      constructor that accepts the Throwable cause and use null as
  //      the cause?
  //      I started with the MissingResourceException class when I created
  //      this exception and in their constructors, they did NOT do the
  //      chain.  I'm not sure why though.


  /**
   * Constructs a <code>MissingResourceException</code> with
   * <code>message</code>, <code>className</code>, <code>key</code>,
   * and <code>cause</code>. This constructor is package private for
   * use by <code>ResourceBundle.getBundle</code>.
   *
   * @param msg     the detail message
   * @param data    the structure or source object that was searched
   * @param key     the key for the entry or subcomponent not found in
   *                <code>data</code>
   * @param cause   the cause (which is saved for later retrieval by the
   *                {@link Throwable.getCause()} method). (A null value is
   *                permitted, and indicates that the cause is nonexistent
   *                or unknown.)
   */
  MissingDataException( Object data, Object key, Throwable cause ) {
    this( createMessage(defaultMsgFmt,data,key), data, key, cause );
  }

  /**
   * Constructs a MissingDataException with the specified information.
   * A detail message is a String that describes this particular exception.
   *
   * @param msg     the detail message
   * @param data    the structure or source object that was searched
   * @param key     the key for the entry or subcomponent not found in
   *                <code>data</code>
   */
  public MissingDataException ( Object data, Object key ) {
    this( createMessage(defaultMsgFmt,data,key), data, key );
  }


  /**
   * Gets the data parameter passed by constructor.
   *
   * @return the data structure or source that was incomplete or missing
   */
  public Object getData () {
    return data;
  }

  /**
   * Gets the key parameter passed by constructor.
   *
   * @return the key or identifier for the entry or subcomponent that
   *         was not found in the data structure or source
   */
  public Object getKey () {
    return key;
  }


  // === [ Convenience Methods ] ==========================================

  /**
   * TBD: Add doc
   *
   */
  protected static String createMessage ( String fmt, Object data, Object key ) {
    return MessageFormat.format( fmt, data, key );
  }


}  // end of MissingDataException class

