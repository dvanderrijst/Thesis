package Zhu;

import Main.Instance;

public class Scenario {
    public int w;
    public int[][] scenario;
    public double probability;
    public Instance instance;

    public Scenario(int w, int[][] scenario, double probability, Instance instance) {
        this.w = w;
        this.scenario = scenario;
        this.probability = probability;
        this.instance = instance;
    }

    public void printScenario(){
        for (int i = 0; i < instance.n ; i++) {
            for (int r = 0; r < instance.q; r++) {
                System.out.println("i="+i+", r="+r+", lifetime="+scenario[i][r]);
            }
        }
    }
}
