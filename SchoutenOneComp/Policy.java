package SchoutenOneComp;

import Main.Instance;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Policy {
    public final int[][] policy;
    public final Instance instance;

    /**
     * This policy can read the policy from the file provided.
     * @param file
     * @param instance
     */
    public Policy(File file, Instance instance) {
        this.instance = instance;
        this.policy = readPolicy(file);
    }

    /**
     * This constructor inserts the policy int[][] directly.
     * @param policy
     * @param instance
     */
    public Policy(int[][] policy, Instance instance){
        this.instance = instance;
        this.policy = policy;
    }

    private int[][] readPolicy(File file) {
        int[][] policy = new int[instance.I0.length][instance.I1.length];

        //first we detect how often the string "i0 i1 a" is present
        String targetString = "i1 i2 i0 a";
        int count = 0;
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.equals(targetString)) {
                    count++;
                }
            }
            System.out.println("this file "+file.getName()+" contains "+count+" policies");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Scanner scanner = new Scanner(file);

            //we first navigate to the correct location in our file.
            String line = scanner.nextLine().trim();
            while (!line.equals("i1 i2 i0 a") && count!=0) {
                count--;
                line = scanner.nextLine().trim();
            }

            while(scanner.hasNextLine()){
                line = scanner.nextLine();

                String[] parts = line.split(" ");


                int i0 = Integer.parseInt(parts[0]);
                int i1 = Integer.parseInt(parts[1]);
                int a = Integer.parseInt(parts[2]);

                policy[i0][i1]=a;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return policy;
    }
}
