package Zhu;

import Main.Instance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class DEF_period extends DEF {
    public final IloNumVar[][][][] Y_rwit_CR;
    public final IloNumVar[][][][] Y_rwit_PR;

    public DEF_period(Instance instance, Omega Omega, String fileName) throws IloException {
        super(instance, Omega, fileName);
        Y_rwit_CR = new IloNumVar[q][lengthOmega][n][T+1];
        Y_rwit_PR = new IloNumVar[q][lengthOmega][n][T+1];
    }

    /**
     * So we can add the new variables and new constraints.
     * @throws IloException
     */
    @Override
    public void setupAndSolve() throws IloException, IOException {
        setVariables();
        setExtraVariables();
        setConstraints();
        setExtraConstraints();
        setObjective();
        cplex.exportModel("def_period.lp");
        cplex.solve();
        cplex.setOut(null);
        checkResult();
        printResultMatrix();
        System.out.println("best costs are "+cplex.getObjValue());
        System.out.println("x1 = "+cplex.getValue(x_i[0])+"\t\t x2 = "+cplex.getValue(x_i[1]));

        FileWriter w = new FileWriter(fileName, true);
        w.write("\nstartTime ="+i.startTime+"\tstartAges="+ Arrays.toString(i.startAges)+"\t kesi="+Arrays.toString(kesi)+"\n");
        w.write("x1 = "+cplex.getValue(x_i[0])+"\t\t x2 = "+cplex.getValue(x_i[1])+"\n");
        w.close();
    }

    /**
     * Add new auxiliary variables.
     * @throws IloException
     */
    private void setExtraVariables() throws IloException {
        for (int r = 0; r < q; r++) {
            for (int i = 0; i < n; i++) {
                for (int w = 0; w < lengthOmega; w++) {
                    for (int t = 0; t < T+1; t++) {
                        Y_rwit_CR[r][w][i][t] = cplex.boolVar("Y_CR(" + r + "," + w + "," + i + "," + t + ")");
                        Y_rwit_PR[r][w][i][t] = cplex.boolVar("Y_CR(" + r + "," + w + "," + i + "," + t + ")");
                    }
                }
            }
        }
    }






    /**
     * Add new auxiliary constraints.
     * @throws IloException
     */
    private void setExtraConstraints() throws IloException {
        for (int r = 0; r < q; r++) {
            for (int i = 0; i < n; i++) {
                for (int w = 0; w < lengthOmega; w++) {
                    for (int t = 0; t < T + 1; t++) {
                        //constraint(1)
                        IloNumExpr lhs1 = cplex.sum(Y_rwit_CR[r][w][i][t], Y_rwit_PR[r][w][i][t]);
                        cplex.addEq(lhs1, w_rwit[r][w][i][t]);

                        //constraint(2)
                        IloNumExpr rhs2 = cplex.diff(1.0, Y_rwi[r][w][i]);
                        cplex.addLe(Y_rwit_CR[r][w][i][t], rhs2);

                        //constraint (3)
                        cplex.addLe(Y_rwit_PR[r][w][i][t], Y_rwi[r][w][i]);
                    }
                }
            }
        }
    }

    /**
     * Removed correction, and objective now only focused on new auxiliary variables.
     * @throws IloException
     */
    @Override
    public void setObjective() throws IloException {
        IloNumExpr obj = cplex.constant(0);
        for(int w = 0 ; w < lengthOmega; w++){

            IloNumExpr sum_w = cplex.constant(0);
            for (int t = 0; t < T + 1 ; t++) {
                IloNumExpr sum_ir = cplex.constant(0);
                for (int i = 0; i < n; i++) {
                    for (int r = 0; r < q; r++) {
//                        sum_ir = cplex.sum(sum_ir, cplex.sum(cplex.prod(cPR_i_t[i][t], Y_rwit_PR[r][w][i][t]), cplex.prod(cCR_i_t[i][t], Y_rwit_CR[r][w][i][t])));
                        IloNumExpr prod1 = cplex.prod(cPR_i_t[i][(t + startTime) % N], Y_rwit_PR[r][w][i][t]);
                        IloNumExpr prod2 = cplex.prod(cCR_i_t[i][(t + startTime) % N], Y_rwit_CR[r][w][i][t]);
                        IloNumExpr sum = cplex.sum(prod1, prod2);
                        sum_ir = cplex.sum(sum_ir, sum);
                    }
                }

                IloNumExpr dz = cplex.prod(d, z_wt[w][t]);

                sum_w = cplex.sum(sum_w, sum_ir, dz);
            }

            obj = cplex.sum(obj, cplex.prod(p_w[w], sum_w));
        }

        cplex.addMinimize(obj, "obj");
    }
}