package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

public class MainS2C {
    public static void main(String[] args) throws IloException {

        Instance instance = new Instance();
//        ModelBRP model1 = new ModelBRP(instance);
        ModelARP modelarp = new ModelARP(instance);
//        ModelARP_2Comp modelarp2 = new ModelARP_2Comp(instance);

        ModelBRP modelbrp = new ModelBRP(instance);
//        ModelBRP_2Comp modelbrp2 = new ModelBRP_2Comp(instance);

//        ModelARP modelarp = new ModelARP(instance);
        ModelMBRP modelmbrp = new ModelMBRP(instance);
//        ModelARP_2Comp modelarp2 = new ModelARP_2Comp(instance);
//        ModelMBRP_2comp modelmbrp2 = new ModelMBRP_2comp(instance);
    }

}
