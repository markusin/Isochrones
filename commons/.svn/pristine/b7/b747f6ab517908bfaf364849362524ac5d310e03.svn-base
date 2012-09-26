package isochrones.tools;

import isochrones.algorithm.Mode;
import isochrones.algorithm.TableEntry;
import isochrones.utils.Config;
import isochrones.utils.DBUtility;
import isochrones.utils.SetupUtil;

import java.util.logging.Logger;

public class SetupTool {

  private Config config;
  protected static final Logger LOGGER = Logger.getLogger(SetupTool.class.getPackage().getName());

  public SetupTool(Config config) {
    this.config = config;
  }

  public void initResultTable(){
    TableEntry isoLinksUM = config.getDestinationEdgeTableEntry();
    LOGGER.info("Creating result table: " + isoLinksUM.getTableName());
    DBUtility.createTargetEdgeTable(config.getConnection(), config.getDbVendor(), isoLinksUM.getTableName());
    DBUtility.insertGeometryMetadata(config.getConnection(), isoLinksUM, config.getDbVendor(),config.getServerSRID());
    DBUtility.createSpatialIndex(config.getConnection(), config.getDbVendor(), isoLinksUM);
    
    /*
    TableEntry isoEntity = config.getDestinationEntityTableEntry();
    LOGGER.info("Creating entity table: " + isoEntity.getTableName());
    DBUtility.createTargetEntityTable(config.getConnection(), config.getDbVendor(), isoEntity.getTableName());
    DBUtility.insertGeometryMetadata(config.getConnection(), isoEntity, config.getDbVendor(),config.getServerSRID());
    DBUtility.createSpatialIndex(config.getConnection(), config.getDbVendor(), isoEntity);
    */
  }

  public void ensureIndices() {
    LOGGER.info("Checking indexes...");
    DBUtility.createEdgeIndices(config.getConnection(), config.getDbVendor(), config.getEdgeTable(), config.getMode());
    if (config.getMode().equals(Mode.MULTIMODAL)) {
      DBUtility.createScheduleIndices(config.getConnection(), config.getDbVendor(), config.getScheduleTable());
    }
  }

  /**
   * 
   * <p>Method main</p>
   * @param args
   */
  public static void main(String[] args) {
    SetupUtil util = new SetupUtil(args, '=');
    SetupTool setupTool = new SetupTool(util.getConfig());
    LOGGER.info("Database setup with following parameters");
    LOGGER.info("Database vendor: " + util.getDBVendor());
    LOGGER.info("Dataset: " + util.getDataset());
    LOGGER.info("Index check enabled: " + util.isIndexCheck());
    setupTool.initResultTable();
    if (util.isIndexCheck()) {
      setupTool.ensureIndices();
    }
  }

}
