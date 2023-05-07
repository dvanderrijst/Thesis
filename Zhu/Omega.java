package Zhu;

import Main.Instance;

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
    private static double[] p_w;
    private final Instance i;
    public Omega(Instance instance) {
        this.i = instance;
        this.T = i.T;
        this.n = i.n;
        this.q = i.q;
        this.alpha = i.alpha;
        this.beta = i.beta;
        this.lenghtOmega = i.lengthOmega;
        this.Twir = new int[lenghtOmega][n][q];

        createScenarios();
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
        p_w = new double[size];

        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            double probability = 1.0;
            for (int j = 0; j < n*q; j++) {
                probability = probability * this.i.P_Xarrray(s[i][j]);
            }
            p_w[i] = probability;
            sum = sum + probability;
        }
        System.out.println("The sum is "+sum+" if this is not equal to one, something goes wrong here.");
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

    public double getpw(int w){
        return p_w[w];
    }
}
