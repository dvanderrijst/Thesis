package Main;

/**
 *
 */
public class Instance {
    //ZHU and SCHOUTEN
    public final double[] cPR_i;
    public final double[] cCR_i;
    public final double CR_average = 50;
    public final double PR_average = 10;
    public final double delta = 0.5;
    public final int d       = 0 ;
    public final int m = 1; //5 years
    public final int N = 12; //12 months in one year
    public final int T       = m*N ;
    public final int[] alpha = new int[]{12,1}; //alpha is 1 year
    public final int[] beta = new int[]{2,2};

    //ZHU
    public final int q       = 4 ;
    public final int lengthOmega;
    private static double[][] probs; //probabilities for component k and time t : probs[k][t]
    public final int n       = 2 ;


    //SCHOUTEN
    public final int[] I0;
    public final int[] I1;
    public final int[] I2;
    public final int[] K = new int[]{1,2};
    public final int M = 13; //we set the maximum age to 3 years

    public Instance() {
        cCR_i = setVariateCosts(CR_average);
        cPR_i = setVariateCosts(PR_average);

        //ZHU
        this.lengthOmega = (int) Math.pow(T+1, n*q);

        //SCHOUTEN
        I0 = setArray(m*N);
        I1 = setArray(M);
        I2 = setArray(M);
    }
    /**
     * sets the variate costs of c_p(i0) and c_f(i0) from the equation on page 985 from Schouten article
     * the value delta is the variation in maintenance costs (e.g. high wind difference throughout the year)
     * January has the highest costs, July the lowest.
     * @param average maintenance costs
     * @return the array with variate costs.
     */
    private double[] setVariateCosts(double average) {
        double[] array = new double[N];
        for (int i = 0; i < N; i++) {
            array[i] = average + delta * average * Math.cos(2*Math.PI*i*(1.0/N));
            System.out.print(array[i]+"\t");
        }
        System.out.println();
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
     * Creates the array probs[][] if this is not done yet. This method is created so that not everytime all the probabilities have to be calculated by the computer.
     * @param x lifetime x
     * @param k component k
     * @return P(X = x) in vector notation, probs[x] is P(X=x)
     */
    public double probX_x_k(int x, int k){

        if(probs == null){
            probs = new double[3][Math.max(T,M) + 2]; //k={1,2} so we use 3 we can use k=1 and k=2.
            for (int kk :K){
                double sum = 0.0;

                for (int i = 0; i < Math.max(T,M); i++) {
                    probs[kk][i] = pXweibull(i, kk);
                    sum = sum + probs[kk][i];
                }
                probs[kk][Math.max(T,M)] = 1.0 - sum;
            }
        }

        return probs[k][x];
    }

    /**
     * Returning the descretised Weibull dist
     * Here, de CDF is F(x)=1-exp((x/alpha)^beta). Then P(X=x)=F(x+1)-F(x)
     *
     * @param x in P(X=x), can go from 0 to infinity
     * @param k is the component number. k ={1,2}
     * @return P(X = x)
     */
    public double pXweibull(int x, int k){

        double expo1 = Math.pow( (x + 1.0) / (double) alpha[k-1], beta[k-1]);
        double F_x1 = 1.0 - Math.exp(-1.0 * expo1);

        double expo2 = Math.pow( (x      ) / (double) alpha[k-1], beta[k-1]);
        double F_x0 = 1.0 - Math.exp(-1.0 * expo2);

        double P_X = F_x1 - F_x0;

        return P_X;
    }

    /**
     * Gives small p, which is p{i_k}=P(X=i_k|X\geq i_k) = P(X=i_k)/(1-sum_{x=0}^i_k P(X=i)
     *
     * @param i_k, age of component k
     * @param k
     * @return p^k{i_k}
     */
    public double probCondX_x_k(int i_k, int k){
        double sum = 0.0;
        //first calculate P(X >= i_k) which is 1 - P(X<i_k)
        for (int i = 0; i < i_k; i++) {
            sum = sum + probX_x_k(i, k);
        }
        double denominator = 1.0 - sum;

        //then calculate P(X=i_k)
        double numerator = probX_x_k(i_k, k);
        return numerator/denominator;
    }
}

