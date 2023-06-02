package Zhu;

import Main.Instance;
import ilog.concert.IloException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PHA_heuristic {
    private static int v = 0;
    private final double epsilon = 0.01;
    private final List<int[][]> scenarios;
    private final Instance instance;
    private double penalty = 1.0;
    private static Map<Integer, int[][][]> map_v_Xwit = new HashMap<>();
    private final Map<Integer, double[][][]> map_v_Wwit = new HashMap<>();
    private final Map<Integer, double[][]> map_average_Xit = new HashMap<>();
    private final Map<Integer, Double> map_gdistances = new HashMap<>();

    public PHA_heuristic(List<int[][]> scenarios, Instance instance) {
        this.scenarios = scenarios;
        this.instance = instance;

        initialize();
        int count = 0;
        while(count<7 && ( map_gdistances.isEmpty() ||  map_gdistances.get(v) > epsilon) ){
//            updateV();
//            decomposition();
//            aggregation();
//            updatePrice();
//            calculateDistance();
//            System.out.println(map_gdistances.get(v));
            count++;
        }
    }

    private void decomposition() throws IloException {

        int[][][] Xwit = new int[scenarios.size()][instance.n+1][instance.T+1];
        map_v_Xwit.put(v,Xwit);
        for (int w = 0; w < scenarios.size() ; w++) {}

    }

    private void updateV() {
        v = v + 1;
        System.out.println("v = "+v);
    }

    private void initialize() {
        //intialize Xwit
        int[][][] Xwit = new int[scenarios.size()][instance.n+1][instance.T+1];
        map_v_Xwit.put(v,Xwit);
        int w = 0;
        for(int[][] scenario : scenarios){
            Algorithm4 hello = new Algorithm4(scenario, instance, map_v_Xwit.get(v), w);
            w++;
        }

        //initialize x average
        double[][] average_Xrit = aggregation();
        map_average_Xit.put(v, average_Xrit);

        //initialize w
        double[][][] Wwit = new double[scenarios.size()][instance.n+1][instance.T+1];
        for (int i = 0; i <= instance.n; i++) {

            for (int t = 0; t < instance.T; t++) {
                for (w = 0; w < scenarios.size() ; w++) {
                    Wwit[w][i][t] = penalty * (Xwit[w][i][t] - average_Xrit[i][t]);
                }
            }
        }
        map_v_Wwit.put(v,Wwit);
    }


    private double[][] aggregation() {
        double[][] average_Xit = new double[instance.n+1][instance.T+1];
        int[][][] Xwit = map_v_Xwit.get(v);
        for (int i = 0; i <= instance.n; i++) {
            for (int t = 0; t < instance.T; t++) {
                double sum = 0.0;
                for (int w = 0; w < scenarios.size(); w++) {
                    sum = sum + 0.2 * Xwit[w][i][t];
                    System.out.print("THIS PROBABILITY INCORRECT!!!!");
                }
                average_Xit[i][t] = sum;
            }
        }
        return average_Xit;
    }
}
