package isochrones.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OSUtil {
  
  /**
   * 
   * <p>Method clearDiskCache</p> clears the disk cache on operating system level.
   * Be aware:
   *  - you need to be either root or in root group.
   *  - command works only with Linux
   */
  public static void clearDiskCache()  {
      java.lang.Runtime rt = java.lang.Runtime.getRuntime();
      System.out.println("Clear OS disk cache .... ");
      try {
        
        Process p=Runtime.getRuntime().exec("sudo sync; sudo sh -c 'echo 3 > /proc/sys/vm/drop_caches'"); 
        p.waitFor(); 
        BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
        String line=reader.readLine(); 
        while(line!=null) { 
          System.out.println(line); 
          line=reader.readLine(); 
        }
        
        //rt.exec("sync");
       // rt.exec("sync && echo 3 > /proc/sys/vm/drop_caches");
        
        // java.lang.Process p = rt.exec("sync; echo 3 > /proc/sys/vm/drop_caches");
        Thread.sleep(200);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
  }
  
  public static void main(String[] args) {
    OSUtil.clearDiskCache();
  }
  
  
}