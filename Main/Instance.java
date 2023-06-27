package Main;

import SchoutenOneComp.ModelBRP;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;

/**
 * This class contains all the data that are inputs for the models.
 * It also contains all methods related to the Weibull distributions.
 * It is chosen to put it all in one instance, to make sure some general components are equal.
 */
public class Instance {
    //ZHU and SCHOUTEN
    public final double[][] cPR_i_t;
    public final double[][] cCR_i_t;
//    public final double[] CR_average = new double[]{50.0, 50.0};
//    public final double[] PR_average = new double[]{10.0, 10.0};
    public final double[] CR_average = new double[]{50.0};
    public final double[] PR_average = new double[]{10.0};
    public final double delta = 0.2;
    public final int d       = 5;
//    public final int d       = 0;
    public final int m = 1; //5 years
    public final int N = 4; //12 months in one year
    public final int ThorizonZhu = 15;
    public final int T       = m*N ;
//    public final double[] alpha = new double[]{6.0, 12.0 };     //alpha is 1 year
//    public final double[] beta = new double[]{2.0, 2.0};
//    public final double[] alpha = new double[]{12.0, 12.0 };     //alpha is 1 year
//    public final double[] beta = new double[]{2.0, 2.0};
    public final double[] alpha = new double[]{6.0};     //alpha is 1 year
    public final double[] beta = new double[]{2.0};


    //ZHU instance
//    public final double CR_average_i = new double[]{14.4, 11.4, 9.4, 8.0, 11.1, 14.2, 7.4};
//    public final double alpha = new double[] {6.9,   5, 7.3, 4.8, 4.2, 4.5, 3.2};
//    public final double beta = new double[]  {6.5, 6.7, 5.4, 4.9, 4.8, 4.4, 5.5};

    //ZHU
    public static Random random = new Random(0);
    public final int q       = 6 ;
    public final int lengthOmega;
    public static double[][] probs; //probabilities for component k and time t : probs[k][t]
    public final int n       = 1 ;
    public final double sigma_SAA ;
    public final double epsilon_SAA ;
    public final double tau_SAA;
    public final double alpha_SAA;
    public static int[] startAges;
    public static int[] kesi ;
    public static int startTime;

    //SCHOUTEN
    public final int[] I0;
    public final int[] I1;
    public final int[] I2;
    public final int[] K ;
    public final int M = m*N;                //we set the maximum age to 3 years
    private final Weibull weibull = new Weibull(this);

    public Instance() {
        //ZHU AND SCHOUTEN
        cCR_i_t = setVariateCosts(CR_average);
        cPR_i_t = setVariateCosts(PR_average);

        //ZHU
        startTime = 0;
        startAges = new int[n];
        kesi = new int[n];
        sigma_SAA = 2*ThorizonZhu*(CR_average[0]*n+d);
        epsilon_SAA = 0.1 * sigma_SAA;
        tau_SAA = 0.1 * epsilon_SAA;
        alpha_SAA = 0.1;
        int sizeX = (int) Math.pow(2, n);
//        lengthOmega = (int) Math.round((2*Math.pow(sigma_SAA, 2)) / (Math.pow(epsilon_SAA - tau_SAA, 2)) * Math.log(sizeX/alpha_SAA));
//        System.out.println(lengthOmega);
lengthOmega = 100;
        //SCHOUTEN
        I0 = setArray(m*N);
        I1 = setArray(M+1);
        I2 = setArray(M+1);
        K = new int[n];
        for (int i = 0; i < n; i++) {
            K[i]=i+1;
        }
    }
    /**
     * sets the variate costs of c_p(i0) and c_f(i0) from the equation on page 985 from Schouten article
     * the value delta is the variation in maintenance costs (e.g. high wind difference throughout the year)
     * January has the highest costs, July the lowest.
     * @param averages maintenance costs
     * @return the array with variate costs.
     */
    private double[][] setVariateCosts(double[] averages) {
        double[][] array = new double[n][N];
        for (int i = 0; i < n; i++) {
            for (int t = 0; t < N; t++) {
                array[i][t] = averages[i] + delta * averages[i] * Math.cos(2 * Math.PI * t * (1.0 / N));
                System.out.print(array[i][t] + "\t");
            }
        }
        System.out.println();
        return array;
    }

    public void setKesiandStartAge(int[] ages){
        for (int i = 0; i < n; i++) {
            startAges[i] = ages[i];
//            if(i==0){kesi[i]=1;}
        }
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
     * Gives small p, which is p{i_k}=P(X=i_k|X\geq i_k) = P(X=i_k)/(1-sum_{x=0}^i_k P(X=i)
     *
     * @param i_k, age of component k
     * @param k
     * @return p^k{i_k}
     */
    public double probCondX_x_k(int i_k, int k){
        //first calculate P(X >= i_k) which is 1 - P(X<i_k)

        //then calculate P(X=i_k)
        return weibull.probCondX_x_k(i_k, k);
    }

    /**
     * Return value for x in Weibull distribution. As we had F(x) as input, and x as output, we use the inverse of the weibull distribution.
     * Inverse: x = Math.ceil(alpha * (ln(1-F(x))^(1/beta) )
     * @param i, the type of component that has its own weibull characteristics.
     * @param startAge, the starting age of the component. The random number is scaled so that the age of starting age or below can not be chosen
     * @return failure time of component type i, startAge already taken into account by substraction.
     */
    public int inverseWeibull(int i, int startAge){

        return weibull.inverseWeibull(i, startAge);
    }

//GETTTERS
    public int[] getK() {
        return K;
    }

    public int getM() {
        return m;
    }

    public int getN() {
        return N;
    }

    public int getT() {
        return T;
    }

    public double[] getAlpha() {
        return alpha;
    }

    public double[] getBeta() {
        return beta;
    }

    public void writeInfo(String fileName) {
        //document what is done
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write("\nm="+m);
            writer.write("\nN="+N);
            writer.write("\nT=mN="+T);
            writer.write("\nn="+n);
            writer.write("\nq="+q);
            writer.write("\nlengthOmega="+lengthOmega);
            writer.write("\nd="+d);
            writer.write("\nCR_average_i="+ Arrays.toString(CR_average));
            writer.write("\nPR_average_i="+ Arrays.toString(PR_average));
            writer.write("\nalpha="+ Arrays.toString(alpha));
            writer.write("\nbeta="+ Arrays.toString(beta));
            writer.write("\ndelta="+delta);
            writer.write("\n"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd\tHH:mm:ss")));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double piOneDim(int i0, int i1, int j0, int j1, int a){
        double pi_value = 0.0;

        if( j0 != (i0 + 1)%(m*N) ){
            // System.out.println("this values for j0 is not corresponding to j0 = i0 + 1 mod(N). We return pi=0.0.");
            pi_value = 0.0;
        }
        else if(a==0){
            if( (j1 == i1 + 1) & (i1!=0 & i1!= M) ) {
                pi_value = 1 - probCondX_x_k(i1, 1);
            }
            else if( (j1 == 0) & (i1!=0 & i1!= M) ) {
                pi_value = probCondX_x_k(i1, 1);
            }
            else{
                pi_value = 0.0;
            }
        }
        else if(a==1){
            if(j1==1) {
                pi_value = 1- probCondX_x_k(0, 1);
            }
            else if(j1==0) {
                pi_value = probCondX_x_k(0, 1);
            }
            else{
                pi_value = 0.0;
            }
        }
        return pi_value;
    }
}

