package isochrones.web.services;

import isochrones.algorithm.Dataset;
import isochrones.algorithm.Direction;
import isochrones.algorithm.Mode;
import isochrones.db.DBResult;
import isochrones.db.DBVendor;
import isochrones.web.config.Config;
import isochrones.web.config.DSetConfig;
import isochrones.web.coverage.ReachabilityTool;
import isochrones.web.coverage.Statistics;
import isochrones.web.db.IWebQuery;
import isochrones.web.db.OracleQuery;
import isochrones.web.db.PostgresQuery;
import isochrones.web.geometry.BBox;
import isochrones.web.geometry.Point;
import isochrones.web.minex.algorithm.MineX;
import isochrones.web.network.node.QueryPoint;
import isochrones.web.network.node.WebNode;
import isochrones.web.utils.DBUtility;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTLayerList;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSPostGISDatastoreEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.AbstractService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * The <code>WebService</code> class
 * </p>
 * is a Singleton Class called by Cometd for processing http request with long polling
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
public class WebService extends AbstractService {

  protected static final Logger LOGGER = Logger.getLogger(WebService.class.getPackage().getName());
  public static DateFormat dateFormat = new SimpleDateFormat("MM'/'dd'/'yyyy' 'HH':'mm");
  public static final String FILE_SEP = System.getProperty("file.separator");

  private Config globalCFG;
  private Map<Dataset, DSetConfig> dSetClientConfigs = new HashMap<Dataset, DSetConfig>();

  private Map<String, Statistics> stats = new HashMap<String, Statistics>();
  private Map<String, Map<Dataset, DSetConfig>> sessionServerConfigs = new HashMap<String, Map<Dataset, DSetConfig>>();
  private Map<String, Config> configs = new HashMap<String, Config>();
  private GeoServerRESTPublisher publisher;
  private GeoServerRESTReader reader;

  /**
   * <p>
   * Constructs a(n) <code>IsochroneService</code> object.
   * </p>
   * This method is invoked only once when cometd is started.
   * 
   * @param bayeux
   */
  public WebService(BayeuxServer bayeux) {
    super(bayeux, "isochrone");
    LOGGER.fine("Initializing Isochrone Service.");
    addService("/service/cfg", "processCfgRequest");
    addService("/service/iso", "processIsochroneRequest");
    addService("/service/inhabitants", "processInhabitantsRequest");
    addService("/service/showRouteDetails", "processShowRoutesRequest");
    InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("etc/config.xml");
    globalCFG = new Config(null, resourceAsStream);
    globalCFG.setOutputWriting(true);
    publisher = new GeoServerRESTPublisher(globalCFG.getProperty("rendering.server.rest.url"),
                                           globalCFG.getProperty("rendering.server.rest.username"),
                                           globalCFG.getProperty("rendering.server.rest.password"));

    try {
      reader = new GeoServerRESTReader(globalCFG.getProperty("rendering.server.rest.url"),
                                       globalCFG.getProperty("rendering.server.rest.username"),
                                       globalCFG.getProperty("rendering.server.rest.password"));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    init();
  }

  /**
   * <p>
   * Method init
   * </p>
   * Deletes session entries that where not unregistered. Initializes the configurations for each dataset
   * 
   * @param globalCfg
   * @param cfgPath
   */
  private void init() {
    String[] dataSets = globalCFG.getProperty("cfg.datasets").split(",");
    Config config = new Config(globalCFG);
    
    cleanupLayersAndTables();
    
    // TODO delete all registered session tables and session layers
    // Collection<SessionEntry> allEntries = DBUtility.getAllSessionEntries(config.getConnection(),
    // config.getProperty("tbl.session"));

    /*
     * publisher.removeDatastore(globalCFG.getProperty("rendering.server.rest.workspace"),
     * globalCFG.getProperty("rendering.server.rest.datastore"));
     */

    // createPostgisDatastore();

    /*
     * for (SessionEntry sessionEntry : allEntries) { unregisterLayer(config.getConnection(),
     * sessionEntry.getEdgeTableName(), sessionEntry.getEdgeLayerName(), publisher);
     * unregisterLayer(config.getConnection(), sessionEntry.getVertexTableName(), sessionEntry.getVertexLayerName(),
     * publisher); unregisterLayer(config.getConnection(), sessionEntry.getBufferTableName(),
     * sessionEntry.getEdgeLayerName(), publisher); }
     */
    // DBUtility.truncateTable(config.getConnection(), config.getProperty("tbl.session"));

    // iterate over each dataset
    for (String dSet : dataSets) {
      Dataset dataSet = Dataset.valueOf(dSet.toUpperCase());
      if (dataSet != null) {
        String fileName = "etc/config_" + dataSet.toString().toLowerCase() + ".xml";
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        config.appendPropertyFile(resourceAsStream);
        IWebQuery query = config.getDbVendor().equals(DBVendor.ORACLE) ? new OracleQuery(config, false)
            : new PostgresQuery(config, false);

        Point lowerCorner = new Point(Double.parseDouble(config.getProperty("sql.spatial.dim1.lower")),
                                      Double.parseDouble(config.getProperty("sql.spatial.dim2.lower")));
        Point upperCorner = new Point(Double.parseDouble(config.getProperty("sql.spatial.dim1.upper")),
                                      Double.parseDouble(config.getProperty("sql.spatial.dim2.upper")));
        BBox serverBBox = new BBox(lowerCorner, upperCorner);
        BBox clientBBox = new BBox(query.transform(lowerCorner), query.transform(upperCorner));

        Calendar targetTime = Calendar.getInstance();
        try {
          targetTime.setTimeInMillis(dateFormat.parse(config.getProperty("client.tArrival")).getTime());
        } catch (ParseException e) {
          e.printStackTrace();
        }
        String[] property = config.getProperty("client.poi").split(",");
        Point queryPoint = new Point(Double.valueOf(property[0]), Double.valueOf(property[1]));
        queryPoint = query.transform(queryPoint);
        DSetConfig dSetConfig = new DSetConfig(dataSet, serverBBox, clientBBox, targetTime, queryPoint,
                                               config.getServerSRID(),
                                               config.getProperty("rendering.server.rest.workspace"),
                                               config.getDbVendor());
        dSetConfig.setEdgeLayer(config.getDestinationEdgeTableEntry().getTableName());
        dSetConfig.setVertexLayer(config.getDestinationVertexTableEntry().getTableName());
        dSetConfig.setVertexAnnotatedTableName(config.getDestinationVertexAnnotatedTableEntry().getTableName());

        String areaBufferTable = config.getProperty("tbl.isoAreaBuffer");
        if (areaBufferTable != null) {
          dSetConfig.setAreaBufferLayer(areaBufferTable);
        }
        dSetClientConfigs.put(dataSet, dSetConfig);
      }
    }

    Thread t = new Thread(new CleanupTool());
    t.start();
  }

  private void cleanupLayersAndTables() {
    Config config = new Config(globalCFG);
    LOGGER.info("Remove all layers from the datastore....");
    deleteAllLayers();
    LOGGER.info("Drop all associated tables from the database ....");
    DBUtility.dropAllResultTables(config.getConnection(), globalCFG.getDbVendor(), "_iso_edg_");
    DBUtility.dropAllResultTables(config.getConnection(), globalCFG.getDbVendor(), "_iso_nod_");
    DBUtility.dropAllResultTables(config.getConnection(), globalCFG.getDbVendor(), "_iso_nan_");
    DBUtility.dropAllResultTables(config.getConnection(), globalCFG.getDbVendor(), "_iso_are_");
  }

  /**
   * <p>
   * Method processConfigurationRequest
   * </p>
   * is invoked whenever a client-session is loaded (or after pushing the refresh button on the client).
   * 
   * @param remote
   * @param message
   * @throws MalformedURLException
   * @throws CloneNotSupportedException
   */
  public void processCfgRequest(ServerSession remote, Message message) throws MalformedURLException,
      CloneNotSupportedException {
    LOGGER.info("Connecting client " + remote.getUserAgent() + " with id:" + remote.getId() + "....");
    Config config = new Config(globalCFG);
    configs.put(remote.getId(), config);

    if (!sessionServerConfigs.containsKey(remote.getId())) {
      sessionServerConfigs.put(remote.getId(), new HashMap<Dataset, DSetConfig>());
    }

    Map<String, Object> input = message.getDataAsMap();
    String[] dataSets = ((String) input.get("dataset")).split(",");
    JSONArray arr = new JSONArray();
    for (String dSet : dataSets) {
      Dataset dataSet = Dataset.valueOf(dSet.toUpperCase());
      if (dataSet != null && dSetClientConfigs.get(dataSet) != null) {
        DSetConfig dsetCfg = (DSetConfig) dSetClientConfigs.get(dataSet).clone();
        dsetCfg.appendSessionId(remote.getId());
        sessionServerConfigs.get(remote.getId()).put(dataSet, dsetCfg);
        arr.put(dsetCfg.toJSON());
      }
    }
    Map<String, Object> output = new HashMap<String, Object>();
    output.put("type", "configuration");
    output.put("dataset", arr.toString());
    output.put("mapserverUrl", globalCFG.getProperty("rendering.server.url"));
    remote.deliver(getServerSession(), "/service/cfg", output, null);

    Connection connection = globalCFG.getConnection();
    GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(globalCFG.getProperty("rendering.server.rest.url"),
                                                                  globalCFG
                                                                      .getProperty("rendering.server.rest.username"),
                                                                  globalCFG
                                                                      .getProperty("rendering.server.rest.password"));

    for (DSetConfig cfg : sessionServerConfigs.get(remote.getId()).values()) {
      registerLayer(connection, cfg, publisher, remote.getId(), config.getProperty("tbl.session"));
    }

  }

  /**
   * <p>
   * Method registerLayer
   * </p>
   * creates the tables and for every session a new datastore via rest
   * 
   * @param connection
   * @param tableEntry
   * @param config
   * @param publisher
   */
  private void registerLayer(Connection connection, DSetConfig dSetConfig, GeoServerRESTPublisher publisher,
                             String sessionId, String sessionTable) {

    // register edge table
    DBUtility.deleteGeometryMetadata(connection, dSetConfig.getEdgeTableEntry(), globalCFG.getDbVendor());
    DBUtility.createTargetEdgeTable(connection, globalCFG.getDbVendor(), dSetConfig.getEdgeTableEntry().getTableName());
    DBUtility.insertGeometryMetadata(connection, dSetConfig.getEdgeTableEntry(), globalCFG.getDbVendor(),
        dSetConfig.getServerSRID());
    DBUtility.createSpatialIndex(connection, globalCFG.getDbVendor(), dSetConfig.getEdgeTableEntry());

    GSFeatureTypeEncoder edgeFeatureTypeEncoder = new GSFeatureTypeEncoder();
    // edgeFeatureTypeEncoder.setName(config.getEdgeLayer());
    edgeFeatureTypeEncoder.setName(dSetConfig.getEdgeTableEntry().getTableName());
    edgeFeatureTypeEncoder.setTitle(dSetConfig.getEdgeTableEntry().getDescription());
    String srs = "EPSG:" + dSetConfig.getServerSRID();
    edgeFeatureTypeEncoder.setSRS(srs);
    edgeFeatureTypeEncoder.setNativeBoundingBox(dSetConfig.getServerExtent().getMinX(), dSetConfig.getServerExtent()
        .getMinY(), dSetConfig.getServerExtent().getMaxX(), dSetConfig.getServerExtent().getMaxY(), srs);

    GSLayerEncoder edgeLayerEncoder = new GSLayerEncoder();
    edgeLayerEncoder.setDefaultStyle("STYLE_ISO_EDGES");
    publisher.publishDBLayer(globalCFG.getProperty("rendering.server.rest.workspace"),
        globalCFG.getProperty("rendering.server.rest.datastore"), edgeFeatureTypeEncoder, edgeLayerEncoder);

    DBUtility.createVertexAnnotationTable(connection, globalCFG.getDbVendor(), dSetConfig
        .getVertexAnnotatedTableEntry().getTableName());

    // register vertex table
    DBUtility.deleteGeometryMetadata(connection, dSetConfig.getVertexTableEntry(), globalCFG.getDbVendor());
    DBUtility.createTargetVertexTable(connection, globalCFG.getDbVendor(), dSetConfig.getVertexTableEntry()
        .getTableName());
    DBUtility.insertGeometryMetadata(connection, dSetConfig.getVertexTableEntry(), globalCFG.getDbVendor(),
        dSetConfig.getServerSRID());
    DBUtility.createSpatialIndex(connection, globalCFG.getDbVendor(), dSetConfig.getVertexTableEntry());

    GSFeatureTypeEncoder vertexFeatureTypeEncoder = new GSFeatureTypeEncoder();
    // vertexFeatureTypeEncoder.setName(config.getVertexLayer());
    vertexFeatureTypeEncoder.setName(dSetConfig.getVertexTableEntry().getTableName());
    vertexFeatureTypeEncoder.setTitle(dSetConfig.getVertexTableEntry().getDescription());
    vertexFeatureTypeEncoder.setSRS(srs);
    vertexFeatureTypeEncoder.setNativeBoundingBox(dSetConfig.getServerExtent().getMinX(), dSetConfig.getServerExtent()
        .getMinY(), dSetConfig.getServerExtent().getMaxX(), dSetConfig.getServerExtent().getMaxY(), srs);
    GSLayerEncoder vertexLayerEncoder = new GSLayerEncoder();
    vertexLayerEncoder.setDefaultStyle("STYLE_VERTICES_EXPIRATION");
    // vertexLayerEncoder.setDefaultStyle("STYLE_ISO_VERTICES");
    publisher.publishDBLayer(globalCFG.getProperty("rendering.server.rest.workspace"),
        globalCFG.getProperty("rendering.server.rest.datastore"), vertexFeatureTypeEncoder, vertexLayerEncoder);

    StringBuilder b = new StringBuilder();
    b.append("Registering client with session id: ").append(sessionId).append("\n");
    b.append("Iso edge table: ").append(dSetConfig.getEdgeTableEntry().getTableName()).append("\n");
    b.append("Iso vertex table: ").append(dSetConfig.getVertexTableEntry().getTableName()).append("\n");
    b.append("Iso vertex annotation table: ").append(dSetConfig.getVertexAnnotatedTableEntry().getTableName())
        .append("\n");

    // register buffer table only for dateset BZ:
    if (dSetConfig.getDataset().equals(Dataset.BZ)) {
      DBUtility.deleteGeometryMetadata(connection, dSetConfig.getAreaBufferTableEntry(), globalCFG.getDbVendor());
      DBUtility.createTargetBufferTable(connection, globalCFG.getDbVendor(), dSetConfig.getAreaBufferTableEntry()
          .getTableName());
      DBUtility.insertGeometryMetadata(connection, dSetConfig.getAreaBufferTableEntry(), globalCFG.getDbVendor(),
          dSetConfig.getServerSRID());
      DBUtility.createSpatialIndex(connection, globalCFG.getDbVendor(), dSetConfig.getAreaBufferTableEntry());

      GSFeatureTypeEncoder bufferFeatureTypeEncoder = new GSFeatureTypeEncoder();
      // vertexFeatureTypeEncoder.setName(config.getVertexLayer());
      bufferFeatureTypeEncoder.setName(dSetConfig.getAreaBufferTableEntry().getTableName());
      bufferFeatureTypeEncoder.setTitle(dSetConfig.getAreaBufferTableEntry().getDescription());
      bufferFeatureTypeEncoder.setSRS(srs);
      bufferFeatureTypeEncoder.setNativeBoundingBox(dSetConfig.getServerExtent().getMinX(), dSetConfig
          .getServerExtent().getMinY(), dSetConfig.getServerExtent().getMaxX(), dSetConfig.getServerExtent().getMaxY(),
          srs);

      GSLayerEncoder bufferLayerEncoder = new GSLayerEncoder();
      bufferLayerEncoder.setDefaultStyle("STYLE_ISO_AREA");
      publisher.publishDBLayer(globalCFG.getProperty("rendering.server.rest.workspace"),
          globalCFG.getProperty("rendering.server.rest.datastore"), bufferFeatureTypeEncoder, bufferLayerEncoder);
      b.append("Iso area table: ").append(dSetConfig.getAreaBufferTableEntry().getTableName()).append("\n");
    }

    /*
     * SessionEntry sessionEntry = new SessionEntry(sessionId, dSetConfig.getEdgeTableEntry().getTableName(), dSetConfig
     * .getEdgeTableEntry().getTableName(), dSetConfig.getVertexTableEntry().getTableName(), dSetConfig
     * .getVertexTableEntry().getTableName(), dSetConfig.getVertexAnnotatedTableEntry().getTableName(), dSetConfig
     * .getAreaBufferTableEntry().getTableName(), dSetConfig.getAreaBufferTableEntry().getTableName()); //
     * DBUtility.addSessionEntry(connection, sessionEntry, sessionTable);
     */
    LOGGER.info(b.toString());

  }

  /**
   * <p>
   * Method unregisterLayer
   * </p>
   * 
   * @param connection
   * @param tableName
   * @param layerName
   * @param publisher
   */
  private void unregisterLayer(Connection connection, String tableName, String layerName,
                               GeoServerRESTPublisher publisher) {
    publisher.removeLayer(globalCFG.getProperty("rendering.server.rest.workspace"), layerName);
    DBUtility.dropTable(connection, globalCFG.getDbVendor(), tableName, true);
  }

  /**
   * <p>
   * Method processIsochroneRequest
   * </p>
   * 
   * @param remoteSession
   * @param message
   */
  public void processIsochroneRequest(ServerSession remoteSession, Message message) {

    LOGGER.info("Processing Isochrone Service from client with id:" + remoteSession.getId());
    Map<String, Object> input = message.getDataAsMap();
    // parsing input parameters
    // String channel = message.getChannel();
    Dataset dataset = Dataset.valueOf(((String) input.get("dataset")).toUpperCase());
    String cfgFile = "/etc/";
    switch (dataset) {
      case BZ:
        cfgFile += "config_bz.xml";
        break;
      case ST:
        cfgFile += "config_st.xml";
        break;
      case SF:
        cfgFile += "config_sf.xml";
        break;
      case WDC:
        cfgFile += "config_wdc.xml";
        break;
      case IT:
        cfgFile += "config_it.xml";
        break;
      default:
        cfgFile += "config_bz.xml";
    }

    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    InputStream resourceAsStream = contextClassLoader.getResourceAsStream(cfgFile);
    if (resourceAsStream == null) {
      resourceAsStream = remoteSession.getClass().getResourceAsStream(cfgFile);
    }
    Config config = configs.get(remoteSession.getId());
    config.appendPropertyFile(resourceAsStream);

    DSetConfig dSetConfig = sessionServerConfigs.get(remoteSession.getId()).get(dataset);
    config.setDestinationEdgeTableEntry(dSetConfig.getEdgeTableEntry());
    config.setDestinationVertexTableEntry(dSetConfig.getVertexTableEntry());
    config.setDestinationVertexAnnotatedTableEntry(dSetConfig.getVertexAnnotatedTableEntry());
    config.setDestinationAreaBufferTableEntry(dSetConfig.getAreaBufferTableEntry());

    Mode mode = Mode.valueOf(((String) input.get("mode")).toUpperCase());
    config.setMode(mode);
    Direction direction = Direction.valueOf(((String) input.get("direction")).toUpperCase());
    config.setDirection(direction);
    Double speed = (Double) input.get("speed");
    long dMax = (Long) input.get("dMax");
    String qPoints = (String) input.get("queryPoints");
    String timeString = ((String) input.get("referenceTime")).trim();
    Calendar time = Calendar.getInstance();
    try {
      time.setTimeInMillis(dateFormat.parse(timeString).getTime());
    } catch (ParseException e) {
      e.printStackTrace();
    }

    Boolean expirationMode = input.get("showExpiration") == null ? false : (Boolean) input.get("showExpiration");
    config.setExpirationMode(expirationMode);

    boolean enableAreaComputation = input.get("bufferDistance") == null ? false : true;
    config.setEnableAreaCalculation(enableAreaComputation);

    StringBuilder b = new StringBuilder();
    b.append("Client ").append(remoteSession.getId())
        .append(" computes isochrone request with following parameters:\n");
    b.append("Dataset: ").append(dSetConfig.getDataset()).append("\n");
    b.append("Transportation mode: ").append(mode).append("\n");
    b.append("Query point:").append(qPoints).append("\n");
    b.append("Time:").append(timeString).append("\n");
    b.append("Maximal duration:").append(dMax).append("\n");
    b.append("Walking Speed:").append(speed).append("\n");
    LOGGER.info(b.toString());

    // we need this for output writing
    MineX algorithm = new MineX(config, getServerSession(), remoteSession, message.getChannel());
    algorithm.computeIsochrone(asQueryPointSet(qPoints), (int) dMax / 60, speed, time, mode);
    // resultSet.sendFinalMessage(tailMsg);
    /*
     * try { Thread.sleep(500); LOGGER.info("Sending final data block!"); resultSet.sendFinalMessage(tailMsg); } catch
     * (InterruptedException e) { e.printStackTrace(); }
     */
    LOGGER.info("Isochrone computation finished!");

    /*
     * stats.remove(remoteSession.getId());
     */

    if (enableAreaComputation) {
      long bufferDistance = (Long) input.get("bufferDistance");
      long start = System.currentTimeMillis();
      ReachabilityTool tool = new ReachabilityTool(config);
      long timeInitBuffer = System.currentTimeMillis() - start;
      start = System.currentTimeMillis();
      String approach = (String) input.get("areaComputationMode");

      if (approach != null && approach.equalsIgnoreCase("LBA")) {
        tool.createIsoAreaLBA(config.getDestinationAreaBufferTableEntry(), (int) dMax, bufferDistance, speed,
            config.isIncoming());
      } else {
        tool.createIsoAreaSBA(config.getDestinationAreaBufferTableEntry(), (int) dMax, bufferDistance, speed);
      }

      // DBUtility
      // .controlIndex(config.getConnection(), config.getDbVendor(), config.getDestinationAreaBufferTableEntry(),
      // false);

      long timeCreateBuffer = System.currentTimeMillis() - start;
      LOGGER.info("Buffer creation finished!");

      start = System.currentTimeMillis();
      Statistics statistics = new Statistics(tool.retrieveTotalInhabitants(), tool.retrieveReachedInhabitants(config
          .getDestinationAreaBufferTableEntry()));
      long timeComputeStatistics = System.currentTimeMillis() - start;
      statistics.setIsoAreaInitializationTime(timeInitBuffer);
      statistics.setIsoAreaCalculationTime(timeCreateBuffer);
      statistics.setComputationTime(timeComputeStatistics);
      statistics.setIsoAreaBufferQueryTime(tool.getTotalTimeBufferQuery());
      stats.put(remoteSession.getId(), statistics);
      LOGGER.info("Inhabitant join finished!");
    }

  }

  /**
   * <p>
   * Method processInhabitantsRequest
   * </p>
   * 
   * @param remote
   * @param message
   */
  public void processInhabitantsRequest(ServerSession remote, Message message) {
    Statistics statistics = stats.get(remote.getId());
    int trial = 0;
    while (statistics == null && trial < 10) {
      statistics = stats.get(remote.getId());
      trial++;

      try {
        Thread.sleep(200);
        statistics = stats.get(remote.getId());
        trial++;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    if (statistics != null) {
      remote.deliver(getServerSession(), message.getChannel(), statistics.toJSON(), null);
    }
  }

  public void processShowRoutesRequest(ServerSession remoteSession, Message message) {
    Map<String, Object> input = message.getDataAsMap();
    Config config = configs.get(remoteSession.getId());
    long stopId = (Long) input.get("stopId");
    DBResult dbResult = null;
    JSONObject result = new JSONObject();
    JSONArray rows = new JSONArray();
    try {
      dbResult = DBUtility.getVertexAnnotation(config, (int) stopId);
      ResultSet resultSet = dbResult.getResultSet();
      while (resultSet.next()) {
        String arrivalTime = resultSet.getString("ARRIVAL_TIME");
        String departureTime = resultSet.getString("DEPARTURE_TIME");
        String routeName = resultSet.getString("ROUTE_SHORT_NAME");
        short routeType = resultSet.getShort("ROUTE_TYPE");

        JSONObject jsonEntryObj = new JSONObject();
        jsonEntryObj.put("routeType", routeType);
        jsonEntryObj.put("routeName", routeName.replaceAll(" BZ", "").trim());
        jsonEntryObj.put("arrivalTime", arrivalTime.equals("00:00") ? "--" : arrivalTime);
        jsonEntryObj.put("departureTime", departureTime.equals("00:00") ? "--" : departureTime);
        rows.put(jsonEntryObj);
      }
      result.put("routeDetails", rows);
      remoteSession.deliver(getServerSession(), message.getChannel(), result, null);

    } catch (SQLException e) {
      e.printStackTrace();
    } catch (JSONException e2) {
      e2.printStackTrace();
    } finally {
      dbResult.close();
    }

  }

  private Set<QueryPoint> asQueryPointSet(String qPoints) {
    Set<QueryPoint> queryPoints = new HashSet<QueryPoint>();
    JSONArray arr;
    try {
      arr = new JSONArray(qPoints);
      queryPoints = new HashSet<QueryPoint>();
      for (int i = 0; i < arr.length(); i++) {
        JSONArray ordinates = (JSONArray) arr.get(i);
        queryPoints.add(new QueryPoint(WebNode.Value.NOT_SET, ordinates.getInt(0), ordinates.getInt(1)));
      }
    } catch (JSONException e) {
      LOGGER.warning(e.getMessage());
      e.printStackTrace();
    }
    return queryPoints;
  }

  private void deleteAllLayers() {
    RESTLayerList layers = reader.getLayers();
    if (layers != null) {
      List<String> layerNames = layers.getNames();
      for (String layerName : layerNames) {
        publisher.removeLayer(globalCFG.getProperty("rendering.server.rest.workspace"), layerName);
      }
    }
  }

  private void createPostgisDatastore() {
    GSPostGISDatastoreEncoder gsPostGISDatastoreEncoder = new GSPostGISDatastoreEncoder();
    gsPostGISDatastoreEncoder.setHost(globalCFG.getDatabaseServerName());
    gsPostGISDatastoreEncoder.setDatabase(globalCFG.getDatabase());
    gsPostGISDatastoreEncoder.setUser(globalCFG.getDatabaseUsername());
    gsPostGISDatastoreEncoder.setPassword(globalCFG.getDatabasePassword());
    gsPostGISDatastoreEncoder.setName(globalCFG.getProperty("rendering.server.rest.datastore"));
    gsPostGISDatastoreEncoder.setNamespace(globalCFG.getProperty("rendering.server.rest.workspace"));
    gsPostGISDatastoreEncoder.setMaxConnections(Integer.parseInt(globalCFG.getProperty("db.maxConnections")));
    gsPostGISDatastoreEncoder.setSchema(globalCFG.getDatabaseSchema());
    gsPostGISDatastoreEncoder.setPort(globalCFG.getDatabasePort());
    gsPostGISDatastoreEncoder.setLooseBBox(true);
    gsPostGISDatastoreEncoder.setPreparedStatements(true);
    publisher.createPostGISDatastore(globalCFG.getProperty("rendering.server.rest.workspace"),
        gsPostGISDatastoreEncoder);
  }

  private class CleanupTool implements Runnable {
    public void run() {
      int cleanupIntervalInSeconds = Integer.parseInt(globalCFG.getProperty("cfg.cleanupInterval")) * 3600;
      while (true) {
        try {
          Thread.sleep(cleanupIntervalInSeconds*1000);
          cleanupLayersAndTables();
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

}
