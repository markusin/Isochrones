/**
 * 
 */
package isochrones.algorithm;

import isochrones.algorithm.datastructure.AbstractTrace;
import isochrones.algorithm.statistics.Statistic;
import isochrones.db.IsochroneQuery;
import isochrones.network.Location;
import isochrones.utils.Config;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.logging.Logger;

/**
 * <p>
 * The <code>BINEBase</code> class
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
public abstract class Isochrone {

  /* Parameters ----------------------------------------------------------- */
  protected Location[] locations;
  protected int maxDuration;
  protected long toTimeInSecondsAfterMidnight;
  protected long fromTimeInSecondsAfterMidnight;
  protected Calendar time;
  protected double walkingSpeed;
  protected Set<Integer> departureDateCodes;
  protected boolean outputWriting;
  protected IsochroneQuery query;
  protected Statistic statistic;
  protected int maxPrioQueueSize;
  protected int maxTraceSize;
  protected Config config;
  
  protected static final Logger LOGGER = Logger.getLogger(Isochrone.class.getPackage().getName());

  protected Isochrone(Config config, IsochroneQuery query, Statistic statistic) {
    this.config = config;
    this.outputWriting = config.isOutputWriting();
    this.query = query;
    this.statistic = statistic;
    /*
    if (outputWriting) {
      query.clearResultTable();
      query.controlDestinationEdgeIndex(true);
    }
    */
  }
  
  /**
   * 
   * <p>Method exploreInitialLocations</p> Explores the links the query points are situated on. 
   * This is the first exploration step in every algorithm.
   */
  protected abstract void exploreInitialLocations();
  
  
  /**
   * 
   * <p>Method exploreInitialNodes</p> explores the initial query nodes
   * @param nodeIds
   */
  protected abstract void exploreInitialNodes(int[] nodeIds);
  

  /**
   * 
   * <p>Method computeIsochrone</p> calculates the isochrones starting from a set of locations
   * @param locations the set of locations
   * @param duration the maximal duration
   * @param walkingSpeed the average walking speed
   * @param targetTime the target time at the locations
   */
  public abstract void computeIsochrone(Location[] locations, int duration, double walkingSpeed,
                                        Calendar targetTime) ;
  
  
  /**
   * 
   * <p>Method computeIsochrone</p> calculates the isochrones starting from a set of vertices 
   * @param nodeIds the vertex identifiers
   * @param duration the maximal duration
   * @param walkingSpeed the average walking speed
   * @param targetTime the target time at the locations
   * @throws SQLException
   */
  public abstract void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime) ;
  
  /**
   * 
   * <p>Method compute</p> is launched after filling the nodes with querypoints
   */
  public abstract void compute();
  

  /**
   * <p>
   * Method printStatistics
   * </p>
   * prints statistical data regarding isochrone computation.
   */
  public final void printStatistics(String algorithmName){
      statistic.print();      
  }
  
  protected Mode getMode() {
    return config.getMode();
  }
  
  /**
   * 
   * <p>Method updateMode</p>
   * @param mode
   
  protected void updateMode(Mode mode) {
    config.setMode(mode);
    statistic.setMode(mode);
    if (outputWriting) {
      if (getMode() == Mode.UNIMODAL) {
        isoLinks = new TableEntry(config.getProperty("tbl.resultW"), config.getProperty("idx.resultW"),
            TableType.LINK);
      } else {
        isoLinks = new TableEntry(config.getProperty("tbl.resultWB"), config.getProperty("idx.resultWB"),
            TableType.LINK);
      }
    }
  }
  */

  public void setParameters(Location[] locations, int duration, double walkingSpeed,
                            Calendar targetTime, AbstractTrace trace) {
    this.locations = locations;
    setParameters(duration, walkingSpeed, targetTime,trace);
  }

  /**
   * 
   * <p>Method setParameters</p>
   * @param duration
   * @param walkingSpeed
   * @param targetTime
   */
  protected void setParameters(int duration, double walkingSpeed, Calendar targetTime, AbstractTrace trace) {
    if (walkingSpeed > 0.0) {
      this.walkingSpeed = walkingSpeed;
    } else {
      this.walkingSpeed = new Double(config.getProperty("par.defaultWalkingSpeed"));
    }
    maxDuration = duration * 60; // The duration in seconds  
    if (getMode() != Mode.UNIMODAL) {
      // set the target time in seconds after midnight
      initCalendar(targetTime,trace);
    }
  }
  /**
   * 
   * <p>Method initCalendar</p> initialized the time parameters: time interval is set in seconds after midnight
   * @param time the time parameter at which the query point is visited
   */
  protected void initCalendar(Calendar time, AbstractTrace trace){
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(time.getTimeInMillis());
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    if(config.isIncoming()){
      this.toTimeInSecondsAfterMidnight = (time.getTimeInMillis() - calendar.getTimeInMillis()) / 1000;
      this.fromTimeInSecondsAfterMidnight = Math.max(0, toTimeInSecondsAfterMidnight - maxDuration);
    } else {
      this.fromTimeInSecondsAfterMidnight = (time.getTimeInMillis() - calendar.getTimeInMillis()) / 1000;
      this.toTimeInSecondsAfterMidnight = fromTimeInSecondsAfterMidnight + maxDuration;
    }
    this.time = time;
    initDateCodes(trace);
  }

  /**
   * 
   * <p>Method readNodeIds</p> reads the node identifier from the given file
   * @param fileName
   * @return

  protected List<Integer> readNodeIds(String fileName) {
    List<Integer> nodeIds = new ArrayList<Integer>();
    try {
      // Open the file that is the first
      // command line parameter

      File f = new File(fileName);
      System.out.println("Reading nodes from file: " + f.getAbsolutePath());

      FileInputStream fstream = new FileInputStream(fileName);
      // Get the object of DataInputStream
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;
      // Read File Line By Line
      while ((strLine = br.readLine()) != null) {
        nodeIds.add(Integer.parseInt(strLine));
      }
      in.close();
    } catch (Exception e) {// Catch exception if any
      System.err.println("Error: " + e.getMessage());
    }
    return nodeIds;
  }
     */
  
  /**
   * 
   * <p>Method writeNodesIntoFile</p> save the passed nodes in the give file
   * @param nodes
   * @param fileName
  protected void writeNodesIntoFile(List<Integer> nodes, String fileName) {
    Writer output = null;
    File dir = new File("out");
    dir.mkdir();
    File file = new File(dir.getPath() + System.getProperty("file.separator") + fileName);
    try {
      file.createNewFile();
      output = new BufferedWriter(new FileWriter(file));
      for (Integer nodeId : nodes) {
        output.write(nodeId + "\n");
      }
      output.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    //System.out.println("Your file has been written into " + file.getAbsolutePath());
  }
  */
  
  /**
   * 
   * <p>Method _getQuery</p> use only for some special cases
   * @return
   */
  public IsochroneQuery _getQuery() {
    return query;
  }
   
  /**
   * 
   * <p>Method getStatistic</p>
   * @return
   */
  public Statistic getStatistic() {
    return statistic;
  }
  
  public final void initDateCodes(AbstractTrace trace) {
    departureDateCodes = trace.getDateCodes(time);
  }
  
  /**
   * <p>
   * Method terminate
   * </p>
   * writes the remaining links into the database and enables the spatial index
   * 
   * @param remainingLinks
   * @throws SQLException
   */
  public abstract void terminate();
  
  /**
   * 
   * <p>Method explore</p>
   * @param link
   * @param n
  
  public abstract void explore(ILink link, INode n);
   */

}
