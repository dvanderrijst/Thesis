package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;

public class MainS2C {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();

        ARPgetFullPolicy getPolicy = new ARPgetFullPolicy(instance, "policiesARP2_1");
//        ModelMBRP arp = new ModelMBRP(instance, "test.txt");
//        arp.doStart();
    }
}

