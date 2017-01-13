import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.concurrent.ThreadLocalRandom;

public class Target {

	public static int WIDTH = 120;
	public static int HEIGHT = 120;
	
	public CoordPair topLeft;
	public CoordPair botRight;
	
	public Target()
	{
		/*double rand = ThreadLocalRandom.current().nextDouble();
		if(rand < 0.5)
		{
			if(rand < 0.25)
			{
				topLeft = new CoordPair(Main.WINDOW_WIDTH - WIDTH, 
									(Main.WINDOW_HEIGHT - HEIGHT)*ThreadLocalRandom.current().nextDouble());
			}
			else
			{
				topLeft = new CoordPair(0, 
									(Main.WINDOW_HEIGHT - HEIGHT)*ThreadLocalRandom.current().nextDouble());
			}
			botRight = new CoordPair(topLeft.x + WIDTH, topLeft.y + HEIGHT);
		} 
		else
		{
			if(rand < 0.75)
			{
				topLeft = new CoordPair((Main.WINDOW_WIDTH - HEIGHT)*ThreadLocalRandom.current().nextDouble(), 
											0);
			}
			else
			{
				topLeft = new CoordPair((Main.WINDOW_WIDTH - HEIGHT)*ThreadLocalRandom.current().nextDouble(), 
											Main.WINDOW_HEIGHT - WIDTH - 22);
			}
			botRight = new CoordPair(topLeft.x + HEIGHT, topLeft.y + WIDTH);
		}*/
		topLeft = new CoordPair(Main.WINDOW_WIDTH - WIDTH/2, Main.WINDOW_HEIGHT/2 - HEIGHT/2);
		botRight = new CoordPair(topLeft.x + WIDTH, topLeft.y + HEIGHT);
	}
	
	void draw(Graphics g)
	{
		g.drawRect((int)topLeft.x, (int)topLeft.y, (int)(botRight.x-topLeft.x), (int)(botRight.y-topLeft.y));
	}

	public int getX()
	{
		return (int)topLeft.x + WIDTH/2;
	}
	
	public int getY()
	{
		return (int)topLeft.y + HEIGHT/2;
	}
	
	public Rectangle getRect()
	{
		return new Rectangle((int)topLeft.x, (int)topLeft.y, WIDTH, HEIGHT);
	}
	
}
