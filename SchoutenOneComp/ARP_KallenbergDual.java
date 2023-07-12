package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;


public class ARP_KallenbergDual {

    private static IloNumVar[][][] x;
    private IloCplex cplex;
    public final Instance instance;

    public final double discount;

    public ARP_KallenbergDual(Instance instance, double discount) throws IloException{
        this.instance = instance;
        this.discount = discount;

        System.out.println("actions grid - rows are age, columns time.");
        for(int i0 : instance.I0){
            for(int i1: instance.I1){
                this.cplex = new IloCplex();
                this.x = new IloNumVar[instance.I0.length][instance.I1.length][2];
                System.out.printf("%10s",getOptimalAction(i0,i1));
            }
            System.out.println();
        }
    }

    /**
     * Sets up the model, returns the optimal action, and cleans the cplex model.
     * @param i0 from starting state is i=(i0,i1)
     * @param i1 from starting state is i=(i0,i1)
     * @return action a that is the optimal action
     * @throws IloException
     */
    public int getOptimalAction(int i0, int i1) throws IloException {
        setVariables();
        setConstraints();
        setObjective();
        cplex.setOut(null);
        cplex.setWarning(null);
        cplex.exportModel("kallenbergDual.lp");
        cplex.solve();
        int a = getAction(i0,i1);
        cplex.endModel();
        cplex.close();
        return a;
    }

    public void setVariables() throws IloException {

        for(int i0 : instance.I0){
            for(int i1 : instance.I1){
                for(int a : getPossibleActions(i0,i1)){
                    x[i0][i1][a] = cplex.numVar(0,Double.POSITIVE_INFINITY, "x("+a+","+i0+","+i1+")");
                }
            }
        }
    }

    public void setConstraints() throws IloException {
        for(int j0 : instance.I0) {
            for (int j1 : instance.I1) {

                IloNumExpr sum = cplex.constant(0);

                for(int i0 : instance.I0) {
                    for (int i1 : instance.I1) {
                        for(int a : getPossibleActions(i0,i1)) {

                            double firstPartSum;
                            if (j0 == i0 && j1 == i1) {
                                firstPartSum = 1 - discount * instance.piOneDim(i0, i1, j0, j1, a);
                            } else {
                                firstPartSum = - discount * instance.piOneDim(i0, i1, j0, j1, a);
                            }

                            sum = cplex.sum(sum, cplex.prod(firstPartSum, x[i0][i1][a]));
                        }
                    }
                }

                cplex.addEq(sum, 10);
            }
        }
    }

    public void setObjective() throws IloException {

        IloNumExpr sum = cplex.constant(0);

        for(int i0 : instance.I0) {
            for (int i1 : instance.I1) {
                for (int a : getPossibleActions(i0, i1)) {
                    sum = cplex.sum(sum, cplex.prod(getCost(i0,i1,a), x[i0][i1][a]));
                }
            }
        }

        cplex.addMinimize(sum);
    }

    public int getAction(int i0_start, int i1_start) throws IloException{
        int[] actions = getPossibleActions(i0_start,i1_start);
        for(int a : actions) {
            if(cplex.getValue(x[i0_start][i1_start][a])>0){
                return a;
            }
        }
        return -1;
    }


    /**
     * Not all actions are possible for certain i1 values. This method returns the possible actions.
     * @param i0 period in state i
     * @param i1 age of component in state i
     * @return possible actions, in int[] array.
     */
    public int[] getPossibleActions(int i0, int i1){
        int[] actions;
        if(i1 == 0 || i1 == instance.M ){
            actions = new int[]{1};
        }
        else{
            actions = new int[]{0,1};
        }
        return actions;
    }

    /**
     * We are working with period dependent costs, and with CM and PM costs. This method return the
     * correct costs when inserting state i=(i0,i1) and action a.
     * @param i0 period in state i
     * @param i1 age of component in state i
     * @param a action done in state i
     * @return cost that will be incurred.
     */
    public double getCost(int i0, int i1, int a){
        double c = 0.0;
        i0 = i0%instance.N;
        if(a==0){
            c = 0.0;
        }
        else if(a==1){
            if(i1!=0) {
                c = instance.cPR_i_t[0][i0];
            }
            else {
                c = instance.cCR_i_t[0][i0];
            }
        }
        return c;
    }



}
