package fr.vergne.optimization.TSP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class CsvReader {

	public static Collection<Location> parse(String filepath)
			throws IOException {
		BufferedReader reader = null;
		try {
			Collection<Location> locations = new LinkedList<Location>();
			reader = new BufferedReader(new FileReader(filepath));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.matches("\\d+")) {
				} else if (line.matches("0\\.\\d+;0\\.\\d+")) {
					String[] split = line.split(";");
					double x = Double.parseDouble(split[0]);
					double y = Double.parseDouble(split[1]);
					locations.add(new Location(x, y));
				} else {
					System.out.println("Line: " + line);
				}
			}

			return locations;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			reader.close();
		}
	}
}
