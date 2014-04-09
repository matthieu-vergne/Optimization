package fr.vergne.optimization.TSP;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import fr.vergne.optimization.TSP.path.Location;
import fr.vergne.optimization.TSP.path.Path2;
import fr.vergne.optimization.generator.Explorator;
import fr.vergne.optimization.generator.Mutator;

public class TSP2 extends AbstractTSP<Path2> {
	private final static Random rand = new Random();
	private final List<Location> references;

	public static void main(String[] args) throws IOException {
		new TSP2().run();
	}

	public TSP2() {
		super();
		this.references = new LinkedList<Location>(getLocations());
	}

	@Override
	protected PathIncubator<Path2> getIncubator(Collection<Location> locations) {
		PathIncubator<Path2> incubator = new PathIncubator<Path2>();
		for (Explorator<Path2> explorator : getExplorators(locations)) {
			incubator.addExplorator(explorator);
		}
		for (Mutator<Path2> mutator : getMutators(locations)) {
			incubator.addMutator(mutator);
		}
		return incubator;
	}

	protected Collection<Mutator<Path2>> getMutators(
			Collection<Location> locations) {
		Collection<Mutator<Path2>> mutators = new LinkedList<Mutator<Path2>>();
		mutators.add(new Mutator<Path2>() {
			@Override
			public String toString() {
				return "rebranch";
			}

			@Override
			public boolean isApplicableOn(Path2 original) {
				return true;
			}

			@Override
			public Path2 generates(Path2 original) {
				List<Integer> genes = original.getIndexes();
				int index = rand.nextInt(genes.size() - 1);
				int newValue = rand.nextInt(genes.size() - index - 1);
				List<Integer> newGenes = new LinkedList<Integer>(genes);
				newGenes.set(index, newValue);
				return new Path2(references, newGenes);
			}
		});
		return mutators;
	}

	protected Collection<Explorator<Path2>> getExplorators(
			final Collection<Location> locations) {
		Collection<Explorator<Path2>> explorators = new LinkedList<Explorator<Path2>>();
		explorators.add(new Explorator<Path2>() {

			@Override
			public String toString() {
				return "random";
			}

			@Override
			public boolean isApplicableOn(Collection<Path2> configuration) {
				return true;
			}

			@Override
			public Path2 generates(Collection<Path2> population) {
				List<Integer> genes = new LinkedList<Integer>();
				for (int i = references.size() - 1; i > 0; i--) {
					genes.add(rand.nextInt(i));
				}
				Path2 path = new Path2(references, genes);
				System.out.println("Random: " + path);
				return path;
			}
		});
		explorators.add(new Explorator<Path2>() {

			@Override
			public String toString() {
				return "combinator";
			}

			@Override
			public boolean isApplicableOn(Collection<Path2> population) {
				return population.size() > 1;
			}

			@Override
			public Path2 generates(Collection<Path2> population) {
				List<Path2> remaining = new LinkedList<Path2>(population);
				Path2 parent1 = remaining.remove(rand.nextInt(remaining.size()));
				Path2 parent2 = remaining.remove(rand.nextInt(remaining.size()));

				List<Integer> genes1 = parent1.getIndexes();
				List<Integer> genes2 = parent2.getIndexes();
				List<Integer> newGenes = new LinkedList<Integer>();
				for (int i = 0; i < references.size() - 1; i++) {
					newGenes.add(rand.nextBoolean() ? genes1.get(i) : genes2
							.get(i));
				}
				Path2 child = new Path2(references, newGenes);
				System.out.println("Combinator: " + child);
				return child;
			}
		});
		return explorators;
	}

}
