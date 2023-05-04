package Roby_Markov;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to model a set of all possible states in a Markov chain
 * 
 * @author 537513rc Roby Cremers
 */

public class States {
	private Set<State> states;
	private Set<State> failed;
	// All states where the (first) component has failed
	private Set<State> failed2;
	// All states where the second component has failed

	/**
	 * Constructs a States object
	 * 
	 * @param numPeriods    the number of periods in a year
	 * @param twoComponents   boolean that is true if there are two components
	 * @param maxCompAge    the maximum age a wind turbine component can become
	 */
	public States(int numPeriods, boolean twoComponents, int maxCompAge) {
		states = new HashSet<>();
		failed = new HashSet<>();
		if (twoComponents) {
			failed2= new HashSet<>();
		}
		
		for (int i1 = 1; i1 <= numPeriods; i1++) {
			for (int i2 = 0; i2 <= maxCompAge; i2++) {
				if (twoComponents) {
					
					for (int i3 = 0; i3 <= maxCompAge; i3++) {
						State newState = new State(i1, i2, i3);
						states.add(newState);
						
						// Add the state to the sets with failed state if necessary
						if (i2 == 0) {
							failed.add(newState);
						}
						if (i3 == 0) {
							failed2.add(newState);
						}
					}
					
				} else {
					State newState = new State(i1, i2);
					
					states.add(newState);
					
					// Add the state to the set with a failed state if necessary
					if (i2 == 0) {
						failed.add(newState);
					}
				}
			}
		}
	}

	/**
	 * Method that returns the set of possible states in the Markov chain
	 * 
	 * @return the set of states
	 */
	public Set<State> getStates() {
		return new HashSet<>(this.states);
	}
	
	/**
	 * Method that returns the set of states where the first component has failed
	 * 
	 * @return the set of states with a failed first component
	 */
	public Set<State> getFailedStatesFirst() {
		return this.failed;
	}
	
	/**
	 * Method that returns the set of states where the second component has failed
	 * 
	 * @return the set of states with a failed second component
	 */
	public Set<State> getFailedStatesSecond() {
		return this.failed2;
	}
	
	/**
	 * Method that returns the set of states where all components have failed
	 * 
	 * @return the set of states with only failed components
	 */
	public Set<State> getFailedStatesAll() {
		if (this.failed2 == null) {
			return this.failed;
		}
		Set<State> failedBoth = new HashSet<>(this.failed);
		failedBoth.retainAll(failed2);
		return failedBoth;
	}
	
	/**
	 * Method that returns the set of states where at least one of the components has failed
	 * 
	 * @return the set of states with at least one failed component
	 */
	public Set<State> getFailedStatesEither() {
		if (this.failed2 == null) {
			return this.failed;
		}
		Set<State> failedEither = new HashSet<>(this.failed);
		failedEither.addAll(failed2);
		return failedEither;
	}

}
