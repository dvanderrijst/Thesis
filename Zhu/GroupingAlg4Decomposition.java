package Zhu;

import Main.Instance;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GroupingAlg4Decomposition extends GroupingAlg4{
    public GroupingAlg4Decomposition(List<Individual> K, int iota, Instance instance, int[][][] Xwit, int w, double[][][] Wwit, double[][][] Xit_average) {
        super(K, iota, instance);
    }
    @Override
    public void resetFinalCosts() {









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
        finalCosts = costs;
    }
}
