package isochrones.utils;

import isochrones.algorithm.Mode;
import isochrones.algorithm.TableEntry;
import isochrones.db.DBResult;
import isochrones.db.DBVendor;
import isochrones.network.GeoPoint;
import isochrones.network.QueryPoint;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtility {

  /**
   * <p>
   * Method dropTable
   * </p>
   * 
   * @param connection
   * @param database
   * @param tableName
   * @param cascade
   */
  public static void dropTable(Connection connection, DBVendor database, String tableName, boolean cascade) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      dropTable_PG(connection, tableName, cascade);
    } else if (database.equals(DBVendor.ORACLE)) {
      dropTable_ORA(connection, tableName, cascade);
    }
  }

  /**
   * <p>
   * Method dropTables
   * </p>
   * 
   * @param connection
   * @param tableName
   * @param cascade
   */
  public static void dropTable_ORA(Connection connection, String tableName, boolean cascade) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("DROP TABLE " + tableName + (cascade ? " CASCADE CONSTRAINTS" : ""));
      connection.commit();
    } catch (SQLException e) {
      if (e.getErrorCode() != ErrorCodes.ORA.TABLE_NOT_EXIST) {
        e.printStackTrace();
      }
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void dropTable_PG(Connection connection, String tableName, boolean cascade) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("DROP TABLE " + tableName + (cascade ? " CASCADE" : ""));
      connection.commit();
    } catch (SQLException e) {
      if (e.getErrorCode() != ErrorCodes.PG.TABLE_NOT_EXIST) {
        e.printStackTrace();
      }
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void truncateTable(Connection connection, String tableName) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("TRUNCATE TABLE " + tableName);
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * <p>
   * Method dropIndex
   * </p>
   * 
   * @param connection
   * @param indexName
   */
  public static void dropIndex(Connection connection, String indexName) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("DROP INDEX " + indexName);
      connection.commit();
    } catch (SQLException e) {
      if (e.getErrorCode() == 1418 || e.getSQLState().equalsIgnoreCase("42704")) {
      } else {
        e.printStackTrace();
      }
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * <p>
   * Method controlIndex
   * </p>
   * 
   * @param connection
   * @param indexName
   * @param disabled
   */
  public static void controlIndex(Connection connection, DBVendor database, TableEntry table, boolean disabled) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      controlIndex_PG(connection, table, disabled);
    } else if (database.equals(DBVendor.ORACLE)) {
      controlIndex_ORA(connection, table, disabled);
    }
  }

  public static void controlIndex_ORA(Connection connection, TableEntry tableEntry, boolean disabled) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("ALTER INDEX " + tableEntry.getIndexName() + (disabled ? " UNUSABLE" : " REBUILD"));
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      }
    }

  }

  /**
   * <p>
   * Method controlIndex_PG
   * </p>
   * there is not a way to disable an index, so
   * 
   * @param connection
   * @param tableEntry
   * @param disabled
   */
  public static void controlIndex_PG(Connection connection, TableEntry tableEntry, boolean disabled) {
    if (disabled) {
      DBUtility.dropIndex(connection, tableEntry.getIndexName());
    } else {
      DBUtility.createSpatialIndex_PG(connection, tableEntry);
    }
  }

  /**
   * <p>
   * Method createTargetEdgeTable
   * </p>
   * 
   * @param connection
   * @param edgeTableName
   * @param database
   */
  public static void createTargetEdgeTable(Connection connection, DBVendor database, String edgeTableName) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      createTargetEdgeTable_PG(connection, edgeTableName);
    } else if (database.equals(DBVendor.ORACLE)) {
      createTargetEdgeTable_ORA(connection, edgeTableName);
    }
  }

  public static void createTargetEdgeTable_PG(Connection connection, String edgeTableName) {
    dropTable_PG(connection, edgeTableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt
          .executeUpdate("CREATE TABLE "
              + edgeTableName
              + " (\"ID\" integer NOT NULL, \"SOURCE\" integer, \"TARGET\" integer, \"OFFSET\" double precision, \"LENGTH\" double precision)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void createTargetEdgeTable_ORA(Connection connection, String edgeTableName) {
    dropTable_ORA(connection, edgeTableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt
          .executeUpdate("CREATE TABLE "
              + edgeTableName
              + " (\"ID\" NUMBER(10,0),\"SOURCE\" NUMBER(10,0), \"TARGET\" NUMBER(10,0), \"OFFSET\" NUMBER(8,4), \"LENGTH\" NUMBER(8,4), \"GEOMETRY\" MDSYS.SDO_GEOMETRY)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * <p>
   * Method createEdgeIndices
   * </p>
   * 
   * @param connection
   * @param database
   * @param edgeTableName
   * @param mode
   */
  public static void createEdgeIndices(Connection connection, DBVendor database, String edgeTableName, Mode mode) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      createEdgeIndices_PG(connection, edgeTableName, mode);
    } else if (database.equals(DBVendor.ORACLE)) {
      createEdgeIndices_ORA(connection, edgeTableName, mode);
    }
  }

  private static void createEdgeIndices_ORA(Connection connection, String edgeTableName, Mode mode) {
    Statement stmnt = null;
    if (mode.equals(Mode.MULTIMODAL)) {
      String indexName = "IDX_" + edgeTableName + "TAR_CMB";
      DBUtility.dropIndex(connection, indexName);
      try {
        stmnt = connection.createStatement();
        stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + edgeTableName + "(TARGET,EDGE_MODE)");
        connection.commit();
      } catch (SQLException e) {
        e.printStackTrace();
        try {
          connection.rollback();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      } finally {
        if (stmnt != null) {
          try {
            stmnt.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }

      indexName = "IDX_" + edgeTableName + "_SRC_CMB";
      DBUtility.dropIndex(connection, indexName);
      try {
        stmnt = connection.createStatement();
        stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + edgeTableName + " (SOURCE,EDGE_MODE)");
        connection.commit();
      } catch (SQLException e) {
        e.printStackTrace();
        try {
          connection.rollback();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      } finally {
        if (stmnt != null) {
          try {
            stmnt.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }

    }

    try {
      String indexName = "IDX_" + edgeTableName + "_TAR";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + edgeTableName + "(TARGET)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      String indexName = "IDX_" + edgeTableName + "_SRC";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + edgeTableName + "(SOURCE)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

  }

  private static void createEdgeIndices_PG(Connection connection, String edgeTableName, Mode mode) {
    Statement stmnt = null;
    if (mode.equals(Mode.MULTIMODAL)) {
      String indexName = "IDX_" + edgeTableName + "_TAR_CMB";
      DBUtility.dropIndex(connection, indexName);
      try {
        stmnt = connection.createStatement();
        stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + edgeTableName + " USING btree(TARGET,EDGE_MODE)");
        connection.commit();
      } catch (SQLException e) {
        e.printStackTrace();
        try {
          connection.rollback();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      } finally {
        if (stmnt != null) {
          try {
            stmnt.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }
      indexName = "IDX_" + edgeTableName + "_SRC_CMB";
      DBUtility.dropIndex(connection, indexName);
      try {
        stmnt = connection.createStatement();
        stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + edgeTableName + " USING btree(SOURCE,EDGE_MODE)");
        connection.commit();
      } catch (SQLException e) {
        e.printStackTrace();
        try {
          connection.rollback();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      } finally {
        if (stmnt != null) {
          try {
            stmnt.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }
    }

    try {
      String indexName = "IDX_" + edgeTableName + "_TAR";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + edgeTableName + " USING btree(TARGET)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    try {
      String indexName = "IDX_" + edgeTableName + "_SRC";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + edgeTableName + " USING btree(SOURCE)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * <p>
   * Method createScheduleIndices
   * </p>
   * 
   * @param connection
   * @param database
   * @param scheduleTableName
   */
  public static void createScheduleIndices(Connection connection, DBVendor database, String scheduleTableName) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      createScheduleIndices_PG(connection, scheduleTableName);
    } else if (database.equals(DBVendor.ORACLE)) {
      createScheduleIndices_ORA(connection, scheduleTableName);
    }
  }

  private static void createScheduleIndices_ORA(Connection connection, String scheduleTableName) {
    Statement stmnt = null;
    try {
      String indexName = "IDX_" + scheduleTableName + "_CMB";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName + "(ROUTE_ID,SOURCE,TARGET)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      String indexName = "IDX_" + scheduleTableName + "_IN_CMB1";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt
          .executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName + "(ROUTE_ID,SOURCE,TARGET,TIME_AD)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      String indexName = "IDX_" + scheduleTableName + "IN_CMB2";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName + "(ROUTE_ID,TARGET,TIME_A)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      String indexName = "IDX_" + scheduleTableName + "_OUT_CMB1";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName + "(ROUTE_ID,SOURCE,TARGET,TIME_D)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      String indexName = "IDX_" + scheduleTableName + "_OUT_CMB2";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName + "(ROUTE_ID,SOURCE,TIME_D)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static void createScheduleIndices_PG(Connection connection, String scheduleTableName) {

    Statement stmnt = null;
    try {
      String indexName = "IDX_" + scheduleTableName + "_CMB";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName
          + " USING btree(ROUTE_ID,SOURCE,TARGET)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      String indexName = "IDX_" + scheduleTableName + "_IN_CMB1";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName
          + " USING btree(ROUTE_ID,SOURCE,TARGET,TIME_A)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      String indexName = "IDX_" + scheduleTableName + "_IN_CMB2";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName
          + " USING btree(ROUTE_ID,TARGET,TIME_A)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      String indexName = "IDX_" + scheduleTableName + "_OUT_CMB1";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName
          + " USING btree(ROUTE_ID,SOURCE,TARGET,TIME_D)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      String indexName = "IDX_" + scheduleTableName + "_OUT_CMB2";
      DBUtility.dropIndex(connection, indexName);
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + scheduleTableName
          + " USING btree(ROUTE_ID,SOURCE,TIME_D)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

  }

  /**
   * <p>
   * Method insertGeometryMetadata
   * </p>
   * 
   * @param connection
   * @param database
   * @param tableName
   */
  public static void insertGeometryMetadata(Connection connection, TableEntry tableEntry, DBVendor database, int srid) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      insertGeometryMetadata_PG(connection, tableEntry, srid);
    } else if (database.equals(DBVendor.ORACLE)) {
      insertGeometryMetadata_ORA(connection, tableEntry, srid);
    }
  }

  public static void insertGeometryMetadata_ORA(Connection connection, TableEntry tableEntry, int srid) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("DELETE MDSYS.USER_SDO_GEOM_METADATA WHERE UPPER(TABLE_NAME)='"
          + tableEntry.getTableName().toUpperCase() + "'");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    double minX = -180, minY = -90, maxX = 180, maxY = 90;

    try {
      stmnt = connection.createStatement();
      String sql = "SELECT MIN(SDO_GEOM.SDO_MIN_MBR_ORDINATE(GEOMETRY,1)) AS minX,"
          + "MAX(SDO_GEOM.SDO_MAX_MBR_ORDINATE(GEOMETRY,1)) AS maxX,"
          + "MIN(SDO_GEOM.SDO_MIN_MBR_ORDINATE(GEOMETRY,2)) AS minY,"
          + "MAX(SDO_GEOM.SDO_MAX_MBR_ORDINATE(GEOMETRY,2)) AS maxY FROM " + tableEntry.getTableName();
      ResultSet rSet = stmnt.executeQuery(sql);
      if (rSet.next()) {
        minX = rSet.getDouble("minX");
        minY = rSet.getDouble("minY");
        maxX = rSet.getDouble("maxX");
        maxY = rSet.getDouble("maxY");
      }
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    String dimEl1 = "MDSYS.SDO_DIM_ELEMENT('X'," + minX + "," + maxX + ",0.001)";
    String dimEl2 = "MDSYS.SDO_DIM_ELEMENT('Y'," + minY + "," + maxY + ",0.001)";

    StringBuilder b = new StringBuilder();
    b.append("INSERT INTO MDSYS.USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID) VALUES ");
    b.append("( '").append(tableEntry.getTableName()).append("',");
    b.append("'GEOMETRY',MDSYS.SDO_DIM_ARRAY(").append(dimEl1).append(",");
    b.append(dimEl2).append("),");
    b.append(srid).append(")");

    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate(b.toString());
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static void dropNetwork_PG(Connection connection, String tableName) {
    CallableStatement prepareCall = null;
    try {
      prepareCall = connection.prepareCall("{call dropgeometrycolumn(?,?)}");
      prepareCall.setString(1, tableName.toLowerCase());
      prepareCall.setString(2, "geometry");
      prepareCall.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (prepareCall != null) {
        try {
          prepareCall.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static void createMetaLinks_PG(Connection connection, String tableName, int srid) {
    CallableStatement prepareCall = null;
    try {
      prepareCall = connection.prepareCall("{call AddGeometryColumn(?,?,?,?,?)}");
      prepareCall.setString(1, tableName.toLowerCase());
      prepareCall.setString(2, "geometry");
      prepareCall.setInt(3, srid);
      prepareCall.setString(4, "LINESTRING");
      prepareCall.setInt(5, 4);
      prepareCall.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (prepareCall != null) {
        try {
          prepareCall.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static void createMetaNodes_PG(Connection connection, String tableName, int srid) {
    CallableStatement prepareCall = null;
    try {
      prepareCall = connection.prepareCall("{call AddGeometryColumn(?,?,?,?,?)}");
      prepareCall.setString(1, tableName.toLowerCase());
      prepareCall.setString(2, "geometry");
      prepareCall.setInt(3, srid);
      prepareCall.setString(4, "POINT");
      prepareCall.setInt(5, 4);
      prepareCall.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (prepareCall != null) {
        try {
          prepareCall.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected static void insertGeometryMetadata_PG_old(Connection connection, TableEntry tableEntry, int srid) {
    // dropNetwork_PG(connection, tableName);
    // createMetaLinks_PG(connection, tableName, srid);
    Statement stmnt = null;
    try {
      // insert into geometry_columns (f_table_name
      stmnt = connection.createStatement();
      stmnt.executeUpdate("DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME='" + tableEntry.getTableName() + "'");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    PreparedStatement pstmnt = null;
    try {
      String sql = "INSERT INTO GEOMETRY_COLUMNS (F_TABLE_CATALOG, F_TABLE_SCHEMA,F_TABLE_NAME,F_GEOMETRY_COLUMN,COORD_DIMENSION,SRID,TYPE) VALUES(?,?,?,?,?,?,?)";
      pstmnt = connection.prepareStatement(sql);
      pstmnt.setString(1, "");
      pstmnt.setString(2, "public");
      pstmnt.setString(3, tableEntry.getTableName());
      pstmnt.setString(4, "geometry");
      pstmnt.setInt(5, 2);
      pstmnt.setInt(6, srid);
      pstmnt.setString(7, tableEntry.getGeometryType());
      pstmnt.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (pstmnt != null) {
        try {
          pstmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected static void insertGeometryMetadata_PG(Connection connection, TableEntry tableEntry, int srid) {
    CallableStatement prepareCall = null;
    try {
      prepareCall = connection.prepareCall("{call addgeometrycolumn(?,?,?,?,?,true)}");
      prepareCall.setString(1, tableEntry.getTableName());
      prepareCall.setString(2, "GEOMETRY");
      prepareCall.setInt(3, srid);
      prepareCall.setString(4, tableEntry.getGeometryType());
      prepareCall.setInt(5, 2);
      prepareCall.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (prepareCall != null) {
        try {
          prepareCall.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * <p>
   * Method createSpatialIndex
   * </p>
   * 
   * @param connection
   * @param database
   * @param tableEntry
   */
  public static void createSpatialIndex(Connection connection, DBVendor database, TableEntry tableEntry) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      createSpatialIndex_PG(connection, tableEntry);
    } else if (database.equals(DBVendor.ORACLE)) {
      createSpatialIndex_ORA(connection, tableEntry);
    }
  }

  protected static void createSpatialIndex_ORA(Connection connection, TableEntry tableEntry) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("DROP INDEX " + tableEntry.getIndexName());
      connection.commit();
    } catch (SQLException e) {
      if (e.getErrorCode() != 1418) {
        e.printStackTrace();
      }
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + tableEntry.getIndexName() + " ON " + tableEntry.getTableName()
          + " (GEOMETRY) INDEXTYPE IS MDSYS.SPATIAL_INDEX");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void createSpatialIndex_PG(Connection connection, TableEntry tableEntry) {
    Statement stmnt = null;
    /*
     * try { stmnt = connection.createStatement(); stmnt.executeUpdate("DROP INDEX " + tableEntry.getIndexName());
     * connection.commit(); } catch (SQLException e) { e.printStackTrace(); try { connection.rollback(); } catch
     * (SQLException e1) { e1.printStackTrace(); } } finally { if (stmnt != null) { try { stmnt.close(); } catch
     * (SQLException e) { e.printStackTrace(); } } }
     */
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + tableEntry.getIndexName() + " ON " + tableEntry.getTableName()
          + " USING GIST(GEOMETRY)");
      connection.commit();
    } catch (SQLException e) {
      if (e.getErrorCode() != 0) {
        e.printStackTrace();
      }
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

  }

  public static void createNodeTable(Connection connection, DBVendor database, TableEntry tableEntry, String edgeTable,
                                     int srid) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      createNodeTable_PG(connection, tableEntry, edgeTable, srid);
    } else if (database.equals(DBVendor.ORACLE)) {
      createNodeTable_ORA(connection, tableEntry, edgeTable);
    }
  }

  private static void createNodeTable_PG(Connection connection, TableEntry tableEntry, String edgeTable, int srid) {
    DBUtility.dropTable_PG(connection, tableEntry.getTableName(), true);
    StringBuffer b = new StringBuffer();
    b.append("CREATE TABLE ").append(tableEntry.getTableName());
    b.append(" AS SELECT SOURCE AS ID, EDGE_MODE FROM ").append(edgeTable);
    b.append(" WHERE EDGE_MODE=1 UNION SELECT TARGET AS ID,EDGE_MODE FROM ").append(edgeTable);
    b.append(" WHERE EDGE_MODE=1");
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate(b.toString());
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    createMetaNodes_PG(connection, tableEntry.getTableName(), srid);

    /*
     * try { stmnt = connection.createStatement(); stmnt.executeUpdate("ALTER TABLE " + tableEntry.getTableName() +
     * " ADD COLUMN GEOMETRY geometry"); connection.commit(); } catch (SQLException e) { e.printStackTrace(); try {
     * connection.rollback(); } catch (SQLException e1) { e1.printStackTrace(); } } finally { if (stmnt != null) { try {
     * stmnt.close(); } catch (SQLException e) { e.printStackTrace(); } } }
     */

    String dulicateTable = "TMP_NODES_DUP";
    DBUtility.dropTable_PG(connection, dulicateTable, false);
    b = new StringBuffer();
    b.append("CREATE TABLE ").append(dulicateTable);
    b.append(" AS SELECT SOURCE AS ID,EDGE_MODE,ST_STARTPOINT(GEOMETRY) AS GEOMETRY FROM ").append(edgeTable);
    b.append(" WHERE EDGE_MODE=0 UNION ALL");
    b.append(" SELECT TARGET AS ID,EDGE_MODE,ST_ENDPOINT(GEOMETRY) AS GEOMETRY FROM ").append(edgeTable);
    b.append(" WHERE EDGE_MODE=0");
    stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate(b.toString());
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    String tmpNodes = "TMP_NODES";
    DBUtility.dropTable_PG(connection, tmpNodes, false);

    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE TABLE " + tmpNodes + " AS ("
          + "SELECT N.ID,N.EDGE_MODE,N.GEOMETRY, row_number() OVER (PARTITION by N.ID) RN FROM " + dulicateTable
          + " N)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("DELETE FROM " + tmpNodes + " WHERE RN>1");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("UPDATE " + tableEntry.getTableName() + " SET GEOMETRY=P.GEOMETRY FROM " + tmpNodes
          + " P WHERE " + tableEntry.getTableName() + ".ID=P.ID");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("INSERT INTO " + tableEntry.getTableName() + " (SELECT P.ID,P.EDGE_MODE,P.GEOMETRY FROM "
          + tmpNodes + " P WHERE P.ID NOT IN( SELECT ID FROM " + tableEntry.getTableName() + "))");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    DBUtility.dropTable_PG(connection, tmpNodes, false);
    DBUtility.dropTable_PG(connection, dulicateTable, false);
  }

  private static void createNodeTable_ORA(Connection connection, TableEntry tableEntry, String edgeTable) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("DROP INDEX " + tableEntry.getIndexName());
      connection.commit();
    } catch (SQLException e) {
      if (e.getErrorCode() != 1418) {
        e.printStackTrace();
      }
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + tableEntry.getIndexName() + " ON " + tableEntry.getTableName()
          + " (GEOMETRY) INDEXTYPE IS MDSYS.SPATIAL_INDEX");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * <p>
   * Method loadQueryPoints
   * </p>
   * 
   * @param connection
   * @param tableName
   * @return
   */
  public static Map<Integer, QueryPoint> loadQueryPoints(Connection connection, String tableName) {
    Map<Integer, QueryPoint> qPoints = new HashMap<Integer, QueryPoint>();
    DBResult dbResult = null;
    try {
      PreparedStatement statement = connection.prepareStatement("SELECT ID, OFFSET FROM " + tableName);
      dbResult = new DBResult(statement, statement.executeQuery());
      ResultSet resultSet = dbResult.getResultSet();
      while (resultSet.next()) {
        int linkId = resultSet.getInt("link_id");
        int offset = resultSet.getInt("offset");
        qPoints.put(linkId, new QueryPoint(linkId, offset));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      dbResult.close();
    }
    return qPoints;
  }

  /**
   * <p>
   * Method getNetworkSize
   * </p>
   * 
   * @param connection
   * @param tableName
   * @param mode
   * @return
   */
  public static int getNetworkSize(Connection connection, String tableName, Mode mode) {
    int totalSize = 0;
    StringBuffer buf = new StringBuffer();
    buf.append("SELECT COUNT(*) NODE_SIZE FROM (SELECT SOURCE FROM ");
    buf.append(tableName);
    buf.append(" UNION SELECT TARGET FROM ");
    buf.append(tableName).append(" )");
    PreparedStatement statement = null;
    ResultSet rSet = null;

    try {
      statement = connection.prepareStatement(buf.toString());
      rSet = statement.executeQuery();
      if (rSet.next()) {
        totalSize = rSet.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (rSet != null)
          rSet.close();
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return totalSize;
  }

  public static int getTotalRows(Connection connection, String tableName) {
    Statement statement = null;
    ResultSet resultSet = null;
    int totalRows = 0;
    try {
      statement = connection.createStatement();
      resultSet = statement.executeQuery("SELECT count(*) FROM " + tableName);
      if (resultSet.next()) {
        totalRows = resultSet.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (resultSet != null) {
        try {
          resultSet.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    return totalRows;
  }

  /**
   * <p>
   * Method getTotalVertexSize
   * </p>
   * counts the number of elements in the table
   * 
   * @param connection
   * @param tableName the name of the vertex table
   * @return
   */
  public static int getTotalVertexSize(Connection connection, String tableName, boolean excludeNullGeometry) {
    int totalSize = 0;
    StringBuffer buf = new StringBuffer();
    buf.append("SELECT COUNT(ID) FROM ").append(tableName);
    if(excludeNullGeometry){
      buf.append(" WHERE GEOMETRY IS NOT NULL");
    }
    PreparedStatement statement = null;
    ResultSet rSet = null;
    try {
      statement = connection.prepareStatement(buf.toString());
      rSet = statement.executeQuery();
      if (rSet.next()) {
        totalSize = rSet.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (rSet != null)
          rSet.close();
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return totalSize;
  }

  public static int getTotalVertexSizeOld(Connection connection, String edgeTable) {
    int totalSize = 0;
    StringBuffer buf = new StringBuffer();
    buf.append("SELECT COUNT(N.*) NODE_SIZE FROM (SELECT DISTINCT SOURCE FROM ");
    buf.append(edgeTable);
    buf.append(" UNION SELECT DISTINCT TARGET FROM ");
    buf.append(edgeTable).append(" ) N");
    PreparedStatement statement = null;
    ResultSet rSet = null;
    try {
      statement = connection.prepareStatement(buf.toString());
      rSet = statement.executeQuery();
      if (rSet.next()) {
        totalSize = rSet.getInt(1);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      try {
        if (rSet != null)
          rSet.close();
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return totalSize;
  }

  public static void createTargetEntityTable(Connection connection, DBVendor database, String tableName) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      createTargetEntityEdgeTable_PG(connection, tableName);
    } else if (database.equals(DBVendor.ORACLE)) {
      createTargetEntityEdgeTable_ORA(connection, tableName);
    }
  }

  public static void createTargetEntityEdgeTable_PG(Connection connection, String edgeTableName) {
    dropTable_PG(connection, edgeTableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE TABLE " + edgeTableName
          + " (\"ENTITY_ID\" integer NOT NULL, \"DISTANCE\" float, \"K_SEQ\" smallint)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void createTargetEntityEdgeTable_ORA(Connection connection, String edgeTableName) {
    dropTable_ORA(connection, edgeTableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt
          .executeUpdate("CREATE TABLE "
              + edgeTableName
              + " (\"ENTITY_ID\" NUMBER(10,0), \"DISTANCE\" NUMBER(8,4),\"K_SEQ\" NUMBER(5,0),\"GEOMETRY\" MDSYS.SDO_GEOMETRY)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * <p>
   * Method scanDummyTable
   * </p>
   * 
   * @param connection
   * @param tableName
   * @param runs
   */
  public static void scanDummyTable(Connection connection, String tableName, int runs) {
    System.out.println("Scanning dummy table " + runs + "-times  to empty the cache...");
    for (int i = 0; i < runs; i++) {
      Statement stmnt = null;
      ResultSet resultSet = null;
      try {
        stmnt = connection.createStatement();
        stmnt.setFetchSize(500);
        resultSet = stmnt.executeQuery("SELECT * FROM " + tableName);
        while (resultSet.next()) {
          resultSet.getInt(1);
        }
        // resultSet = stmnt.executeQuery("SELECT * FROM " + tableName + " ORDER BY RANDOM() LIMIT 1");
      } catch (SQLException e) {
        e.printStackTrace();
        try {
          connection.rollback();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      } finally {
        if (stmnt != null) {
          try {
            if (resultSet != null)
              resultSet.close();
            stmnt.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }
    }
    System.out.println("End dummy table scan.");
  }

  /**
   * <p>
   * Method getAllVertices returns all the vertices in the given table
   * </p>
   * 
   * @param connection
   * @param vertexTable
   * @return
   * @throws SQLException
   */
  public static Collection<Integer> getAllVertices(Connection connection, String vertexTable) throws SQLException {
    List<Integer> vertices = new ArrayList<Integer>();
    StringBuffer buf = new StringBuffer();
    buf.append("SELECT ID FROM ").append(vertexTable).append(" WHERE GEOMETRY IS NOT NULL ORDER BY GEOMETRY");
    PreparedStatement statement = null;
    ResultSet rSet = null;
    try {
      statement = connection.prepareStatement(buf.toString());
      rSet = statement.executeQuery();
      while (rSet.next()) {
        vertices.add(rSet.getInt(1));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (rSet != null)
          rSet.close();
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return vertices;
  }

  /**
   * <p>
   * Method getDensityTuples
   * </p>
   * 
   * @param connection
   * @param vertexTable
   * @param nodeId
   * @return
   * @throws SQLException
   */
  public static DBResult getDensityTuples(Connection connection, String vertexTable, int nodeId, double maxRange) throws SQLException {
    StringBuffer buf = new StringBuffer();
    buf.append("SELECT ST_DISTANCE(V.GEOMETRY,Q.GEOMETRY) e_dist FROM ").append(vertexTable).append(" V, ");
    buf.append(vertexTable).append(" Q");
    buf.append(" WHERE Q.ID=? AND V.GEOMETRY IS NOT NULL AND ST_DWITHIN(V.GEOMETRY,Q.GEOMETRY,?) ORDER BY ST_DISTANCE(V.GEOMETRY,Q.GEOMETRY) ");

    PreparedStatement stmt = connection.prepareStatement(buf.toString());
    stmt.setInt(1, nodeId);
    stmt.setDouble(2, maxRange);
    return new DBResult(stmt, stmt.executeQuery());
  }
  
 
  /**
   * 
   * <p>Method getBestpossibleRange</p>
   * @param connection
   * @param vertexTable
   * @param nodeId
   * @param maxRowSize
   * @param maxRange
   * @return
   * @throws SQLException
   */
  public static double getBestpossibleRange(Connection connection, String vertexTable, int nodeId, int maxRowSize, double maxRange, int percentage) throws SQLException {
    StringBuffer buf = new StringBuffer();
    buf.append("SELECT count(*) FROM ").append(vertexTable).append(" V, ");
    buf.append(vertexTable).append(" Q");
    buf.append(" WHERE Q.ID=? AND V.GEOMETRY IS NOT NULL AND ST_DWITHIN(V.GEOMETRY,Q.GEOMETRY,?) ");
    
    double range = maxRange;
    int rowSize = 0;
    
    PreparedStatement stmt = connection.prepareStatement(buf.toString());
    stmt.setInt(1, nodeId);
    stmt.setDouble(2, range);
    ResultSet rset = stmt.executeQuery();
    
    if(rset.next()){
      rowSize = rset.getInt(1);
    }
    
    if(rset!=null){
      rset.close();
    }
    
    if(stmt!=null){
      stmt.close();
    }
    
    double perc = percentage/ 100d;
    
    short tries = 1, adpatDecrement=0, adaptIncrement=0;
    
    // we adapt range as long it is not in the range of 10% of the rowsize
    while(rowSize<maxRowSize-maxRowSize*perc || rowSize>maxRowSize+maxRowSize*perc) {
      if(rowSize<maxRowSize){
        range += range*perc;
        adaptIncrement++;
      } else {
        range -= range*perc;
        adpatDecrement++;
      }
      // System.out.println("Adapting range to: " + range);
      stmt = connection.prepareStatement(buf.toString());
      stmt.setInt(1, nodeId);
      stmt.setDouble(2, range);
      rset = stmt.executeQuery();
      if(rset.next()){
        rowSize = rset.getInt(1);
      }
      
      if(rset!=null){
        rset.close();
      }
      
      if(stmt!=null){
        stmt.close();
      }
      ++tries;
    }
    if(tries>1){
      System.out.println("Tries:" + tries + "\t increment:" + adaptIncrement + "\t decrement:" + adpatDecrement);
    }
    
    return range;
  }

  /**
   * 
   * <p>Method createDensityTable</p>
   * @param connection
   * @param densityTable
   */
  public static void createDensityTable(Connection connection, String densityTable) {
    dropTable_PG(connection, densityTable, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE TABLE " + densityTable
          + " (id integer NOT NULL, density integer NOT NULL, e_dist double precision NOT NULL)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 
   * <p>Method createDensityTable_Index</p>
   * @param connection
   * @param densityTable
   */
  public static void createDensityTable_Index(Connection connection, String densityTable) {
    String indexName = "IDX_" + densityTable;
    DBUtility.dropIndex(connection, indexName);

    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE INDEX " + indexName + " ON " + densityTable + " USING btree(ID)");
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static DBResult getAllVerticesSortedByX(Connection connection, String vertexTable) throws SQLException {
    StringBuffer buf = new StringBuffer();
    buf.append("SELECT ID,ST_X(GEOMETRY) X,ST_Y(GEOMETRY) Y FROM ").append(vertexTable).append(" V ");
    buf.append("WHERE GEOMETRY IS NOT NULL ");
    buf.append("ORDER BY ST_X(GEOMETRY)");
    PreparedStatement stmt = connection.prepareStatement(buf.toString());
    return new DBResult(stmt, stmt.executeQuery());
  }
  
  

}
