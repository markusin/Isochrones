package isochrones.utils;

public class RuntimeDurationUtil extends DurationUtil {

  int frequency = 1;

  public RuntimeDurationUtil(String[] args, char sep) {
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
    buf.append("########## Experiment parameters ###########\n");
    buf.append("Frequency: " + frequency).append("\n");
    return buf;
  }
  
  @Override
  protected String getExperimentDescription() {
    return "#### Starting runtime duration experiment .... #####\n";
  }

}
