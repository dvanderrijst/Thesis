import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class ZhuCplexModel {
    private IloCplex cplex;
    private int T;
    private int n;
    private int q;
    private int lengthOmega = 1;

    private IloNumVar[] x_i;
    private IloNumVar z;
    private IloNumVar[][][][] x_rwit;
    private IloNumVar[][] z_wt;
    private IloNumVar[][][][] w_rwit;
    private IloNumVar[][][][] Y_rwit;

    private int[][][] T_wir;
    private boolean[] xi_i;


    public ZhuCplexModel() throws IloException {
        cplex = new IloCplex();
        T = 10;
        n = 4;
        q = 3;


        x_i = new IloNumVar[n+1];
        x_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T+1];
        z_wt = new IloNumVar[lengthOmega+1][T+1];
        w_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T+1];
        Y_rwit = new IloNumVar[q+1][lengthOmega+1][n+1][T+1];

        T_wir = setScenarios();
        xi_i = setXi();

        setModel();
    }

    private void setModel() {

    }


    /**
     * This method sets all components on working modus
     * @return boolean saying that all components start in working modus
     */
    private boolean[] setXi() {
        boolean[] xi = new boolean[n+1];
        for(int i=1; i<= n; i++){
            xi[i]=false;
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
}
