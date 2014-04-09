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
import java.util.logging.Logger;

import fr.vergne.optimization.TSP.path.Location;
import fr.vergne.optimization.TSP.path.Path1;
import fr.vergne.optimization.generator.Explorator;
import fr.vergne.optimization.generator.Mutator;

public class TSP1 extends AbstractTSP<Path1> {
	private final static Random rand = new Random();
	private static final Logger log = Logger.getAnonymousLogger();

	public static void main(String[] args) throws IOException {
		new TSP1().run();
	}

	@Override
	protected PathIncubator<Path1> getIncubator(Collection<Location> locations) {
		PathIncubator<Path1> incubator = new PathIncubator<Path1>();
		for (Explorator<Path1> explorator : getExplorators(locations)) {
			incubator.addExplorator(explorator);
		}
		for (Mutator<Path1> mutator : getMutators(locations)) {
			incubator.addMutator(mutator);
		}
		return incubator;
	}

	@SuppressWarnings("unchecked")
	protected Collection<Mutator<Path1>> getMutators(
			Collection<Location> locations) {
		Mutator<Path1> littleMutation = new Mutator<Path1>() {
			@Override
			public String toString() {
				return "little";
			}

			@Override
			public boolean isApplicableOn(Path1 original) {
				return true;
			}

			@Override
			public Path1 generates(Path1 original) {
				List<Location> genes = original.getLocations();
				int index = rand.nextInt(genes.size());
				int newIndex = rand.nextInt(genes.size() - 1);
				List<Location> newGenes = new LinkedList<Location>(genes);
				newGenes.add(newIndex, newGenes.remove(index));
				return new Path1(newGenes);
			}
		};
		Mutator<Path1> bigMutation = new Mutator<Path1>() {
			@Override
			public String toString() {
				return "big";
			}

			@Override
			public boolean isApplicableOn(Path1 original) {
				return true;
			}

			@Override
			public Path1 generates(Path1 original) {
				List<Location> genes = original.getLocations();
				List<Location> newGenes = new ArrayList<Location>(genes);
				int start = rand.nextInt(genes.size());
				int end = rand.nextInt(genes.size());
				if (start > end) {
					int temp = start;
					start = end;
					end = temp;
				}
				Collections.reverse(newGenes.subList(start, end));
				return new Path1(newGenes);
			}
		};
		return Arrays.asList(littleMutation, bigMutation);
	}

	@SuppressWarnings("unchecked")
	protected Collection<Explorator<Path1>> getExplorators(
			final Collection<Location> locations) {
		Explorator<Path1> random = new Explorator<Path1>() {

			@Override
			public String toString() {
				return "random";
			}

			@Override
			public boolean isApplicableOn(Collection<Path1> configuration) {
				return true;
			}

			@Override
			public Path1 generates(Collection<Path1> population) {
				List<Location> genes = new LinkedList<Location>(locations);
				Collections.shuffle(genes);
				Path1 path = new Path1(genes);
				System.out.println("Random: " + path);
				return path;
			}
		};
		Explorator<Path1> combinator = new Explorator<Path1>() {
			@Override
			public String toString() {
				return "combinator";
			}

			private final Random rand = new Random();

			@Override
			public boolean isApplicableOn(Collection<Path1> population) {
				return population.size() > 1;
			}

			@Override
			public Path1 generates(Collection<Path1> population) {
				log.info("Explorating...");
				List<Location> remaining = new LinkedList<Location>(locations);
				LinkedList<Location> genes = new LinkedList<Location>();
				int geneSize = locations.size();

				genes.add(remaining.remove(rand.nextInt(remaining.size())));
				log.fine("Gene " + genes.size() + "/" + geneSize + ": "
						+ genes.getLast());
				while (!remaining.isEmpty()) {
					log.finer("Voting...");
					Location from = genes.getLast();
					Map<Location, Integer> votes = new HashMap<Location, Integer>();
					for (Location location : remaining) {
						votes.put(location, 0);
					}
					for (Path1 voter : population) {
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

					log.finer("Selecting...");
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
					log.fine("Gene " + genes.size() + "/" + geneSize + ": "
							+ best);
				}

				Path1 path = new Path1(genes);
				log.info("Explorator generated: " + path);
				System.out.println("Combinator: " + path);
				return path;
			}
		};
		return Arrays.asList(random, combinator);
	}

}
