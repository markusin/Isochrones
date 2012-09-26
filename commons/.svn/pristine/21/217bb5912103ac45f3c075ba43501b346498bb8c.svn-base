package isochrones.ant.tasks;

import isochrones.db.ConnectionFactory;
import isochrones.db.DBResult;
import isochrones.db.DBVendor;
import isochrones.utils.DBUtility;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.postgresql.PGStatement;

/**
 * <p>
 * The <code>DensitytableGenerationTask</code> generates the density table of a given vertex table
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
public class VertexDensityGenerationTask extends Task {

  private String vertexTableName, vertexDensityTable;
  private int[] sizePoints;
  private String host, dbname, user, passwd;
  private double maxRange = Double.NEGATIVE_INFINITY;
  private int port = Integer.MIN_VALUE, percentage = Integer.MIN_VALUE;

  @Override
  public void execute() {
    validate();
    ConnectionFactory factory = null;
    
    long start = System.currentTimeMillis();

    try {
      factory = new ConnectionFactory(DBVendor.POSTGRESQL, false);
      factory.register(host, dbname, port, user, passwd);

      DBUtility.createDensityTable(factory.getConnection(), vertexDensityTable);

      Collection<Integer> vertices = DBUtility.getAllVertices(factory.getConnection(), vertexTableName);
      Collection<DensityEntry> result = new ArrayList<DensityEntry>();
      
      int processed = 1;
      int totalSize = vertices.size();
      int processedPercentage = 1;
      for (Integer vertexId : vertices) {
        System.out.println("Processing vertex: " + vertexId + "\t" + processed + "/" + totalSize);
        maxRange = DBUtility.getBestpossibleRange(factory.getConnection(), vertexTableName, vertexId,sizePoints[sizePoints.length-1],maxRange,percentage); 
        DBResult dbResult = DBUtility.getDensityTuples(factory.getConnection(), vertexTableName, vertexId,maxRange);

        //SortedMap<Integer, DensityEntry> entries = new TreeMap<Integer, DensityEntry>();
        DensityEntry[] entries = new DensityEntry[sizePoints.length];
        ResultSet rset = dbResult.getResultSet();
        int density = 1;
        int i = 0;
        
        while (rset.next() && i<sizePoints.length) {
          if (sizePoints[i] <= density) {
            entries[i] = new DensityEntry(vertexId,sizePoints[i++], rset.getDouble("e_dist"));
          }
          density++;
        }
        
        dbResult.close();
        
        while(i<sizePoints.length){
          entries[i] = new DensityEntry(vertexId,sizePoints[i],entries[i-1].getDensity());
          i++;
        }
        
        result.addAll(Arrays.asList(entries));

        if(processedPercentage<processed/totalSize*100){
          System.out.println("#######" +processedPercentage +"% processed");
          processedPercentage++;
        }
        processed++;
      }
      
      // store values in the table
      String sql = "INSERT INTO " + vertexDensityTable + " (id,density,e_dist) values(?,?,?)";
      PreparedStatement pStmt = factory.getConnection().prepareStatement(sql);
      ((PGStatement) pStmt).setPrepareThreshold(1000);
      
      for (DensityEntry entry : result) {
        pStmt.setInt(1, entry.getId());
        pStmt.setInt(2, entry.getDensity());
        pStmt.setDouble(3, entry.getEDist());
        pStmt.executeUpdate();
      }
      factory.getConnection().commit();
      // finally create the index
      DBUtility.createDensityTable_Index(factory.getConnection(), vertexDensityTable);
      
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    
    System.out.println("Vertex-density generated terminated in " + (System.currentTimeMillis()-start)/1000 + " seconds.");

  }

  public void setVertexTable(String vertexTable) {
    this.vertexTableName = vertexTable;
  }

  public void setDensityTable(String densityTable) {
    this.vertexDensityTable = densityTable;
  }

  public void setSizePoints(String sizePoints) {
    String[] spts = sizePoints.split(",");
    this.sizePoints = new int[spts.length];
    for (int i = 0; i < spts.length; i++) {
      this.sizePoints[i] = Short.parseShort(spts[i]);
    }
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setDbName(String dbName) {
    this.dbname = dbName;
  }

  public void setPort(String port) {
    this.port = Integer.parseInt(port);
  }

  public void setUser(String user) {
    this.user = user;
  }

  public void setPasswd(String passwd) {
    this.passwd = passwd;
  }
  
  public void setMaxRange(String maxRange) {
    this.maxRange = Double.parseDouble(maxRange);
  }
  
  /**
   * 
   * <p>Method setPercentage</p> sets the percentage in which the size should be. Recommendent is 5-10%
   * @param percentage
   */
  public void setPercentage(String percentage) {
    this.percentage = Integer.parseInt(percentage);
  }

  private void validate() {
    if (vertexTableName == null)
      throw new BuildException("You must specify the vertex table in attribute vertexTableName.");
    if (vertexDensityTable == null)
      throw new BuildException("You must specify the vertex density table in attribute vertexTableName.");
    if (sizePoints == null)
      throw new BuildException("You must specify size points in attribute sizePoints.");
    if (host == null)
      throw new BuildException("You must specify the host of the db in attribute host.");
    if (dbname == null)
      throw new BuildException("You must specify the name of the db in attribute dbName.");
    if (port == Integer.MIN_VALUE)
      throw new BuildException("You must specify the port of the db in attribute port.");
    if (user == null)
      throw new BuildException("You must specify the username in attribute user.");
    if (passwd == null)
      throw new BuildException("You must specify the password in attribute passwd.");
    if (maxRange == Double.NEGATIVE_INFINITY)
      throw new BuildException("You must specify the maxima range in attribute maxRange.");
    if (maxRange == Double.NEGATIVE_INFINITY)
      throw new BuildException("You must specify the maxima range in attribute maxRange.");
    if (percentage == Integer.MIN_VALUE)
      throw new BuildException("You must specify the percentage of the range in attribute percentage.");
  }
  
 

  /**
   * <p>
   * Method main
   * </p>
   * only for testing
   * 
   * @param args
   */
  public static void main(String[] args) {
    VertexDensityGenerationTask task = new VertexDensityGenerationTask();
    task.setVertexTable("it_nodes");
    task.setDensityTable("it_test_density");
    task.setSizePoints("500,1000,1500,2000,2500,3000,3500,4000,4500,5000,5500,6000,6500,7000,8500,9000,9500,10000");
    task.setHost("localhost");
    task.setDbName("iso2");
    task.setPort("5432");
    task.setUser("postgres");
    task.setPasswd("AifaXub2");
    task.setMaxRange("25000");
    task.setPercentage("10");
    task.execute();
  }

}
