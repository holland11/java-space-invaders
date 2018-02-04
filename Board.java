import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;


public class Board extends JPanel implements Runnable {
	
	private List<Bullet> bullets;
	private Alien[][] aliens;
	private Player player;
	private int windowX = 500;
	private int windowY = 500;
	private int playerW = 25;
	private int playerH = 25;
	private int playerStartX = (windowX / 2) - (playerW / 2);
	private int playerStartY = (windowY) - playerH - 5;
	private int playerX;
	private int playerY;
	private boolean right, left, fire, gameOn;
	private Thread thread;
	private int moveDist = 5;
	private int bulletSpeed = 7;
	private long bulletSpacing = 180;
	private long lastBullet;
	private long tempTime;
	private int alienCols = 11;
	private int alienRows = 5;
	private int alienSpacingX = 8;
	private int alienSpacingY = 6;
	private int alienW = (((8*windowX)/10) / alienCols) - alienSpacingX;
	private int alienH = (((4*windowY)/10) / alienRows) - alienSpacingY;
	private int lives;
	private long lastAlienMove;
	private long alienMoveFreq = 400;
	private boolean aliensGoingRight = true;
	private int aliensRemaining = alienCols * alienRows;
	
	public Board() {
		initBoard();
	}
	
	public void initBoard() {
		setPreferredSize(new Dimension(windowX, windowY));
		newGame();
		
		thread = new Thread(this);
		thread.start();
	}
	
	public void newGame() {
		player = new Player();
		lives = 3;
		playerX = playerStartX;
		playerY = playerStartY;
		bullets = new ArrayList<Bullet>();
		aliens = new Alien[5][11];
		initAliens();
		lastBullet = System.currentTimeMillis();
		gameOn = true;
	}
	
	public void initAliens() {
		for (int i = 0; i < alienRows; i++) {
			for (int j = 0; j < alienCols; j++) {
				aliens[i][j] = new Alien((j*(alienW+alienSpacingX))+(windowX/10),(i*(alienH+alienSpacingY)));
			}
		}
		lastAlienMove = System.currentTimeMillis();
	}
	
	public void updateAliens() {
		long tempTime = System.currentTimeMillis();
		if (tempTime - lastAlienMove < alienMoveFreq) {
			return;
		}
		
		boolean drop = false;
		if (aliensGoingRight && (aliens[0][alienCols-1].x >= (windowX - alienW))) {
			aliensGoingRight = false;
			drop = true;
		}
		else if (!aliensGoingRight && (aliens[0][0].x <= 0)) {
			aliensGoingRight = true;
			drop = true;
		}
		// If aliens traveling right, positive moveDist, else negative moveDist
		int dropDist = 0;
		int moveDist = 7;
		if (drop) {
			dropDist = (alienH + alienSpacingY) / 2;
			moveDist = 0;
			alienMoveFreq -= alienMoveFreq / 4;
		}
		else {
			moveDist = aliensGoingRight ? moveDist : -moveDist;
		}
		
		for (int i = 0; i < alienRows; i++) {
			for (int j = 0; j < alienCols; j++) {
				aliens[i][j].x += moveDist;
				aliens[i][j].y += dropDist;
			}
		}
		lastAlienMove = System.currentTimeMillis();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawBG(g);
		if (gameOn) {
			drawBullets(g);
			drawPlayer(g);
		}
		drawAliens(g);
	}
	
	public void drawAliens(Graphics g) {
		for (int i = 0; i < alienRows; i++) {
			for (int j = 0; j < alienCols; j++) {
				if (aliens[i][j].active) {
					aliens[i][j].draw(g, alienW, alienH);
				}
			}
		}
	}
	
	public void drawBullets(Graphics g) {
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).draw(g);
		}
	}
	
	public void drawBG(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, windowX, windowY);
	}
	
	public void drawPlayer(Graphics g) {
		player.draw(g, playerX, playerY, playerW, playerH);
	}
	
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case (KeyEvent.VK_D):
			case (KeyEvent.VK_RIGHT): 
				right = true;
				break;
			case (KeyEvent.VK_A):
			case (KeyEvent.VK_LEFT):
				left = true;
				break;
			case (KeyEvent.VK_SPACE):
				fire = true;
				break;
		}
	}
	
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case (KeyEvent.VK_D):
			case (KeyEvent.VK_RIGHT):
				right = false;
				break;
			case (KeyEvent.VK_A):
			case (KeyEvent.VK_LEFT):
				left = false;
				break;
			case (KeyEvent.VK_SPACE):
				fire = false;
				break;
		}
	}

	public void run() {
		while (gameOn) {
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			}
			updatePlayer();
			updateBullets();
			updateAliens();
			alienShootChance();
			checkCollisions();
			repaint();
		}
	}
	
	public void alienShootChance() {
		if (aliensRemaining == 0) {
			return;
		}
		double chance = alienCols * alienRows * 2 / aliensRemaining; // % chance each gameloop that an alien will fire
		double rand = 0;
		for (int i = 0; i < alienCols; i++) {
			for (int j = alienRows-1; j >= 0; j--) {
				if (aliens[j][i].active) {
					rand = Math.random() * 100;
					if (rand <= chance) {
						alienFire(aliens[j][i]);
					}
					break;
				}
			}
		}
	}
	
	public void alienFire(Alien alien) {
		bullets.add(new Bullet((alien.x + (alienW/2)), alien.y + alienH, false));
	}
	
	public void checkCollisions() {
		/*
		 * Check each bullet. Check whether it is traveling up or down. 
		 * Create Rectangle objects with the bullet and target's attributes.
		 * Test intersection of alien with bulletrect when bullet is traveling up, 
		 * else check player rect with bulletrect.
		 * If alien is hit, check to see if it was the front of its row. 
		 * If it is, find and assert the new front of the row if one exists.
		 */
		if (!gameOn) {
			return;
		}
		
		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			Rectangle bulletRect = new Rectangle(b.x, b.y, b.width, b.height);
			if (b.travellingUp) {
				for (int j = 0; j < alienRows; j++) {
					for (int k = 0; k < alienCols; k++) {
						if (!aliens[j][k].active)
							continue;
						Rectangle alienRect = new Rectangle(aliens[j][k].x, aliens[j][k].y, alienW, alienH);
						if (bulletRect.intersects(alienRect)) {
							bullets.remove(i);
							aliens[j][k].active = false;
							aliensRemaining--;
							i--;
						}
					}
				}
			}
			else {
				Rectangle playerRect = new Rectangle(playerX, playerY, playerW, playerH);
				if (bulletRect.intersects(playerRect)) {
					lives--;
					newSpawn();
				}
			}
		}
		if (lives < 1) {
			gameOn = false;
		}
	}
	
	public void newSpawn() {
		bullets.clear();
		playerX = playerStartX;
		playerY = playerStartY;
	}
	
	public void updateBullets() {
		if (!gameOn) {
			return;
		}
		
		for (int i = 0; i < bullets.size(); i++) {
			if (bullets.get(i).travellingUp)
				bullets.get(i).y -= bulletSpeed;
			else {
				bullets.get(i).y += bulletSpeed;
			}
			if (bullets.get(i).y < 0 || bullets.get(i).y > windowY) {
				bullets.remove(i);
				i--;
			}
		}
	}
	
	public void updatePlayer() {
		if (!gameOn)
			return;
		
		if (right && left) {}
		else if (right) {
			playerX += moveDist;
		}
		else if (left) {
			playerX -= moveDist;
		}
		if (playerX > windowX - playerW) {
			playerX = windowX - playerW;
		}
		else if (playerX < 0) {
			playerX = 0;
		}	
		tempTime = System.currentTimeMillis();
		if (fire && (tempTime - lastBullet) > bulletSpacing) {
			bullets.add(new Bullet(playerX + (playerW / 2), playerY, true));
			lastBullet = tempTime;
		}
	}
}
