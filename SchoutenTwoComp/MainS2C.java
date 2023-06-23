package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;

public class MainS2C {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();

//        ModelARP arp = new ModelARP(instance, "SchoutenTwoComp/policies/ARPonebyone/PurePolicies/policies.txt");
//        arp.doStart();
        ARPgetFullPolicy getPolicy = new ARPgetFullPolicy(instance, "SchoutenTwoComp/policies/ARPonebyone/AllPolicies");

    }
}

