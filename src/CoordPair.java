
public class CoordPair {
	
	public double x;
	public double y;

	public CoordPair(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public CoordPair()
	{
		this.x = 0.0;
		this.y = 0.0;
	}
	
	public CoordPair addX(double dx)
	{
		return new CoordPair(this.x+dx, this.y);
	}
	
	public CoordPair addY(double dy)
	{
		return new CoordPair(this.x, this.y+dy);
	}
	
}
