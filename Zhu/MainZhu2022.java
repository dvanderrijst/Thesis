package Zhu;

import Main.Instance;
import ilog.concert.IloException;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainZhu2022 {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();
        int Tzhu = 5;
        instance.ThorizonZhu = Tzhu;
        String fileName = "Output/policiesZHU_Tzhu="+Tzhu+"/policy_ZhuT="+Tzhu+".txt";
        FileWriter f = new FileWriter(fileName);
        writeInfo(instance, fileName);

        for (int i0 : instance.I0) {
            for (int i1 : instance.I1) {
                for (int i2 : instance.I2) {

                    instance.startTime = i0;
                    instance.startAges[0] = i1;
                    instance.startAges[1] = i2;

                    Omega omega = new Omega(instance);

                    DEF_period defperiod = new DEF_period(instance, omega, fileName);
                    defperiod.setupAndSolve();
                    defperiod.cleanup();
                }
            }
        }

        splitDEFZhu(Tzhu);

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


    private static void splitDEFZhu(int ThorizonZhu) throws IOException {
        String folder = "Output/policiesZHU_Tzhu="+ThorizonZhu+"";
        String inputFileName = "policy_ZhuT="+ThorizonZhu+".txt";

        //first we read the data in a big array a[][][][]
        int[][][] a_i0_i1_i2 = new int[12][13][13];
        String inputFile = "/Users/donnavanderrijst/Downloads/Programming/Thesis/"+folder+"/"+inputFileName;
        Scanner scanner = new Scanner(new File(inputFile));
        String nextLine = scanner.nextLine();
        while(nextLine.split("=")[0].compareTo("startTime")!=1){
            nextLine = scanner.nextLine();
        }

        //define the patterns
        Pattern regex1 = Pattern.compile("startTime =(\\d+)\tstartAges=\\[(\\d+), (\\d+)\\]\t kesi=\\[(\\d+), (\\d+)\\]");
        Pattern regexX = Pattern.compile("x1 = (-?\\d+(?:\\.\\d+)?)\t\t x2 = (-?\\d+(?:\\.\\d+)?)");

        //find a values
        while(!nextLine.isEmpty()){
            Matcher matcher1 = regex1.matcher(nextLine);
            matcher1.find();
            int i0 = Integer.parseInt(matcher1.group(1));
            int i1 = Integer.parseInt(matcher1.group(2)) + 1;
            int i2 = Integer.parseInt(matcher1.group(3)) + 1;
            int kesi0 = Integer.parseInt(matcher1.group(4));
            int kesi1 = Integer.parseInt(matcher1.group(5));

            if(kesi0 == 1){
                i1 = 0;
            }
            if(kesi1 == 1){
                i2 = 0;
            }
            if(i1==12){
                System.out.println("some wrong NOT here. ");
            }

            nextLine = scanner.nextLine();
            Matcher matcherX = regexX.matcher(nextLine);
            matcherX.find();
            int x1 = (int) Math.round(Double.parseDouble(matcherX.group(1)));
            int x2 = (int) Math.round(Double.parseDouble(matcherX.group(2)));

            if(x1 == 0 && x2 == 0) {
                a_i0_i1_i2[i0][i1][i2] = -1 ; //choose to put -1, as when creating the array default value is 0.
            } else if(x1 == 1 && x2 == 0) {
                a_i0_i1_i2[i0][i1][i2] = 1 ;
            } else if(x1 == 0 && x2 == 1) {
                a_i0_i1_i2[i0][i1][i2] = 2 ;
            } else if(x1 == 1 && x2 == 1) {
                System.out.println(i0+"\t"+i1+"\t"+i2);
                a_i0_i1_i2[i0][i1][i2] = 3 ;
            } else{
                System.out.println("some wrong here. ");
            }

            scanner.nextLine();
            nextLine = scanner.nextLine();
        }


        //do the writing, from a[][][] to the files.
        int i0 = 0;
        File f = new File(folder+"/policyZHU_i0=" + i0 + ".dat");
        FileWriter w = new FileWriter(f, true);
        w.write("i1 i2 i0 a\n");

        for (int i1 = 0; i1 < 13; i1++) {
            for (int i2 = 0; i2 < 13; i2++) {
                String entry = null;
                if (a_i0_i1_i2[i0][i1][i2] != 0) {
                    if (a_i0_i1_i2[i0][i1][i2] == -1) {
                        entry = i1 + " " + i2 + " " + i0 + " " + 0;
                    } else {
                        entry = i1 + " " + i2 + " " + i0 + " " + a_i0_i1_i2[i0][i1][i2];
                    }
                }
                if (entry != null) {
                    w.write(entry + "\n");
                }
            }
        }
        w.close();

    }
}
