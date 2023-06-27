package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;

public class ModelARP extends ModelBRP {
    public ModelARP(Instance i, String fileName) throws IloException, IOException {
        super(i, fileName);
    }

    @Override
    public void printSolution() throws IloException {
        cplex.exportModel("model1CompARP.lp");
//        cplex.setOut(null);
        cplex.solve();
        double averageCosts = cplex.getObjValue();
        System.out.println("Yearly costs are " + averageCosts*N);

        printX();
    }

    @Override
    public void setConstraints() throws IloException {
        constraint10b();
        constraint10e();
    }

}
