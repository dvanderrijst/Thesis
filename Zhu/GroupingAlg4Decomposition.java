package Zhu;

import Main.Instance;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GroupingAlg4Decomposition extends GroupingAlg4{
    public final double[][][] Wwit;
    public final double[][] Xit_average;
    public final int w;
    public final double penalty;
    public GroupingAlg4Decomposition(List<Individual> K, int iota, Instance instance, int[][][] Xwit, int w, double[][][] Wwit, double[][] Xit_average, double penalty) {
        super(K, iota, instance);
        this.Wwit = Wwit;
        this.Xit_average = Xit_average;
        this.w = w;
        this.penalty = penalty;
    }
    @Override
    public void resetFinalCosts() {
        //first, we add the penaltycosts and maintenance costs
        double costs = 0.0;
        Set<Integer> countUniqueTaus = new LinkedHashSet<>();
        for (Individual individual : K) {
            double costsIndividualMaintenance = instance.cPR_i[individual.getTau(m) % instance.N];
            double costsIndividualPenalty = individual.getPenaltyCosts(m);
            costs = costs + costsIndividualMaintenance + costsIndividualPenalty ;
//            System.out.println("add for tau ="+individual.getTau(m));

            countUniqueTaus.add(individual.getTau(m));
        }
        costs = costs + d * countUniqueTaus.size();


        //then we convert it to a temproary X vector so we can use the Wwit values.
        int[][] Xit = new int[instance.n+1][instance.T+1];
        for(Individual individual : K) {
            if (individual.getTau(m) < instance.T + 1) {
                Xit[individual.i()][individual.getTau(m)]++;
                if (Xit[individual.i()][individual.getTau(m)] > 1) {
                    System.out.println("Xit takes the value of 2, meaning that once a replacement is done it is immediately replaced again, which ofcourse doesn't make sense.");
                }
            }
            else{
                System.out.println("if tau is larger than T, we should also adjust the for loop but in general this is not possible.");
            }
        }

        //fill up z values located at row instance.n
        for (int t = 0; t < instance.T+1 ; t++) {
            for (int i = 0; i < instance.n; i++) {
                if (Xit[i][t]==1) {
                    Xit[instance.n][t] = 1;
                }
            }
        }

        //now we can add the W and Xbar costs
        //adding WX and rho/2||X-Xbar|| as well
        for (int t = 0; t < instance.T+1; t++) {
            for (int i = 0; i < instance.n + 1; i++) {
                costs = costs + Wwit[w][i][t]*Xit[i][t] + 0.5*penalty* Math.abs(Xit[i][t]-Xit_average[i][t]) ;
            }
        }

        finalCosts = costs;
    }
}
