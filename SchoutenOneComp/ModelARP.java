package SchoutenOneComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;
/**
 * This class models the p-ARP for a single component. It mainly uses the methods from the p-BRP model.
 *
 * @author 619034dr Donna van der Rijst
 */
public class ModelARP extends ModelBRP {
    /**
     * Constructor for the ModelARP class
     * @param i Instance containing all important parameters
     * @param fileName to write output and found policy in.
     * @throws IloException as we work with CPLEX.
     * @throws IOException as we work with an input file. This might raise errors.
     */
    public ModelARP(Instance i, String fileName) throws IloException, IOException {
        super(i, fileName);
    }

    /**
     * Overrides the printSoltution as we want to put the cplex .lp file under a different name.
     * @throws IloException as we work with a CPLEX model
     */
    @Override
    public void printSolution() throws IloException {
        cplex.exportModel("model1CompARP.lp");
//        cplex.setOut(null);
        cplex.solve();
        double averageCosts = cplex.getObjValue();
        System.out.println("Yearly costs are " + averageCosts*N);

        printX();
    }

    /**
     * Overrides setConstraints, as the p-ARP uses only constraints 10b and 10e.
     * @throws IloException as we work with a CPLEX model
     */
    @Override
    public void setConstraints() throws IloException {
        constraint10b();
        constraint10e();
    }

}
