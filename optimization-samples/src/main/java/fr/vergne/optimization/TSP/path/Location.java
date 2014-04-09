package fr.vergne.optimization.TSP.path;

public class Location {
	private final double x;
	private final double y;

	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Location) {
			Location l = (Location) obj;
			return l.x == x && l.y == y;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int) Math.round(x + y);
	}

	@Override
	public String toString() {
		double x = (double) Math.round(this.x * 100) / 100;
		double y = (double) Math.round(this.y * 100) / 100;
		return "(" + x + "," + y + ")";
	}
}
