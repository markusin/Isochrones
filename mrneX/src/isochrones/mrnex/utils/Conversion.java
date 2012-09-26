package isochrones.mrnex.utils;

import isochrones.mrnex.network.link.ADiscreteLink;
import isochrones.network.link.DiscreteLink;

import java.util.Collection;
import java.util.HashSet;

public class Conversion {
  
  /**
   * 
   * <p>Method asDiscreteLinks</p>
   * @param fromLinks
   * @return
   */
  public static Collection<DiscreteLink> asDiscreteLinks(Collection<ADiscreteLink> fromLinks) {
    if(fromLinks==null) return null;
    HashSet<DiscreteLink> toLinks = new HashSet<DiscreteLink>(fromLinks.size());
    for (ADiscreteLink link : fromLinks) {
      toLinks.add(link.getLink());
    }
    return toLinks;
  }

}
