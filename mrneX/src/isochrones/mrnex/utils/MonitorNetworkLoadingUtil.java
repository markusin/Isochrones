package isochrones.mrnex.utils;

import isochrones.utils.DurationUtil;

public class MonitorNetworkLoadingUtil extends DurationUtil {

  boolean countVertex = true;

    public MonitorNetworkLoadingUtil(String[] args, char sep) {
      super(args, sep);
      for (String arg : args) {
        String value = arg.substring(arg.indexOf(sep) + 1);
        if (arg.startsWith("countVertex")) {
          countVertex = Boolean.valueOf(value);
        }
      }
    }

    /**
     * 
     * <p>Method isCountVertex</p>
     * @return
     */
    public boolean isCountVertex() {
      return countVertex;
    }

    @Override
    protected StringBuffer getExperimentInfo() {
      StringBuffer buf = super.getExperimentInfo();
      buf.append("########## Experiment parameters ###########\n");
      buf.append("Count vertices: " + countVertex).append("\n");
      return buf;
    }
    
    @Override
    protected String getExperimentDescription() {
      return "#### Starting runtime duration experiment .... #####\n";
    }

    
  }
