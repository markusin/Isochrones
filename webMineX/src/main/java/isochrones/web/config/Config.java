/**
 * 
 */
package isochrones.web.config;

import isochrones.algorithm.TableEntry;
import isochrones.algorithm.TableType;
import isochrones.db.DBVendor;

import java.io.InputStream;

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
public class Config extends isochrones.utils.Config {

  protected TableEntry destinationAreaBufferTableEntry,destinationVertexTableEntry,destinationVertexAnnotatedTableEntry;
  private boolean enableAreaCalculation = false, expirationMode = false;

  /**
   * <p>
   * Constructs a(n) <code>Config</code> object.
   * </p>
   * 
   * @param inputStream
   */
  public Config(DBVendor dbVendor,InputStream inputStream) {
    super(dbVendor,inputStream);
  }

  /**
   * <p>
   * Constructs a(n) <code>Config</code> object.
   * </p>
   * 
   * @param config
   */
  public Config(Config config) {
    super(config);
  }

  @Override
  public void appendPropertyFile(InputStream inputStream) {
    super.appendPropertyFile(inputStream);
    destinationAreaBufferTableEntry = new TableEntry(getProperty("tbl.isoAreaBuffer"),
                                                     getProperty("idx.isoAreaBuffer"), TableType.POLYGON_BUFFER);
    destinationVertexTableEntry = new TableEntry(getProperty("tbl.isoNodes"), getProperty("idx.isoNodes"),
        TableType.NODE);
    destinationVertexAnnotatedTableEntry = new TableEntry(getProperty("tbl.isoNodesAnnotations"));
  }

  public void setExpirationMode(boolean expirationMode) {
    this.expirationMode = expirationMode;
  }

  /**
   * <p>
   * Method isExpirationMode
   * </p>
   * 
   * @return
   */
  public boolean isExpirationMode() {
    return expirationMode;
  }
  
  /**
   * 
   * <p>Method enableAreaCalculation</p>
   * @return
   */
  public boolean enableAreaCalculation() {
    return enableAreaCalculation;
  }
  
  /**
   * 
   * <p>Method setEnableAreaCalculation</p>
   * @param enableAreaCalculation
   */
  public void setEnableAreaCalculation(boolean enableAreaCalculation) {
    this.enableAreaCalculation = enableAreaCalculation;
  }
  

  public void setDestinationAreaBufferTableEntry(TableEntry destinationAreaBufferTableEntry) {
    this.destinationAreaBufferTableEntry = destinationAreaBufferTableEntry;
  }

  public TableEntry getDestinationAreaBufferTableEntry() {
    return destinationAreaBufferTableEntry;
  }

  public TableEntry getDestinationVertexTableEntry() {
    return destinationVertexTableEntry;
  }

  public void setDestinationVertexTableEntry(TableEntry destinationVertexTableEntry) {
    this.destinationVertexTableEntry = destinationVertexTableEntry;
  }
  
  public void setDestinationVertexAnnotatedTableEntry(TableEntry vertexAnnotatedTableEntry) {
    this.destinationVertexAnnotatedTableEntry = vertexAnnotatedTableEntry;
  }

  public TableEntry getDestinationVertexAnnotatedTableEntry() {
    return destinationVertexAnnotatedTableEntry;
  }

}
