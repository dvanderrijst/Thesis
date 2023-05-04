package Roby_Markov;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.distribution.WeibullDistribution;

/**
 * Class to model all possible transitions given a starting state. It holds all
 * transition objects of the Markov Chain.
 * 
 * @author 537513rc Roby Cremers
 *
 */

public class TransitionMatrix {
	// Map that links pair of the Transition states and the action that is performed
	// to the transition probability
	private Map<State, Transition> transitions;

	/**
	 * Constructs a TransitionMatrix object
	 * 
	 * @param states          the set of possible states in the Markov chain
	 * @param numPeriods      the number of periods in a year
	 * @param twoComponents   boolean that is true if there are two components
	 * @param maxComponentAge the maximum age of a wind turbine component
	 * @param weibull         the distribution of the component(s)
	 */
	public TransitionMatrix(States states, int numPeriods, boolean twoComponents, int maxComponentAge,
			WeibullDistribution weibull) {
		transitions = new HashMap<>();

		for (State state : states.getStates()) {
			transitions.put(state, new Transition(state, numPeriods, twoComponents, maxComponentAge, weibull));
		}
	}

	/**
	 * Method to return the possible actions given the current state 'from'
	 * 
	 * @return the possible actions
	 */
	public Set<Integer> getActions(State from) {
		return this.transitions.get(from).getActions();
	}

	/**
	 * Method to return the possible next states given the current state and action
	 * 
	 * @param from   the current state
	 * @param action the action to be performed in the current period
	 * @return the possible next states
	 */
	public Set<State> getNextStates(State from, int action) {
		return this.transitions.get(from).getNextStates(action);
	}

	/**
	 * Method to return the probability of a transition from state 'from' to state
	 * 'to' given action 'action'
	 * 
	 * @param from   the state that we start from
	 * @param to     the state that we go to
	 * @param action the action that is performed
	 * @return the probability of this transition given the action
	 */
	public double getProbability(State from, State to, int action) {
		return this.transitions.get(from).getProbability(action, to);
	}
}
