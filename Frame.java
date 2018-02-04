import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;


public class Frame extends JFrame implements KeyListener {
	Board board;
	
	
	public Frame() {
		initFrame();
	}
	
	public void initFrame() {
		board = new Board();
		add(board);
		setTitle("Space Invaders - Patrick Holland");
		setResizable(false);
		setFocusable(true);
		addKeyListener(this);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Frame frame = new Frame();
				frame.setVisible(true);
			}
		});
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		board.keyPressed(e);
	}

	public void keyReleased(KeyEvent e) {
		board.keyReleased(e);
	}
}
