package isochrones.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
*
* <p>The <code>BreakEvenPointUtil</code> class</p>
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p> </p>
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
* 
* This Tool iterates over the rows and compare the break even point between two different files
* If the break even point is exact at a row, that the value in that row from the first column is returned
* otherwise the value is calculated with linear interpolation.
* 
 */
public class BreakEvenPointUtil extends org.apache.tools.ant.Task {
  
  /**
   * 
   * <p>Method calculateBreakEventpoint</p>
   * @param fileURL1
   * @param fileURL2
   * @param columnIndex
   * @return
   */
  public static double calculateBreakEventpoint(URL fileURL1, URL fileURL2, short columnIndex) {
    // we load each column in an array
    int[] column1, columnFile1, columnFile2;
    
    
    
    
    return 0;
  }
  

  /**
   * 
   * <p>Method main</p> 
   * @param args first element is the absolute file name of the first file, the second the absolute filename of the second file and the third one is the column index of the specified attribute.
   * @throws MalformedURLException 
   * 
   */
  public static void main(String[] args) throws MalformedURLException {
    URL fileURL1 = new URL(args[0]);
    URL fileURL2 = new URL(args[2]);
    short columnIndex = Short.parseShort(args[3]);
    
    double bePoint = BreakEvenPointUtil.calculateBreakEventpoint(fileURL1,fileURL2,columnIndex);
    
  }


  
  
}
