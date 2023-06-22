package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;

public class MainS2C {
    public static void main(String[] args) throws IloException, IOException {
        Instance instance = new Instance();

//        ModelMBRP mbrp = new ModelMBRP(instance, "SchoutenTwoComp/policies/MBRP/PurePolicies/policies.txt");
//        mbrp.doStart();
        MBRPgetFullPolicy getPolicy = new MBRPgetFullPolicy(instance, "SchoutenTwoComp/policies/MBRP/AllPolicies");

    }
}

