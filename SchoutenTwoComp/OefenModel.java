package SchoutenTwoComp;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class OefenModel {
    private IloCplex cplex = new IloCplex();
    private static IloNumVar[][] x = new IloNumVar[4][2];
    private final double[][] P = {
            {0.1, 0.9, 0.0, 0.0},
            {0.2, 0.0, 0.8, 0.0},
            {0.3, 0.0, 0.0, 0.7},
            {0.1, 0.9, 0.0, 0.0},
    };
    private final int c = 5;
    private final int[] A = new int[]{0,1};
    private final int[] states = new int[]{0,1,2,3};

    public OefenModel() throws IloException {
        initializeVars();
        constraint10b();
        constraint10e();
        setObj();

        cplex.exportModel("oefenModel.lp");
        cplex.setOut(null);
        cplex.solve();
        System.out.println(cplex.getObjValue());
    }

    private void initializeVars() throws IloException {
        for(int a : A){
            for(int i : states){
                x[i][a] = cplex.numVar(0,1,"x(0,"+i+","+a+")");
            }
        }
    }

    private void setObj() throws IloException{
        IloNumExpr sum = cplex.constant(0);
        for(int i : states){
            sum = cplex.sum(sum, cplex.prod(x[i][1], c));
        }
        cplex.addMinimize(sum);
    }

    private int A(int i){
        int A;
        if(i==0 || i==3){
            A = 1;
        }
        else {
            A = 0;
        }
        return A;
    }

    public void constraint10e() throws IloException{
    //10e
        IloNumExpr sum = cplex.constant(0.0);
        for(int i1 : states){
                sum = cplex.sum(sum,x[i1][A(i1)]);
            }
        cplex.addEq(sum, 1);
    }

    public void constraint10b() throws IloException {
        //10b

            for (int i1 : states) {

                IloNumExpr sum2 = cplex.constant(0.0);
                    for (int j1 : states) {
                            sum2 = cplex.sum(sum2, cplex.prod(P[j1][i1], x[j1][A(j1)]));
                        System.out.println("i1="+i1+", j1="+j1+", P[i1][j1]="+P[i1][j1]+", x[j1][A(j1)]="+x[j1][A(j1)]);
                    }
                System.out.println(sum2);
                cplex.addEq(cplex.diff(x[i1][A(i1)], sum2), 0.0);
            }

    }
}
