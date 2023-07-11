package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ARP_Kallenberg {
    public final Instance instance;
    public final int i0_start;
    public final int i1_start;
    private final Map<Integer, IloCplex> cplexMap = new HashMap(){};
    public final double discount;
    private static IloNumVar[][][] v;

    public ARP_Kallenberg(Instance instance, int i0_start, int i1_start, double discount) throws IloException {
        this.instance = instance;
        this.i0_start = i0_start;
        this.i1_start = i1_start;
        this.discount = discount;
    }

    public void startUp() throws IloException {
        IloCplex cplex0 = new IloCplex();
        cplex0.setName("cplex0");
        IloCplex cplex1 = new IloCplex();
        cplex1.setName("cplex1");
        cplexMap.put(0,cplex0);
        cplexMap.put(1,cplex1);
        v = new IloNumVar[2][instance.I0.length][instance.I1.length];

        setVariables();
        setConstraints();
        setObjective();
    }


    public void setVariables()  throws IloException{
        for (int a : cplexMap.keySet()){
            IloCplex cplex = cplexMap.get(a);

            for(int i0 : instance.I0){
                for(int i1 : instance.I1){
                    v[a][i0][i1] = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, "v("+a+","+i0+","+i1+")");
                }
            }

        }
    }

    public void setConstraints()  throws IloException{
        for(int a0 : cplexMap.keySet()){
            IloCplex cplex = cplexMap.get(a0);

            for(int i0 : instance.I0) {
                for (int i1 : instance.I1) {
                    for (int a : getPossibleActions(i0, i1)) {
                        double lhs1 = getCost(i0, i1, a);

                        IloNumExpr lhsSum = cplex.constant(0);
                        for (int j0 : instance.I0) {
                            for (int j1 : instance.I1) {
                                lhsSum = cplex.sum(lhsSum, cplex.prod(instance.piOneDim(i0, i1, j0, j1, a), v[a0][j0][j1]));
                            }
                        }
                        IloNumExpr lhs2 = cplex.prod(discount, lhsSum);

                        IloNumExpr lhs = cplex.sum(lhs1, lhs2);


                        IloNumExpr rhs = v[a0][i0][i1];
                        cplex.addLe(lhs, rhs, "c_("+a+","+i0+","+i1+")");
                    }
                }
            }
        }
    }



    public void setObjective()  throws IloException{
        for (int a : cplexMap.keySet()){
            IloCplex cplex = cplexMap.get(a);

            IloNumExpr sum = cplex.constant(0);
            for (int j0 : instance.I0) {
                for (int j1 : instance.I1) {
                    sum = cplex.sum(sum, cplex.prod(instance.piOneDim(i0_start, i1_start, j0, j1, a), v[a][j0][j1]));
                }
            }
            IloNumExpr obj = cplex.prod(discount, sum);
            obj = cplex.sum(obj, getCost(i0_start, i1_start,a));

            cplex.addMinimize(obj);
        }
    }


    public int getAction() throws IloException {
        int bestAction;

        IloCplex cplex0 = cplexMap.get(0);
        cplex0.exportModel("cplex0.lp");
        cplex0.solve();
        System.out.println(cplex0.getValue(v[0][5][11]));
        double obj0 = cplex0.getObjValue();

        IloCplex cplex1 = cplexMap.get(1);
        cplex1.exportModel("cplex1.lp");
        cplex1.solve();
        System.out.println(cplex1.getValue(v[1][5][11]));
        double obj1 = cplex1.getObjValue();

        System.out.println("obj0="+obj0);
        System.out.println("obj1="+obj1);

        if(obj0 < obj1){
            bestAction = 0 ;
        }
        else {
            bestAction = 1 ;
        }

        return bestAction;
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
