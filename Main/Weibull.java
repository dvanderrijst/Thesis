package Main;

public class Weibull {
    private final Instance instance;

    public Weibull(Instance instance) {
        this.instance = instance;
    }

    /**
     * Creates the array probs[][] if this is not done yet. This method is created so that not everytime all the probabilities have to be calculated by the computer.
     *
     * @param x lifetime x
     * @param k component k
     * @return P(X = x) in vector notation, probs[x] is P(X=x)
     */
    public double probX_x_k(int x, int k) {

        if (Instance.probs == null) {
            int sizeProbs = Math.max(instance.getT(), instance.getM() + 1);
            Instance.probs = new double[3][sizeProbs]; //k={1,2} so we use 3 we can use k=1 and k=2.
            for (int kk : instance.getK()) {
                double sum = 0.0;

                for (int i = 0; i < sizeProbs - 1 ; i++) {
                    Instance.probs[kk][i] = pXweibull(i, kk);
                    sum = sum + Instance.probs[kk][i];
                    System.out.println("t=" + i + "\t p2_i=" + Instance.probs[kk][i]);
                }
                Instance.probs[kk][sizeProbs - 1] = 1.0 - sum;
            }
        }
        return Instance.probs[k][x];
    }

    /**
     * Returning the descretised Weibull dist
     * Here, de CDF is F(x)=1-exp((-x/alpha)^beta). Then P(X=x)=F(x+1)-F(x)
     *
     * @param x in P(X=x), can go from 0 to infinity
     * @param k is the component number. k ={1,2}
     * @return P(X = x)
     */
    public double pXweibull(int x, int k) {

        double expo1 = Math.pow((x + 1.0) / (double) instance.getAlpha()[k - 1], instance.getBeta()[k - 1]);
        double F_x1 = 1.0 - Math.exp(-1.0 * expo1);

        double expo2 = Math.pow((x) / (double) instance.getAlpha()[k - 1], instance.getBeta()[k - 1]);
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
    public double probCondX_x_k(int i_k, int k) {
        double sum = 0.0;
        //first calculate P(X >= i_k) which is 1 - P(X<i_k)
        for (int i = 0; i < i_k; i++) {
            sum = sum + probX_x_k(i, k);
        }
        double denominator = 1.0 - sum;

        //then calculate P(X=i_k)
        double numerator = probX_x_k(i_k, k);
        return numerator / denominator;
    }

    /**
     * Return value for x in Weibull distribution. As we had F(x) as input, and x as output, we use the inverse of the weibull distribution.
     * Inverse: x = Math.ceil(alpha * (ln(1-F(x))^(1/beta) )
     *
     * @param i,        the type of component that has its own weibull characteristics.
     * @param startAge, the starting age of the component. The random number is scaled so that the age of starting age or below can not be chosen
     * @return failure time of component type i, startAge already taken into account by substraction.
     */
    public int inverseWeibull(int i, int startAge) {
        double Fx_forStartAge = 1.0 - Math.exp(-1.0 * Math.pow(startAge / instance.getAlpha()[i], instance.getBeta()[i]));
//        System.out.println("startAge=" + startAge + "\t alpha[i]=" + instance.getAlpha()[i] + "\tbeta[i]=" + instance.getBeta()[i] + " Fx=" + Fx_forStartAge);
//
        double random = Fx_forStartAge + (1.0 - Fx_forStartAge) * Instance.random.nextDouble();

//       This is exactly how they do it in the paper. HOWEVER, we now take the rounded value, and not the ceil. What does this mean for our benadering naar Schouten
//        int x = Math.max(1,(int) Math.round(instance.getAlpha()[i] * Math.pow(-Math.log(1 - random), 1.0 / instance.getBeta()[i])) - startAge);

        //This is how we do it, as also described in the appendix
        int x = (int) Math.ceil(instance.getAlpha()[i] * Math.pow(-Math.log(1 - random), 1.0 / instance.getBeta()[i])) - startAge;
        System.out.println(x);


//        System.out.println(x);
        return x;
    }
}