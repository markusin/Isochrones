package isochrones.utils;

import java.io.File;

public abstract class ExperimentUtil extends LauncherUtil {

  File outputFile;

  private boolean singleBreakpoint;
  private boolean clearCache = true;
  private boolean appendOutput = false;
  private boolean printHeader = false;
  

  public ExperimentUtil(String[] args, char sep) {
    super(args, sep);
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      if (arg.startsWith("outputFile")) {
        File dir = new File(outputDir);
        if (!dir.exists()) {
          dir.mkdir();
        }
        outputFile = new File(outputDir + value);
      } else if (arg.startsWith("singleBreakpoint") && isSet(value)) {
        singleBreakpoint = Boolean.valueOf(value);
      } else if (arg.startsWith("clearCache") && isSet(value)) {
        clearCache = Boolean.valueOf(value);
      } else if (arg.startsWith("appendOutput") && isSet(value)) {
        appendOutput = Boolean.valueOf(value);
      }
    }
    printHeader = !appendOutput;  // we only print header if append flag is not set
  }
  
  public boolean printHeader() {
    return printHeader;
  }

  protected StringBuffer getExperimentInfo() {
    StringBuffer buf = new StringBuffer();
    buf.append("########## Experiment parameters ###########\n");
    buf.append("Output file: " + outputFile.getAbsolutePath()).append("\n");
    buf.append("Single breakpoint: " + singleBreakpoint);
    buf.append("Cache clearing: " + clearCache);
    
    return buf;
  }
  
  /**
   * 
   * <p>Method getOutputFile</p>
   * @return
   */
  public final File getOutputFile() {
    return outputFile;
  }
  
  /**
   * 
   * <p>Method isSingleBreakpoint</p> 
   * @return true, if the experiment is terminated after each size or duration breakpoint
   */
  public boolean isSingleBreakpoint() {
    return singleBreakpoint;
  }
  
  /**
   * 
   * <p>Method clearCache</p>
   * @return
   */
  public boolean clearCache() {
    return clearCache;
  }
  
  /**
   * 
   * <p>Method appendOutput</p>
   * @return
   */
  public boolean appendOutput() {
    return appendOutput;
  }
  
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getHeaderInfo());
    buf.append(getSetupInfo());
    buf.append(getInputParameters());
    buf.append(getExperimentInfo());
    buf.append(getExperimentDescription());
    buf.append(getTailInfo());
    return buf.toString();
  }
  
  /**
   * 
   * <p>Method getExperimentDescription</p> Short description about the experiment
   * @return
   */
  protected abstract String getExperimentDescription();
  
}
