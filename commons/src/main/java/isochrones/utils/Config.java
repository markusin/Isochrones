package isochrones.utils;

import isochrones.algorithm.Dataset;
import isochrones.algorithm.Direction;
import isochrones.algorithm.Mode;
import isochrones.algorithm.TableEntry;
import isochrones.algorithm.TableType;
import isochrones.db.ConnectionFactory;
import isochrones.db.DBVendor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * <p>
 * The <code>Config</code> class
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
public class Config {

  /**
   * the persistent set of properties
   */
  private Properties properties;
  protected static final Logger LOGGER = Logger.getLogger(Config.class.getPackage().getName());

  private static ConnectionFactory factory;
  private Connection connection;

  private String user, passwd, serverName, database, schema;
  private int port;

  DBVendor dbVendor = DBVendor.POSTGRESQL;
  Dataset dataSet = Dataset.BZ;

  Mode mode = Mode.UNIMODAL;
  boolean outputWriting = false;
  private String algorithmName;

  boolean debug = false;
  boolean useDensity = true;
  protected TableEntry destinationEdgeTableEntry;
  private Direction direction;
  int k = 0;
  private TableEntry destinationEntity;
  private int maxMemorySize;
  private short densityLimit = Short.MIN_VALUE;
  
  /**
   * <p>
   * Constructs a(n) <code>Config</code> object.
   * </p>
   * 
   * @param dbVendor
   * @throws FileNotFoundException
   */
  public Config(DBVendor dbVendor) {
    this(dbVendor, Thread.currentThread().getContextClassLoader().getResourceAsStream("etc/config.xml"));
  }

  /**
   * <p>
   * Constructs a(n) <code>Config</code> object.
   * </p>
   * 
   * @param dbVendor
   * @param dataset
   * @throws FileNotFoundException
   */
  public Config(DBVendor dbVendor, Dataset dataset) {
    this(dbVendor, Thread.currentThread().getContextClassLoader().getResourceAsStream("etc/config.xml"));
    this.dataSet = dataset;
    String fileName = "etc/config_" + dataset.toString().toLowerCase() + ".xml";
    InputStream dSetProps = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    appendPropertyFile(dSetProps);
  }

  /**
   * <p>
   * Constructs a(n) <code>Config</code> object.
   * </p>
   * 
   * @param dbVendor
   * @param dataset
   * @param baseDir
   * @throws FileNotFoundException
   */
  public Config(DBVendor dbVendor, Dataset dataset, String baseDir) throws FileNotFoundException {
    this(dbVendor, new FileInputStream(new File(baseDir + "config.xml")));
    this.dataSet = dataset;
    String fileName = baseDir + "config_" + dataset.toString().toLowerCase() + ".xml";
    appendPropertyFile(new FileInputStream(new File(fileName)));
  }
  
  /**
   * 
   * <p>Method setKNN</p>
   * @param useKNN
   */
  public void setK(int k) {
    this.k=k;
  }

  /**
   * <p>
   * Constructs a(n) <code>Config</code> object.
   * </p>
   * 
   * @param inputStream
   */
  public Config(DBVendor dbVendor, InputStream inputStream) {
    properties = new Properties();
    try {
      properties.loadFromXML(inputStream);
      properties.putAll(System.getProperties());
      String dbString = properties.getProperty("db.vendor", DBVendor.POSTGRESQL.toString()).toUpperCase();
      this.dbVendor = dbVendor == null ? DBVendor.valueOf(dbString) : dbVendor;
      if (factory == null) {
        if (this.dbVendor.equals(DBVendor.POSTGRESQL)) {
          serverName = getProperty("org.postgresql.servername");
          database = getProperty("org.postgresql.database");
          schema = getProperty("org.postgresql.schema");
          user = getProperty("org.postgresql.username");
          passwd = getProperty("org.postgresql.password");
          port = Integer.parseInt(getProperty("org.postgresql.port"));
        } else {
          serverName = getProperty("oracle.jdbc.servername");
          database = getProperty("oracle.jdbc.database");
          schema = getProperty("oracle.jdbc.username");
          user = getProperty("oracle.jdbc.username");
          passwd = getProperty("oracle.jdbc.password");
          port = Integer.parseInt(getProperty("oracle.jdbc.port"));
        }
        factory = new ConnectionFactory(this.dbVendor, Boolean.parseBoolean(getProperty("db.pooling")));
        LOGGER.fine("Registering driver on database " + this.dbVendor);
        factory.register(serverName, database, port, user, passwd);
      }
    } catch (IOException e) {
      throw new RuntimeException("Unable to load the config file: " + e);
    } catch (SQLException e) {
      throw new RuntimeException("Unable to connect to the database: " + e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Unable to find the database driver class: " + e);
    }
  }

  public Config(Config config) {
    properties = new Properties();
    appendProperties(config.getProperties());
    this.algorithmName = config.getAlgorithmName();
    this.dbVendor = config.getDbVendor();
    this.outputWriting = config.isOutputWriting();
    connection = getConnection();

    /*
     * destinationEdgeTableEntry = new TableEntry(getProperty("tbl.isoLinks"), getProperty("idx.isoLinks"),
     * TableType.LINK); destinationVertexTableEntry = new TableEntry(getProperty("tbl.isoNodes"),
     * getProperty("idx.isoNodes"), TableType.NODE);
     */
  }
  

  /**
   * Returns the value of the entry specified by the passed <code>key</code>.
   * 
   * @param key the key that identifies the entry
   * @return the value of the entry
   */
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  public Properties getProperties() {
    return properties;
  }

  /**
   * <p>
   * Method getDbVendor
   * </p>
   * 
   * @return
   */
  public DBVendor getDbVendor() {
    return dbVendor;
  }

  /**
   * <p>
   * Method getDataSet
   * </p>
   * 
   * @return
   */
  public Dataset getDataSet() {
    return dataSet;
  }

  /**
   * <p>
   * Method appendPropertyFile
   * </p>
   * appends the properties of that stream. An existing property is overwritten
   * 
   * @param inputStream
   */
  public void appendPropertyFile(InputStream inputStream) {
    try {
      properties.loadFromXML(inputStream);
      properties.putAll(System.getProperties());

      destinationEdgeTableEntry = new TableEntry(getProperty("tbl.isoLinks"), getProperty("idx.isoLinks"),
                                                 TableType.LINK);
      
      destinationEntity = new TableEntry(getProperty("tbl.isoEntities"), getProperty("idx.isoEntities"),
          TableType.NODE);
      
      if(densityLimit==Short.MIN_VALUE && getProperty("limit.density")!= null) {
        densityLimit = Short.parseShort(getProperty("limit.density"));
      }
      
    } catch (InvalidPropertiesFormatException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * <p>
   * Method appendPropertyFile
   * </p>
   * 
   * @param uri
   */
  public void appendPropertyFile(URI uri) {
    FileInputStream inputStream;
    try {
      inputStream = new FileInputStream(new File(uri));
      appendPropertyFile(inputStream);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
    }
  }

  public void appendProperties(Properties properties) {
    this.properties.putAll(properties);
  }

  /**
   * <p>
   * Method getEdgeTable
   * </p>
   * 
   * @return the table name of the network / edges
   */
  public String getEdgeTable() {
    return getProperty("tbl.links");
  }

  /**
   * <p>
   * Method getVertexTable
   * </p>
   * 
   * @return the table name of the vertices
   */
  public String getVertexTable() {
    return getProperty("tbl.nodes");
  }

  /**
   * <p>
   * Method getScheduleTable
   * </p>
   * 
   * @return the table name of the schedule
   */
  public String getScheduleTable() {
    return getProperty("tbl.times");
  }

  /**
   * <p>
   * Method getRouteTable
   * </p>
   * 
   * @return the table name of the routes
   */
  public String getRouteTable() {
    return getProperty("tbl.routes");
  }

  /**
   * <p>
   * Method getDaymarkerTable
   * </p>
   * 
   * @return
   */
  public String getDaymarkerTable() {
    return getProperty("tbl.dateCodes");
  }

  /**
   * <p>
   * Method getClientSRID
   * </p>
   * 
   * @return
   */
  public int getClientSRID() {
    return Integer.parseInt(getProperty("cfg.clientSRID"));
  }

  /**
   * <p>
   * Method getServerSRID
   * </p>
   * 
   * @return
   */
  public int getServerSRID() {
    return Integer.parseInt(getProperty("sql.spatial.srid"));
  }

  /**
   * <p>
   * Method getBatchSize
   * </p>
   * 
   * @return
   */
  public int getBatchSize() {
    return Integer.parseInt(getProperty("db.batchSize"));
  }
  
  /**
   * 
   * <p>Method getFlushLimit</p>
   * @return
   */
  public int getFlushLimit() {
    return Integer.parseInt(getProperty("cfg.flushLimit"));
  }

  /**
   * <p>
   * Method getConnection
   * </p>
   * 
   * @return
   */
  public Connection getConnection() {
    if (connection == null) {
      try {
        connection = factory.getConnection();
      } catch (SQLException e) {
        throw new RuntimeException("Unable to obtain a connection: " + e);
      }
    }
    return connection;
  }

  /**
   * <p>
   * Method getNewConnection
   * </p>
   * used only for some special cases
   * 
   * @return
   */
  public Connection getNewConnection() {
    if (connection == null) {
      try {
        connection = factory.getConnection();
      } catch (SQLException e) {
        throw new RuntimeException("Unable to obtain a connection: " + e);
      }
    }
    return connection;
  }

  /**
   * <p>
   * Method setMode
   * </p>
   * 
   * @param mode
   */
  public void setMode(Mode mode) {
    this.mode = mode;
  }

  /**
   * <p>
   * Method getMode
   * </p>
   * 
   * @return
   */
  public Mode getMode() {
    return mode;
  }
  
  /**
   * 
   * <p>Method getDirection</p>
   * @return
   */
  public Direction getDirection() {
    return direction;
  }
  
  /**
   * 
   * <p>Method isIncoming</p>
   * @return true, if incoming direction is considered
   */
  public boolean isIncoming(){
    return direction.equals(Direction.INCOMMING);
  }
  
  /**
   * 
   * <p>Method setDirection</p>
   * @param direction
   */
  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  /**
   * <p>
   * Method setOutputWriting
   * </p>
   * 
   * @param outputWriting
   */
  public void setOutputWriting(boolean outputWriting) {
    this.outputWriting = outputWriting;
  }

  /**
   * <p>
   * Method isOutputWriting
   * </p>
   * 
   * @return
   */
  public boolean isOutputWriting() {
    return outputWriting;
  }

  /**
   * <p>
   * Method getResultTableEntry
   * </p>
   * 
   * @return
   */
  public TableEntry getDestinationEdgeTableEntry() {
    return destinationEdgeTableEntry;
  }

  public void setDestinationEdgeTableEntry(TableEntry destinationEdgeTableEntry) {
    this.destinationEdgeTableEntry = destinationEdgeTableEntry;
  }

  /**
   * <p>
   * Method _setProperty
   * </p>
   * Overwrites a specific property. Use only for special reasons!!!
   * 
   * @param key
   * @param value
   */
  public void _setProperty(String key, String value) {
    properties.put(key, value);
  }

  /**
   * <p>
   * Method setAlgorithmName
   * </p>
   * 
   * @param algorithmName
   */
  public void setAlgorithmName(String algorithmName) {
    this.algorithmName = algorithmName;
  }

  /**
   * <p>
   * Method getAlgorithmName
   * </p>
   * 
   * @return
   */
  public String getAlgorithmName() {
    return algorithmName;
  }

  /**
   * <p>
   * Method isDebug
   * </p>
   * 
   * @return
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * <p>
   * Method useDensity
   * </p>
   * 
   * @return
   */
  public boolean useDensity() {
    return useDensity;
  }

  /**
   * <p>
   * Method setDebug
   * </p>
   * 
   * @param debug
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * <p>
   * Method setUseDensity
   * </p>
   * 
   * @param useDensity
   */
  public void setUseDensity(boolean useDensity) {
    this.useDensity = useDensity;
  }

  
  /**
   * <p>
   * Method getDatabase
   * </p>
   * 
   * @return the name of the database
   */
  public String getDatabase() {
    return database;
  }

  /**
   * <p>
   * Method getDatabaseServerName
   * </p>
   * 
   * @return
   */
  public String getDatabaseServerName() {
    return serverName;
  }

  public int getDatabasePort() {
    return port;
  }

  public String getDatabaseUsername() {
    return user;
  }

  public String getDatabasePassword() {
    return passwd;
  }

  public String getDatabaseSchema() {
    return schema;
  }

  /**
   * 
   * <p>Method isKNN</p>
   * @return
   */
  public boolean isKNN() {
    return k>0;
  }
  
  /**
   * 
   * <p>Method getK</p>
   * @return
   */
  public int getK() {
    return k;
  }

  /**
   * 
   * <p>Method getDestinationEntityTableEntry</p>
   * @return
   */
  public TableEntry getDestinationEntityTableEntry() {
    return destinationEntity;
  }
  
  /**
   * 
   * <p>Method setDestinationEntity</p>
   * @param destinationEntity
   */
  public void setDestinationEntity(TableEntry destinationEntity) {
    this.destinationEntity = destinationEntity;
  }

  /**
   * 
   * <p>Method getMaxMemorySize</p>
   * @return
   */
  public int getMaxMemorySize() {
    return maxMemorySize;
  }
  
  /**
   * 
   * <p>Method setMaxMemorySize</p>
   * @param maxMemorySize
   */
  public void setMaxMemorySize(int maxMemorySize) {
    this.maxMemorySize = maxMemorySize;
  }

  public void setDensityLimit(short densityLimit) {
    this.densityLimit = densityLimit;
  }
  
  /**
   * 
   * <p>Method getDensityLimit</p> returns the density limit, being the number of vertices
   * @return the number of vertices 
   */
  public short getDensityLimit() {
    return densityLimit;
  }

}
