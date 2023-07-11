package SchoutenOneComp;
import Main.Instance;
import ilog.concert.IloException;

import java.io.File;
import java.io.IOException;

public class MainS1C {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();

//        ARP_Kallenberg kallenberg = new ARP_Kallenberg(instance, 4, 10, 0.90);
//        System.out.println(kallenberg.getAction());

        ARP_KallenbergDual kallenbergDual = new ARP_KallenbergDual(instance, 4, 2, 0.90);
        kallenbergDual.startUp();
        System.out.println(kallenbergDual.getAction());



//        ModelARP arp = new ModelARP(instance, "arpTest.txt");

//        Policy policy = new Policy(new File("arpTest.txt"), instance);
//        delta delta = new delta(policy, instance);



    }
}
