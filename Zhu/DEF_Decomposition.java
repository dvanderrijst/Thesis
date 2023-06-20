package Zhu;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;

import java.io.IOException;

public class DEF_Decomposition extends DEF {
    private final double[][][] average_Xrit;
    private final double[][][] Writ_prev;
    private final int w = 0;
    private final double penalty;

    public DEF_Decomposition(Instance i, Omega Omega, double[][][] average_Xrit, double[][][] Writ_prev, double penalty, String fileName) throws IloException, IOException {
        super(i, Omega, fileName);
        this.average_Xrit = average_Xrit;
        this.Writ_prev = Writ_prev;
        this.penalty = penalty;
        setupAndSolve();
    }

    @Override
    public void setObjective()throws IloException, IOException {

        //sum over N, individual costs
        IloNumExpr sum_n = cplex.constant(0);
        for (int i = 0; i < n ; i++) {

            IloNumExpr sum_r1 = cplex.constant(0);
            for (int r = 0; r < q; r++) {
                sum_r1 = cplex.sum(sum_r1, Y_rwi[r][w][i]);
            }
            IloNumExpr prod_r1 = cplex.prod(cPR_i_t[n][0], sum_r1);


            IloNumExpr sum_r2 = cplex.constant(0);
            for (int r = 0; r < q; r++) {
                sum_r1 = cplex.sum(sum_r1, cplex.diff(x_rwit[r][w][i][T], Y_rwi[r][w][i]));
            }
            IloNumExpr prod_r2 = cplex.prod(cCR_i_t[n][0], sum_r2);

            sum_n = cplex.sum(sum_n, prod_r1, prod_r2);
        }

        //sum over T, shared costs
        IloNumExpr sum_t = cplex.constant(0);
        for (int t = 0; t <= T; t++) {
            sum_t = cplex.sum(sum_t, cplex.prod(d, z_wt[w][t]));
        }

        //multiplying  x by w
        IloNumExpr sum_w = cplex.constant(0);
        for (int r = 0; r < q; r++) {
            for (int i = 0; i < n; i++) {
                for (int t = 0; t <= T; t++) {
                    sum_w = cplex.sum(sum_w, cplex.prod(Writ_prev[r][i][t], x_rwit[r][w][i][t]));
                }
            }
        }

        //eucledian norm and penalty multiplication
        IloNumExpr sumUnderSR = cplex.constant(0);
        for (int r = 0; r < q; r++) {
            for (int i = 0; i < n; i++) {
                for (int t = 0; t <= T; t++) {
                    sumUnderSR = cplex.sum(sumUnderSR, cplex.square(cplex.diff(x_rwit[r][0][i][t], average_Xrit[r][i][t])));
                }
            }
        }
        IloNumExpr sumEucleadian = cplex.prod(0.5*penalty, sumUnderSR);
        IloNumExpr sumTot = cplex.sum(sum_n, cplex.sum(sum_t, cplex.sum(sum_w, sumEucleadian)));

        cplex.addMinimize(sumTot, "obj");
    }

}
