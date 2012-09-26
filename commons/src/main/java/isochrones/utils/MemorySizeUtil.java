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
public class MemorySizeUtil extends SizeUtil {

  public MemorySizeUtil(String[] args, char sep) {
    super(args, sep);
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      // TODO add here ..
    }
  }
  
 
  
  @Override
  protected StringBuffer getExperimentInfo() {
    StringBuffer buf = super.getExperimentInfo();
    // TODO add here ..
    return buf;
  }
  
  @Override
  protected String getExperimentDescription() {
    return "#### Starting memory size experiment .... #####\n";
  }
  
}
