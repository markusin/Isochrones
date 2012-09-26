package isochrones.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Logger;


/**
 *
 * <p>The <code>IOUtility</code> class</p>
 * <p>
 * <a href="http://www.http://www.inf.unibz.it/dis">Database Information Systems - Research Group</a>
 * </p>
 * <p>
 * Dominikanerplatz 39100 Bolzano, Italy.
 * </p>
 * <p>
 * </p>
 * 
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 1.0
 */
public class IOUtility {
  
  private static Logger LOGGER = Logger.getLogger(IOUtility.class.getPackage().getName());

  /**
   * <p>
   * Method readFromFile
   * </p>
   * 
   * @param fileURI the name of the file to be readed
   * @return String the content of the file
   */
  public static String readFromFile(URI fileURI) {
    InputStreamReader inputStreamReader = null;
    InputStream fstream = null;
    BufferedReader br = null;
    try {
      File file = new File(fileURI);
      fstream = new FileInputStream(file);
      inputStreamReader = new InputStreamReader(fstream);
      br = new BufferedReader(inputStreamReader);

      StringBuilder sb = new StringBuilder();
      String strLine;
      while ((strLine = br.readLine()) != null) {
        sb.append(strLine).append("\n");
      }
      return sb.toString();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("Cannot find file with uri: " + fileURI);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Problem when reading the stream");
    }
  }
  
  
  /**
   * 
   * <p>Method writeIntoFile</p>
   * @param fileURI the uri of the destination file
   * @param content the content to be written into
   */
  public static void writeIntoFile(URI fileURI, String content) {
    FileWriter fileWriter = null;
    File file = null;
    try {
      file = new File(fileURI);
      fileWriter = new FileWriter(file);
      fileWriter.write(content);
    } catch (IOException e) {
      throw new RuntimeException("Problem on storing out file \n " + e);
    } finally {
      if(fileWriter!=null) {
        try {
          fileWriter.close();
        } catch (IOException e) {
          LOGGER.severe("Problems on closing file. \n" + e );
        }
      }
    }
  }

  /**
   * @param fileUri the filename containing an Xml structure
   * @return an JDOM xml document
   *
  public static Document readFromXMLFile(URI fileUri) {
    try {
      return new SAXBuilder().build(new File(fileUri));
    } catch (JDOMException e) {
      String msg = "Problem on parsing xml file";
      LOGGER.warning(msg);
      throw new RuntimeException(msg);
    } catch (IOException e) {
      String msg = "Problem on reading file " + fileUri;
      LOGGER.warning(msg);
      throw new RuntimeException(msg);
    }
  }

  /**
   * @param fileName the filen containing an Xml structure
   * @return an JDOM xml document
   * @throws IOException
   * @throws JDOMException
   *
  public static Document readFromXMLFile(File file) throws JDOMException, IOException {
    return new SAXBuilder().build(file);
  }
  */

}
