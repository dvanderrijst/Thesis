package Main;

public class Instance {
    //ZHU and SCHOUTEN
    public final double[] cPR_i;
    public final double[] cCR_i;
    public final double CR_average = 6;
    public final double PR_average = 2;
    public final double CR_delta = 0.3;
    public final double PR_delta = 0.3;
    public final int d       = 3 ;
    public final int n       = 2 ;
    public final int m = 1; //5 years
    public final int N = 4; //12 months in one year
    public final int T       = m*N ;
    public final int[] alpha = new int[]{1,2};
    public final int[] beta = new int[]{2,2};

    //ZHU
    public final int q       = 4 ;
    public final int lengthOmega;
    private static double[] probs;


    //SCHOUTEN
    public final int[] I0;
    public final int[] I1;
    public final int[] I2;
    public final int M = 4; //we set the maximum age to 3 years.

    public Instance() {

        this.lengthOmega = (int) Math.pow(T+1, n*q);
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

    /**
     *
     * @param x
     * @return P(X=x) in vector notation, probs[x] is P(X=x)
     */
    public double P_Xarrray(int x){
        if(probs == null){
            probs = new double[T+2];
            double sum = 0.0;

            for (int i = 1; i <= T ; i++) {
                probs[i] = P_Xweibull(i, 1);//this is wrong!
                sum = sum + probs[i];
            }
            System.out.println(sum);
            probs[T+1] = 1.0 - sum;
        }
        return probs[x];
    }

    /**
     * Returning the descretised Weibull dist
     * Here, de CDF is F(x)=1-exp((x/alpha)^beta). Then P(X=x)=F(x)-F(x-1)
     *
     * @param x in P(X=x)
     * @param k
     * @return P(X = x)
     */
    public double P_Xweibull(int x, int k){
        double P_X;
        double hi = Math.pow(x / (double) alpha[k-1], beta[k-1]);
        double F_x = 1.0 - Math.exp(-1*hi);
        if(x==0){
            P_X = F_x;
        }
        else {
            double hii = Math.pow((x - 1.0) / (double) alpha[k-1], beta[k-1]);
            double F_x_1 = 1.0 - Math.exp(-1.0 * hii);
            P_X = F_x - F_x_1;
        }
        return P_X;
    }

    /**
     * Gives small p, which is p{i_k}=P(X=i_k|X\geq i_k) = P(X=i_k)/(1-sum_{x=0}^i_k P(X=i)
     *
     * @param i_k, age of component k
     * @param k
     * @return p^k{i_k}
     */
    public double p_i(int i_k, int k){
        double sum = 0.0;
        for (int i = 0; i < i_k; i++) {
            sum = sum + P_Xweibull(i, k);
        }
        double denominator = 1.0 - sum;
        double numerator = P_Xweibull(i_k, k);
        return numerator/denominator;
    }
}

