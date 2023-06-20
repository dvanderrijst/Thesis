package Zhu;

import Main.Instance;
import ilog.concert.IloException;

import java.io.*;
import java.util.Arrays;


public class MainZhu2022 {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();
        String fileName = "policiesZHU_Tzhu=15/policy_ZhuT=15.txt";
        writeInfo(instance, fileName);

//        for (int i0 = 0; i0 < 12; i0++) {
        int i0 = 0;
        int i1 = 2;
        int i2 = 15;
//            for (int i1 = 1; i1 < 12; i1++) {
//        instance.kesi = new int[]{0,1};
//                for (int i1 = 0; i1 < 12; i1++) {
                    //changing instance settings
                    instance.startTime = i0;
                    instance.startAges[0] = i1;
                    instance.startAges[1] = i2;

                    Omega omega = new Omega(instance);
                    DEF_period defperiod = new DEF_period(instance, omega, fileName);
                    defperiod.setupAndSolve();
                    defperiod.cleanup();
//                }
//            }
//        }
    }

    private static void writeInfo(Instance instance, String fileName) {
        //document what is done
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write("\nm="+instance.m);
            writer.write("\nN="+instance.N);
            writer.write("\nT=mN="+instance.T);
            writer.write("\nThorizonZhu="+instance.ThorizonZhu);
            writer.write("\nn="+instance.n);
            writer.write("\nq="+instance.q);
            writer.write("\nlengthOmega="+instance.lengthOmega);
            writer.write("\nd="+instance.d);
            writer.write("\nCR_average_i="+ Arrays.toString(instance.CR_average));
            writer.write("\nPR_average_i="+ Arrays.toString(instance.PR_average));
            writer.write("\nstartAges="+ Arrays.toString(instance.startAges));
            writer.write("\nkesi="+ Arrays.toString(instance.kesi));
            writer.write("\nalpha="+ Arrays.toString(instance.alpha));
            writer.write("\nbeta="+ Arrays.toString(instance.beta));
            writer.write("\ndelta="+instance.delta);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
