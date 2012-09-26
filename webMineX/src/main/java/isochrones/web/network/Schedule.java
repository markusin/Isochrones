package isochrones.web.network;


public class Schedule {
  
  int routeId;
  long departureTime, arrivalTime;
  
  public Schedule(int routeId) {
    this.routeId = routeId;
  }
  
  public void setDepartureTime(long departureTime) {
    this.departureTime = departureTime;
  }
  
  public void setArrivalTime(long arrivalTime) {
    this.arrivalTime = arrivalTime;
  }
  
  public int getRouteId() {
    return routeId;
  }
  
  public long getArrivalTime() {
    return arrivalTime;
  }
  
  public long getDepartureTime() {
    return departureTime;
  }

}
