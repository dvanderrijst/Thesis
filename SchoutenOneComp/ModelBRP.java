package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class ModelBRP {
    public final Instance i;
    public final int M;
    public final int m;
    public final int N;
    public final int[] I0;
    public final int[] I1;
    public static IloCplex cplex;
    public final int[] A = new int[]{0,1};
    public static IloNumVar[][][] x;
    public static IloNumVar[] y;


    public ModelBRP(Instance i) throws IloException {
        this.i = i;
        this.M = i.M;
        this.m = i.m;
        this.N = i.N;
        this.I0 = i.I0;
        this.I1 = i.I1;
        cplex = new IloCplex();

        setVariables();
        setObjective();
        setConstraints();

//        addedConstraint_setXtozero();

        printSolution();
    }

    private void addedConstraint_setXtozero() throws IloException {
        for(int i0 : I0){
            for(int i1 : I1){
                if(i1 != 0 & i1!=M-1){
                    cplex.addEq(x[i0][i1][1],0);
                }
            }
        }
    }

    public void printSolution() throws IloException {
        cplex.exportModel("model1CompBRP.lp");
//        cplex.setOut(null);
        cplex.solve();
        double averageCosts = cplex.getObjValue();
        System.out.println("Yearly costs are " + averageCosts*N);

        for(int i0 : I0){
            System.out.printf("%10.0f", cplex.getValue(y[i0]));
        }

        printX();
    }

    public void printX() throws IloException {
//        System.out.println("\n for a=0: ");
//        for(int i1 : I1) {
//            for (int i0 : I0) {
//                double xval = 0.0;
//                int[] actions = A(i0,i1);
//                boolean found = IntStream.of(actions).anyMatch(n -> n == 0);
//                if(found){
//                    xval=cplex.getValue(x[i0][i1][0]);
//                }
//                System.out.printf("%10.5f", xval);
//            }
//            System.out.println();
//        }
//        System.out.println("\n for a=1: ");
//        for(int i1 : I1) {
//            for (int i0 : I0) {
//                double xval = 0.0;
//                int[] actions = A(i0,i1);
//                boolean found = IntStream.of(actions).anyMatch(n -> n == 1);
//                if(found){
//                    xval=cplex.getValue(x[i0][i1][1]);
//                }
//                System.out.printf("%10.5f", xval);
//            }
//            System.out.println();
//        }
        System.out.println("\n actions grid - rows are age, columns time.");
        for(int i1 : I1) {
            for (int i0 : I0) {
                int[] actions = A(i0,i1);
                boolean notPrinted = true;
                for(int a : actions){
                    if(cplex.getValue(x[i0][i1][a])>0.00000000){
                        System.out.printf("%10s", a);
                        notPrinted = false;
                    }
                }
                if(notPrinted){
                    System.out.printf("%10s", "-");
                }
            }
            System.out.println();
        }
    }

    public void setConstraints() throws IloException {
        constraint10b();
        constraint10cd();
        constraint10e();
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

    public void setVariables() throws IloException {
        setVarX();
        setVarY();
    }

    public void setVarY() throws IloException{
        y = new IloNumVar[I0.length];
        for (int i0 : I0) {
            IloNumVar var = cplex.intVar(0,1,"y("+i0+")");
            y[i0] = var;
        }
    }

    public void setVarX() throws IloException{
        x = new IloNumVar[I0.length][I1.length][A.length];
        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int a : A){
                    IloNumVar var = cplex.numVar(0.0, 1.0 / (m * N), "x(" + i0 + "," + i1 + "," + a + ")"); //set the limit of x to 1/mN. This is actually positive infinity but this might save storage?
                    x[i0][i1][a] = var;
                }
            }
        }
    }

    public int[] A(int i0, int i1){
        int[] actions;
        if(i1 == 0 || i1 == M-1 ){
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
                c = i.cPR_i_t[0][i0];
            }
            else {
                c = i.cCR_i_t[0][i0];
            }
        }
        return c;
    }
    private double pi(int i0, int i1, int j0, int j1, int a){
        double pi_value = 0.0;

        if( j0 != (i0 + 1)%(m*N) ){
            // System.out.println("this values for j0 is not corresponding to j0 = i0 + 1 mod(N). We return pi=0.0.");
            pi_value = 0.0;
        }
        else if(a==0){
            if( (j1 == i1 + 1) & (i1!=0 & i1!=M) ) {
                pi_value = 1 - i.probCondX_x_k(i1, 1);
            }
            else if( (j1 == 0) & (i1!=0 & i1!=M) ) {
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
    public void constraint10e() throws IloException{
        //10e
        for(int i0 : I0){
            IloNumExpr sum = cplex.constant(0.0);
            for(int i1 : I1){
                int[] action = A(i0,i1);
                for(int a : action){
                    sum = cplex.sum(sum,x[i0][i1][a]);
                }
            }
            cplex.addEq(sum, 1.0/(m*N), "10e,"+i0);
        }
    }
    public void constraint10cd() throws IloException {
        //10c & 10d
        for(int i0 : I0){
            for(int i1 : I1){
                if(i1!=0 & i1!=M-1){
                    cplex.addLe(cplex.sum( x[i0][i1][0],y[i0]), 1.0,"10c,"+i0+","+i1);

                    cplex.addLe(cplex.diff(x[i0][i1][1],y[i0]), 0.0,"10d,"+i0+","+i1);
                }
            }
        }
    }
    public void constraint10b() throws IloException {
        //10b
        for (int i0 : I0) {
            for (int i1 : I1) {

                IloNumExpr sum1 = cplex.constant(0.0);
                int[] actions = A(i0, i1);
                for (int a : actions) {
                    sum1 = cplex.sum(sum1, x[i0][i1][a]);
                }



                IloNumExpr sum2 = cplex.constant(0.0);
                for (int j0 : I0) {
                    for (int j1 : I1) {
                        int[] actionsJ = A(j0, j1);
                        for (int a : actionsJ) {
                            sum2 = cplex.sum(sum2, cplex.prod(pi(j0, j1, i0, i1, a), x[j0][j1][a]));
//                            System.out.println(pi(j0, j1, i0, i1, a)+"*"+x[j0][j1][a]);
                        }
                    }
                }
                cplex.addEq(cplex.diff(sum1, sum2), 0.0, "10b," + i0 + "," + i1);
            }
        }
    }

}
