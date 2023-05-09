package SchoutenTwoComp;

import Main.Instance;
import com.sun.security.jgss.GSSUtil;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.CpxException;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Model {
    private final Instance i;
    private final int M;
    private final int m;
    private final int N;
    private final int[] I0;
    private final int[] I1;
    private final int[] I2;
    private final int[] K;
    private static IloCplex cplex;
    private static List<IloConstraint> constraints = new ArrayList<>();
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
        K = i.K;

//        piPrint();

        setVariables();
        setObjective();
        setConstraints();
        cplex.setParam(IloCplex.Param.DetTimeLimit, 60000);
        cplex.exportModel("model.lp");
        cplex.solve();

        IloConstraint[] constrs = new IloConstraint[constraints.size()];
        double[] doubless = new double[constraints.size()];
        int count = 0;
        for(IloConstraint cons : constraints){
            constrs[count]=cons;
            doubless[count] = count;
        }
        for(IloConstraint con : constrs) {
            try{
            IloCplex.ConflictStatus conflictStatus = cplex.getConflict(con);
            System.out.println(conflictStatus);}
            catch(CpxException e){}
        }



        printSolution();
    }

    private void printSolution() throws IloException {
        System.out.println("OBJECTIVE IS " + cplex.getObjValue());

        for(int k : K) {
            System.out.println("\n\nk="+k);

            System.out.print("i0\t");
            for (int i0 : I0) {
                System.out.print(i0 + "\t");
            }

            System.out.print("\ny\t");
            for (int i0 : I0) {
                System.out.print((int) Math.round(cplex.getValue(y[k][i0])) + "\t");
            }
            System.out.print("\nt\t");
            for (int i0 : I0) {
                System.out.print((int) Math.round(cplex.getValue(t[k][i0])) + "\t");
            }

            for (int ik : I1) {
                System.out.print("\nz_"+ik+"\t");
                for (int i0 : I0) {
                    System.out.print((int) Math.round(cplex.getValue(z[k][i0][ik])) + "\t");
                }
            }
        }
        System.out.println();

        for (int i0 : I0) {
            double sumoverA = 0.0;
            for (int a : A) {
//                System.out.println("i0=" + i0+", a=" + a);
                double sum = 0.0;
                for (int i1 : I1) {
                    for (int i2 : I2) {
                        if (x[i0][i1][i1][a] != null) {
                            try{
//                                System.out.printf("%10.3f",cplex.getValue(x[i0][i1][i2][a]));
                                sum = sum + cplex.getValue(x[i0][i1][i2][a]);
                            }
                            catch (IloCplex.UnknownObjectException e){
//                                System.out.printf("%10.3f", 0.0);
                            }
                        }
                        if(i1 == I1.length - 1 & i2 == I2.length-1){
//                            System.out.printf(" sum = %10.3f", sum);
                        }
                    }
//                    System.out.println();
                }
                sumoverA = sumoverA + sum;
                if(a==3){
//                    System.out.println("Total sum over a and over i1,i2 is "+sumoverA+"\n\n ");
                }
            }
        }
        double sum = 0.0;
        for(int i0 : I0){
            for(int i1 : I1){
                for(int i2 : I2){
                    int[] actions = A(i0,i1,i2);
                    for(int a : actions){
                        if(a==3 & Math.round(cplex.getValue(x[i0][i1][i2][a])*1000)/1000.0>0.00) {
                            sum = sum + cplex.getValue(x[i0][i1][i2][a]);
                            System.out.print("i0=" + i0 + ", i1=" + i1 + ", i2=" + i2);
                            System.out.println(", a=" + a + " and x*c = " + Math.round(cplex.getValue(x[i0][i1][i2][a]) * 1000) / 1000.0 + " * " + c(i0, i1, i2, a));
                        }
                    }
                }
            }
        }
        System.out.println("the sum is "+sum);


        System.out.println("OBJECTIVE IS " + cplex.getObjValue());
    }


    private void setConstraints() throws IloException {
        System.out.println("set constraints 9b");
        setConstraint9b();
        System.out.println("set constraint 9c");
        setConstraint9c();
        System.out.println("set constraints 9d,9e,9f,9g");
        setConstraint9defg();
        System.out.println("set constraints 9h, 9i");
        setConstraint9hi();
        System.out.println("set constraints 9j, 9k");
        setConstraint9jk();
        System.out.println("set constraints 9l");
        setConstraint9l();
        System.out.println("set constraints 9m");
        setConstraint9m();
    }

    private void setObjective() throws IloException {
        System.out.println("setting objective");

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
//                    int[] actions = A(i0,i1,i2);
//                    for (int a : actions) {
                    for(int a : A){
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
//                    if(k==2){var = cplex.intVar(0,0);
//                    }
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
//         System.out.println("for i1="+i1+", i2="+i2+" we find array"+ Arrays.toString(A));
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
            // System.out.println("this values for j0 is not corresponding to j0 = i0 + 1 mod(N). We return pi=0.0.");
        }
        else if(a==0){
            if((j1 == (i1 + 1)) & (j2 == (i2 + 1)) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.probCondX_x_k(i1, 1))*(1.0-i.probCondX_x_k(i2, 2));
            }
            else if((j1 == (i1 + 1)) & (j2 == 0) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.probCondX_x_k(i1, 1))*i.probCondX_x_k(i2, 2);
            }
            else if((j1 == 0) & (j2 == (i2 + 1)) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = i.probCondX_x_k(i1, 1)*(1.0-i.probCondX_x_k(i2, 2));
            }
            else if((j1 == 0) & (j2 == 0) & (i1 != 0) & (i1 != M) & (i2 != 0) & (i2 != M)){
                pi_value = i.probCondX_x_k(i1, 1)*i.probCondX_x_k(i2, 2);
            }
            else{
                pi_value = 0.0;
            }
        }
        else if(a==1){
            if     ((j1 == 1) & (j2 == (i2 + 1)) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.probCondX_x_k(0, 1))*(1.0-i.probCondX_x_k(i2, 2));
            }
            else if((j1 == 1) & (j2 == 0) & (i2 != 0) & (i2 != M)){
                pi_value = (1.0 - i.probCondX_x_k(0, 1))*i.probCondX_x_k(i2, 2);
            }
            else if((j1 == 0) & (j2 == (i2 + 1)) & (i2 != 0) & (i2 != M)){
                pi_value = i.probCondX_x_k(0, 1)*(1.0-i.probCondX_x_k(i2, 2));
            }
            else if((j1 == 0) & (j2 == 0) & (i2 != 0) & (i2 != M)){
                pi_value = i.probCondX_x_k(0, 1)*i.probCondX_x_k(i2, 2);
            }
            else{
                pi_value = 0.0;
            }
        }
        else if(a==2) {
            if ((j1 == (i1 + 1)) & (j2 == 1) & (i1 != 0) & (i1 != M)) {
                pi_value = (1.0 - i.probCondX_x_k(i1, 1)) * (1.0 - i.probCondX_x_k(0, 2));
            } else if ((j1 == (i1 + 1)) & (j2 == 0) & (i1 != 0) & (i1 != M)) {
                pi_value = (1.0 - i.probCondX_x_k(i1, 1)) * i.probCondX_x_k(0, 2);
            } else if ((j1 == 0) & (j2 == 1) & (i1 != 0) & (i1 != M)) {
                pi_value = i.probCondX_x_k(i1, 1) * (1.0 - i.probCondX_x_k(0, 2));
            } else if ((j1 == 0) & (j2 == 0) & (i1 != 0) & (i1 != M)) {
                pi_value = i.probCondX_x_k(i1, 1) * i.probCondX_x_k(0, 2);
            } else {
                pi_value = 0.0;
            }
        }
        else if(a==3){
            if((j1 == 1) & (j2 == 1)){

                pi_value = (1.0 - i.probCondX_x_k(0, 1))*(1.0-i.probCondX_x_k(0, 2));
            }
            else if((j1 == 1) & (j2 == 0) ){
                pi_value = (1.0 - i.probCondX_x_k(0, 1))*i.probCondX_x_k(0, 2);
            }
            else if((j1 == 0) & (j2 == 1)){
                pi_value = i.probCondX_x_k(0, 1)*(1.0-i.probCondX_x_k(0, 2));
            }
            else if((j1 == 0) & (j2 == 0)){
//                double one = i.probCondX_x_k(0, 1);
//                double two = i.probCondX_x_k(0, 2);
                pi_value = i.probCondX_x_k(0, 1)*i.probCondX_x_k(0, 2);
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

    private void piPrint(){
        //for printing
        double[][][][][][][] piPrint = new double[I0.length][I1.length][I2.length][I0.length][I1.length][I2.length][4];
        for(int i0 : I0){
            for(int i1 : I1){
                for(int i2 : I2){
                        for(int j1 : I1){
                            for (int j2 : I2){
                                for( int a : A ){
                                    int j0 = i0 + 1;
                                    piPrint[i0][i1][i2][j0][j1][j2][a] = pi(i0,i1,i2,j0,j1,j2,a);
                                    System.out.println(piPrint[i0][i1][i2][j0][j1][j2][a]);
                                }
                            }
                        }

                }
            }
        }
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
            if(i2 == 0 || i1 == 0){
                System.out.println("this can not be possible");
                System.exit(1);
            }
            c = 0.0;
        }
        else if(a==1){
            if(i2 == 0){
                System.out.println("this can not be possible");
                System.exit(1);
            }
            else if (i1 != 0){
                c = i.cPR_i[i0] + i.d;
            }
            else if(i1 ==0){
                c = i.cCR_i[i0] + i.d;
            }
        }
        else if(a==2) {
            if(i1 == 0){
                System.out.println("this can not be possible");
                System.exit(1);
            }
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
                c =  2 * i.cCR_i[i0] + i.d;
            }
        }
        else{
            System.out.println("This value for a does not exist");
        }
        return c;
    }

    private void setConstraint9b() throws IloException{
        for (int i0 : I0) {
            System.out.println(i0);
            for (int i1 :I1) {
                for (int i2 : I2) {

                    IloNumExpr sum1 = cplex.constant(0.0);
                    int[] actions = A(i0,i1,i2);
                    for(int a : actions){
                        sum1 = cplex.sum(sum1, x[i0][i1][i2][a]);
                    }

                    IloNumExpr sum2 = cplex.constant(0.0);
                    for(int j0 : I0) {
                        for (int j1 : I1) {
                            for (int j2 : I2) {
                                int[] actionsJ = A(j0, j1, j2);
                                for (int a : actionsJ) {
                                    sum2 = cplex.sum(sum2, cplex.prod(pi(j0, j1, j2, i0, i1, i2, a), x[j0][j1][j2][a]));
                                }
                            }
                        }
                    }
                    constraints.add(cplex.addEq(cplex.diff(sum1,sum2),0.0, "9b,"+i0+","+i1+","+i2));
                }
            }
        }
    }
    private void setConstraint9c() throws IloException{
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
            constraints.add(cplex.addEq(sum, fraction, "9c"));
        }
    }
    private void setConstraint9defg() throws IloException{
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
                            if(i1!=0 & i1!=M & i2!=0 & i2!=M){sum9d = cplex.sum( x[i0][i1][i2][0], z[k][i0][i1]);}
                            if(i1!=0 & i1!=M){dif9e = cplex.diff(x[i0][i1][i2][k], z[k][i0][i1]);}
                            if(i2!=0 & i2!=M){sum9f = cplex.sum( x[i0][i1][i2][k], z[3-k][i0][i2]);}
                            dif9g = cplex.diff(x[i0][i1][i2][3], z[k][i0][i1]);
                        } else if (k==2) {
                            if(i1!=0 & i1!=M & i2!=0 & i2!=M){sum9d = cplex.sum( x[i0][i1][i2][0], z[k][i0][i2]);}
                            if(i2!=0 & i2!=M){dif9e = cplex.diff(x[i0][i1][i2][k], z[k][i0][i2]);}
                            if(i1!=0 & i1!=M){sum9f = cplex.sum( x[i0][i1][i2][k], z[3-k][i0][i1]);}
                            dif9g = cplex.diff(x[i0][i1][i2][3], z[k][i0][i2]);
                        }
                        else{
                            System.out.println("something wrong with the value of k");
                            System.exit(1);
                        }
                        // add constraints 9d, 9e, 9f, 9g to the model
                        if (sum9d != null) {
                            constraints.add(cplex.addLe(sum9d, 1.0, "9d"));
                        }
                        if (dif9e != null) {
                            constraints.add(cplex.addLe(dif9e, 0.0, "9e"));
                        }
                        if (sum9f != null) {
                            constraints.add(cplex.addLe(sum9f, 1.0, "9f"));
                        }
                        if (dif9g != null) {
                            constraints.add(cplex.addLe(dif9g, 0.0, "9g"));
                        }
                    }
                }
            }
        }
    }
    private void setConstraint9hi() throws IloException{
        for (int i0 : I0){
            for (int j0: I0 ) {
                for ( int k : K){
                    if(j0 < i0){
                        IloNumExpr sumL = cplex.sum(t[k][i0], cplex.prod(j0, y[k][j0]), cplex.prod(m*N, y[k][i0]));
                        int sumR = m*N + i0;
                        constraints.add(cplex.addLe(sumL,sumR, "9h"));
                    }
                    if(j0 > i0){
                        IloNumExpr sumL = cplex.sum(t[k][i0], cplex.prod(j0, y[k][j0]));
                        int sumR = m*N + i0;
                        constraints.add(cplex.addLe(sumL, sumR, "9i"));
                    }
                }
            }
        }
    }
    private void setConstraint9jk() throws IloException{
        for (int i0 : I0){
            for (int k : K){

                //constraint 9j
                if(k==1){
                    for(int i1 : I1){
                        IloNumExpr diff = cplex.diff(z[k][i0][i1], y[k][i0]);
                        constraints.add(cplex.addLe(diff, 0.0, "9j"));
                    }
                }
                else if(k==2){
                    for(int i2: I2){
                        IloNumExpr diff = cplex.diff(z[k][i0][i2], y[k][i0]);
                        constraints.add(cplex.addLe(diff, 0.0, "9j"));
                    }
                }

                //constraint 9k
                if(k==1){
                    for(int i1: I1){
                        for(int j1 : I1){
                            if (j1 > i1){
                                IloNumExpr diff = cplex.diff(z[k][i0][i1], z[k][i0][j1]);
                                constraints.add(cplex.addLe(diff, 0.0, "9k"));
                            }
                        }
                    }
                }
                else if (k==2){
                    for(int i2: I2){
                        for(int j2 : I2){
                            if (j2 > i2){
                                IloNumExpr diff = cplex.diff(z[k][i0][i2], z[k][i0][j2]);
                                constraints.add(cplex.addLe(diff, 0.0, "9k"));
                            }
                        }
                    }
                }
            }
        }
    }
    private void setConstraint9l() throws IloException{
        for(int i0 : I0){
            int k = 1;
            for(int i1 : I1){
                IloNumExpr left = cplex.sum(cplex.prod(M,y[k][i0]), i1+1);
                IloNumExpr right = cplex.sum(cplex.prod(M,z[k][i0][i1]), t[k][i0]);
                right = cplex.sum(right, M);
                constraints.add(cplex.addLe(left, right, "9l_1"));
            }
            k = 2;
            for(int i2: I2){
                IloNumExpr left = cplex.sum(cplex.prod(M,y[k][i0]), i2+1);
                IloNumExpr right = cplex.sum(cplex.prod(M,z[k][i0][i2]), t[k][i0]);
                right = cplex.sum(right, M);
                constraints.add(cplex.addLe(left, right, "9l_2"));
            }
        }
    }

    private void setConstraint9m() throws IloException{
        for(int i0 : I0){
            int k = 1;
            for(int i1 : I1){
                IloNumExpr left = cplex.sum(cplex.prod(M,z[k][i0][i1]), t[k][i0]);
                int right = M+i1;
                constraints.add(cplex.addLe(left, right, "9l_1"));
            }
            k = 2;
            for(int i2: I2){
                IloNumExpr left = cplex.sum(cplex.prod(M,z[k][i0][i2]), t[k][i0]);
                int right = M+i2;
                constraints.add(cplex.addLe(left, right, "9m_1"));
            }
        }
    }

}
