package Zhu;

import Main.Instance;
import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class MainZhu2022 {
    public static void main(String[] args) throws IloException {
        Instance instance = new Instance();
        Omega omega = new Omega(instance);

        DEF def = new DEF(instance, omega, "HELOOO.lp");
        def.setupAndSolve();





        //print Twir
//        for (int w = 0; w < instance.lengthOmega ; w++) {
//            System.out.println("omega="+w);
//            System.out.println("r\t1\t2\t3\t4\t5");
//            for (int i = 0; i < instance.n; i++) {
//                System.out.print("i\t");
//                for (int r = 0; r < instance.q; r++) {
//                    System.out.print(omega.Twir[w][i][r]+"\t");
//                }
//                System.out.println();
//            }
//        }

//        DEF def = new DEF(instance, omega, "outputHere.lp");
//        def.setupAndSolve();




//        List<int[][]> list = generate5scenarios();
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
}
