package Zhu;

import Main.Instance;
import ilog.concert.IloException;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainZhu2022 {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();

        String fileName = "rolllinnng.lp";
        //document what is done
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write("\nm="+instance.m);
            writer.write("\nN="+instance.N);
            writer.write("\nT=mN="+instance.T);
            writer.write("\nn="+instance.n);
            writer.write("\nq="+instance.q);
            writer.write("\nlengthOmega="+instance.lengthOmega);
            writer.write("\nd="+instance.d);
            writer.write("\nCR_average_i="+ Arrays.toString(instance.CR_average));
            writer.write("\nPR_average_i="+ Arrays.toString(instance.PR_average));
            writer.write("\nalpha="+ Arrays.toString(instance.alpha));
            writer.write("\nbeta="+ Arrays.toString(instance.beta));
            writer.write("\ndelta="+instance.delta);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DEF_rolling defRol = new DEF_rolling(instance, fileName);
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
