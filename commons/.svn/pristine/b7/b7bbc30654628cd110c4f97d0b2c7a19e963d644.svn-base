package isochrones.algorithm;

public class TableEntry {

  private String indexName;
  private String tableName;
  private String description;
  private TableType type;

  /**
   * <p>
   * Constructs a(n) <code>TableEntry</code> object.
   * </p>
   */
  public TableEntry(String tableName) {
    this.tableName = tableName.toLowerCase();
  }

  /**
   * <p>
   * Constructs a(n) <code>TableEntry</code> object.
   * </p>
   * 
   * @param tableName
   * @param indexName
   */
  public TableEntry(String tableName, String indexName, TableType type) {
    this.tableName = tableName;
    this.indexName = indexName;
    this.description = type.toString();
    this.type = type;
  }

  public String getTableName() {
    return tableName;
  }

  public String getIndexName() {
    return indexName;
  }

  public String getDescription() {
    return description;
  }

  public TableType getType() {
    return type;
  }

  public String getGeometryType() {
    switch (type) {
      case NODE:
        return "POINT";
      case LINK:
        return "LINESTRING";
      case LINK_BUFFER:
      case POLYGON_BUFFER:
        return "POLYGON";
      default:
        return "POINT";
    }
  }

  @Override
  public String toString() {
    return tableName + ", " + indexName;
  }

}
