package isochrones.utils;


public abstract class DurationUtil extends ExperimentUtil {

  int[] durationCheckpoints;

  /**
   * <p>
   * Constructs a(n) <code>DurationUtil</code> object.
   * </p>
   * 
   * @param args the arguments to be read and set as property
   * @param sep the separator that separates the properties
   * @param algorithm the name of the algorithm
   */
  public DurationUtil(String[] args, char sep) {
    super(args, sep);
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      if (arg.startsWith("durationCheckPoints") && isSet(value)) {
        String[] entries = value.split(",");
        durationCheckpoints = new int[entries.length];
        for (int i = 0; i < entries.length; i++) {
          durationCheckpoints[i] = Integer.parseInt(entries[i]);
        }
        dMax = durationCheckpoints[durationCheckpoints.length-1]+1;
      }
    }
  }

  public int[] getDurationCheckpoints() {
    return durationCheckpoints;
  }
  
  protected StringBuffer getExperimentInfo() {
    StringBuffer buf = new StringBuffer();
    buf.append("########## Experiment parameter ###########\n");
    buf.append("Duration checkpoints:");
    for (int i = 0; i < durationCheckpoints.length; i++) {
      buf.append(durationCheckpoints[i]).append(i<durationCheckpoints.length-1 ? "," : "");
    }
    buf.append("\n");
    return buf;
  }
  
}

