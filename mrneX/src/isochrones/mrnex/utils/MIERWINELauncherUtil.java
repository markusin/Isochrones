package isochrones.mrnex.utils;

import isochrones.utils.LauncherUtil;

public class MIERWINELauncherUtil extends LauncherUtil {

  boolean useDensity = true, debug = false;
  
  public MIERWINELauncherUtil(String[] args, char sep) {
    super(args, sep);
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      if (arg.startsWith("useDenisity")) {
        useDensity = Boolean.valueOf(value);
      } else if (arg.startsWith("debug")) {
        debug = Boolean.valueOf(value);
      }
    }
  }
  
  /**
   * 
   * <p>Method isUsedDensity</p>
   * @return
   */
  public boolean isUsedDensity() {
    return useDensity;
  }
  
  /**
   * 
   * <p>Method isDebugMode</p>
   * @return
   */
  public boolean isDebugMode() {
    return debug;
  }

}
