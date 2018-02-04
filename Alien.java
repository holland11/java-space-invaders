import java.awt.Color;
import java.awt.Graphics;


public class Alien {

	public int x;
	public int y;
	public boolean active;
	
	public Alien(int x, int y) {
		this.x = x;
		this.y = y;
		active = true;
	}
	
	public void draw(Graphics g, int w, int h) {
		g.setColor(Color.red);
		g.fillRect(x, y, w, h);
	}
}
