package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

public class ModelARP_2Comp extends ModelMBRP_2comp{
    public ModelARP_2Comp(Instance i) throws IloException {
        super(i);
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

        printActionGridComp1();
    }

    @Override
    public void setConstraints() throws IloException {
        setConstraint9b();
        setConstraint9c();
    }
}
