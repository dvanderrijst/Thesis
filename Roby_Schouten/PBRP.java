package Roby_Schouten;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import Roby_Markov.CostFunction;
import Roby_Markov.MarkovChain;
import Roby_Markov.State;

/**
 * Class to solve the periodic Block Replacement Policy for one or two
 * components
 * 
 * @author 537513rc Roby Cremers
 *
 */
public class PBRP {
	private boolean twoComponents;

	private CostFunction costs;
	private MarkovChain markov;

	private int r;

	// Create a map that links to each state and action the value of the x-variable
	private Map<State, Map<Integer, Double>> xSol;
	/*
	 * Create a map that links the number (1 for y1 and 2 for y2) to a map with for
	 * each period the value of the y-variable
	 */
	private Map<Integer, Map<Integer, Integer>> ySol;
	private double objective;

	private boolean silent;

	private IloCplex cplex;

	/*
	 * Create map for the decision variables such that we can add constraints on the
	 * variables for the move up and delay heuristic
	 */
	private Map<Integer, IloIntVar> y_1;
	private Map<State, Map<Integer, IloNumVar>> x;

	/**
	 * Constructs an pBRP model object for one or two components
	 * 
	 * @param twoComponents boolean that is true if there are two components in the
	 *                      Markov chain
	 * @param costs         the cost functions for maintenance of a wind turbine
	 *                      component
	 * @param markov        the Markov chain for the wind turbine component(s)
	 * @param r             the maximum number of PM activities per period
	 * @param silent        if true, we do not print the optimal solution
	 */
	public PBRP(boolean twoComponents, CostFunction costs, MarkovChain markov, int r, boolean silent) {
		this.twoComponents = twoComponents;
		this.costs = costs;
		this.markov = markov;
		this.r = r;
		this.silent = silent;
	}

	/**
	 * Method to create an optimal solution for the p-BRP
	 * 
	 * @throws IloException
	 */
	public void solveModel() throws IloException {
		// Create the model
		this.cplex = new IloCplex();

		// Create the variables and their domain restrictions
		this.x = new HashMap<>();
		for (State state : this.markov.getStates()) {
			Map<Integer, IloNumVar> x_state = new HashMap<>();
			for (int action : this.markov.getTransitionMatrix().getActions(state)) {
				x_state.put(action, cplex.numVar(0, Double.MAX_VALUE,
						"x(" + state.toString() + "," + Integer.toString(action) + ")"));
			}
			x.put(state, new HashMap<>(x_state));
		}

		this.y_1 = new HashMap<>();
		Map<Integer, IloIntVar> y_2 = new HashMap<>();
		for (int i1 = 1; i1 <= this.markov.getNumPeriods(); i1++) {
			if (twoComponents) {
				y_1.put(i1, cplex.boolVar("y (" + i1 + ",1)"));
				y_2.put(i1, cplex.boolVar("y (" + i1 + ",2)"));
			} else {
				y_1.put(i1, cplex.boolVar("y (" + i1 + ")"));
			}
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
			/*
			 * In case of two components, it could be that 1 is not a possible action for
			 * the state 'state'
			 */
			if (this.markov.getTransitionMatrix().getActions(state).contains(1)) {
				objective = cplex.sum(objective, cplex.prod(this.costs.getPM(state.getPeriod()), x.get(state).get(1)));
			}
		}
		for (State state : failed1) {
			/*
			 * In case of two components, it could be that 1 is not a possible action for
			 * the state 'state'
			 */
			if (this.markov.getTransitionMatrix().getActions(state).contains(1)) {
				objective = cplex.sum(objective, cplex.prod(this.costs.getCM(state.getPeriod()), x.get(state).get(1)));
			}
		}

		if (twoComponents) {
			for (State state : notFailed1) {
				if (this.markov.getTransitionMatrix().getActions(state).contains(3)) {
					objective = cplex.sum(objective,
							cplex.prod(this.costs.getPM(state.getPeriod()), x.get(state).get(3)));
				}
			}
			for (State state : failed1) {
				if (this.markov.getTransitionMatrix().getActions(state).contains(3)) {
					objective = cplex.sum(objective,
							cplex.prod(this.costs.getCM(state.getPeriod()), x.get(state).get(3)));
				}
			}

			for (State state : notFailed2) {
				if (this.markov.getTransitionMatrix().getActions(state).contains(2)) {
					objective = cplex.sum(objective,
							cplex.prod(this.costs.getPM(state.getPeriod()), x.get(state).get(2)));
				}
				if (this.markov.getTransitionMatrix().getActions(state).contains(3)) {
					objective = cplex.sum(objective,
							cplex.prod(this.costs.getPM(state.getPeriod()), x.get(state).get(3)));
				}
			}
			for (State state : failed2) {
				if (this.markov.getTransitionMatrix().getActions(state).contains(2)) {
					objective = cplex.sum(objective,
							cplex.prod(this.costs.getCM(state.getPeriod()), x.get(state).get(2)));
				}
				if (this.markov.getTransitionMatrix().getActions(state).contains(3)) {
					objective = cplex.sum(objective,
							cplex.prod(this.costs.getCM(state.getPeriod()), x.get(state).get(3)));
				}
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

		// Add restrictions that make sure PM is performed (only) in periods with y=1
		if (!twoComponents) {
			for (State state : notFailed1) {
				if (this.markov.getTransitionMatrix().getActions(state).contains(0)) {
					cplex.addLe(cplex.sum(x.get(state).get(0), y_1.get(state.getPeriod())), 1,
							"1.PM if y=1 " + state.toString());
				}
				// Note that it should only hold when the age is not 0 or the maximum age
				if (state.getAge() != this.markov.getMaxCompAge()) {
					cplex.addLe(cplex.diff(x.get(state).get(1), y_1.get(state.getPeriod())), 0,
							"2.PM if y=1 " + state.toString());
				}
			}
		} else {
			for (State state : this.markov.getStates()) {

				if (x.get(state).containsKey(0) && x.get(state).containsKey(2)) {
					cplex.addLe(
							cplex.sum(cplex.sum(x.get(state).get(0), x.get(state).get(2)), y_1.get(state.getPeriod())),
							1, "1.PM if y1=1 " + state.toString());
				} else if (x.get(state).containsKey(2)) {
					/*
					 * Note that it is possible that action 0 is not a possible action but 2 is
					 * (when the first component has failed), but not the other way around
					 */
					cplex.addLe(cplex.sum(x.get(state).get(2), y_1.get(state.getPeriod())), 1,
							"1.PM if y1=1 " + state.toString());
				}

				if (x.get(state).containsKey(0) && x.get(state).containsKey(1)) {
					cplex.addLe(
							cplex.sum(cplex.sum(x.get(state).get(0), x.get(state).get(1)), y_2.get(state.getPeriod())),
							1, "1.PM if y2=1 " + state.toString());
				} else if (x.get(state).containsKey(1)) {
					/*
					 * Note that it is possible that action 0 is not a possible action but 1 is
					 * (when the second component has failed), but not the other way around
					 */
					cplex.addLe(cplex.sum(x.get(state).get(1), y_2.get(state.getPeriod())), 1,
							"1.PM if y2=1 " + state.toString());
				}

				if (notFailed1.contains(state) && state.getAge() != this.markov.getMaxCompAge()) {
					if (x.get(state).containsKey(1) && x.get(state).containsKey(3)) {
						cplex.addLe(cplex.diff(cplex.sum(x.get(state).get(1), x.get(state).get(3)),
								y_1.get(state.getPeriod())), 0, "2.PM if y1=1 " + state.toString());
					} else if (x.get(state).containsKey(3)) {
						cplex.addLe(cplex.diff(x.get(state).get(3), y_1.get(state.getPeriod())), 0,
								"2.PM if y1=1 " + state.toString());
					} else if (x.get(state).containsKey(1)) {
						cplex.addLe(cplex.diff(x.get(state).get(1), y_1.get(state.getPeriod())), 0,
								"2.PM if y1=1 " + state.toString());
					}
				}

				if (notFailed2.contains(state) && state.getAge2() != this.markov.getMaxCompAge()) {
					if (x.get(state).containsKey(2) && x.get(state).containsKey(3)) {
						cplex.addLe(cplex.diff(cplex.sum(x.get(state).get(2), x.get(state).get(3)),
								y_2.get(state.getPeriod())), 0, "2.PM if y2=1 " + state.toString());
					} else if (x.get(state).containsKey(3)) {
						cplex.addLe(cplex.diff(x.get(state).get(3), y_2.get(state.getPeriod())), 0,
								"2.PM if y2=1 " + state.toString());
					} else if (x.get(state).containsKey(2)) {
						cplex.addLe(cplex.diff(x.get(state).get(2), y_2.get(state.getPeriod())), 0,
								"2.PM if y2=1 " + state.toString());
					}
				}

			}
		}

		// If two components, add restrictions on number of PM activities per period
		if (twoComponents) { 
			for (int i1 = 1; i1 <= this.markov.getNumPeriods(); i1++) {
				cplex.addLe(cplex.sum(y_1.get(i1), y_2.get(i1)), this.r);
			}
		}

		/*
		 * In case of two components and large scale parameter, we restrict the values
		 * of the variables in the hope that it solves faster: we ensure that we do not
		 * preventively maintain in december, januari, februari, march or april
		 */
		// TODO: comment this when delta=0 or delta=1
		if (twoComponents && this.markov.getDistribution().getScale() > 12) {
			for (int i = 0; i < this.markov.getDistribution().getScale() / 12; i++) {
				for (int i1 = 1; i1 <= 3; i1++) {
					cplex.addEq(y_1.get(12 * i + i1), 0);
					cplex.addEq(y_2.get(12 * i + i1), 0);

					for (int i2 = 1; i2 < this.markov.getMaxCompAge(); i2++) {
						for (int i3 = 1; i3 < this.markov.getMaxCompAge(); i3++) {
							State state = new State(12 * i + i1, i2, i3);
							cplex.addEq(x.get(state).get(1), 0);
							cplex.addEq(x.get(state).get(2), 0);
						}
					}
				}

				cplex.addEq(y_1.get(12 * (i + 1)), 0);
				cplex.addEq(y_2.get(12 * (i + 1)), 0);

				for (int i2 = 1; i2 < this.markov.getMaxCompAge(); i2++) {
					for (int i3 = 1; i3 < this.markov.getMaxCompAge(); i3++) {
						State state = new State(12 * (i + 1), i2, i3);
						cplex.addEq(x.get(state).get(1), 0);
						cplex.addEq(x.get(state).get(2), 0);
					}
				}
			}
		}

		/*
		 * In case of two components and large scale parameter, we restrict the values
		 * of the variables in the hope that it solves faster: we ensure that we do not
		 * maintain more than once a year
		 */
		if (twoComponents && this.markov.getDistribution().getScale() > 12) {
			for (int i = 0; i < this.markov.getDistribution().getScale() / 12; i++) {
				IloNumExpr restrFasterSolve1 = cplex.constant(0);
				IloNumExpr restrFasterSolve2 = cplex.constant(0);

				for (int i1 = 12 * i + 1; i1 <= 12 * i + 12; i1++) {
					restrFasterSolve1 = cplex.sum(restrFasterSolve1, y_1.get(i1));
					restrFasterSolve2 = cplex.sum(restrFasterSolve2, y_2.get(i1));
				}

				cplex.addLe(restrFasterSolve1, 1);
				cplex.addLe(restrFasterSolve2, 1);
			}
		}

		// Do not print all information while solving
		cplex.setOut(null);

//		cplex.exportModel("model_p-BRP.lp");

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

			this.ySol = new HashMap<>();
			if (!this.twoComponents) {
				Map<Integer, Integer> y1Sol = new HashMap<>();
				for (int i = 1; i <= this.markov.getNumPeriods(); i++) {
					y1Sol.put(i, (int) Math.round(cplex.getValue(y_1.get(i))));
				}
				this.ySol.put(1, y1Sol);
			} else {
				Map<Integer, Integer> y1Sol = new HashMap<>();
				Map<Integer, Integer> y2Sol = new HashMap<>();
				for (int i = 1; i <= this.markov.getNumPeriods(); i++) {
					y1Sol.put(i, (int) Math.round(cplex.getValue(y_1.get(i))));
					y2Sol.put(i, (int) Math.round(cplex.getValue(y_2.get(i))));
				}
				this.ySol.put(1, y1Sol);
				this.ySol.put(2, y2Sol);
			}

			if (!this.silent) {
				System.out.println("Found optimal solution!");
				System.out.println("Objective = " + cplex.getObjValue() * 12);

				// Print in which periods we maintain
				for (int i1 = 1; i1 <= this.markov.getNumPeriods(); i1++) {
					if (this.ySol.get(1).get(i1) > 0.5) {
						System.out.println("We should maintain component 1 as y1=1");
					}
					if (this.ySol.get(2).get(i1) > 0.5) {
						System.out.println("We should maintain component 2 as y2=1");
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

	}

	/**
	 * Method to add constraints to the cplex model to not allow PM in certain
	 * periods (if the component has not reached age M)
	 * 
	 * @param Q the periods for which we want to restrict PM
	 * @throws IloException
	 */
	public void solveWithConstraintsUnavailable(Set<Integer> Q) throws IloException {
		// We add the constraints that make sure no PM is allowed in periods in Q
		for (Integer i1 : Q) {
			this.cplex.addEq(this.y_1.get(i1), 0);
		}

		cplex.setOut(null);

		// cplex.exportModel("model_p-BRPModified.lp");

		this.cplex.solve();

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

			Map<Integer, Integer> y1Sol = new HashMap<>();
			for (int i = 1; i <= this.markov.getNumPeriods(); i++) {
				y1Sol.put(i, (int) Math.round(cplex.getValue(y_1.get(i))));
			}
			this.ySol.put(1, y1Sol);
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

	}

	/**
	 * Method to close the cplex model
	 */
	public void closeModel() {
		cplex.close();
	}

	/**
	 * Method that queries the optimal solution of the p-BRP
	 * 
	 * @return the optimal solution
	 */
	public Map<State, Map<Integer, Double>> getxSol() {
		return this.xSol;
	}

	/**
	 * Method that queries the optimal solution of the p-BRP
	 * 
	 * @return the optimal solution
	 */
	public Map<Integer, Map<Integer, Integer>> getySol() {
		return this.ySol;
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
