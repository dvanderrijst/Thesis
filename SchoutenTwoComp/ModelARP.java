package SchoutenTwoComp;

import Main.Instance;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;

import static SchoutenTwoComp.ModelMBRP_2comp.constraints;

public class ModelARP extends ModelBRP {
    public ModelARP(Instance i) throws IloException {
        super(i);
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
//        addedConstraint_setXtozero();
    }




    private void addedConstraint_setXtozero() throws IloException {
        for(int i0 : I0){
            for(int i1 : I1){
                if(i1 != 0 & i1!=M-1){
                    cplex.addEq(x[i0][i1][1],0);
                }
            }
        }
    }
}
