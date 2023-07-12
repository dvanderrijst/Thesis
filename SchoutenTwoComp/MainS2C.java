package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;

public class MainS2C {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();

        ModelARP arp = new ModelARP(instance, "SchoutenTwoComp/test.txt");
        arp.doStart();

        ARP_KallenbergDual arp2Dkallenberg = new ARP_KallenbergDual(instance, 0.9999999999);
//        ARP_KallenbergDual arp2Dkallenberg = new ARP_KallenbergDual(instance, 0.99);






//        ARPgetFullPolicy getPolicy = new ARPgetFullPolicy(instance, "SchoutenTwoComp/policies/ARPonebyone/AllPolicies");

    }
}

