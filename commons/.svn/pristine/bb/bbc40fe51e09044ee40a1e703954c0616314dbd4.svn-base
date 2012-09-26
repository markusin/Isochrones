package isochrones.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The <code>DBResult</code> class allows to construct objects which consist of an SQL <code>Statement</code> and an
 * <code>ResultSet</code>. These objects can be created by one class and then passed to another for iteration.
 * When the destination class calls the <code>close()</code> method on the <code>DBResult</code> object, the statement
 * and the result set will be closed.
 * <p>
 * IMPORTANT NOTE: as for usual result sets, also the <code>DBResult</code> has to be CLOSED after utilization.
 * 
 * @author Willi Cometti
 * @author Markus Innerebner
 * @version 2.0
 */
public class DBResult {

  /**
   * the SQL statement
   */
  private Statement statement;
  /**
   * the result set
   */
  private ResultSet resultSet;

  /**
   * Class constructor that creates a <code>DBResult</code> object consisting of an SQL statement and an Oracle result
   * set.
   * 
   * @param statement the SQL statement
   * @param resultSet the Oracle result set obtained by the <code>
   *                     statement</code> execution
   */
  public DBResult(Statement statement, ResultSet resultSet) {
    this.statement = statement;
    this.resultSet = resultSet;
  }
  
  public DBResult(Statement statement) {
    this.statement = statement;
  }

  /**
   * Returns the result set.
   * 
   * @return the result set
   */
  public ResultSet getResultSet() {
    return resultSet;
  }

  /**
   * Closes the statement and the result set contained in this <code>DBResult</code> object.
   * 
   */
  public void close() {
    try {
      if(resultSet !=null) resultSet.close();
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void finalize() throws SQLException {
    close();
  }
}
