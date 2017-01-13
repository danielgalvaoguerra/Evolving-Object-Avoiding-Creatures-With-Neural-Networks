import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Main extends JPanel implements WindowListener{
	
	public static int WINDOW_WIDTH = 800;
	public static int WINDOW_HEIGHT = 800;
	private static int global_counter = 0;
	private static int N = 200; // number of creatures per generation
	private static int GENERATION_LIFESPAN = 2;
	private static int FRAMES_PER_GENERATION = 1200;
	
	public static int INPUT_NODES = 6;
	public static int HIDDEN_NODES = 4;
	public static int OUTPUT_NODES = 4;
	//public static int NUM_CONNECTIONS = INPUT_NODES*HIDDEN_NODES + HIDDEN_NODES*HIDDEN_NODES + HIDDEN_NODES*OUTPUT_NODES;
	public static int NUM_CONNECTIONS = INPUT_NODES*HIDDEN_NODES + HIDDEN_NODES*OUTPUT_NODES;
	//public static int NUM_CONNECTIONS = INPUT_NODES*OUTPUT_NODES;

	public Timer paintTimer;
	public Timer computeTimer;
	public Timer FPSTimer;
	TimerTask makeNewGeneration;
	public int frameCount_FPS;
	public int frameCount_generation;
	
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame = new JFrame();
		frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Evolving Collision-Avoiding Creatures");
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		
		Main window = new Main();
		frame.addWindowListener(window);
		frame.add(window);
		window.setFocusable(true);
		window.requestFocusInWindow();
		frame.setVisible(true);
	}
	
	private Creature[] creatures;
	private ArrayList<float[]> genePool;
	private Obstacle obstacle;
	private Target target;
	private int generation;
	
	//private float localBestFitness;
	//private int bestIndex;
	
	public Main()
	{
		/*createStartingPop();
		readLastGeneration();*/
		
		creatures = new Creature[N];
		genePool = new ArrayList<>();
		target = new Target();
		obstacle = new Obstacle(target);
		for(int i=0; i<N; i++)
			creatures[i] = new Creature(makeRandomWeights(), obstacle, target);
		
		//readLastGeneration();
		
		generation--; // cuz startNewGeneration does generation++
		startNewGeneration();
		frameCount_FPS = 0;
		frameCount_generation = 0;

		Thread paintThread = new Thread(new Runnable() {
			public void run() {
				startPaintTimer();
			}
		});
		paintThread.start();
		
		Thread computeThread = new Thread(new Runnable() {
			public void run() {
				startComputeTimer();
			}
		});
		computeThread.start();
		
		TimerTask updateFPS = new TimerTask() {
			public void run() {
				System.out.println(frameCount_FPS);
				frameCount_FPS = 0;
			}
		};
		FPSTimer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFPS.run();
			}
		});
		FPSTimer.start();
		
		makeNewGeneration = new TimerTask() {
			public void run() {
				genePool.clear();
				//localBestFitness = 2000;
				for(int creatureIndex=0; creatureIndex<N; creatureIndex++)
				{
					Creature c = creatures[creatureIndex];
					float start_dist_target = dist(Math.abs(target.getX() - c.startX), Math.abs(target.getY() - c.startY));
					float end_dist_target = dist((float)Math.abs(target.getX() - c.getX()), (float)Math.abs(target.getY() - c.getY()));
					//float start_dist_obstacle = dist(Math.abs(obstacle.getX() - c.startX), Math.abs(obstacle.getY() - c.startY));
					//float end_dist_obstacle = dist((float)Math.abs(obstacle.getX() - c.getX()), (float)Math.abs(obstacle.getY() - c.getY()));
					//float currentFitness = (start_dist_target - end_dist_target)/start_dist_target;
					float sd = dist(Math.abs(Main.WINDOW_WIDTH - c.startX)/2f, Math.abs(Main.WINDOW_HEIGHT/2 - c.startY));
					float ed = dist((float)Math.abs(Main.WINDOW_WIDTH - c.getX())/2f, (float)Math.abs(Main.WINDOW_HEIGHT/2 - c.getY()));
					float currentFitness = (1/c.distanceTravelled)*800;
					if(c.reachedTarget == true && c.hitObstacle == false)
					{
						for(int i=0; i<1000; i++)
							genePool.add(c.weights);
					}
					//if(c.minDistToObstacle != c.startX && c.reachedTarget)
						//currentFitness += 1.0;
					/*if(c.facedObstacle == true && c.hitObstacle == false)
						currentFitness += 0.25;*/
					if(currentFitness < 0 || c.getX() < 0 || c.hitObstacle)
						currentFitness = 0.001f;
					
					c.fitness = currentFitness;
					// WANT TO MAXIMIZE FITNESS (6.0 is best, 0.001 is worst)
				}
				
				/*for(int i=0; i<N; i++)
				{
					for(int j=0; j<(int)(1000f*creatures[i].fitness); j++)
						genePool.add(creatures[i].weights);
				}*/
				
				target = new Target();
				obstacle = new Obstacle(target);
				
				if(genePool.size() == 0)
				{
					System.out.println("NO CREATURES ----");
					for(int i=0; i<N; i++)
						creatures[i] = new Creature(makeRandomWeights(), obstacle, target);
				}
				else
				{
					for(int i=0; i<N; i++)
					{
						float[] childGenes;
						int randIdx = ThreadLocalRandom.current().nextInt(0, genePool.size());
						if(ThreadLocalRandom.current().nextInt(0, 100) == 4){
							System.out.println("MUTATION");
							if(ThreadLocalRandom.current().nextInt(0, 50) == 4){
								childGenes = makeRandomWeights();
							}
							else
							{
								childGenes = mutateGenes(genePool.get(randIdx));
							}
						}else{
							childGenes = makeChild(genePool.get(randIdx));
						}
						
						creatures[i] = new Creature(childGenes, obstacle, target);
					}
				}
				
				startNewGeneration();
				saveLastGeneration();
			}
		};
		/*Timer generationTimer = new Timer(GENERATION_LIFESPAN*1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makeNewGeneration.run();
			}
		});
		generationTimer.start();*/
	}
	
	private void startNewGeneration()
	{
		generation++;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		//System.out.println((System.currentTimeMillis() - lastFrame)/1000);
		frameCount_generation++;
		frameCount_FPS++;
		
		g.setColor(Color.BLACK);
		g.drawString("Generation: "+String.valueOf(generation), 20, 20);
		
		// update and draw current creatures
		for(Creature c : creatures)
		{
			//c.update();
			c.draw(g);
		}
		obstacle.draw(g);
		target.draw(g);
		
		if(frameCount_generation == FRAMES_PER_GENERATION)
		{
			frameCount_generation = 0;
			makeNewGeneration.run();
		}
		
		//repaint();
	}
	
	float[] makeChild(float[] parentGenes)
	{
		float[] a = new float[parentGenes.length];
		
		for(int i=0; i<parentGenes.length; i++)
		{
			if(ThreadLocalRandom.current().nextInt(0, 101) < 50)	
			{
				a[i] = (float)(parentGenes[i] + (Math.random()/(10.0) - 0.05));
			}
			else
				a[i] = parentGenes[i];
			
			if(a[i] > 1.0f) a[i] = 1.0f;
			if(a[i] < -1.0f) a[i] = -1.0f;
		}
		
		return a;
	}
	
	float[] mutateGenes(float[] parentGenes)
	{
		float[] a = new float[parentGenes.length];
		
		for(int i=0; i<parentGenes.length; i++)
		{
			if(ThreadLocalRandom.current().nextInt(0, 101) < 66)
				a[i] = (float)(parentGenes[i] + (Math.random()/2.0 - 0.25));
			else
				a[i] = (float)(parentGenes[i]);
			
			if(a[i] > 1.0f) a[i] = 1.0f;
			if(a[i] < -1.0f) a[i] = -1.0f;
		}
		
		return a;
	}
	
	float[] makeRandomWeights()
	{
		float[] a = new float[NUM_CONNECTIONS];
		for(int i=0; i<NUM_CONNECTIONS; i++){
			a[i] = (float)(ThreadLocalRandom.current().nextDouble()*2.0 - 1.0);
		}

		return a;
	}
	
	void readLastGeneration()
	{
		creatures = new Creature[N];
		target = new Target();
		obstacle = new Obstacle(target);
		int counter = 0;
		
		float[] w = new float[NUM_CONNECTIONS];
		List<String> lines = new ArrayList<String>();
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader("data/last_generation.txt"));
			String line = br.readLine();
			generation = Integer.parseInt(line);
			
			line = br.readLine();
			while (line != null)
			{
				String[] split = line.split(",");
				for(int i=0; i<split.length; i++)
				{
					w[i] = Float.valueOf(split[i]);
				}
				creatures[counter] = new Creature(w, obstacle, target);
				counter++;
				
			    lines.add(line);
			    line = br.readLine();
			}
			br.close();
			
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
	}
	
	void saveLastGeneration()
	{
		FileWriter fileWriter;
		PrintWriter printWriter;
		
		try {
			fileWriter = new FileWriter(new File("data/last_generation.txt"));
			printWriter = new PrintWriter(fileWriter);
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		printWriter.println(generation);
		for(Creature c : creatures)
		{
			float[] w = c.weights;
			for(int j=0; j<w.length; j++)
			{
				printWriter.print(w[j]+",");
			}
			printWriter.println("");
		}
		
		printWriter.close();
	}
	
	float dist(float a, float b)
	{
		return (float)Math.sqrt(a*a + b*b);
	}
	
	void startPaintTimer()
	{
		paintTimer = new Timer(1, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		paintTimer.start();
	}
	
	void startComputeTimer()
	{
		computeTimer = new Timer(1, new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				for(Creature c : creatures)
				{
					c.update();
				}
			}
		});
		computeTimer.start();
	}
	
	public void windowClosed(WindowEvent e) {
		System.out.println("LAJKSDHFALSKJDF AKSJD FLAKJSDHFLKAJSDFLAJSD FP98A SDP98FUAP EJFQ9898239PRHA SDF AX X");
		saveLastGeneration();
	}
	
	public void windowOpened(WindowEvent e) {
	}
	public void windowClosing(WindowEvent e) {
	}
	public void windowIconified(WindowEvent e) {
	}
	public void windowDeiconified(WindowEvent e) {
	}
	public void windowActivated(WindowEvent e) {
	}
	public void windowDeactivated(WindowEvent e) {}
}
