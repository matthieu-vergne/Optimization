package fr.vergne.optimization.TSP;

import fr.vergne.optimization.incubator.impl.ExperimentalIncubator;
import fr.vergne.optimization.population.Evaluator;

public class PathIncubator extends ExperimentalIncubator<Path> {

	public PathIncubator() {
		super(new Evaluator<Path, Double>() {

			@Override
			public Double evaluate(Path path) {
				return path.getLength();
			}

		});
	}

	int generatedIndividuals = 0;

	public int getGeneratedIndividuals() {
		return generatedIndividuals;
	}

	@Override
	public void incubate() {
		super.incubate();
		generatedIndividuals++;
	}

}
