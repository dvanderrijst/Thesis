package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.io.IOException;

public class ModelARP_2Comp extends ModelMBRP_2Comp {
    public ModelARP_2Comp(Instance i, String fileName) throws IloException, IOException {
        super(i, fileName);
    }

    @Override
    public void printSolution() throws IloException {
        double averageCosts = cplex.getObjValue();
        System.out.println("Yearly costs are " + averageCosts*N);

        double sum = 0.0;
        for (int i0 : I0) {
            for (int i1 : I1) {
                for (int i2 : I2) {
                    int[] actions = A(i0,i1,i2);
                    for(int a : actions){
                        sum = sum + cplex.getValue(x[i0][i1][i2][a])*cComponentOne(i0,i1,i2,a);
                    }
                }
            }
        }
        System.out.println("Yearly cost for only the first component are "+sum*N);

        for (IloConstraint cons : constraints) {
            IloCplex.BasisStatus status = cplex.getBasisStatus(cons);
            if(status == IloCplex.BasisStatus.Basic) {
//                System.out.println("constraint " + cons.getName() + " has basic status of " + cplex.getBasisStatus(cons));
            }
        }

        printActionGridComp1();
    }

    @Override
    public void setConstraints() throws IloException {
        setConstraint9b();
        setConstraint9c();
    }
}
