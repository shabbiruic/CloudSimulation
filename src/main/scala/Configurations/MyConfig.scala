package Configurations

import java.util

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}

object Myconfig {

  val logger:Logger = LoggerFactory.getLogger(Myconfig.getClass)

  val config = ConfigFactory.load()

    def getString(key: String): String = {
      try {
        logger.info("fetching String " + key +" ...." )
        config.getString(key)

      }
      catch {
        case w: ConfigException.WrongType => logger.error("\""+key + "\" key contains invalid string");throw new ConfigKeyException()
        case e: ConfigException.Missing => logger.error("\""+key + "\" key is not present in Config");throw new ConfigKeyException()
      }
    }
    def getDouble(key: String): Double= {
      try {
        logger.info("fetching Double " + key +" ...." )
        config.getDouble(key)
      }
      catch {
        case w: ConfigException.WrongType => logger.error("\""+key + "\" key not contains the Number");throw new ConfigKeyException()
        case m: ConfigException.Missing => logger.error("\""+key + "\" key is not present in Config");throw new ConfigKeyException()
      }
    }

  def getObject(key: String): Config ={

    try {
      logger.info("fetching Object " + key +" ...." )
      config.getObject(key).toConfig
    }
    catch {
      case w: ConfigException.WrongType => logger.error("\""+key + "\" key is not a Object type");throw new ConfigKeyException()
      case m: ConfigException.Missing => logger.error("\""+key + "\" key is not present in Config");throw new ConfigKeyException()
    }
  }

  def getStringList(key: String): util.List[String] = {
    try {
      logger.info("fetching String List" + key +" ...." )
      config.getStringList(key)
    }
    catch {
      case w: ConfigException.WrongType => logger.error("\"" + key + "\" key is not a List of String type"); throw new ConfigKeyException()
      case m: ConfigException.Missing => logger.error("\"" + key + "\" key is not present in Config"); throw new ConfigKeyException()
    }
  }
  }
