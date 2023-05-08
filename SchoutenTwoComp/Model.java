package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Model {
    private final Instance i;
    private final int M;
    private final int m;
    private final int N;
    private final int[] I0;
    private final int[] I1;
    private final int[] I2;
    private final int[] K = new int[]{1,2};
    private static IloCplex cplex;
    private final int[] A = new int[]{0,1,2,3};  //there are 4 actions: a={0,1,2,3}
    private static IloNumVar[][][][] x;
    private static IloNumVar[][] y;
    private static IloNumVar[][][] z;
    private static IloNumVar[][] t;
    public Model(Instance i) throws IloException {
        cplex = new IloCplex();
        this.i = i;
        M = i.M;
        m = i.m;
        N = i.N;
        I0 = i.I0;
        I1 = i.I1;
        I2 = i.I2;
        
        setVariables();
        setObjective();
        setConstraints();
        cplex.solve();
    }

    private void setConstraints() throws IloException {
        //set constraints 9b
        for (int i0 : I0) {
            for (int i1 :I1) {
                for (int i2 : I2) {

                    IloNumExpr sum1 = cplex.constant(0.0);
                    int[] actions = A(i0,i1,i2);
                    for(int a : actions){
                        sum1 = cplex.sum(sum1, x[i0][i1][i2][a]);
                    }

                    IloNumExpr sum2 = cplex.constant(0.0);
                    for (int j0 : I0) {
                        for (int j1 : I1) {
                            for (int j2 : I2) {
                                int[] actionsJ = A(j0, j1, j2);
                                for (int aj : actionsJ) {
                                    sum2 = cplex.sum(sum2, cplex.prod(pi(j0,j1,j2,i0,i1,i2,aj),x[j0][j1][j2][aj]));
                                }
                            }
                        }
                    }
                    cplex.addEq(cplex.prod(sum1,sum2),0.0);
                }
            }
        }

        //set constraint 9c
        for (int i0 : I0){
            IloNumExpr sum = cplex.constant(0.0);
            for (int i1 :I1) {
                for (int i2 : I2) {
                    int[] actions = A(i0,i1,i2);
                    for(int a : actions){
                        sum = cplex.sum(sum, x[i0][i1][i2][a]);
                    }
                }
            }
            double fraction = 1.0/(m*N);
            cplex.addEq(sum, fraction);
        }

        //set constraints 9d,9e,9f,9g
        for (int i0 : I0) {
            for (int i1 :I1) {
                for (int i2 : I2) {
                    for (int k : K){

                        //constraint 9d & 9e & 9f & 9g
                        IloNumExpr sum9d = null;
                        IloNumExpr dif9e = null;
                        IloNumExpr sum9f = null;
                        IloNumExpr dif9g = null;
                        if(k==1){
                            sum9d = cplex.sum( x[i0][i1][i2][0], z[k][i0][i1]);
                            dif9e = cplex.diff(x[i0][i1][i2][k], z[k][i0][i1]);
                            sum9f = cplex.sum( x[i0][i1][i2][k], z[3-k][i0][i2]);
                            dif9g = cplex.diff(x[i0][i1][i2][3], z[k][i0][i1]);
                        } else if (k==2) {
                            sum9d = cplex.sum( x[i0][i1][i2][0], z[k][i0][i2]);
                            dif9e = cplex.diff(x[i0][i1][i2][k], z[k][i0][i2]);
                            sum9f = cplex.sum( x[i0][i1][i2][k], z[3-k][i0][i1]);
                            dif9g = cplex.diff(x[i0][i1][i2][3], z[k][i0][i2]);
                        }
                        cplex.addLe(sum9d,1.0);
                        cplex.addLe(dif9e,0.0);
                        cplex.addLe(sum9f,1.0);
                        cplex.addLe(dif9g,0.0);
                    }
                }
            }
        }

        //set constraints 9h, 9i
        for (int i0 : I0){
            for (int j0: I0 ) {
                for ( int k : K){
                    if(j0 < i0){
                        IloNumExpr sumL = cplex.sum(t[k][i0], cplex.prod(j0, y[k][j0]), cplex.prod(m*N, y[k][i0]));
                        int sumR = m*N + i0;
                        cplex.addLe(sumL,sumR);
                    }
                    if(j0 > i0){
                        IloNumExpr sumL = cplex.sum(t[k][i0], cplex.prod(j0, y[k][j0]));
                        int sumR = m*N + i0;
                        cplex.addLe(sumL, sumR);
                    }
                }
            }
        }

        //set constraints 9j, 9k
        for (int i0 : I0){
            for (int k : K){

                //constraint 9j
                if(k==1){
                    for(int i1 : I1){
                        IloNumExpr diff = cplex.diff(z[k][i0][i1], y[k][i0]);
                        cplex.addLe(diff, 0.0);
                    }
                }
                else if(k==2){
                    for(int i2: I2){
                        IloNumExpr diff = cplex.diff(z[k][i0][i2], y[k][i0]);
                        cplex.addLe(diff, 0.0);
                    }
                }

                //constraint 9k
                if(k==1){
                    for(int i1: I1){
                        for(int j1 : I1){
                            if (j1 > i1){
                                IloNumExpr diff = cplex.diff(z[k][i0][i1], z[k][i0][j1]);
                                cplex.addLe(diff, 0.0);
                            }
                        }
                    }
                }
                else if (k==2){
                    for(int i2: I2){
                        for(int j2 : I2){
                            if (j2 > i2){
                                IloNumExpr diff = cplex.diff(z[k][i0][i2], z[k][i0][j2]);
                                cplex.addLe(diff, 0.0);
                            }
                        }
                    }
                }
            }
        }
    }

    private void setObjective() throws IloException {

        IloNumExpr sum = cplex.constant(0.0);

        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int i2 : I2) {
                    int[] actions = A(i0,i1,i2);
                    for(int a : actions){
                        IloNumExpr prod = cplex.prod(x[i0][i1][i2][a],c(i0,i1,i2,a));
                        sum = cplex.sum(sum,prod);
                    }
                }
            }
        }
        cplex.addMinimize(sum);
    }

    /**
     * creates the variables  x[i0][i1][i2][a], y[k][i0],  z[k][i0][ik] and t[k][i0] for in our cplex model.
     * @throws IloException
     */
    private void setVariables() throws IloException {
        // x[i0][i1][i2][a]
        x = new IloNumVar[I0.length][I1.length][I2.length][A.length];
        for (int i0 : I0) {
            for (int i1 :I1) {
                for (int i2 : I2) {
                    for (int a : A) {
                        IloNumVar var = cplex.numVar(0.0,1.0/(m*N), "x("+i0+","+i1+","+i2+","+a+")"); //set the limit of x to 1/mN. This is actually positive infinity but this might save storage?
                        x[i0][i1][i2][a] = var;
                    }
                }
            }
        }

        // y[k][i0]
        y = new IloNumVar[K.length + 1][I0.length]; //we do 3 here, so that we can use k=1 and k=2. To keep this clear
        for (int k : K) {
            for (int i0 : I0) {
                IloNumVar var = cplex.boolVar("y("+k+","+i0+")");
                y[k][i0] = var;
            }
        }

        // z[k][i0][ik]
        z = new IloNumVar[K.length + 1][I0.length][I1.length];
        for (int k : K) {
            for (int i0 : I0) {
                for (int ik : I1) {
                    IloNumVar var = cplex.boolVar("z("+k+","+i0+","+ik+")");
                    z[k][i0][ik] = var;
                }
            }
        }

        // t[k][i0]
        t = new IloNumVar[K.length + 1][I0.length]; //we do 3 here, so that we can use k=1 and k=2. To keep this clear
        for (int k : K) {
            for (int i0 : I0) {
                IloNumVar var = cplex.intVar(0,m*N+1,"t("+k+","+i0+")"); //m*N+1 seems like a reasonable upperbound.
                t[k][i0] = var;
            }
        }
    }

    /**
     * return the array int[] A. These are the action points that are possible, given the values of i0, i1, i2 which are the
     * time, age component 1 and age component 2 respectively.
     * @param i0 time
     * @param i1 age component 1
     * @param i2 age component 2
     * @return possible action points
     */
    private int[] A(int i0, int i1, int i2){
        int[] A;
        if ( (i1 == 0 || i1 == M) & (i2 == 0 || i2 == M)){
            A = new int[]{3} ;
        }
        else if( (i1 == 0 || i1 == M) & (i2 != 0 & i2 != M)){
            A = new int[]{1,3} ;
        }
        else if( (i2 == 0 || i2 == M) & (i1 != 0 & i1 != M)){
            A = new int[]{2,3} ;
        }
        else{
            A = new int[]{0,1,2,3};
        }
        return A;
    }

    /**
     * the value for pi(). See the corresponding thesis-report to find this in an easier-to-read format.
     * @param i0 time current
     * @param i1 age component 1
     * @param i2 age component 2
     * @param j0 time next step
     * @param j1 age component 1 next step
     * @param j2 age component 2 next step
     * @param a action taken
     * @return value of pi
     */
    private double pi(int i0, int i1, int i2, int j0, int j1, int j2, int a){
        double pi_value = 0.0;
        if( j0 != (i0+1)%i.N){
            System.out.println("this values for j0 is not corresponding to j0 = i0 + 1 mod(N). We return pi=0.0.");
        }
        else if(a==0){
            if((j1 == (i1 + 1)) & (j2 == (i2 + 1)) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.p_i(i1))*(1.0-i.p_i(i2));
            }
            else if((j1 == (i1 + 1)) & (j2 == 0) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.p_i(i1))*i.p_i(i2);
            }
            else if((j1 == 0) & (j2 == (i2 + 1)) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = i.p_i(i1)*(1.0-i.p_i(i2));
            }
            else if((j1 == 0) & (j2 == 0) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = i.p_i(i1)*i.p_i(i2);
            }
            else{
                pi_value = 0.0;
            }
        }
        else if(a==1){
            if     ((j1 == 1) & (j2 == (i2 + 1)) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.p_i(0))*(1.0-i.p_i(i2));
            }
            else if((j1 == 1) & (j2 == 0) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.p_i(0))*i.p_i(i2);
            }
            else if((j1 == 0) & (j2 == (i2 + 1)) & (i2 != 0) & (i2 != M)){
                pi_value = i.p_i(0)*(1.0-i.p_i(i2));
            }
            else if((j1 == 0) & (j2 == 0) & (i2 != 0) & (i2 != M)){
                pi_value = i.p_i(0)*i.p_i(i2);
            }
            else{
                pi_value = 0.0;
            }
        }
        else if(a==2) {
            if ((j1 == (i1 + 1)) & (j2 == 1) & (i1 != 0) & (i1 != M)) {
                pi_value = (1.0 - i.p_i(i1)) * (1.0 - i.p_i(0));
            } else if ((j1 == (i1 + 1)) & (j2 == 0) & (i1 != 0) & (i1 != M)) {
                pi_value = (1.0 - i.p_i(i1)) * i.p_i(0);
            } else if ((j1 == 0) & (j2 == 1) & (i1 != 0) & (i1 != M)) {
                pi_value = i.p_i(i1) * (1.0 - i.p_i(0));
            } else if ((j1 == 0) & (j2 == 0) & (i1 != 0) & (i1 != M)) {
                pi_value = i.p_i(i1) * i.p_i(0);
            } else {
                pi_value = 0.0;
            }
        }
        else if(a==3){
            if((j1 == 1) & (j2 == 1)){
                pi_value = (1.0 - i.p_i(0))*(1.0-i.p_i(0));
            }
            else if((j1 == 1) & (j2 == 0) ){
                pi_value = (1.0 - i.p_i(0))*i.p_i(0);
            }
            else if((j1 == 0) & (j2 == 1)){
                pi_value = i.p_i(0)*(1.0-i.p_i(0));
            }
            else if((j1 == 0) & (j2 == 0)){
                pi_value = i.p_i(0)*i.p_i(0);
            }
            else{
                pi_value = 0.0;
            }
        }
        else{
            System.out.println("This value for a does not exist");
        }
        return pi_value;
    }

    /**
     * Costs for a certain time in the year, for ages i1 and i2 for the two components, and for action a.
     * @param i0 time
     * @param i1 age component 1
     * @param i2 age component 2
     * @param a action taken
     * @return value of the costs
     */
    private double c(int i0, int i1, int i2, int a){
        double c = 0.0;
        if (a==0){
            c = 0.0;
        }
        else if(a==1){
            if (i1 != 0){
                c = i.cPR_i[i0] + i.d;
            }
            else if(i1 ==0){
                c = i.cCR_i[i0] + i.d;
            }
        }
        else if(a==2) {
            if (i2 != 0) {
                c = i.cPR_i[i0] + i.d;
            } else if (i2 == 0) {
                c = i.cCR_i[i0] + i.d;
            }
        }
        else if(a==3){
            if (i1 != 0 & i2 != 0){
                c = 2 * i.cPR_i[i0] + i.d;
            }
            else if (i1 == 0 & i2 != 0){
                c = i.cCR_i[i0] + i.cPR_i[i0] + i.d;
            }
            else if (i1 != 0 & i2 == 0){
                c = i.cCR_i[i0] + i.cPR_i[i0] + i.d;
            }
            else if (i1 == 0 & i2 == 0) {
                c = 2 * i.cCR_i[i0] + i.d;
            }
        }
        else{
            System.out.println("This value for a does not exist");
        }
        return c;
    }

}
