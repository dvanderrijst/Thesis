import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;


public class ZhuCplexModel {
    private final IloCplex cplex;
    private final int T;
    private final int T_prime;
    private final int n;
    private final int q;
    private final int lengthOmega = 1;

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
        q = 3;

        T_wir = setScenarios();
        T_prime = T + getMaxTwir();
        xi_i = setXi();

        x_i = new IloNumVar[n+1];
        x_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T_prime+1];
        z_wt = new IloNumVar[lengthOmega+1][T_prime+1];
        w_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T_prime+1];
        y_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T_prime+1];
        u_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T_prime+1];
        v_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T_prime+1];
        Y_rwi = new IloNumVar[q+1][lengthOmega+1][n+1];


        setVariables();
        setConstraints();
        cplex.solve();
    }

    private void setConstraints() throws IloException {
        //constraint 1b
        for(int i = 1; i<= n ; i++){
            for (int t = 0; t < T ; t++) {
                for (int r = 1; r <= q ; r++) {
                    for (int w = 1; w <= lengthOmega ; w++) {

                        cplex.addLe(x_rwit[r][w][i][t], x_rwit[r][w][i][t+1]);

                    }
                }
            }
        }
        //constraint 1c
        for(int i = 1; i<= n ; i++){
            for (int t = 0; t < T ; t++) {
                for (int r = 1; r < q ; r++) {
                    for (int w = 1; w <= lengthOmega ; w++) {

                        cplex.addLe(x_rwit[r+1][w][i][t+1],x_rwit[r][w][i][t]);

                    }
                }
            }
        }
        //constraint 1d
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
        //constraint 1f
        for(int i = 1; i<= n ; i++){
            for (int r = 1; r < q ; r++) {
                for (int w = 1; w <= lengthOmega ; w++) {
                    for (int t = 0; t < T-T_wir[w][i][r] ; t++) {

                        cplex.addLe(x_rwit[r][w][i][t], x_rwit[r+1][w][i][T_wir[w][i][r+1]]);

                    }
                }
            }
        }
        //constraint 1g
        for (int w = 1; w <= lengthOmega ; w++) {
            for (int j = 1; j <= n ; j++) {
                if(T_wir[w][j][1] <= T){

                    cplex.addEq(x_rwit[1][w][j][T_wir[w][j][1]], 1) ;

                }
            }
        }
        //constraint 1h
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
            x_i[i] = cplex.boolVar("x_i");
            for(int r=1; r<= q; r++){
                for(int w=1; w<= lengthOmega ; w++){

                    Y_rwi[r][w][i] = cplex.boolVar("Y_rwi");

                    for(int t=0; t<= T_prime; t++){
                        if(i==1 && r==1){
                            z_wt[w][t] = cplex.boolVar("z_wt");
                        }
                        x_rwit[r][w][i][t] = cplex.boolVar("x_rwit");
                        w_rwit[r][w][i][t] = cplex.boolVar("w_rwit");
                        y_rwit[r][w][i][t] = cplex.boolVar("y_rwit");
                        u_rwit[r][w][i][t] = cplex.boolVar("u_rwit");
                        v_rwit[r][w][i][t] = cplex.boolVar("v_rwit");
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

        //for now, we will only have one scenario so I'll do this manuel:
        scenarios[1][1][1] = 3;
        scenarios[1][1][2] = 3;
        scenarios[1][1][3] = 3;
        scenarios[1][2][1] = 4;
        scenarios[1][2][2] = 4;
        scenarios[1][2][3] = 4;
        scenarios[1][3][1] = 5;
        scenarios[1][3][2] = 5;
        scenarios[1][3][3] = 5;
        scenarios[1][4][1] = 6;
        scenarios[1][4][2] = 6;
        scenarios[1][4][3] = 6;

        return scenarios;
    }

    private int getMaxTwir() {
        return 6;
    }
}
