package SchoutenOneComp;

import Main.Instance;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class creates a policy, and is used to easily retrieve actions for certain states.
 * Two type of constructors can be called.
 *
 * @author 619034dr Donna van der Rijst
 */
public class Policy {
    public final int[][] policy;
    public final Instance instance;

    /**
     * Constructor that can create a policy by finding it in a file.
     * @param file where the policy stands in.
     * @param instance Instance containing all important parameters
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
     * Constructor with the policy as a 2D array int[][] directly.
     * @param policy policy
     * @param instance Instance containing all important parameters.
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

    /**
     * Method to easily retrieving the right policy with input state i=(i0,i1)
     * @param i0 period of state i
     * @param i1 age of component of state i
     * @return the optimal action: a/R_i
     */
    public int get(int i0, int i1) {
        return policy[i0][i1];
    }

    /**
     * Method used to read the file and return the policy.
     * @param file input file containing the policy.
     * @return the policy
     */
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
}
