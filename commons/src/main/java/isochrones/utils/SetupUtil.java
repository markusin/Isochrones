/**
 * 
 */
package isochrones.utils;

import isochrones.algorithm.Dataset;
import isochrones.algorithm.Direction;
import isochrones.algorithm.Mode;
import isochrones.db.DBVendor;

import java.io.FileNotFoundException;

/**
 * <p>
 * The <code>SetupDatabase</code> class
 * </p>
 * <p>
 * Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a>
 * </p>
 * <p>
 * Domenikanerplatz - Bozen, Italy.
 * </p>
 * <p>
 * </p>
 * 
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class SetupUtil {

  protected DBVendor dbvendor = DBVendor.ORACLE; // default oracle DB
  protected Dataset dataSet = Dataset.BZ; // default dataset is bozen
  protected String configDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "etc"
      + System.getProperty("file.separator");
  protected Mode mode = Mode.UNIMODAL;
  protected Direction direction = Direction.INCOMMING; 
  protected boolean checkIndex = false;
  protected boolean useDensity = false;
  protected boolean debug = false;
  protected int k = 0;
  protected int maxMemorySize = Integer.MAX_VALUE;

  Config config;
  private short densityLimit = Short.MIN_VALUE;

  public SetupUtil(String[] args, char sep) {
    if (args.length == 0) {
      System.out.println("No arguments set. Therefore using default values.");
    }
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      if (arg.startsWith("dbType") && isSet(value)) {
        dbvendor = DBVendor.valueOf(value.toUpperCase());
      } else if (arg.startsWith("dataset") && isSet(value)) {
        dataSet = Dataset.valueOf(value.toUpperCase());
      } else if (arg.startsWith("configDir") && isSet(value)) {
        configDir = value;
        if (!configDir.endsWith(System.getProperty("file.separator"))) {
          configDir.concat(System.getProperty("file.separator"));
        }
      } else if (arg.startsWith("mode") && isSet(value)) {
        mode = Mode.valueOf(value.toUpperCase());
      } else if (arg.startsWith("checkIndexes") && isSet(value)) {
        checkIndex = Boolean.valueOf(value);
      } else if (arg.startsWith("useDensity") && isSet(value)) {
          useDensity = Boolean.valueOf(value);
      } else if (arg.startsWith("densityLimit") && isSet(value)) {
          densityLimit = Short.parseShort(value);
      } else if (arg.startsWith("debug") && isSet(value)) {
        debug = Boolean.valueOf(value);
      } else if (arg.startsWith("direction") && isSet(value)) {
        direction = Direction.valueOf(value.toUpperCase());
      } else if (arg.startsWith("k") && isSet(value)) {
        k = Integer.valueOf(value);
      } else if (arg.startsWith("maxMemorySize") && isSet(value)) {
        maxMemorySize = Integer.valueOf(value);
      }
    }
    try {
      config = new Config(dbvendor, dataSet, configDir);
      config.setMode(mode);
      config.setDirection(direction);
      config.setDebug(debug);
      config.setUseDensity(useDensity);
      if (densityLimit != Short.MIN_VALUE) {
        config.setDensityLimit(densityLimit);
      }
      config.setK(k);
      config.setMaxMemorySize(maxMemorySize);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    // fillProperties(configDir);
  }

  /**
   * <p>
   * Method getDatabase
   * </p>
   * returns the database vendor
   * 
   * @return
   */
  public DBVendor getDBVendor() {
    return dbvendor;
  }

  /**
   * <p>
   * Method getDataset
   * </p>
   * returns the data set
   * 
   * @return
   */
  public Dataset getDataset() {
    return dataSet;
  }

  /**
   * <p>
   * Method isIndexCheck
   * </p>
   * 
   * @return
   */
  public boolean isIndexCheck() {
    return checkIndex;
  }

  /**
   * <p>
   * Method getMode
   * </p>
   * return the transportation mode
   * 
   * @return
   */
  public Mode getMode() {
    return mode;
  }
  
  public Direction getDirection() {
    return direction;
  }

  /*
   * protected void fillProperties(String configDir) { String cfgFile = configDir.concat("config_bz.xml"); if (dSet ==
   * Dataset.SF) { cfgFile = configDir.concat("config_sf.xml"); } else if (dSet == Dataset.GRID) { cfgFile =
   * configDir.concat("config_grid.xml"); } else if (dSet == Dataset.SPIDER) { cfgFile =
   * configDir.concat("config_spider.xml"); } else if (dSet == Dataset.ST) { cfgFile =
   * configDir.concat("config_st.xml"); } else if (dSet == Dataset.WDC) { cfgFile = configDir.concat("config_wdc.xml");
   * } else if (dSet == Dataset.IT) { cfgFile = configDir.concat("config_it.xml"); } File file = new File(cfgFile);
   * Config.replacePropertyFile(file.toURI()); }
   */

  public Config getConfig() {
    return config;
  }

  /**
   * <p>
   * Method getInputParameters
   * </p>
   * displays the specified parameters
   * 
   * @return
   */
  protected StringBuffer getInputParameters() {
    StringBuffer buf = new StringBuffer();
    buf.append("###### Setup parameters:######").append("\n");
    buf.append("Database: " + dbvendor).append("\n");
    buf.append("Dataset: " + dataSet).append("\n");
    buf.append("Debugging mode: " + debug).append("\n");
    if (useDensity) {
      buf.append("Density approach: " + true).append("\n");
    }
    if (densityLimit != Short.MIN_VALUE) {
      buf.append("Density threshold (vertex per distance):" + densityLimit).append("\n");
    }
    buf.append("Transportation mode: ").append(mode).append("\n");
    return buf;
  }

  protected String getConfigDir() {
    return configDir;
  }
  
  protected boolean isSet(String value){
    if(value.startsWith("$") || value.equalsIgnoreCase("setme") || value.equals("")) return false;
    return true;
  }

}
