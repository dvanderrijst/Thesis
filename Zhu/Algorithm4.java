package Zhu;

import Main.Instance;

import java.util.*;

/**
 * This class is created to run Algorithm 4 for one scenario of the paper Zhu 2021. In step 2.2, Grouping Rule is applied. This is a seperate class.
 */
public class Algorithm4 {
    public final int[] deltas = new int[]{0}; //dit mag geen 0 zijn toch? dan wordt het corrective.
    public final int[] iotas;
    public final List<Individual> scenario;
    public int index = 0 ;
    public final Instance instance;
    public final Map<Integer, int[][]> keepTrackGroups = new HashMap<>();
    public final Map<Integer, Double> keepTrackCosts = new HashMap<>();
    public int[][][] Xwit;
    public int w;

    public Algorithm4(int[][] lifetimes, Instance instance, int[][][] Xwit, int w) {
        List<Individual> scenario = new ArrayList<>();
        for (int i = 0; i < instance.n ; i++) {
            for (int r = 0; r < instance.q ; r++) {
                Individual individual = new Individual(i, r, lifetimes[i][r], instance);
                scenario.add(individual);
            }
        }
        this.scenario = scenario;
        this.instance = instance;
        this.Xwit     = Xwit;
        this.w = w;

        iotas = new int[instance.T];
        for (int i = 0; i < instance.T; i++) {
            iotas[i] = i+1;
        }
    }

    public int[][][] doAlgorithm4() {
        for(int delta : deltas){
            for(int iota : iotas){
                scenario.get(0).initializeAllBetas(instance.n, instance.q);
//                System.out.print("\ndelta="+delta+" and iota="+iota+"\t\t\t\t");
                doStep2(delta, iota);
                cleanIndividuals();
            }
        }
        Xwit = returnBestSolution(Xwit, w);
        return Xwit;
    }

    private void cleanIndividuals() {
        for(Individual individual : scenario){
            individual.setBetaPrime(-1);
        }
    }


    private void doStep2(int delta, int iota) {
        int r = 0;
        boolean stop = false;

        //step 2.0
        assignTentativeReplacementTimes(delta);

        //step 2.1
        List<Individual> K = createK(r);

        while(r < instance.q && K.size() !=0) {
            //step 2.2
            step2_2(K, iota, instance);

            //step 2.3
            K.clear();
            r++;
            assignTentativeReplacementTimes(delta);

            K = createK(r);
        }

        index++;
        saveFinalGroups();
//        printFinalGroups();
    }

    public void step2_2(List<Individual> K, int iota, Instance instance) {
        Algorithm4_Grouping group = new Algorithm4_Grouping(K, iota, instance);
        group.doGrouping();
    }


    private void assignTentativeReplacementTimes(int delta) {
        for (int i = 0; i < instance.n ; i++) {
            for (int r = 0; r < instance.q ; r++) {
                Individual individual = getIndividual(i, r);
                if(individual.betaPrime == -1) {
                    if(r == 0 ) {
                        individual.setBeta(individual.lifetime - delta);
                        individual.setAllBetas(i,r,individual.lifetime - delta);
                    }
                    else if (r > 0) {
                        Individual prevIndividual = getIndividual(i, r - 1);
                        int prevBeta;
                        if( prevIndividual.betaPrime != -1 ) {
                            prevBeta = prevIndividual.betaPrime;
                        } else {
                            prevBeta = prevIndividual.getBeta();
                        }
                        individual.setBeta(prevBeta + individual.lifetime - delta);
                        individual.setAllBetas(i,r,prevBeta + individual.lifetime - delta);
                    }
                }
            }
        }
    }

    private void saveFinalGroups() {
        int[][] Xit = new int[instance.n+1][instance.T+1];

        for(Individual individual : scenario) {
            if (individual.getBetaPrime() < instance.T + 1) {
                Xit[individual.i()][individual.getBetaPrime()]++;
                if (Xit[individual.i()][individual.getBetaPrime()] > 1) {
//                    System.out.println("Xit takes the value of 2, meaning that once a replacement is done it is immediately replaced again, which ofcourse doesn't make sense.");
                }
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

        double costs2 = calculateCosts(Xit);

        keepTrackGroups.put(index, Xit);
        keepTrackCosts.put(index, costs2);
    }

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
        return costs;
    }

    private void printFinalGroups() {
        for(Individual individual : scenario){
            System.out.println(individual+" with i="+individual.i()+" \tr="+individual.r()+" has betaPrime="+individual.getBetaPrime());
        }
    }

    private List<Individual> createK(int r) {
        List<Individual> selectedIndividuals = new ArrayList<>();
         for (Individual individual : scenario) {
             if (individual.r() == r) {
                 if(individual.getBeta() <= instance.T){
                 selectedIndividuals.add(individual);}
             }
         }
        return selectedIndividuals;
    }

    private Individual getIndividual(int i, int r) {
        for (Individual individual : scenario) {
            if (individual.i() == i && individual.r() == r) {
                return individual;
            }
        }
        System.out.println("There is no such individual");
        System.exit(1);
        return null;
    }
    private int[][][] returnBestSolution(int[][][] Xwit, int w) {
        Integer minKey = null;
        Double minValue = null;
        for (Map.Entry<Integer, Double> entry : keepTrackCosts.entrySet()) {
            if (minValue == null || entry.getValue() < minValue) {
                minKey = entry.getKey();
                minValue = entry.getValue();
            }
        }

        //just for printing
        if (minKey != null) {
//            System.out.println("Key with the lowest value: " + minKey);
        } else {
            System.out.println("The map is empty.");
        }
//        System.out.println("these groups have the lowest costs="+keepTrackCosts.get(minKey));
        {
            for (int i = 0; i < instance.n + 1 ; i++) {
                for (int t = 0; t < instance.T + 1 ; t++) {
//                    System.out.print(keepTrackGroups.get(minKey)[i][t]+"\t");
                }
//                System.out.println();
            }
        }

        //filling in Xwit by setting it equal to the best Xit in keeptrackgroups.
        for (int i = 0; i < instance.n + 1 ; i++) {
            for (int t = 0; t < instance.T + 1; t++) {
                Xwit[w][i][t] = keepTrackGroups.get(minKey)[i][t];
            }
        }

//        printXwit(Xwit, w);

        return Xwit;
    }

    private void printXwit(int[][][] Xwit, int w) {
        for (int i = 0; i < instance.n + 1 ; i++) {
            for (int t = 0; t < instance.T + 1 ; t++) {
                System.out.print(Xwit[w][i][t]+"\t");
            }
            System.out.println();
        }
    }
}
