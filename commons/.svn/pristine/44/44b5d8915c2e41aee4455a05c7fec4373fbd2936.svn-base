package isochrones.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.PooledConnection;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;

import org.postgresql.ds.PGConnectionPoolDataSource;

/**
 * 
*
* <p>The <code>ConnectionFactory</code> class</p>
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p>This class is the factory class for creating a connection. </p> 
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* Should become a Singleton
* @version 2.2
 */
public class ConnectionFactory {
  
  boolean pooling = false;
  DBVendor vendor = DBVendor.POSTGRESQL;
  private PooledConnection connectionPool;
  private Connection connection;
  protected static final Logger LOGGER = Logger.getLogger(ConnectionFactory.class.getPackage().getName());
  
  
  /**
   * 
   * <p>Constructs a(n) <code>ConnectionFactory</code> object.</p>
   * @param vendor
   * @param pooling
   */
  public ConnectionFactory(DBVendor vendor, boolean pooling) {
    this.pooling = pooling;
    this.vendor = vendor;
  }
  
  /**
   * 
   * <p>Method register</p>
   * @param serverName
   * @param database
   * @param port
   * @param user
   * @param passwd
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  public void register(String serverName, String database, int port, String user, String passwd) throws SQLException, ClassNotFoundException{
    if(pooling) {
      if (vendor.equals(DBVendor.ORACLE)) {
        OracleConnectionPoolDataSource pds = new OracleConnectionPoolDataSource();
        pds.setServerName(serverName);
        pds.setDatabaseName(database);
        pds.setPortNumber(port);
        pds.setUser(user);
        pds.setPassword(passwd);
        pds.setDriverType("thin");
        connectionPool = pds.getPooledConnection();
      } else {
        PGConnectionPoolDataSource pds = new PGConnectionPoolDataSource();
        pds.setServerName(serverName);
        pds.setDatabaseName(database);
        pds.setPortNumber(port);
        pds.setUser(user);
        pds.setPassword(passwd);
        connectionPool = pds.getPooledConnection();
      }
    } else {
      String url;
      if (vendor.equals(DBVendor.POSTGRESQL)) {
        Class.forName("org.postgresql.Driver"); // load the driver
        url = "jdbc:postgresql://" + serverName + ":" + port + "/" + database +"?binaryTransfer=true";
      } else {
        Class.forName("oracle.jdbc.driver.OracleDriver"); // load the driver
        url = "jdbc:oracle:thin:@" + serverName + ":" + port + ":" + database ;
      }
      connection = DriverManager.getConnection(url,user,passwd);
      connection.setAutoCommit(false);
    }
  }
  
  /**
   * 
   * <p>Method getConnection</p>
   * @return
   * @throws SQLException
   */
  public Connection getConnection() throws SQLException{
    if(pooling) {
      Connection conn = connectionPool.getConnection();
      conn.setAutoCommit(false);
      return conn;
    } else {
      return connection;
    }
  }

}
