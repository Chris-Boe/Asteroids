import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class AsteroidsGame extends Applet implements Runnable, KeyListener {
	int x, y, xVelocity, yVelocity; // velocity variables store the direction in
									// which the circle is currently moving
	Thread thread;
	long startTime, endTime, framePeriod; // long=very big integer
	Dimension dim; // stores the size of the back buffer
	Image img; // the back buffer object
	Graphics g; // used to draw on the back buffer
	Ship ship;
	boolean paused; // True if game is paused. Enter is pause key.
	Shot[] shots; // variable that stores the new array of Shots
	int numShots; // Stores the number of shots in the array
	boolean shooting; // true if the ship is currently shooting
	Asteroid[] asteroids;
	int numAsteroids;
	double astRadius, minAstVel, maxAstVel;
	int astNumHits, astNumSplit;
	int level;
	int points;
	static int lives = 5;

	public void init() {
		resize(500, 500); // make sure the applet is the right size
		ship = new Ship(250, 250, 0, .35, .98, .1, 12); // creates the ship
		paused = true;
		shots = new Shot[41]; // Allocate the space for the array.
		// We allocate enough space to store the maximum number of
		// shots that can possibly be on the screen at one time.
		// 41 is the max because no more than one shot can be fired per
		// frame and shots only last for 40 frames (40 is the value passed
		// in for lifeLeft when shots are created)
		numShots = 0; // no shots on the screen to start with
		shooting = false; // ship is not shooting
		numAsteroids = 0;
		level = 0;
		astRadius = 60;
		minAstVel = .5;
		maxAstVel = 5;
		astNumHits = 3;
		astNumSplit = 2;
		addKeyListener(this);
		startTime = 0;
		endTime = 0;
		framePeriod = 25; // 25 milliseconds
		dim = getSize(); // set dim equal to the size of the applet
		img = createImage(dim.width, dim.height); // create the back buffer
		g = img.getGraphics(); // retrieve Graphics object for back buffer
		thread = new Thread(this); // create the thread
		thread.start(); // start the thread running
	}

	public void setUpNextLevel() {
		level++;
		ship = new Ship(250, 250, 0, .35, .98, .1, 12);
		numShots = 0;
		paused = false;
		shooting = false;
		asteroids = new Asteroid[level
				* (int) Math.pow(astNumSplit, astNumHits - 1) + 1];
		numAsteroids = level;
		for (int i = 0; i < numAsteroids; i++)
			asteroids[i] = new Asteroid(Math.random() * dim.width,
					Math.random() * dim.height, astRadius, minAstVel,
					maxAstVel, astNumHits, astNumSplit);
	}

	public void paint(Graphics gfx) {
		g.setColor(Color.black); // clear the screen with black
		g.fillRect(0, 0, 500, 500);
		if (lives > 0) {
			for (int i = 0; i < numShots; i++)
				shots[i].draw(g);
			for (int i = 0; i < numAsteroids; i++)
				asteroids[i].draw(g);
			ship.draw(g);
			g.setColor(Color.cyan);
			g.drawString("Level " + level, 20, 20);
			g.drawString("Points: " + points, 430, 20);
			g.drawString("lives: " + lives, 20, 430);
		} else {
			g.setColor(Color.red);
			g.drawString("Game Over", 225, 250);
			g.drawString("Press enter to try again", 175, 275);
		}	
		gfx.drawImage(img, 0, 0, this);
	}

	public void run() {
		for (;;) {
			startTime = System.currentTimeMillis();
			if (numAsteroids <= 0)
				setUpNextLevel();
			if (!paused) {
				ship.move(dim.width, dim.height); // move the ship
				// this loop moves each shot and deletes dead shots:
				for (int i = 0; i < numShots; i++) {
					shots[i].move(dim.width, dim.height);
					// removes shot if it has gone for too long without hitting
					// anything
					if (shots[i].getLifeLeft() <= 0) {
						// shifts all the next shots up one space in the array
						deleteShot(i);
						i--; // move the outer loop back one so the shot shifted
								// up is not skipped
					}
				}
				updateAsteroids();
				if (shooting && ship.canShoot()) {
					// add a shot on to the array if the ship is shooting
					shots[numShots] = ship.shoot();
					numShots++;
				}
			}
			repaint();
			try {
				endTime = System.currentTimeMillis();
				if (framePeriod - (endTime - startTime) > 0)
					Thread.sleep(framePeriod - (endTime - startTime));
			} catch (InterruptedException e) {
			}
		}
	}

	private void deleteShot(int index) {
		numShots--;
		for (int i = index; i < numShots; i++)
			shots[i] = shots[i + 1];
		shots[numShots] = null;
	}

	private void deleteAsteroid(int index) {
		numAsteroids--;
		for (int i = index; i < numAsteroids; i++)
			asteroids[i] = asteroids[i + 1];
		asteroids[numAsteroids] = null;
		points++;
	}

	private void addAsteroid(Asteroid ast) {
		asteroids[numAsteroids] = ast;
		numAsteroids++;
	}

	private void updateAsteroids() {
		for (int i = 0; i < numAsteroids; i++) {
			asteroids[i].move(dim.width, dim.height);
			if (asteroids[i].shipCollision(ship)) {
				level--;
				lives--;
				points--;
				numAsteroids = 0;
				return;
			}
			for (int j = 0; j < numShots; j++) {
				if (asteroids[i].shotCollision(shots[j])) {
					deleteShot(j);
					if (asteroids[i].getHitsLeft() > 1) {
						for (int k = 0; k < asteroids[i].getNumSplit(); k++)
							addAsteroid(asteroids[i].createSplitAsteroid(
									minAstVel, maxAstVel));
					}
					deleteAsteroid(i);
					j = numShots;
					i--;
				}
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if(lives == 0 && e.getKeyCode() == KeyEvent.VK_ENTER){
			level = 0;
			points = 0;
			lives = 5;
			ship.setActive(true);
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			// These first two lines allow the asteroids to move
			// while the player chooses when to enter the game.
			// This happens when the player is starting a new life.
			if (!ship.isActive() && !paused)
				ship.setActive(true);
			else {
				paused = !paused; // enter is the pause button
				if (paused) // grays out the ship if paused
					ship.setActive(false);
				else
					ship.setActive(true);
			}
		} else if (paused || !ship.isActive()) 
			// if the game is paused or ship is inactive, do not
			 // respond to the controls except for enter to unpause
			return;
		else if (e.getKeyCode() == KeyEvent.VK_UP)
			ship.setAccelerating(true);
		else if (e.getKeyCode() == KeyEvent.VK_LEFT)
			ship.setTurningLeft(true);
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			ship.setTurningRight(true);
		else if (e.getKeyCode() == KeyEvent.VK_SPACE)
			shooting = true;
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP)
			ship.setAccelerating(false);
		else if (e.getKeyCode() == KeyEvent.VK_LEFT)
			ship.setTurningLeft(false);
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			ship.setTurningRight(false);
		else if (e.getKeyCode() == KeyEvent.VK_SPACE)
			shooting = false;
	}

	public void keyTyped(KeyEvent e) {
		// empty method but still needed to implement the KeyListerner interface
	}

	public void update(Graphics gfx) {
		paint(gfx); // call paint without clearing the screen
	}
}
