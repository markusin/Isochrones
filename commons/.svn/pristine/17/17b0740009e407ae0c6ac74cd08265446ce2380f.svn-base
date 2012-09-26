package isochrones.tools.precomputation;

import isochrones.network.NWMode;
import isochrones.utils.Config;
import isochrones.utils.DBUtility;
import isochrones.utils.precomputation.DensityUtility;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.postgresql.PGStatement;

/**
 * <p>
 * The <code>DensityCalculation</code> class
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
public class DensityCalculation {

  private Config config;

  /**
   * <p>
   * Constructs a(n) <code>DensityCalculation</code> object.
   * </p>
   * 
   * @param query
   */
  public DensityCalculation(DensityUtility util) {
    this.config = util.getConfig();
    DBUtility.truncateTable(config.getConnection(),
        util.isCountEdges() ? config.getProperty("tbl.edge.density") : config.getProperty("tbl.vertex.density"));
  }

  /**
   * <p>
   * Method getDiscreteVertices
   * </p>
   * 
   * @return
   */
  public Set<Integer> getDiscreteVertices() {
    SortedSet<Integer> stopStations = new TreeSet<Integer>();
    String sql = "SELECT SOURCE AS STOP_ID FROM " + config.getEdgeTable() + " WHERE EDGE_MODE=" + NWMode.DISCRETE + " UNION"
        + " SELECT TARGET FROM " + config.getEdgeTable() + " WHERE EDGE_MODE=" + NWMode.DISCRETE;

    PreparedStatement pStmt = null;
    ResultSet rSet = null;
    try {
      pStmt = config.getConnection().prepareStatement(sql);
      rSet = pStmt.executeQuery();
      while (rSet.next()) {
        stopStations.add(rSet.getInt("STOP_ID"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (rSet != null)
          rSet.close();
        if (pStmt != null)
          pStmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return stopStations;
  }

  /**
   * <p>
   * Method computeDensity
   * </p>
   * 
   * @param vertexId
   * @param distances
   * @return
   */
  public Map<Integer, Integer> computeEdgeDensity(int vertexId, int[] distances) {

    HashMap<Integer, Integer> densities = new HashMap<Integer, Integer>();

    String sql = "SELECT count(*)  FROM get_edges_pgr(?,?,?,'" + config.getEdgeTable() + "','"
        + config.getVertexTable() + "')";

    PreparedStatement pStmt = null;
    ResultSet rSet = null;
    try {
      pStmt = config.getConnection().prepareStatement(sql);
      for (int i = 0; i < distances.length; i++) {
        pStmt.setDouble(1, distances[i]);
        pStmt.setDouble(2, 1.0);
        pStmt.setInt(3, vertexId);
        rSet = pStmt.executeQuery();
        if (rSet.next()) {
          densities.put(distances[i], rSet.getInt(1));
          // int linkId = rSet.getInt("ID");
          // int startNodeId = rSet.getInt("SOURCE");
          // int endNodeId = rSet.getInt("TARGET");
          // double length = rSet.getDouble("LENGTH");
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
      try {
        config.getConnection().rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }

    } finally {
      try {
        if (rSet != null)
          rSet.close();
        if (pStmt != null)
          pStmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return densities;
  }
  
  public Map<Integer, Integer> computeVertexDensity(int vertexId, int[] distances) {

    HashMap<Integer, Integer> densities = new HashMap<Integer, Integer>();

    String sql = "SELECT COUNT(DISTINCT TARGET) FROM get_edges_pgr(?,?,?,'" + config.getEdgeTable() + "','"
        + config.getVertexTable() + "')";

    PreparedStatement pStmt = null;
    ResultSet rSet = null;
    try {
      pStmt = config.getConnection().prepareStatement(sql);
      for (int i = 0; i < distances.length; i++) {
        pStmt.setDouble(1, distances[i]);
        pStmt.setDouble(2, 1.0);
        pStmt.setInt(3, vertexId);
        rSet = pStmt.executeQuery();
        if (rSet.next()) {
            densities.put(distances[i], rSet.getInt(1));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        config.getConnection().rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }

    } finally {
      try {
        if (rSet != null)
          rSet.close();
        if (pStmt != null)
          pStmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return densities;
  }


  private void storeDensities(Integer vertexId, Map<Integer, Integer> distanceDensity, boolean countEdges) {

    String sql = "INSERT INTO " + (countEdges ? config.getProperty("tbl.edge.density") : config.getProperty("tbl.vertex.density")) + " (ID,DISTANCE,DENSITY) VALUES (?,?,?)";
    PreparedStatement pStmt = null;
    try {
      pStmt = config.getConnection().prepareStatement(sql);
      ((PGStatement) pStmt).setPrepareThreshold(config.getBatchSize());
      for (Integer distance : distanceDensity.keySet()) {
        pStmt.setInt(1, vertexId);
        pStmt.setInt(2, distance); // distance
        pStmt.setInt(3, distanceDensity.get(distance)); // density
        pStmt.executeUpdate();
      }
      config.getConnection().commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        config.getConnection().rollback();

      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      try {
        pStmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public void terminate() {
    try {
      config.getConnection().commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * <p>Method main</p>
   * @param args
   */
  public static void main(String[] args) {
    DensityUtility util = new DensityUtility(args, '=');
    DensityCalculation dCalc = new DensityCalculation(util);
    Set<Integer> discreteVertices = dCalc.getDiscreteVertices();
    for (Integer v : discreteVertices) {
      System.out.println("Computing densities for vertex " + v);
      Map<Integer, Integer> distanceDensity;
      if(util.isCountEdges()) {
        distanceDensity = dCalc.computeEdgeDensity(v, util.getDistances());
      } else {
        distanceDensity = dCalc.computeVertexDensity(v, util.getDistances());
      }
      dCalc.storeDensities(v, distanceDensity, util.isCountEdges());
    }
  }

}
