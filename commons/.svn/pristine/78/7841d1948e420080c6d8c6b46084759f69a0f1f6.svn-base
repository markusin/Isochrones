package isochrones.utils;

public class MemoryEntry  {
  int isoSize, isoContinuousLinkSize, isoBusLinkSize, isoNodeSize, traceContinuousLinkSize, traceDiscreteLinkSize, traceNodeSize, duration;
  

  public MemoryEntry(int isoContinuousLinkSize, int isoBusLinkSize, int isoNodeSize, int traceContinuousLinkSize, int traceDiscreteLinkSize, int traceNodeSize) {
    this.isoSize = isoContinuousLinkSize + isoBusLinkSize + isoNodeSize;
    this.isoContinuousLinkSize = isoContinuousLinkSize;
    this.isoBusLinkSize = isoBusLinkSize;
    this.isoNodeSize = isoNodeSize;
    this.traceContinuousLinkSize = traceContinuousLinkSize;
    this.traceDiscreteLinkSize = traceDiscreteLinkSize;
    this.traceNodeSize = traceNodeSize;
  }
  
  public MemoryEntry(int isoContinuousLinkSize, int isoBusLinkSize, int isoNodeSize, int traceContinuousLinkSize, int traceDiscreteLinkSize, int traceNodeSize, int duration) {
    this.isoSize = isoContinuousLinkSize + isoBusLinkSize + isoNodeSize;
    this.isoContinuousLinkSize = isoContinuousLinkSize;
    this.isoBusLinkSize = isoBusLinkSize;
    this.isoNodeSize = isoNodeSize;
    this.traceContinuousLinkSize = traceContinuousLinkSize;
    this.traceDiscreteLinkSize = traceDiscreteLinkSize;
    this.traceNodeSize = traceNodeSize;
    this.duration = duration;
    
  }

  public int getIsoSize() {
    return isoSize;
  }

  public int getIsoContinuousEdgeSize() {
    return isoContinuousLinkSize;
  }
  
  public int getIsoDiscreteEdgeSize() {
    return isoBusLinkSize;
  }
  
  public int getIsoNodeSize() {
    return isoNodeSize;
  }
  
  
  public int getTraceContinuousEdgeSize() {
    return traceContinuousLinkSize;
  }
  
  public int getTraceDiscreteEdgeSize() {
    return traceDiscreteLinkSize;
  }

  public int getTraceNodeSize() {
    return traceNodeSize;
  }

  
  public int getDuration() {
    return duration;
  }
}
