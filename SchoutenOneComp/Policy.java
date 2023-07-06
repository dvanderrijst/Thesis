package SchoutenOneComp;

import Main.Instance;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
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

        // Printing the elements of the 2D array
        for (int i = 0; i < policy.length; i++) {
            for (int j = 0; j < policy[i].length; j++) {
                System.out.print(policy[i][j] + " ");
            }
            System.out.println(); // Move to the next line after printing each row
        }
    }

    /**
     * This constructor inserts the policy int[][] directly.
     * @param policy
     * @param instance
     */
    public Policy(int[][] policy, Instance instance){
        this.instance = instance;
        this.policy = policy;

        // Printing the elements of the 2D array
        for (int i = 0; i < policy.length; i++) {
            for (int j = 0; j < policy[i].length; j++) {
                System.out.print(policy[i][j] + " ");
            }
            System.out.println(); // Move to the next line after printing each row
        }
    }

    private int[][] readPolicy(File file) {
        int[][] policy = new int[instance.I0.length][instance.I1.length];
        for(int i0 : instance.I0){
            for(int i1: instance.I1){
                policy[i0][i1]=-1;
            }
        }

        //first we detect how often the string "i0 i1 a" is present
        String targetString = "i0 i1 a";
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
            while (!line.equals("i0 i1 a") && count!=0) {
                while(!line.equals("i0 i1 a")) {
                    line = scanner.nextLine().trim();
                }
                line = scanner.nextLine().trim();
                count--;
            }


            while(!line.isEmpty()){

                String[] parts = line.split(" ");

                int i0 = Integer.parseInt(parts[0]);
                int i1 = Integer.parseInt(parts[1]);
                int a = Integer.parseInt(parts[2]);

                policy[i0-1][i1]=a;

                if(scanner.hasNext()){line = scanner.nextLine().trim();}
                else{line="";}
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        boolean containsValue = true;
        while(containsValue) {
            for (int i0 : instance.I0) {
                for (int i1 : instance.I1) {
                    if (policy[i0][i1] == -1) {
                        int i0prev = (i0 - 1 + instance.N) % instance.N;
                        if (policy[i0prev][i1 - 1] != -1) {
                            policy[i0][i1] = policy[i0prev][i1 - 1];
                        }
                    }
                }
            }
            containsValue = false;
            // Check if the array contains -1
            for (int i = 0; i < policy.length; i++) {
                for (int j = 0; j < policy[i].length; j++) {
                    if (policy[i][j] == -1) {
                        containsValue = true;
                        break; // Exit the loop if the value is found
                    }
                }
                if (containsValue) {
                    break; // Exit the outer loop if the value is found
                }
            }
        }

        return policy;
    }

    public int get(int i0, int i1) {
        return policy[i0][i1];
    }

    public Policy getAdjustedPolicy(int i0, int i1, int a){
        int[][] newPolicy = deepCopy(policy);
        newPolicy[i0][i1] = a;


        //We first make sure that all values before our action are 0.
        boolean notStop = true;
        int count = 1;
        while(notStop) {
            if (newPolicy[(i0 - count + instance.N) % instance.N][i1 - count] != 0 && (i1-count)!=0 ) {
                newPolicy[(i0 - count + instance.N) % instance.N][i1 - count] = 0;
                count++;
                if(count >= instance.N){
                    System.out.println("not sure if this will work....");
                    System.exit(1);
                }
            } else {
                notStop = false;
            }
        }

        //If we have action 1, we also need to make sure that the actions after (i0,i1) are 1 too.
        if(a==1){
            notStop = true;
            count = 1;
            while(notStop) {
                if (newPolicy[(i0 + count + instance.N) % instance.N][i1 + count] == 0 ) {
                    newPolicy[(i0 + count + instance.N) % instance.N][i1 + count] = 1;
                    count++;
                    if(count >= instance.N){
                        System.out.println("not sure if this will work....");
                        System.exit(1);
                    }
                } else {
                    notStop = false;
                }
            }
        }

        return new Policy(newPolicy, instance);
    }

    /**
     * This method creates a new 2D array copy with the same dimensions as the original array.
     * It then iterates over each row of the original array and creates a new row in the copy array.
     * The elements of each row are copied using System.arraycopy() to ensure a deep copy of the individual elements.
     * @param original int[][]
     * @return deepcopy of the original
     */
    public static int[][] deepCopy(int[][] original) {
        if (original == null) {
            return null;
        }

        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = new int[original[i].length];
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }

        return copy;
    }

}
