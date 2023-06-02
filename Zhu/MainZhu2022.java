package Zhu;

import Main.Instance;
import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.List;

public class MainZhu2022 {
    public static void main(String[] args) throws IloException {
        System.out.println("Hello donnie");
        Instance instance = new Instance();
        Omega omega = new Omega(instance);

        //zhuSmallInstance(instance, omega)
        PHA pha = new PHA(omega, instance);
//        List<int[][]> list = generate5scenarios();
        double[] hello = new double[]{0.0	,0.006920387509683934,	0.020523027137251914	,0.033474806045074725,	0.04545237206562901,	0.0561917060722181,	0.06550032276851925	,0.07326289826737059,	0.07944020638276157	,0.08406202315667718,	0.08721530016576963,	0.08902932948876624,	0.0};
        double sum = 0.0;
        for(double hi : hello){
            sum = sum + hi;
        }
        System.out.println(sum);

        PHA_heuristic algorithm4 = new PHA_heuristic(omega.getScenarios(), instance);
    }

    private static List<Individual> manuallyCreateIndividuals(Instance instance, int[][] lifetimes){
        List<Individual> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int r = 0; r < 5; r++) {
                Individual individual = new Individual(i, r, lifetimes[i][r], instance);
                list.add(individual);
            }
        }
        return list;
    }

    private static List<int[][]> generate5scenarios(){
        int[][] array1 = {
                {4, 6, 8, 3, 9},
                {5, 7, 10, 2, 4},
                {9, 3, 6, 8, 5},
                {7, 2, 4, 10, 6}
        };
        int[][] array2 = {
                {5, 8, 2, 9, 6},
                {7, 4, 10, 3, 8},
                {9, 5, 6, 2, 4},
                {3, 7, 4, 6, 10}
        };
        int[][] array3 = {
                {3, 9, 4, 8, 5},
                {6, 2, 10, 7, 9},
                {4, 3, 5, 6, 8},
                {7, 10, 2, 9, 4}
        };
        int[][] array4 = {
                {9, 5, 8, 2, 7},
                {4, 10, 3, 6, 9},
                {7, 4, 9, 5, 3},
                {2, 6, 8, 10, 4}
        };
        int[][] array5 = {
                {6, 2, 9, 4, 8},
                {10, 7, 3, 5, 9},
                {4, 6, 8, 2, 3},
                {5, 9, 7, 10, 4}
        };
        List<int[][]> list = new ArrayList<>();
        list.add(array1);
        list.add(array2);
        list.add(array3);
        list.add(array4);
        list.add(array5);
        return list;
    }

    private static void zhuSmallInstance(Instance instance, Omega omega) throws IloException {        //manually create three scenarios as input.
        int[][][] Twir = new int[3][2][2]; // w = 3, q = 2, n = 2
        Twir[0][0][0] = 0;
        Twir[0][0][1] = 1;
        Twir[0][1][0] = 2;
        Twir[0][1][1] = 3;
        //        Twir[1][0][0] = 2;
//        Twir[1][0][1] = 1;
//        Twir[1][1][0] = 3;
//        Twir[1][1][1] = 2;
//        Twir[2][0][0] = 1;
//        Twir[2][0][1] = 1;
//        Twir[2][1][0] = 3;
//        Twir[2][1][1] = 1;
        double[] probs = new double[]{0.0000000001};//, 0.333333, 0.666666666};

        //        ZhuCplexModel model = new ZhuCplexModel(instance, omega.getTwir(), omega.getProbabilityScenario());
        ZhuCplexModel model = new ZhuCplexModel(instance, Twir, probs, "modelZhuDEF.lp");
        model.setupAndSolve();
    }


}
