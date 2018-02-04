import java.awt.Color;
import java.awt.Graphics;


public class Player {
	

	public Player() {
	}
	
	public void draw(Graphics g, int x, int y, int w, int h) {
		g.setColor(Color.green);
		g.fillRect(x, y, w, h);
	}
}
