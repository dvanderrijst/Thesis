package Main;

import ilog.concert.IloException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MainRandom {
    public static void main(String[] args) throws IloException, IOException {
        for (int i = 0; i < 12; i++) {
            String inputFileName = "/Users/donnavanderrijst/Downloads/Programming/Thesis/policySchoutenARP.txt";
            Scanner scanner = new Scanner(new File(inputFileName));


            File f = new File("policies/policyARP_i0=" + i + ".dat");
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

        String inputFileName = "/Users/donnavanderrijst/Downloads/Programming/Thesis/policySchoutenARP.txt";
        Scanner scanner = new Scanner(new File(inputFileName));

        File f = new File("policies/info.txt");
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
