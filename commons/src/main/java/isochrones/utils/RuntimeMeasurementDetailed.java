package isochrones.utils;

public class RuntimeMeasurementDetailed extends RuntimeMeasurement {
  
  double rangeQueryRuntime = Double.MIN_VALUE, pointQueryRuntime = Double.MIN_VALUE;
  double densityTime = Double.MIN_VALUE, locationTime = Double.MIN_VALUE;
  double scheduleExactTime = Double.MIN_VALUE, scheduleHomoTime = Double.MIN_VALUE, scheduleHeteroTime = Double.MIN_VALUE;
  
  /**
   * 
   * <p>Constructs a(n) <code>RuntimeMeasurementDetailed</code> object.</p>
   * @param runtime
   * @param rangeQueryRuntime
   * @param pointQueryRuntime
   * @param densityTime
   * @param locationTime
   * @param scheduleExactTime
   * @param scheduleHomoTime
   * @param scheduleHeteroTime
   */
  public RuntimeMeasurementDetailed(double runtime, double rangeQueryRuntime, double pointQueryRuntime, 
                                    double densityTime, double locationTime, 
                                    double scheduleExactTime, double scheduleHomoTime, double scheduleHeteroTime) {
    super(runtime);
    this.runtime = runtime;
    this.rangeQueryRuntime = rangeQueryRuntime;
    this.pointQueryRuntime = pointQueryRuntime;
    
    this.densityTime = densityTime;
    this.locationTime = locationTime;
    this.scheduleExactTime = scheduleExactTime;
    this.scheduleHomoTime = scheduleHomoTime;
    this.scheduleHeteroTime = scheduleHeteroTime;
    
  }
  
  public double getRuntime() {
    return runtime;
  }
  
  public double getRangeQueryRuntime() {
    return rangeQueryRuntime;
  }
  
  public double getPointQueryRuntime() {
    return pointQueryRuntime;
  }
  
  public double getDensityTime() {
    return densityTime;
  }
  
  public double getLocationTime() {
    return locationTime;
  }
  
  public double getScheduleExactTime() {
    return scheduleExactTime;
  }
  
  public double getScheduleHomoTime() {
    return scheduleHomoTime;
  }
  
  public double getScheduleHeteroTime() {
    return scheduleHeteroTime;
  }

}
