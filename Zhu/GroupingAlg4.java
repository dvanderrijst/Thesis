package Zhu;

import Main.Instance;

import java.util.*;

/**
 * This class in the Grouping Rule needed in step 2.2 in Algorithm 4 in the paper. The body of this class is doGrouping().
 * Furthermore, the class variables are the following:

 * groups - a list with the computed groups. These groups change for different values of m.
 * groupCosts - a map to document the groupCosts found for each value of m.
 * iota - iota value used in step 2.3, received from Algorithm 4.
 * v - starting position of individual for a group.
 * t - time of the individual at location t, equal to K.get(v-1).getBeta().
 * vPrime - used in step 2.2 and 2.3
 * instance - Instance used
 * m - grouping option indication value
 * d - costs for doing maintenance, the economic dependence variable
 * iota - iota value used in step 2.3, received from Algorithm 4.
 * K - the set received from Algorithm 4 and the set to create groups from.
 * finalCosts - updated for every iota value, needed in Algorithm 4.
 */
public class GroupingAlg4 {
    public  final List<List<Individual>> groups = new ArrayList<>();
    public  final Map<Integer, Double> groupCosts = new HashMap<>();
    public  int v;
    public  int t;
    public  int vPrime;

    public  int m;
    public  final int d;
    public double finalCosts;
    // for Algorithm 4
    public  final int iota;
    public  final List<Individual> K;
    public  final Instance instance;


    public GroupingAlg4(List<Individual> K, int iota, Instance instance) {
        this.K = K;
        this.iota = iota;
        this.instance = instance;
        m = 1;
        d = instance.d;
    }

    /**
     * This method is the body of this class. It works as the following:
     * Step 1: sorts set K in ascending order based on beta.
     * Step 2: for each m=1 to m=|K|-1, we compute group costs
     * Step 2.1: it initializes the starting individual v to create the first group.
     * Step 2.2: add other individuals that are in the iota time interval.
     * Step 2.3: update tau values of each individual, and updates v to the next starting individual. This is configured in the while loop statement.
     * Step 2.4: if this starting individual falls besides K, we are done and compute the costs. If not, we go to step 2.2.
     * Step 2: We find the vaue for m that configures the cheapest set.
     * Step 3: We set the best final tau values for each individual. We need this in the Algorithm4 class.
     */
    public void doGrouping() {

        //step 1
        K.sort(Comparator.comparingInt(Individual::getBeta));

        //step 2
        while (m <= K.size()) {
//            System.out.println("m="+m);
            //step 2.1
            v = m ;

            while(v <= K.size()){
                //step 2.2
                step2_2();
                //step 2.3
                step2_3();
            }

            //step 2.4
            resetFinalCosts();
//            System.out.print("costs="+finalCosts+" for ");
//            printGroups();
            groupCosts.put(m, finalCosts);
            groups.clear();
            m++;
        }
//        System.out.println("created all groups");

        //step 2
        Map.Entry<Integer, Double> minEntry = Collections.min(groupCosts.entrySet(), Map.Entry.comparingByValue());
        if (minEntry == null) {
            System.out.println("minEntry is null, so we cannot find the value for m.");
            System.exit(1);
        } else {
//            System.out.println("found the minEntry and resetting the costs");
            m = minEntry.getKey();
            resetFinalCosts();
        }

        //step 3
        setFinalBetaPrime();
    }

    /**
     * Step 2.2: add other individuals that are in the iota time interval.
     */
    private void step2_2() {
        List<Individual> group = new ArrayList<>(Collections.singleton(K.get(v - 1)));
        t = K.get(v-1).getBeta();
        vPrime = v + 1;

        while (vPrime <= K.size() && K.get(vPrime - 1).getBeta() <= t + iota) {
            group.add(K.get(vPrime - 1));
            vPrime++;
        }

        groups.add(group);
    }

    /**
     * Step 2.3: update tau values of each individual, and updates v to the next starting individual. This is configured in the while loop statement.
     */
    private void step2_3() {
        int varTheta = vPrime - 1;
        for (vPrime = v; vPrime <= varTheta; vPrime++) {
            K.get(vPrime - 1).setTau(m, t);
            K.get(vPrime - 1).setPenaltyCosts(m, t);
        }
        v = varTheta + 1;
    }

    /**
     * This method is used in the Algorithm 4 to get the finalCosts. As for different iota's these differ, we need to reset it for every iota.
     */
    public void resetFinalCosts() {
        int[][] Xit = new int[instance.n+1][instance.T+1];

        for(Individual individual : K) {
            if (individual.getTau(m) < instance.T + 1) {
                Xit[individual.i()][individual.getTau(m)]++;
                if (Xit[individual.i()][individual.getTau(m)] > 1) {
                    System.out.println("Xit takes the value of 2, meaning that once a replacement is done it is immediately replaced again, which ofcourse doesn't make sense.");
                }
            }
        }
        for (int t = 0; t < instance.T+1 ; t++) {
            for (int i = 0; i < instance.n; i++) {
                if (Xit[i][t]==1) {
                    Xit[instance.n][t] = 1;
                }
            }
        }

//        printXit(Xit);


//      public double calculateCosts(int[][] Xit) {
        double costs2 = 0.0;

        //setup costs d
        for (int time = 0; time < instance.T+1; time++) {
            costs2 = costs2 + instance.d * Xit[instance.n][time];
        }

        //maintenance costs and penalty costs
        for(Individual individual : K){
            int i = individual.i;
            int t = individual.getTau(m);

            if(t<instance.T+1) {
                costs2 = costs2 + instance.cPR_i[t % instance.N] * Xit[i][t] + individual.getPenaltyCosts(m) * Xit[i][t];
            }
        }
        finalCosts = costs2;
//        System.out.println("Costs ="+costs2);
//        System.out.println("\n\n");
    }

    private void printXit(int[][] Xit) {
        //jsut for printing
        for (int i = 0; i < instance.n+1; i++) {
            for (int t = 0; t < instance.T + 1; t++) {
                System.out.print(Xit[i][t]+"\t");
            }
            System.out.println();
        }
    }

    /**
     * Step 3: reset all the beta prime values of each individual. If an individual did not receive a tau because it was in the beginning of K, the original beta is used.
     */
    private void setFinalBetaPrime() {
        for(Individual individual : K){
            individual.setBetaPrime(individual.getTau(m));
            individual.setAllBetas(individual.i, individual.r, individual.getTau(m));
        }
    }

    /**
     * Used when interested in how the groups look like.
     */
    private void printGroups() {
        for(List<Individual> group : groups){
            System.out.print("Group:\n");
            for(Individual individual : group){
                System.out.print(individual.i()+":\tt="+individual.getTau(m)+"\n");
            }
        }
    }
    private Individual getIndividual(int i, int r) {
        for (Individual individual : K) {
            if (individual.i() == i && individual.r() == r) {
                return individual;
            }
        }
        System.out.println("There is no such individual");
        System.exit(1);
        return null;
    }
}
