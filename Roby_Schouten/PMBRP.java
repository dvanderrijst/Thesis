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
 * Class to solve the periodic Block Replacement Policy
 * 
 * @author 537513rc Roby Cremers
 *
 */
public class PMBRP {
	private CostFunction costs;
	private MarkovChain markov;

	private Map<State, Map<Integer, Double>> xSol;

	private double objective;

	private boolean silent;

	/**
	 * Constructs an pBRP model object
	 * 
	 * @param costs  the cost functions for maintenance of a wind turbine component
	 * @param markov the Markov chain for the wind turbine component(s)
	 * @param silent if true, we do not print the optimal solution
	 */
	public PMBRP(CostFunction costs, MarkovChain markov, boolean silent) {
		this.costs = costs;
		this.markov = markov;
		this.silent = silent;
	}

	/**
	 * Method to create an optimal solution for the p-MBRP
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

		Map<Integer, IloIntVar> y = new HashMap<>();
		for (int i = 1; i <= this.markov.getNumPeriods(); i++) {
			y.put(i, cplex.boolVar("y (" + i + ")"));
		}

		Map<State, IloIntVar> z = new HashMap<>();
		for (State state : this.markov.getStates()) {
			z.put(state, cplex.boolVar("z" + state.toString()));
		}

		Map<Integer, IloIntVar> t = new HashMap<>();
		for (int i = 1; i <= this.markov.getNumPeriods(); i++) {
			t.put(i, cplex.intVar(0, Integer.MAX_VALUE, "t (" + i + ")"));
		}

		// Create the objective
		IloNumExpr objective = cplex.constant(0);
		Set<State> notFailed = new HashSet<>(this.markov.getStates());
		Set<State> failed = new HashSet<>(this.markov.getFailedStatesAll());
		notFailed.removeAll(failed);

		for (State state : notFailed) {
			objective = cplex.sum(objective, cplex.prod(this.costs.getPM(state.getPeriod()), x.get(state).get(1)));
		}
		for (State state : failed) {
			objective = cplex.sum(objective, cplex.prod(this.costs.getCM(state.getPeriod()), x.get(state).get(1)));
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
				State state = new State(i1, i2);
				for (int action : this.markov.getTransitionMatrix().getActions(state)) {
					restrSteadyState2 = cplex.sum(restrSteadyState2, x.get(state).get(action));
				}
			}
			cplex.addEq(restrSteadyState2, 1 / ((double) this.markov.getNumPeriods()), "SteadyState2(" + i1 + ")");
		}

		/*
		 * Add restrictions that make sure that if we perform PM for a period i_1 and
		 * age i_2 (z_i_1,i_2=1), then we preventively maintain in period i_1 (y_i_1
		 * forced to be 1)
		 */
		for (State state : this.markov.getStates()) {
			cplex.addLe(cplex.diff(z.get(state), y.get(state.getPeriod())), 0, "yEquals1IfzEquals1" + state.toString());
		}

		/*
		 * Add restrictions that make sure that if we perform PM in a certain period for
		 * a certain age, we also perform PM in that period for all larger ages
		 */
		for (State state : this.markov.getStates()) {
			for (int j = state.getAge() + 1; j <= this.markov.getMaxCompAge(); j++) {
				cplex.addLe(cplex.diff(z.get(state), z.get(new State(state.getPeriod(), j))), 0,
						"PMforLargerAges(" + state.toString() + "," + j + ")");
			}
		}

		// Add restrictions that make sure that t(k)<=T_k-T_{k-1} for each k=2,...,n
		for (int i = 0; i <= this.markov.getNumPeriods(); i++) {
			for (int j = 1; j < i; j++) {
				IloNumExpr restr_t = cplex.constant(0);
				restr_t = cplex.sum(restr_t,
						cplex.sum(t.get(i), cplex.prod(y.get(j), j + this.markov.getNumPeriods())));
				cplex.addLe(restr_t, this.markov.getNumPeriods() + i, "RestrOn_t(k)(" + i + "," + j + ")");
			}
		}

		// Add restrictions that make sure that t(1)<=T_1-T_n+mN
		for (int i = 1; i <= this.markov.getNumPeriods(); i++) {
			for (int j = i + 1; j <= this.markov.getNumPeriods(); j++) {
				IloNumExpr restr_t0 = cplex.constant(0);
				restr_t0 = cplex.sum(restr_t0, cplex.sum(t.get(i), cplex.prod(y.get(j), j)));
				cplex.addLe(restr_t0, this.markov.getNumPeriods() + i, "RestrOn_t(0)(" + i + "," + j + ")");
			}
		}

		/*
		 * Add restrictions that threshold t should be larger than i_2 if y_{i_1}=1 but
		 * we do not perform PM for period i_1 and age i_2 (z_{i_1,i_2}=0)
		 */
		for (State state : this.markov.getStates()) {
			IloNumExpr restrTresholdLarger = cplex.constant(0);
			restrTresholdLarger = cplex.sum(restrTresholdLarger, cplex.diff(
					cplex.prod(this.markov.getMaxCompAge(), y.get(state.getPeriod())),
					cplex.sum(cplex.prod(this.markov.getMaxCompAge(), z.get(state)), t.get(state.getPeriod()))));
			cplex.addLe(restrTresholdLarger, this.markov.getMaxCompAge() - 1 - state.getAge(),
					"RestTresholdLarger" + state.toString());
		}

		/*
		 * Add restrictions that threshold t should be smaller than i_2 if we perform PM
		 * for period i_1 and age i_2 (z_{i_1,i_2}=1)
		 */
		for (State state : this.markov.getStates()) {
			IloNumExpr restrTresholdLarger = cplex.constant(0);
			restrTresholdLarger = cplex.sum(restrTresholdLarger,
					cplex.sum(cplex.prod(this.markov.getMaxCompAge(), z.get(state)), t.get(state.getPeriod())));
			cplex.addLe(restrTresholdLarger, this.markov.getMaxCompAge() + state.getAge(),
					"RestTresholdSmaller" + state.toString());
		}

		// Additional restrictions to make sure that x is linked to the value of z
		for (State state : notFailed) {
			if (this.markov.getTransitionMatrix().getActions(state).contains(0)) {
				cplex.addLe(cplex.sum(x.get(state).get(0), z.get(state)), 1, "1.PM if z=1 " + state.toString());
			}
			cplex.addLe(cplex.diff(x.get(state).get(1), z.get(state)), 0, "2.PM if z=1 " + state.toString());
		}

		// Do not print all information while solving
		cplex.setOut(null);

		// cplex.exportModel("model_p-MBRP.lp");

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

				/*
				 * Print what the maintenance block times and corresponding critical maintenance
				 * ages are
				 */
				for (int i1 = 1; i1 <= this.markov.getNumPeriods(); i1++) {
					for (int i2 = 1; i2 <= this.markov.getMaxCompAge(); i2++) {
						if (this.xSol.get(new State(i1, i2)).get(1) > 0.0) {
							System.out.println(
									"We maintain in period " + i1 + " and have critical maintenance age " + i2);
							break;
						}
					}
				}

				// Finally, we compute the fraction of maintenance activities that are CM
				double sumProbCM = 0;
				double sumTotalMaintenance = 0;
				for (int i1 = 1; i1 <= this.markov.getNumPeriods(); i1++) {
					sumProbCM += this.xSol.get(new State(i1, 0)).get(1);
					for (int i2 = 0; i2 <= this.markov.getMaxCompAge(); i2++) {
						sumTotalMaintenance += this.xSol.get(new State(i1, i2)).get(1);
					}
				}
				System.out.println(
						"The faction of maintenance activities that are CM is " + (sumProbCM / sumTotalMaintenance));
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