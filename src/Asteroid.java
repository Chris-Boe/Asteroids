import java.awt.*;

public class Asteroid {
 double x, y, xVelocity, yVelocity, radius;
 int hitsLeft, numSplit;
 
 	public Asteroid(double x, double y, double radius, double minVelocity, 
 			double maxVelocity, int hitsLeft, int numSplit) {
 		this.x = x;
 		this.y = y;
 		this.radius = radius;
 		this.hitsLeft = hitsLeft;
 		this.numSplit = numSplit;
 		//calculates a random dir and a random vel between min & max
 		double vel = minVelocity + Math.random() * (maxVelocity - minVelocity),
 				dir = 2 * Math.PI * Math.random(); //random direction
 		xVelocity = vel*Math.cos(dir);
 		yVelocity = vel*Math.sin(dir);
 	}
 	public void move(int scrnWidth, int scrnHeight) {
 		x += xVelocity;
 		y += yVelocity;
 		//wrap around code:
 		if(x < 0 - radius)
 			x += scrnWidth + 2 * radius;
 		else if(x > scrnWidth + radius)
 			x -= scrnWidth + 2 * radius;
 		if(y < 0 - radius)
 			y += scrnHeight + 2 * radius;
 		else if(y > scrnHeight + radius)
 			y -= scrnHeight + 2 * radius;
 	}
 	public void draw(Graphics g){
 		g.setColor(Color.GRAY);
 		g.fillOval((int)(x - radius + .5), (int)(y - radius + .5),
 				(int)(2 * radius), (int)(2 * radius));
 	}
 	public int getHitsLeft() {
 		return hitsLeft;
 	}
 	public int getNumSplit() {
 		return numSplit;
 	}
 	public boolean shipCollision(Ship ship) {
 		//Use distance formula to check for collisions
 		//Distance^2 = (x1-x2)^2 + (y1-y2)^2
 		// if (shipRadius + asteroidRadius)^2 > (x1-x2)^2 + (y1-y2)^2, 
 		// then they have collided.
 		if(Math.pow(radius+ship.getRadius(), 2) > Math.pow(ship.getX()-x, 2)
 				+ Math.pow(ship.getY()-y, 2) && ship.isActive()){
 			return true;}
 		return false;
 	}
 	public boolean shotCollision(Shot shot){
 		//shot radius = 0
 		if(Math.pow(radius, 2) > Math.pow(shot.getX()-x, 2) +
 				Math.pow(shot.getY()-y, 2))
 			return true;
 		return false;
 	}
 	public Asteroid createSplitAsteroid(double minVelocity, double maxVelocity){
 		return new Asteroid(x, y, radius/Math.sqrt(4), 
 				minVelocity, maxVelocity, hitsLeft - 1, numSplit);
 	}
}




