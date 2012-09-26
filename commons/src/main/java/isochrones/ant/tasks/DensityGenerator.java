package isochrones.ant.tasks;

import isochrones.db.ConnectionFactory;
import isochrones.db.DBResult;
import isochrones.db.DBVendor;
import isochrones.network.Vertex;
import isochrones.utils.DBUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.postgresql.PGStatement;

public class DensityGenerator {

  Connection connection;

  /**
   * <p>
   * Constructs a(n) <code>DensityGenerator</code> object.
   * </p>
   * 
   * @param host
   * @param db
   * @param port
   * @param user
   * @param passwd
   */
  public DensityGenerator(String host, String db, int port, String user, String passwd) {
    ConnectionFactory factory = new ConnectionFactory(DBVendor.POSTGRESQL, false);
    try {
      factory.register(host, db, port, user, passwd);
      connection = factory.getConnection();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<DensityEntry> createDensityTable(String vertexTable, int[] sizePoints) throws SQLException {

    ArrayList<DensityEntry> result = new ArrayList<DensityEntry>();
    int vertexSize = DBUtility.getTotalVertexSize(connection, vertexTable, true);
    Vertex[] V = new Vertex[vertexSize];

    int maxSize = sizePoints[sizePoints.length - 1];

    DBResult dbResult = DBUtility.getAllVerticesSortedByX(connection, vertexTable);
    ResultSet rSet = dbResult.getResultSet();
    int i = 0;
    while (rSet.next()) { // filling the array with values
      V[i++] = new Vertex(rSet.getInt(1), rSet.getDouble(2), rSet.getDouble(3));
    }

    double[] distances = new double[maxSize];

    // defining comparator
    Comparator<Double> descendingCmp = new Comparator<Double>() {
      @Override
      public int compare(Double o1, Double o2) {
        if (o1 < o2)
          return 1;
        if (o1 > o2)
          return -1;
        return 0;
      }
    };

    AbstractQueue<Double> heap = new PriorityQueue<Double>(maxSize, descendingCmp);
    for (i = 0; i < vertexSize; i++) {
      System.out.println("Iteration: " + i + "/" + vertexSize);
      double k = Double.POSITIVE_INFINITY;
      int size = 0;

      int l = i, r = i, idx = 0;
      while (V[r].x - V[l].x <= 2 * k && idx < vertexSize - 1) {
        if (l == 0) { // left out of range condition
          idx = ++r;
        } else if (r == vertexSize - 1) { // right out of range condition
          idx = --l;
        } else { // choose between left and right element that one with smallest distance to its neighbor
          idx = V[i].x - V[l - 1].x < V[r + 1].x - V[i].x ? --l : ++r;
        }
        if (size < maxSize) { // as long we do not reach max size we simply add in heap and sort
          heap.add(eDist(V[i], V[idx]));
          size++;
        } else {
          double d = eDist(V[i], V[idx]);
          if (heap.peek() > d) {
            heap.poll();
            heap.add(d);
            k = heap.peek();
          }
        }
      }

      // convert heap to a sorted array
      int j = distances.length - 1;

      while (!heap.isEmpty()) {
        distances[j--] = heap.poll();
      }

      // reading for each size the corresponding euclidean distance
      for (j = 0; j < sizePoints.length; j++) {
        int p = sizePoints[j];
        result.add(new DensityEntry(V[i].id, p, distances[p - 1]));
      }

    }
    return result;
  }

  private double eDist(Vertex v1, Vertex v2) {
    return Math.sqrt(Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2));
  }

  private void storeResult(ArrayList<DensityEntry> result, String resultTable) throws SQLException {
    DBUtility.createDensityTable(connection, resultTable);

    String sql = "INSERT INTO " + resultTable + " (id,density,e_dist) values(?,?,?)";
    PreparedStatement pStmt = connection.prepareStatement(sql);
    ((PGStatement) pStmt).setPrepareThreshold(1000);

    for (DensityEntry entry : result) {
      pStmt.setInt(1, entry.getId());
      pStmt.setInt(2, entry.getDensity());
      pStmt.setDouble(3, entry.getEDist());
      pStmt.executeUpdate();
    }
    connection.commit();
    // finally create the index
    DBUtility.createDensityTable_Index(connection, resultTable);
  }

  /**
   * <p>
   * Method main
   * </p>
   * 
   * @param args
   * @throws SQLException
   */
  public static void main(String[] args) throws SQLException {
    // new DensityGenerator("bz_nodes", "bz_test_density2",new int[]{500,1000,1500,2000,2500}, "localhost", "iso2",
    // 5432, "postgres", "AifaXub2");
    // new DensityGenerator("sf_nodes", "sf_test_density2",new
    // int[]{500,1000,1500,2000,2500,3000,3500,4000,4500,5000,5500,6000,6500,7000}, "localhost", "iso2", 5432,
    // "postgres", "AifaXub2");
    long start = System.currentTimeMillis();
    DensityGenerator generator = new DensityGenerator("localhost", "iso2", 5432, "postgres", "AifaXub2");
    ArrayList<DensityEntry> result = generator.createDensityTable("it_nodes", new int[] { 500, 1000, 1500, 2000, 2500,
        3000, 3500, 4000, 4500, 5000 });
    generator.storeResult(result, "it_test_density");
    System.out.println("Vertex-density terminated in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");

  }

  /**
   * <p>
   * The <code>Direction</code> class
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
  public interface Direction {
    public static final short NORTH = 0;
    public static final short SOUTH = 1;
    public static final short WEST = 2;
    public static final short EAST = 3;
  }

}
