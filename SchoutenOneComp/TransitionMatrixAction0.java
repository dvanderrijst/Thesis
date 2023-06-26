package SchoutenOneComp;

import Main.Instance;
import org.apache.commons.math4.*;
import org.apache.commons.math4.legacy.linear.MatrixUtils;
import org.apache.commons.math4.legacy.linear.NonSquareMatrixException;
import org.apache.commons.math4.legacy.linear.RealMatrix;

/**
 * This class is created to create the transition matrices with power v.
 * As it is easier to multiply 2D matrices with eachother, we will give our pi values a 1D structure so we can create a 2D transition matrix. See the method
 * where we create the pi how the array looks like. Throughout this whole class, we use action = 0.
 */
public class TransitionMatrixAction0 {
    public final double[][] pi_i0_i1_j0_j1_action0;
    public final Instance i;

    public TransitionMatrixAction0(Instance instance) {
        this.i = instance;
        this.pi_i0_i1_j0_j1_action0 = setupPi();
    }

    /**
     * piPower is the probability matrix for multiple steps, given that we do not do any maintenance. So action state is always zero.
     * @return
     */
    public double[][] getPiPowerMatrix(int power) throws IllegalArgumentException {
        RealMatrix matrixA = MatrixUtils.createRealMatrix(pi_i0_i1_j0_j1_action0);

        double[][] B = null;
        if (!matrixA.isSquare()) {
            throw new IllegalArgumentException("Matrix is not square.");
        }
        else{
        RealMatrix matrixB = matrixA.power(power);
        B = matrixB.getData();
        }


        return B;
    }


    /**
     * piPower is the probability matrix for multiple steps, given that we do not do any maintenance. So action state is always zero.
     * @return
     */
    public double getPiPowerValue(int i0, int i1, int j0, int j1, int power) {

        RealMatrix matrixA = MatrixUtils.createRealMatrix(pi_i0_i1_j0_j1_action0);
        RealMatrix matrixB = matrixA.power(x);

        double[][] B = matrixB.getData();

        return B[i0 + i1 * i.I0.length][j0 + j1 * i.I0.length];
    }


    /**
     * In order to find a 1D array containing all pi values, we give it the following structure. We have pi(i0, i1, a) as pi value. Then, the array looks the following.
     * PI = { p(0,0,0), p(1,0,0), p(2,0,0), ... , p(0,1,0), p(1,1,0), p(2,1,0), ..., p(0,2,0), p(1,2,0), p(2,2,0), ..., p(M, M, 0)}
     *
     * Example: for |I0|=3, |I1|=5,(M=4)
     * PI_1D = { p(0,0,0), p(1,0,0), p(2,0,0), p(0,1,0), p(1,1,0), p(2,1,0), p(0,2,0), p(1,2,0), p(2,2,0), p(0,3,0), p(1,3,0), p(2,3,0), p(0,4,0), p(1,4,0), p(2,4,0)}
     * PI_1D = {     0         1         2         3       4           5       6           7        8          9           10      11      12          13      14
     * @return 2D transitionMatrix
     */
    private double[][] setupPi() {
        double[][] pi_i0_i1_j0_j1_action0 = new double[i.I0.length * i.I1.length][i.I0.length * i.I1.length]; //we have action state {0,1}.

        for (int i0 = 0; i0 < i.I0.length; i0++) {
            for (int i1 = 0; i1 < i.I1.length; i1++) {
                for (int j0 = 0; j0 < i.I0.length; j0++) {
                    for (int j1 = 0; j1 < i.I1.length; j1++) {
                        pi_i0_i1_j0_j1_action0[i0 + i1 * i.I0.length][j0 + j1 * i.I0.length] = i.piOneDim(i0,i1,j0,j1,0);
                    }
                }
            }
        }
        return pi_i0_i1_j0_j1_action0;
    }

}
