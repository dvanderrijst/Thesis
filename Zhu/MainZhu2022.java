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

        System.exit(1);

        String fileName = "policy.txt";
        writeInfo(instance, fileName);


        for (int i0 = 0; i0 < instance.N; i0++) {
            for (int i1 = 1; i1 < 6; i1++) {
                for (int i2 = 1; i2 < 5; i2++) {
                    //changing instance settings
                    instance.startTime = i0;
                    instance.startAges[0]=i1;
                    instance.startAges[1]=i2;

                    Omega omega = new Omega(instance);
                    DEF_period defperiod = new DEF_period(instance, omega, fileName);
                    defperiod.setupAndSolve();
                    defperiod.cleanup();
                }
            }
        }
    }

    private static void writeInfo(Instance instance, String fileName) {
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
    }
}
