package isochrones.network;

public class Vertex implements Comparable<Vertex>{
  
  public int id;
  public double x,y;
  
  public Vertex(int id, double x, double y) {
    super();
    this.id = id;
    this.x = x;
    this.y = y;
  }

  @Override
  public int compareTo(Vertex o) {
    if(this.x<o.x) return -1;
    if(this.x>o.x) return 1;
    if(this.y<o.y) return -1;
    if(this.y>o.y) return 1;
    return 0;
  }
  
  
  
  

}
