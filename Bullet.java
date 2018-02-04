import java.awt.Color;
import java.awt.Graphics;


public class Bullet {

	public int x;
	public int y;
	public int height = 12;
	public int width = 2;
	public boolean travellingUp;
	
	public Bullet(int x, int y, boolean up) {
		this.x = x;
		this.y = y;
		travellingUp = up;
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(x, y, width, height);
	}
}
