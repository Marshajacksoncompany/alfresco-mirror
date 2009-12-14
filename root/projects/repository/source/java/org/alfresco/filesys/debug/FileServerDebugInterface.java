package org.alfresco.filesys.debug;

/*
 * FileServerDebugInterface.java
 *
 * Copyright (c) 2007 Starlasoft. All rights reserved.
 */

import org.springframework.extensions.config.ConfigElement;
import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.debug.DebugInterfaceBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Alfresco File Server Debug Interface Class
 * 
 * @author gkspencer
 */
public class FileServerDebugInterface extends DebugInterfaceBase {

  // Logger to use for all file server debug output
  
  private static final Log logger = LogFactory.getLog("org.alfresco.fileserver");
  
  // temporary buffer for debugPrint
  
  private StringBuilder m_printBuf;

  /**
   * Class constructor
   */
  public FileServerDebugInterface() {
    m_printBuf = new StringBuilder(120);
  }
  
  /**
   * Output a debug string.
   *
   * @param str String
   */
  public void debugPrint(String str, int level) {
    if ( level <= getLogLevel())
      m_printBuf.append( str);
  }

  /**
   * Output a debug string, and a newline.
   *
   * @param str String
   */
  public void debugPrintln(String str, int level) {
    if ( level <= getLogLevel()) {
      
      // Check if there is any buffered output
      
      if ( m_printBuf.length() > 0) {
        m_printBuf.append( str);
        logOutput( m_printBuf.toString(), level);
        m_printBuf.setLength( 0);
      }
      else
        logOutput( str, level);
    }
  }

  /**
   * Output to the logger at the appropriate log level
   * 
   * @param str String
   * @param level int
   */
  protected void logOutput(String str, int level) {
	  switch ( level) {
		case Debug.Debug:
		  logger.debug( str);
		  break;
		case Debug.Info:
		  logger.info( str);
		  break;
		case Debug.Warn:
		  logger.warn( str);
		  break;
		case Debug.Fatal:
		  logger.fatal( str);
		  break;
		case Debug.Error:
		  logger.error( str);
		  break;
	  }
  }
  
  /**
   * Initialize the debug interface using the specified named parameters.
   *
   * @param params ConfigElement
   * @exception Exception
   */
  public void initialize(ConfigElement params)
    throws Exception {

    // Set the log level from the log4j Log object
	  
	int logLevel = Debug.Error;
	
	if ( logger.isDebugEnabled())
	  logLevel = Debug.Debug;
	else if ( logger.isInfoEnabled())
	  logLevel = Debug.Info;
	else if ( logger.isWarnEnabled())
	  logLevel = Debug.Warn;
	else if ( logger.isErrorEnabled())
	  logLevel = Debug.Error;
	else if ( logger.isFatalEnabled())
	  logLevel = Debug.Fatal;
	
	setLogLevel(logLevel);
  }
}
