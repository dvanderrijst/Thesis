package Roby_Schouten;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Roby_Markov.CostFunction;
import Roby_Markov.MarkovChain;
import Roby_Markov.State;

/**
 * Main class to run the models for one component
 * 
 * @author 537513rc Roby Cremers
 *
 */

public class MainOneComp {

	public static void main(String[] args) throws Exception {
		int numPeriods = 12;
		int PM_avg = 10;
		int CM_avg = 50;
		double phi = -2 * Math.PI / 12;

		List<Integer> betas = new ArrayList<>();
		betas.add(2);
//		betas.add(3);

		List<Integer> alphas = new ArrayList<>();
		alphas.add(1 * numPeriods);
		alphas.add(3 * numPeriods);
//		alphas.add(5*numPeriods);
//		alphas.add(8*numPeriods);
//		alphas.add(10*numPeriods);

		List<Double> deltas = new ArrayList<>();
		deltas.add(0.0);
		deltas.add(0.1);
		deltas.add(0.2);
		deltas.add(0.3);
		deltas.add(0.4);
		deltas.add(0.5);

		// Create a map that links the value of alpha to the value of m
		Map<Integer, Integer> m = new HashMap<>();
		m.put(1 * numPeriods, 1);
		m.put(3 * numPeriods, 3);
//		m.put(5 * numPeriods, 5);
//		m.put(8 * numPeriods, 8);
//		m.put(10 * numPeriods, 10);

		Long startTime = System.currentTimeMillis();

		pARP(numPeriods, PM_avg, CM_avg, phi, alphas, betas, deltas);

		pBRP(numPeriods, PM_avg, CM_avg, phi, alphas, betas, deltas, m);

		pMBRP(numPeriods, PM_avg, CM_avg, phi, alphas, betas, deltas, m);

		/*
		 * To obtain the costs of not maintaining at all, we use the p-BRP model and add
		 * the constraint that we cannot perform PM in any period. To check that these
		 * are indeed equal for each value of delta, I iterate over delta
		 */
//		boolean twoComponents = false;
//		int r = 1;
//
//		for (int alpha : alphas) {
//
//			// Create a list with the periods for which PM not allowed
//			int numPeriodsBRP = numPeriods * m.get(alpha);
//			Set<Integer> periodsNoPM = new HashSet<>();
//			for (int i = 1; i <= numPeriodsBRP; i++) {
//				periodsNoPM.add(i);
//			}
//
//			for (int beta : betas) {
//				MarkovChain markov = new MarkovChain(numPeriodsBRP, 2000, twoComponents, alpha, beta);
//				Map<State, Map<Integer, Double>> x_constant = new HashMap<>();
//
//				for (double delta : deltas) {
//					CostFunction costs = new CostFunction(numPeriodsBRP, PM_avg, CM_avg, delta, phi);
//
//					PBRP pBRP = new PBRP(twoComponents, costs, markov, r, true);
//					System.out.println("For alpha = " + alpha + " and beta = " + beta + " and delta = " + delta * 100
//							+ "%, we have optimal yearly costs when not maintaining: ");
//					pBRP.solveModel();
//
//					pBRP.solveWithConstraintsUnavailable(periodsNoPM);
//					System.out.println("Objective = " + pBRP.getObjective() * 12);
//
//					pBRP.closeModel();
//				}
//			}
//		}

		long endTime = System.currentTimeMillis();

		System.out.println("Execution time is : " + (endTime - startTime));

	}

	/**
	 * Method to compute the optimal p-ARP for component(s)
	 * 
	 * @param numPeriods number of periods in a year (or m years)
	 * @param PM_avg     the yearly average cost for preventive maintenance
	 * @param CM_avg     the yearly average cost for corrective maintenance
	 * @param phi        models when the costs are the highest
	 * @param alphas     the list of scale parameters of the Weibull distribution of
	 *                   the component(s)
	 * @param betas      the list of shape parameters of the Weibull distribution of
	 *                   the component(s)
	 * @param deltas     models how much the costs differ over the year (more if
	 *                   larger value)
	 * @param m          the number of years that takes up one cycle
	 * @throws Exception
	 */
	public static void pARP(int numPeriods, int PM_avg, int CM_avg, double phi, List<Integer> alphas,
			List<Integer> betas, List<Double> deltas) throws Exception {
		boolean twoComponents = false;
		int r = 1;

		for (int alpha : alphas) {
			for (int beta : betas) {

				MarkovChain markov = new MarkovChain(numPeriods, twoComponents, alpha, beta);
				Map<State, Map<Integer, Double>> x_constant = new HashMap<>();

				for (double delta : deltas) {
					CostFunction costs = new CostFunction(numPeriods, PM_avg, CM_avg, delta, phi);

					PARP pARP = new PARP(twoComponents, costs, markov, r, false);
					System.out.println("p-ARP: For alpha = " + alpha + " and beta = " + beta + " and delta = "
							+ delta * 100 + "%, we have: ");
					pARP.solveModel();

					if (delta == 0.0) {
						// We solve the optimal solution for the constant-cost case
						x_constant = pARP.getSol();
					} else {
						computeSavingsConstantPolicy(x_constant, pARP.getObjective(), markov, costs);
					}

					System.out.println();
				}
			}
		}
	}

	/**
	 * Method to compute the optimal p-BRP for component(s)
	 * 
	 * @param the    number of periods in a year (or m years)
	 * @param PM_avg the yearly average cost for preventive maintenance
	 * @param CM_avg the yearly average cost for corrective maintenance
	 * @param phi    models when the costs are the highest
	 * @param alphas the list of scale parameters of the Weibull distribution of the
	 *               component(s)
	 * @param betas  the list of shape parameters of the Weibull distribution of the
	 *               component(s)
	 * @param deltas models how much the costs differ over the year (more if larger
	 *               value)
	 * @param m      the number of years that takes up one cycle
	 * @throws Exception
	 */
	public static void pBRP(int numPeriods, int PM_avg, int CM_avg, double phi, List<Integer> alphas,
			List<Integer> betas, List<Double> deltas, Map<Integer, Integer> m) throws Exception {
		boolean twoComponents = false;
		int r = 1;

		for (int alpha : alphas) {
			for (int beta : betas) {

				int numPeriodsBRP = numPeriods * m.get(alpha);
				MarkovChain markov = new MarkovChain(numPeriodsBRP, twoComponents, alpha, beta);
				Map<State, Map<Integer, Double>> x_constant = new HashMap<>();

				for (double delta : deltas) {
					CostFunction costs = new CostFunction(numPeriodsBRP, PM_avg, CM_avg, delta, phi);

					PBRP pBRP = new PBRP(twoComponents, costs, markov, r, false);
					System.out.println("p-BRP: For alpha = " + alpha + " and beta = " + beta + " and delta = "
							+ delta * 100 + "%, we have: ");
					pBRP.solveModel();

					if (delta == 0.0) {
						// We solve the optimal solution for the constant-cost case
						x_constant = pBRP.getxSol();
					} else {
						computeSavingsConstantPolicy(x_constant, pBRP.getObjective(), markov, costs);
					}

					System.out.println();
				}
			}
		}
	}

	/**
	 * Method to compute the optimal p-MBRP for component(s)
	 * 
	 * @param the    number of periods in a year (or m years)
	 * @param PM_avg the yearly average cost for preventive maintenance
	 * @param CM_avg the yearly average cost for corrective maintenance
	 * @param phi    models when the costs are the highest
	 * @param alphas the list of scale parameters of the Weibull distribution of the
	 *               component(s)
	 * @param betas  the list of shape parameters of the Weibull distribution of the
	 *               component(s)
	 * @param deltas models how much the costs differ over the year (more if larger
	 *               value)
	 * @param m      the number of years that takes up one cycle
	 * @throws Exception
	 */
	public static void pMBRP(int numPeriods, int PM_avg, int CM_avg, double phi, List<Integer> alphas,
			List<Integer> betas, List<Double> deltas, Map<Integer, Integer> m) throws Exception {
		boolean twoComponents = false;

		for (int alpha : alphas) {
			for (int beta : betas) {

				int numPeriodsMBRP = numPeriods * m.get(alpha);
				MarkovChain markov = new MarkovChain(numPeriodsMBRP, twoComponents, alpha, beta);
				Map<State, Map<Integer, Double>> x_constant = new HashMap<>();

				for (double delta : deltas) {
					CostFunction costs = new CostFunction(numPeriodsMBRP, PM_avg, CM_avg, delta, phi);

					PMBRP pMBRP = new PMBRP(costs, markov, false);
					System.out.println("p-MBRP: For alpha = " + alpha + " and beta = " + beta + " and delta = "
							+ delta * 100 + "%, we have: ");
					pMBRP.solveModel();

					if (delta == 0.0) {
						// We solve the optimal solution for the constant-cost case
						x_constant = pMBRP.getSol();
					} else {
						computeSavingsConstantPolicy(x_constant, pMBRP.getObjective(), markov, costs);
					}

					System.out.println();
				}
			}
		}
	}

	/**
	 * Method to compute the percentage savings due to considering time-varying
	 * costs in determining the optimal policy
	 * 
	 * @param x_constant the optimal x-variables (policy) when considering constant
	 *                   costs
	 * @param objective  the optimal objective of the policy when considering
	 *                   time-varying costs
	 * @param markov     the markov chain corresponding to the component(s)
	 * @param costs      the cost function corresponding to PM and CM for the
	 *                   component(s)
	 */
	public static void computeSavingsConstantPolicy(Map<State, Map<Integer, Double>> x_constant, double objective,
			MarkovChain markov, CostFunction costs) {
		/*
		 * We compute the average monthly costs when we apply the constant cost policy
		 * to the case of time-varying costs
		 */
		double objectiveConstant = 0;

		Set<State> notFailed = new HashSet<>(markov.getStates());
		Set<State> failed = new HashSet<>(markov.getFailedStatesAll());
		notFailed.removeAll(failed);

		for (State state : notFailed) {
			objectiveConstant += costs.getPM(state.getPeriod()) * x_constant.get(state).get(1);
		}
		for (State state : failed) {
			objectiveConstant += costs.getCM(state.getPeriod()) * x_constant.get(state).get(1);
		}

		double savingsPercentage = 100 * ((double) (objectiveConstant - objective)) / objective;
		System.out.println(
				"The percentage cost savings due to considering time-varying costs in determining the optimal policy is "
						+ savingsPercentage + "%");
	}

}
