package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

public class MainS2C {
    public static void main(String[] args) throws IloException {

        Instance instance = new Instance();
        ModelBRP model1 = new ModelBRP(instance);
        ModelMBRP model2 = new ModelMBRP(instance);
    }

}
