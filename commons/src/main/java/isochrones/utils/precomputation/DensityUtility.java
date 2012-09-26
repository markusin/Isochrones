package isochrones.utils.precomputation;

import isochrones.utils.SetupUtil;

public class DensityUtility extends SetupUtil {

  int[] distances;
  boolean countEdges = false;

  /**
   * <p>
   * Constructs a(n) <code>DensityUtility</code> object.
   * </p>
   * 
   * @param args
   * @param sep
   */
  public DensityUtility(String[] args, char sep) {
    super(args, sep);
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      if (arg.startsWith("distanceParameters") && isSet(value)) {
        String[] distValues = value.split(",");
        distances = new int[distValues.length];
        for (int i = 0; i < distValues.length; i++) {
          distances[i] = Integer.parseInt(distValues[i]);
        }
      } else if(arg.startsWith("countEdges") && isSet(value)) {
        countEdges = Boolean.parseBoolean(value);
      } 
    }
  }

  /**
   * <p>
   * Method getDistances
   * </p>
   * 
   * @return
   */
  public int[] getDistances() {
    return distances;
  }
  
  /**
   * 
   * <p>Method isCountEdges</p> boolean flag that specifies if counting the edges in the range or the vertices
   * @return true, than counts edges ; false counts the vertices
   */
  public boolean isCountEdges() {
    return countEdges;
  }

}
