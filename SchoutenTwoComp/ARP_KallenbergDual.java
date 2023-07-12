package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.IOException;


public class ARP_KallenbergDual {

    private static IloNumVar[][][][] x;
    private IloCplex cplex;
    public final Instance instance;
    public final ModelMBRP modelMBRP;

    public final double discount;

    public ARP_KallenbergDual(Instance instance, double discount) throws IloException, IOException {
        this.instance = instance;
        this.discount = discount;
        this.modelMBRP = new ModelMBRP(instance, "justForMethods,.txt");

        System.out.println("actions grid - rows are age i1, columns age i2");
//        for(int i0 : instance.I0){
        this.cplex = new IloCplex();
        this.x = new IloNumVar[instance.I0.length][instance.I1.length][instance.I2.length][4];
        setUpAndSolve();
            int i0 = 5;
            for(int i1: instance.I1){
                for(int i2 : instance.I2) {
                    System.out.printf("%8s", getAction(i0, i1, i2));
                }
//            }
            System.out.println();
        }

        cplex.endModel();
        cplex.close();
    }

    /**
     * Sets up the model, returns the optimal action, and cleans the cplex model.
     * @return action a that is the optimal action
     * @throws IloException
     */
    public void setUpAndSolve() throws IloException {
        setVariables();
        setConstraints();
        setObjective();
        cplex.setOut(null);
        cplex.setWarning(null);
        cplex.exportModel("kallenbergDual.lp");
        cplex.solve();
    }

    public void setVariables() throws IloException {

        for(int i0 : instance.I0){
            for(int i1 : instance.I1) {
                for (int i2 : instance.I2) {
                    for (int a : modelMBRP.A(i0, i1, i2)) {
                        x[i0][i1][i2][a] = cplex.numVar(0, Double.POSITIVE_INFINITY, "x(" + a + "," + i0 + "," + i1 + "," + i2 + ")");
                    }
                }
            }
        }
    }

    public void setConstraints() throws IloException {
        for(int j0 : instance.I0) {
            for (int j1 : instance.I1) {
                for (int j2 : instance.I2) {

                    IloNumExpr sum = cplex.constant(0);

                    for (int i0 : instance.I0) {
                        for (int i1 : instance.I1) {
                            for (int i2 : instance.I2) {
                                for (int a : modelMBRP.A(i0, i1, i2)) {

                                    double firstPartSum;
                                    if (j0 == i0 && j1 == i1 && j2 == i2) {
                                        firstPartSum = 1 - discount * modelMBRP.pi(i0, i1, i2, j0, j1, j2, a);
                                    } else {
                                        firstPartSum = -discount * modelMBRP.pi(i0, i1, i2, j0, j1, j2, a);
                                    }

                                    sum = cplex.sum(sum, cplex.prod(firstPartSum, x[i0][i1][i2][a]));
                                }
                            }
                        }
                    }

                    cplex.addEq(sum, 10);
                }
            }
        }
    }

    public void setObjective() throws IloException {

        IloNumExpr sum = cplex.constant(0);

        for(int i0 : instance.I0) {
            for (int i1 : instance.I1) {
                for (int i2 : instance.I2) {
                    for (int a : modelMBRP.A(i0, i1,i2)) {
                        sum = cplex.sum(sum, cplex.prod(modelMBRP.c(i0, i1, i2, a), x[i0][i1][i2][a]));
                    }
                }
            }
        }

        cplex.addMinimize(sum);
    }

    public int getAction(int i0_start, int i1_start, int i2_start) throws IloException{
        int[] actions = modelMBRP.A(i0_start,i1_start,i2_start);
        for(int a : actions) {
            if(cplex.getValue(x[i0_start][i1_start][i2_start][a])>0){
                return a;
            }
        }
        return -1;
    }



}
