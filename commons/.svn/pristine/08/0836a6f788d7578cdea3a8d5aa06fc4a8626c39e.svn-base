package isochrones.utils;

import isochrones.utils.entries.Detail;
import isochrones.utils.entries.RuntimeDetailedEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class AggregateUtil {

  private static Comparator<Long> comparator = new Comparator<Long>() {
    @Override
    public int compare(Long o1, Long o2) {
      final int BEFORE = -1;
      final int EQUAL = 0;
      final int AFTER = 1;

      if (o1 < o2) {
        return BEFORE;
      }
      if (o1 > o2) {
        return AFTER;
      }
      return EQUAL;
    }
  };

  public static SortedMap<Integer, RuntimeMeasurement> computeMedian(List<SortedMap<Integer, RuntimeEntry>> runTimes) {
    SortedMap<Integer, RuntimeMeasurement> output = new TreeMap<Integer, RuntimeMeasurement>();
    SortedMap<Integer, List<Long>> runtimesClustered = new TreeMap<Integer, List<Long>>();

    for (SortedMap<Integer, RuntimeEntry> runTime : runTimes) {
      for (Entry<Integer, RuntimeEntry> entry : runTime.entrySet()) {
        int currentDur = entry.getKey();
        if (!runtimesClustered.containsKey(currentDur)) {
          runtimesClustered.put(entry.getKey(), new ArrayList<Long>());
        }
        runtimesClustered.get(currentDur).add(entry.getValue().getRuntime());
      }
    }

    for (Integer dMax : runtimesClustered.keySet()) {
      List<Long> runtimeValues = runtimesClustered.get(dMax);
      Collections.sort(runtimeValues, AggregateUtil.comparator);
      RuntimeMeasurement runtimeMeasurement = new RuntimeMeasurement(median(runtimeValues));
      output.put(dMax, runtimeMeasurement);
    }
    return output;
  }

  public static SortedMap<Integer, RuntimeDetailedEntry> computeMedianDetailed(List<SortedMap<Integer, RuntimeDetailedEntry>> runtimes) {

    HashMap<Detail, SortedMap<Integer, List<Long>>> runtimeValues = new HashMap<Detail, SortedMap<Integer, List<Long>>>();
    Map<Detail, Long> times = runtimes.iterator().next().values().iterator().next().getRuntimes();
    
    

    SortedMap<Integer, RuntimeDetailedEntry> medianValues = new TreeMap<Integer, RuntimeDetailedEntry>();

    for (Detail runtimeDetail : times.keySet()) {
      runtimeValues.put(runtimeDetail, new TreeMap<Integer, List<Long>>());
    }

    for (SortedMap<Integer, RuntimeDetailedEntry> runTime : runtimes) {
      
      for (Entry<Integer, RuntimeDetailedEntry> entry : runTime.entrySet()) {
        int currentDur = entry.getKey();
        Map<Detail, Long> currentRuntime = entry.getValue().getRuntimes();
        for (Detail runtimeDetail : currentRuntime.keySet()) {
          SortedMap<Integer,List<Long>> detailValues = runtimeValues.get(runtimeDetail);
          if(!detailValues.containsKey(currentDur)){
            detailValues.put(currentDur, new ArrayList<Long>());
          }
          detailValues.get(currentDur).add(currentRuntime.get(runtimeDetail));
        }
      }
    }

    for (Detail detail : runtimeValues.keySet()) {
      SortedMap<Integer, List<Long>> runtimesPerDuration = runtimeValues.get(detail);
      for (Integer currentDuration : runtimesPerDuration.keySet()) {
        List<Long> values = runtimesPerDuration.get(currentDuration);
        Collections.sort(values, AggregateUtil.comparator);
        if (!medianValues.containsKey(currentDuration)) {
          medianValues.put(currentDuration, new RuntimeDetailedEntry());
        }
        medianValues.get(currentDuration).getRuntimes().put(detail, Math.round(median(values)));
        // adding lookups and loaded 
        RuntimeDetailedEntry firstEntry = runtimes.iterator().next().get(currentDuration);
        medianValues.get(currentDuration).addLookups(firstEntry.getLookups());
        medianValues.get(currentDuration).addLoaded(firstEntry.getLoaded());
      }
    }
    return medianValues;
    // done for average
  }

  /**
   * <p>
   * Method computeAverage
   * </p>
   * 
   * @param runTimes
   * @return
   */
  public static SortedMap<Integer, RuntimeMeasurement> computeAverage(List<SortedMap<Integer, RuntimeEntry>> runTimes) {
    SortedMap<Integer, RuntimeMeasurement> output = new TreeMap<Integer, RuntimeMeasurement>();
    SortedMap<Integer, List<Long>> runtimesClustered = new TreeMap<Integer, List<Long>>();

    for (SortedMap<Integer, RuntimeEntry> runTime : runTimes) {
      for (Entry<Integer, RuntimeEntry> entry : runTime.entrySet()) {
        int currentDur = entry.getKey();
        if (!runtimesClustered.containsKey(currentDur)) {
          runtimesClustered.put(entry.getKey(), new ArrayList<Long>());
        }
        runtimesClustered.get(currentDur).add(entry.getValue().getRuntime());
      }
    }

    for (Integer dMax : runtimesClustered.keySet()) {
      List<Long> runtimeValues = runtimesClustered.get(dMax);

      Collections.sort(runtimeValues, AggregateUtil.comparator);

      RuntimeMeasurement runtimeMeasurement = new RuntimeMeasurement(average(runtimeValues));
      output.put(dMax, runtimeMeasurement);
    }
    return output;
  }

  public static SortedMap<Integer, RuntimeDetailedEntry> computeAverageDetailed(List<SortedMap<Integer, RuntimeDetailedEntry>> runtimes) {

    HashMap<Detail, SortedMap<Integer, List<Long>>> runtimeValues = new HashMap<Detail, SortedMap<Integer, List<Long>>>();
    Map<Detail, Long> times = runtimes.iterator().next().values().iterator().next().getRuntimes();

    SortedMap<Integer, RuntimeDetailedEntry> averageValues = new TreeMap<Integer, RuntimeDetailedEntry>();

    for (Detail runtimeDetail : times.keySet()) {
      runtimeValues.put(runtimeDetail, new TreeMap<Integer, List<Long>>());
    }

    for (SortedMap<Integer, RuntimeDetailedEntry> runTime : runtimes) {
      for (Entry<Integer, RuntimeDetailedEntry> entry : runTime.entrySet()) {
        int currentDur = entry.getKey();
        Map<Detail, Long> currentRuntime = entry.getValue().getRuntimes();
        for (Detail runtimeDetail : currentRuntime.keySet()) {
          SortedMap<Integer,List<Long>> detailValues = runtimeValues.get(runtimeDetail);
          if(!detailValues.containsKey(currentDur)){
            detailValues.put(currentDur, new ArrayList<Long>());
          }
          detailValues.get(currentDur).add(currentRuntime.get(runtimeDetail));
        }
      }
    }

    for (Detail detail : runtimeValues.keySet()) {
      SortedMap<Integer, List<Long>> runtimesPerDuration = runtimeValues.get(detail);
      for (Integer currentDuration : runtimesPerDuration.keySet()) {
        List<Long> values = runtimesPerDuration.get(currentDuration);
        Collections.sort(values, AggregateUtil.comparator);
        if (!averageValues.containsKey(currentDuration)) {
          averageValues.put(currentDuration, new RuntimeDetailedEntry());
        }
        averageValues.get(currentDuration).getRuntimes().put(detail, Math.round(average(values)));
        // adding lookups and loaded 
        RuntimeDetailedEntry firstEntry = runtimes.iterator().next().get(currentDuration);
        averageValues.get(currentDuration).addLookups(firstEntry.getLookups());
        averageValues.get(currentDuration).addLoaded(firstEntry.getLoaded());
      }
    }
    return averageValues;
    // done for average
  }

  public static double median(Long[] values) {
    int middle = values.length / 2; // subscript of middle element
    if (values.length % 2 == 1) {
      // Odd number of elements -- return the middle one.
      return values[middle];
    } else {
      // Even number -- return average of middle two
      // Must cast the numbers to double before dividing.
      return (values[middle - 1] + values[middle]) / 2.0;
    }
  }
  
  public static double median(Double[] values) {
    int middle = values.length / 2; // subscript of middle element
    if (values.length % 2 == 1) {
      // Odd number of elements -- return the middle one.
      return values[middle];
    } else {
      // Even number -- return average of middle two
      // Must cast the numbers to double before dividing.
      return (values[middle - 1] + values[middle]) / 2.0;
    }
  }

  public static double median(List<Long> values) {
    return median(values.toArray(new Long[values.size()]));
  }
  
  public static double average(Collection<Long> values) {
    double avg = 0;
    for (Long val : values) {
      avg += val;
    }
    avg = avg / values.size();
    return avg;
  }
  
  public static double average(Double[] values) {
    double avg = 0;
    for (int i = 0; i < values.length; i++) {
      avg += values[i];
    }
    return avg / values.length;
  }
  
  /**
   * 
   * <p>Method singleAverage</p> returns the average runtime per each single call
   * @param runtime
   * @param calls
   * @return return zero, if either runtime or calls is equal to zero otherwise the average
   */
  public static double singleAverage(double runtime, int calls){
    if(calls==0 || runtime==0) return 0;
    return (double) runtime / calls;
  }

}
