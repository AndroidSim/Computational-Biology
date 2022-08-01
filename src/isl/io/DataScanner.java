/*
 * Copyright 2003-2016 - Regents of the University of California, San
 * Francisco.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package isl.io;

import java.io.*;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import bsg.util.ClassUtils;
import static bsg.util.StringUtils.*;


//import java.net.URL;
// TBD: import java.text.Format;


/**
 * A simple text scanner which can parse primitive types and strings using
 * regular expressions.
 *
 * This class is a wrapper around Java's @{link Scanner} class with a few
 * convenience methods added.  Since the @{link Scanner} class is <code>
 * final</code>, it must be wrappered instead of extended.
 *
 * TBD: Add more class description
 *
 * @future
 *    - 
 *
 * @author Ken Cline
 * @see Scanner
 * @since 1.0
 * @version $Revision: $  $Date: $
 *
 * $Id: $
 */
public class DataScanner implements Iterator<String>
{

  // TBD: Add doc
  private static final Logger log = LoggerFactory.getLogger( DataScanner.class );


  /**
   * TBD: Add doc
   */
  protected Scanner scanner = null;

  /**
   * TBD: Add doc
   */
  protected Object source = null;

  /**
   * TBD: Add doc
   */
  protected String charsetName = null;


  /**
   * Constructs a <code>DataScanner</code> delegates scanning calls
   * to the provided @{link Scanner}.
   *
   * This constructor is called by all other constructors after they
   * create an instance of <code>Scanner</code> from their respective
   * arguments.
   * 
   * @param scan  A pre-initialized <code>Scanner</code> object
   * @return A wrapped scanner
   */
  public DataScanner( Scanner scan ) {
    if ( scan == null ) {
      throw new NullPointerException( "scanner" );
    }

    this.scanner = scan;
  }

  /**
   * Constructs a new <code>DataScanner</code> that produces values scanned
   * from the specified source.
   *
   * @param  src A character source implementing the {@link Readable}
   *             interface
   */
  public DataScanner ( Readable src ) {
    this( new Scanner(src) );
    this.source = src;
  }

  /**
   * Constructs a new <code>DataScanner</code> that produces values scanned
   * from the specified input stream. Bytes from the stream are converted
   * into characters using the underlying platform's
   * {@linkplain java.nio.charset.Charset#defaultCharset() default charset}.
   *
   * @param src An input stream to be scanned
   */
  public DataScanner ( InputStream src ) {
    this( new Scanner(src) );
    this.source = src;
  }

  /**
   * Constructs a new <code>DataScanner</code> that produces values scanned
   * from the specified input stream. Bytes from the stream are converted
   * into characters using the specified charset.
   *
   * @param src An input stream to be scanned
   * @param charsetName The encoding type used to convert bytes from the
   *        stream into characters to be scanned
   * @throws IllegalArgumentException if the specified character set
   *         does not exist
   */
  public DataScanner ( InputStream src, String charsetName ) {
    this( new Scanner(src,charsetName) );
    this.source = src;
    this.charsetName = charsetName;
  }

  /**
   * Constructs a new <code>DataScanner</code> that produces values scanned
   * from the specified file. Bytes from the file are converted into
   * characters using the underlying platform's
   * {@linkplain java.nio.charset.Charset#defaultCharset() default charset}.
   *
   * @param src A file to be scanned
   * @throws FileNotFoundException if source is not found
   */
  public DataScanner ( File src )
    throws FileNotFoundException
  {
    this( new Scanner(src) );
    this.source = src;
  }

  /**
   * Constructs a new <code>DataScanner</code> that produces values scanned
   * from the specified file. Bytes from the file are converted into
   * characters using the specified charset.
   *
   * @param src A file to be scanned
   * @param charsetName The encoding type used to convert bytes from the file
   *        into characters to be scanned
   * @throws FileNotFoundException if source is not found
   * @throws IllegalArgumentException if the specified encoding is
   *         not found
   */
  public DataScanner ( File src, String charsetName )
    throws FileNotFoundException
  {
    this( new Scanner(src,charsetName) );
    this.source = src;
    this.charsetName = charsetName;
  }

  /**
   * Constructs a new <code>DataScanner</code> that produces values scanned
   * from the specified string.
   *
   * @param src A string to scan
   */
  public DataScanner ( String src ) {
    this( new Scanner(src) );
    this.source = src;
  }

  /**
   * Constructs a new <code>DataScanner</code> that produces values scanned
   * from the specified channel. Bytes from the source are converted into
   * characters using the underlying platform's
   * {@linkplain java.nio.charset.Charset#defaultCharset() default charset}.
   *
   * @param src A channel to scan
   */
  public DataScanner ( ReadableByteChannel src ) {
    this( new Scanner(src) );
    this.source = src;
  }

  /**
   * Constructs a new <code>DataScanner</code> that produces values scanned
   * from the specified channel. Bytes from the source are converted into
   * characters using the specified charset.
   *
   * @param src A channel to scan
   * @param charsetName The encoding type used to convert bytes from the
   *        channel into characters to be scanned
   * @throws IllegalArgumentException if the specified character set
   *         does not exist
   */
  public DataScanner ( ReadableByteChannel src, String charsetName ) {
    this( new Scanner(src,charsetName) );
    this.source = src;
    this.charsetName = charsetName;
  }


  // === [ Configuration Methods ] ========================================

  /**
   * {@copydoc Scanner#delimiter()}
   */
  public Pattern delimiter() {
    return scanner.delimiter();
  }

  /**
   * {@copydoc Scanner#useDelimiter(Pattern)}
   */
  public DataScanner useDelimiter ( Pattern pattern ) {
    scanner.useDelimiter( pattern );
    return this;
  }

  /**
   * {@copydoc Scanner#useDelimiter(String)}
   */
  public DataScanner useDelimiter ( String pattern ) {
    scanner.useDelimiter( pattern );
    return this;
  }


  /**
   * {@copydoc Scanner#locale()}
   */
  public Locale locale () {
    return scanner.locale();
  }

  /**
   * {@copydoc Scanner#useLocale(Locale)}
   */
  public DataScanner useLocale ( Locale locale ) {
    scanner.useLocale( locale );
    return this;
  }


  /**
   * {@copydoc Scanner#radix()}
   */
  public int radix () {
    return scanner.radix();
  }

  /**
   * {@copydoc Scanner#useRadix(int)}
   */
  public DataScanner useRadix ( int radix ) {
    scanner.useRadix( radix );
    return this;
  }


  // === [ Match, Find & Skip Methods ] ===================================

  // Public methods that ignore delimiters

  /**
   * {@copydoc Scanner#match()}
   */
  public MatchResult match () {
    return scanner.match();
  }


  /**
   * {@copydoc Scanner#findInLine(String)}
   */
  public String findInLine ( String pattern ) {
    return scanner.findInLine( pattern );
  }

  /**
   * {@copydoc Scanner#findInLine(Pattern)}
   */
  public String findInLine ( Pattern pattern ) {
    return scanner.findInLine( pattern );
  }

  /**
   * {@copydoc Scanner#findWithinHorizon(String,int)}
   */
  public String findWithinHorizon ( String pattern, int horizon ) {
    return scanner.findWithinHorizon( pattern, horizon );
  }

  /**
   * {@copydoc Scanner#findWithinHorizon(Pattern,int)}
   */
  public String findWithinHorizon ( Pattern pattern, int horizon ) {
    return scanner.findWithinHorizon( pattern, horizon );
  }


  /**
   * {@copydoc Scanner#skip(Pattern)}
   */
  public DataScanner skip ( Pattern pattern ) {
    scanner.skip( pattern );
    return this;
  }

  /**
   * {@copydoc Scanner#skip(String)}
   */
  public DataScanner skip ( String pattern ) {
    scanner.skip( pattern );
    return this;
  }


  // === [ Iteration Methods: String ] ====================================

  /**
   * {@copydoc Scanner#hasNext()}
   */
  public boolean hasNext () {
    return scanner.hasNext();
  }

  /**
   * {@copydoc Scanner#next()}
   */
  public String next () {
    return scanner.next();
  }


  /**
   * {@copydoc Scanner#remove()}
   */
  public void remove () {
    scanner.remove();
  }


  /**
   * {@copydoc Scanner#hasNext(String)}
   */
  public boolean hasNext ( String pattern ) {
    return scanner.hasNext( pattern );
  }

  /**
   * {@copydoc Scanner#next(String)}
   */
  public String next ( String pattern ) {
    return scanner.next( pattern );
  }

  /**
   * {@copydoc Scanner#hasNext(Pattern)}
   */
  public boolean hasNext ( Pattern pattern ) {
    return scanner.hasNext( pattern );
  }

  /**
   * {@copydoc Scanner#next(Pattern)}
   */
  public String next ( Pattern pattern ) {
    return scanner.next( pattern );
  }


  /**
   * {@copydoc Scanner#hasNextLine()}
   */
  public boolean hasNextLine () {
    return scanner.hasNextLine();
  }

  /**
   * {@copydoc Scanner#nextLine()}
   */
  public String nextLine () {
    return scanner.nextLine();
  }


  // === [ Iteration Methods: Boolean ] ===================================

  /**
   * {@copydoc Scanner#hasNextBoolean()}
   */
  public boolean hasNextBoolean () {
    return scanner.hasNextBoolean();
  }

  /**
   * {@copydoc Scanner#nextBoolean()}
   */
  public boolean nextBoolean () {
    return scanner.nextBoolean();
  }


  // === [ Iteration Methods: Byte ] ======================================

  /**
   * {@copydoc Scanner#hasNextByte()}
   */
  public boolean hasNextByte () {
    return scanner.hasNextByte();
  }

  /**
   * {@copydoc Scanner#hasNextByte(int)}
   */
  public boolean hasNextByte ( int radix ) {
    return scanner.hasNextByte( radix );
  }

  /**
   * {@copydoc Scanner#nextByte()}
   */
  public byte nextByte () {
    return scanner.nextByte();
  }

  /**
   * {@copydoc Scanner#nextByte(int)}
   */
  public byte nextByte ( int radix ) {
    return scanner.nextByte( radix );
  }


  // === [ Iteration Methods: Short ] =====================================

  /**
   * {@copydoc Scanner#hasNextShort()}
   */
  public boolean hasNextShort () {
    return scanner.hasNextShort();
  }

  /**
   * {@copydoc Scanner#hasNextShort(int)}
   */
  public boolean hasNextShort ( int radix ) {
    return scanner.hasNextShort( radix );
  }

  /**
   * {@copydoc Scanner#nextShort()}
   */
  public short nextShort () {
    return scanner.nextShort();
  }

  /**
   * {@copydoc Scanner#nextShort(int)}
   */
  public short nextShort ( int radix ) {
    return scanner.nextShort( radix );
  }


  // === [ Iteration Methods: Int ] =======================================

  /**
   * {@copydoc Scanner#hasNextInt()}
   */
  public boolean hasNextInt () {
    return scanner.hasNextInt();
  }

  /**
   * {@copydoc Scanner#hasNextInt(int)}
   */
  public boolean hasNextInt ( int radix ) {
    return scanner.hasNextInt( radix );
  }

  /**
   * {@copydoc Scanner#nextInt()}
   */
  public int nextInt () {
    return scanner.nextInt();
  }

  /**
   * {@copydoc Scanner#nextInt(int)}
   */
  public int nextInt ( int radix ) {
    return scanner.nextInt( radix );
  }


  // === [ Iteration Methods: Long ] ======================================

  /**
   * {@copydoc Scanner#hasNextLong()}
   */
  public boolean hasNextLong () {
    return scanner.hasNextLong();
  }

  /**
   * {@copydoc Scanner#hasNextLong(int)}
   */
  public boolean hasNextLong ( int radix ) {
    return scanner.hasNextLong( radix );
  }

  /**
   * {@copydoc Scanner#nextLong()}
   */
  public long nextLong () {
    return scanner.nextLong();
  }

  /**
   * {@copydoc Scanner#nextLong(int)}
   */
  public long nextLong ( int radix ) {
    return scanner.nextLong( radix );
  }


  // === [ Iteration Methods: Float ] =====================================

  /**
   * {@copydoc Scanner#hasNextFloat()}
   */
  public boolean hasNextFloat () {
    return scanner.hasNextFloat();
  }

  /**
   * {@copydoc Scanner#nextFloat()}
   */
  public float nextFloat () {
    return scanner.nextFloat();
  }


  // === [ Iteration Methods: Double ] ====================================

  /**
   * {@copydoc Scanner#hasNextDouble()}
   */
  public boolean hasNextDouble () {
    return scanner.hasNextDouble();
  }

  /**
   * {@copydoc Scanner#nextDouble()}
   */
  public double nextDouble () {
    return scanner.nextDouble();
  }


  // Convenience methods for scanning multi precision numbers

  // === [ Iteration Methods: BigInteger ] ================================

  /**
   * {@copydoc Scanner#hasNextBigInteger()}
   */
  public boolean hasNextBigInteger () {
    return scanner.hasNextBigInteger();
  }

  /**
   * {@copydoc Scanner#hasNextBigInteger(int)}
   */
  public boolean hasNextBigInteger ( int radix ) {
    return scanner.hasNextBigInteger( radix );
  }

  /**
   * {@copydoc Scanner#nextBigInteger()}
   */
  public BigInteger nextBigInteger () {
    return scanner.nextBigInteger();
  }

  /**
   * {@copydoc Scanner#nextBigInteger(int)}
   */
  public BigInteger nextBigInteger ( int radix ) {
    return scanner.nextBigInteger( radix );
  }


  // === [ Iteration Methods: BigDecimal ] ================================

  /**
   * {@copydoc Scanner#hasNextBigDecimal()}
   */
  public boolean hasNextBigDecimal () {
    return scanner.hasNextBigDecimal();
  }

  /**
   * {@copydoc Scanner#nextBigDecimal()}
   */
  public BigDecimal nextBigDecimal () {
    return scanner.nextBigDecimal();
  }


  // === [ Input Operations ] =============================================

  // Scanner operations, e.g. close, reset

  /**
   * {@copydoc Scanner#close()}
   */
  public void close () {
    scanner.close();
  }


  /**
   * {@copydoc Scanner#reset()}
   */
  public DataScanner reset () {
    scanner.reset();
    // TBD: Does the DataScanner need to do any additional steps
    //      (beyond what Scanner does) during reset?
    return this;
  }


  /**
   * {@copydoc Scanner#ioException()}
   */
  public IOException ioException () {
    return scanner.ioException();
  }


  /**
   * {@copydoc Scanner#toString()}
   */
  public String toString () {
    // TBD: Modify this for DataScanner, eg include source and charsetName
    //      if non-null
    return scanner.toString();
  }


  // === [ Generic Type Convenience Methods ] =============================

  /**
   * TBD: Add doc
   *
   */
  public boolean hasNext ( Class<?> cls ) {
    Method hasNext  = null;
    Object value    = null;

    try {
      hasNext = getScannerMethod( cls, "hasNext" );
      //log.debug( "Target: {}, class: {}, method: {}",
      //           new Object[]{this,cls,hasNext} );
      value = hasNext.invoke( this );
      return ((Boolean)value).booleanValue();
    } catch ( Exception e ) {
      // HACK: Add more information to message and possibly break up
      //       catch into separate types
      String msg = "Unable to scan next value of type '" + cls + "'"
                 + " (scanner method: " + hasNext + ")";
      log.error( msg, e );
      throw new RuntimeException( msg, e );
    }
  }


  /**
   * TBD: Add doc
   *
   * @throws ClassCastException if return value from the <code>next<T></code>
   *            method, <i>invoked via reflection</i>, can not be converted
   *            to type <code>T</code>; <b>this will occur if the specified
   *            <code>cls</code> is a primitive type such as
   *            <code>int.class</code></b> (use <code>Integer.class</code>
   *            instead
   */
  public <T> T next ( Class<T> cls ) {
    Method next   = null;
    Object value  = null;

    try {
      next = getScannerMethod( cls, "next" );
      //log.debug( "Target: {}, class: {}, method: {}",
      //           new Object[]{this,cls,next} );
      value = next.invoke( this );
      return cls.cast( value );
    } catch ( Exception e ) {
      // HACK: Add more information to message and possibly break up
      //       catch into separate types
      String msg = "Unable to scan next value of type '" + cls + "'"
                 + " (scanner method: " + next + ")";
      log.error( msg, e );
      throw new RuntimeException( msg, e );
    }
  }


  /**
   * TBD: Add doc
   */
  protected static Map<Class<?>, Method> methodCache = new LinkedHashMap<>();

  /**
   * TBD: Add doc
   */
  public static final Class<?> [] NO_PARAMS = new Class<?>[ 0 ];


  /**
   * TBD: Add doc
   */
  public Method getScannerMethod ( Class<?> cls, String pfx )
                          throws NoSuchMethodException
  {
    Method mthd = null;
    String name = null;

    //log.debug( "Getting scanner method for type '{}'", cls );
    mthd = methodCache.get( cls );

    if ( mthd == null ) {
      name = createScannerMethodName( cls, pfx ).toString();
      mthd = this.getClass().getMethod( name, NO_PARAMS );
      log.debug( "Adding method '{}' to cache for type '{}'", name, cls );
      methodCache.put( cls, mthd );
    }

    return mthd;
  }


  // TBD: MessageFormat object of creating method name
  // TBD: Create a ClassNameFormat object that converts Class to name

  /**
   * TBD: Add doc
   */
  protected static StringBuilder createScannerMethodName ( Class<?> cls, String pfx )
  {
    StringBuilder name = new StringBuilder( pfx );
    String clsName = null;

    if ( cls == null ) {
      log.debug( "No class specified; returning default scanner method name: '{}'",
                 name );
      return name;
    }

    if ( String.class.isAssignableFrom(cls) ) {
      log.debug( "Specified class is String;"
               + " returning default scanner method name: '{}'", name );
      return name;
    }

    if ( Integer.class.isAssignableFrom(cls) ) {
      name.append( "Int" );
      log.debug( "Specified class is Integer;"
               + " returning scanner method name: '{}'", name );
      return name;
    }

    clsName = cls.getSimpleName();
    log.debug( "Creating scanner method name for class '{}' (simple name: '{}')",
               cls, clsName );

    if ( cls.isPrimitive() ) {
      clsName = capitalizeFirst( clsName );
      log.debug( "Class is a primitive type, capitalized class name: '{}'",
                 clsName );
    }

    // TBD: Check if isArray, isEnum, isAnonymousClass, isAnnotation, other?

    name = name.append( clsName );

    //log.debug( "Returning scanner method name '{}' for class '{}'", name, cls );
    return name;
  }



  // ======================================================================

  // Main method for command line testing
  // ------------------------------------

  /**
   *
   */
  public static void main ( String[] args ) {
    int MAX_LINES   = 5;
    int MAX_VALUES  = 5;
    DataScanner lineScan = null;
    DataScanner  csvScan = null;
    String   line = null;
    Class<?> type = null;
    Object   val  = null;

    for ( String arg : args ) {

      try {
        lineScan = new DataScanner( ClassUtils.getResourceFile(arg) );
        // lineScan.skip( "^(\\s*#)?" );
        log.debug( "Scanning data from file '{}'", arg );

        for ( int i = 0 ; lineScan.hasNextLine() && i < MAX_LINES ; i++ ) {
          line = lineScan.nextLine();
          type = ( i == 0 ? String.class : Double.class );
          csvScan = new DataScanner( line ).useDelimiter( "\\s*,\\s*" );

          for ( int j = 0 ; csvScan.hasNext() && j < MAX_VALUES ; j++ ) {
            val = csvScan.next( type );
            log.debug( "value at ({},{}) = {}", new Object[]{i,j,val} );
          }

        }
      } catch ( Exception e ) {
        String msg = "Unable to scan data from file '" + arg + "'"; 
        log.error( msg, e );
        //throw new RuntimeException( msg, e );
      }

    }

  }    

  // TBD:
  //    * Skip method should report how much was skipped
  //    * Create a skip method that ignore NoSuchElementException
  //    * Create a skipLine method, perhaps

}  // end of DataScanner class


