package fr.vergne.optimization.TSP;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.vergne.optimization.TSP.path.AbstractPath;
import fr.vergne.optimization.TSP.path.Location;
import fr.vergne.optimization.generator.Mutator;
import fr.vergne.optimization.population.impl.OptimizerPool.Optimizer;

public abstract class AbstractTSP<Path extends AbstractPath> {
	private long startTime;
	private long lastDisplay = 0;
	private final Collection<Location> locations;
	private final Logger log = Logger.getAnonymousLogger();
	private final JCanvas canvas = new JCanvas();

	public AbstractTSP() {
		log.setLevel(Level.ALL);
		System.out.println("Parse file...");
		try {
			locations = CsvReader.parse("res/250cities.csv");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public Collection<Location> getLocations() {
		return locations;
	}

	public void run() throws IOException {
		System.out.println("Init incubator...");
		PathIncubator<Path> incubator = getIncubator(locations);

		System.out.println("Start algo...");
		startTime = System.currentTimeMillis();
		Logger.getAnonymousLogger().getParent().getHandlers()[0]
				.setLevel(Level.OFF);
		do {
			incubator.incubate();
			displayResult(incubator);
		} while (incubator.hasEvolved());
	}

	protected abstract PathIncubator<Path> getIncubator(
			Collection<Location> locations);

	private void displayResult(PathIncubator<Path> incubator) {
		Path best = incubator.getOptimizerPool().getBest().next();
		if (best.getLength() < canvas.getPath().getLength()
				|| System.currentTimeMillis() > lastDisplay + 1000) {
			lastDisplay = System.currentTimeMillis();
			canvas.setPath(best);
			double time = (double) (System.currentTimeMillis() - startTime) / 1000;
			String terminal = String.format("%8.3fs| %8d| %5d -", time,
					incubator.getGeneratedIndividuals(), incubator
							.getPopulation().size());
			for (Optimizer<Path> optimizer : incubator.getOptimizerPool()) {
				Path path = optimizer.getRepresentative();
				double length = (double) Math.round(path.getLength() * 100) / 100;
				String desc = "" + length + " |";
				for (Mutator<Path> mutator : incubator.getMutators()) {
					double optimality = (double) Math.round(optimizer
							.getOptimalityWith(mutator) * 100) / 100;
					String initial = mutator.toString().substring(0, 1);
					desc += initial + "=" + optimality + "|";
				}
				terminal += " " + (path == best ? "[" + desc + "]" : desc);
			}
			System.out.println(terminal);
		}
	}

}
