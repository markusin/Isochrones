package isochrones.mrnex.utils;

import isochrones.algorithm.TableEntry;
import isochrones.algorithm.TableType;
import isochrones.db.DBVendor;
import isochrones.utils.SetupUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtility extends isochrones.utils.DBUtility {
  
  public static void createTargetLogLoadedRangeTable(Connection connection, DBVendor database, TableEntry tableEntry, int srid) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      createTargetLogLoadedRangeTable_PG(connection, tableEntry,srid);
    } else if (database.equals(DBVendor.ORACLE)) {
      createTargetLogLoadedRangeTable_ORA(connection, tableEntry,srid);
    }
  }

  private static void createTargetLogLoadedRangeTable_ORA(Connection connection, TableEntry tableEntry, int srid) {
    dropTable_ORA(connection, tableEntry.getTableName(), true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt
          .executeUpdate("CREATE TABLE "
              + tableEntry.getTableName()
              + " (NODE_ID NUMBER(10,0),INSERT_SEQ NUMBER(5,0), MAX_RADIUS NUMBER(10,4), RADIUS NUMBER(10,4), GEOMETRY MDSYS.SDO_GEOMETRY)");
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
    DBUtility.insertGeometryMetadata_ORA(connection, tableEntry, srid);
    DBUtility.createSpatialIndex_ORA(connection, tableEntry);
  }

  private static void createTargetLogLoadedRangeTable_PG(Connection connection, TableEntry tableEntry, int srid) {
    dropTable_PG(connection, tableEntry.getTableName(), true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt
          .executeUpdate("CREATE TABLE "
              + tableEntry.getTableName()
              + " (NODE_ID integer NOT NULL, INSERT_SEQ integer, MAX_RADIUS float, RADIUS float)");
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
    
    DBUtility.insertGeometryMetadata_PG(connection, tableEntry, srid);
  }
  
  public static void createTargetLogNodesLoadedWithRange(Connection connection, DBVendor database, TableEntry tableEntry, int srid) {
    if (database.equals(DBVendor.POSTGRESQL)) {
      createTargetLogNodesLoadedWithRange_PG(connection, tableEntry,srid);
    } else if (database.equals(DBVendor.ORACLE)) {
      createTargetLogNodesLoadedWithRange_ORA(connection, tableEntry,srid);
    }
  }
  
  private static void createTargetLogNodesLoadedWithRange_ORA(Connection connection, TableEntry tableEntry, int srid) {
    dropTable_ORA(connection, tableEntry.getTableName(), true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt
          .executeUpdate("CREATE TABLE "
              + tableEntry.getTableName()
              + " (NODE_ID NUMBER(10,0),DISTANCE NUMBER(10,4), REMAINING_DISTANCE NUMBER(10,4), GEOMETRY MDSYS.SDO_GEOMETRY)");
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
    DBUtility.insertGeometryMetadata_ORA(connection, tableEntry, srid);
    DBUtility.createSpatialIndex_ORA(connection, tableEntry);
  }
  
  private static void createTargetLogNodesLoadedWithRange_PG(Connection connection, TableEntry tableEntry, int srid) {
    dropTable_PG(connection, tableEntry.getTableName(), true);
    Statement stmnt = null;
    try {
      stmnt = connection.createStatement();
      stmnt
          .executeUpdate("CREATE TABLE "
              + tableEntry.getTableName()
              + " (NODE_ID integer NOT NULL, DISTANCE float, REMAINING_DISTANCE float)");
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
    DBUtility.insertGeometryMetadata_PG(connection, tableEntry, srid);
  }
  
  public static void main(String[] args) {
    SetupUtil util = new SetupUtil(args, '=');
    System.out.println("Creating table " + util.getConfig().getProperty("tbl.log_circles"));
    TableEntry tableEntry = new TableEntry(util.getConfig().getProperty("tbl.log_circles"),util.getConfig().getProperty("idx.log_circles"),TableType.POLYGON_BUFFER);
    DBUtility.createTargetLogLoadedRangeTable(util.getConfig().getConnection(), util.getConfig().getDbVendor(), tableEntry, util.getConfig().getServerSRID());
    System.out.println("Creating table " + util.getConfig().getProperty("tbl.log_nodes"));
    tableEntry = new TableEntry(util.getConfig().getProperty("tbl.log_nodes"),util.getConfig().getProperty("idx.log_nodes"),TableType.NODE);
    DBUtility.createTargetLogNodesLoadedWithRange(util.getConfig().getConnection(), util.getConfig().getDbVendor(), tableEntry, util.getConfig().getServerSRID());
    
    
  }

}
