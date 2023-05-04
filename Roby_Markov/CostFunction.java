package Roby_Markov;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to model the cost function of maintenance of a wind turbine component
 * @author 537513rc Roby Cremers
 *
 */

public class CostFunction {
	private List<Double> PM;
	private List<Double> CM;
	
	/**
	 * Constructor for the CostFunction class
	 * @param numPeriods the number of periods that we want to model
	 * @param PM_avg the yearly average cost for preventive maintenance
	 * @param CM_avg the yearly average cost for corrective maintenance
	 * @param delta models how much the costs differ over the year (more if larger value)
	 * @param phi models when the costs are the highest
	 */
	public CostFunction(int numPeriods, int PM_avg, int CM_avg, double delta, double phi) {
		this.PM = new ArrayList<>();
		this.CM = new ArrayList<>();
		
		for (int i = 1; i <= numPeriods; i++) {
			// Calculate PM cost for period i and add to list 'PM' and do same for CM
			this.PM.add(PM_avg + PM_avg*delta*Math.cos(2*Math.PI*i/((double)12) + phi));
			this.CM.add(CM_avg + CM_avg*delta*Math.cos(2*Math.PI*i/((double)12) + phi));
		}		
	}
	
	/**
	 * Returns the PM cost that corresponds to the given period
	 * @param period the period for which you want to know the PM cost
	 * @return the corresponding PM cost
	 */
	public double getPM(int period) {
		return PM.get(period-1);
	}
	
	/**
	 * Returns the CM cost that corresponds to the given period
	 * @param period the period for which you want to know the CM cost
	 * @return the corresponding CM cost
	 */
	public double getCM(int period) {
		return CM.get(period-1);
	}
}
