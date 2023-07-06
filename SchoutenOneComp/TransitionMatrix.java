package SchoutenOneComp;

import Main.Instance;

import java.text.DecimalFormat;


/**
 * This class is created to create the transition matrices with power v.
 * As it is easier to multiply 2D matrices with eachother, we will give our pi values a 1D structure so we can create a 2D transition matrix. See the method
 * where we create the pi how the array looks like. Throughout this whole class, we do no PM.
 */
public class TransitionMatrix {
    public final double[][] pi_i0_i1_j0_j1_policy;
    public final Instance i;

    public TransitionMatrix(Instance instance) {
        this.i = instance;
        this.pi_i0_i1_j0_j1_policy = setupPi();

//        printMatrix(pi_i0_i1_j0_j1_policy);
    }

    /**
     * piPower is the probability matrix for multiple steps, given that we do not do any maintenance. So action state is always zero.
     * @return
     */
    public double getPiPowerValue(int i0, int i1, int j0, int j1, int x) {
        if(i0==j0 && i1==j1){
            return 1.0;
        }

        double[][] A = pi_i0_i1_j0_j1_policy;
        double[][] B = A; // Initialize B as A

        for (int pow = 1; pow < x; pow++) {
            double[][] temp = new double[A.length][A[0].length];

            for (int i = 0; i < A.length; i++) {
                for (int j = 0; j < A[0].length; j++) {
                    for (int k = 0; k < A.length; k++) {
                        temp[i][j] += A[i][k] * B[k][j];
                    }
                }
            }

            B = temp; // Update B with the result of matrix multiplication
        }
//        System.out.println("\n\n\n\nprinting for pow = "+x+" and we return for i0="+i0+", i1="+i1+", j0="+j0+", j1="+j1+" the value of "+B[i0 + i1 * i.I0.length][j0 + j1 * i.I0.length]);
//        printMatrix(B);

        return B[i0 + i1 * i.I0.length][j0 + j1 * i.I0.length];
    }

    public double getPiValue(int i0, int i1, int j0, int j1){
        return pi_i0_i1_j0_j1_policy[i0 + i1 * i.I0.length][j0 + j1 * i.I0.length];
    }

    /**
     * In order to find a 1D array containing all pi values, we give it the following structure. We have p(i0, i1, a) as pi value.
     * We convert all states to a 1D array as: i0 + i1 * |I0|.      ( Or as: i0 + i1 * T )
     *
     * Example: for T=3, M=4 :
     * PI_1D = { p(0,0,a), p(1,0,a), p(2,0,a), p(0,1,a), p(1,1,a), p(2,1,a), p(0,2,a), p(1,2,a), p(2,2,a), p(0,3,a), p(1,3,a), p(2,3,a), p(0,4,a), p(1,4,a), p(2,4,a)}
     * PI_1D = {     0         1         2         3       4           5       6           7        8          9           10      11      12          13      14
     *
     * Furthermore, we find the pi values from the weibull distribution we access through the instance.
     * @return 2D transitionMatrix
     */
    private double[][] setupPi() {
        double[][] pi_i0_i1_j0_j1_policy = new double[i.I0.length * i.I1.length][i.I0.length * i.I1.length];

        for (int i0 = 0; i0 < i.I0.length; i0++) {
            for (int i1 = 0; i1 < i.I1.length; i1++) {
                for (int j0 = 0; j0 < i.I0.length; j0++) {
                    for (int j1 = 0; j1 < i.I1.length; j1++) {

                        int a = 0;
                        if( i1==0 || i1==i.M ){ a=1; }

                        pi_i0_i1_j0_j1_policy[i0 + i1 * i.I0.length][j0 + j1 * i.I0.length] = i.piOneDim(i0,i1,j0,j1,a);

                    }
                }
            }
        }
        return pi_i0_i1_j0_j1_policy;
    }


    private void printMatrix(double[][] pi_i0_i1_j0_j1_action0) {

        double[][] matrix = pi_i0_i1_j0_j1_action0;

        DecimalFormat df = new DecimalFormat("0.000");

        int countRows = 0;
        for (double[] row : matrix) {
            if(countRows % i.I0.length == 0){
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            }
            countRows++;
            double sum = 0.0;
            int countColumns = 0;
            for (double value : row) {
                if(countColumns % i.I0.length == 0){
                    System.out.print("|\t");
                }
                countColumns++;
                sum = sum + value;
                String formattedValue = df.format(value);
                System.out.print(formattedValue + "\t");
            }
            System.out.println("\t\t"+sum);
        }
    }
}
