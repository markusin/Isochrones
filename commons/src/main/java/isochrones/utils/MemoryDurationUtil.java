package isochrones.utils;


public class MemoryDurationUtil extends DurationUtil {

    public MemoryDurationUtil(String[] args, char sep) {
      super(args, sep);
      /*
      for (String arg : args) {
        String value = arg.substring(arg.indexOf(sep) + 1);
      }
      */
    }
    
    
    @Override
    protected String getExperimentDescription() {
      return "#### Starting memory duration experiment .... #####\n";
    }
}
