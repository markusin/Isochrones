/**
 * 
 */
package isochrones.utils;

/**
 *
 * <p>The <code>ObjectSizer</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
/**
* Measures the approximate size of an object in memory, given a Class which
* has a no-argument constructor.
*/
public final class ObjectSizer {

  /**
  * First and only argument is the package-qualified name of a class
  * which has a no-argument constructor.
  */
  public static void main(String... aArguments){
    Class theClass = null;
    try {
      theClass = Class.forName(aArguments[0]);
    }
    catch (Exception ex) {
      System.err.println("Cannot build a Class object: " + aArguments[0]);
      System.err.println("Use a package-qualified name, and check classpath.");
    }
    long size = ObjectSizer.getObjectSize( theClass );
    System.out.println("Approximate size of " + theClass + " objects :" + size);
  }
  
  public static long getObjectSize(String className) {
    try {
      return ObjectSizer.getObjectSize(Class.forName(className));
    } catch (ClassNotFoundException e) {
      System.err.println("Cannot build a Class object: " + className);
      System.err.println("Use a package-qualified name, and check classpath.");
      e.printStackTrace();
    }
    return -1;
  }


  /**
  * Return the approximate size in bytes, and return zero if the class
  * has no default constructor.
  *
  * @param aClass refers to a class which has a no-argument constructor.
  */
  public static long getObjectSize( Class aClass ){
    long result = 0;

    //if the class does not have a no-argument constructor, then
    //inform the user and return 0.
    try {
      aClass.getConstructor( new Class[]{} );
    }
    catch ( NoSuchMethodException ex ) {
      System.err.println(aClass + " does not have a no-argument constructor.");
      return result;
    }

    //this array will simply hold a bunch of references, such that
    //the objects cannot be garbage-collected
    Object[] objects = new Object[fSAMPLE_SIZE];

    //build a bunch of identical objects
    try {
      Object throwAway = aClass.newInstance();

      long startMemoryUse = getMemoryUse();
      for (int idx=0; idx < objects.length ; ++idx) {
        objects[idx] = aClass.newInstance();
      }
      long endMemoryUse = getMemoryUse();

      float approximateSize = ( endMemoryUse - startMemoryUse ) /100f;
      result = Math.round( approximateSize );
    }
    catch (Exception ex) {
      System.err.println("Cannot create object using " + aClass);
    }
    return result;
  }

  // PRIVATE //
  private static int fSAMPLE_SIZE = 100;
  private static long fSLEEP_INTERVAL = 100;

  private static long getMemoryUse(){
    putOutTheGarbage();
    long totalMemory = Runtime.getRuntime().totalMemory();

    putOutTheGarbage();
    long freeMemory = Runtime.getRuntime().freeMemory();

    return (totalMemory - freeMemory);
  }

  private static void putOutTheGarbage() {
    collectGarbage();
    collectGarbage();
  }

  private static void collectGarbage() {
    try {
      System.gc();
      Thread.currentThread().sleep(fSLEEP_INTERVAL);
      System.runFinalization();
      Thread.currentThread().sleep(fSLEEP_INTERVAL);
    }
    catch (InterruptedException ex){
      ex.printStackTrace();
    }
  }
} 
