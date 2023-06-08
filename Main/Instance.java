package Main;

import java.util.Random;

/**
 * This class contains all the data that are inputs for the models.
 * It also contains all methods related to the Weibull distributions.
 * It is chosen to put it all in one instance, to make sure some general components are equal.
 */
public class Instance {
    //ZHU and SCHOUTEN
    public final double[] cPR_i;
    public final double[] cCR_i;
    public final double CR_average = 14.4;
    public final double PR_average = 1;
    public final double delta = 0.0;
    public final int d       = 5;
    public final int m = 1; //5 years
    public final int N = 5; //12 months in one year
    public final int T       = m*N ;
    public final double[] alpha = new double[]{6.5, 6.7}; //alpha is 1 year
    public final double[] beta = new double[]{ 6.9,   5 };

    //ZHU instance
//    public final double CR_average_i = new double[]{14.4, 11.4, 9.4, 8.0, 11.1, 14.2, 7.4};
//    public final double alpha = new double[]{6.5, 6.7, 5.4, 4.9, 4.8, 4.4, 5.5};
//    public final double beta = new double[] {6.9,   5, 7.3, 4.8, 4.2, 4.5, 3.2};


    //ZHU
    public static Random random = new Random();
    public final int q       = T +2 ;
    public final int lengthOmega;
    private static double[][] probs; //probabilities for component k and time t : probs[k][t]
    public final int n       = 2 ;
    public final double sigma_SAA ;
    public final double epsilon_SAA ;
    public final double tau_SAA;
    public final double alpha_SAA;
    public final int[] startAges = new int[n];
    public final int[] kesi = new int[n];

    //SCHOUTEN
    public final int[] I0;
    public final int[] I1;
    public final int[] I2;
    public final int[] K ;
    public final int M = m*N;                //we set the maximum age to 3 years

    public Instance() {
        cCR_i = setVariateCosts(CR_average);
        cPR_i = setVariateCosts(PR_average);

        //ZHU
        setKesiandStartAge();
        sigma_SAA = 2*T*(CR_average*n+d);
        epsilon_SAA = 0.1 * sigma_SAA;
        tau_SAA = 0.1 * epsilon_SAA;
        alpha_SAA = 0.1;
        int sizeX = q*n*(T+1);
        lengthOmega = (int) Math.round((2*Math.pow(sigma_SAA, 2)) / (Math.pow(epsilon_SAA - tau_SAA, 2)) * Math.log(sizeX/alpha_SAA));
//        lengthOmega = 100;
        System.out.println("lengthOmega  = "+lengthOmega);

        //SCHOUTEN
        I0 = setArray(m*N);
        I1 = setArray(M);
        I2 = setArray(M);

        K = new int[n];
        for (int i = 0; i < n; i++) {
            K[i]=i+1;
        }
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

    private void setKesiandStartAge(){
        for (int i = 0; i < n; i++) {
            startAges[i] = 2;
            if(i==0){kesi[i]=1;}
        }
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
                        System.out.println("t="+i+"\t p2_i="+probs[kk][i]);

                }
                probs[kk][Math.max(T,M)] = 1.0 - sum;
            }
        }
        return probs[k][x];
    }

    /**
     * Returning the descretised Weibull dist
     * Here, de CDF is F(x)=1-exp((-x/alpha)^beta). Then P(X=x)=F(x+1)-F(x)
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


    /**
     * Return value for x in Weibull distribution. As we had F(x) as input, and x as output, we use the inverse of the weibull distribution.
     * Inverse: x = Math.ceil(alpha * (ln(1-F(x))^(1/beta) )
     * @param i, the type of component that has its own weibull characteristics.
     * @param startAge, the starting age of the component. The random number is scaled so that the age of starting age or below can not be chosen
     * @return failure time of component type i, startAge already taken into account by substraction.
     */
    public int inverseWeibull(int i, int startAge){
        double Fx_forStartAge = 1.0 - Math.exp(-1.0 * Math.pow(startAge/alpha[i], beta[i]));
        System.out.println("startAge="+startAge+"\t alpha[i]="+alpha[i]+"\tbeta[i]="+beta[i]+" Fx="+Fx_forStartAge);

        double random = Fx_forStartAge + (1.0 - Fx_forStartAge ) *this.random.nextDouble();
        int x = (int) Math.ceil(alpha[i] * Math.pow( - Math.log(1-random),  1.0/beta[i])) - startAge;
        System.out.println(x);
        return x;
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

