package SchoutenOneComp;
import Main.Instance;
import ilog.concert.IloException;

import java.io.File;
import java.io.IOException;

public class MainS1C {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();

        ModelARP arp = new ModelARP(instance, "arpTest.txt");
        Policy policy = new Policy(new File("arpTest.txt"), instance);
        TransitionMatrix matrix = new TransitionMatrix(instance, policy);

        delta delta = new delta(matrix, policy, instance);
        double deltaValue = delta.getdelta(1, 1, 2, 2);
        System.out.println(deltaValue);
    }
}
