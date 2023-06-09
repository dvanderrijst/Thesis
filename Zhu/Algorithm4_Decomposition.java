package Zhu;

import Main.Instance;

import java.util.List;

public class Algorithm4_Decomposition extends Algorithm4{
    public final double[][][] Wwit;
    public final double[][] Xit_average;
    public final int w;
    public final double penalty;
    public int[][][] Xwit;

    public Algorithm4_Decomposition(int[][] lifetimes, Instance instance, int[][][] Xwit, int w, double[][][] Wwit, double[][] Xit_average, double penalty) {
        super(lifetimes, instance, Xwit, w);
        this.Wwit = Wwit;
        this.Xit_average = Xit_average;
        this.w = w;
        this.penalty = penalty;
        this.Xwit = Xwit;
    }

    @Override
    public void step2_2(List<Individual> K, int iota, Instance instance) {
        Algorithm4_GroupingDecomposition group = new Algorithm4_GroupingDecomposition(K, iota, instance, Xwit, w, Wwit, Xit_average, penalty);
        group.doGrouping();
    }
    @Override
    public double calculateCosts(int[][] Xit) {
        double costs = 0.0;
        for (int t = 0; t < instance.T+1; t++) {
            for (int i = 0; i < instance.n + 1; i++) {
                if (i == instance.n) {
                    costs = costs + instance.d * Xit[i][t];
                }
                else {
                    costs = costs + instance.cPR_i_t[i][t % instance.N ] * Xit[i][t];
                }
            }
        }

        //adding WX and rho/2||X-Xbar|| as well
        for (int t = 0; t < instance.T+1; t++) {
            for (int i = 0; i < instance.n + 1; i++) {
                costs = costs + Wwit[w][i][t]*Xit[i][t] + 0.5*penalty* Math.abs(Xit[i][t]-Xit_average[i][t]) ;
            }
        }
        return costs;
    }
}
