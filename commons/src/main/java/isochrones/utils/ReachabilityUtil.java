package isochrones.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ReachabilityUtil extends LauncherUtil {

    String outputDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "out"
        + System.getProperty("file.separator");
    File outFile;
    Calendar[] targetTimes;
    Calendar startTargetTime, endTargetTime;
    
    NumberFormat formatter = new DecimalFormat("#0.00");
    

    /**
     * 
     * <p>Constructs a(n) <code>ReachabilityUtil</code> object.</p>
     * @param args
     * @param sep
     * @param algorithm
     */
    public ReachabilityUtil(String[] args, char sep, String algorithm) {
      super(args, sep, algorithm);
      for (String arg : args) {
        String value = arg.substring(arg.indexOf(sep) + 1);
        if (arg.startsWith("outputDir") && isSet(value)) {
          outputDir = value;
          if (!outputDir.endsWith(System.getProperty("file.separator"))) {
            outputDir.concat(System.getProperty("file.separator"));
          }
        } else if (arg.startsWith("targetTimes") && isSet(value)) {
          String[] values = value.split(",");
          targetTimes = new Calendar[values.length];
          for (int i = 0; i < values.length; i++) {
            try {
              Calendar cal = Calendar.getInstance();
              cal.setTimeInMillis(dateFormat.parse(values[i]).getTime());
              targetTimes[i] = cal;
            } catch (ParseException e) {
              e.printStackTrace();
            }
          }
        } else if (arg.startsWith("startTargetTime") && isSet(value)) {
          startTargetTime = Calendar.getInstance();
          try {
            startTargetTime.setTimeInMillis(dateFormat.parse(value).getTime());
          } catch (ParseException e) {
            e.printStackTrace();
          }
        } else if (arg.startsWith("endTargetTime") && isSet(value)) {
          endTargetTime = Calendar.getInstance();
          try {
            endTargetTime.setTimeInMillis(dateFormat.parse(value).getTime());
          } catch (ParseException e) {
            e.printStackTrace();
          }
        }
      }
      outFile = new File(outputDir + dataSet + "_" + algorithm  + "_" + mode + "_reachbility.dat");
    }

    public String getTimeAsString() {
      return dateFormat.format(time.getTime());
    }

    /**
     * <p>
     * Method printHead
     * </p>
     * 
     * @return
     */
    public String printHead() {
      StringBuilder b = new StringBuilder();
      b.append(getHeaderInfo());
      // b.append(getParameters());
      b.append("#### Memory test parameters #########\n");
     //b.append("Snap size:").append(snapSize).append("\n");
      b.append("Output directory:").append(outputDir).append("\n");
      b.append("Write isochrones in DB: ").append(outputWriting).append("\n");
      b.append("\n#### Start memory tests #####");
      return b.toString();
    }

    /**
     * <p>
     * Method printTail
     * </p>
     * 
     * @return
     */
    public String printTail() {
      StringBuilder b = new StringBuilder();
      b.append("##### End  of memory tests #####\n");
      b.append("###################################\n");
      return b.toString();
    }
    
  
  /**
   * 
   * <p>Method writeTraceInfosIntoFile</p>
   * @param sizeLogger
   * @param fileName2
   */
    public void logIntoFile(Map<Calendar, List<ReachabilityEntry>> sizeLogger, String fileName, int totalNetworkSize) {
    Writer out1 = null;
    File dir = new File("out");
    dir.mkdir();
    File sizeFile = new File(dir.getPath() + System.getProperty("file.separator") + fileName + ".dat");
    try {
      sizeFile.createNewFile();
      out1 = new BufferedWriter(new FileWriter(sizeFile));
      out1.write("#tArrival \t dmax \t |V| reached \t %|V| reached \n");
      //out1.write("#Duration \t");
      for (Calendar targetTime : sizeLogger.keySet()) {
        for (ReachabilityEntry reachabilityEntry : sizeLogger.get(targetTime)) {
          out1.write(outDateFormat.format(targetTime.getTime()) + "\t");
          out1.write(reachabilityEntry.getDuration() + "\t");
          out1.write(reachabilityEntry.getNumberOfNodes() + "\t");
          Double percentage = ((double)reachabilityEntry.getNumberOfNodes()/totalNetworkSize)*100;
          out1.write(formatter.format(percentage) + "\n");
        }
      }
      
      
      /*
      
      int i = 0;
      TreeMap<Integer, Map<Calendar, Double>> logTable = new TreeMap<Integer, Map<Calendar,Double>>();
      for (Calendar targetTime : sizeLogger.keySet()) {
        out1.write("Reachability (" +  dateFormat.format(targetTime.getTime()) + ")");
        if(i<sizeLogger.size()-1){
          out1.write("\t");
        } else {
          out1.write("\n");
        }
        i++;
        
        for(ReachabilityEntry entry : sizeLogger.get(targetTime)){
          if(!logTable.containsKey(entry.getDuration())){
            logTable.put(entry.getDuration(), new TreeMap<Calendar,Double>());
          }
          //int reachedSize = entry.getNumberOfNodes() + entry.getNumberOfPedLinks() + entry.getNumberOfBusLinks();
          int reachedSize = entry.getNumberOfNodes();// + entry.getNumberOfPedLinks() + entry.getNumberOfBusLinks();
          Double percentage = ((double)reachedSize/totalNetworkSize)*100;
          logTable.get(entry.getDuration()).put(targetTime,percentage);
        }
      }
      
      for (Iterator<Integer> iterator = logTable.keySet().iterator(); iterator.hasNext();) {
        Integer duration = iterator.next();
        out1.write(duration + "\t");
        i = 0;
        for(Double percentage : logTable.get(duration).values()){
          out1.write(formatter.format(percentage));
          if(i<logTable.get(duration).size()-1){
            out1.write("\t");
          } else {
            out1.write("\n");
          }
          i++;
        }
      }
      */
      out1.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Size measurement has been written into " + sizeFile.getAbsolutePath());
  }
    
    public Calendar[] getTargetTimes() {
      return targetTimes;
    }
    
    public Calendar getStartTargetTime() {
      return startTargetTime;
    }
    
    public Calendar getEndTargetTime() {
      return endTargetTime;
    }

}
