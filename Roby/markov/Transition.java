package Roby.markov;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.distribution.WeibullDistribution;

/**
 * Class to model all possible transitions given a starting state. It holds all
 * possible actions given that state and the corresponding possible next states
 * with the probability of ending up in that state.
 * 
 * @author 537513rc Roby Cremers
 *
 */
public class Transition {
	private State from;
	private int maxCompAge;

	/*
	 * Create a map that links all possible actions given the State 'from' to the
	 * possible next states and the corresponding probability
	 */
	private Map<Integer, Map<State, Double>> transitions;

	/**
	 * Constructs a Transition object for one or two components
	 * 
	 * @param from            the state to transition from
	 * @param numPeriods      the number of periods in a year (or m years)
	 * @param twoComponents   boolean that is true if there are two components
	 * @param maxComponentAge the maximum age of a wind turbine component
	 * @param weibull         the distribution of the component(s)
	 */
	public Transition(State from, int numPeriods, boolean twoComponents, int maxComponentAge,
			WeibullDistribution weibull) {
		this.from = from;
		this.transitions = new HashMap<>();
		this.maxCompAge = maxComponentAge;

		if (!twoComponents) {
			computeProbsOneComp(numPeriods, maxComponentAge, weibull);
		} else {
			computeProbsTwoComp(numPeriods, maxComponentAge, weibull);
		}

	}

	private void computeProbsOneComp(int numPeriods, int maxComponentAge, WeibullDistribution weibull) {
		int nextPeriod = (this.from.getPeriod() + 1);
		nextPeriod = (nextPeriod > numPeriods) ? nextPeriod % numPeriods : nextPeriod;

		if (this.from.getAge() != 0 && this.from.getAge() != maxComponentAge) {
			// These are the possible next states when performing action 0 (do nothing)
			Map<State, Double> nextStates0 = new HashMap<>();

			/*
			 * Note that conditional probability
			 * P(X=i_2|X>=i_2)=P(X=i_2)/P(X>=i_2)=P(X=i_2)/(1-P(X<=i_2)+P(X=i_2))
			 * Discretization now results in
			 * p_{i_2}=P(X=i_2+1|X>=i_2)=(P(X<=i_2+1)-P(X<=i_2))/(1-P(X<=i_2))
			 */
			double probFail = (weibull.cumulativeProbability(from.getAge() + 1)
					- weibull.cumulativeProbability(from.getAge()))
					/ (1 - weibull.cumulativeProbability(from.getAge()));

			nextStates0.put(new State(nextPeriod, 0), probFail);
			nextStates0.put(new State(nextPeriod, this.from.getAge() + 1), 1 - probFail);

			this.transitions.put(0, nextStates0);
		}

		// These are the possible next states when performing action 1 (maintaining)
		Map<State, Double> nextStates1 = new HashMap<>();

		double probFail = (weibull.cumulativeProbability(1) - weibull.cumulativeProbability(0))
				/ (1 - weibull.cumulativeProbability(0));

		nextStates1.put(new State(nextPeriod, 0), probFail);
		nextStates1.put(new State(nextPeriod, 1), 1 - probFail);

		this.transitions.put(1, nextStates1);
	}

	private void computeProbsTwoComp(int numPeriods, int maxComponentAge, WeibullDistribution weibull) {
		int nextPeriod = (this.from.getPeriod() + 1);
		nextPeriod = (nextPeriod > numPeriods) ? nextPeriod % numPeriods : nextPeriod;

		if (this.from.getAge() != 0 && this.from.getAge() != maxComponentAge && this.from.getAge2() != 0
				&& this.from.getAge2() != maxComponentAge) {
			// These are the possible next states when performing action 0 (do nothing)
			Map<State, Double> nextStates0 = new HashMap<>();

			/*
			 * Note that conditional probability
			 * P(X=i_2|X>=i_2)=P(X=i_2)/P(X>=i_2)=P(X=i_2)/(1-P(X<=i_2)+P(X=i_2))
			 * Discretization now results in
			 * p_{i_2}=P(X=i_2+1|X>=i_2)=(P(X<=i_2+1)-P(X<=i_2))/(1-P(X<=i_2))
			 */
			double probFail1 = (weibull.cumulativeProbability(from.getAge() + 1)
					- weibull.cumulativeProbability(from.getAge()))
					/ (1 - weibull.cumulativeProbability(from.getAge()));
			double probFail2 = (weibull.cumulativeProbability(from.getAge2() + 1)
					- weibull.cumulativeProbability(from.getAge2()))
					/ (1 - weibull.cumulativeProbability(from.getAge2()));

			nextStates0.put(new State(nextPeriod, 0, 0), probFail1 * probFail2);
			nextStates0.put(new State(nextPeriod, 0, this.from.getAge2() + 1), probFail1 * (1 - probFail2));
			nextStates0.put(new State(nextPeriod, this.from.getAge() + 1, 0), (1 - probFail1) * probFail2);
			nextStates0.put(new State(nextPeriod, this.from.getAge() + 1, this.from.getAge2() + 1),
					(1 - probFail1) * (1 - probFail2));

			this.transitions.put(0, nextStates0);
		}

		if (this.from.getAge2() != 0 && this.from.getAge2() != maxComponentAge) {
			/*
			 * These are the possible next states when performing action 1 (only maintaining
			 * first component)
			 */
			Map<State, Double> nextStates1 = new HashMap<>();

			/*
			 * Note that conditional probability
			 * P(X=i_2|X>=i_2)=P(X=i_2)/P(X>=i_2)=P(X=i_2)/(1-P(X<=i_2)+P(X=i_2))
			 * Discretization now results in
			 * p_{i_2}=P(X=i_2+1|X>=i_2)=(P(X<=i_2+1)-P(X<=i_2))/(1-P(X<=i_2))
			 */
			double probFail1 = (weibull.cumulativeProbability(1) - weibull.cumulativeProbability(0))
					/ (1 - weibull.cumulativeProbability(0));
			double probFail2 = (weibull.cumulativeProbability(from.getAge2() + 1)
					- weibull.cumulativeProbability(from.getAge2()))
					/ (1 - weibull.cumulativeProbability(from.getAge2()));

			nextStates1.put(new State(nextPeriod, 0, 0), probFail1 * probFail2);
			nextStates1.put(new State(nextPeriod, 0, this.from.getAge2() + 1), probFail1 * (1 - probFail2));
			nextStates1.put(new State(nextPeriod, 1, 0), (1 - probFail1) * probFail2);
			nextStates1.put(new State(nextPeriod, 1, this.from.getAge2() + 1), (1 - probFail1) * (1 - probFail2));

			this.transitions.put(1, nextStates1);
		}

		if (this.from.getAge() != 0 && this.from.getAge() != maxComponentAge) {
			/*
			 * These are the possible next states when performing action 2 (only maintaining
			 * second component)
			 */
			Map<State, Double> nextStates2 = new HashMap<>();

			/*
			 * Note that conditional probability
			 * P(X=i_2|X>=i_2)=P(X=i_2)/P(X>=i_2)=P(X=i_2)/(1-P(X<=i_2)+P(X=i_2))
			 * Discretization now results in
			 * p_{i_2}=P(X=i_2+1|X>=i_2)=(P(X<=i_2+1)-P(X<=i_2))/(1-P(X<=i_2))
			 */
			double probFail1 = (weibull.cumulativeProbability(from.getAge() + 1)
					- weibull.cumulativeProbability(from.getAge()))
					/ (1 - weibull.cumulativeProbability(from.getAge()));
			double probFail2 = (weibull.cumulativeProbability(1) - weibull.cumulativeProbability(0))
					/ (1 - weibull.cumulativeProbability(0));

			nextStates2.put(new State(nextPeriod, 0, 0), probFail1 * probFail2);
			nextStates2.put(new State(nextPeriod, 0, 1), probFail1 * (1 - probFail2));
			nextStates2.put(new State(nextPeriod, this.from.getAge() + 1, 0), (1 - probFail1) * probFail2);
			nextStates2.put(new State(nextPeriod, this.from.getAge() + 1, 1), (1 - probFail1) * (1 - probFail2));

			this.transitions.put(2, nextStates2);
		}

		/*
		 * We restrict the number of permitted PM operations: we can only possibly
		 * maintain both when one of them is broken or at maximum age
		 */
		if (this.from.getAge() == 0 || this.from.getAge2() == 0 || this.from.getAge() == this.maxCompAge
				|| this.from.getAge2() == this.maxCompAge) {
			/*
			 * These are the possible next states when performing action 3 (maintaining both
			 * components)
			 */
			Map<State, Double> nextStates3 = new HashMap<>();

			double probFail = (weibull.cumulativeProbability(1) - weibull.cumulativeProbability(0))
					/ (1 - weibull.cumulativeProbability(0));

			nextStates3.put(new State(nextPeriod, 0, 0), probFail * probFail);
			nextStates3.put(new State(nextPeriod, 0, 1), probFail * (1 - probFail));
			nextStates3.put(new State(nextPeriod, 1, 0), (1 - probFail) * probFail);
			nextStates3.put(new State(nextPeriod, 1, 1), (1 - probFail) * (1 - probFail));

			this.transitions.put(3, nextStates3);
		}
	}

	/**
	 * Method to return the state that we transition from
	 * 
	 * @return state to transition from
	 */
	public State getFrom() {
		return this.from;
	}

	/**
	 * Method to return the possible actions given the current state
	 * 
	 * @return the possible actions
	 */
	public Set<Integer> getActions() {
		return this.transitions.keySet();
	}

	/**
	 * Method to return the possible next states given the current state and action
	 * 
	 * @return the possible next states
	 */
	public Set<State> getNextStates(int action) {
		return this.transitions.get(action).keySet();
	}

	/**
	 * Method to return the probability of a transition from state 'from' to state
	 * 'to' given action 'action'
	 * 
	 * @param to     the state that we go to
	 * @param action the action that is performed
	 * @return the probability of this transition given the action
	 */
	public double getProbability(int action, State to) {
		if (!this.transitions.containsKey(action) || !this.transitions.get(action).containsKey(to)) {
			return 0.0;
		}
		return this.transitions.get(action).get(to);
	}
}
