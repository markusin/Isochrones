package isochrones.utils;


/**
 * <p>
 * The <code>MemoryUtil</code> class
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
public class RuntimeSizeUtil extends SizeUtil {

  int frequency = 1;

  public RuntimeSizeUtil(String[] args, char sep) {
    super(args, sep);
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      if (arg.startsWith("frequency") && isSet(value)) {
        frequency = Integer.valueOf(value);
      }
    }
  }
  
  /**
   * <p>
   * Method getFrequency
   * </p>
   * 
   * @return the frequency of the increment
   */
  public int getFrequency() {
    return frequency;
  }
  
  @Override
  protected StringBuffer getExperimentInfo() {
    StringBuffer buf = super.getExperimentInfo();
    buf.append("Frequency: " + frequency).append("\n");
    return buf;
  }
  
  @Override
  protected String getExperimentDescription() {
    return "#### Starting runtime size experiment .... #####\n";
  }
  
}
