package isochrones.web.utils;

import isochrones.utils.LauncherUtil;
import isochrones.web.config.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class WebLauncherUtil extends LauncherUtil {

  private Config config;

  public WebLauncherUtil(String[] args, char sep) {
    super(args, sep);
    try {
      config = new Config(getDBVendor(),new FileInputStream(new File(getConfigDir() + "config.xml")));
      config.appendPropertyFile(new FileInputStream(new File(getConfigDir() + "config_" + getDataset().toString().toLowerCase() + ".xml")));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public Config getConfig() {
    return config;
  }

}
