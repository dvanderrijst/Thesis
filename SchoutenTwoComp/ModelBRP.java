package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloException;

import java.io.IOException;

public class ModelBRP extends ModelMBRP {
    public ModelBRP(Instance i, String fileName) throws IloException, IOException {
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


        printActionGridComp1();
    }

    @Override
    public void setConstraints() throws IloException {
        setConstraint9b();
        setConstraint9c();
        setConstraintsBRP();
    }

    private void setConstraintsBRP() throws IloException {
            //10c & 10d
            for(int i0 : I0) {
                for (int i1 : I1) {
                    for (int i2 : I2) {
                        if (i1 != 0 & i2 != 0 & i1!=M-1 & i2!=M-1) {
                        constraints.add(cplex.addLe(cplex.diff(x[i0][i1][i2][1], y[1][i0]), 0.0, "10d," + i0 + "," + i1));
                        constraints.add(cplex.addLe(cplex.diff(x[i0][i1][i2][2], y[2][i0]), 0.0, "10d," + i0 + "," + i1));

                        constraints.add(cplex.addLe(cplex.diff(x[i0][i1][i2][3], y[1][i0]), 0.0, "10d," + i0 + "," + i1));
                        constraints.add(cplex.addLe(cplex.diff(x[i0][i1][i2][3], y[2][i0]), 0.0, "10d," + i0 + "," + i1));
                        }
                        constraints.add(cplex.addLe(cplex.sum(x[i0][i1][i2][0], y[1][i0]), 1.0, "10c1," + i0 + "," + i1));
                        constraints.add(cplex.addLe(cplex.sum(x[i0][i1][i2][0], y[2][i0]), 1.0, "10c2," + i0 + "," + i1));

                        constraints.add(cplex.addLe(cplex.sum(x[i0][i1][i2][1], y[2][i0]), 1.0, "9f" + i0 + "," + i1 + "," + i2 + ",k=" + 1 + "a=1"));
                        constraints.add(cplex.addLe(cplex.sum(x[i0][i1][i2][2], y[1][i0]), 1.0, "9f" + i0 + "," + i1 + "," + i2 + ",k=" + 2 + "a=1"));
                    }
                }
            }

    }
}

