package Zhu;

import Main.Instance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * As the article in Schouten also a discretised Weibull distribution is used, we will do the same.
 * Here, de CDF is F(x)=1-exp((x/alpha)^beta). Then P(X=x)=F(x)-F(x-1)
 * The number of scenarios is (T+1)^n. So for T=10, and n=3, we already have 1331 scenarios.
 * We say that the components fail independently.
 */
public class Omega {
    public int[][][] Twir;
    public final double[] p_w;
    public final Instance instance;
    public Omega(Instance instance) {
        this.instance = instance;
        this.Twir = new int[instance.lengthOmega][instance.n][instance.q];
        this.p_w = new double[instance.lengthOmega];
        createScenarios();
//        createScenariosFromZhu();
    }
    public Omega(Instance instance, int[][][] Twir, double[] p_w){
        this.instance = instance;
        this.Twir = Twir;
        this.p_w = p_w;
    }

    /**
     * This creates scenarios like it is done in the paper of Zhu. It computes variables according to the Weibull distribution and put it in a scenario.
     * The values for p(w) are all the same: 1/lengthOmega
     */
    public void createScenarios(){
        int[] kesi = instance.kesi;
        int[] startAges = instance.startAges;

        for (int omega = 0; omega < instance.lengthOmega; omega++) {
            for (int i = 0; i < instance.n; i++) {
                for (int r = 0; r < instance.q; r++) {
                    if(kesi[i]==1 && r==0){
                        Twir[omega][i][r] = 0 ;
                        System.out.println("Why is this happening? x_i may not be zero.");
                        System.exit(1);
                    }
                    else if(r==0){
                        Twir[omega][i][r] = instance.inverseWeibull(i, startAges[i]);}
                    else{
                        Twir[omega][i][r] = instance.inverseWeibull(i, 0);}
                }
            }
            p_w[omega] = 1.0 / instance.lengthOmega;
        }
    }

    public void createScenariosFromZhu(){
        String folderPath = "table1_2";

        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            int omega = -1;
            for (File file : files) {
                omega++;
                if (file.isFile() && file.getName().endsWith(".csv")) {
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        int i = 0;
                        while (i < instance.n) {
                            String line = br.readLine();
                            if (!line.equals("") && line != null) {
                                String[] values = line.split(",");
                                int r = 0;
                                while (r < instance.q) {
                                    Twir[omega][i][r] = (int) Double.parseDouble(values[r].trim());
                                    r++;
                                }
                                i++;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                p_w[omega] = 1.0 / instance.lengthOmega;
//            }
            }
        }
    }

    /**
     * This method is to only return one scenario of the whole scenario set, so we can use the PHA.
     * @param w the scenario
     * @return a very small scenario set, namelijk a scenario set containing only one scenario.
     */
    public int[][][] getTir(int w){
        int[][][] Tir = new int[1][instance.n][instance.q];
        for (int i = 0; i < instance.n; i++) {
            for (int r = 0; r < instance.q; r++) {
                Tir[0][i][r]=Twir[w][i][r];
            }
        }
        return Tir;
    }

    public List<Scenario> getScenarios(){
        List<Scenario> scenarios = new ArrayList<>();
        for (int w = 0; w < instance.lengthOmega; w++) {
            int[][] scenario = new int[instance.n][instance.q];
            for (int i = 0; i < instance.n; i++) {
                for (int r = 0; r < instance.q; r++) {
                    scenario[i][r]=Twir[w][i][r];
                }
            }
            Scenario scenarioObject = new Scenario(w, scenario, p_w[w], instance);
            scenarios.add(scenarioObject);
        }
        return scenarios;
    }
}
