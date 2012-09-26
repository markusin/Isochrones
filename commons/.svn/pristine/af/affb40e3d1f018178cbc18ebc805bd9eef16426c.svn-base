package isochrones.utils;

import isochrones.algorithm.Mode;
import isochrones.network.Location;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LauncherUtil extends SetupUtil {

  public static final DateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm");
  public static final DateFormat outDateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'-'HH':'mm");
  // parameters with default values
  Integer dMax = 60;
  Double speed = 1.0;
  Calendar time = Calendar.getInstance();
  Location[] locations;
  int[] nodesOfInterest;
  String algorithm, algorithmClassName;
  boolean outputWriting = false;
  protected String outputDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "out"
      + System.getProperty("file.separator");

  /**
   * <p>
   * Constructs a(n) <code>LauncherUtil</code> object.
   * </p>
   * 
   * @param args the array of parameters passed in key value format
   * @param sep the separator which separates key from value
   */
  public LauncherUtil(String[] args, char sep) {
    super(args, sep);
    if (args.length == 0) {
      System.out.println("No arguments set. Therefore using default values.");
    }

    String network = null;
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      if (arg.startsWith("algorithmName") && isSet(value)) {
        this.algorithm = value;
        config.setAlgorithmName(value);
      } else if (arg.startsWith("algorithmClassName") && isSet(value)) {
        this.algorithmClassName = value;
      } else if (arg.startsWith("outputWriting") && isSet(value)) {
        outputWriting = Boolean.valueOf(value);
        config.setOutputWriting(outputWriting);
      } else if (arg.startsWith("lOI") && isSet(value)) {
          String[] values = value.split(";");
          locations = new Location[values.length];
          for (int i = 0; i < values.length; i++) {
            String[] entity = values[i].split(",");
            locations[i] = new Location(Integer.valueOf(entity[0]), Double.valueOf(entity[1]));
          }
      } else if (arg.startsWith("nOI") && isSet(value)) {
          String[] values = value.split(";");
          nodesOfInterest = new int[values.length];
          for (int i = 0; i < values.length; i++) {
            nodesOfInterest[i] = Integer.valueOf(values[i]);
          }
      } else if (arg.startsWith("dMax") && isSet(value)) {
        dMax = Integer.valueOf(value);
      } else if (arg.startsWith("speed") && isSet(value)) {
        speed = Double.valueOf(value);
      } else if (arg.startsWith("targetTime") && isSet(value)) {
        try {
          time.setTimeInMillis(dateFormat.parse(value).getTime());
        } catch (ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else if (arg.startsWith("network") && isSet(value)) {
        if (!value.isEmpty())
          network = value;
      } else if (arg.startsWith("outputDir") && isSet(value)) {
        outputDir = value;
        String lastChar = String.valueOf(outputDir.charAt(outputDir.length()-1));
        if(!lastChar.equals(System.getProperty("file.separator"))){
          outputDir += System.getProperty("file.separator");
        }
      }
    }
    if (network != null) {
      config._setProperty("tbl.links", network);
    }
  }

  @Deprecated
  /**
   * <p>
   * Constructs a(n) <code>LauncherUtil</code> object.
   * </p>
   * 
   * @param args the arguments to be read and set as property
   * @param sep the separator that separates the properties
   * @param algorithm the name of the algorithm
   */
  public LauncherUtil(String[] args, char sep, String algorithm) {
    this(args, sep);
    this.algorithm = algorithm;
  }

  /**
   * <p>
   * Method getMaxDuration
   * </p>
   * returns the maximal duration
   * 
   * @return
   */
  public Integer getMaxDuration() {
    return dMax;
  }
  
  /**
   * 
   * <p>Method getLocations</p>
   * @return
   */
  public Location[] getLocations() {
    return locations;
  }

  /**
   * <p>
   * Method getNodesOfInterest
   * </p>
   * 
   * @return
   */
  public int[] getNodesOfInterest() {
    return nodesOfInterest;
  }

  /**
   * <p>
   * Method getSpeed
   * </p>
   * returns the walking speed
   * 
   * @return
   */
  public Double getSpeed() {
    return speed;
  }

  /**
   * <p>
   * Method getTargetTime
   * </p>
   * returns the latest arrival time
   * 
   * @return
   */
  public Calendar getTime() {
    return time;
  }

  public String getTimeAsString() {
    return dateFormat.format(time.getTime());
  }

  /**
   * <p>
   * Method getAlgorithmName
   * </p>
   * 
   * @return
   */
  public String getAlgorithmName() {
    return algorithm;
  }

  /**
   * <p>
   * Method getAlgorithmClassName
   * </p>
   * 
   * @return the java class Name of the algorithm
   */
  public String getAlgorithmClassName() {
    return algorithmClassName;
  }

  /**
   * <p>
   * Method isOutputWriting
   * </p>
   * 
   * @return
   */
  public boolean isOutputWriting() {
    return outputWriting;
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getHeaderInfo());
    buf.append(getSetupInfo());
    buf.append(getInputParameters());
    buf.append(getTailInfo());
    return buf.toString();
  }

  /**
   * <p>
   * Method printInfo
   * </p>
   * 
   * @return
   */
  public String printInfo() {
    return toString();
  }

  /**
   * <p>
   * Method getHeader
   * </p>
   * 
   * @return
   */
  protected String getHeaderInfo() {
    return "########## Algorithm " + algorithm + " ###########\n";
  }

  /**
   * <p>
   * Method getSetupInfo
   * </p>
   * 
   * @return
   */
  protected StringBuffer getSetupInfo() {
    StringBuffer buf = new StringBuffer();
    buf.append("###### Database settings:######\n");
    buf.append("DB vendor: ").append(getDBVendor()).append("\n");
    buf.append("Network table name: ").append(config.getEdgeTable()).append("\n");
    if (mode.equals(Mode.MULTIMODAL)) {
      buf.append("Schedule table: ").append(config.getScheduleTable()).append("\n");
      buf.append("Calendar table: ").append(config.getDaymarkerTable()).append("\n");
    }
    if (outputWriting) {
      buf.append("Isochrone table name:").append(config.getDestinationEdgeTableEntry()).append("\n");
    }
    buf.append("DB writing: ").append(outputWriting).append("\n");
    return buf;
  }

  @Override
  protected StringBuffer getInputParameters() {
    StringBuffer buf = super.getInputParameters();
    buf.append("###### Algorithm parameters: ######").append("\n");
    if (locations != null && locations.length > 0) {
      buf.append("Locations of interest: ");
      for (int i = 0; i < locations.length; i++) {
        if (i > 0) {
          buf.append(",");
        }
        buf.append(locations[i]);
      }
      buf.append("\n");
    }

    if (nodesOfInterest != null && nodesOfInterest.length > 0) {
      buf.append("Nodes of interest: ");
      for (int i = 0; i < nodesOfInterest.length; i++) {
        if (i > 0) {
          buf.append(",");
        }
        buf.append(nodesOfInterest[i]);
      }
      buf.append("\n");
    }
    buf.append("Maximal duration: ").append(dMax).append("min (=").append(dMax * 60).append("sec)").append("\n");
    buf.append("Walking speed: ").append(speed).append("(m/s)\n");
    if (!mode.equals(Mode.UNIMODAL)) {
      buf.append("Latest arrival time: ").append(getTimeAsString()).append("\n");
    }
    buf.append("Travel mode: ").append(mode).append("\n");
    buf.append("Dataset: ").append(dataSet).append("\n");
    return buf;
  }

  protected final String getTailInfo() {
    return "#############################################\n";
  }

}