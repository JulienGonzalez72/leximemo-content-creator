package main;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JPanel;

public class CompletionBar extends JPanel {
	
	private int max;
	private Map<Integer, Boolean> completion = new HashMap<>();
	
	public CompletionBar(int max) {
		this.max = max;
		for (int i = 0; i < max; i++) {
			completion.put(i, false);
		}
	}
	
	@Override
	protected void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		Graphics2D g = (Graphics2D) gr;
		
		for (int i = 0; i < max; i++) {
			g.setColor(completion.get(i) ? Color.GREEN : Color.RED);
			Rectangle2D rect = new Rectangle2D.Float(x() + width() / (float) max * i, y(),
					width() / (float) max, height());
			g.fill(rect);
			g.setColor(new Color(0, 0, 0, 50));
			g.draw(new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight() - 1));
		}
	}
	
	public void complete(int index) {
		completion.put(index, true);
		repaint();
	}
	
	public void setClickListener(Consumer<Integer> event) {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				float x = e.getX() - x();
				event.accept((int) (x / (float) width() * max));
			}
		});
	}
	
	public float hMargin() {
		return getWidth() / 30f;
	}
	
	public float vMargin() {
		return getHeight() / 20f;
	}
	
	public float x() {
		return hMargin();
	}
	
	public float y() {
		return vMargin();
	}
	
	public float width() {
		return getWidth() - hMargin() * 2;
	}
	
	public float height() {
		return getHeight() - vMargin() * 2;
	}
	
}
