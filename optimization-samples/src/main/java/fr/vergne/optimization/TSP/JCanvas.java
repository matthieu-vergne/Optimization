package fr.vergne.optimization.TSP;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class JCanvas extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JFrame frame;
	private Path path = new Path(Collections.<Location> emptyList()) {
		public Double getLength() {
			return Double.MAX_VALUE;
		};
	};
	private int radius;
	private double xRate;
	private double yRate;

	public JCanvas() {
		frame = new JFrame("Travelling Salesman");
		WindowAdapter wa = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		};
		frame.addWindowListener(wa);
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(1000, 600));
		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
	}

	public void setPath(Path path) {
		boolean wasEmpty = getPath().getLocations().isEmpty();
		this.path = path;
		if (wasEmpty) {
			resize();
		}
		repaint();
	}

	private void resize() {
		double xMax = 1;
		double yMax = 1;
		for (Location location : path.getLocations()) {
			xMax = Math.max(location.getX(), xMax);
			yMax = Math.max(location.getY(), yMax);
		}

		radius = 5;
		xRate = (getWidth() - radius) / xMax;
		yRate = (getHeight() - radius) / yMax;
		xRate = Math.min(xRate, yRate);
		yRate = xRate;
		int dx = frame.getWidth() - getWidth();
		int dy = frame.getHeight() - getHeight();
		frame.setSize((int) (xRate * xMax) + radius + dx, (int) (yRate * yMax)
				+ radius + dy);
	}

	public Path getPath() {
		return path;
	}

	public void paint(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		Color originalColor = g.getColor();

		if (path.getLocations().isEmpty()) {
			return;
		} else {
			g.setColor(Color.RED);
			List<Location> remains = new LinkedList<Location>(path.getLocations());
			remains.add(remains.get(0));
			Location start = remains.remove(0);
			while (!remains.isEmpty()) {
				Location end = remains.remove(0);
				int xStart = (int) (start.getX() * xRate);
				int yStart = (int) (start.getY() * yRate);
				int xEnd = (int) (end.getX() * xRate);
				int yEnd = (int) (end.getY() * yRate);
				int[] xPoints = new int[] { xStart, xEnd, xEnd, xStart };
				int[] yPoints = new int[] { yStart, yEnd, yEnd, yStart };
				if (Math.abs(xStart - xEnd) <= Math.abs(yStart - yEnd)) {
					xPoints[0]--;
					xPoints[1]--;
					xPoints[2]++;
					xPoints[3]++;
				} else {
					yPoints[0]++;
					yPoints[1]++;
					yPoints[2]--;
					yPoints[3]--;
				}
				g.fillPolygon(xPoints, yPoints, xPoints.length);
				start = end;
			}

			g.setColor(Color.BLUE);
			for (Location location : path.getLocations()) {
				int x = (int) (location.getX() * xRate) - radius;
				int y = (int) (location.getY() * yRate) - radius;
				int diameter = 2 * radius;
				g.fillOval(x, y, diameter, diameter);
			}

			g.setColor(originalColor);
		}
	}

}
