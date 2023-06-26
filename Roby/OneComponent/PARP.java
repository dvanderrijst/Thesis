package oneTwoComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import Roby.markov.CostFunction;
import Roby.markov.MarkovChain;
import Roby.markov.State;

/**
 * Class to solve the periodic Age Replacement Policy for one or two components
 * 
 * @author 537513rc Roby Cremers
 *
 */
public class PARP {
	private boolean twoComponents;

	private CostFunction costs;
	private MarkovChain markov;

	private int r;

	private Map<State, Map<Integer, Double>> xSol;

	private double objective;

	private boolean silent;

	/**
	 * Constructs an pARP model object for one or two components
	 * 
	 * @param twoComponents boolean that is true if there are two components in the
	 *                      Markov chain
	 * @param costs         the cost functions for maintenance of a wind turbine
	 *                      component
	 * @param markov        the Markov chain for the wind turbine component(s)
	 * @param r             the maximum number of PM activities per period
	 * @param silent        if true, we do not print the optimal solution
	 */
	public PARP(boolean twoComponents, CostFunction costs, MarkovChain markov, int r, boolean silent) {
		this.costs = costs;
		this.markov = markov;
		this.twoComponents = twoComponents;
		this.r = r;
		this.silent = silent;
	}

	/**
	 * Method to create an optimal solution for the p-ARP
	 * 
	 * @throws IloException
	 */
	public void solveModel() throws IloException {
		// Create the model
		IloCplex cplex = new IloCplex();

		// Create the variables and their domain restrictions
		Map<State, Map<Integer, IloNumVar>> x = new HashMap<>();
		for (State state : this.markov.getStates()) {
			Map<Integer, IloNumVar> x_state = new HashMap<>();
			for (int action : this.markov.getTransitionMatrix().getActions(state)) {
				x_state.put(action, cplex.numVar(0, Double.MAX_VALUE,
						"x(" + state.toString() + "," + Integer.toString(action) + ")"));
			}
			x.put(state, new HashMap<>(x_state));
		}

		// Create the objective
		IloNumExpr objective = cplex.constant(0);

		Set<State> notFailed1 = new HashSet<>(this.markov.getStates());
		Set<State> failed1 = new HashSet<>(this.markov.getFailedStatesFirst());
		notFailed1.removeAll(failed1);

		Set<State> notFailed2 = new HashSet<>(this.markov.getStates());
		Set<State> failed2 = new HashSet<>();
		if (twoComponents) {
			failed2 = new HashSet<>(this.markov.getFailedStatesSecond());
			notFailed2.removeAll(failed2);
		}

		for (State state : notFailed1) {
			// In case of two components, it could be that 1 is not a possible action for
			// the state 'state'
			if (this.markov.getTransitionMatrix().getActions(state).contains(1)) {
				objective = cplex.sum(objective, cplex.prod(this.costs.getPM(state.getPeriod()), x.get(state).get(1)));
			}
		}
		for (State state : failed1) {
			// In case of two components, it could be that 1 is not a possible action for
			// the state 'state'
			if (this.markov.getTransitionMatrix().getActions(state).contains(1)) {
				objective = cplex.sum(objective, cplex.prod(this.costs.getCM(state.getPeriod()), x.get(state).get(1)));
			}
		}

		if (twoComponents) {
			for (State state : notFailed1) {
				objective = cplex.sum(objective, cplex.prod(this.costs.getPM(state.getPeriod()), x.get(state).get(3)));
			}
			for (State state : failed1) {
				objective = cplex.sum(objective, cplex.prod(this.costs.getCM(state.getPeriod()), x.get(state).get(3)));
			}

			for (State state : notFailed2) {
				if (this.markov.getTransitionMatrix().getActions(state).contains(2)) {
					objective = cplex.sum(objective,
							cplex.prod(this.costs.getPM(state.getPeriod()), x.get(state).get(2)));
				}
				objective = cplex.sum(objective, cplex.prod(this.costs.getPM(state.getPeriod()), x.get(state).get(3)));
			}
			for (State state : failed2) {
				if (this.markov.getTransitionMatrix().getActions(state).contains(2)) {
					objective = cplex.sum(objective,
							cplex.prod(this.costs.getCM(state.getPeriod()), x.get(state).get(2)));
				}
				objective = cplex.sum(objective, cplex.prod(this.costs.getCM(state.getPeriod()), x.get(state).get(3)));
			}
		}

		cplex.addMinimize(objective);

		// Add the restrictions

		// Add the first part of the steady state equations
		for (State state : this.markov.getStates()) {
			IloNumExpr restrSteadyState1 = cplex.constant(0);

			for (int action : this.markov.getTransitionMatrix().getActions(state)) {
				restrSteadyState1 = cplex.sum(restrSteadyState1, x.get(state).get(action));
			}

			/*
			 * We determine the set of possible previous states, as only these have a
			 * possible transition probability to the current state
			 */
			Set<State> possiblePrev = new HashSet<>();

			int prevPeriod = state.getPeriod() - 1;
			if (state.getPeriod() == 1) {
				// Note: if we are in period 1, we were in period N in the previous period
				prevPeriod = this.markov.getNumPeriods();
			}

			if (!twoComponents) {

				if (state.getAge() == 0 || state.getAge() == 1) {
					/*
					 * Note: if the component has age 0 or 1, it could have had any age in the
					 * previous period
					 */
					for (int i = 0; i <= this.markov.getMaxCompAge(); i++) {
						possiblePrev.add(new State(prevPeriod, i));
					}
				} else {
					/*
					 * Note: if the component has an age different from 0 or 1, its age was one less
					 * in the previous period
					 */
					possiblePrev.add(new State(prevPeriod, state.getAge() - 1));
				}

			} else {

				if (state.getAge() == 0 || state.getAge() == 1) {
					/*
					 * Note: if the first component has age 0 or 1, it could have had any age in the
					 * previous period
					 */

					if (state.getAge2() == 0 || state.getAge2() == 1) {
						/*
						 * Note: if the second component has age 0 or 1, it could have had any age in
						 * the previous period
						 */
						for (int j2 = 0; j2 <= this.markov.getMaxCompAge(); j2++) {
							for (int j3 = 0; j3 <= this.markov.getMaxCompAge(); j3++) {
								possiblePrev.add(new State(prevPeriod, j2, j3));
							}
						}
					} else {
						/*
						 * Note: if the second component has an age different from 0 or 1, its age was
						 * one less in the previous period
						 */

						for (int j2 = 0; j2 <= this.markov.getMaxCompAge(); j2++) {
							possiblePrev.add(new State(prevPeriod, j2, state.getAge2() - 1));
						}
					}

				} else {
					/*
					 * Note: if the first component has an age different from 0 or 1, its age was
					 * one less in the previous period
					 */
					if (state.getAge2() == 0 || state.getAge2() == 1) {
						for (int j3 = 0; j3 <= this.markov.getMaxCompAge(); j3++) {
							possiblePrev.add(new State(prevPeriod, state.getAge() - 1, j3));
						}
					} else {
						possiblePrev.add(new State(prevPeriod, state.getAge() - 1, state.getAge2() - 1));
					}
				}

			}

			for (State statePrev : possiblePrev) {
				for (int action : this.markov.getTransitionMatrix().getActions(statePrev)) {
					restrSteadyState1 = cplex.diff(restrSteadyState1,
							cplex.prod(this.markov.getTransitionMatrix().getProbability(statePrev, state, action),
									x.get(statePrev).get(action)));
				}
			}

			cplex.addEq(restrSteadyState1, (double) 0, "SteadyState1(" + state.toString() + ")");
		}

		// Add the second part of the steady state equations
		for (int i1 = 1; i1 <= this.markov.getNumPeriods(); i1++) {
			IloNumExpr restrSteadyState2 = cplex.constant(0);
			for (int i2 = 0; i2 <= this.markov.getMaxCompAge(); i2++) {

				if (!twoComponents) {

					State state = new State(i1, i2);
					for (int action : this.markov.getTransitionMatrix().getActions(state)) {
						restrSteadyState2 = cplex.sum(restrSteadyState2, x.get(state).get(action));
					}

				} else {

					for (int i3 = 0; i3 <= this.markov.getMaxCompAge(); i3++) {
						State state = new State(i1, i2, i3);
						for (int action : this.markov.getTransitionMatrix().getActions(state)) {
							restrSteadyState2 = cplex.sum(restrSteadyState2, x.get(state).get(action));
						}
					}
				}

			}
			cplex.addEq(restrSteadyState2, 1 / ((double) this.markov.getNumPeriods()), "SteadyState2(" + i1 + ")");
		}

		/*
		 * If there are two components and r = 1, add the restriction on the number of
		 * PM activities
		 */
		if (twoComponents && r == 1) {
			Set<State> bothNotFailed = new HashSet<>(notFailed1);
			bothNotFailed.retainAll(notFailed2);
			for (State state : bothNotFailed) {
				cplex.addEq(x.get(state).get(3), 0);
			}
		}

		// Do not print all information while solving
		cplex.setOut(null);

		// cplex.exportModel("model_p-ARP.lp");

		cplex.solve();

		// Query the solution
		if (cplex.getStatus() == IloCplex.Status.Optimal) {
			// Save the solution
			this.objective = cplex.getObjValue();
			this.xSol = new HashMap<>();
			for (State state : this.markov.getStates()) {
				Map<Integer, Double> x_state = new HashMap<>();
				for (int action : this.markov.getTransitionMatrix().getActions(state)) {
					x_state.put(action, cplex.getValue(x.get(state).get(action)));
				}
				this.xSol.put(state, new HashMap<>(x_state));
			}

			if (!this.silent) {
				System.out.println("Found optimal solution!");
				System.out.println("Objective = " + cplex.getObjValue() * 12);

				// Find and print for each period the critical maintenance age
				if (!twoComponents) {
					for (int i1 = 1; i1 <= this.markov.getNumPeriods(); i1++) {
						for (int i2 = 1; i2 <= this.markov.getMaxCompAge(); i2++) {
							if (this.xSol.get(new State(i1, i2)).get(1) > 0.0) {
								System.out.println("Period " + i1 + " has critical maintenance age " + i2);
								break;
							}

							if (i2 == this.markov.getMaxCompAge()) {
								System.out.println("Period " + i1 + " has critical maintenance age M");
							}
						}
					}
				} else {
					for (int i1 = 1; i1 <= this.markov.getNumPeriods(); i1++) {
						// First determine the critical maintenance age for component 1 and then for
						// component 2
						for (int i2 = 1; i2 <= this.markov.getMaxCompAge(); i2++) {
							boolean ageFound = false;
							for (int i3 = 0; i3 <= this.markov.getMaxCompAge(); i3++) {
								if ((this.xSol.get(new State(i1, i2, i3)).containsKey(1)
										&& this.xSol.get(new State(i1, i2, i3)).get(1) > 0.0)
										|| (this.xSol.get(new State(i1, i2, i3)).containsKey(3)
												&& this.xSol.get(new State(i1, i2, i3)).get(3) > 0.0)) {
									System.out.println("Period " + i1 + " has critical maintenance age " + i2
											+ " for the first component");
									ageFound = true;
									break;
								}
							}
							if (ageFound) {
								break;
							}
							if (i2 == this.markov.getMaxCompAge()) {
								System.out.println(
										"Period " + i1 + " has critical maintenance age M for the first component");
							}
						}

						for (int i3 = 1; i3 <= this.markov.getMaxCompAge(); i3++) {
							boolean ageFound = false;
							for (int i2 = 0; i2 <= this.markov.getMaxCompAge(); i2++) {
								if ((this.xSol.get(new State(i1, i2, i3)).containsKey(2)
										&& this.xSol.get(new State(i1, i2, i3)).get(2) > 0.0)
										|| (this.xSol.get(new State(i1, i2, i3)).containsKey(3)
												&& this.xSol.get(new State(i1, i2, i3)).get(3) > 0.0)) {
									System.out.println("Period " + i1 + " has critical maintenance age " + i3
											+ " for the second component");
									ageFound = true;
									break;
								}
							}
							if (ageFound) {
								break;
							}
							if (i3 == this.markov.getMaxCompAge()) {
								System.out.println(
										"Period " + i1 + " has critical maintenance age M for the second component");
							}
						}
					}
				}

				// Finally, we compute the fraction of maintenance activities that are CM
				if (!twoComponents) {
					double sumProbCM = 0;
					double sumTotalMaintenance = 0;
					for (int i1 = 1; i1 <= this.markov.getNumPeriods(); i1++) {
						sumProbCM += this.xSol.get(new State(i1, 0)).get(1);
						for (int i2 = 0; i2 <= this.markov.getMaxCompAge(); i2++) {
							sumTotalMaintenance += this.xSol.get(new State(i1, i2)).get(1);
						}
					}
					System.out.println("The faction of maintenance activities that are CM is "
							+ (sumProbCM / sumTotalMaintenance));
				}
			}
		} else {
			if (cplex.getStatus() == IloCplex.Status.Infeasible) {
				System.out.println("infeasible");
			}
			if (cplex.getStatus() == IloCplex.Status.Error) {
				System.out.println("Error");
			}
			if (cplex.getStatus() == IloCplex.Status.Unbounded) {
				System.out.println("Unbounded");
			}
			if (cplex.getStatus() == IloCplex.Status.Unknown) {
				System.out.println("Unknown");
			}
			System.out.println("No optimal solution found");
		}

		// Close the model
		cplex.close();
	}

	/**
	 * Method that queries the optimal solution of the p-BRP
	 * 
	 * @return the optimal solution
	 */
	public Map<State, Map<Integer, Double>> getSol() {
		return this.xSol;
	}

	/**
	 * Method that queries the optimal objective value of the p-BRP
	 * 
	 * @return the optimal objective
	 */
	public double getObjective() {
		return this.objective;
	}
}
