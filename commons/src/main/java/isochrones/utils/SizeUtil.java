package isochrones.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public abstract class SizeUtil extends ExperimentUtil {

  int[] sizeCheckpoints;
  int[] durationCheckpoints;
  NumberFormat formatter = new DecimalFormat("#0.00");

  public SizeUtil(String[] args, char sep) {
    super(args, sep);
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      if (arg.startsWith("sizeCheckPoints") && isSet(value)) {
        String[] entries = value.split(",");
        sizeCheckpoints = new int[entries.length];
        for (int i = 0; i < entries.length; i++) {
          sizeCheckpoints[i] = Integer.parseInt(entries[i]);
        }
      } else if (arg.startsWith("durationCheckPoints") && isSet(value)) {
        String[] entries = value.split(",");
        durationCheckpoints = new int[entries.length];
        for (int i = 0; i < entries.length; i++) {
          durationCheckpoints[i] = Integer.parseInt(entries[i]);
        }
        dMax = durationCheckpoints[durationCheckpoints.length-1];
      }
    }
  }

  protected StringBuffer getExperimentInfo() {
    StringBuffer buf = new StringBuffer();
    buf.append("########## Experiment parameter ###########\n");
    buf.append("Size Checkpoints:");
    for (int i = 0; i < sizeCheckpoints.length; i++) {
      buf.append(sizeCheckpoints[i]).append(i < sizeCheckpoints.length - 1 ? "," : "");
    }
    buf.append("\n");
    if(durationCheckpoints!=null){
      buf.append("\nDuration Checkpoints:");
      for (int i = 0; i < durationCheckpoints.length; i++) {
        buf.append(durationCheckpoints[i]).append(i < durationCheckpoints.length - 1 ? "," : "");
      }
      buf.append("\n");
    }
    
    return buf;
  }

  public int[] getSizeCheckpoints() {
    return sizeCheckpoints;
  }
  
  public int[] getDurationCheckpoints() {
    return durationCheckpoints;
  }

}
