import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.Random;

public class ZhuCplexModel {
    private final IloCplex cplex;
    private final int T;
    private final int T_prime;
    private final int n;
    private final int q;
    private final int[] cPR_i;
    private final int[] cCR_i;
    private final int d;
    private final int lengthOmega;
    private final IloNumVar[] x_i;
    private final IloNumVar[][][][] x_rwit;
    private final IloNumVar[][] z_wt;
    private final IloNumVar[][][][] w_rwit;
    private final IloNumVar[][][][] y_rwit;
    private final IloNumVar[][][][] v_rwit;
    private final IloNumVar[][][][] u_rwit;
    private final IloNumVar[][][] Y_rwi;

    private final int[][][] T_wir;
    private final int[] xi_i;


    public ZhuCplexModel() throws IloException {

        cplex = new IloCplex();
        T = 10;
        n = 4;
        q = 1;
        lengthOmega = 4;
        cPR_i = new int[]{ 0, 1,2,3,4};
        cCR_i = new int[]{ 0, 1,2,3,4};
        d = 3;

        T_wir = setScenarios();
        T_prime = T + getMaxTwir();
        xi_i = setXi();

        x_i = new IloNumVar[n+1];
        x_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T+1];
        z_wt = new IloNumVar[lengthOmega+1][T+1];
        w_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T_prime+1];
        y_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T_prime+1];
        u_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T_prime+1];
        v_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T_prime+1];
        Y_rwi = new IloNumVar[q+1][lengthOmega+1][n+1];


        setVariables();
        setConstraints();
        setObjective();
        cplex.exportModel("model.lp");
//        cplex.setOut(null);
        cplex.solve();
        System.out.println(cplex.getCplexStatus());
        checkResult();
    }

    private void checkResult() throws IloException {
        for(int i = 1; i<= n ; i++){
            for (int w = 1; w <= lengthOmega ; w++) {
                if(cplex.getValue(x_rwit[q][w][i][T])==1){
                    System.out.println("This model is not correct. The value for q is chosen too small, causing less replacement too occur than neccessary");
                }
                else if(cplex.getValue(x_rwit[q][w][i][T])==0){
                    System.out.println("okay here");
                }
            }
        }
    }

    private void setObjective() throws IloException {

        //for this example, we set p(w) to 1.
        IloNumExpr sum_w = cplex.constant(0);

        for(int w=1 ; w<=lengthOmega; w++){

            IloNumExpr sum_n = cplex.constant(0);
            for (int i = 1; i <= n ; i++) {

                IloNumExpr sum_r1 = cplex.constant(0);
                for (int r = 1; r <= q; r++) {
                    sum_r1 = cplex.sum(sum_r1, Y_rwi[r][w][i]);
                }
                IloNumExpr prod_r1 = cplex.prod(cPR_i[n], sum_r1);


                IloNumExpr sum_r2 = cplex.constant(0);
                for (int r = 1; r <= q; r++) {
                    sum_r1 = cplex.sum(sum_r1, cplex.diff(x_rwit[r][w][i][T], Y_rwi[r][w][i]));
                }
                IloNumExpr prod_r2 = cplex.prod(cCR_i[n], sum_r2);

                sum_n = cplex.sum(sum_n, prod_r1, prod_r2);
            }

            IloNumExpr sum_t = cplex.constant(0);
            for (int t = 0; t < T; t++) {
                sum_t = cplex.sum(sum_t, cplex.prod(d, z_wt[w][t]));
            }

            sum_w = cplex.sum(sum_w, cplex.prod(1, cplex.sum(sum_n, sum_t)));
        }

        cplex.addMinimize(sum_w);
    }

    private void setConstraints() throws IloException {
        // Constraint 1b is the definition for x_rwit and ensures that the item is replaced at or before t+1 when it is replaced at or before t. In other words, I makes sure that I_ir can not come to live suddenly and remains replaced.
        for(int i = 1; i<= n ; i++){
            for (int t = 0; t < T ; t++) {
                for (int r = 1; r <= q ; r++) {
                    for (int w = 1; w <= lengthOmega ; w++) {
                        cplex.addLe(x_rwit[r][w][i][t], x_rwit[r][w][i][t+1]);
                    }
                }
            }
        }
        // Constraint 1c implies that individual I_i,r+1 can only be replaced after I_ir is replaced.
        for(int i = 1; i<= n ; i++){
            for (int t = 0; t < T ; t++) {
                for (int r = 1; r < q ; r++) {
                    for (int w = 1; w <= lengthOmega ; w++) {
                        cplex.addLe(x_rwit[r+1][w][i][t+1],x_rwit[r][w][i][t]);
                    }
                }
            }
        }
        // Constraint 1d ensures that maintenance costs d incurs when any component is replaced at time t. Apparently two maintenance periods behind each other has costs d once.
        for(int i = 1; i<= n ; i++){
            for (int t = 1; t <= T ; t++) {
                for (int w = 1; w <= lengthOmega ; w++) {

                    IloNumExpr sum = cplex.constant(0);
                    for (int r = 1; r <= q ; r++) {
                        sum = cplex.sum(sum, cplex.diff(x_rwit[r][w][i][t], x_rwit[r][w][i][t-1]));
                    }
                    cplex.addLe(sum, z_wt[w][t]);

                }
            }
        }
        // Constraint 1e is missing, however we found one from the other article that could be 1e.
        for(int i = 1; i<= n ; i++){
            for (int w = 1; w < lengthOmega; w++) {
                cplex.addLe(x_rwit[1][w][i][0],z_wt[w][0]);
            }
        }
        // Constraint 1f is adjusted so that it takes the same form as the previous article. It ensuress that individual I_ir is replaced before or at the endof its lifetime.
        for(int i = 1; i<= n ; i++){
            for (int r = 1; r < q ; r++) {
                for (int w = 1; w <= lengthOmega ; w++) {
                    for (int t = 0; t <= T-T_wir[w][i][r+1] ; t++) {

                        cplex.addLe(x_rwit[r][w][i][t], x_rwit[r+1][w][i][t+T_wir[w][i][r+1]]);

                    }
                }
            }
        }
        //constraint 1g ensures that the first individual is replaced before its lifetime.
        for (int w = 1; w <= lengthOmega ; w++) {
            for (int j = 1; j <= n ; j++) {
                if(T_wir[w][j][1] <= T){

                    cplex.addEq(x_rwit[1][w][j][T_wir[w][j][1]], 1) ;

                }
            }
        }
        //constraint 1h implies that only the first individual can be replaced at t=0
        for (int i = 1; i <= n ; i++) {
            for (int r = 2; r <= q ; r++) {
                for (int w = 1; w <= lengthOmega; w++) {

                    cplex.addEq(x_rwit[r][w][i][0], 0);

                }
            }
        }
        //constraint 1i
        for (int i = 1; i <= n ; i++) {
            for (int w = 1; w <= lengthOmega; w++) {

                cplex.addEq(x_i[i], x_rwit[1][w][i][0]);

            }
        }
        //constraint 1j
        for (int i = 1; i <= n ; i++) {
            cplex.addGe(x_i[i],xi_i[i]);
        }
        //constraint 1k
        for (int i = 1; i <= n ; i++) {
            for (int w = 1; w <= lengthOmega; w++) {

                IloNumExpr diff = cplex.diff(1,w_rwit[1][w][i][T_wir[w][i][1]]);
                cplex.addEq(Y_rwi[1][w][i], diff);

            }
        }
        //constraint 1l
        for (int i = 1; i <= n ; i++) {
            for (int r = 2; r <=q ; r++) {
                for (int w = 1; w <= lengthOmega ; w++) {

                    IloNumExpr sum1 = cplex.constant(0);
                    for(int t=T_wir[w][i][r]; t<=T+T_wir[w][i][r]; t++){
                        sum1 = cplex.sum(sum1, cplex.sum(u_rwit[r][w][i][t], v_rwit[r][w][i][t]));
                    }

                    IloNumExpr sum2 = cplex.constant(0);
                    for(int t=0; t<=T_wir[w][i][r]-1; t++){
                        sum2 = cplex.sum(sum2, w_rwit[r][w][i][t]);
                    }

                    cplex.addEq(Y_rwi[r][w][i], cplex.prod(0.5,cplex.sum(sum1,sum2)));
                }
            }
        }
        //constraint 1m
        for (int i = 1; i <= n ; i++) {
            for (int r = 2; r <=q ; r++) {
                for (int w = 1; w <= lengthOmega ; w++) {
                    for(int t = T_wir[w][i][r]; t<=T_prime; t=t+getMaxTwir()){
                        cplex.addEq(y_rwit[r][w][i][t], cplex.diff(w_rwit[r][w][i][t],w_rwit[r-1][w][i][t-T_wir[w][i][r]]));
                    }
                }
            }
        }
        //constraint 1n
        for(int i = 1; i<= n ; i++) {
            for (int t = 1; t <= T; t++) {
                for (int w = 1; w <= lengthOmega; w++) {
                    for (int r = 1; r <= q; r++) {
                        cplex.addEq(w_rwit[r][w][i][t], cplex.diff(x_rwit[r][w][i][t], x_rwit[r][w][i][t-1]));
                    }
                }
            }
        }
        //constraint 1o
        for(int i = 1; i<= n ; i++) {
            for (int w = 1; w <= lengthOmega; w++) {
                for (int r = 1; r <= q; r++) {
                  cplex.addEq(w_rwit[r][w][i][0], x_rwit[r][w][i][0]);
                }
            }
        }
        //constraint 1p
        for(int i = 1; i<= n ; i++) {
            for (int w = 1; w <= lengthOmega; w++) {
                for (int r = 1; r <= q; r++) {
                    for (int t = T+1 ; t <= T_prime ; t++) {
                        cplex.addEq(w_rwit[r][w][i][t], 0);
                    }
                }
            }
        }
        //constraint 1r & 1s
        for(int i = 1; i<= n ; i++){
            for (int t = 0; t <= T_prime ; t++) {
                for (int r = 1; r <= q ; r++) {
                    for (int w = 1; w <= lengthOmega ; w++) {
                        cplex.addEq(y_rwit[r][w][i][t], cplex.diff(u_rwit[r][w][i][t], v_rwit[r][w][i][t]));
                        cplex.addLe(cplex.sum(u_rwit[r][w][i][t], v_rwit[r][w][i][t]), 1);
                    }
                }
            }
        }
    }

    private void setVariables() throws IloException {
        for(int i = 1; i<= n; i++){
            x_i[i] = cplex.boolVar("x("+i+")");
            for(int r=1; r<= q; r++){
                for(int w=1; w<= lengthOmega ; w++){

                    Y_rwi[r][w][i] = cplex.boolVar("Y("+r+","+w+","+i+")");

                    for(int t=0; t<= T_prime; t++){
                        if(i==1 && r==1 && t<=T){
                            z_wt[w][t] = cplex.boolVar("z("+w+","+i+")");
                        }
                        if(t<=T){x_rwit[r][w][i][t] = cplex.boolVar("x("+r+","+w+","+i+","+t+")");}
                        w_rwit[r][w][i][t] = cplex.boolVar("w("+r+","+w+","+i+","+t+")");
                        y_rwit[r][w][i][t] = cplex.boolVar("y("+r+","+w+","+i+","+t+")");
                        u_rwit[r][w][i][t] = cplex.boolVar("u("+r+","+w+","+i+","+t+")");
                        v_rwit[r][w][i][t] = cplex.boolVar("v("+r+","+w+","+i+","+t+")");
                    }
                }
            }
        }
    }


    /**
     * This method sets all components on working modus
     * @return boolean saying that all components start in working modus
     */
    private int[] setXi() {
        int[] xi = new int[n+1];
        for(int i=1; i<= n; i++){
            xi[i]=0;
        }
        return xi;
    }


    /**
     * This method manually sets one scenario
     * @return one set of scenarios
     */
    private int[][][] setScenarios() {
        int[][][] scenarios = new int[lengthOmega+1][n+1][q+1];
        Random rand = new Random(1234);
        double hello = rand.nextDouble();
        System.out.println(hello);
        hello = rand.nextDouble();
        System.out.println(hello);

        for (int w = 1; w <= lengthOmega ; w++) {
            for (int i = 1; i <= n ; i++) {

                int sumTime = 0;
                for (int r = 1; r < q ; r++) {
                    double factor = (1.0/q)*2.0;
//                    int Time = (int) Math.round(factor*T*rand.nextDouble())+1;
                    Random rand2 = new Random();
                    hello = rand2.nextDouble();
                    System.out.println(hello);
                    int Time = (int) Math.round(T*hello)+1;
                    sumTime = sumTime + Time;
                    scenarios[w][i][r] = Time;
                }

                if(sumTime<=T) {
                    scenarios[w][i][q] = T - sumTime + 1;
                } else if (sumTime > T) {
                    scenarios[w][i][q] = (int) Math.round(T*rand.nextDouble())+1;
                }
                System.out.println(scenarios[w][i][q]);
            }
        }
        return scenarios;
    }

    private int getMaxTwir() {
        int max = 0;
        for (int w = 1; w <= lengthOmega ; w++) {
            for (int i = 1; i <= n ; i++) {
                for (int r = 1; r <= q ; r++) {
                    if(T_wir[w][i][r]>max){
                        max = T_wir[w][i][r];
                    }
                }
            }
        }
        return max;
    }
}
