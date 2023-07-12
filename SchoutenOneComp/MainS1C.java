package SchoutenOneComp;
import Main.Instance;
import ilog.concert.IloException;

import java.io.File;
import java.io.IOException;

public class MainS1C {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();

        ModelARP arp = new ModelARP(instance, "arpTest.txt");
        System.out.println("\n\n\n");
        ARP_KallenbergDual kdual = new ARP_KallenbergDual(instance, 0.999999);


//        Policy policy = new Policy(new File("arpTest.txt"), instance);
//        delta delta = new delta(policy, instance);



    }
}
