import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.concurrent.ThreadLocalRandom;

public class Obstacle {
	
	public static int WIDTH = 100;
	public static int HEIGHT = 75;
	
	private static int spawnRegion_width = Main.WINDOW_WIDTH/6;
	private static int spawnRegion_height = Main.WINDOW_HEIGHT-HEIGHT;
	
	public CoordPair topLeft;
	public CoordPair botRight;
	
	public Obstacle(Target t)
	{
		/*topLeft = new CoordPair((Main.WINDOW_WIDTH - WIDTH)*ThreadLocalRandom.current().nextDouble(),
								(Main.WINDOW_HEIGHT - HEIGHT)*ThreadLocalRandom.current().nextDouble());
		/*topLeft = new CoordPair((Main.WINDOW_WIDTH/3) + WIDTH/2 + spawnRegion_width*ThreadLocalRandom.current().nextDouble(),
								(Main.WINDOW_HEIGHT - HEIGHT)*ThreadLocalRandom.current().nextDouble());*/
		/*botRight = new CoordPair(topLeft.x + WIDTH, topLeft.y + HEIGHT);
		while(getRect().intersects(t.getRect()) == true)
		{
			topLeft = new CoordPair((Main.WINDOW_WIDTH - WIDTH)*ThreadLocalRandom.current().nextDouble(),
									(Main.WINDOW_HEIGHT - HEIGHT)*ThreadLocalRandom.current().nextDouble());
			botRight = new CoordPair(topLeft.x + WIDTH, topLeft.y + HEIGHT);
		}*/
		topLeft = new CoordPair(Main.WINDOW_WIDTH/2 - WIDTH/2, Main.WINDOW_HEIGHT/2 - HEIGHT/2);
		botRight = new CoordPair(topLeft.x + WIDTH, topLeft.y + HEIGHT);
	}
	
	void draw(Graphics g)
	{
		//g.setColor(Color.BLACK);
		//Graphics2D g2 = (Graphics2D)g;
		g.drawRect((int)topLeft.x, (int)topLeft.y, WIDTH, HEIGHT);
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
