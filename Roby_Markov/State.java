package Roby_Markov;

/**
 * Class to model a state in a Markov chain
 * 
 * @author 537513rc Roby Cremers
 *
 */

public class State {
	private int period;
	private int componentAge;
	private int componentAge2 = -1;

	/**
	 * Constructs a State object with one component
	 * 
	 * @param period       the period of this state
	 * @param componentAge the age of the component in this state
	 */
	public State(int period, int componentAge) {
		this.period = period;
		this.componentAge = componentAge;
	}

	/**
	 * Constructs a State object with two components
	 * 
	 * @param period        the period of this state
	 * @param componentAge1 the age of the first component in this state
	 * @param componentAge2 the age of the second component in this state
	 */
	public State(int period, int componentAge1, int componentAge2) {
		this.period = period;
		this.componentAge = componentAge1;
		this.componentAge2 = componentAge2;
	}

	/**
	 * Method to return the period of this State
	 * 
	 * @return the period of this state
	 */
	public int getPeriod() {
		return this.period;
	}

	/**
	 * Method to return the age of the (first) component of this State
	 * 
	 * @return the age of the (first) component in this state
	 */
	public int getAge() {
		return this.componentAge;
	}

	/**
	 * Method to return the age of the second component of this State if it exists
	 * 
	 * @return the age of the second component in this state, if not existent -1
	 */
	public int getAge2() {
		return this.componentAge2;
	}

	@Override
	public boolean equals(Object other) {
		/*
		 * Two states are equal if they have the same period, both either 1 or 2
		 * components and the ages of the components are the same
		 */
		State other2 = (State) other;
		return (this.componentAge == other2.getAge() && this.period == other2.getPeriod()
				&& this.componentAge2 == other2.getAge2());
	}

	@Override
	public int hashCode() {
		/*
		 * Note that if either of the three is one bigger or smaller, the values of the
		 * other characteristics cannot compensate for this
		 */
		int result = 5 + this.componentAge * 15 + this.period + (this.componentAge2 + 1) * 5003;
		return result;
	}

	@Override
	public String toString() {
		String state = "(" + Integer.toString(this.period) + ", " + Integer.toString(this.componentAge);
		if (this.componentAge2 != -1) {
			state += ", " + Integer.toString(this.componentAge2);
		}
		return state + ")";
	}
}
