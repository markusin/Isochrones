package isochrones.utils;

/**
 * 
*
* <p>The <code>TupleEntry</code> class</p>
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p> </p>
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
 */
public class TupleEntry implements Comparable<TupleEntry> {
  int duration;
  int numberOfPedLinksTuple;
  int numberOfTransportLinksTuple;

  public TupleEntry(int duration, int numberOfPedLinksTuple, int numberOfTransportLinksTuple) {
    this.duration = duration;
    this.numberOfPedLinksTuple = numberOfPedLinksTuple;
    this.numberOfTransportLinksTuple = numberOfTransportLinksTuple;
  }

  public int getDuration() {
    return duration;
  }

  public int getLoadedTuplesPedLinks() {
    return numberOfPedLinksTuple;
  }
  
  public int getLoadedTuplesTransportLinks() {
    return numberOfTransportLinksTuple;
  }
  
  public int getLoadedTuples() {
    return numberOfTransportLinksTuple + numberOfPedLinksTuple;
  }
  
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }
  
  @Override
  public int hashCode() {
    return duration;
  }


  @Override
  public int compareTo(TupleEntry other) {
    final int BEFORE = -1;
    final int EQUAL = 0;
    final int AFTER = 1;

    if (this == other) {
        return EQUAL;
    }

    int value = Double.compare(this.duration, duration);

    if (value < 0) {
        return BEFORE;
    }
    if (value > 0) {
        return AFTER;
    }
    return EQUAL;
  }
  
}
