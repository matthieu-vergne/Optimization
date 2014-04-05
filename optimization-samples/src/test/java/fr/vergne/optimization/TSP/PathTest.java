package fr.vergne.optimization.TSP;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fr.vergne.optimization.TSP.Location;
import fr.vergne.optimization.TSP.Path;

public class PathTest {

	@Test
	public void testEquals() {
		Location l1 = new Location(1, 0);
		Location l2 = new Location(2, 0);
		Location l3 = new Location(3, 0);
		Location l4 = new Location(4, 0);

		Path p1234 = new Path(Arrays.asList(l1, l2, l3, l4));
		Path p1243 = new Path(Arrays.asList(l1, l2, l4, l3));
		Path p1324 = new Path(Arrays.asList(l1, l3, l2, l4));
		Path p1342 = new Path(Arrays.asList(l1, l3, l4, l2));
		Path p1423 = new Path(Arrays.asList(l1, l4, l2, l3));
		Path p1432 = new Path(Arrays.asList(l1, l4, l3, l2));
		Path p2134 = new Path(Arrays.asList(l2, l1, l3, l4));
		Path p2143 = new Path(Arrays.asList(l2, l1, l4, l3));
		Path p2314 = new Path(Arrays.asList(l2, l3, l1, l4));
		Path p2341 = new Path(Arrays.asList(l2, l3, l4, l1));
		Path p2413 = new Path(Arrays.asList(l2, l4, l1, l3));
		Path p2431 = new Path(Arrays.asList(l2, l4, l3, l1));
		Path p3214 = new Path(Arrays.asList(l3, l2, l1, l4));
		Path p3241 = new Path(Arrays.asList(l3, l2, l4, l1));
		Path p3124 = new Path(Arrays.asList(l3, l1, l2, l4));
		Path p3142 = new Path(Arrays.asList(l3, l1, l4, l2));
		Path p3421 = new Path(Arrays.asList(l3, l4, l2, l1));
		Path p3412 = new Path(Arrays.asList(l3, l4, l1, l2));
		Path p4231 = new Path(Arrays.asList(l4, l2, l3, l1));
		Path p4213 = new Path(Arrays.asList(l4, l2, l1, l3));
		Path p4321 = new Path(Arrays.asList(l4, l3, l2, l1));
		Path p4312 = new Path(Arrays.asList(l4, l3, l1, l2));
		Path p4123 = new Path(Arrays.asList(l4, l1, l2, l3));
		Path p4132 = new Path(Arrays.asList(l4, l1, l3, l2));

		Map<Path, Collection<Path>> map = new HashMap<Path, Collection<Path>>();
		map.put(p1234, Arrays.asList(p1234, p4123, p3412, p2341, p4321, p1432,
				p2143, p3214));
		map.put(p1243, Arrays.asList(p1243, p3124, p4312, p2431, p3421, p1342,
				p2134, p4213));
		map.put(p1324, Arrays.asList(p1324, p4132, p2413, p3241, p4231, p1423,
				p3142, p2314));
		map.put(p1342, Arrays.asList(p1342, p2134, p4213, p3421, p2431, p1243,
				p3124, p4312));
		map.put(p1423, Arrays.asList(p1423, p3142, p2314, p4231, p3241, p1324,
				p4132, p2413));
		map.put(p1432, Arrays.asList(p1432, p2143, p3214, p4321, p2341, p1234,
				p4123, p3412));
		map.put(p2134, Arrays.asList(p2134, p4213, p3421, p1342, p4312, p2431,
				p1243, p3124));
		map.put(p2143, Arrays.asList(p2143, p3214, p4321, p1432, p3412, p2341,
				p1234, p4123));
		map.put(p2314, Arrays.asList(p2314, p4231, p1423, p3142, p4132, p2413,
				p3241, p1324));
		map.put(p2341, Arrays.asList(p2341, p1234, p4123, p3412, p1432, p2143,
				p3214, p4321));
		map.put(p2413, Arrays.asList(p2413, p3241, p1324, p4132, p3142, p2314,
				p4231, p1423));
		map.put(p2431, Arrays.asList(p2431, p1243, p3124, p4312, p1342, p2134,
				p4213, p3421));
		map.put(p3214, Arrays.asList(p3214, p4321, p1432, p2143, p4123, p3412,
				p2341, p1234));
		map.put(p3241, Arrays.asList(p3241, p1324, p4132, p2413, p1423, p3142,
				p2314, p4231));
		map.put(p3124, Arrays.asList(p3124, p4312, p2431, p1243, p4213, p3421,
				p1342, p2134));
		map.put(p3142, Arrays.asList(p3142, p2314, p4231, p1423, p2413, p3241,
				p1324, p4132));
		map.put(p3421, Arrays.asList(p3421, p1342, p2134, p4213, p1243, p3124,
				p4312, p2431));
		map.put(p3412, Arrays.asList(p3412, p2341, p1234, p4123, p2143, p3214,
				p4321, p1432));
		map.put(p4231, Arrays.asList(p4231, p1423, p3142, p2314, p1324, p4132,
				p2413, p3241));
		map.put(p4213, Arrays.asList(p4213, p3421, p1342, p2134, p3124, p4312,
				p2431, p1243));
		map.put(p4321, Arrays.asList(p4321, p1432, p2143, p3214, p1234, p4123,
				p3412, p2341));
		map.put(p4312, Arrays.asList(p4312, p2431, p1243, p3124, p2134, p4213,
				p3421, p1342));
		map.put(p4123, Arrays.asList(p4123, p3412, p2341, p1234, p3214, p4321,
				p1432, p2143));
		map.put(p4132, Arrays.asList(p4132, p2413, p3241, p1324, p2314, p4231,
				p1423, p3142));

		for (Path pA : map.keySet()) {
			for (Path pB : map.keySet()) {
				if (map.get(pA).contains(pB)) {
					assertTrue(pA + " = " + pB, pA.equals(pB));
				} else {
					assertFalse(pA + " != " + pB, pA.equals(pB));
				}

			}
		}
	}

}
