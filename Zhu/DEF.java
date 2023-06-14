package Zhu;

import Main.Instance;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.Random;

public class DEF {
    public final IloCplex cplex;
    public final int T;
    public final int T_prime;
    public final int n;
    public final int q;
    public final double[][] cPR_i_t;
    public final double[][] cCR_i_t;
    public final int d;
    public final int lengthOmega;
    public final IloNumVar[] x_i;
    public final IloNumVar[][][][] x_rwit;
    public final IloNumVar[][] z_wt;
    public final IloNumVar[][][][] w_rwit;
    public final IloNumVar[][][][] y_rwit;
    public final IloNumVar[][][][] v_rwit;
    public final IloNumVar[][][][] u_rwit;
    public final IloNumVar[][][] Y_rwi;
    public final int[][][] T_wir;
    public final int[] kesi;
    public final double[] p_w;
    public final Instance i;
    public final String fileName;

    public DEF(Instance instance, Omega Omega, String fileName) throws IloException {
        this.fileName = fileName;
        cplex = new IloCplex();
//        cplex.setOut(null);
//        cplex.setWarning(null);

        this.i = instance;
        T = instance.T;
        n = instance.n;
        q = instance.q;
        lengthOmega = instance.lengthOmega;
        cPR_i_t = instance.cPR_i_t;
        cCR_i_t = instance.cCR_i_t;

        d = instance.d;

        kesi = instance.kesi;

        p_w = Omega.p_w;
        T_wir = Omega.Twir;

        T_prime = T + getMaxTwir();

        x_i = new IloNumVar[n];
        x_rwit = new IloNumVar[q][lengthOmega][n][T+1];
        z_wt = new IloNumVar[lengthOmega][T+1];
        w_rwit = new IloNumVar[q][lengthOmega][n][T_prime+1];
        y_rwit = new IloNumVar[q][lengthOmega][n][T_prime+1];
        u_rwit = new IloNumVar[q][lengthOmega][n][T_prime+1];
        v_rwit = new IloNumVar[q][lengthOmega][n][T_prime+1];
        Y_rwi = new IloNumVar[q][lengthOmega][n];
    }

    public void setupAndSolve() throws IloException {
        setVariables();
        setConstraints();
        setObjective();
        cplex.exportModel(fileName);
        cplex.solve();
        cplex.setOut(null);
//        System.out.println(cplex.getCplexStatus());
        checkResult();
        printResultMatrix();
        System.out.println("best costs are "+cplex.getObjValue());
        System.out.println("x1 = "+cplex.getValue(x_i[0])+"\t\t x2 = "+cplex.getValue(x_i[1]));
    }

    private void printResultMatrix() throws IloException {
        for (int omega = 0; omega < 10; omega++) {
            System.out.print("\n\n\n For omega = "+omega+" we find the following matrixes.");
            for (int i = 0; i < n; i++) {
                System.out.println("\nni = "+i+" and we have T[w][i][0] = "+T_wir[omega][i][0]+" and T[w][i][1]="+T_wir[omega][i][1]);


                for (int r = 0; r < q; r++) {
                    System.out.printf("%8s", "\nx_it r=" + r);
                    for (int t = 0; t <= T; t++) {
                        System.out.printf("%8.0f", cplex.getValue(x_rwit[r][omega][i][t]));
                    }
                }
                System.out.printf("%8s", "\nz wt    ");
                for (int t = 0; t <= T; t++) {
                    System.out.printf("%8.0f", cplex.getValue(z_wt[omega][t]));
                }
                for (int r = 0; r < q; r++) {
                    System.out.printf("%8s", "\nw_it r=" + r);
                    for (int t = 0; t <= T_prime; t++) {
                        System.out.printf("%8.0f", cplex.getValue(w_rwit[r][omega][i][t]));
                    }
                }
                for (int r = 1; r < q; r++) {
                    System.out.printf("%8s", "\ny_it r=" + r);
                    for (int t = 0; t <= T_prime; t++) {
                        System.out.printf("%8.0f", cplex.getValue(y_rwit[r][omega][i][t]));
                    }
                }
                for (int r = 0; r < q; r++) {
                    System.out.printf("%8s", "\nY iw r=" + r);
                    System.out.printf("%8.1f", cplex.getValue(Y_rwi[r][omega][i]));
                }
                System.out.println();
            }
        }
    }

    /**
     * In this method, we put some notes that we could forget later in. To make sure that we do not forget.
     * @throws IloException
     */
    private void checkResult() throws IloException {
        for(int i = 0; i< n ; i++){
            for (int w = 0; w < lengthOmega ; w++) {
                if(cplex.getValue(x_rwit[q-1][w][i][T])==1){
                    System.out.println("This model is not correct. The value for q is chosen too small, causing less replacement too occur than neccessary");
               }
            }
        }
    }

    /**
     * Sets the objective for our CPLEX model.
     * @throws IloException
     */
    public void setObjective() throws IloException {

        IloNumExpr sum_w = cplex.constant(0);

        for(int w=0 ; w<lengthOmega; w++){

            IloNumExpr sum_n = cplex.constant(0);
            for (int i = 0; i < n ; i++) {

                IloNumExpr sum_r1 = cplex.constant(0);
                for (int r = 0; r < q; r++) {
                    sum_r1 = cplex.sum(sum_r1, Y_rwi[r][w][i]);
                }
                IloNumExpr prod_r1 = cplex.prod(cPR_i_t[i][0], sum_r1);

                IloNumExpr sum_r2 = cplex.constant(0);
                for (int r = 0; r < q; r++) {
                    sum_r2 = cplex.sum(sum_r2, cplex.diff(x_rwit[r][w][i][T], Y_rwi[r][w][i]));
                }
                IloNumExpr prod_r2 = cplex.prod(cCR_i_t[i][0], sum_r2);

                IloNumExpr sumCorrection = cplex.constant(0);
                for (int r = 1; r < q; r++) {
                    sumCorrection = cplex.sum(sumCorrection, cplex.diff(x_rwit[r-1][w][i][T], x_rwit[r][w][i][T]));
                }
                IloNumExpr prodCorrection = cplex.prod( -0.5 * (cPR_i_t[i][0] - cCR_i_t[i][0]), sumCorrection);

                sum_n = cplex.sum(sum_n, prod_r1, prod_r2, prodCorrection);
            }

            IloNumExpr sum_t = cplex.constant(0);
            for (int t = 0; t <= T; t++) {
                sum_t = cplex.sum(sum_t, cplex.prod(d, z_wt[w][t]));
            }

            sum_w = cplex.sum(sum_w, cplex.prod(p_w[w], cplex.sum(sum_n, sum_t)));
        }

        cplex.addMinimize(sum_w, "obj");
    }

    /**
     * Sets all the constraints found in the article in our CPLEX model.
     * @throws IloException
     */
    private void setConstraints() throws IloException{
        // Constraint 1b is the definition for x_rwit and ensures that the item is replaced at or before t+1 when it is replaced at or before t. In other words, I makes sure that I_ir can not come to live suddenly and remains replaced.
//        System.out.print("adding constraints 1b");
        for(int i = 0; i< n ; i++){
            for (int t = 0; t < T ; t++) {
                for (int r = 0; r < q ; r++) {
                    for (int w = 0; w < lengthOmega ; w++) {
                        cplex.addLe(x_rwit[r][w][i][t], x_rwit[r][w][i][t+1],"b_"+r+w+i+t);
                    }
                }
            }
        }
        // Constraint 1c implies that individual I_i,r+1 can only be replaced after I_ir is replaced.
//        System.out.print("adding constraints 1c");
        for(int i = 0; i< n ; i++){
            for (int t = 0; t < T ; t++) {
                for (int r = 0; r < q-1 ; r++) {
                    for (int w = 0; w < lengthOmega ; w++) {
                        cplex.addLe(x_rwit[r+1][w][i][t+1],x_rwit[r][w][i][t],"c_"+r+w+i+t);
                    }
                }
            }
        }
        // Constraint 1d ensures that maintenance costs d incurs when any component is replaced at time t. Apparently two maintenance periods behind each other has costs d once.
//        System.out.print("adding constraints 1d");
        for(int i = 0; i< n ; i++){
            for (int t = 1; t <= T ; t++) {
                for (int w = 0; w < lengthOmega ; w++) {

                    IloNumExpr sum = cplex.constant(0);
                    for (int r = 0; r < q ; r++) {
                        sum = cplex.sum(sum, cplex.diff(x_rwit[r][w][i][t], x_rwit[r][w][i][t-1]));
                    }
                    cplex.addLe(sum, z_wt[w][t],"d_"+w+t);

                }
            }
        }
//        System.out.print("adding constraints 1e");
        // Constraint 1e is missing, however we found one from the other article that could be 1e. This is equation (12).
        for(int i = 0; i< n ; i++){
            for (int w = 0; w < lengthOmega; w++) {
                cplex.addLe(x_rwit[0][w][i][0],z_wt[w][0],"a_e_0"+w+i+"0");
            }
        }
//        System.out.print("adding constraints 1f");
        // Constraint 1f is adjusted so that it takes the same form as the previous article. It ensuress that individual I_ir is replaced before or at the endof its lifetime.
        for(int i = 0; i< n ; i++){
            for (int r = 0; r < q-1 ; r++) {
                for (int w = 0; w < lengthOmega ; w++) {
                    for (int t = 0; t <= T-T_wir[w][i][r+1] ; t++) {

                        cplex.addLe(x_rwit[r][w][i][t], x_rwit[r+1][w][i][t+T_wir[w][i][r+1]],"f_"+r+w+i+t);

                    }
                }
            }
        }
//        System.out.print("adding constraints 1g");
        //constraint 1g ensures that the first individual is replaced before its lifetime.
        for (int w = 0; w < lengthOmega ; w++) {
            for (int i = 0; i < n ; i++) {
                if(T_wir[w][i][0] <= T){
                    cplex.addEq(x_rwit[0][w][i][T_wir[w][i][0]], 1,"g_0"+w+i) ;
                }
            }
        }
//        System.out.print("adding constraints 1h");
        //constraint 1h implies that only the first individual can be replaced at t=0
        for (int i = 0; i < n ; i++) {
            for (int r = 1; r < q ; r++) {
                for (int w = 0; w < lengthOmega; w++) {
                    cplex.addEq(x_rwit[r][w][i][0], 0,"h_"+r+w+i+"0");
                }
            }
        }
//        System.out.print("adding constraints 1i");
        //constraint 1i
        for (int i = 0; i < n ; i++) {
            for (int w = 0; w < lengthOmega; w++) {
                cplex.addEq(x_i[i], x_rwit[0][w][i][0],"i_"+w+i);
            }
        }
//        System.out.print("adding constraints 1j");
        //constraint 1j
        for (int i = 0; i < n ; i++) {
            cplex.addGe(x_i[i], kesi[i],"j_"+i);
        }
//        System.out.print("adding constraints 1k");
        //constraint 1k
        for (int i = 0; i < n ; i++) {
            for (int w = 0; w < lengthOmega; w++) {
                IloNumExpr diff = cplex.diff(1,w_rwit[0][w][i][T_wir[w][i][0]]);
                cplex.addLe(Y_rwi[0][w][i], diff,"k_0"+w+i);
            }
        }
        //added this constraint myself.. this is neccessary in case the first component is never replaced
        for (int i = 0; i < n ; i++) {
            for (int w = 0; w < lengthOmega; w++) {
                IloNumExpr sumW = cplex.constant(0);
                for (int t = 0; t < T; t++) {
                    sumW = cplex.sum(sumW, w_rwit[0][w][i][t]);
                }
                cplex.addLe(Y_rwi[0][w][i], sumW,"k_0sum"+w+i);
            }
        }



//        System.out.print("adding constraints 1l");
        //constraint 1l
        for (int i = 0; i < n ; i++) {
            for (int r = 1; r <q ; r++) {
                for (int w = 0; w < lengthOmega ; w++) {

                    IloNumExpr sum1 = cplex.constant(0);
                    for(int t=T_wir[w][i][r]; t<=T+T_wir[w][i][r]; t++){
                        sum1 = cplex.sum(sum1, cplex.sum(u_rwit[r][w][i][t], v_rwit[r][w][i][t]));
                    }

                    IloNumExpr sum2 = cplex.constant(0);
                    for(int t=0; t<= (T_wir[w][i][r]-1); t++){
                        sum2 = cplex.sum(sum2, w_rwit[r][w][i][t]);
                    }

                    cplex.addEq(Y_rwi[r][w][i], cplex.prod(0.5 , cplex.sum(sum1,sum2)),"l_"+r+w+i);
                }
            }
        }

//        System.out.print("adding constraints 1m");
        //constraint 1m
        for (int i = 0; i < n ; i++) {
            for (int w = 0; w < lengthOmega ; w++) {
                for (int r = 1; r < q ; r++) {
                    for (int t = T_wir[w][i][r] ; t <= T_prime; t++) {
                        cplex.addEq(y_rwit[r][w][i][t], cplex.diff(w_rwit[r][w][i][t],w_rwit[r-1][w][i][t-T_wir[w][i][r]]),"m_"+r+w+i+t);
                    }
                }
            }
        }



//        System.out.print("adding constraints 1n");
        //constraint 1n
        for(int i = 0; i< n ; i++) {
            for (int t = 1; t <= T; t++) {
                for (int w = 0; w < lengthOmega; w++) {
                    for (int r = 0; r < q; r++) {
                        cplex.addEq(w_rwit[r][w][i][t], cplex.diff(x_rwit[r][w][i][t], x_rwit[r][w][i][t-1]),"n_"+r+w+i+t);
                    }
                }
            }
        }
//        System.out.print("adding constraints 1o");
        //constraint 1o
        for(int i = 0; i< n ; i++) {
            for (int w = 0; w < lengthOmega; w++) {
                for (int r = 0; r < q; r++) {
                  cplex.addEq(w_rwit[r][w][i][0], x_rwit[r][w][i][0],"o_"+r+w+i+"0");
                }
            }
        }
//        System.out.print("adding constraints 1p");
        //constraint 1p
        for(int i = 0; i< n ; i++) {
            for (int w = 0; w < lengthOmega; w++) {
                for (int r = 0; r < q; r++) {
                    for (int t = T+1 ; t <= T_prime ; t++) {
                        cplex.addEq(w_rwit[r][w][i][t], 0,"p_"+r+w+i+t);
                    }
                }
            }
        }
//        System.out.print("adding constraints 1r 1s");
        //constraint 1r & 1s
        for(int i = 0; i< n ; i++){
            for (int t = 0; t <= T_prime ; t++) {
                for (int r = 0; r < q ; r++) {
                    for (int w = 0; w < lengthOmega ; w++) {
                        cplex.addEq(y_rwit[r][w][i][t], cplex.diff(u_rwit[r][w][i][t], v_rwit[r][w][i][t]),"r_"+r+w+i+t);
                        cplex.addLe(cplex.sum(u_rwit[r][w][i][t], v_rwit[r][w][i][t]), 1,"s_"+r+w+i+t);
                    }
                }
            }
        }
    }

    private void setVariables() throws IloException, OutOfMemoryError {
        int count = 0;

        try {
            for (int i = 0; i < n; i++) {
                x_i[i] = cplex.boolVar("x(" + i + ")");
                count++;
                for (int r = 0; r < q; r++) {
                    for (int w = 0; w < lengthOmega; w++) {

                        Y_rwi[r][w][i] = cplex.numVar(0.0, 1.0,"YY(" + r + "," + w + "," + i + ")");
                        count++;

                        for (int t = 0; t <= T_prime; t++) {
                            if (i == 0 && r == 0 && t <= T) {
                                z_wt[w][t] = cplex.boolVar("z(" + w + "," + t + ")");
                                count++;
                            }
                            if (t <= T) {
                                x_rwit[r][w][i][t] = cplex.boolVar("x(" + r + "," + w + "," + i + "," + t + ")");
                                count++;
                            }
                            w_rwit[r][w][i][t] = cplex.boolVar("w(" + r + "," + w + "," + i + "," + t + ")");
                            y_rwit[r][w][i][t] = cplex.intVar(-1,1,"y(" + r + "," + w + "," + i + "," + t + ")");
                            u_rwit[r][w][i][t] = cplex.boolVar("u(" + r + "," + w + "," + i + "," + t + ")");
                            v_rwit[r][w][i][t] = cplex.boolVar("v(" + r + "," + w + "," + i + "," + t + ")");
                            count = count + 4;
                        }
                    }
                }
                System.out.println(count);
            }
        }
        catch(OutOfMemoryError e){
            System.out.println(count);
        }
    }

    /**
     * This method sets the scenarios. The life times are uniformly distributed over [0, (2T)/q ]. The fraction is then rounded
     * to its nearest higher integer. This is just for keeping the algorithm running. Life times are actually exponential distributed.
     * @return one set of scenarios
     */
    private int[][][] setScenarios() {
        int[][][] scenarios = new int[lengthOmega][n][q];
        Random rand = new Random(1234);

        for (int w = 0; w < lengthOmega ; w++) {
            for (int i = 0; i < n ; i++) {

                int sumTime = 0;
                for (int r = 0; r < q-1 ; r++) {
                    double factor = (1.0/q)*2.0;
                    int Time = (int) Math.ceil(factor*T*rand.nextDouble());
                    sumTime = sumTime + Time;
                    scenarios[w][i][r] = Time;
                }

                if(sumTime<=T) {
                    scenarios[w][i][q-1] = T - sumTime + 1;
                } else {
                    scenarios[w][i][q-1] = (int) Math.ceil(T*rand.nextDouble());
                }
            }
        }
        return scenarios;
    }

    /**
     * The value for Tprime needs the higstest possible lifetime T^w_ir. This value is determined here by searching though the Twir values
     * @return max Twir
     */
    private int getMaxTwir() {
        int max = 0;
        for (int w = 0; w < lengthOmega ; w++) {
            for (int i = 0; i < n ; i++) {
                for (int r = 0; r < q ; r++) {
                    if(T_wir[w][i][r]>max){
                        max = T_wir[w][i][r];
                    }
                }
            }
        }
        return max;
    }
    public int[][][] get_x_rit() throws IloException {
        int[][][] x_rit = new int[q][n][T+1];
        for (int r = 0; r < q; r++) {
            for (int i = 0; i < n; i++) {
                for (int t = 0; t <= T ; t++) {
                    x_rit[r][i][t] = (int) cplex.getValue(x_rwit[r][0][i][t]);
                }
            }
        }
        return x_rit;
    }
    public int[] get_x_i() throws IloException{
        int[] xvalues = new int[i.n];
        for (int j = 0; j < n; j++) {
            xvalues[j] = (int) Math.round(cplex.getValue(x_i[j]));
        }
        return xvalues;
    }
    public double getObjValue() throws IloException{
        return cplex.getObjValue();
    }

    /**
     * Cleans up the CPLEX model in order to free up some memory.
     * This is important if you create many models, as memory used
     * by CPLEX is not freed up automatically by the JVM.
     * @throws IloException if something goes wrong with CPLEX
     */
    public void cleanup() throws IloException
    {
        cplex.end();
    }

    public int[] returnNewStartAges() throws IloException {
        int[] newAges = new int[i.n];
        for (int ii = 0; ii < n; ii++) {
            if(cplex.getValue(x_i[ii])==0){
                newAges[ii] = i.startAges[ii] + 1;
            }
            else {newAges[ii] = 0;}
        }
        return newAges;
    }
}
