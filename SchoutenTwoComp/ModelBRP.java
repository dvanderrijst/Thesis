package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

public class ModelBRP {
    private final Instance i;
    private final int M;
    private final int m;
    private final int N;
    private final int[] I0;
    private final int[] I1;
//    private final int[] I2;
//    private final int[] K;
    private static IloCplex cplex;
    private static List<IloConstraint> constraints = new ArrayList<>();
//    private final int[] A = new int[]{0,1,2,3};  //there are 4 actions: a={0,1,2,3}
    private final int[] A = new int[]{0,1};
    private static IloNumVar[][][] x;
    private static IloNumVar[] y;
//    private static IloNumVar[][][] z;
//    private static IloNumVar[][] t;


    public ModelBRP(Instance i) throws IloException {
        this.i = i;
        this.M = i.M;
        this.m = i.m;
        this.N = i.n;
        this.I0 = i.I0;
        this.I1 = i.I1;
        cplex = new IloCplex();

        setVariables();
        setObjective();
        setConstraints();
    }

    private void setConstraints() throws IloException {
        for (int i0 : I0) {
            for (int i1 :I1) {
                    IloNumExpr sum1 = cplex.constant(0.0);
                    int[] actions = A(i0,i1);
                    for(int a : actions){
                        sum1 = cplex.sum(sum1, x[i0][i1][a]);
                    }

                    IloNumExpr sum2 = cplex.constant(0.0);
                    for(int j0 : I0) {
                        for (int j1 : I1) {
                                int[] actionsJ = A(j0, j1);
                                for (int a : actionsJ) {
                                    sum2 = cplex.sum(sum2, cplex.prod(pi(j0, j1, i0, i1, a), x[j0][j1][a]));
                                }
                            }
                    }
                    constraints.add(cplex.addEq(cplex.diff(sum1,sum2),0.0, "9b,"+i0+","+i1));
                }
            }

    }

    private void setObjective() throws IloException {
        IloNumExpr sum = cplex.constant(0.0);

        for (int i0 : I0) {
            for (int i1 : I1) {
                int[] actions = A(i0,i1);
                for(int a : actions){
                    IloNumExpr prod = cplex.prod(x[i0][i1][a],c(i0,i1,a));
                    sum = cplex.sum(sum,prod);
                }
            }
        }
        cplex.addMinimize(sum);
    }
    private void setVariables() throws IloException {
        x = new IloNumVar[I0.length][I1.length][A.length];
        for (int i0 : I0) {
            for (int i1 : I1) {
                int[] actions = A(i0,i1);
                for(int a : actions) {
                    IloNumVar var = cplex.numVar(0.0, 1.0 / (m * N), "x(" + i0 + "," + i1 + "," + a + ")"); //set the limit of x to 1/mN. This is actually positive infinity but this might save storage?
                    x[i0][i1][a] = var;
                }
            }
        }

        y = new IloNumVar[I0.length];
        for (int i0 : I0) {
            IloNumVar var = cplex.boolVar("y("+i0+")");
            y[i0] = var;
        }
    }
    private int[] A(int i0, int i1){
        int[] actions;
        if(i1 == 0 || i1 == M ){
            actions = new int[]{1};
        }
        else{
            actions = new int[]{0,1};
        }
        return actions;
    }
    private double c(int i0, int i1, int a){
        double c = 0.0;
        i0 = i0%N;
        if(a==0){
            c = 0.0;
        }
        else if(a==1){
            if(i1!=0) {
                c = i.cPR_i[i0];
            }
            else {
                c = i.cCR_i[i0];
            }
        }
        return c;
    }

    private double pi(int i0, int i1, int j0, int j1, int a){
        double pi_value = 0.0;

        if( j0 != i0 + 1 ){
            // System.out.println("this values for j0 is not corresponding to j0 = i0 + 1 mod(N). We return pi=0.0.");
        }
        else if(a==0){
            if( (j1 == i1 + 1) & (i1!=0 || i1!=M) ) {
                pi_value = 1 - i.probCondX_x_k(i1, 1);
            }
            else if( (j1 == 0) & (i1!=0 || i1!=M) ) {
                pi_value = i.probCondX_x_k(i1, 1);
            }
            else{
                pi_value = 0.0;
            }
        }
        else if(a==1){
            if(j1==1) {
                pi_value = 1- i.probCondX_x_k(0, 1);
            }
            else if(j1==0) {
                pi_value = i.probCondX_x_k(0, 1);
            }
            else{
                pi_value = 0.0;
            }
        }
        return pi_value;
    }
}
