package isochrones.ant.tasks;

import isochrones.utils.IOUtility;
import isochrones.utils.MathUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
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
public class BreakEvenPointTask extends Task {

  private URL fileInURL1, fileInURL2, fileoutURL;
  private short columnIndex;

  @Override
  public void execute() {
    validate();
    try {

      String file1 = IOUtility.readFromFile(fileInURL1.toURI());

      Double[] outValues = storeColumnInArray(file1, (short) 1);
      Double[] inValues1 = storeColumnInArray(file1, columnIndex);

      String file2 = IOUtility.readFromFile(fileInURL2.toURI());
      Double[] inValues2 = storeColumnInArray(file2, columnIndex);

      if (outValues.length != inValues1.length || outValues.length != inValues2.length) {
        throw new IllegalArgumentException("The numer of rows are not identical in the file " + fileInURL1 + fileInURL2);
      }

      double breaEventPoint = calculateBreaEventPoint(inValues1, inValues2, outValues);
      Logger.getAnonymousLogger().info("Break even point is: " + breaEventPoint);
      IOUtility.writeIntoFile(fileoutURL.toURI(), breaEventPoint + "\n");

    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

  }

  private void validate() {
    if (fileInURL1 == null)
      throw new BuildException("You must specify an attribute named fileInURL1.");
    if (fileInURL2 == null)
      throw new BuildException("You must specify an attribute named fileInURL2.");
    if (fileoutURL == null)
      throw new BuildException("You must specify an attribute named fileoutURL.");
  }

  /**
   * <p>
   * Method calculateBreaEventPoint
   * </p>
   * calculates the break even point
   * 
   * @param inValues1
   * @param inValues2
   * @param outValues
   * @return
   */
  private double calculateBreaEventPoint(Double[] inValues1, Double[] inValues2, Double[] outValues) {

    for (int i = 0; i < inValues1.length; i++) {
      if (inValues1[i] > inValues2[i]) {
        double currOutVal = outValues[i - 1];
        double ipInValue1 = 0, ipInValue2 = 0;
        while (ipInValue1 <= ipInValue2) {
          currOutVal++;
          ipInValue1 = MathUtil.interpolate(inValues1[i - 1], inValues1[i], outValues[i - 1], outValues[i], currOutVal);
          ipInValue2 = MathUtil.interpolate(inValues2[i - 1], inValues2[i], outValues[i - 1], outValues[i], currOutVal);
        }
        return currOutVal;
      } else if (inValues1[i] == inValues2[i]) {
        return outValues[i];
      }
    }

    return -1;
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

  /**
   * <p>
   * Method setFile1
   * </p>
   * 
   * @param fileIn1
   */
  public void setFileIn1(String fileIn1) {
    try {
      fileInURL1 = new File(fileIn1).toURI().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  /**
   * <p>
   * Method setFile2
   * </p>
   * 
   * @param fileIn2
   */
  public void setFileIn2(String fileIn2) {
    try {
      fileInURL2 = new File(fileIn2).toURI().toURL();
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
    BreakEvenPointTask beTask = new BreakEvenPointTask();
    beTask.setFileIn1(args[0]);
    beTask.setFileIn2(args[1]);
    beTask.setColumnIndex(args[2]);
    beTask.setFileOut(args[3]);
    beTask.execute();
  }

}
