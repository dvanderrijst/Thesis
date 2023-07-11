package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;


public class ARP_KallenbergDual extends ARP_Kallenberg{

    private static IloNumVar[][][] x;
    private final IloCplex cplex = new IloCplex();

    public ARP_KallenbergDual(Instance instance, int i0_start, int i1_start, double discount) throws IloException {
        super(instance, i0_start, i1_start, discount);
    }

    @Override
    public void startUp() throws IloException {
        setVariables();
        setConstraints();
        setObjective();
        cplex.solve();
    }

    @Override
    public void setVariables() throws IloException {
        x = new IloNumVar[instance.I0.length][instance.I1.length][2];
        for(int i0 : instance.I0){
            for(int i1 : instance.I1){
                for(int a : getPossibleActions(i0,i1)){
                    x[i0][i1][a] = cplex.numVar(0,Double.POSITIVE_INFINITY, "x("+a+","+i0+","+i1+")");
                }
            }
        }
    }

    @Override
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

    @Override
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

    @Override
    public int getAction() throws IloException{
        if(cplex.getValue(x[i0_start][i1_start][0])>0){
            return 0;
        }
        else{
            return 1;
        }
    }



}
