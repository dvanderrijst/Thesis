package SchoutenTwoComp;

public class S2CInstance {
    public final int m = 5; //5 years
    public final int N = 12; //12 months in one year
    public final int[] I0;
    public final int[] I1;
    public final int[] I2;
    public final double[] cPR_i;
    public final double[] cCR_i;
    public final double CR_average = 50;
    public final double PR_average = 10;
    public final double CR_delta = 0.3;
    public final double PR_delta = 0.3;
    public final int M = 36; //we set the maximum age to 3 years.
    public final int d       = 3 ; //setup costs
    public final int alpha = 1; //weibull dist alpha
    public final int beta = 2; // weibull dist beta

    public S2CInstance() {
        I0 = setArray(m*N);
        I1 = setArray(M);
        I2 = setArray(M);

        cCR_i = setVariateCosts(CR_average, CR_delta);
        cPR_i = setVariateCosts(PR_average, PR_delta);
    }

    /**
     * sets the variate costs of c_p(i0) and c_f(i0) from the equation on page 985 from Schouten article
     * @param average maintenance costs
     * @param delta variation in maintenance costs (This could have to do with high wind difference throughout the year)
     * @return
     */
    private double[] setVariateCosts(double average, double delta) {
        double[] array = new double[N];
        for (int i = 0; i < N; i++) {
            array[0] = average + delta * Math.cos(2*Math.PI*(1.0/N)-(2*Math.PI/12));
        }
        return array;
    }

    /**
     * creates the array, or sets that we need.
     * @param size
     * @return
     */
    private int[] setArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        return array;
    }




}
