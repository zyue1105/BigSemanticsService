package ecologylab.bigsemantics.service.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ecologylab.logging.AbstractLogger;
import ecologylab.logging.LogLevel;

public class Log4jLogger extends AbstractLogger
{

  static Map<LogLevel, Level> transLevel;

  static
  {
    transLevel = new HashMap<LogLevel, Level>();
    transLevel.put(LogLevel.DEBUG, Level.DEBUG);
    transLevel.put(LogLevel.INFO, Level.INFO);
    transLevel.put(LogLevel.WARNING, Level.WARN);
    transLevel.put(LogLevel.ERROR, Level.ERROR);
    transLevel.put(LogLevel.FATAL, Level.FATAL);
  }

  Logger                      logger;

  Log4jLogger(Logger logger)
  {
    this.logger = logger;
  }

  @Override
  public void log(LogLevel level, String fmt, Object... args)
  {
    logger.log(transLevel.get(level), String.format(fmt, args));
  }

}
