package fr.vergne.optimization.TSP;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import fr.vergne.optimization.TSP.path.AbstractPath;
import fr.vergne.optimization.TSP.path.AbstractPath.Transition;
import fr.vergne.optimization.TSP.path.Location;
import fr.vergne.optimization.TSP.path.Path3;
import fr.vergne.optimization.generator.Explorator;
import fr.vergne.optimization.generator.Mutator;

public class TSP3 extends AbstractTSP<Path3> {
	private final static Random rand = new Random();
	private final List<Location> reference = new LinkedList<Location>(
			getLocations());

	public static void main(String[] args) throws IOException {
		new TSP3().run();
	}

	@Override
	protected PathIncubator<Path3> getIncubator(Collection<Location> locations) {
		PathIncubator<Path3> incubator = new PathIncubator<Path3>();
		for (Explorator<Path3> explorator : getExplorators(locations)) {
			incubator.addExplorator(explorator);
		}
		for (Mutator<Path3> mutator : getMutators(locations)) {
			incubator.addMutator(mutator);
		}
		incubator.setMaxSize(5);
		return incubator;
	}

	protected Collection<Mutator<Path3>> getMutators(
			Collection<Location> locations) {
		Collection<Mutator<Path3>> mutators = new LinkedList<Mutator<Path3>>();
		mutators.add(new Mutator<Path3>() {

			@Override
			public String toString() {
				return "little";
			}

			@Override
			public boolean isApplicableOn(Path3 original) {
				Set<Location> locations = new HashSet<Location>();
				List<Transition> transitions = new LinkedList<Transition>(
						original.getTransitions());
				for (Transition transition : transitions) {
					locations.add(transition.getL1());
					locations.add(transition.getL2());
				}
				return locations.containsAll(reference)
						&& reference.containsAll(locations);
			}

			@Override
			public Path3 generates(Path3 original) {
				List<Transition> transitions = new LinkedList<Transition>(
						original.getTransitions());
				Location move = reference.get(rand.nextInt(reference.size()));

				Location from = null;
				Location to = null;
				Iterator<Transition> iterator = transitions.iterator();
				while (iterator.hasNext()) {
					Transition transition = iterator.next();
					if (transition.getL1().equals(move)) {
						to = transition.getL2();
						iterator.remove();
					} else if (transition.getL2().equals(move)) {
						from = transition.getL1();
						iterator.remove();
					} else {
						// unrelated transition
					}
				}
				// FIXME check transitions make closed loops
				transitions.add(new Transition(from, to));

				Transition insertion = transitions.remove(rand
						.nextInt(transitions.size()));
				transitions.add(new Transition(insertion.getL1(), move));
				transitions.add(new Transition(move, insertion.getL2()));

				return new Path3(reference, transitions);
			}
		});
		return mutators;
	}

	protected Collection<Explorator<Path3>> getExplorators(
			final Collection<Location> locations) {
		Collection<Explorator<Path3>> explorators = new LinkedList<Explorator<Path3>>();
		explorators.add(new Explorator<Path3>() {

			@Override
			public String toString() {
				return "random";
			}

			@Override
			public boolean isApplicableOn(Collection<Path3> population) {
				return true;
			}

			@Override
			public Path3 generates(Collection<Path3> population) {
				List<Location> locations = new LinkedList<Location>(reference);
				Collections.shuffle(locations);
				Collection<Transition> transitions = AbstractPath
						.explode(locations);

				Boolean[] genes = new Boolean[Path3.geneSize(reference)];
				Iterator<Transition> chain = Path3.transitionChain(reference);
				int index = 0;
				while (chain.hasNext()) {
					Transition transition = chain.next();
					genes[index] = transitions.contains(transition);
					index++;
				}
				Path3 path = new Path3(reference, genes);
				// System.out.println("Random: " + path);
				return path;
			}
		});
		explorators.add(new Explorator<Path3>() {

			@Override
			public String toString() {
				return "combinator";
			}

			@Override
			public boolean isApplicableOn(Collection<Path3> population) {
				return population.size() >= 2;
			}

			@Override
			public Path3 generates(Collection<Path3> population) {
				// TreeSet<Path3> list = new TreeSet<Path3>(population);
				LinkedList<Path3> list = new LinkedList<Path3>(population);
				Collections.shuffle(list);
				Iterator<Path3> iterator = list.iterator();
				Path3 parent1 = iterator.next();
				Path3 parent2 = iterator.next();

				Boolean[] genes1 = parent1.getGenes();
				Boolean[] genes2 = parent2.getGenes();
				Boolean[] newGenes = new Boolean[Path3.geneSize(reference)];
				for (int i = 0; i < genes1.length; i++) {
					newGenes[i] = rand.nextBoolean() ? genes1[i] : genes2[i];
					newGenes[i] = rand.nextDouble() > 0.999 ? !newGenes[i]
							: newGenes[i];
				}

				Path3 child = new Path3(reference, newGenes);
				// System.out.println("Combinator: " + child);
				return child;
			}
		});
		return explorators;
	}
}
