package isochrones.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class TestEntry {
  
  private int duration;
  private SortedMap<Integer, TestInfo> linkEntries = new TreeMap<Integer, TestInfo>();
  private int frequency, invoked, numberOfTestedQueryPoints;
  
  public TestEntry(int duration, int frequency, int numberOfTestedQueryPoints) {
    this.duration = duration;
    this.frequency = frequency;
    this.numberOfTestedQueryPoints = numberOfTestedQueryPoints;
  }
  
  /**
   * 
   * <p>Method add</p>
   * @param linkId
   */
  public void add(Integer linkId) {
    TestInfo t;
    if(linkEntries.containsKey(linkId)) {
      t = linkEntries.get(linkId);
    } else {
      t = new TestInfo();
    }
    linkEntries.put(linkId, t);
  }
  
  /**
   * 
   * <p>Method isFullTested</p> test if every element in the testentry was entirely tested
   * @return
   */
  public boolean isFullTested() {
    return invoked == frequency * numberOfTestedQueryPoints;
  }
  
  /**
   * 
   * <p>Method isEntirelyTested</p> returns true, if for the specific link of interests the number
   * of invoked tests is equal to the test frequency.
   * @param linkId
   * @return true if every link was tested as many times as the frequency, else false
   */
  public boolean isEntirelyTested(int linkId) {
    return linkEntries.get(linkId).getInvocations() == frequency;
  }
  
  public int size() {
    return linkEntries.size();
  }
  
  /**
   * 
   * <p>Method getTestedLinkIds</p> returns the ordered set of tested links ids
   * @return
   */
  public Set<Integer> getTestedLinkIds() {
    return linkEntries.keySet();
  }
  
  
  /**
   * 
   * <p>Method getLinkTestInfos</p>
   * @return
   */
  public Collection<TestInfo> getLinkTestInfos() {
    return linkEntries.values();
  }

  /**
   * 
   * <p>Method addTime</p>
   * @param linkId
   * @return
   */
  public boolean addTime(int linkId, long time) {
    return linkEntries.get(linkId).addTime(time);
  }
  
  /**
   * 
   * <p>Method addElementsInMM</p>
   * @param linkId
   * @param elementsInMM
   */
  public void addElementsInMM(int linkId, int elementsInMM) {
    linkEntries.get(linkId).addElementsInMM(elementsInMM);
  }
  
  
  /**
   * 
   * <p>Method getDuration</p>
   * @return
   */
  public int getDuration() {
    return duration;
  }

  
  /**
   * 
   * <p>Method getRandomLink</p> return a random link to be tested. 
   * If the link is tested as many times as the frequency, the following 
   * link is taken. If all links are tested frequency time, then return null. 
   * @return
   */
  public Integer getRandomLink(){
    int probe = (int) Math.round(Math.random() * (size()-1));
    Integer[] durations = linkEntries.keySet().toArray(new Integer[0]);
    TestInfo durationEntry = linkEntries.get(durations[probe]);
    int passageNr = 0;
    while(durationEntry.getInvocations()==frequency && passageNr<linkEntries.size()) { 
      // choose the next, cause already tested
      probe = (probe + 1) % (linkEntries.size()); 
      durationEntry = linkEntries.get(durations[probe]);
      passageNr++;
    }
    return passageNr>linkEntries.size() ? null : durations[probe];
  }
  
  /**
   * 
  *
  * <p>The <code>TestInfo</code> class</p>
  * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
  * <p> Domenikanerplatz -  Bozen, Italy.</p>
  * <p> </p>
  * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
  * @version 2.2
   */
  public class TestInfo {
    int invocation;
    List<Long> runTimes = new ArrayList<Long>();
    int elementsInMM = 0;
    
    public List<Long> getRuntimes() {
      return runTimes;
    }
    
    public double getAverageRuntime(){
      long totalRuntime=0L;
      for (Long runTime : runTimes) {
        totalRuntime+=runTime;
      }
      return totalRuntime/invocation;
    }
    
    public int getInvocations() {
      return invocation;
    }
    
    public boolean addTime(long time) {
      boolean firstTime = runTimes.isEmpty();
      runTimes.add(time);
      invocation++;
      invoked++;
      return firstTime;
    }
    
    /**
     * 
     * <p>Method addElementsInMM</p>
     * @param nrOfElements
     */
    public void addElementsInMM(int nrOfElements) {
      if(this.elementsInMM>0) {
        if(this.elementsInMM!=nrOfElements){
          System.err.println("Something wrong with element!");
        }
      } else {
        this.elementsInMM = nrOfElements;
      }
    }
    
    /**
     * 
     * <p>Method getElementsInMM</p>
     * @return
     */
    public int getElementsInMM() {
      return elementsInMM;
    }
    
  }
  
}
