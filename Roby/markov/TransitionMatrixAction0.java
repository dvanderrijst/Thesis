package Roby.markov;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to model the n-step transition probability matrices for the states of a
 * Markov chain if we do not preventively maintain
 * 
 * @author 537513rc Roby Cremers
 *
 */

public class TransitionMatrixAction0 {
	MarkovChain markov;

	// Create a List in which we can store the n-step probabilities, such that we
	// only have to compute them once
	List<double[][]> nStepProbabilities = new ArrayList<>();

	/**
	 * Construct a TransitionMatrixAction0 object
	 * @param markov the Markov chain for this transition matrix
	 */
	public TransitionMatrixAction0(MarkovChain markov) {
		this.markov = markov;

		double[][] matrix = new double[markov.getStates().size()][markov.getStates().size()];
		
		// Note that the period does not matter for the probabilities so we only do it for period 1
		for (int i2 = 0; i2 <= this.markov.getMaxCompAge(); i2++) {
			Set<State> possibleNext = new HashSet<>();
			if (i2 != this.markov.getMaxCompAge() && i2 != 0) {
				possibleNext.add(new State(2, i2 + 1));
				possibleNext.add(new State(2, 0));
				
				for (State stateNext : possibleNext) {
					matrix[i2][stateNext.getAge()] = this.markov.getTransitionMatrix()
							.getProbability(new State(1,i2), stateNext, 0);
				}
			} else {
				// If we have age 0 or M (the maximum age), we have to perform CM
				possibleNext.add(new State(2, 1));
				possibleNext.add(new State(2, 0));
				
				for (State stateNext : possibleNext) {
					matrix[i2][stateNext.getAge()] = this.markov.getTransitionMatrix()
							.getProbability(new State(1, i2), stateNext, 1);
				}
			}	
		}

		// Check if sum over all rows equals 1
		for (State state : markov.getStates()) {
			double sum = 0;
			for (int j = 0; j < matrix.length; j++) {
				sum += matrix[state.getAge()][j];
			}
			if (sum != 1) {
				System.out.println("Error, the sum for state " + state.toString() + " equals " + sum);
			}
		}

		this.nStepProbabilities.add(matrix);
	}

	/**
	 * Method to return the number of a state in the transition matrix
	 * 
	 * @param state the state for which we want to know the number
	 * @return the number of the state
	 */
	public int getNumber(State state) {
		/*
		 * We give each state a number. State with number m has period
		 * m/(maxCompAge+1)+1 and age m-(period-1)*(maxCompAge+1). That is, the number
		 * of the state is m=age+(period-1)*(maxCompAge+1)
		 */
		return state.getAge() + (state.getPeriod() - 1) * (this.markov.getMaxCompAge() + 1);
	}

	/**
	 * Method to return a state given its number in the transition matrix
	 * 
	 * @param m the number or the state
	 * @return the corresponding state
	 */
	public State getState(int m) {
		/*
		 * We give each state a number. State with number m has period
		 * m/(maxCompAge+1)+1 and age m-(period-1)*(maxCompAge+1).
		 */
		int period = m / (this.markov.getMaxCompAge() + 1) + 1;
		return new State(period, m - (period - 1) * (this.markov.getMaxCompAge() + 1));
	}

	/**
	 * Method to compute what period currentPeriod plus v periods is
	 * 
	 * @param currentPeriod the period that we want to count from
	 * @param v             the number of periods we want to go further or back
	 * @return the period that is v periods from currentPeriod
	 */
	public int getPeriod(int currentPeriod, int v) {
		int periodPlusv = currentPeriod + v;
		if (v > 0) {
			periodPlusv = (periodPlusv > this.markov.getNumPeriods()) ? periodPlusv % this.markov.getNumPeriods()
					: periodPlusv;
		} else {
			periodPlusv = (periodPlusv <= 0) ? this.markov.getNumPeriods() + periodPlusv : periodPlusv;
		}
		return periodPlusv;
	}

	/**
	 * Method to multiply two matrices
	 * 
	 * @param A the matrix for which we want to compute the square
	 * @param B
	 */
	private static double[][] multiply(double A[][], double B[][]) {
		double result[][] = new double[A.length][B[0].length];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++) {
				result[i][j] = 0;
				for (int k = 0; k < A[0].length; k++)
					result[i][j] += A[i][k] * B[k][j];
			}
		}

		return result;
	}

	/**
	 * Method to compute the n-step transition probability matrix
	 * 
	 * @param n specifies that we want the n-step transition matrix
	 */
	private void power(int n) {
		if (this.nStepProbabilities.size() >= n) {
			return;
		}

		int size = this.nStepProbabilities.size();
		for (int i = 0; i < n - size; i++) {
			double[][] newMatrix = multiply(this.nStepProbabilities.get(this.nStepProbabilities.size() - 1),
					this.nStepProbabilities.get(0));
			this.nStepProbabilities.add(newMatrix);
		}
	}

	/**
	 * Method to return the n-step transition probability from a state 'from' to a
	 * state 'to'
	 * 
	 * @param n    specifies that we want the n-step transition matrix
	 * @param from the state that we start in
	 * @param to   the state that we want to go to
	 * @return the n-step transition probability
	 */
	public double getnStepTransitionProb(int n, State from, State to) {		
		if (this.getNumber(from) > this.markov.getNumPeriods()*(this.markov.getMaxCompAge()+1) || this.getNumber(to) > this.markov.getNumPeriods()*(this.markov.getMaxCompAge()+1)) {
			// If one of the states is not a possible state, we return 0
			return 0;
		}
		if (this.getPeriod(from.getPeriod(),1) != to.getPeriod()) {
			// If state 'to' is not in the next period, we return 0
			return 0;
		}

		this.power(n);
		
		return this.nStepProbabilities.get(n-1)[from.getAge()][to.getAge()];
	}

}
