package isochrones.web.config;

import java.util.HashMap;
import java.util.Map;

public final class UsefulObjectFactory {
  
  private static Map<Integer, UsefulObject> store = new HashMap<Integer, UsefulObject>();
  
  public static final class UsefulObject {
      private UsefulObject(int parameter) {
          // init
      }
      public void someUsefulMethod() {
          // some useful operation
      }
  }
  public static UsefulObject get(int parameter) {
      synchronized (store) {
        UsefulObject result = store.get(parameter);
          if (result == null) {
              result = new UsefulObject(parameter);
              store.put(parameter, result);
          }
          return result;
      }
  }
  
  
  
  public static void main(String[] args) {
    UsefulObjectFactory.get(50);
  } 
}
