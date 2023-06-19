package Zhu;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Algorithm3 {
    private static int v = 0;
    private final double epsilon = 0.01;
    private Omega Omega;
    private Instance instance;
    private double penalty = 0.5;
    private int n;
    private int T;
    private int q;
    private static Map<Integer, int[][][][]> map_v_Xrwit = new HashMap<>();
    private static Map<Integer, double[][][][]> map_v_Wrwit = new HashMap<>();
    private static Map<Integer, double[][][]> map_average_Xrit = new HashMap<>();
    private static Map<Integer, Double> map_gdistances = new HashMap<>();

    public Algorithm3(Omega omega, Instance instance) throws IloException, IOException {
        this.Omega = omega;
        this.instance = instance;
        n = instance.n;
        T = instance.T;
        q = instance.q;

//        for (int i = 0; i < Omega.getProbabilitiesScenarios().length; i++) {
//            System.out.println("i="+i+"\t "+Omega.getProbabilityScenario(i)[0]);
//        }

        initialize();

        int count = 0;
        while(count<5 && ( map_gdistances.isEmpty() ||  map_gdistances.get(v) > epsilon) ){
            //printX
            for (int w = 0; w < instance.lengthOmega; w++) {
//                printint3(getXrit(w,map_v_Xrwit.get(v)));
            }

            //printW
            for (int w = 0; w < instance.lengthOmega; w++) {
//                printdouble3(getWrit(w,map_v_Wrwit.get(v)), "W");
            }

            //printXaverage
            System.out.println(
                    " \n\n\n\n\nX AVERAGE"
            );
            printdouble3(map_average_Xrit.get(v), "X_average");


            updateV();
            decomposition();
            aggregation();
            updatePrice();
            calculateDistance();
            System.out.println(map_gdistances.get(v));
            count++;
        }

        for (int i = 1; i <= v; i++) {
            System.out.println("g=\t"+map_gdistances.get(i));
        }
    }

    private void calculateDistance() {
        double sumW = 0.0;
        double[][][] average_Xrit = map_average_Xrit.get(v);
        int[][][][] x_rwit = map_v_Xrwit.get(v);

        for (int w = 0; w < instance.lengthOmega ; w++) {
            double distance = 0.0;
            for (int r = 0; r < q; r++) {
                for (int i = 0; i < n; i++) {
                    for (int t = 0; t <= T; t++) {
                        distance = distance + Math.pow( x_rwit[r][w][i][t] - average_Xrit[r][i][t] , 2.0) ;
                    }
                }
            }
//            System.out.println(distance);
            sumW = sumW + Omega.p_w[w]*distance;
        }
        map_gdistances.put(v,sumW);
    }

    private void updatePrice() {
        double[][][][] Wrwit_v = new double[q][instance.lengthOmega][n][T+1];
        double[][][][] Wrwit_vprev = map_v_Wrwit.get(v-1);
        double[][][] average_Xrit = map_average_Xrit.get(v);
        int[][][][] Xrwit = map_v_Xrwit.get(v);


        for (int i = 0; i < n; i++) {
            for (int r = 0; r < q; r++) {
                for (int t = 0; t <= T; t++) {
                    for (int w = 0; w < instance.lengthOmega; w++) {
                        Wrwit_v[r][w][i][t] = Wrwit_vprev[r][w][i][t] + penalty * ( Xrwit[r][w][i][t] - average_Xrit[r][i][t]);
//                        Wrwit_v[r][w][i][t] = penalty * ( Xrwit[r][w][i][t] - average_Xrit[r][i][t]);
                    }
                }
            }
        }
        map_v_Wrwit.put(v,Wrwit_v);
    }

    private void aggregation() {
        //initialize x average
        double[][][] average_Xrit = new double[q][n][T+1];
        int[][][][] Xrwit = map_v_Xrwit.get(v);
        for (int i = 0; i < n; i++) {
            for (int r = 0; r < q; r++) {
                for (int t = 0; t <= T; t++) {

                    double sum = 0.0;
                    for (int w = 0; w < instance.lengthOmega; w++) {
                        sum = sum + Omega.p_w[w] * Xrwit[r][w][i][t];
                    }
                    average_Xrit[r][i][t] = sum;
                }
            }
        }

//        printdouble3(average_Xrit);
        map_average_Xrit.put(v,average_Xrit);
    }

    private void updateV() {
        v = v + 1;
        System.out.println("v = "+v);
    }
    private void decomposition() throws IloException, IOException {
        int[][][][] Xrwit = new int[q][instance.lengthOmega][n][T+1];
        for (int w = 0; w < instance.lengthOmega ; w++) {
            Omega omegaOneScenario = new Omega(instance, this.Omega.getTir(w), new double[]{1.0});
            DEF_Decomposition model = new DEF_Decomposition(instance, omegaOneScenario, map_average_Xrit.get(v-1), getWrit(w, map_v_Wrwit.get(v-1)), penalty, "model.lp");
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
        map_v_Xrwit.put(v, Xrwit);
    }



    private void initialize() throws IloException, IOException {

        //initialize Xrwit
        int[][][][] Xrwit = new int[q][instance.lengthOmega][n][T+1];
        for (int w = 0; w < instance.lengthOmega; w++) {
            DEF model = new DEF(instance, Omega, "modelZhuPha_v="+v+".lp");
            model.setupAndSolve();
            int[][][] x_rit = model.get_x_rit();
//            printint3(x_rit);
            model.cleanup();

            for (int i = 0; i < n ; i++) {
                for (int r = 0; r < q; r++) {
                    for (int t = 0; t <= T; t++) {
                        Xrwit[r][w][i][t] = x_rit[r][i][t];
                    }
                }
            }
        }
        map_v_Xrwit.put(v,Xrwit);

        //initialize x average
        aggregation();
        double[][][] average_Xrit = map_average_Xrit.get(0);

        //initialize w
        double[][][][] Wrwit = new double[q][instance.lengthOmega][n][T+1];
        for (int i = 0; i < n; i++) {
            for (int r = 0; r < q; r++) {
                for (int t = 0; t <= T; t++) {
                    for (int w = 0; w < instance.lengthOmega; w++) {
                        Wrwit[r][w][i][t] = penalty * ( Xrwit[r][w][i][t] - average_Xrit[r][i][t]);
                    }
                }
            }
        }
        map_v_Wrwit.put(v,Wrwit);
//        System.out.println(map_v_Wrwit.get(0));
    }

    private double[][][] getWrit(int w, double[][][][] Wrwit){
        double[][][] Writ = new double[q][n][T+1];

        for (int r = 0; r < q; r++) {
            for (int i = 0; i < n; i++) {
                for (int t = 0; t <= T; t++) {
                    Writ[r][i][t] = Wrwit[r][w][i][t];
                }
            }
        }
        return Writ;
    }

    private int[][][] getXrit(int w, int[][][][] Xrwit){
        int[][][] Xrit = new int[q][n][T+1];

        for (int r = 0; r < q; r++) {
            for (int i = 0; i < n; i++) {
                for (int t = 0; t <= T; t++) {
                    Xrit[r][i][t] = Xrwit[r][w][i][t];
                }
            }
        }
        return Xrit;
    }


    private void printdouble3(double[][][] writ, String variable) {
        for (int i = 0; i < n; i++) {
            System.out.println("\n\n"+variable+" for i="+i);

            System.out.printf("%8s", "\nt= ");
            for (int t = 0; t <= T; t++) {
                System.out.printf("%8s", t);
            }

            for (int r = 0; r < q; r++) {
                System.out.printf("%8s", "\nr="+r);
                for (int t = 0; t <= T; t++) {
                    System.out.printf("%8.2f", writ[r][i][t]);
                }
            }
        }
    }

    private void printint3(int[][][] xrit) {
        for (int i = 0; i < n; i++) {
            System.out.println("\n\nx for i="+i);

            System.out.printf("%8s", "\nt= ");
            for (int t = 0; t <= T; t++) {
                System.out.printf("%8s", t);
            }

            for (int r = 0; r < q; r++) {
                System.out.printf("%8s", "\nr="+r);
                for (int t = 0; t <= T; t++) {
                    System.out.printf("%8s", xrit[r][i][t]);
                }
            }
        }
    }
}
