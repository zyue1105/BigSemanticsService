package ecologylab.semantic.service.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ecologylab.logging.BasicLoggerNames;
import ecologylab.logging.ILogger;
import ecologylab.logging.ILoggerFactory;

public class Log4jLoggerFactory implements ILoggerFactory
{

  private static String configurationFile = "/log4j.configuration";

  static
  {
    PropertyConfigurator.configure(BasicLoggerNames.class.getResourceAsStream(configurationFile));
  }

  @Override
  public ILogger getLogger(String name)
  {
    Logger logger = Logger.getLogger(name);
    return new Log4jLogger(logger);
  }

  @Override
  public ILogger getLogger(Class clazz)
  {
    return getLogger(clazz.getName());
  }

}
