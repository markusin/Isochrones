package isochrones.ant.tasks;

import isochrones.utils.AggregateUtil;
import isochrones.utils.IOUtility;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * <p>
 * The <code>BreakEvenPointTask</code> class
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
public class AverageTask extends Task {

  private URL fileInURL, fileoutURL;
  private List<String> columnLabels = new ArrayList<String>();
  private short columnIndex;
  private short[] visibleColumns;
  private static final NumberFormat formatter = new DecimalFormat("#0.00");

  @Override
  public void execute() {
    validate();
    try {

      String file = IOUtility.readFromFile(fileInURL.toURI());

      String[] columnLabels = getColumnLabels(file);

      Double[] outValues = storeColumnInArray(file, (short) 1);
      Double[] inValues = storeColumnInArray(file, columnIndex);

      List<Map<Double,Double>> otherValues = new ArrayList<Map<Double,Double>>();

      for (int i = 0; i < visibleColumns.length; i++) {
        otherValues.add(storeColumnInArray(file, visibleColumns[i],outValues.length));
      }

      SortedMap<Double, List<Double>> values = new TreeMap<Double, List<Double>>();
      // iterate over all records
      for (int i = 0; i < outValues.length; i++) {
        double outValue = outValues[i];
        if (!values.containsKey(outValues[i])) {
          values.put(outValue, new ArrayList<Double>());
        } 
        values.get(outValue).add(inValues[i]);
      }

      StringBuilder b = new StringBuilder();
      b.append(columnLabels[0]).append("\t");
      b.append("Average").append("\t").append("Median").append("\t").append("Min").append("\t").append("Max");
      for (int i = 0; i < visibleColumns.length; i++) {
        b.append("\t").append(columnLabels[visibleColumns[i] - 1]);
      }
      b.append("\n");

      for (Double key : values.keySet()) {
        Collections.sort(values.get(key));
        double avg = AggregateUtil.average(values.get(key).toArray(new Double[values.get(key).size()]));
        double median = AggregateUtil.median(values.get(key).toArray(new Double[values.get(key).size()]));
        double min = values.get(key).get(0);
        double max = values.get(key).get(values.get(key).size() - 1);
        
        b.append(key).append("\t").append(formatter.format(avg)).append("\t").append(formatter.format(median)).append("\t").append(min).append("\t").append(max);
        for (Map<Double, Double> map : otherValues) {
          b.append("\t").append(map.get(key));
        }
        b.append("\n");
      }
      Logger.getAnonymousLogger().info(b.toString());
      IOUtility.writeIntoFile(fileoutURL.toURI(), b.toString());

    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

  }

  private void validate() {
    if (fileInURL == null)
      throw new BuildException("You must specify an attribute named fileInURL1.");
    if (fileoutURL == null)
      throw new BuildException("You must specify an attribute named fileoutURL.");
  }

  private String[] getColumnLabels(String fileContent) {
    StringTokenizer lineTokenizer = new StringTokenizer(fileContent, "\n");
    ArrayList<String> values = new ArrayList<String>();
    if (lineTokenizer.hasMoreTokens()) { // simply parses first line
      String rowToken = lineTokenizer.nextToken();
      StringTokenizer columnTokenizer = new StringTokenizer(rowToken, "\t");
      while (columnTokenizer.hasMoreTokens()) {
        values.add(columnTokenizer.nextToken());
      }
    }
    return values.toArray(new String[values.size()]);
  }

  /**
   * <p>
   * Method storeColumnInArray
   * </p>
   * 
   * @param fileContent the file to parse and store the specified column in the array
   * @param columnIndex the index of the column to be stored
   * @return the stored column as a double array
   */
  private Double[] storeColumnInArray(String fileContent, short columnIndex) {
    StringTokenizer lineTokenizer = new StringTokenizer(fileContent, "\n");
    ArrayList<Double> values = new ArrayList<Double>();
    boolean skipFirstRow = true;
    while (lineTokenizer.hasMoreTokens()) {
      String rowToken = lineTokenizer.nextToken();
      if (skipFirstRow) {
        skipFirstRow = false;
        StringTokenizer columnTokenizer = new StringTokenizer(rowToken, "\t");
        while (columnTokenizer.hasMoreTokens()) {
          columnLabels.add(columnTokenizer.nextToken());
        }
      } else {
        StringTokenizer columnTokenizer = new StringTokenizer(rowToken, "\t");
        int column = 1;
        while (columnTokenizer.hasMoreTokens()) {
          String columnToken = columnTokenizer.nextToken();
          if (column == columnIndex) {
            values.add(Double.parseDouble(columnToken));
            break; // exits from inner loop
          }
          column++;
        }
      }
    }
    return values.toArray(new Double[values.size()]);
  }
  
  private SortedMap<Double, Double> storeColumnInArray(String fileContent, short columnIndex, int length) {
    StringTokenizer lineTokenizer = new StringTokenizer(fileContent, "\n");
    TreeMap<Double,Double> values = new TreeMap<Double, Double>();
    
    for (int i = 0; i <= length; i++) {
      String rowToken = lineTokenizer.nextToken();
      if (i>0) { // skipping first line
        StringTokenizer columnTokenizer = new StringTokenizer(rowToken, "\t");
        int column = 1;
        double referenceValue = -1d, value = -1d;
        while (columnTokenizer.hasMoreTokens()) {
          String columnToken = columnTokenizer.nextToken();
          if(column==1){
            referenceValue = Double.parseDouble(columnToken);
          } else {
            if (column == columnIndex) {
              value = Double.parseDouble(columnToken);
            }
          }
          values.put(referenceValue, value);
          column++;
        }
      } 
    }
    return values;
  }


  /**
   * <p>
   * Method setFile1
   * </p>
   * 
   * @param fileIn
   */
  public void setFileIn(String fileIn) {
    try {
      fileInURL = new File(fileIn).toURI().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  /**
   * <p>
   * Method setColumnIndex
   * </p>
   * 
   * @param columnIndex
   */
  public void setColumnIndex(String columnIndex) {
    this.columnIndex = Short.parseShort(columnIndex);
  }

  /**
   * <p>
   * Method setVisibleColumns
   * </p>
   * 
   * @param vColumnIndices a list of visible columns to be displayed in the output file Note: index starts with 1
   */
  public void setVisibleColumns(String vColumnIndices) {
    String[] vColumnsIdx = vColumnIndices.split(",");
    this.visibleColumns = new short[vColumnsIdx.length];
    for (int i = 0; i < vColumnsIdx.length; i++) {
      this.visibleColumns[i] = Short.parseShort(vColumnsIdx[i]);
    }
  }

  /**
   * <p>
   * Method setFileOut
   * </p>
   * 
   * @param fileOut
   */
  public void setFileOut(String fileOut) {
    try {
      fileoutURL = new File(fileOut).toURI().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  /**
   * <p>
   * Method main
   * </p>
   * only for testing
   * 
   * @param args
   */
  public static void main(String[] args) {
    AverageTask beTask = new AverageTask();
    beTask.setFileIn(args[0]);
    beTask.setFileOut(args[1]);
    beTask.setColumnIndex(args[2]);
    beTask.setVisibleColumns("4,5");
    beTask.execute();
  }

}
