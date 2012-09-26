package isochrones.web.utils;

import isochrones.algorithm.TableEntry;
import isochrones.db.DBResult;
import isochrones.db.DBVendor;
import isochrones.web.config.Config;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class DBUtility extends isochrones.utils.DBUtility {

  /**
   * <p>
   * Method getAllEntries
   * </p>
   * 
   * @param connection
   * @param sessionTable
   * @return
   */
  public static Collection<SessionEntry> getAllSessionEntries(Connection connection, String sessionTable) {
    Collection<SessionEntry> sessionEntries = new ArrayList<SessionEntry>();
    DBResult dbResult = null;
    try {
      PreparedStatement statement = connection
          .prepareStatement("SELECT SESSION_ID,EDGE_TABLE,EDGE_LAYER,VERTEX_TABLE,VERTEX_LAYER,VERTEX_ANNOTATED_TABLE,BUFFER_TABLE,BUFFER_LAYER FROM "
              + sessionTable);
      dbResult = new DBResult(statement, statement.executeQuery());
      ResultSet rSet = dbResult.getResultSet();
      while (rSet.next()) {
        String sessionId = rSet.getString("SESSION_ID");
        String edgeTablename = rSet.getString("EDGE_TABLE");
        String edgeLayer = rSet.getString("EDGE_LAYER");
        String vertexTablename = rSet.getString("VERTEX_TABLE");
        String vertexLayer = rSet.getString("VERTEX_LAYER");
        String areaBufferTablename = rSet.getString("BUFFER_TABLE");
        String areaBufferLayer = rSet.getString("BUFFER_LAYER");
        String vertexAnnotatedTablename = rSet.getString("VERTEX_ANNOTATED_TABLE");

        sessionEntries.add(new SessionEntry(sessionId, edgeTablename, edgeLayer, vertexTablename, vertexLayer,
                                            vertexAnnotatedTablename, areaBufferTablename, areaBufferLayer));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      dbResult.close();
    }
    return sessionEntries;
  }

  public static void addSessionEntry(Connection connection, SessionEntry sessionEntry, String sessionTable) {
    DBResult dbResult = null;
    try {
      PreparedStatement stmt = connection
          .prepareStatement("INSERT INTO "
              + sessionTable
              + "(SESSION_ID,EDGE_TABLE,EDGE_LAYER,VERTEX_TABLE,VERTEX_LAYER,BUFFER_TABLE,BUFFER_LAYER) VALUES (?,?,?,?,?,?,?) ");
      stmt.setString(1, sessionEntry.getSessionId());
      stmt.setString(2, sessionEntry.getEdgeTableName());
      stmt.setString(3, sessionEntry.getEdgeLayerName());
      stmt.setString(4, sessionEntry.getVertexTableName());
      stmt.setString(5, sessionEntry.getVertexLayerName());
      stmt.setString(6, sessionEntry.getVertexTableName());
      stmt.setString(7, sessionEntry.getVertexLayerName());
      stmt.executeUpdate();
      dbResult = new DBResult(stmt);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      dbResult.close();
    }
  }

  public static void createTargetVertexTable(Connection connection, DBVendor database, String tableName) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      createTargetVertexTable_PG(connection, tableName);
    } else if (database.equals(DBVendor.ORACLE)) {
      createTargetVertexTable_ORA(connection, tableName);
    }
  }

  public static void createTargetVertexTable_PG(Connection connection, String tableName) {
    // dropTable_PG(connection, tableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE TABLE " + tableName
          + " (\"ID\" integer NOT NULL, \"DISTANCE\" double precision, \"ROUTE_ID\" smallint, \"STATE\" VARCHAR(7), \"T_TYPE\" smallint)");
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

  public static void createTargetVertexTable_ORA(Connection connection, String tableName) {
    dropTable_ORA(connection, tableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE TABLE " + tableName
          + " (\"ID\" NUMBER(10,0) NOT NULL, \"DISTANCE\" NUMBER, \"ROUTE_ID\" NUMBER(5,0), \"STATE\" VARCHAR(10),\"T_TYPE\" NUMBER(2,0),"
          + "\"GEOMETRY\" MDSYS.SDO_GEOMETRY, CONSTRAINT pkey_" + tableName + " PRIMARY KEY (\"ID\"))");
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

  public static void createTargetBufferTable(Connection connection, DBVendor dbVendor, String tableName) {
    if (dbVendor.equals(DBVendor.POSTGRESQL)) {
      createTargetBufferTable_PG(connection, tableName);
    } else if (dbVendor.equals(DBVendor.ORACLE)) {
      createTargetBufferTable_ORA(connection, tableName);
    }
  }

  public static void createTargetBufferTable_PG(Connection connection, String tableName) {
    //dropTable_PG(connection, tableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE TABLE " + tableName + " (\"ID\" integer NOT NULL, CONSTRAINT pkey_"
          + tableName + " PRIMARY KEY (\"ID\"))");
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

  public static void createTargetBufferTable_ORA(Connection connection, String tableName) {
    dropTable_ORA(connection, tableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      
      /*stmnt.executeUpdate("CREATE TABLE " + tableName
          + " (\"ID\" NUMBER(10,0) NOT NULL,\"GEOMETRY\" MDSYS.SDO_GEOMETRY, CONSTRAINT pkey_" + tableName
          + " PRIMARY KEY (\"ID\"))");
      */
      stmnt.executeUpdate("CREATE TABLE " + tableName
          + " (\"ID\" NUMBER(10,0) NOT NULL,\"GEOMETRY\" MDSYS.SDO_GEOMETRY)");
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

  public static void createVertexAnnotationTable(Connection connection, DBVendor dbVendor, String tableName) {
    if (dbVendor.equals(DBVendor.POSTGRESQL)) {
      createVertexAnnotationTable_PG(connection, tableName);
    } else if (dbVendor.equals(DBVendor.ORACLE)) {
      createVertexAnnotationTable_ORA(connection, tableName);
    }
  }

  public static void createVertexAnnotationTable_PG(Connection connection, String tableName) {
    dropTable_PG(connection, tableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE TABLE " + tableName
          + " (\"ID\" integer NOT NULL, \"ROUTE_ID\" smallint,\"TIME_A\" integer,\"TIME_D\" integer)");
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

  public static void createVertexAnnotationTable_ORA(Connection connection, String tableName) {
    dropTable_ORA(connection, tableName, true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("CREATE TABLE " + tableName
          + " (\"ID\" NUMBER(10,0) NOT NULL, \"ROUTE_ID\" NUMBER(5,0), \"TIME_A\" NUMBER(10,0),\"TIME_D\" NUMBER(10,0))");
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

  public static void dropAllResultTables(Connection connection, DBVendor database, String regex) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      dropAllResultTables_PG(connection, regex);
    } else if (database.equals(DBVendor.ORACLE)) {
      dropAllResultTables_ORA(connection, regex);
    }
  }

  private static void dropAllResultTables_PG(Connection connection, String regex) {

    CallableStatement prepareCall = null;
    try {
      prepareCall = connection.prepareCall("{call dropTables('public', '%" + regex + "%')}");
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
    /*
     * Statement stmnt = null; try { stmnt = connection.createStatement();
     * stmnt.executeUpdate("SELECT dropTables('public', '%" + regex + "%')"); connection.commit(); } catch (SQLException
     * e) { e.printStackTrace(); try { connection.rollback(); } catch (SQLException e1) { e1.printStackTrace(); } }
     * finally { if (stmnt != null) { try { stmnt.close(); } catch (SQLException e) { e.printStackTrace(); } } }
     */

  }

  private static void dropAllResultTables_ORA(Connection connection, String regex) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("select DROPTABLES('" + regex + "') from DUAL");
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

  private static void dropAllResultTables_ORA1(Connection connection, String regex) {
    CallableStatement prepareCall = null;
    try {
      prepareCall = connection.prepareCall("{call DROPTABLES(?)}");
      prepareCall.setString(1, regex);
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

  public static void insertGeometryMetadata(Connection connection, TableEntry tableEntry, DBVendor database, int srid) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      insertGeometryMetadata_PG(connection, tableEntry, srid);
    } else if (database.equals(DBVendor.ORACLE)) {
      insertGeometryMetadata_ORA(connection, tableEntry, srid);
    }
  }

  public static void deleteGeometryMetadata(Connection connection, TableEntry tableEntry, DBVendor database) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      deleteGeometryMetadata_PG(connection, tableEntry.getTableName());
    } else if (database.equals(DBVendor.ORACLE)) {
      deleteGeometryMetadata_ORA(connection, tableEntry.getTableName());
    }
  }

  protected static void insertGeometryMetadata_PG(Connection connection, TableEntry tableEntry, int srid) {
    CallableStatement prepareCall = null;
    try {
      prepareCall = connection.prepareCall("{call AddGeometryColumn(?,?,?,?,?,?)}");
      prepareCall.setString(1, tableEntry.getTableName());
      prepareCall.setString(2, "GEOMETRY");
      prepareCall.setInt(3, srid);
      prepareCall.setString(4, tableEntry.getGeometryType());
      prepareCall.setInt(5, 2);
      prepareCall.setBoolean(6, true);
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
   * Method deleteGeometryMetadata_ORA
   * </p>
   * 
   * @param connection
   * @param tableName
   */
  public static void deleteGeometryMetadata_ORA(Connection connection, String tableName) {
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt.executeUpdate("DELETE MDSYS.USER_SDO_GEOM_METADATA WHERE UPPER(TABLE_NAME)='" + tableName.toUpperCase()
          + "'");
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
   * Method deleteGeometryMetadata_PG
   * </p>
   * 
   * @param connection
   * @param tableName
   */
  public static void deleteGeometryMetadata_PG(Connection connection, String tableName) {
    CallableStatement prepareCall = null;
    try {
      prepareCall = connection.prepareCall("{call DropGeometryTable(?)}");
      prepareCall.setString(1, tableName);
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
   * Method getVertexAnnotation
   * </p>
   * returns the annotated information of a specified vertex
   * 
   * @param config
   * @param vertexId
   * @return
   * @throws SQLException
   */
  public static DBResult getVertexAnnotation(Config config, int vertexId) throws SQLException {
    if (config.getDbVendor().equals(DBVendor.POSTGRESQL)) {
      return getVertexAnnotation_PG(config, vertexId);
    } else if (config.getDbVendor().equals(DBVendor.ORACLE)) {
      return getVertexAnnotation_ORA(config, vertexId);
    }
    return null;
  }

  private static DBResult getVertexAnnotation_PG(Config config, int vertexId) throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT to_char(to_timestamp(CAST(\"TIME_A\" AS text),'SSSSS'),'HH24:MI') \"ARRIVAL_TIME\", to_char(to_timestamp(CAST(\"TIME_D\" AS text),'SSSSS'),'HH24:MI') \"DEPARTURE_TIME\", R.ROUTE_SHORT_NAME \"ROUTE_SHORT_NAME\", R.ROUTE_TYPE \"ROUTE_TYPE\" FROM ");
    sb.append(config.getDestinationVertexAnnotatedTableEntry().getTableName()).append(" V, ")
        .append(config.getRouteTable());
    sb.append(" R WHERE V.\"ID\"=? AND V.\"ROUTE_ID\"=R.ROUTE_ID ORDER BY ");
    sb.append(config.isIncoming() ? "V.\"TIME_D\" DESC" : "V.\"TIME_A\" ASC");
    PreparedStatement statement = config.getConnection().prepareStatement(sb.toString());
    statement.setInt(1, vertexId);
    return new DBResult(statement, statement.executeQuery());
  }

  private static DBResult getVertexAnnotation_ORA(Config config, int vertexId) throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT TO_CHAR(TO_TIMESTAMP(CAST(\"TIME_A\" AS VARCHAR2(5)),'SSSSS'),'HH24:MI') \"ARRIVAL_TIME\", TO_CHAR(TO_TIMESTAMP(CAST(\"TIME_D\" AS VARCHAR2(5)),'SSSSS'),'HH24:MI') \"DEPARTURE_TIME\", R.ROUTE_SHORT_NAME \"ROUTE_SHORT_NAME\", R.ROUTE_TYPE \"ROUTE_TYPE\" FROM ");
    sb.append(config.getDestinationVertexAnnotatedTableEntry().getTableName()).append(" V, ")
        .append(config.getRouteTable());
    sb.append(" R WHERE V.\"ID\"=? AND V.\"ROUTE_ID\"=R.ROUTE_ID ORDER BY ");
    sb.append(config.isIncoming() ? "V.\"TIME_D\" DESC" : "V.\"TIME_A\" ASC");
    PreparedStatement statement = config.getConnection().prepareStatement(sb.toString());
    statement.setInt(1, vertexId);
    return new DBResult(statement, statement.executeQuery());
  }

}
