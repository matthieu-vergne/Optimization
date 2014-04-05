package fr.vergne.optimization.TSP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.vergne.optimization.generator.Explorator;
import fr.vergne.optimization.generator.Generator;
import fr.vergne.optimization.generator.Mutator;

public class TSP {
	private static long startTime;
	private final static Random rand = new Random();

	public static void main(String[] args) throws IOException {
		final Logger log = Logger.getAnonymousLogger();
		log.setLevel(Level.ALL);
		System.out.println("Parse file...");
		final Collection<Location> locations = CsvReader
				.parse("res/250cities.csv");
		PathIncubator incubator = new PathIncubator();

		System.out.println("Init mutations...");
		Generator<Path> random = new Generator<Path>() {

			@Override
			public Path generates() {
				List<Location> genes = new LinkedList<Location>(locations);
				Collections.shuffle(genes);
				return new Path(genes);
			}
		};
		Mutator<Path> littleMutation = new Mutator<Path>() {

			private Path reference;

			@Override
			public void setReference(Path reference) {
				this.reference = reference;
			}

			@Override
			public Path generates() {
				List<Location> genes = reference.getLocations();
				int index = rand.nextInt(genes.size());
				int newIndex = rand.nextInt(genes.size() - 1);
				List<Location> newGenes = new LinkedList<Location>(genes);
				newGenes.add(newIndex, newGenes.remove(index));
				return new Path(newGenes);
			}

			@Override
			public boolean isApplicableOn(Path path) {
				return true;
			}
		};
		Mutator<Path> bigMutation = new Mutator<Path>() {

			private Path reference;

			@Override
			public void setReference(Path reference) {
				this.reference = reference;
			}

			@Override
			public Path generates() {
				List<Location> genes = reference.getLocations();
				List<Location> newGenes = new ArrayList<Location>(genes);
				int start = rand.nextInt(genes.size());
				int end = rand.nextInt(genes.size());
				if (start > end) {
					int temp = start;
					start = end;
					end = temp;
				}
				Collections.reverse(newGenes.subList(start, end));
				return new Path(newGenes);
			}

			@Override
			public boolean isApplicableOn(Path input) {
				return true;
			}
		};
		Explorator<Path> combinator = new Explorator<Path>() {

			private Collection<Path> population;

			@Override
			public boolean isApplicableOn(Collection<Path> population) {
				return population.size() > 1;
			}

			@Override
			public void setReference(Collection<Path> population) {
				// TODO retrieve potential optima (high scores)
				this.population = population;
			}

			private final Random rand = new Random();

			@Override
			public Path generates() {
				log.info("Explorating...");
				List<Location> remaining = new LinkedList<Location>(locations);
				LinkedList<Location> genes = new LinkedList<Location>();
				int geneSize = locations.size();

				genes.add(remaining.remove(rand.nextInt(remaining.size())));
				log.info("Gene " + genes.size() + "/" + geneSize + ": "
						+ genes.getLast());
				while (!remaining.isEmpty()) {
					log.fine("Voting...");
					Location from = genes.getLast();
					Map<Location, Integer> votes = new HashMap<Location, Integer>();
					for (Location location : remaining) {
						votes.put(location, 0);
					}
					for (Path voter : population) {
						List<Location> genes2 = voter.getLocations();
						int index = genes2.indexOf(from);
						for (int delta : Arrays.asList(1, geneSize - 1)) {
							int indexTo = (index + delta) % geneSize;
							Location to = genes2.get(indexTo);
							Integer score = votes.get(to);
							if (score == null) {
								// already used location
							} else {
								votes.put(to, score + 1);
							}
						}
					}

					log.fine("Selecting...");
					Location best = null;
					Integer bestScore = null;
					for (Entry<Location, Integer> entry : votes.entrySet()) {
						Location location = entry.getKey();
						Integer score = entry.getValue();
						if (bestScore == null || bestScore < score) {
							best = location;
							bestScore = score;
						} else {
							// not better
						}
					}

					remaining.remove(best);
					genes.add(best);
					log.info("Gene " + genes.size() + "/" + geneSize + ": "
							+ best);
				}

				Path individual = new Path(genes);
				log.info("Explorator generated: " + individual);
				System.out.println("Explorator generated: " + individual);
				return individual;
			}
		};

		System.out.println("Init incubator...");
		incubator.addGenerator(random);
		incubator.addGenerator(littleMutation);
		incubator.addGenerator(bigMutation);
		incubator.addGenerator(combinator);

		JCanvas canvas = new JCanvas();
		System.out.println("Start algo...");
		startTime = System.currentTimeMillis();
		Logger.getAnonymousLogger().getParent().getHandlers()[0]
				.setLevel(Level.OFF);
		while (true) {
			incubator.incubate();
			displayResult(incubator, canvas);
		}
	}

	static long lastDisplay = 0;

	private static void displayResult(PathIncubator incubator, JCanvas canvas) {
		Path best = incubator.getPopulationManager().getBest().next();
		if (best.getLength() < canvas.getPath().getLength()
				|| System.currentTimeMillis() > lastDisplay + 1000) {
			lastDisplay = System.currentTimeMillis();
			canvas.setPath(best);
			double time = (double) (System.currentTimeMillis() - startTime) / 1000;
			String terminal = String.format("%8.3fs| %8d| %5d - ", time,
					incubator.getGeneratedIndividuals(), incubator
							.getPopulationManager().getPopulation().size());
			terminal += ((double) Math.round(best.getLength() * 100) / 100);
			System.out.println(terminal);
		}
	}

}
