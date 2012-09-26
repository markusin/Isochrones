package isochrones.web.comet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextLoaderListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("Context initialized");
    
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    System.out.println("Context destroyed");
    
  }

}
