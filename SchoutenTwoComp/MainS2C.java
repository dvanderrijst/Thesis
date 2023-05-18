package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

public class MainS2C {
    public static void main(String[] args) throws IloException {

        Instance instance = new Instance();
        //Zhu



        //Schouten
        ModelMBRP hey = new ModelMBRP(instance);
        ModelMBRP_2comp modelarp2 = new ModelMBRP_2comp(instance);
    }

}
