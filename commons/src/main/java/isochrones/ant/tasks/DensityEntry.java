package isochrones.ant.tasks;

class DensityEntry {
  
  private int id, density;
  private double eDist;

  public DensityEntry(int id, int density, double eDist) {
    this.id = id;
    this.density = density;
    this.eDist = eDist;
  }

  public int getId() {
    return id;
  }

  public int getDensity() {
    return density;
  }

  public double getEDist() {
    return eDist;
  }
  
  
}
