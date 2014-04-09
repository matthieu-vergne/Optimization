package fr.vergne.optimization.TSP;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import fr.vergne.optimization.TSP.path.AbstractPath;
import fr.vergne.optimization.TSP.path.AbstractPath.Transition;
import fr.vergne.optimization.TSP.path.Location;

public class JCanvas extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JFrame frame;
	private AbstractPath path = new AbstractPath() {
		public Double getLength() {
			return Double.MAX_VALUE;
		}

		@Override
		public Collection<Transition> getTransitions() {
			return Collections.<Transition> emptyList();
		}
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

	public void setPath(AbstractPath path) {
		boolean wasEmpty = getPath().getTransitions().isEmpty();
		this.path = path;
		if (wasEmpty) {
			resize();
		}
		repaint();
	}

	private void resize() {
		double xMax = 1;
		double yMax = 1;
		for (Transition transition : path.getTransitions()) {
			for (Location location : Arrays.asList(transition.getL1(),
					transition.getL2())) {
				xMax = Math.max(location.getX(), xMax);
				yMax = Math.max(location.getY(), yMax);
			}
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

	public AbstractPath getPath() {
		return path;
	}

	public void paint(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		Color originalColor = g.getColor();

		if (path.getTransitions().isEmpty()) {
			return;
		} else {
			g.setColor(Color.RED);
			Set<Location> locations = new HashSet<Location>();
			for (Transition transition : path.getTransitions()) {
				Location from = transition.getL1();
				Location to = transition.getL2();
				locations.add(from);
				locations.add(to);

				int xStart = (int) (from.getX() * xRate);
				int yStart = (int) (from.getY() * yRate);
				int xEnd = (int) (to.getX() * xRate);
				int yEnd = (int) (to.getY() * yRate);
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
			}

			g.setColor(Color.BLUE);
			for (Location location : locations) {
				int x = (int) (location.getX() * xRate) - radius;
				int y = (int) (location.getY() * yRate) - radius;
				int diameter = 2 * radius;
				g.fillOval(x, y, diameter, diameter);
			}

			g.setColor(originalColor);
		}
	}

}
