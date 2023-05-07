package Zhu;

import Main.Instance;
import ilog.concert.IloException;

import java.util.HashMap;
import java.util.Map;

public class PHA {
    private static int v = 0;
    private final double epsilon = 0.01;
    private Omega omega;
    private Instance instance;
    private double penalty = 0.5;
    private int n;
    private int T;
    private int q;
    private static Map<Integer, int[][][][]> map_v_Xrwit = new HashMap<>();
    private static Map<Integer, double[][][][]> map_v_Wrwit = new HashMap<>();
    private static Map<Integer, double[][][]> map_average_Xrit = new HashMap<>();

    public PHA(Omega omega, Instance instance) throws IloException {
        this.omega = omega;
        this.instance = instance;
        n = instance.n;
        T = instance.T;
        q = instance.q;

        initialize();
    }

    private void initialize() throws IloException {
        //initialize Xrwit
        int[][][][] Xrwit = new int[q][omega.lenghtOmega][n][T+1];

        for (int w = 0; w < omega.lenghtOmega; w++) {
            ZhuCplexModel model = new ZhuCplexModel(instance, omega.getTir(w));
            int[][][] x_rit = model.get_x_rit();
            model.cleanup();

            for (int i = 0; i < n ; i++) {
                for (int r = 0; r < q; r++) {
                    for (int t = 0; t <= T; t++) {
                        Xrwit[r][w][i][t] = x_rit[r][i][t];
                    }
                }
            }
        }
        map_v_Xrwit.put(0,Xrwit);

        //initialize x average
        double[][][] average_Xrit = new double[q][n][T+1];
        for (int i = 0; i < n; i++) {
            for (int r = 0; r < q; r++) {
                for (int t = 0; t <= T; t++) {

                    double sum = 0.0;
                    for (int w = 0; w < omega.lenghtOmega; w++) {
                        sum = sum + omega.getpw(w)*Xrwit[r][w][i][t];
                    }
                    System.out.println("Is this still a double?"+sum);
                    average_Xrit[r][i][t] = sum;

                }
            }
        }
        map_average_Xrit.put(0,average_Xrit);


        //initialize w
        double[][][][] Wrwit = new double[q][omega.lenghtOmega][n][T+1];
        for (int i = 0; i < n; i++) {
            for (int r = 0; r < q; r++) {
                for (int t = 0; t <= T; t++) {
                    for (int w = 0; w < omega.lenghtOmega; w++) {
                        Wrwit[r][w][i][t] = penalty * ( Xrwit[r][w][i][t] - average_Xrit[r][i][t]);
                    }
                }
            }
        }
        map_v_Wrwit.put(0,Wrwit);
    }
}
