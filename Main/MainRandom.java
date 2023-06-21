package Main;

import ilog.concert.IloException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainRandom {
    public static void main(String[] args) throws IloException, IOException {
        splitARPSchouten();
//        splitMBRPSchouten();
//        splitDEFZhu();
    }

    private static void splitDEFZhu() throws IOException {
        String folder = "policiesZHU_Tzhu=15";
        String inputFileName = "policy_ZhuT=15.txt";

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

    private static void splitARPSchouten() throws IOException {
        String folder = "policiesARP2_1";
        String inputFile = "allPoliciesSchoutenARP.txt";
        String inputFileName = folder+"/"+inputFile;

        for (int i = 0; i < 12; i++) {
            Scanner scanner = new Scanner(new File(inputFileName));

            File f = new File(folder+"/fullPolicyARP_i0=" + i + ".dat");
            FileWriter w = new FileWriter(f, true);
            w.write("i1 i2 i0 a\n");

            while(scanner.hasNext()) {
                String nextLine = scanner.nextLine();
                String[] split = nextLine.split(" ");
                if(split.length < 3){
                    break;
                }

                if (Integer.parseInt(split[2]) == i) {
                    w.write(nextLine+"\n");
                }
            }
            w.close();
        }

        Scanner scanner = new Scanner(new File(inputFileName));
        File f = new File(folder+"/info.txt");
        FileWriter w = new FileWriter(f, true);

        while(scanner.hasNext()) {
            String nextLine = scanner.nextLine();
            String[] split = nextLine.split(" ");
            if(split.length < 3){
                break;
            }
        }

        while(scanner.hasNext()){
            String nextLine = scanner.nextLine();
            w.write(nextLine+"\n");
        }
        w.close();
    }
    private static void splitMBRPSchouten() throws IOException {

        for (int i = 0; i < 12; i++) {
            String inputFileName = "/Users/donnavanderrijst/Downloads/Programming/Thesis/policiesMBRP2/policySchoutenMBRP.txt";
            Scanner scanner = new Scanner(new File(inputFileName));


            File f = new File("policiesMBRP2/policyMBRP_i0=" + i + ".dat");
            FileWriter w = new FileWriter(f, true);
            w.write("i1 i2 i0 a\n");

            while(scanner.hasNext()) {
                String nextLine = scanner.nextLine();
                String[] split = nextLine.split(" ");
                if(split.length < 3){
                    break;
                }

                if (Integer.parseInt(split[2]) == i) {
                    w.write(nextLine+"\n");
                }
            }
            w.close();
        }

        String inputFileName = "/Users/donnavanderrijst/Downloads/Programming/Thesis/policiesMBRP2/policySchoutenMBRP.txt";
        Scanner scanner = new Scanner(new File(inputFileName));

        File f = new File("policiesMBRP2/info.txt");
        FileWriter w = new FileWriter(f, true);

        while(scanner.hasNext()) {
            String nextLine = scanner.nextLine();
            String[] split = nextLine.split(" ");
            if(split.length < 3){
                break;
            }
        }

        while(scanner.hasNext()){
            String nextLine = scanner.nextLine();
            w.write(nextLine+"\n");
        }
        w.close();
    }

}
