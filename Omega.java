/**
 * As the article in Schouten also a discretised Weibull distribution is used, we will do the same.
 * Here, de CDF is F(x)=1-exp((x/alpha)^beta). Then P(X=x)=F(x)-F(x-1)
 * The number of scenarios is (T+1)^n. So for T=10, and n=3, we already have 1331 scenarios.
 * We say that the components fail independently.
 */
public class Omega {
    private int T;
    private int[][][] Twir;
    private int n;
    private int q;
    private int alpha;
    private int beta;
    public int lenghtOmega;
    private static double[] probs;
    public Omega(ZhuInstance instance) {
        this.T = instance.T;
        this.n = instance.n;
        this.q = instance.q;
        this.alpha = instance.alpha;
        this.beta = instance.beta;
        this.lenghtOmega = instance.lengthOmega;
        this.Twir = new int[lenghtOmega][n][q];
    }

    public void createScenarios(){
        int size = (int) Math.pow(T+1, n*q);
        int[][] s = new int[size][n*q];
        int V = T+1;

        for(int i = 0; i<n*q ; i++) {
            int R = (int) Math.pow(T+1, i);
            int S = (int) Math.pow(T+1, n*q - i);
            int D = (int) Math.pow(T+1, n*q - i - 1);

            for (int r = 0; r < R; r++) {
                for (int v = 0; v < V; v++) {
                    int d = 0;
                    while (d < D) {
                        int i1 = r * S + v * D + d;
                        s[i1][i] = v+1;
                        d++;
                    }
                }
            }
        }
//        System.out.print("printing the results:");
//        for (int i = 0; i < size; i++) {
//            System.out.println();
//            for (int j = 0; j < n; j++) {
//                System.out.print(s[i][j]+"\t");
//            }
//        }
        System.out.println("Distributing them between components n");

        for (int w = 0; w < size ; w++) {
            int nq = 0;
            for (int r = 0; r < q; r++) {
                int i = 0 ;
                while(i<n){
                    Twir[w][i][r]=s[w][nq];
                    i++;
                    nq++;
                }
                i = 0;
            }
            nq = 0;
        }

        for (int r = 0; r < q; r++) {
//            System.out.println("For r = "+r+" we have matrix T[w][i]");
            for (int w = 0; w < size; w++) {
                for (int i = 0; i < n; i++) {
//                    System.out.print(Twir[w][i][r]+"\t");
                }
//                System.out.println();
            }
        }


        System.out.println("Now we are linking the probabilities to it....");
        double[] p_w = new double[size];

        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            double probability = 1.0;
            for (int j = 0; j < n*q; j++) {
                probability = probability * P_X(s[i][j]);
            }
            p_w[i] = probability;
            sum = sum + probability;
        }
        System.out.println("The sum is "+sum+" if this is not equal to one, something goes wrong here.");
    }

    /**
     * Returning the descretised Weibull dist:
     * Here, de CDF is F(x)=1-exp((x/alpha)^beta). Then P(X=x)=F(x)-F(x-1)
     * @param x
     * @return P(X=x)
     */
    private double P_X(int x){
        if(probs == null){
            probs = new double[T+2];
            double sum = 0.0;

            for (int i = 1; i <= T ; i++) {
                double hi = Math.pow(i / (double) alpha, beta);
                double F_x = 1.0 - Math.exp(-1*hi);
                double hii = Math.pow((i - 1) / (double) alpha,beta);
                double F_x_1 = 1.0 - Math.exp(-1*hii);
                probs[i] = F_x - F_x_1;
                sum = sum + probs[i];
            }
            System.out.println(sum);
            probs[T+1] = 1.0 - sum;
        }
        return probs[x];
    }

    public int[][][] getTwir(){
        return Twir;
    }

    public int[][][] getTir(int w){
        int[][][] Tir = new int[1][n][q];
        for (int i = 0; i < n; i++) {
            for (int r = 0; r < q; r++) {
                Tir[0][i][r]=Twir[w][i][r];
            }
        }
        return Tir;
    }
}
