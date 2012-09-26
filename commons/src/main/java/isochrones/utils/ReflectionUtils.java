package isochrones.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {
  /**
   * 
   * <p>Method createObject</p>
   * @param constructor
   * @param arguments
   * @return
   */
  public static Object createObject(Constructor<?> constructor, Object[] arguments) {
//    System.out.println("Constructor: " + constructor.toString());
    Object object = null;

    try {
      object = constructor.newInstance(arguments);
//      System.out.println("Object: " + object.toString());
      return object;
    } catch (InstantiationException e) {
      System.out.println(e);
    } catch (IllegalAccessException e) {
      System.out.println(e);
    } catch (IllegalArgumentException e) {
      System.out.println(e);
    } catch (InvocationTargetException e) {
      System.out.println(e);
    }
    return object;
  }

}
