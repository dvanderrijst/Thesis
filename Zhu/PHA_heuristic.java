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
//            printXwit(map_v_Xwit.get(v));
//            if(v>0){printXwitDiff(map_v_Xwit.get(v-1), map_v_Xwit.get(v));}
            printWwit(map_v_Wwit.get(v));
            printXaverage(map_average_Xit.get(v));
            updateV();
            decomposition();
            aggregation();
            updatePrice();
//            calculateDistance();
//            System.out.println(map_gdistances.get(v));
            count++;
        }
    }

    private void calculateDistance(){

    }
    private void decomposition() {
        int[][][] Xwit = new int[scenarios.size()][instance.n+1][instance.T+1];
        map_v_Xwit.put(v,Xwit);

        for (int w = 0; w < scenarios.size() ; w++) {
            Algorithm4Decomposition hello = new Algorithm4Decomposition(scenarios.get(w), instance, map_v_Xwit.get(v), w, map_v_Wwit.get(v-1), map_average_Xit.get(v-1), penalty);
            map_v_Xwit.put(v, hello.doAlgorithm4());
        }
    }



    private void updateV() {
        v = v + 1;
        System.out.println("v = "+v);
    }

    private void initialize() {
        //intialize Xwit
        int[][][] Xwit = new int[scenarios.size()][instance.n+1][instance.T+1];
        map_v_Xwit.put(v,Xwit);
        for (int w = 0; w < scenarios.size() ; w++) {
//            System.out.println("w="+w);
            Algorithm4 hello = new Algorithm4(scenarios.get(w), instance, map_v_Xwit.get(v), w);
            map_v_Xwit.put(v,hello.doAlgorithm4());
        }

        //initialize x average
        aggregation();

        //initialize w
        updatePrice();
    }

    private void updatePrice(){
        double[][][] Wwit = new double[scenarios.size()][instance.n+1][instance.T+1];

        double[][] Xit_average = map_average_Xit.get(v);
        int[][][] Xwit = map_v_Xwit.get(v);

        if(v>0){
            double[][][] Writ_prev = map_v_Wwit.get(v-1);
            for (int i = 0; i <= instance.n; i++) {
                for (int t = 0; t < instance.T; t++) {
                    for (int w = 0; w < scenarios.size() ; w++) {
                        Wwit[w][i][t] = Writ_prev[w][i][t] + penalty * (Xwit[w][i][t] - Xit_average[i][t]);
                    }
                }
            }
        }
        else{ //for initialization
            for (int i = 0; i <= instance.n; i++) {
                for (int t = 0; t < instance.T; t++) {
                    for (int w = 0; w < scenarios.size() ; w++) {
                        Wwit[w][i][t] = penalty * (Xwit[w][i][t] - Xit_average[i][t]);
                    }
                }
            }
        }
        map_v_Wwit.put(v,Wwit);
    }


    private void aggregation() {
        double[][] average_Xit = new double[instance.n+1][instance.T+1];
        int[][][] Xwit = map_v_Xwit.get(v);


        //print Xwit X
//        printXwit(map_v_Xwit.get(v));

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

        //print average X
//        printXaverage(average_Xit);

        map_average_Xit.put(v, average_Xit);
    }



    private void printWwit(double[][][] doubles) {
        for (int w = 0; w < scenarios.size(); w++) {
            for (int i = 0; i < instance.n + 1; i++) {
                for (int t = 0; t < instance.T + 1; t++) {
                    System.out.print(doubles[w][i][t]+"\t");
                }
                System.out.println();
            }
            System.out.println("\n");
        }
    }

    private void printXwit(int[][][] ints) {
        for (int w = 0; w < scenarios.size(); w++) {
            for (int i = 0; i < instance.n + 1; i++) {
                for (int t = 0; t < instance.T + 1; t++) {
                    System.out.print(ints[w][i][t]+"\t");
                }
                System.out.println();
            }
            System.out.println("\n");
        }
    }

    private void printXwitDiff(int[][][] ints, int[][][] ints2) {
        for (int w = 0; w < scenarios.size(); w++) {
            for (int i = 0; i < instance.n + 1; i++) {
                for (int t = 0; t < instance.T + 1; t++) {
                    System.out.print((ints[w][i][t]-ints2[w][i][t])+"\t");
                }
                System.out.println();
            }
            System.out.println("\n");
        }
    }

    private void printXaverage(double[][] doubles){
        for (int i = 0; i < instance.n + 1; i++) {
        for (int t = 0; t < instance.T + 1; t++) {
            System.out.print(doubles[i][t]+"\t");
        }
        System.out.println();
    }}
}
