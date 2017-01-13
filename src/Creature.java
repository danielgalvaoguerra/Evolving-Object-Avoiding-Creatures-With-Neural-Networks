import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.concurrent.ThreadLocalRandom;

public class Creature {
	
	private static int WIDTH = 30;
	private static int HEIGHT = 30;
	private static int START_X = 50;
	private static float SPEED = 130.0f; // 2.75
	
	public boolean highlighted;
	public boolean hitObstacle;
	public boolean reachedTarget;
	public float fitness;
	public float[] weights;
	public int startX;
	public int startY;
	public int minDistToObstacle;
	public boolean facedObstacle;
	private boolean currentFaced;
	private boolean lastFaced;
	private boolean lastLastFaced;
	public int distanceTravelled;
	
	private int lastX;
	private int lastY;
	private CoordPair topLeft;
	private CoordPair botRight;
	private Obstacle obstacle;
	private Target target;
	private Rectangle rect;
	private int[][] rayCoords;
	
	private float[] inputNodes;
	private float[] hiddenNodes1; // holds the value of each hidden node
	private float[] hiddenEdges1; // holds the weight of the edges between inputs and hidden nodes
	private float[] hiddenNodes2;
	private float[] hiddenEdges2; // edges between the two hidden layers
	private float[] outputNodes; // holds the value of each output node
	private float[] outputEdges; // holds the weights between hidden and output nodes

	public Creature(float[] weights, Obstacle o, Target t)
	{
		double rand1 = ThreadLocalRandom.current().nextDouble();
		double rand2 = ThreadLocalRandom.current().nextDouble();
		//startX = (int)((Main.WINDOW_WIDTH/4)*rand1);
		//startY = (int)((Main.WINDOW_HEIGHT - HEIGHT)*rand2);
		startX = 50;
		startY = (int)((Main.WINDOW_HEIGHT - HEIGHT)*rand1);
		topLeft = new CoordPair(startX, startY);
		botRight = new CoordPair(topLeft.x+WIDTH, topLeft.y+HEIGHT);
		this.rect = new Rectangle((int)topLeft.x, (int)topLeft.y, WIDTH, HEIGHT);
		Rectangle temp = o.getRect();
		temp.setBounds(temp.x - 15, temp.y - 15, temp.width + 30, temp.height + 30);
		/*while(rect.intersects(temp) == true)
		{
			rand1 = ThreadLocalRandom.current().nextDouble();
			rand2 = ThreadLocalRandom.current().nextDouble();
			startX = (int)((Main.WINDOW_WIDTH/4)*rand1);
			startY = (int)((Main.WINDOW_HEIGHT - HEIGHT)*rand2);
			topLeft = new CoordPair(startX, startY);
			botRight = new CoordPair(topLeft.x+WIDTH, topLeft.y+HEIGHT);
			this.rect = new Rectangle((int)topLeft.x, (int)topLeft.y, WIDTH, HEIGHT);
		}*/
		this.obstacle = o;
		this.target = t;
		
		this.weights = weights;
		
		this.inputNodes = new float[Main.INPUT_NODES];
		this.hiddenNodes1 = new float[Main.HIDDEN_NODES];
		this.hiddenNodes2 = new float[Main.HIDDEN_NODES];
		this.outputNodes = new float[Main.OUTPUT_NODES];
		
		this.hiddenEdges1 = new float[Main.INPUT_NODES*Main.HIDDEN_NODES];
		this.hiddenEdges2 = new float[Main.HIDDEN_NODES*Main.HIDDEN_NODES];
		//this.outputEdges = new float[Main.HIDDEN_NODES*Main.OUTPUT_NODES];
		this.outputEdges = new float[Main.INPUT_NODES*Main.OUTPUT_NODES];
		
		//for no hidden layers
		/*for(int i=0; i<weights.length; i++)
			outputEdges[i] = weights[i];
		*/
		
		for(int i=0; i<hiddenEdges1.length; i++)
			hiddenEdges1[i] = weights[i];
		for(int i=hiddenEdges1.length; i<weights.length; i++)
			outputEdges[i - hiddenEdges1.length] = weights[i];
		
		/*
		for(int i=(hiddenEdges1.length+hiddenEdges2.length); i<weights.length; i++)
		{
			float a = weights[i];
			int l = hiddenEdges1.length + hiddenEdges2.length;
			int idx = i-l;
			outputEdges[idx] = a;
		}*/
		
		this.highlighted = false;
		this.hitObstacle = false;
		this.reachedTarget = false;
		this.minDistToObstacle = 3000;
		this.facedObstacle = false;
		this.lastFaced = false;
		this.lastLastFaced = false;
		this.lastX = startX;
		this.lastY = startY;
		rayCoords = new int[999][2];
		distanceTravelled = 0;
	}
	
	void update()
	{
		double dx = getX() - lastX;
		double dy = getY() - lastY;
		distanceTravelled += Math.abs(dx) + Math.abs(dy);
		
		rect = new Rectangle((int)topLeft.x, (int)topLeft.y, WIDTH, HEIGHT);
		if(rect.intersects(obstacle.getRect()))
			hitObstacle = true;

		if(rect.intersects(target.getRect()))
			reachedTarget = true;
			
		
		//(Math.random()*2 - 1)*SPEED
		// Populate input nodes
		inputNodes[0] = (float)(topLeft.x + WIDTH/2);
		inputNodes[1] = (float)(topLeft.y + HEIGHT/2);
		inputNodes[2] = (float)obstacle.getX();
		inputNodes[3] = (float)obstacle.getY();
		inputNodes[4] = Main.WINDOW_WIDTH;
		inputNodes[5] = Main.WINDOW_HEIGHT/2f;
		/*for(int i=0; i<inputNodes.length; i++){
			inputNodes[i] = 1f/(1f + (float)Math.exp((-1)*inputNodes[i]));
		}*/
		
		// for one hidden layer
		for(int hiddenNode1=0; hiddenNode1<Main.HIDDEN_NODES; hiddenNode1++)
		{
			hiddenNodes1[hiddenNode1] = 0;
			for(int inputNode=0; inputNode<Main.INPUT_NODES; inputNode++)
			{
				// Populating hiddenNodes array (runs 8*16 times)
				hiddenNodes1[hiddenNode1] += inputNodes[inputNode]*hiddenEdges1[hiddenNode1*Main.INPUT_NODES + inputNode];
			}
			hiddenNodes1[hiddenNode1] = (float)Math.tanh((0.005)*hiddenNodes1[hiddenNode1]);
		}
		for(int outputNode=0; outputNode<Main.OUTPUT_NODES; outputNode++)
		{
			outputNodes[outputNode] = 0;
			for(int hiddenNode=0; hiddenNode<Main.HIDDEN_NODES; hiddenNode++)
			{
				// Populating outputNodes array
				outputNodes[outputNode] += hiddenNodes1[hiddenNode]*outputEdges[outputNode*Main.HIDDEN_NODES + hiddenNode];
			}
			outputNodes[outputNode] = (float)Math.tanh((0.005)*outputNodes[outputNode]);
		}
		
		// two hidden layers
		/*
		for(int hiddenNode1=0; hiddenNode1<Main.HIDDEN_NODES; hiddenNode1++)
		{
			hiddenNodes1[hiddenNode1] = 0;
			for(int inputNode=0; inputNode<Main.INPUT_NODES; inputNode++)
			{
				// Populating hiddenNodes array (runs 8*16 times)
				hiddenNodes1[hiddenNode1] += inputNodes[inputNode]*hiddenEdges1[hiddenNode1*Main.INPUT_NODES + inputNode];
			}
		}
		for(int hiddenNode2=0; hiddenNode2<Main.HIDDEN_NODES; hiddenNode2++)
		{
			hiddenNodes2[hiddenNode2] = 0;
			for(int hiddenNode1=0; hiddenNode1<Main.HIDDEN_NODES; hiddenNode1++)
			{
				// Populating hiddenNodes array (runs 8*16 times)
				hiddenNodes2[hiddenNode2] += hiddenNodes1[hiddenNode1]*hiddenEdges2[hiddenNode2*Main.HIDDEN_NODES + hiddenNode1];
			}
		}
		for(int outputNode=0; outputNode<Main.OUTPUT_NODES; outputNode++)
		{
			outputNodes[outputNode] = 0;
			for(int hiddenNode2=0; hiddenNode2<Main.HIDDEN_NODES; hiddenNode2++)
			{
				// Populating outputNodes array
				outputNodes[outputNode] += hiddenNodes2[hiddenNode2]*outputEdges[outputNode*Main.HIDDEN_NODES + hiddenNode2];
			}
		}*/
		
		
		// for no hidden layers
		/*for(int outputNode=0; outputNode<Main.OUTPUT_NODES; outputNode++)
		{
			outputNodes[outputNode] = 0;
			for(int inputNode=0; inputNode<Main.INPUT_NODES; inputNode++)
			{
				outputNodes[outputNode] += inputNodes[inputNode]*outputEdges[outputNode*Main.OUTPUT_NODES + inputNode];
				outputNodes[outputNode] = 1f/(1f + (float)Math.exp((-1)*outputNodes[outputNode]));
			}
		}*/
		// output is between 0 and 1
		
		/*
		// normalizing values to be between 0.0 and 1.0 (max is 8000 min is 0, new max/min = 1/-1)
		double up = ((outputNodes[0])/(8000))*(2) - 1;
		double right = ((outputNodes[1])/(8000))*(2) - 1;
		double left = ((outputNodes[2])/(8000))*(2) - 1;
		double down = ((outputNodes[3])/(8000))*(2) - 1;
		*/
		
		/*double up = outputNodes[0]/6000;
		double right = outputNodes[1]/6000;
		double left = outputNodes[2]/6000;
		double down = outputNodes[3]/6000;*/
		double up = outputNodes[0];
		double right = outputNodes[1];
		double left = outputNodes[2];
		double down = outputNodes[3];
		topLeft.x += right*SPEED;
		topLeft.x -= left*SPEED;
		botRight.x += right*SPEED;
		botRight.x -= left*SPEED;
		
		topLeft.y += down*SPEED;
		topLeft.y -= up*SPEED;
		botRight.y += down*SPEED;
		botRight.y -= up*SPEED;
		
		lastX = (int)getX();
		lastY = (int)getY();
		lastLastFaced = lastFaced;
		lastFaced = currentFaced;
	}
	
	void draw(Graphics g)
	{
		/*if(highlighted){
			g.setColor(Color.GREEN);
			g.fillOval((int)topLeft.x, (int)topLeft.y, (int)(botRight.x-topLeft.x), (int)(botRight.y-topLeft.y));
		}*/
		if(reachedTarget)
			g.setColor(Color.GREEN);
		if(hitObstacle)
			g.setColor(Color.RED);
		g.drawOval((int)topLeft.x, (int)topLeft.y, (int)(botRight.x-topLeft.x), (int)(botRight.y-topLeft.y));
		//for(int i=0; i<rayCoords.length; i++)
			//g.drawOval(rayCoords[i][0], rayCoords[i][1], 5, 5);
		//g.drawString(String.valueOf(((outputNodes[1])/(8000))*(2) - 1), (int)botRight.x, (int)topLeft.y);
	
		g.setColor(Color.BLACK);
	}
	
	int distance(CoordPair a, CoordPair b)
	{
		return (int)Math.sqrt( (b.x-a.x)*(b.x-a.x) + (b.y-a.y)*(b.y-a.y) );
	}
	
	public double getX()
	{
		return (float)(topLeft.x + WIDTH/2);
	}
	
	public double getY()
	{
		return (float)(topLeft.y + HEIGHT/2);
	}
	
}
