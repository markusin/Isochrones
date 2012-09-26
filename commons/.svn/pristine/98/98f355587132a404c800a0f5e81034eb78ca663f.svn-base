package isochrones.launchers.algorithm;

import isochrones.algorithm.Isochrone;
import isochrones.utils.Config;
import isochrones.utils.LauncherUtil;
import isochrones.utils.ReflectionUtils;

import java.lang.reflect.Constructor;

public class IsochroneLauncher {
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public IsochroneLauncher(LauncherUtil util) {
    System.out.println(util.printInfo());
    Class testClass;
    Class[] intArgsClass = new Class[] { Config.class};

    try {
      testClass = Class.forName(util.getAlgorithmClassName());
      Constructor<Isochrone> constructor = testClass.getConstructor(intArgsClass);
      Object[] arguments = new Object[] {util.getConfig()};
      
      Isochrone algorithm = (Isochrone) ReflectionUtils.createObject(constructor, arguments);
      long time1 = System.currentTimeMillis();
      if(util.getNodesOfInterest()!=null){
        System.out.println("Node is q point");
        algorithm.computeIsochrone(util.getNodesOfInterest(), util.getMaxDuration(), util.getSpeed(), util.getTime());  
      } else {
        algorithm.computeIsochrone(util.getLocations(), util.getMaxDuration(), util.getSpeed(), util.getTime());
      }
      System.out.println("Total running time: " + (System.currentTimeMillis() - time1) + " ms");
      algorithm.printStatistics(util.getAlgorithmName());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  /**
   * <p>Method main</p>
   * @param args 
   */
  public static void main(String[] args) {
    new IsochroneLauncher(new LauncherUtil(args, '='));
  }

}
