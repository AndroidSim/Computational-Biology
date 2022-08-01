
#include <stdlib.h>
#include <iostream>
#include <typeinfo>
#include <algorithm>
#include <string>
#include <stdarg.h>
#include <string.h>
//#include <math.h>
//#include <complex>

#include "isl_model_ref_AbstractCDModelTest.h"
#include "Model.h"
#include "ConvectionDispersion.h"
#include "ExtendedConvectionDispersion.h"

using namespace std;

extern "C" {

  // === [ Logging Constants and Utility Methods ] ========================

  /**
   * TBD: Add doc
   *
   */
  const int LOG_LEVEL_ALL    =   0;
  const int LOG_LEVEL_TRACE  =  10;
  const int LOG_LEVEL_DEBUG  =  20;
  const int LOG_LEVEL_INFO   =  30;
  const int LOG_LEVEL_WARN   =  40;
  const int LOG_LEVEL_ERROR  =  50;
  const int LOG_LEVEL_OFF    = 100;

  const int DEFAULT_LOG_LEVEL = LOG_LEVEL_DEBUG;

  static int LogLevel = DEFAULT_LOG_LEVEL;


  /**
   * TBD: Add doc
   *
   */
  struct LogLevelEntry {
    const char * key;
    int level;
  };

  LogLevelEntry FuncLevels [] = {
    { "getLogLevelValue",                LOG_LEVEL_DEBUG },
    { "getLogLevelName",                 LOG_LEVEL_DEBUG },
    { "setLogLevel",                     LOG_LEVEL_DEBUG },
    { "getLogLevel",                     LOG_LEVEL_DEBUG },
    { "isLogLevel",                      LOG_LEVEL_DEBUG },

    { "getBesselFunction",               LOG_LEVEL_DEBUG },

    { "complexSqrt",                     LOG_LEVEL_DEBUG },
    { "complexExp",                      LOG_LEVEL_DEBUG },
    { "getComplexMathFunction",          LOG_LEVEL_DEBUG },

    { "length",                          LOG_LEVEL_INFO  },
    { "listStrings",                     LOG_LEVEL_INFO  },

    { "isMatchingTypesMapEntry",         LOG_LEVEL_INFO  },
    { "getTypesMapEntry",                LOG_LEVEL_INFO  },
    { "getConversionSpecifier",          LOG_LEVEL_INFO  },
    { "getVarArg",                       LOG_LEVEL_DEBUG },
    { "toStringArray",                   LOG_LEVEL_INFO  },
    { "listArgs",                        LOG_LEVEL_INFO  },

    { "instanceOf",                      LOG_LEVEL_INFO  },
    { "isConventionalCDModel",           LOG_LEVEL_INFO  },
    { "isExtendedCDModel",               LOG_LEVEL_INFO  },
    { "getClsName",                      LOG_LEVEL_INFO  },
    { "getClassName",                    LOG_LEVEL_INFO  },
    { "getConstructor",                  LOG_LEVEL_INFO  },
    { "newObject",                       LOG_LEVEL_INFO  },
    { "createGetterName",                LOG_LEVEL_INFO  },
    { "getGetter",                       LOG_LEVEL_INFO  },
    { "getBoolean",                      LOG_LEVEL_INFO  },
    { "getDouble",                       LOG_LEVEL_INFO  },
    { "toCppComplex",                    LOG_LEVEL_INFO  },
    { "toJavaComplex",                   LOG_LEVEL_INFO  },
    { "getComplex",                      LOG_LEVEL_INFO },
    { "getCppClassName",                 LOG_LEVEL_INFO  },
    { "isInstanceOf",                    LOG_LEVEL_INFO  },
    { "isConvectionDispersion",          LOG_LEVEL_INFO  },
    { "isExtendedConvectionDispersion",  LOG_LEVEL_INFO  },

    { "setCDModelDefaults",              LOG_LEVEL_DEBUG },
    { "setCDModel",                      LOG_LEVEL_INFO  },

    { "Java_isl_model_ref_AbstractCDModelTest_setCppLogLevel", LOG_LEVEL_INFO  },
    { "Java_isl_model_ref_AbstractCDModelTest_createCppCDM",   LOG_LEVEL_INFO },
    { "Java_isl_model_ref_AbstractCDModelTest_deleteCppCDM",   LOG_LEVEL_INFO  },
    { "Java_isl_model_ref_AbstractCDModelTest_ecd",            LOG_LEVEL_DEBUG },
    { "Java_isl_model_ref_AbstractCDModelTest_integrand",      LOG_LEVEL_DEBUG },
    { "Java_isl_model_ref_AbstractCDModelTest_bessel",         LOG_LEVEL_DEBUG },
    { "Java_isl_model_ref_AbstractCDModelTest_complexMath",    LOG_LEVEL_INFO  },
    { "Java_isl_model_ref_AbstractCDModelTest_l_1ecd",         LOG_LEVEL_DEBUG },
    { "Java_isl_model_ref_AbstractCDModelTest_g",              LOG_LEVEL_DEBUG },

  };

  /**
   * TBD: Add doc
   *
   */
  int getLogLevelValue ( const char * name ) {
    string str = name;

    if ( name == NULL ) return DEFAULT_LOG_LEVEL;

    transform( str.begin(), str.end(), str.begin(), ::toupper );

    if ( str == "ALL"   ) return LOG_LEVEL_ALL;
    if ( str == "TRACE" ) return LOG_LEVEL_TRACE;
    if ( str == "DEBUG" ) return LOG_LEVEL_DEBUG;
    if ( str == "INFO"  ) return LOG_LEVEL_INFO;
    if ( str == "WARN"  ) return LOG_LEVEL_WARN;
    if ( str == "ERROR" ) return LOG_LEVEL_ERROR;
    if ( str == "OFF"   ) return LOG_LEVEL_OFF;

    cout << "ERROR: Unknown Log Level name '" << name << "'"
         << " -- returning zero (0)"
         << endl;
    return 0;

  }  // end method -- getLogLevelValue(const char *)

  /**
   * TBD: Add doc
   *
   */
  const char * getLogLevelName ( int level ) {

    if ( level == LOG_LEVEL_ALL   ) return "ALL";
    if ( level == LOG_LEVEL_TRACE ) return "TRACE";
    if ( level == LOG_LEVEL_DEBUG ) return "DEBUG";
    if ( level == LOG_LEVEL_INFO  ) return "INFO";
    if ( level == LOG_LEVEL_WARN  ) return "WARN";
    if ( level == LOG_LEVEL_ERROR ) return "ERROR";
    if ( level == LOG_LEVEL_OFF   ) return "OFF";

    cout << "ERROR: Unknown Log Level value '" << level << "'"
         << " -- returning empty string ''"
         << endl;
    return "";

  }  // end method -- getLogLevelName(int)

  /**
   * TBD: Add doc
   *
   */
  void setLogLevel ( const char * name ) {
    if ( name == NULL ) {
      LogLevel = DEFAULT_LOG_LEVEL;
      cout << "DEBUG: Specified level name is null"
           << " -- Log level set to default"
           << " '" << getLogLevelName(LogLevel) << "' (" << LogLevel << ")"
           << endl;
    }

    LogLevel = getLogLevelValue( name );
    // cout << "DEBUG: Log level set to '" << name << "' (" << LogLevel << ")"
    //     << endl;

  }  // end method -- setLogLevel(const char *)


  /**
   * TBD: Add doc
   *
   */
  int getLogLevel ( const char * func, int line ) {
    LogLevelEntry entry;
    int N = sizeof( FuncLevels ) / sizeof( LogLevelEntry );

    //cout << "DEBUG: Getting the log level for function '" << func << "'"
    //     << " and line '" << line << "' ..."
    //     << endl;

    if ( func == NULL ) {
      return LogLevel;
    }

    for ( int i = 0 ; i < N ; i++ ) {
      entry = FuncLevels[ i ];
      if ( strstr(func,entry.key) != NULL ) {
        //cout << "DEBUG: Function '" << func << "' matched Log Level Map"
        //     << " entry with key '" << entry.key << "' -- returning"
        //     << " entry.level = " << getLogLevelName(entry.level)
        //     << " (" << entry.level << ")"
        //     << endl;
        return entry.level;
      }
    }

    cout << "DEBUG: Function '" << func << "' did NOT match any entries"
         << " in the Log Level Map -- returning default log level"
         << " = " << getLogLevelName(LogLevel) << " (" << LogLevel << ")"
         << endl;

    return LogLevel;

  }  // end method -- getLogLevel(const char *,int)


  /**
   * TBD: Add doc
   *
   */
  bool isLogLevel ( int level, const char * func, int line ) {
    int lvl = getLogLevel( func, line );

    if ( level == LOG_LEVEL_OFF ) {
      return ( lvl == LOG_LEVEL_OFF );
    }
    return ( level >= lvl );

  }  // end method -- isLogLevel(int,const char *,int)

#define isAllEnabled()   isLogLevel( LOG_LEVEL_ALL,    __func__, __LINE__ )
#define isTraceEnabled() isLogLevel( LOG_LEVEL_TRACE,  __func__, __LINE__ )
#define isDebugEnabled() isLogLevel( LOG_LEVEL_DEBUG,  __func__, __LINE__ )
#define isInfoEnabled()  isLogLevel( LOG_LEVEL_INFO,   __func__, __LINE__ )
#define isWarnEnabled()  isLogLevel( LOG_LEVEL_WARN,   __func__, __LINE__ )
#define isErrorEnabled() isLogLevel( LOG_LEVEL_ERROR,  __func__, __LINE__ )
#define isOffEnabled()   isLogLevel( LOG_LEVEL_OFF,    __func__, __LINE__ )


  // === [ Math Utility Methods ] =========================================

  typedef double (* BesselFunction)( double );

  /**
   * TBD: Add doc
   *
   */
  BesselFunction getBesselFunction ( const char * name ) {
    string str = name;

    if ( name == NULL ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Bessel function name not specified; exiting..."
             << endl;
      }
      exit( -1 );
    }

    transform( str.begin(), str.end(), str.begin(), ::tolower );

    if ( str == "j0" ) return &j0;
    if ( str == "j1" ) return &j1;
    if ( str == "y0" ) return &y0;
    if ( str == "y1" ) return &y1;

    // if ( str == "jn" ) return &jn;
    // if ( str == "yn" ) return &yn;
    if ( str == "jn" || str == "yn" ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Unsupported Bessel function type '" << name << "';"
             << " exiting..."
             << endl;
      }
      exit( -1 );
    }

    if ( isErrorEnabled() ) {
      cout << "ERROR: Unknown Bessel function name '" << name << "'"
           << " -- exiting..."
           << endl;
    }
    exit( -1 );

  }  // end method -- getBesselFunction(const char *)


  typedef complex<double> (* ComplexMathFunction)( complex<double> );


  complex<double> complexSqrt ( complex<double> z ) {
    return sqrt( z );
  }

  complex<double> complexExp ( complex<double> z ) {
    return exp( z );
  }


  /**
   * TBD: Add doc
   *
   */
  ComplexMathFunction getComplexMathFunction ( const char * name ) {
    string str = name;

    if ( name == NULL ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Complex math function name not specified; exiting..."
             << endl;
      }
      exit( -1 );
    }

    transform( str.begin(), str.end(), str.begin(), ::tolower );

    if ( str == "sqrt" )  return &complexSqrt;
    if ( str == "exp"  )  return &complexExp;

    // if ( str == "add" )       return &complexAdd;
    // if ( str == "subtract" )  return &complexSubtract;
    // if ( str == "multiply" )  return &complexMultiply;
    // if ( str == "divide"   )  return &complexDivide;

    if ( isErrorEnabled() ) {
      cout << "ERROR: Unknown Complex math function name '" << name << "'"
           << " -- exiting..."
           << endl;
    }
    exit( -1 );

  }  // end method -- getComplexMathFunction(const char *)


  // === [ String Utility Methods ] =======================================

  /**
   * TBD: Add doc
   *
   */
  int length ( const char * arr [] ) {
    return ( arr == NULL ? 0
                         : (sizeof(arr) / sizeof(const char *)) + 1 );
  }

  const char * ELEMENT_SEPARATOR  = ", ";
  const char * ELEMENT_PREFIX     = "'";
  const char * ELEMENT_SUFFIX     = "'";
  const char * ELEMENT_UNKNOWN    = "?";

  /**
   * TBD: Add doc
   *
   */
  string listStrings ( const char * values[],
                       const char * pfx = "'",  const char * sfx = "'",
                       const char * sep = ", ", const char * unk = "?",
                       bool freeValues = false )
  {
    int     N     = length( (const char **)values );
    string  list  = "";

    pfx = ( pfx ? pfx : ELEMENT_PREFIX    );
    sfx = ( sfx ? sfx : ELEMENT_SUFFIX    );
    sep = ( sep ? sep : ELEMENT_SEPARATOR );
    unk = ( unk ? unk : ELEMENT_UNKNOWN   );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Listing string array as a concatenated string"
           << " with element prefix: '" << pfx << "', suffix: '" << sfx<< "'"
           << ", separator: '" << sep << "'"
           << " and unknown value identifier: '" << unk << "'"
           << endl;
    }

    for ( int i = 0 ; i < N ; i++ ) {
      if ( list.size() > 0 ) list += sep;

      if ( strlen(values[i]) > 0 ) {
        list.append( pfx ).append( values[i] ).append( sfx );
      } else {
        list.append( unk );
        // list.append( " (error: " ).append( status ).append( ")" );
      }

      if ( freeValues ) {
        if ( isDebugEnabled() ) {
          cout << "DEBUG: Freeing values[ " << i << " ] of string array"
               << " ('" << values[i] << "')"
               << endl;
        }
        free( (char *)values[i] );
        values[ i ] = NULL;
      }
    }

    if ( freeValues ) {
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Freeing values string array"
             << endl;
      }
      free( values );
      values = NULL;
    }

    if ( isDebugEnabled() ) {
      cout << "DEBUG: String array as list: " << list
           << endl;
    }

    return list;

  }  // end method -- listStrings(const char *,va_list, [...])


  // === [ JNI Utility Methods ] ==========================================

  // From: http://download.oracle.com/javase/1.5.0/docs/guide/jni/spec/types.html
  //
  // Type Signature              Java Type               C Conversion Specifier
  // --------------              ---------               ----------------------
  //  Z                          boolean                  "%d"
  //  B                          byte                     "%"  ???
  //  C                          char                     "%c"
  //  S                          short                    "%hd"
  //  I                          int                      "%d"
  //  J                          long                     "%ld"
  //  F                          float                    "%f"
  //  D                          double                   "%Lg"
  //  L fully-qualified-class ;  fully-qualified-class    "%p"
  //  [ type                     type[]                   "%p"  ???
  //  ( arg-types ) ret-type     method type              N/A
  //
  // Example, the Java method:
  //    long f (int n, String s, int[] arr); 
  // has the following type signature:
  //    (ILjava/lang/String;[I)J 
  //

  /**
   * TBD: Add doc
   *
   */
  struct TypeEntry {
    const char * typeName;
    char javaKey;
    const char * specifier;
    const char * cType;
  };

  TypeEntry TypesMap [] = {
    { "boolean", 'Z',  "%d",   "bool"         },
    { "byte",    'B',  "%c",   "signed char"  },   // Java byte == signed char ???
    { "char",    'C',  "%c",   "char"         },
    { "short",   'S',  "%hd",  "short int"    },
    { "int",     'I',  "%d",   "int"          },
    { "long",    'J',  "%lld", "long long"    },
    { "float",   'F',  "%f",   "float"        },
    { "double",  'D',  "%g",   "double"       },
    { "*[]",     '[',  "%p",   "void *"       },  // type[]                 (TBD)
    { "*",       'L',  "%p",   "void *"       },  // fully-qualified-class  (TBD)
    { "",        'V',  NULL,   NULL           },
  };

  enum SearchType {
    TYPE_NAME,
    JAVA_KEY,
    SPECIFIER,
  };


  /**
   * TBD: Add doc
   *
   */
  bool isMatchingTypesMapEntry ( TypeEntry * entry,
                                 const char * key, SearchType keyType )
  {
    bool matched = false;

    if ( entry == NULL ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Invalid Argument -- TypesEntry is NULL; exiting..."
             << endl;
      }
      exit( -1 );
    }

    if ( key == NULL || strlen(key) < 1 ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Invalid Argument -- key is NULL or empty;"
             << " return false..."
             << endl;
      }
      return false;
    }

    switch ( keyType ) {
      case TYPE_NAME :
        if ( isErrorEnabled() ) {
          cout << "ERROR: Unsupported Operation -- Matching type using"
               << " type name has not been implemented yet;"
               << " exiting..."
               << endl;
        }
        exit( -1 );
        return matched;
        break;
      case JAVA_KEY :
        matched = ( entry->javaKey == key[0] );
        if ( isDebugEnabled() ) {
          cout << "DEBUG: Java type key '" << key << "' "
               << (matched ? "matched" : "did NOT match" ) << " TypesMap entry"
               << " { type name: '" << entry->typeName  << "',"
               <<   " java key: '"  << entry->javaKey   << "',"
               <<   " specifier: '" << entry->specifier << "' }"
               << " -- returning " << (matched ? "true" : "false")
               << endl;
        }
        return matched;
        break;
      case SPECIFIER :
        if ( isErrorEnabled() ) {
          cout << "ERROR: Unsupported Operation -- Matching type using"
               << " conversion specifier has not been implemented yet;"
               << " exiting..."
               << endl;
        }
        exit( -1 );
        return matched;
        break;
    }

    if ( isErrorEnabled() ) {
      cout << "ERROR: Invalid Argument -- keyType is not one of the"
           << " supported types: 'TYPE_NAME' (" << TYPE_NAME << "),"
           << " 'JAVA_KEY' (" << JAVA_KEY << ") or"
           << " 'SPECIFIER' (" << SPECIFIER << "); exiting..."
           << endl;
    }
    exit( -1 );

  }  // end method -- isMatchingTypesMapEntry(TypeEntry *,const char *,SearchType)


  /**
   * TBD: Add doc
   *
   */
  TypeEntry * getTypesMapEntry ( const char * key, SearchType keyType ) {
    TypeEntry * entry;
    int N = sizeof( TypesMap ) / sizeof( TypeEntry );
    const char * specifier = NULL;

    if ( key == NULL || strlen(key) < 1 ) {
      entry = &( TypesMap[N-1] );
      if ( isWarnEnabled() ) {
        cout << "WARN: Type identifier key is NULL or empty"
             << " -- returning default type entry:"
             << " { type name: '" << entry->typeName  << "',"
             <<   " java key: '"  << entry->javaKey   << "',"
             <<   " specifier: '" << entry->specifier << "' }"
             << endl;
      }
      return entry;
    }

    for ( int i = 0 ; i < N ; i++ ) {
      entry = &( TypesMap[i] );
      if ( isMatchingTypesMapEntry(entry,key,keyType) ) {
        if ( isDebugEnabled() ) {
          cout << "DEBUG: Type key '" << key << "' matched TypesMap entry"
               << " { type name: '" << entry->typeName  << "',"
               <<   " java key: '"  << entry->javaKey   << "',"
               <<   " specifier: '" << entry->specifier << "' }"
               << " -- returning entry"
               << endl;
        }
        return entry;
      }
    }

    entry = &( TypesMap[N-1] );
    if ( isWarnEnabled() ) {
      cout << "WARN: Type key '" << key << "' did NOT matched"
           << " any TypesMap entries"
           << " -- returning default type entry:"
           << " { type name: '" << entry->typeName  << "',"
           <<   " java key: '"  << entry->javaKey   << "',"
           <<   " specifier: '" << entry->specifier << "' }"
           << endl;
    }
    return entry;

  }  // end method -- getTypesMapEntry(const char *,SearchType)


  /**
   * TBD: Add doc
   *
   */
  const char * getConversionSpecifier ( const char * key ) {
    TypeEntry * entry;

    if ( key == NULL || strlen(key) < 1 ) {
      if ( isWarnEnabled() ) {
        cout << "WARN: Java type key is NULL or empty"
             << " -- returning default conversion specifier: '%p'"
             << endl;
      }
      return "%p";
    }

    entry = getTypesMapEntry( key, JAVA_KEY );

    if ( entry->specifier != NULL ) {
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Type key '" << key << "' matched TypesMap entry"
             << " { type name: '" << entry->typeName  << "',"
             <<   " java key: '"  << entry->javaKey   << "',"
             <<   " specifier: '" << entry->specifier << "' }"
             << " -- returning specifier"
             << endl;
      }
      return entry->specifier;
    }

    if ( isWarnEnabled() ) {
      cout << "WARN: Type key '" << key << "' did NOT matched"
           << " any TypesMap entries"
           << " -- returning default conversion specifier: '%p'"
           << endl;
    }
    return "%p";

  }  // end method -- getConversionSpecifier(const char *)


  /*
  / **
   * TBD: Add doc
   *
   * /
  void ** getVarArg ( const char * typeKey, va_list args ) {
    void * arg = NULL;

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Getting arg of type '" << typeKey << "'"
           << " from var arg list "
           << endl;
    }

    switch ( typeKey[0] ) {
      //case 'Z' : arg = (void *)va_arg( args, bool        ); break;
      //case 'B' : arg = (void *)va_arg( args, signed char ); break;
      //case 'C' : arg = (void *)va_arg( args, char        ); break;
      //case 'S' : arg = (void *)va_arg( args, short int   ); break;
      case 'Z' : arg = (void *)va_arg( args, int         ); break;
      case 'B' : arg = (void *)va_arg( args, int         ); break; // Java byte
      case 'C' : arg = (void *)va_arg( args, int         ); break;
      case 'S' : arg = (void *)va_arg( args, int         ); break;
      case 'I' : arg = (void *)va_arg( args, int         ); break;
      case 'J' : arg = (void *)va_arg( args, long long   ); break; // Java long

      //case 'F' : arg = (void *)va_arg(args,float      ); break;
      case 'F' : arg = (void *)va_arg( args, double      ); break;
      case 'D' : arg = (void *)va_arg( args, double      ); break;

      case '[' : arg = (void *)va_arg( args, void *      ); break; // (TBD)
      case 'L' : arg = (void *)va_arg( args, void *      ); break; // (TBD)

      default  :
        if ( isErrorEnabled() ) {
          cout << "ERROR: Invalid type key '" << typeKey << "'"
               << " -- Supported Types: 'Z', 'B', 'C', 'S', 'I', 'J',"
               << " 'F', 'D', '[', 'L'; exiting..."
               << endl;
        }
        exit( -1 );
        break;
    }

    if ( isDebugEnabled ()) {
      cout << "DEBUG: Returning next '" << typeKey << "' type arg"
           << " from var arg list, value pointer: '" << arg << "'"
           << endl;
    }

    return &arg;

  }  // end method -- getVarArg(const char *,va_list)
  */


  /**
   * TBD: Add doc
   *
   */
  char ** toStringArray ( const char * types[], va_list args )
  {
    int           N        = length( types );
    const char *  typeKey  = NULL;
    const char *  spec     = NULL;
    char **       values   = NULL;
    char *        buf      = NULL;
    int           s        = -1;

    // TBD: Check if types is NULL or empty

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Converting var arg list to an array of strings"
           << " (size: " << N << ")"
           << " -- expected arg types: " << listStrings(types)
           << endl;
    }

    values = (char **)malloc( N * sizeof(char *) );

    for ( int i = 0 ; i < N ; i++ ) {
      typeKey = types[ i ];
      buf = values[ i ] = (char *)malloc( 100 * sizeof(char *) );

      // if ( isDebugEnabled() ) {
      //   cout << "DEBUG: Converting arg " << i
      //        << " of type '" << typeKey << "' from var arg list"
      //        << " to string and storing in buffer"
      //        << endl;
      // }

      spec = getConversionSpecifier( typeKey );

      switch ( typeKey[0] ) {
        //case 'Z' : s = sprintf( buf, spec, va_arg(args,bool       ) ); break;
        //case 'B' : s = sprintf( buf, spec, va_arg(args,signed char) ); break;
        //case 'C' : s = sprintf( buf, spec, va_arg(args,char       ) ); break;
        //case 'S' : s = sprintf( buf, spec, va_arg(args,short int  ) ); break;
        case 'Z' : s = sprintf( buf, spec, va_arg(args,int        ) ); break;
        case 'B' : s = sprintf( buf, spec, va_arg(args,int        ) ); break;
        case 'C' : s = sprintf( buf, spec, va_arg(args,int        ) ); break;
        case 'S' : s = sprintf( buf, spec, va_arg(args,int        ) ); break;
        case 'I' : s = sprintf( buf, spec, va_arg(args,int        ) ); break;
        case 'J' : s = sprintf( buf, spec, va_arg(args,long long  ) ); break;

        //case 'F' : s = sprintf( buf, spec, va_arg(args,float      ) ); break;
        case 'F' : s = sprintf( buf, spec, va_arg(args,double     ) ); break;
        case 'D' : s = sprintf( buf, spec, va_arg(args,double     ) ); break;

        case '[' : s = sprintf( buf, spec, va_arg(args,void *     ) ); break;
        case 'L' : s = sprintf( buf, spec, va_arg(args,void *     ) ); break;

        default  :
          if ( isErrorEnabled() ) {
            cout << "ERROR: Invalid type key '" << typeKey << "'"
                 << " -- Supported Types: 'Z', 'B', 'C', 'S', 'I', 'J',"
                 << " 'F', 'D', '[', 'L'; exiting..."
                 << endl;
          }
          exit( -1 );
          break;
      }

      if ( s < 0 ) {
        if ( isErrorEnabled() ) {
          cout << "ERROR: Unable to convert var arg " << i
               << " of type '" << typeKey << "' to string"
               << " using 'sprintf(buf,\"" << spec << "\",arg)'"
               << " -- Error code: " << s << ";"
               //<< " setting buffer to '" << errValue << "'"
               << " setting buffer to empty string ''"
               << endl;
        }
        //if ( errValue != NULL ) sprintf( buf, "%s", errValue );
        sprintf( buf, "%s", "" );
      }

      if ( isDebugEnabled() ) {
        cout << "DEBUG: Converted " << i << " arg from var arg list"
             << " -- type '" << typeKey << "', value: '" << buf << "'"
             << endl;
      }

    }

    return (char **)values;

  }  // end method -- toStringArray(const char *[],va_list)


  /**
   * TBD: Add doc
   *
   */
  string listArgs ( const char * types[], va_list args,
                    const char * pfx = "'",  const char * sfx = "'",
                    const char * sep = ", ", const char * unk = "?"  )
  {
    int      N       = length( types );
    char **  values  = NULL;
    string   list    = "";

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Listing variable argument list as a string"
           << " with types list: " << listStrings(types) // << ", va_list: " << args
           << endl;
    }

    values = toStringArray( types, args );
    list = listStrings( (const char **)values, pfx, sfx, sep, unk, true );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Listed variable argument list as string: " << list
           << endl;
    }

    return list;
  }  // end method -- listArgs(const char *,va_list, [...])


  /**
   * TBD: Add doc
   *
   */
  bool instanceOf ( JNIEnv * env, jobject jobj, string type ) {
    jclass cls = NULL;
    jboolean status = false;

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Checking if Java object '" << jobj << "'"
    //        << " is a reference to an instance of '" << type << "'"
    //        << endl;
    // }

    if ( jobj == NULL ) {
      if ( isWarnEnabled() ) {
        cout << "WARN: Object pointer arg is NULL -- returning false"
             << endl;
      }
      return false;
    }

    // TBD: Validate type and jobj

    cls = env->FindClass( type.c_str() );
    // TBD: Check that cls is valid
    // TBD: Add check env->ExceptionOccurred()
    

    status = env->IsInstanceOf( jobj, cls );
    if ( isDebugEnabled() ) {
      cout << "DEBUG: Checked if Java object '" << jobj << "'"
           << " is a reference to an instance of '" << type << "'"
           << " ... result: '" << (status ? "true" : "false") << "'"
           << endl;
    }

    return (bool)status;

  }  // end method -- instanceOf(JNIEnv *,jobject,string)

  /**
   * TBD: Add doc
   *
   */
  bool isConventionalCDModel ( JNIEnv * env, jobject jobj ) {
    return instanceOf( env, jobj, "isl/model/ref/ConventionalCDModel" );
  }

  /**
   * TBD: Add doc
   *
   */
  bool isExtendedCDModel ( JNIEnv * env, jobject jobj ) {
    return instanceOf( env, jobj, "isl/model/ref/ExtendedCDModel" );
  }


  /**
   * TBD: Add doc
   * Why do I need to write this method?  This is very basic ==> JNI
   * should be providing this.
   *
   */
  const char * getClsName ( JNIEnv * env, jclass cls ) {
    jclass         clsObj   = 0;
    jmethodID      mid      = 0;
    jstring        clsName  = 0;
    const char *   name      = 0;


    //clsObj = env->GetObjectClass( cls );
    clsObj = env->FindClass( "java/lang/Class" );
    // TBD: Check that clsObj is valid
    // TBD: Add check env->ExceptionOccurred()

    mid = env->GetMethodID( clsObj, "getName", "()Ljava/lang/String;" );
    if ( mid == 0 ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Unable to find method 'Class.getName()'"
             << " for class '" << cls << "'; exiting..."
             << endl;
      }
      exit( -1 );
    }
    // TBD: Add check env->ExceptionOccurred()

    env->ExceptionClear();

    clsName = (jstring)env->CallObjectMethod( cls, mid );
    if( env->ExceptionOccurred() ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Failed call to 'Class.getName()'"
             << " for class '" << cls << "'; exiting..."
             << endl;
        env->ExceptionDescribe();
      }
      exit( -1 );
    }

    // TBD: Check that clsName is valid
    name = (const char *)env->GetStringUTFChars( clsName, NULL );
    // TBD: Add check env->ExceptionOccurred()

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Class name for class '" << cls << "'"
           << ": '" << name << "' (string object: '" << clsName << "')"
          << endl;
    }

    return name;

  }  // end method -- getClsName(JNIEnv *,jclass)

  /**
   * TBD: Add doc
   *
   */
  const char * getClassName ( JNIEnv * env, jobject jobj ) {
    jclass cls = 0;

    // TBD: Check that env and jobj are valid
    cls = env->GetObjectClass( jobj );
    // TBD: Check that cls is valid
    // TBD: Add check env->ExceptionOccurred()

    return getClsName( env, cls );

  }  // end method -- getClassName(JNIEnv *,jobject)


  /**
   * TBD: Add doc
   *
   */
  jmethodID getConstructor ( JNIEnv * env, string clsName, const char * types[] )
  {
    jclass     cls      = NULL;
    jmethodID  method   = NULL;
    string     sig      = "()V";
    int        n        = 0;

    cls = env->FindClass( clsName.c_str() );
    // TBD: Check that cls is valid
    // TBD: Add check env->ExceptionOccurred()

    n = length( types );
    if ( n > 0 ) {
      for ( int i = 0 ; i < n ; i++ ) {
        // TBD: Check if types[i] is NULL or empty
        sig.insert( 1, types[i] );
      }
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Type signature for constructor: '" << sig << "'"
             << endl;
      }
    } else {
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Types list is NULL or empty"
             << " -- assuming no-arg constructor should be returned"
             << " (type signature: '" << sig << "'"
             << endl;
      }
    }

    method = env->GetMethodID( cls, "<init>", sig.c_str() );
    if ( method == 0 ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Unable to find constructor method '<init>'"
             << " for class '" << clsName << "'"
             << " and types list: " << listStrings(types) << "; exiting..."
             << endl;
      }
      exit( -1 );
    }
    // TBD: Add check env->ExceptionOccurred()

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Returning Java class constructor"
           << " method ID '" << method << "'"
           << " for class name '" << clsName << "'"
           << " and types list: " << listStrings(types)
           << endl;
    }

    return method;

  }  // end method -- getConstructor(JNIEnv *,string)


  // TBD: Cache constructor methods in hash table by class name for efficiency


  /**
   * TBD: Add doc
   *
   */
  // HERE HERE HERE
  //jobject newObject ( JNIEnv * env, string clsName, const char * types[], va_list args )
  //{
  jobject newObject ( JNIEnv * env, string clsName, const char * types[], ... )
  {
    jclass   cls   = NULL;
    jobject  jobj  = NULL;
    string   list  = "";
    va_list  args;

    va_start( args, types );
    list = listArgs( types, args );
    va_end( args );
    //list = "???";

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Creating a new instance of Java class"
    //        << " '" << clsName << "' with constructor args: " << list
    //        << endl;
    // }

    va_start( args, types );

    // TBD: Check clsName is valid

    cls = env->FindClass( clsName.c_str() );
    // TBD: Check that cls is valid
    // TBD: Add check env->ExceptionOccurred()

    env->ExceptionClear();

    jobj = env->NewObjectV( cls, getConstructor(env,clsName,types), args );
    if ( jobj == 0 ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Unable to create new instance of '" << clsName <<"'"
             << " with java class: '" << cls << "'"
             << " and constructor args: " << list << "; exiting..."
             << endl;
        env->ExceptionDescribe();
      }
      exit( -1 );
    }

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Created new instance of Java class '" << clsName << "'"
           << ": " << jobj 
           << "; constructor args: " << list
           << endl;
    }

    va_end( args );
    return jobj;

  }  // end method -- newObject(JNIEnv *,string,...)



  /**
   * TBD: Add doc
   *
   */
  string createGetterName ( const char * field, bool isBool = false ) {
    string prefix = ( isBool ? "is" : "get" );

    // if ( field == nil ) {
    //   // TBD:
    // }

    // if ( startsWith(field,"get") {
    //   // TBD:
    //   return field;
    // }

    // TBD: return prefix + capitalize(field);
    return prefix + field;
  }  // end method -- createGetterName(const char *,bool)

  /**
   * TBD: Add doc
   *
   */
  jmethodID getGetter ( JNIEnv * env, jobject jobj, string name, string type )
  {
    const char *  clsName  = getClassName( env, jobj );
    jclass        cls      = NULL;
    jmethodID     method   = NULL;

    cls = env->GetObjectClass( jobj );
    // TBD: Add check if cls is valid
    // TBD: Add check env->ExceptionOccurred()
    // TBD:
    //   * Add check to see if type already starts with "()"
    //   * Add support for specifying type with Java FQCN, eg "isl.util.Complex"
    //   * Add support for converting Java primitives to their signature,
    //     eg "double" ==> "D", "int" ==> "I", and support array types
    //   * Add check for ';' at end of class names
    type = "()" + type;
    method = env->GetMethodID( cls, name.c_str(), type.c_str() );
    if ( method == 0 ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Unable to find getter method '" << name << "'"
             << " for class '" << clsName << "'; exiting..."
             << endl;
      }
      exit( -1 );
    }
    // TBD: Add check env->ExceptionOccurred()

    return method;

  }  // end method -- getGetter(JNIEnv *,jobject,string,string)


  // TBD: Cache getter methods in hash table by field name for efficiency


  /**
   * TBD: Add doc
   *
   */
  bool getBoolean ( JNIEnv * env, jobject jobj, const char * field ) {
    const char *  clsName  = getClassName( env, jobj );
    string        name     = createGetterName( field, true );
    jmethodID     method   = NULL;
    jboolean      value    = 0.0;

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Getting a boolean value '" << field << "' from"
           << " Java object '" << jobj << "' (class: " << clsName << ")"
           << " using getter method '" << name << "'"
           << endl;
    }

    method = getGetter( env, jobj, name, "Z" );

    env->ExceptionClear();

    value = env->CallBooleanMethod( jobj, method );
    if( env->ExceptionOccurred() ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Failed call to getter method '" << name << "'"
             << " for class '" << clsName << "' and field '" << field << "'"
             << "; exiting..."
             << endl;
        env->ExceptionDescribe();
      }
      exit( -1 );
    }

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Returning boolean value '" << (value ? "true" : "false") << "'"
           << " for field '" << field << "' retrieved from"
           << " Java object '" << jobj << "' (class: " << clsName << ")"
           << " using getter method '" << name << "'"
          << endl;
    }

    //delete name;
    return value;

  }  // end method -- getBoolean(JNIEnv *,jobject,const char *)


  /**
   * TBD: Add doc
   *
   */
  double getDouble ( JNIEnv * env, jobject jobj, const char * field ) {
    const char *  clsName  = getClassName( env, jobj );
    string        name     = createGetterName( field, false );
    jmethodID     method   = NULL;
    jdouble       value    = 0.0;

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Getting a double value '" << field << "' from"
           << " Java object '" << jobj << "' (class: " << clsName << ")"
           << " using getter method '" << name << "'"
           << endl;
    }

    method = getGetter( env, jobj, name, "D" );

    env->ExceptionClear();

    value = env->CallDoubleMethod( jobj, method );
    if( env->ExceptionOccurred() ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Failed call to getter method '" << name << "'"
             << " for class '" << clsName << "' and field '" << field << "'"
             << "; exiting..."
             << endl;
        env->ExceptionDescribe();
      }
      exit( -1 );
    }

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Returning double value '" << value << "'"
           << " for field '" << field << "' retrieved from"
           << " Java object '" << jobj << "' (class: " << clsName << ")"
           << " using getter method '" << name << "'"
          << endl;
    }

    //delete name;
    return value;

  }  // end method -- getDouble(JNIEnv *,jobject,const char *)


  /**
   * TBD: Add doc
   *
   */
  complex<double> toCppComplex ( JNIEnv * env, jobject jcomplex,
                                 const char * field  )
  {
    double           real     = 0.0;
    double           imag     = 0.0;
    complex<double>  z;

    real = getDouble( env, jcomplex, "Real" );
    imag = getDouble( env, jcomplex, "Imag" );
    z = complex<double>( real, imag );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Java Complex number for field '" << field << "'"
           << ": '" << jcomplex << "'"
           << " ( = real: '" << real << "', imag: '" << imag << "' )"
           << " converted to a C++ complex number:  '" << z << "'"
           << endl;
    }

    return z;

  }  // end method -- toCppComplex(JNIEnv *,jobject,const char *)


  /**
   * TBD: Add doc
   *
   */
  jobject toJavaComplex ( JNIEnv * env, complex<double> ccomplex,
                          const char * field  )
  {
    const char * clsName = "isl/util/Complex";
    jdouble  real  = ccomplex.real();
    jdouble  imag  = ccomplex.imag();
    jobject  z     = NULL;
    const char * types [] = { "D", "D" }; // Future: { "double", "double" };

    if ( field == NULL ) field = "?";

    z = newObject( env, clsName, types, real, imag );
    if ( z == NULL ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Unable to create new instance of '" << clsName <<"'"
             << " for class '" << clsName << "'; exiting..."
             << endl;
      }
      exit( -1 );
    }

    if ( isDebugEnabled() ) {
      cout << "DEBUG: C++ complex number for field '" << field << "'"
           << ": '" << ccomplex << "'"
           << " ( = real: '" << real << "', imag: '" << imag << "' )"
           << " converted to a Java complex number:  '" << z << "'"
           << endl;
    }

    return z;

  }  // end method -- toJavaComplex(JNIEnv *,complex<double>,const char *)


  /**
   * TBD: Add doc
   *
   */
  complex<double> getComplex ( JNIEnv * env, jobject jobj,
                               const char * field )
  {
    const char *     clsName  = getClassName( env, jobj );
    string           name     = createGetterName( field, false );
    jmethodID        method   = NULL;
    jobject          value    = NULL;
    complex<double>  z;

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Getting a Complex value '" << field << "' from"
    //        << " Java object '" << jobj << "' (class: " << clsName << ")"
    //        << " using getter method '" << name << "'"
    //        << endl;
    // }

    method = getGetter( env, jobj, name, "Lisl/util/Complex;" );

    env->ExceptionClear();

    value = env->CallObjectMethod( jobj, method );
    if( env->ExceptionOccurred() ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: Failed call to getter method '" << name << "'"
             << " for class '" << clsName << "'; exiting..."
             << endl;
        env->ExceptionDescribe();
      }
      exit( -1 );
    }

    z = toCppComplex( env, value, field );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Returning C++ complex<double> number '" << z << "'"
           << " ( = real: '" << real(z) << "', imag: '" << imag(z) << "' )"
           << " converted from value '" << value << "'"
           << " for field '" << field << "' retrieved from"
           << " Java object '" << jobj << "' (class: " << clsName << ")"
           << " using getter method '" << name << "'"
          << endl;
    }


    //delete name;
    return z;

  }  // end method -- getComplex(JNIEnv *,jobject,const char *)


  // === [ Convection Dispersion Model Utility Methods ] ==================

  /**
   * TBD: Add doc
   *
   */
  string getCppClassName ( Model * objPtr ) {
    string name = "?";
    int    n = 0;

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Getting C++ class name for object pointer"
    //        << " '" << objPtr << "'"
    //        << endl;
    // }

    if ( objPtr == NULL ) {
      if ( isWarnEnabled() ) {
        cout << "WARN: Object pointer arg is NULL -- returning empty string ''"
             << endl;
      }
      return "";
    }

    name = typeid( *objPtr ).name();
    if ( isDebugEnabled() ) {
      cout << "DEBUG: typeid name for object pointer"
           << " '" << objPtr << "' is '" << name << "'"
           << endl;
    }

    // Class names returned by the 'typeid' function are prefixed
    // with the name length with my version of C++:
    // $ g++ --version  ==>  g++ (Ubuntu/Linaro 4.4.4-14ubuntu5) 4.4.5

    if ( sscanf(name.c_str(),"%d",&n) == 1 ) {
      char buf [ n+2 ];
      if ( sscanf(name.c_str(),"%*d%s",buf) == 1 ) {
        if ( isDebugEnabled() ) {
          cout << "DEBUG: Parsed length '" << n << "' and class name"
               << " '" << buf << "' from typeid name string '" << name << "'"
               << " for object pointer" << " '" << objPtr << "';"
               << " returning parsed class name..."
               << endl;
        }
        name = buf;
      } else {
        if ( isErrorEnabled() ) {
          cout << "ERROR: Failed to parse class name of length " << n
               << " from typeid name string '" << name << "'"
               << " for object pointer" << " '" << objPtr << "'; exiting..."
               << endl;
        }
        exit( -1 );
      }
    } else {
      if ( isWarnEnabled() ) {
        cout << "WARN: No length prefix found on the typeid name string"
             << " '" << name << "' for object pointer" << " '" << objPtr << "'"
             << " -- ignoring error and returning '" << name << "'"
             << " as the C++ class name."
             << endl;
      }
    }

    return name;

  }  // end method -- getCppClassName(Model *)

  /**
   * TBD: Add doc
   *
   */
  bool isInstanceOf ( Model * objPtr, string type ) {
    string name = "?";

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Checking if object pointer '" << objPtr << "'"
           << " references an instance of '" << type << "'"
           << endl;
    }

    if ( objPtr == NULL ) {
      if ( isWarnEnabled() ) {
        cout << "WARN: Object pointer arg is NULL -- returning false"
             << endl;
      }
      return false;
    }

    name = getCppClassName( objPtr );
    if ( isDebugEnabled() ) {
      cout << "DEBUG: Object pointer '" << objPtr << "' references"
           << " an instance of '" << name << "'"
           << " -- returning '" << (type == name ? "true" : "false") << "'"
           << endl;
    }

    return ( type == name );

  }  // end method -- isInstanceOf(Model *, string)

  /**
   * TBD: Add doc
   *
   */
  bool isConvectionDispersion ( Model * cppCDM ) {
    return isInstanceOf( cppCDM, "ConvectionDispersion" );
  }

  /**
   * TBD: Add doc
   *
   */
  bool isExtendedConvectionDispersion ( Model * cppCDM ) {
    return isInstanceOf( cppCDM, "ExtendedConvectionDispersion" );
  }


  // HACK HACK HACK
  // Instead of creating my own initialization method perhaps I should
  // hack the liver_model code ?

  /**
   * Set/Reset Convection Dispersion (CD) parameters to default values.
   *
   */
  void setCDModelDefaults ( Model * cppCDM ) {

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Setting C++ CD Model parameters with default values"
           << " found in 'liver_model.cc' ..."
           << endl;
    }

    // General CD parameters
    cppCDM->setK1( 0.03  );  // ecd
    cppCDM->setK2( 0.01  );  // ecd
    cppCDM->setKE( 0.10  );  // ecd
    cppCDM->setD(  0.265 );  // ecd  D_N: .265
    cppCDM->setT(  6.35  );  // ecd T: 6.35 sec
    cppCDM->setM(  1.0   );  // ecd bolus mass
    cppCDM->setQ(  0.312 );  // ecd perfusate flow  30ml/min = .5ml/sec

    cppCDM->setExtracting( false );

    if ( isConvectionDispersion(cppCDM) ) {
      // Traditional CD parameters
      // L := length -- chosen to visually fit Figure 4A for the traditional model
      // v := axial perfusate velocity
      ((ConvectionDispersion *)cppCDM)->setL( 7 );
      ((ConvectionDispersion *)cppCDM)->setV( 1 );
    } else {
      // Extended CD parameters
      // a := ecd f/V1 (f is flow between 1st and 2nd vascular compartments;
      //                V1 & V2 are compartments volumes)
      // b := ecd f/V2 
      ((ExtendedConvectionDispersion *)cppCDM)->setA( 0.00654 );
      ((ExtendedConvectionDispersion *)cppCDM)->setB( 0.0248 );
    }

    // Other parameters
    //    double p = 0.858;
    //    double D2 = 3.77;
    //    double T2 = 35.4;
    // 

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: C++ CD Model parameters set to the default values"
    //        << " found in 'liver_model.cc'."
    //        << endl;
    // }

  }  // end method -- setCDModelDefaults(Model *)

  /**
   * Set Convection Dispersion (CD) parameters to the same value as
   * the provided Java CD object.
   *
   */
  void setCDModel ( Model * cppCDM, JNIEnv * env, jobject javaCDM ) {

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Setting C++ CD Model parameters with values"
           << " from Java object '" << javaCDM << "'"
           << " (class: " << getClassName(env,javaCDM) << ") ..."
           << endl;
    }

    // General CD parameters
    cppCDM->setK1( getDouble(env,javaCDM,"K1")  );  // ecd
    cppCDM->setK2( getDouble(env,javaCDM,"K2")  );  // ecd
    cppCDM->setKE( getDouble(env,javaCDM,"Ke")  );  // ecd
    cppCDM->setD(  getDouble(env,javaCDM,"Dn")  );  // ecd D_N: .265
    cppCDM->setT(  getDouble(env,javaCDM,"T")   );  // ecd T: 6.35 sec
    cppCDM->setM(  getDouble(env,javaCDM,"M")   );  // ecd bolus mass
    cppCDM->setQ(  getDouble(env,javaCDM,"Q")   );  // ecd perfusate flow
                                                  //     30ml/min = .5ml/sec

    cppCDM->setExtracting( getBoolean(env,javaCDM,"Extracting")  );

    if ( isConvectionDispersion(cppCDM) ) {
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Conventional CD Model"
             << " ==> setting 'L' and 'v' parameters with values"
             << " from Java object '" << javaCDM << "'"
             << " (class: " << getClassName(env,javaCDM) << ") ..."
             << endl;
      }
      // Traditional CD parameters
      // L := length -- chosen to visually fit Figure 4A for the traditional model
      // v := axial perfusate velocity
      ((ConvectionDispersion *)cppCDM)->setL( getDouble(env,javaCDM,"L") );
      ((ConvectionDispersion *)cppCDM)->setV( getDouble(env,javaCDM,"V") );
    } else {
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Extended CD Model"
             << " ==> setting 'a' and 'b' parameters with values"
             << " from Java object '" << javaCDM << "'"
             << " (class: " << getClassName(env,javaCDM) << ") ..."
             << endl;
      }
      // Extended CD parameters
      // a := ecd f/V1 (f is flow between 1st and 2nd vascular compartments;
      //                V1 & V2 are compartments volumes)
      // b := ecd f/V2 
      ((ExtendedConvectionDispersion *)cppCDM)->setA( getComplex(env,javaCDM,"A") );
      ((ExtendedConvectionDispersion *)cppCDM)->setB( getComplex(env,javaCDM,"B") );
    }

    // Other parameters
    //    double p = 0.858;
    //    double D2 = 3.77;
    //    double T2 = 35.4;
    // 

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: C++ CD Model parameters set with values"
    //        << " from Java object '" << javaCDM << "'"
    //        << " (class: " << getClassName(env,javaCDM) << ") ..."
    //        << endl;
    // }

  }  // end method -- setCDModel(Model *,JNIEnv *,jobject)


  // === [ JNI Methods ] ==================================================

  /*
   * Class:     isl_model_ref_AbstractCDModelTest
   * Method:    setCppLogLevel
   * Signature: (Ljava/lang/String;)V
   */
  JNIEXPORT void JNICALL
  Java_isl_model_ref_AbstractCDModelTest_setCppLogLevel
  ( JNIEnv * env, jobject caller, jstring level )
  {
    const char * name = NULL;

    if ( level != NULL ) {
      name = (const char *)env->GetStringUTFChars( level, NULL );
      // TBD: Add check env->ExceptionOccurred()
    }
    setLogLevel( name );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Log level set to '" << name << "' (" << LogLevel << ")"
           << " -- jstring arg: '" << level << "'"
           << endl;
    }

  }  // end method -- Java_isl_model_ref_AbstractCDModelTest_setCppLogLevel

  /*
   * Class:     isl_model_ref_AbstractCDModelTest
   * Method:    createCppCDM
   * Signature: (Lisl/model/ref/AbstractCDModel;)J
   */
  JNIEXPORT jlong JNICALL
  Java_isl_model_ref_AbstractCDModelTest_createCppCDM
  ( JNIEnv * env, jobject caller, jobject javaCDM )
  {
    Model * cppCDM = NULL;

    if ( javaCDM == NULL ) {
      if ( isErrorEnabled() ) {
        cout << "ERROR: UNable to initialize C++ Convection Dispersion Model"
             << " -- handle to Java CDM is NULL; exiting..."
             << endl;
      }
      exit( -1 );
    }

    if ( isConventionalCDModel(env,javaCDM) ) {
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Creating new C++ Conventional CD Model"
             << " from Java object '" << javaCDM << "'"
             << " (class: " << getClassName(env,javaCDM) << ") ..."
             << endl;
      }
      cppCDM = new ConvectionDispersion();
    } else {
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Creating new C++ Extended CD Model"
             << " from Java object '" << javaCDM << "'"
             << " (class: " << getClassName(env,javaCDM) << ") ..."
             << endl;
      }
      cppCDM = new ExtendedConvectionDispersion();
    }

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Initializing C++ new CD Model with Java CD Model"
    //        << "'" << javaCDM << "'"
    //        << " (class: " << getClassName(env,javaCDM) << ") ..."
    //        << endl;
    // }

    setCDModel( cppCDM, env, javaCDM );

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Created new C++ " << getCppClassName(cppCDM)
    //        << " object from Java object '" << javaCDM << "'"
    //        << " (class: " << getClassName(env,javaCDM) << ")"
    //        << " -- returning pointer as long: " << ((jlong)cppCDM)
    //        << endl;
    // }

    return (jlong)cppCDM;

  }  // end method -- Java_isl_model_ref_AbstractCDModelTest_createCppCDM


  /*
   * Class:     isl_model_ref_AbstractCDModelTest
   * Method:    deleteCppCDM
   * Signature: (J)V
   */
  JNIEXPORT void JNICALL
  Java_isl_model_ref_AbstractCDModelTest_deleteCppCDM
  ( JNIEnv * env, jobject caller, jlong ptr )
  {
    Model * cppCDM = (Model *)ptr;

    // TBD: check is ptr is valid

    if ( isConvectionDispersion(cppCDM) ) {
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Deleting C++ Conventional CD Model object: " << cppCDM
             << endl;
      }
      delete (ConvectionDispersion *)cppCDM;
    } else {
      if ( isDebugEnabled() ) {
        cout << "DEBUG: Deleting C++ Extended CD Model object: " << cppCDM
             << endl;
      }
      delete (ExtendedConvectionDispersion *)cppCDM;
    }

    //delete cppCDM;

  }  // end method -- Java_isl_model_ref_AbstractCDModelTest_deleteCppCDM

  /*
   * Class:     isl_model_ref_AbstractCDModelTest
   * Method:    ecd
   * Signature: (JDD)D
   */
  JNIEXPORT jdouble JNICALL
  Java_isl_model_ref_AbstractCDModelTest_ecd
  (JNIEnv * env, jobject caller, jlong ptr, jdouble z, jdouble t )
  {
    Model * cppCDM = (Model *)ptr;
    double value = 0.0;

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Calling C++ CD Model function ecd(z,t) "
    //        << " with z = " << z << " and t = " << t << " ..."
    //        << endl;
    // }

    value = cppCDM->ecd( z, t );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: C++ CD Model function ecd(z,t) called"
           << " with z = " << z << " and t = " << t
           << " -- returned value '" << value << "'"
           << endl;
    }

    return value;

  }  // end method -- Java_isl_model_ref_AbstractCDModelTest_ecd


  /*
   * Class:     isl_model_ref_AbstractCDModelTest
   * Method:    integrand
   * Signature: (JDDD)D
   */
  JNIEXPORT jdouble JNICALL
  Java_isl_model_ref_AbstractCDModelTest_integrand
  ( JNIEnv * env, jobject caller, jlong ptr, jdouble z, jdouble u, jdouble t )
  {
    ConvectionDispersion * cppCDM = (ConvectionDispersion *)ptr;
    double value = 0.0;

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Calling C++ Conventional CD Model function"
    //        << " integrand(z,u,t) with z = " << z << ", u = " << u
    //        << " and t = " << t << " ..."
    //        << endl;
    // }

    value = cppCDM->integrand( z, u, t );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: C++ Conventional CD Model function integrand(z,u,t) "
           << " called with z = " << z << ", u = " << u << " and t = " << t
           << " -- returned value '" << value << "'"
           << endl;
    }

    return value;

  }  // end method -- Java_isl_model_ref_AbstractCDModelTest_integrand


  /*
   * Class:     isl_model_ref_AbstractCDModelTest
   * Method:    bessel
   * Signature: (Ljava/lang/String;D)D
   */
  JNIEXPORT jdouble JNICALL
  Java_isl_model_ref_AbstractCDModelTest_bessel
  ( JNIEnv * env, jobject caller, jstring func, jdouble x )
  {
    const char * name = NULL;
    BesselFunction bessel = &j0;
    double result = 0.0;

    if ( func != NULL ) {
      name = (const char *)env->GetStringUTFChars( func, NULL );
      // TBD: Add check env->ExceptionOccurred()
    } else {
      name = "j0";
      if ( isDebugEnabled() ) {
        cout << "DEBUG: No function name specified"
             << " -- using default function '" << name << "'"
             << endl;
      }
    }

    bessel = getBesselFunction( name );
    result = bessel( x );
    if ( isDebugEnabled() ) {
      cout << "DEBUG: Called bessel function "
           << name << "( " << x << " ) = " << result
           << endl;
    }

    return result;

  }  // end method -- Java_isl_model_ref_AbstractCDModelTest_bessel


  /*
   * Class:     isl_model_ref_AbstractCDModelTest
   * Method:    complexMath
   * Signature: (Ljava/lang/String;Lisl/util/Complex;)Lisl/util/Complex;
   */
  JNIEXPORT jobject JNICALL
  Java_isl_model_ref_AbstractCDModelTest_complexMath
  ( JNIEnv * env, jobject caller, jstring func, jobject z )
  {
    const char * name = NULL;
    ComplexMathFunction cmf    = &complexSqrt;
    complex<double> cppZ       = 0;
    complex<double> cppResult  = 0;
    jobject         result     = NULL;

    if ( func != NULL ) {
      name = (const char *)env->GetStringUTFChars( func, NULL );
      // TBD: Add check env->ExceptionOccurred()
    } else {
      name = "sqrt";
      if ( isDebugEnabled() ) {
        cout << "DEBUG: No function name specified"
             << " -- using default function '" << name << "'"
             << endl;
      }
    }

    cmf = getComplexMathFunction( name );
    cppZ = toCppComplex(env,z,"z");
    cppResult = cmf( cppZ );
    result = toJavaComplex( env, cppResult, "complex math return value" );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: Called complex math function "
           << name << "( " << cppZ << " ) = " << cppResult
           << " = '" << result << "' (Java object)"
           << endl;
    }

    return result;

  }  // end method -- Java_isl_model_ref_AbstractCDModelTest_complexMath


  /*
   * Class:     isl_model_ref_AbstractCDModelTest
   * Method:    l_ecd
   * Signature: (JLisl/util/Complex;)Lisl/util/Complex;
   */
  JNIEXPORT jobject JNICALL
  Java_isl_model_ref_AbstractCDModelTest_l_1ecd
  ( JNIEnv * env, jobject caller, jlong ptr, jobject s )
  {
    ExtendedConvectionDispersion * cppCDM = NULL;
    complex<double>  cppS = toCppComplex(env,s,"s");
    complex<double>  Cout;
    jobject          jCout = NULL;

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Calling C++ Extended CD Model function l_ecd(s) "
    //        << " with s = " << s << " (Java object)"
    //        << " = " << cppS << " (C++ complex<double>) ..."
    //        << endl;
    // }

    cppCDM = (ExtendedConvectionDispersion *)ptr;
    Cout = cppCDM->l_ecd( cppS );
    // jCout = newObject( env, "isl/util/Complex", real(Cout), imag(Cout) );
    jCout = toJavaComplex( env, Cout, "Cout (l_ecd return value)" );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: C++ Extended CD Model function l_ecd(s) called"
           << " with s = " << s << " (Java object)"
           << " = " << cppS << " (C++ complex<double>)"
           << " -- return value '" << Cout << "' (C++ complex<double>)"
           <<   " = '" << jCout << "' (Java object)"
           << endl;
    }

    return jCout;

  }  // end method -- Java_isl_model_ref_AbstractCDModelTest_l_1ecd


  /*
   * Class:     isl_model_ref_AbstractCDModelTest
   * Method:    g
   * Signature: (JLisl/util/Complex;)Lisl/util/Complex;
   */
  JNIEXPORT jobject JNICALL
  Java_isl_model_ref_AbstractCDModelTest_g
  ( JNIEnv * env, jobject caller, jlong ptr, jobject s )
  {
    ExtendedConvectionDispersion * cppCDM = NULL;
    complex<double>  cppS = toCppComplex(env,s,"s");
    complex<double>  gs;
    jobject          jGs = NULL;

    // if ( isDebugEnabled() ) {
    //   cout << "DEBUG: Calling C++ Extended CD Model function g(s) "
    //        << " with s = " << s << " (Java object)"
    //        << " = " << cppS << " (C++ complex<double>) ..."
    //        << endl;
    // }

    cppCDM = (ExtendedConvectionDispersion *)ptr;
    gs = cppCDM->g( cppS );
    //jGs = newObject( env, "isl/util/Complex", real(gs), imag(gs) );
    jGs = toJavaComplex( env, gs, "g(s) return value" );

    if ( isDebugEnabled() ) {
      cout << "DEBUG: C++ Extended CD Model function g(s) called"
           << " with s = " << s << " (Java object)"
           << " = " << cppS << " (C++ complex<double>)"
           << " -- return value '" << gs << "' (C++ complex<double>)"
           <<   " = '" << jGs << "' (Java object)"
           << endl;
    }

    return jGs;

  }  // end method -- Java_isl_model_ref_AbstractCDModelTest_g

}  // extern "C"


// TO DO 6/28/2011
// -----
//    * Add toJavaKey function
//    * Call toJavaKey in getConstructor
//    * Add function to call for unsupported operation errors
//    * Add support for wild card comparison ?
//    * Add function to convert '.' to '/' in class names
//         before calling FindClass and etc
//    * Add 'toString( JNIEnv * env, jobject jobj )' method
//         that invokes the jobject's toString method
//

