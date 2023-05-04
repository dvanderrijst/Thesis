package Roby_Markov;

import java.util.Set;

import org.apache.commons.math3.distribution.WeibullDistribution;

/**
 * Class that models a Markov chain for one or two wind turbine components
 * 
 * @author 537513rc Roby Cremers
 *
 */
public class MarkovChain {
	private States states;
	
	private TransitionMatrix transitionMatrix;
	
	private int numPeriods;
	private int maxCompAge;
	
	private WeibullDistribution weibull;

	/**
	 * Constructs a MarkovChain object
	 * 
	 * @param numPeriods    the number of periods in a year (or m years)
	 * @param twoComponents boolean that is true if there are two components in the
	 *                      Markov chain
	 * @param alpha         the scale parameter of the Weibull distribution of the
	 *                      component(s)
	 * @param beta          the shape parameter of the Weibull distribution of the
	 *                      component(s)
	 */
	public MarkovChain(int numPeriods, boolean twoComponents, int alpha, int beta) {
		WeibullDistribution distribution = new WeibullDistribution(beta, alpha);
		this.weibull = distribution; 
		
		this.maxCompAge = (int) (distribution.inverseCumulativeProbability(0.999) + 1);
		this.numPeriods = numPeriods;

		this.states = new States(this.numPeriods, twoComponents, this.maxCompAge);
		this.transitionMatrix = new TransitionMatrix(this.states, this.numPeriods, twoComponents, this.maxCompAge,
				distribution);
	}
	
	/**
	 * Constructs a MarkovChain object where the maximum component age is specified
	 * 
	 * @param numPeriods    the number of periods in a year (or m years)
	 * @param maxCompAge	the maximum component age
	 * @param twoComponents boolean that is true if there are two components in the
	 *                      Markov chain
	 * @param alpha         the scale parameter of the Weibull distribution of the
	 *                      component(s)
	 * @param beta          the shape parameter of the Weibull distribution of the
	 *                      component(s)
	 */
	public MarkovChain(int numPeriods, int maxCompAge, boolean twoComponents, int alpha, int beta) {
		WeibullDistribution distribution = new WeibullDistribution(beta, alpha);
		this.weibull = distribution; 
		
		this.maxCompAge = maxCompAge;
		this.numPeriods = numPeriods;

		this.states = new States(this.numPeriods, twoComponents, this.maxCompAge);
		this.transitionMatrix = new TransitionMatrix(this.states, this.numPeriods, twoComponents, this.maxCompAge,
				distribution);
	}


	/**
	 * Method that returns the set of states of this Markov chain
	 * 
	 * @return the states of this Markov chain
	 */
	public Set<State> getStates() {
		return this.states.getStates();
	}

	/**
	 * Method that returns the set of states where the first component has failed
	 * 
	 * @return the set of states with a failed first component
	 */
	public Set<State> getFailedStatesFirst() {
		return this.states.getFailedStatesFirst();
	}

	/**
	 * Method that returns the set of states where the second component has failed
	 * 
	 * @return the set of states with a failed second component
	 */
	public Set<State> getFailedStatesSecond() {
		return this.states.getFailedStatesSecond();
	}

	/**
	 * Method that returns the set of states where all components have failed
	 * 
	 * @return the set of states with only failed components
	 */
	public Set<State> getFailedStatesAll() {
		return this.states.getFailedStatesAll();
	}

	/**
	 * Method that returns the set of states where at least one of the components has failed
	 * 
	 * @return the set of states with at least one failed component
	 */
	public Set<State> getFailedStatesEither() {
		return this.states.getFailedStatesEither();
	}
	
	/**
	 * Method that returns the transition matrix of this Markov chain
	 * 
	 * @return the transition matrix of this Markov chain
	 */
	public TransitionMatrix getTransitionMatrix() {
		return this.transitionMatrix;
	}

	/**
	 * Method that returns the number of periods in a year (or m years)
	 * 
	 * @return the number of periods
	 */
	public int getNumPeriods() {
		return this.numPeriods;
	}

	/**
	 * Method to set the maximum age of the component
	 */
	public void setMaxCompAge(int maxAge) {
		this.maxCompAge = maxAge;
	}
	
	/**
	 * Method that returns the maximum age of a component
	 * 
	 * @return the maximum age of a component
	 */
	public int getMaxCompAge() {
		return this.maxCompAge;
	}
	
	/**
	 * Method that returns the distribution of a component
	 * 
	 * @return the distribution of a component
	 */
	public WeibullDistribution getDistribution() {
		return this.weibull;
	}

}
