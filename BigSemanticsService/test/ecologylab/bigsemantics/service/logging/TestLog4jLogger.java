package ecologylab.bigsemantics.service.logging;

import org.apache.log4j.Logger;
import org.junit.Test;

import ecologylab.logging.LogLevel;

/**
 * 
 * @author quyin
 *
 */
public class TestLog4jLogger
{
  
  /**
   * This test makes sure that when an unescaped format string is used for the log() method,
   * it won't throw any exceptions.
   */
  @Test
  public void testUnescapedFormatStrings()
  {
    Logger _logger = Logger.getLogger("test");
    Log4jLogger logger = new Log4jLogger(_logger);
    logger.log(LogLevel.DEBUG, "%C");
  }

}
