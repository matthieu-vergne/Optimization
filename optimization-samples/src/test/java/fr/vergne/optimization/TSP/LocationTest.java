package fr.vergne.optimization.TSP;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.optimization.TSP.Location;

public class LocationTest {

	@Test
	public void testCoords() {
		Location location = new Location(5, 1);
		assertEquals(5, location.getX(), 0);
		assertEquals(1, location.getY(), 0);
	}

	@Test
	public void testToString() {
		Location location = new Location(5, 1);
		assertEquals("(5.0,1.0)", location.toString());
	}

}
